package com.rudresh.xpulse.core.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class OcrMedicine(
    val name: String,
    val dose: String,
    val frequency: String,
)

@Singleton
class PrescriptionOcr @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun readLines(uriString: String): List<String> {
        val image = InputImage.fromFilePath(context, Uri.parse(uriString))
        val result = suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
        return result.textBlocks
            .flatMap { block -> block.lines }
            .map { it.text.trim() }
            .filter { it.isNotBlank() }
    }

    fun parseMedicines(lines: List<String>): List<OcrMedicine> =
        lines.mapNotNull { line -> parseLine(line) }.distinctBy { it.name.lowercase() }

    private fun parseLine(line: String): OcrMedicine? {
        val cleaned = line.replace(Regex("^[\\d]+[).\\-]\\s*"), "").trim()
        if (cleaned.length < 3) return null
        if (IGNORE_PATTERN.containsMatchIn(cleaned)) return null

        val strength = STRENGTH_PATTERN.find(cleaned)?.value.orEmpty()
        val frequencyToken = FREQUENCY_TOKENS.firstOrNull { cleaned.contains(it.first, ignoreCase = true) }
        val hasDrugSignal = strength.isNotBlank() || frequencyToken != null ||
            DOSE_FORM_PATTERN.containsMatchIn(cleaned)
        if (!hasDrugSignal) return null

        val name = cleaned
            .replace(FREQUENCY_STRIP_PATTERN, " ")
            .replace(Regex("\\s{2,}"), " ")
            .trim()
            .take(60)
        if (name.isBlank()) return null

        return OcrMedicine(
            name = name,
            dose = DOSE_FORM_PATTERN.find(cleaned)?.value?.replaceFirstChar { it.uppercase() } ?: "1 dose",
            frequency = frequencyToken?.second ?: "As directed",
        )
    }

    private companion object {
        val STRENGTH_PATTERN = Regex("\\d+\\s*(mg|mcg|ml|g|iu)\\b", RegexOption.IGNORE_CASE)
        val DOSE_FORM_PATTERN = Regex(
            "\\b(tablet|tab|capsule|cap|syrup|drops?|injection|inj|cream|ointment|sachet)\\b",
            RegexOption.IGNORE_CASE,
        )
        val IGNORE_PATTERN = Regex(
            "\\b(dr\\.?|clinic|hospital|patient|address|phone|date|signature|reg\\.?\\s*no|diagnosis|advice|follow\\s*up)\\b",
            RegexOption.IGNORE_CASE,
        )
        val FREQUENCY_STRIP_PATTERN = Regex(
            "\\b(1-0-1|1-1-1|0-0-1|1-0-0|0-1-0|1-1-0|od|bd|tds|qid|hs|sos|prn|twice|thrice|once|daily|morning|night|after food|before food)\\b",
            RegexOption.IGNORE_CASE,
        )
        val FREQUENCY_TOKENS = listOf(
            "1-1-1" to "Three times daily",
            "1-0-1" to "Twice daily",
            "1-1-0" to "Twice daily",
            "1-0-0" to "Once daily · morning",
            "0-0-1" to "Once daily · night",
            "0-1-0" to "Once daily · afternoon",
            "tds" to "Three times daily",
            "qid" to "Four times daily",
            "bd" to "Twice daily",
            "od" to "Once daily",
            "hs" to "At bedtime",
            "sos" to "As needed",
            "prn" to "As needed",
            "thrice" to "Three times daily",
            "twice" to "Twice daily",
            "once" to "Once daily",
        )
    }
}
