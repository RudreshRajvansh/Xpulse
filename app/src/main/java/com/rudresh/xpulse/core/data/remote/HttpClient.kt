package com.rudresh.xpulse.core.data.remote

import com.rudresh.xpulse.core.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HttpClient @Inject constructor(
    private val serverConfig: ServerConfig,
    private val sessionManager: SessionManager,
) {

    @Volatile
    private var token: String = ""

    val baseUrl: String get() = serverConfig.baseUrl

    suspend fun isServerReachable(): Boolean = runCatching {
        JSONObject(request("GET", "/health", null)).optString("status") == "ok"
    }.getOrDefault(false)

    @Synchronized
    fun setToken(value: String) {
        token = value
    }

    suspend fun revokeSession() {
        val current = token
        if (current.isEmpty()) return
        runCatching { request("POST", "/auth/logout", "{}", current) }
        clearTokenIf(current)
    }

    @Synchronized
    private fun clearTokenIf(expected: String): Boolean {
        if (token != expected) return false
        token = ""
        return true
    }

    suspend fun getObject(path: String): JSONObject = JSONObject(request("GET", path, null))

    suspend fun getArray(path: String): JSONArray = JSONArray(request("GET", path, null))

    suspend fun postObject(path: String, body: JSONObject = JSONObject()): JSONObject =
        JSONObject(request("POST", path, body.toString()))

    suspend fun postArray(path: String, body: JSONObject = JSONObject()): JSONArray =
        JSONArray(request("POST", path, body.toString()))

    suspend fun putObject(path: String, body: JSONObject): JSONObject =
        JSONObject(request("PUT", path, body.toString()))

    suspend fun deleteObject(path: String): JSONObject = JSONObject(request("DELETE", path, null))

    private suspend fun request(method: String, path: String, body: String?, bearer: String = token): String =
        withContext(Dispatchers.IO) {
            val connection = (URL("${serverConfig.baseUrl}$path").openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 15_000
                readTimeout = 70_000
                setRequestProperty("Accept", "application/json")
                if (bearer.isNotEmpty()) {
                    setRequestProperty("Authorization", "Bearer $bearer")
                }
                if (body != null) {
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                }
            }
            try {
                if (body != null) {
                    connection.outputStream.use { it.write(body.toByteArray()) }
                }
                val code = connection.responseCode
                if (code in 200..299) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    val raw = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                    if (code == HttpURLConnection.HTTP_UNAUTHORIZED && !path.startsWith("/auth/") && bearer.isNotEmpty()) {
                        if (clearTokenIf(bearer)) sessionManager.endSession()
                    }
                    throw IOException(errorMessage(raw, code))
                }
            } catch (e: IOException) {
                throw e
            } catch (e: Exception) {
                throw IOException("Cannot reach the server at ${serverConfig.baseUrl}", e)
            } finally {
                connection.disconnect()
            }
        }

    private fun errorMessage(raw: String, code: Int): String = try {
        JSONObject(raw).optString("error").ifBlank { "Request failed ($code)" }
    } catch (e: Exception) {
        "Request failed ($code)"
    }

}
