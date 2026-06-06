package com.vibely.wc26

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.vibely.wc26.core.navigation.BrowseGroups
import com.vibely.wc26.core.navigation.BrowseTeams
import com.vibely.wc26.core.navigation.Home
import com.vibely.wc26.core.navigation.Scan
import com.vibely.wc26.core.navigation.Search
import com.vibely.wc26.core.navigation.Specials
import com.vibely.wc26.core.navigation.Stats
import com.vibely.wc26.core.navigation.TeamSheet
import com.vibely.wc26.core.ui.theme.PaniniWC26Theme
import com.vibely.wc26.feature.browse.groups.BrowseGroupsScreen
import com.vibely.wc26.feature.browse.specials.SpecialsScreen
import com.vibely.wc26.feature.browse.teams.BrowseTeamsScreen
import com.vibely.wc26.feature.browse.teamsheet.TeamSheetScreen
import com.vibely.wc26.feature.home.HomeScreen
import com.vibely.wc26.feature.placeholder.PlaceholderScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PaniniWC26Theme { App() }
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
                    onStickerClick = { /* Phase 4: open bottom sheet */ },
                )
            }
            entry<Specials> {
                SpecialsScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onStickerClick = { /* Phase 4: open bottom sheet */ },
                )
            }
            entry<Search> {
                PlaceholderScreen(
                    title = stringResource(R.string.placeholder_search_title),
                    onBack = { backStack.removeLastOrNull() },
                )
            }
            entry<Scan> {
                PlaceholderScreen(
                    title = stringResource(R.string.placeholder_scan_title),
                    onBack = { backStack.removeLastOrNull() },
                )
            }
            entry<Stats> {
                PlaceholderScreen(
                    title = stringResource(R.string.placeholder_stats_title),
                    onBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}
