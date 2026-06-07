package com.vibely.wc26.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * All in-app destinations. Nav3 serializes these into the back stack via kotlinx-serialization.
 * Adding a route = adding a sealed-class branch here + an `entry<>` block in MainActivity.
 */
@Serializable
sealed interface Route : NavKey

@Serializable data object Home : Route

@Serializable data object BrowseGroups : Route

@Serializable data class BrowseTeams(
    val groupLetter: String,
) : Route

@Serializable data class TeamSheet(
    val teamCode: String,
) : Route

@Serializable data object Specials : Route

// Phase placeholders — real screens land in later phases.
@Serializable data object Search : Route

@Serializable data object Scan : Route

@Serializable data object Stats : Route

@Serializable data object Settings : Route

@Serializable data object Import : Route
