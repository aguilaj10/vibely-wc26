package com.vibely.wc26.feature.scan

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Thin coroutine wrapper around ML Kit's Latin TextRecognizer. Kept as a
 * singleton so the underlying detector model (≈4MB on first download, cached
 * after that via Play Services) stays warm across scans.
 *
 * Latin recognizer is sufficient for v1 — every player name in the WC26
 * catalog is Latin-script (including accented Spanish, Portuguese, etc.).
 */
@Singleton
class TextRecognitionSource @Inject constructor() {

    private val recognizer: TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Run OCR over [bitmap] and return the concatenated text. [rotationDegrees]
     * must be the image-rotation hint from CameraX (0/90/180/270) so ML Kit can
     * upright the frame internally — passing 0 on a sideways frame produces
     * garbage results.
     *
     * Returns an empty string when nothing is detected.
     */
    suspend fun recognize(bitmap: Bitmap, rotationDegrees: Int): String =
        suspendCancellableCoroutine { cont ->
            val image = InputImage.fromBitmap(bitmap, rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { result -> cont.resume(result.text) }
                .addOnFailureListener { error -> cont.resumeWithException(error) }
        }
}
