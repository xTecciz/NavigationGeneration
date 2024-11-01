package com.example.feature_laucnher_api

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

interface FeatureLauncherApi {

    fun NavGraphBuilder.registerGraph(
        navController: NavHostController,
        modifier: Modifier = Modifier,
        exitFlow: () -> Unit,
    )
}
