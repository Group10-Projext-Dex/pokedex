package com.example.project_dex

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project_dex.viewmodels.LocationViewModel

/**
 * Main entry point for Location feature
 * This file delegates to the actual UI implementation in the ui package
 */
@Composable
fun LocationScreen(
    modifier: Modifier = Modifier,
    onDetailViewChange: (Boolean) -> Unit = {},
    locationViewModel: LocationViewModel = viewModel()
) {
    com.example.project_dex.ui.LocationScreen(
        modifier = modifier,
        locationViewModel = locationViewModel,
        onDetailViewChange = onDetailViewChange
    )
}