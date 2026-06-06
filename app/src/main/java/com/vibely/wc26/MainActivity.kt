package com.vibely.wc26

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.vibely.wc26.core.navigation.BrowseGroups
import com.vibely.wc26.core.navigation.BrowseTeams
import com.vibely.wc26.core.navigation.Home
import com.vibely.wc26.core.navigation.Scan
import com.vibely.wc26.core.navigation.Search
import com.vibely.wc26.core.navigation.Settings
import com.vibely.wc26.core.navigation.Specials
import com.vibely.wc26.core.navigation.Stats
import com.vibely.wc26.core.navigation.TeamSheet
import com.vibely.wc26.core.ui.theme.PaniniWC26Theme
import com.vibely.wc26.domain.prefs.ThemeMode
import com.vibely.wc26.feature.browse.groups.BrowseGroupsScreen
import com.vibely.wc26.feature.browse.specials.SpecialsScreen
import com.vibely.wc26.feature.browse.teams.BrowseTeamsScreen
import com.vibely.wc26.feature.browse.teamsheet.TeamSheetScreen
import com.vibely.wc26.feature.home.HomeScreen
import com.vibely.wc26.feature.scan.ScanScreen
import com.vibely.wc26.feature.search.SearchScreen
import com.vibely.wc26.feature.settings.SettingsScreen
import com.vibely.wc26.feature.stats.StatsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val shell: AppShellViewModel = hiltViewModel()
            val themeMode by shell.themeMode.collectAsStateWithLifecycle()
            val catalogState by shell.catalogState.collectAsStateWithLifecycle()
            val darkTheme = when (themeMode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }
            PaniniWC26Theme(darkTheme = darkTheme) {
                when (val state = catalogState) {
                    CatalogState.Loading -> CatalogLoadingGate()
                    is CatalogState.Error -> CatalogErrorGate(
                        message = state.message,
                        onRetry = shell::retryCatalogLoad,
                    )
                    CatalogState.Ready -> App()
                }
            }
        }
    }
}

@Composable
private fun App() {
    val backStack = rememberNavBackStack(Home)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Home> {
                HomeScreen(
                    onBrowse = { backStack.add(BrowseGroups) },
                    onSearch = { backStack.add(Search) },
                    onScan = { backStack.add(Scan) },
                    onStats = { backStack.add(Stats) },
                    onSettings = { backStack.add(Settings) },
                )
            }
            entry<BrowseGroups> {
                BrowseGroupsScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onGroupClick = { letter -> backStack.add(BrowseTeams(letter)) },
                    onSpecialsClick = { backStack.add(Specials) },
                )
            }
            entry<BrowseTeams> { key ->
                BrowseTeamsScreen(
                    groupLetter = key.groupLetter,
                    onBack = { backStack.removeLastOrNull() },
                    onTeamClick = { code -> backStack.add(TeamSheet(code)) },
                )
            }
            entry<TeamSheet> { key ->
                TeamSheetScreen(
                    teamCode = key.teamCode,
                    onBack = { backStack.removeLastOrNull() },
                )
            }
            entry<Specials> {
                SpecialsScreen(
                    onBack = { backStack.removeLastOrNull() },
                )
            }
            entry<Search> {
                SearchScreen(
                    onBack = { backStack.removeLastOrNull() },
                )
            }
            entry<Scan> {
                ScanScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onSearch = {
                        backStack.removeLastOrNull()
                        backStack.add(Search)
                    },
                )
            }
            entry<Stats> {
                StatsScreen(
                    onBack = { backStack.removeLastOrNull() },
                )
            }
            entry<Settings> {
                SettingsScreen(
                    onBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}

@Composable
private fun CatalogLoadingGate() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CatalogErrorGate(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.catalog_error_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.catalog_error_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.catalog_error_retry))
        }
    }
}
