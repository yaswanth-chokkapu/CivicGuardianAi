package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.EmergencyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CrisisSenseAppShell()
            }
        }
    }
}

sealed class NavigationScreen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : NavigationScreen("home", "Home", Icons.Outlined.Home)
    object Contacts : NavigationScreen("contacts", "Contacts", Icons.Outlined.Contacts)
    object History : NavigationScreen("history", "History", Icons.Outlined.History)
    object Settings : NavigationScreen("settings", "Settings", Icons.Outlined.Settings)
}

@Composable
fun CrisisSenseAppShell() {
    val navController = rememberNavController()
    val viewModel: EmergencyViewModel = viewModel()
    val context = LocalContext.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom navigation bar on the active confirmation dispatch screen
    val shouldShowBottomBar = currentRoute != "confirmation"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        bottomBar = {
            if (shouldShowBottomBar) {
                // Glassmorphic floating bottom navigation bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(68.dp)
                        .clip(RoundedCornerShape(34.dp))
                        .background(Color(0xE60D0D14))
                        .border(1.dp, GlassBorder, RoundedCornerShape(34.dp))
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val navItems = listOf(
                            NavigationScreen.Home,
                            NavigationScreen.Contacts,
                            NavigationScreen.History,
                            NavigationScreen.Settings
                        )

                        navItems.forEach { item ->
                            val isSelected = currentRoute == item.route
                            
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (isSelected) AccentTeal else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        color = if (isSelected) Color.White else Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = AccentTeal.copy(alpha = 0.12f)
                                ),
                                modifier = Modifier.testTag("nav_item_${item.route}")
                            )
                        }
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        // Main Navigation Routing
        NavHost(
            navController = navController,
            startDestination = NavigationScreen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (shouldShowBottomBar) 80.dp else 0.dp)
        ) {
            // Home Screen (Supports My Safety & Witness modes)
            composable(NavigationScreen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToConfirmation = {
                        navController.navigate("confirmation") {
                            launchSingleTop = true
                        }
                    },
                    onOpenProfile = {
                        Toast.makeText(context, "Anonymous Profile Secure • Session Enforced", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Confirmation Emergency Tracker Screen
            composable("confirmation") {
                ConfirmationScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Contacts Screen
            composable(NavigationScreen.Contacts.route) {
                ContactsScreen(
                    viewModel = viewModel
                )
            }

            // History Logs Screen
            composable(NavigationScreen.History.route) {
                HistoryScreen(
                    viewModel = viewModel
                )
            }

            // Settings Preferences Screen
            composable(NavigationScreen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToContacts = {
                        navController.navigate(NavigationScreen.Contacts.route) {
                            popUpTo(NavigationScreen.Home.route)
                        }
                    }
                )
            }
        }
    }
}
