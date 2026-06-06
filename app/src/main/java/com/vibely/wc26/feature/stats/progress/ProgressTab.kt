package com.vibely.wc26.feature.stats.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vibely.wc26.R
import com.vibely.wc26.core.ui.components.LabeledProgressBar
import com.vibely.wc26.core.ui.format.countLabel
import com.vibely.wc26.core.ui.format.percentLabel
import com.vibely.wc26.domain.model.ProgressSummary

@Composable
fun ProgressTab(
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stats = state.stats ?: return

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "overall") {
            OverallCard(summary = stats.overall, dupeTotal = stats.overall.totalDuplicates)
        }
        item(key = "specials") {
            SectionLabel(text = stringResource(R.string.stats_progress_specials_label))
            LabeledProgressBar(
                progress = stats.specials.progress,
                label = stats.specials.countLabel,
                trailing = stats.specials.percentLabel,
            )
        }
        item(key = "groups_header") {
            SectionLabel(text = stringResource(R.string.stats_progress_by_group))
        }
        items(state.groups, key = { "group:${it.letter}" }) { row ->
            val summary = row.summary ?: ProgressSummary(0, 0, 0, 0)
            LabeledProgressBar(
                progress = summary.progress,
                label = stringResource(R.string.browse_group_format, row.letter),
                trailing = summary.countLabel,
            )
        }
        item(key = "teams_header") {
            SectionLabel(text = stringResource(R.string.stats_progress_by_team))
        }
        items(state.teams, key = { "team:${it.team.code}" }) { row ->
            val summary = row.summary ?: ProgressSummary(0, 0, 0, 0)
            LabeledProgressBar(
                progress = summary.progress,
                label = row.team.name,
                trailing = teamTrailing(summary),
            )
        }
    }
}

@Composable
private fun OverallCard(summary: ProgressSummary, dupeTotal: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.home_overall_progress),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        LabeledProgressBar(
            progress = summary.progress,
            label = summary.countLabel,
            trailing = summary.percentLabel,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.stats_progress_total_duplicates, dupeTotal),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
    )
}

/** Team rows show owned/total + (optional) plural dupe count: "5 / 20 · 2 dupes". */
@Composable
private fun teamTrailing(summary: ProgressSummary): String {
    if (summary.totalDuplicates <= 0) return summary.countLabel
    val dupeSuffix = pluralStringResource(
        R.plurals.stats_progress_dupes_suffix,
        summary.totalDuplicates,
        summary.totalDuplicates,
    )
    return stringResource(
        R.string.stats_progress_team_trailing,
        summary.countLabel,
        dupeSuffix,
    )
}
