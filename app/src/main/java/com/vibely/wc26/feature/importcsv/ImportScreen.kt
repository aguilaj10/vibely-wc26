package com.vibely.wc26.feature.importcsv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vibely.wc26.R
import com.vibely.wc26.domain.usecase.ImportOwnershipUseCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ImportViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.browse_back_content_desc),
                        )
                    }
                },
            )
        },
    ) { inner ->
        Column(
            modifier =
                Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.import_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = state.csvInput,
                onValueChange = viewModel::setCsvInput,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                label = { Text(stringResource(R.string.import_csv_label)) },
                placeholder = { Text(stringResource(R.string.import_csv_placeholder)) },
                supportingText = {
                    Text(stringResource(R.string.import_csv_support, state.parsedCount))
                },
            )

            OutlinedButton(
                onClick = { showConfirmDialog = true },
                enabled = state.parsedCount > 0 && !state.isImporting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text =
                        if (state.isImporting) {
                            stringResource(R.string.import_importing)
                        } else {
                            stringResource(R.string.import_action)
                        },
                )
            }

            if (state.result != null) {
                val result = state.result!!
                val (text, isError) =
                    if (result.error != null) {
                        stringResource(R.string.import_error, result.error) to true
                    } else {
                        formatSuccess(result) to false
                    }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                )
            }
        }
    }

    if (showConfirmDialog) {
        ImportConfirmDialog(
            count = state.parsedCount,
            onConfirm = {
                showConfirmDialog = false
                viewModel.import()
            },
            onDismiss = { showConfirmDialog = false },
        )
    }
}

private fun formatSuccess(result: ImportOwnershipUseCase.ImportResult): String {
    val parts = mutableListOf("Imported ${result.importedCount} stickers")
    if (result.skippedIds.isNotEmpty()) parts += "Skipped ${result.skippedIds.size} (quantity ≤ 0)"
    if (result.unknownIds.isNotEmpty()) parts += "Unknown IDs: ${result.unknownIds.joinToString(", ")}"
    return parts.joinToString(separator = "\n")
}

@Composable
private fun ImportConfirmDialog(
    count: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.import_confirm_title)) },
        text = { Text(stringResource(R.string.import_confirm_body, count)) },
        confirmButton = {
            OutlinedButton(onClick = onConfirm) {
                Text(stringResource(R.string.import_confirm_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_reset_confirm_cancel))
            }
        },
    )
}
