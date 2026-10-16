package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WallpaperPreviewDialog
import com.example.ui.screens.AiStudioScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppNavTab
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val userPrefs by viewModel.userPreferences.collectAsState()
                val snackbarMessage by viewModel.snackbarMessage.collectAsState()
                val previewWallpaper by viewModel.previewWallpaper.collectAsState()
                val isApplying by viewModel.isApplying.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }
                var currentTab by remember { mutableStateOf(AppNavTab.HOME) }
                var subScreen by remember { mutableStateOf<String?>(null) } // "diagnostics" or "permissions"

                // Show Snackbar when message triggers
                LaunchedEffect(snackbarMessage) {
                    snackbarMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearSnackbar()
                    }
                }

                // If onboarding not finished, show onboarding
                if (!userPrefs.isOnboardingCompleted) {
                    OnboardingScreen(
                        onFinish = { viewModel.completeOnboarding() }
                    )
                } else if (subScreen == "diagnostics") {
                    BackHandler { subScreen = null }
                    DiagnosticsScreen(
                        viewModel = viewModel,
                        onBack = { subScreen = null }
                    )
                } else if (subScreen == "permissions") {
                    BackHandler { subScreen = null }
                    PermissionsScreen(
                        viewModel = viewModel,
                        onBack = { subScreen = null }
                    )
                } else {
                    BackHandler(enabled = currentTab != AppNavTab.HOME) {
                        currentTab = AppNavTab.HOME
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = when (currentTab) {
                                            AppNavTab.HOME -> "Wallpaper Flow AI"
                                            AppNavTab.EXPLORE -> "Explore Wallpapers"
                                            AppNavTab.AI_STUDIO -> "AI Studio"
                                            AppNavTab.FAVORITES -> "Saved Favorites"
                                            AppNavTab.SETTINGS -> "Settings & Automation"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                },
                                actions = {
                                    IconButton(
                                        onClick = { subScreen = "permissions" },
                                        modifier = Modifier.testTag("top_bar_permissions_button")
                                    ) {
                                        Icon(
                                            Icons.Default.Security,
                                            contentDescription = "Permissions",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { subScreen = "diagnostics" },
                                        modifier = Modifier.testTag("top_bar_diagnostics_button")
                                    ) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = "Diagnostics",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == AppNavTab.HOME,
                                    onClick = { currentTab = AppNavTab.HOME },
                                    icon = {
                                        Icon(
                                            if (currentTab == AppNavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                            contentDescription = "Home"
                                        )
                                    },
                                    label = { Text("Home", fontSize = 11.sp) },
                                    modifier = Modifier.testTag("nav_home_tab")
                                )

                                NavigationBarItem(
                                    selected = currentTab == AppNavTab.EXPLORE,
                                    onClick = { currentTab = AppNavTab.EXPLORE },
                                    icon = {
                                        Icon(
                                            if (currentTab == AppNavTab.EXPLORE) Icons.Filled.Explore else Icons.Outlined.Explore,
                                            contentDescription = "Explore"
                                        )
                                    },
                                    label = { Text("Explore", fontSize = 11.sp) },
                                    modifier = Modifier.testTag("nav_explore_tab")
                                )

                                NavigationBarItem(
                                    selected = currentTab == AppNavTab.AI_STUDIO,
                                    onClick = { currentTab = AppNavTab.AI_STUDIO },
                                    icon = {
                                        Icon(
                                            if (currentTab == AppNavTab.AI_STUDIO) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                                            contentDescription = "AI Studio"
                                        )
                                    },
                                    label = { Text("AI Studio", fontSize = 11.sp) },
                                    modifier = Modifier.testTag("nav_ai_studio_tab")
                                )

                                NavigationBarItem(
                                    selected = currentTab == AppNavTab.FAVORITES,
                                    onClick = { currentTab = AppNavTab.FAVORITES },
                                    icon = {
                                        Icon(
                                            if (currentTab == AppNavTab.FAVORITES) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Favorites"
                                        )
                                    },
                                    label = { Text("Favorites", fontSize = 11.sp) },
                                    modifier = Modifier.testTag("nav_favorites_tab")
                                )

                                NavigationBarItem(
                                    selected = currentTab == AppNavTab.SETTINGS,
                                    onClick = { currentTab = AppNavTab.SETTINGS },
                                    icon = {
                                        Icon(
                                            if (currentTab == AppNavTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                            contentDescription = "Settings"
                                        )
                                    },
                                    label = { Text("Settings", fontSize = 11.sp) },
                                    modifier = Modifier.testTag("nav_settings_tab")
                                )
                            }
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentTab) {
                                AppNavTab.HOME -> HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToExplore = { currentTab = AppNavTab.EXPLORE },
                                    onNavigateToAiStudio = { currentTab = AppNavTab.AI_STUDIO },
                                    onNavigateToDiagnostics = { subScreen = "diagnostics" }
                                )
                                AppNavTab.EXPLORE -> ExploreScreen(viewModel = viewModel)
                                AppNavTab.AI_STUDIO -> AiStudioScreen(viewModel = viewModel)
                                AppNavTab.FAVORITES -> FavoritesScreen(viewModel = viewModel)
                                AppNavTab.SETTINGS -> SettingsScreen(
                                    viewModel = viewModel,
                                    onNavigateToPermissions = { subScreen = "permissions" },
                                    onNavigateToDiagnostics = { subScreen = "diagnostics" }
                                )
                            }
                        }
                    }
                }

                // Fullscreen Wallpaper Preview Modal if an image is selected
                previewWallpaper?.let { wallpaper ->
                    WallpaperPreviewDialog(
                        wallpaper = wallpaper,
                        onDismiss = { viewModel.setPreviewWallpaper(null) },
                        onApply = { target ->
                            viewModel.applyWallpaper(wallpaper, target)
                        },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(wallpaper)
                        },
                        isApplying = isApplying
                    )
                }
            }
        }
    }
}
