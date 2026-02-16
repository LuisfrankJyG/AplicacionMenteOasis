package com.LuisS.menteoasis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.LuisS.menteoasis.ui.theme.MenteOasisTheme
import com.LuisS.menteoasis.ui.features.asistencia.AsistenciaScreen
import com.LuisS.menteoasis.ui.features.cumpleanos.CumpleanosScreen
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Asistencia : Screen("asistencia", "Asistencia", Icons.Default.Person)
    object Cumpleanos : Screen("cumpleanos", "Cumpleaños", Icons.Default.DateRange)
}

val items = listOf(
    Screen.Asistencia,
    Screen.Cumpleanos,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MenteOasisTheme {
                MenteOasisApp()
            }
        }
    }
}

@Composable
fun MenteOasisApp() {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            // Only show bottom bar on main screens
            val isMainScreen = items.any { it.route == currentDestination?.route }
            if (isMainScreen) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Asistencia.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Asistencia.route) { AsistenciaScreen() }
            composable(Screen.Cumpleanos.route) { CumpleanosScreen() }
        }
    }
}