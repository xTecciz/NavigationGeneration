package com.example.feature_laucnher_api

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

fun NavGraphBuilder.register(
    featureLauncherApi: FeatureLauncherApi,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    exitFlow: () -> Unit = {},
) {
    with(featureLauncherApi) {
        registerGraph(
            navController = navController,
            modifier = modifier,
            exitFlow = exitFlow,
        )
    }
}
