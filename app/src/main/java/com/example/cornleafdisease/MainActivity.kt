package com.example.cornleafdisease

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.cornleafdisease.data.model.ScanResult
import com.example.cornleafdisease.data.repository.DatabaseRepositoryImpl
import com.example.cornleafdisease.ui.theme.CornLeafTheme
import com.example.cornleafdisease.ui.theme.GreenPrimary
import com.example.cornleafdisease.ui.theme.GreenDark
import com.example.cornleafdisease.ui.screens.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ModelTest.test(this)
        ModelTest.testAnfis(this)
        setContent {
            CornLeafTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    
    // Repository and shared state (Local SharedPreferences)
    val repository = remember { DatabaseRepositoryImpl(context) }
    var latestScanResult by remember { mutableStateOf<ScanResult?>(null) }
    var isViewingHistoryItem by remember { mutableStateOf(false) }
    var historyState by remember { mutableStateOf(emptyList<ScanResult>()) }

    // Sync history initially
    LaunchedEffect(Unit) {
        historyState = repository.getScanHistory()
    }

    val bottomItems = listOf(
        BottomNavItem("Home", Screen.Home.route, Icons.Default.Home),
        BottomNavItem("Guide", Screen.Guide.route, Icons.AutoMirrored.Filled.MenuBook),
        BottomNavItem("Disease Info", Screen.DiseaseInfo.route, Icons.Default.Info),
        BottomNavItem("History", Screen.ClassificationHistory.route, Icons.Default.History)
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Guide.route,
        Screen.DiseaseInfo.route,
        Screen.ClassificationResult.route,
        Screen.ClassificationHistory.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = GreenPrimary) {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label, tint = Color.White) },
                            label = { Text(item.label, color = Color.White) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                                indicatorColor = GreenDark
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onCaptureClick = { navController.navigate(Screen.DetectDisease.route) }
                )
            }
            composable(Screen.DetectDisease.route) {
                DetectDiseaseScreen(
                    onBack = { navController.popBackStack() },
                    onResult = { result ->
                        latestScanResult = result
                        isViewingHistoryItem = false
                        navController.navigate(Screen.ClassificationResult.route)
                    }
                )
            }
            composable(Screen.ClassificationResult.route) {
                ClassificationResultScreen(
                    result = latestScanResult,
                    isFromHistory = isViewingHistoryItem,
                    onBack = { navController.popBackStack() },
                    onNewScan = {
                        navController.navigate(Screen.DetectDisease.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onSaveClick = { scanResult ->
                        scope.launch {
                            repository.saveScanResult(scanResult)
                            historyState = repository.getScanHistory() // Refresh local state
                        }
                    }
                )
            }
            composable(Screen.DiseaseInfo.route) {
                DiseaseInfoScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Guide.route) {
                GuideScreen()
            }
            composable(Screen.ClassificationHistory.route) {
                ClassificationHistoryScreen(
                    history = historyState,
                    onBack = { navController.popBackStack() },
                    onDelete = { id ->
                        scope.launch {
                            repository.deleteScanResult(id)
                            historyState = repository.getScanHistory()
                        }
                    },
                    onItemClick = { scanResult ->
                        latestScanResult = scanResult
                        isViewingHistoryItem = true
                        navController.navigate(Screen.ClassificationResult.route)
                    }
                )
            }
        }
    }
}

data class BottomNavItem(val label: String, val route: String, val icon: ImageVector)

@Preview
@Composable
fun MainAppPreview() {
    MainApp()
}
