package com.vibely.wc26.feature.stickerdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vibely.wc26.R
import com.vibely.wc26.core.util.duplicatesOf
import com.vibely.wc26.domain.model.Sticker
import com.vibely.wc26.domain.model.Team

/**
 * Bottom sheet for adjusting a single sticker's quantity. Stateless — the host
 * screen's ViewModel owns selection state and persists changes via use cases.
 * Tap the number to edit it directly (numeric IME); otherwise use −/+.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerDetailSheet(
    sticker: Sticker,
    team: Team?,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onSetQuantity: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        StickerDetailContent(
            sticker = sticker,
            team = team,
            quantity = quantity,
            onIncrement = onIncrement,
            onDecrement = onDecrement,
            onSetQuantity = onSetQuantity,
        )
    }
}

@Composable
private fun StickerDetailContent(
    sticker: Sticker,
    team: Team?,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onSetQuantity: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = headerLine(sticker, team),
            style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = sticker.displayName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Stepper(
            quantity = quantity,
            onIncrement = onIncrement,
            onDecrement = onDecrement,
            onSetQuantity = onSetQuantity,
        )
        Spacer(modifier = Modifier.height(16.dp))
        DuplicatesLine(quantity = quantity)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun Stepper(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onSetQuantity: (Int) -> Unit,
) {
    var editing by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(
            icon = Icons.Outlined.Remove,
            contentDescription = stringResource(R.string.sticker_detail_decrement),
            enabled = quantity > 0,
            onClick = onDecrement,
        )
        Spacer(modifier = Modifier.size(20.dp))
        Box(
            modifier = Modifier.widthIn(min = 72.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (editing) {
                QuantityField(
                    initial = quantity,
                    onConfirm = { value ->
                        editing = false
                        onSetQuantity(value)
                    },
                    onCancel = { editing = false },
                )
            } else {
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .clickableNoIndication { editing = true },
                )
            }
        }
        Spacer(modifier = Modifier.size(20.dp))
        StepperButton(
            icon = Icons.Outlined.Add,
            contentDescription = stringResource(R.string.sticker_detail_increment),
            enabled = true,
            onClick = onIncrement,
        )
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

@Composable
private fun QuantityField(
    initial: Int,
    onConfirm: (Int) -> Unit,
    onCancel: () -> Unit,
) {
    var text by remember { mutableStateOf(initial.toString()) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    OutlinedTextField(
        value = text,
        onValueChange = { raw ->
            // Strip anything non-digit and cap at 3 digits to keep stepper sane.
            text = raw.filter(Char::isDigit).take(3)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                val parsed = text.toIntOrNull()
                if (parsed != null) onConfirm(parsed.coerceAtLeast(0)) else onCancel()
            },
        ),
        modifier = Modifier
            .widthIn(min = 96.dp)
            .focusRequester(focusRequester),
    )
}

@Composable
private fun DuplicatesLine(quantity: Int) {
    val dupes = duplicatesOf(quantity)
    if (dupes <= 0) {
        Text(
            text = stringResource(R.string.sticker_detail_no_duplicates),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        Text(
            text = pluralStringResource(R.plurals.sticker_detail_duplicates, dupes, dupes, dupes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun headerLine(sticker: Sticker, team: Team?): String = when {
    team != null -> stringResource(
        R.string.sticker_detail_header_team,
        sticker.id,
        team.name,
        team.group,
    )
    sticker.section != null -> stringResource(
        R.string.sticker_detail_header_special,
        sticker.id,
        sticker.section,
    )
    else -> sticker.id
}

/** Clickable text without a ripple — feels lighter for tap-to-edit affordance. */
@Composable
private fun Modifier.clickableNoIndication(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return this.then(
        Modifier.clickable(
            interactionSource = interaction,
            indication = null,
            onClick = onClick,
        ),
    )
}
