package com.rudresh.xpulse.core.data.remote

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
) {

    @Volatile
    private var token: String = ""

    val baseUrl: String get() = serverConfig.baseUrl

    suspend fun isServerReachable(): Boolean = runCatching {
        JSONObject(request("GET", "/health", null)).optString("status") == "ok"
    }.getOrDefault(false)

    fun setToken(value: String) {
        token = value
    }

    fun clearToken() {
        token = ""
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

    private suspend fun request(method: String, path: String, body: String?): String =
        withContext(Dispatchers.IO) {
            val connection = (URL("${serverConfig.baseUrl}$path").openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 15_000
                readTimeout = 70_000
                setRequestProperty("Accept", "application/json")
                if (token.isNotEmpty()) {
                    setRequestProperty("Authorization", "Bearer $token")
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
