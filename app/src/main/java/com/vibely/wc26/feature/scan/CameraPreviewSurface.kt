package com.vibely.wc26.feature.scan

import android.graphics.Bitmap
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Composable host for the CameraX preview. Binds [Preview] + [ImageCapture]
 * use cases to the local lifecycle. Provides [CameraController.capture] which
 * suspends until a frame has been captured and decoded to a [CapturedFrame].
 *
 * Callers should treat the [CameraController] as a short-lived handle —
 * recomposition recreates the binding, so storing it in a ViewModel is unsafe.
 */
@Composable
fun CameraPreviewSurface(
    onControllerReady: (CameraController) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    // Cap captures at ~720p. ML Kit Latin recognizer reads sticker-sized text
    // fine at this resolution, and a 12 MP full-res bitmap is ~48 MB ARGB —
    // overkill for OCR and rough on memory if the user scans rapidly.
    val imageCapture = remember {
        ImageCapture.Builder()
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            Size(CAPTURE_TARGET_WIDTH, CAPTURE_TARGET_HEIGHT),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
                        ),
                    )
                    .build(),
            )
            .build()
    }

    LaunchedEffect(previewView, lifecycleOwner) {
        val cameraProvider = withContext(Dispatchers.IO) {
            awaitCameraProvider(context)
        }
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageCapture,
        )
        onControllerReady(CameraController(imageCapture, context))
    }

    AndroidView(
        modifier = modifier,
        factory = { previewView },
    )
}

/** Handle returned by [CameraPreviewSurface] for triggering captures. */
class CameraController internal constructor(
    private val imageCapture: ImageCapture,
    private val context: android.content.Context,
) {
    /** Capture one frame; suspends until decode completes. Throws on capture errors. */
    suspend fun capture(): CapturedFrame = suspendCoroutine { cont ->
        val executor = ContextCompat.getMainExecutor(context)
        imageCapture.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        val bitmap = image.toBitmap()
                        val rotation = image.imageInfo.rotationDegrees
                        cont.resume(CapturedFrame(bitmap = bitmap, rotationDegrees = rotation))
                    } catch (t: Throwable) {
                        cont.resumeWithException(t)
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    cont.resumeWithException(exception)
                }
            },
        )
    }
}

data class CapturedFrame(
    val bitmap: Bitmap,
    val rotationDegrees: Int,
)

/** Bridge ListenableFuture → suspend coroutine for ProcessCameraProvider. */
private suspend fun awaitCameraProvider(context: android.content.Context): ProcessCameraProvider =
    suspendCoroutine { cont ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            { cont.resume(future.get()) },
            ContextCompat.getMainExecutor(context),
        )
    }

private const val CAPTURE_TARGET_WIDTH = 1280
private const val CAPTURE_TARGET_HEIGHT = 720
