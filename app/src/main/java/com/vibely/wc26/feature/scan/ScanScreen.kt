package com.vibely.wc26.feature.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vibely.wc26.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScanViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val rapidEnabled by viewModel.rapidModeEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> permissionGranted = granted }

    var controller by remember { mutableStateOf<CameraController?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scan_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.browse_back_content_desc),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearch) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(R.string.scan_action_manual_search),
                        )
                    }
                },
            )
        },
    ) { inner ->
        Box(modifier = Modifier.padding(inner).fillMaxSize()) {
            when {
                !permissionGranted -> PermissionRationale(
                    onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                )
                else -> ScanCameraSurface(
                    state = state,
                    rapidEnabled = rapidEnabled,
                    onControllerReady = { controller = it },
                    onCaptureClick = {
                        val ctl = controller ?: return@ScanCameraSurface
                        scope.launch {
                            runCatching { ctl.capture() }.fold(
                                onSuccess = { frame ->
                                    viewModel.onFrameCaptured(frame.bitmap, frame.rotationDegrees)
                                },
                                onFailure = { viewModel.onCaptureFailed() },
                            )
                        }
                    },
                    onToggleRapidMode = viewModel::setRapidMode,
                )
            }

            when (val s = state) {
                is ScanUiState.Matched -> ScanMatchSheet(
                    matched = s,
                    onIncrement = viewModel::incrementPending,
                    onDecrement = viewModel::decrementPending,
                    onSave = viewModel::commit,
                    onSelectAlternative = viewModel::selectAlternative,
                    onDismiss = viewModel::dismiss,
                )
                is ScanUiState.NoMatch -> ScanNoMatchSheet(
                    onTryAgain = viewModel::dismiss,
                    onSearchManually = {
                        viewModel.dismiss()
                        onSearch()
                    },
                    onDismiss = viewModel::dismiss,
                )
                ScanUiState.Idle, ScanUiState.Reading -> Unit
            }
        }
    }
}

@Composable
private fun PermissionRationale(onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.PhotoCamera,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.scan_permission_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.scan_permission_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequest) {
            Text(stringResource(R.string.scan_permission_action))
        }
    }
}

@Composable
private fun ScanCameraSurface(
    state: ScanUiState,
    rapidEnabled: Boolean,
    onControllerReady: (CameraController) -> Unit,
    onCaptureClick: () -> Unit,
    onToggleRapidMode: (Boolean) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreviewSurface(
            onControllerReady = onControllerReady,
            modifier = Modifier.fillMaxSize(),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            FilterChip(
                selected = rapidEnabled,
                onClick = { onToggleRapidMode(!rapidEnabled) },
                label = { Text(stringResource(R.string.scan_rapid_mode)) },
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
        ) {
            when (state) {
                ScanUiState.Reading -> CircularProgressIndicator(
                    modifier = Modifier.size(56.dp),
                    color = Color.White,
                )
                else -> CaptureFab(onClick = onCaptureClick)
            }
        }
    }
}

@Composable
private fun CaptureFab(onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Icon(
            imageVector = Icons.Outlined.PhotoCamera,
            contentDescription = stringResource(R.string.scan_action_capture),
            modifier = Modifier.size(32.dp),
        )
    }
}
