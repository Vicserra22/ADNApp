package com.adn.adnapp.core.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.adn.adnapp.R
import com.adn.adnapp.core.navigation.Screen
import com.adn.adnapp.core.theme.AdnColors
import com.adn.adnapp.domain.model.AppArea
import com.adn.adnapp.feature.dashboard.DashboardScreen
import com.adn.adnapp.feature.dayviewer.DayViewerScreen
import com.adn.adnapp.feature.home.HomeScreen
import com.adn.adnapp.feature.home.FoodSearchScreen
import com.adn.adnapp.feature.profile.ProfileScreen
import com.adn.adnapp.feature.soon.SoonScreen

private data class BallItem(val screen: Screen?, val icon: ImageVector? = null, val center: Boolean = false)
private data class TreeAction(val label: String, val destination: Screen? = null)

@Composable
fun MainScreen(onNavigateToSplash: () -> Unit, onNavigateToDietSelection: () -> Unit) {
    val navController = rememberNavController()
    var treeOpen by remember { mutableStateOf(false) }
    var selectedArea by remember { mutableStateOf<AppArea?>(null) }
    val items = listOf(
        BallItem(Screen.Home, Icons.Default.Home),
        BallItem(Screen.Dashboard),
        BallItem(null, center = true),
        BallItem(Screen.Profile, Icons.Default.Person),
        BallItem(Screen.Soon)
    )
    Box(Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Surface(tonalElevation = 4.dp) {
                    Row(
                        Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val entry by navController.currentBackStackEntryAsState()
                        val route = entry?.destination?.route
                        items.forEach { item ->
                            NavigationBall(item, selected = item.screen?.route == route) {
                                if (item.center) {
                                    treeOpen = !treeOpen
                                    if (!treeOpen) selectedArea = null
                                } else item.screen?.let { screen ->
                                    treeOpen = false
                                    selectedArea = null
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(navController, Screen.Home.route, Modifier.padding(padding)) {
                composable(Screen.Home.route) { HomeScreen() }
                composable(Screen.FoodSearch.route) { FoodSearchScreen() }
                composable(Screen.Dashboard.route) {
                    DashboardScreen(onOpenDay = { date ->
                        navController.navigate(Screen.DayViewer.createRoute(date))
                    })
                }
                composable(
                    route = Screen.DayViewer.route,
                    arguments = listOf(navArgument("date") { type = NavType.StringType })
                ) { entry ->
                    val date = entry.arguments?.getString("date") ?: return@composable
                    DayViewerScreen(date = date, onBack = navController::popBackStack)
                }
                composable(Screen.Profile.route) { ProfileScreen(onNavigateToSplash = onNavigateToSplash) }
                composable(Screen.Soon.route) { SoonScreen() }
            }
        }
        if (treeOpen) {
            BackHandler { treeOpen = false; selectedArea = null }
            AreaTreeOverlay(
                selectedArea = selectedArea,
                onAreaSelected = { selectedArea = if (selectedArea == it) null else it },
                onAction = { action ->
                    treeOpen = false
                    selectedArea = null
                    action.destination?.let { screen ->
                        navController.navigate(screen.route) { launchSingleTop = true }
                    } ?: onNavigateToDietSelection()
                },
                onDismiss = { treeOpen = false; selectedArea = null }
            )
        }
    }
}

@Composable
private fun NavigationBall(item: BallItem, selected: Boolean, onClick: () -> Unit) {
    val ballSize = if (item.center) 64.dp else 48.dp
    val stroke = when {
        item.center -> BorderStroke(3.dp, AdnColors.Green20)
        selected -> BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary)
        else -> BorderStroke(0.dp, Color.Transparent)
    }
    Box(
        Modifier.size(ballSize).shadow(if (item.center) 8.dp else 3.dp, CircleShape)
            .border(stroke, CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Surface(Modifier.fillMaxSize(), CircleShape, color = MaterialTheme.colorScheme.primary) {}
        if (item.center) {
            Surface(Modifier.size(44.dp), CircleShape, color = MaterialTheme.colorScheme.surface) {
                Image(painterResource(R.drawable.adn_logo), "Abrir áreas", Modifier.padding(5.dp))
            }
        } else if (item.icon != null) {
            Icon(item.icon, item.screen?.route, tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp))
        } else if (item.screen == Screen.Dashboard) {
            AnalysisIcon()
        }
    }
}

@Composable
private fun AnalysisIcon() {
    val color = MaterialTheme.colorScheme.onPrimary
    Canvas(Modifier.size(25.dp)) {
        val barWidth = size.width * .2f
        val gap = size.width * .12f
        listOf(.45f, .72f, 1f).forEachIndexed { index, heightRatio ->
            val height = size.height * heightRatio
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(index * (barWidth + gap), size.height - height),
                size = androidx.compose.ui.geometry.Size(barWidth, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f)
            )
        }
    }
}

@Composable
private fun AreaTreeOverlay(
    selectedArea: AppArea?,
    onAreaSelected: (AppArea) -> Unit,
    onAction: (TreeAction) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = .42f)).clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 92.dp)
                .clickable(enabled = false) {},
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Explorar áreas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Selecciona una rama", color = MaterialTheme.colorScheme.primary)
                BoxWithConstraints(Modifier.fillMaxWidth().height(390.dp)) {
                    val areaGap = (maxWidth - 72.dp) / 3
                    val actionGap = (maxWidth - 72.dp) / 2
                    TreeBranches(selectedArea)
                    AppArea.entries.forEachIndexed { index, area ->
                        TreeBall(
                            label = area.label(),
                            selected = selectedArea == area,
                            modifier = Modifier.offset(x = areaGap * index, y = 235.dp),
                            onClick = { onAreaSelected(area) }
                        )
                    }
                    selectedArea?.actions()?.forEachIndexed { index, action ->
                        TreeBall(
                            label = action.label,
                            selected = false,
                            modifier = Modifier.offset(x = actionGap * index, y = 50.dp),
                            onClick = { onAction(action) }
                        )
                    }
                    Surface(
                        Modifier.size(64.dp).offset(x = (maxWidth - 64.dp) / 2, y = 320.dp),
                        CircleShape, color = MaterialTheme.colorScheme.primary
                    ) {
                        Image(painterResource(R.drawable.adn_logo), null, Modifier.padding(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TreeBranches(selectedArea: AppArea?) {
    val branch = MaterialTheme.colorScheme.primary
    Canvas(Modifier.fillMaxSize()) {
        val root = androidx.compose.ui.geometry.Offset(size.width / 2f, 352.dp.toPx())
        val areaY = 271.dp.toPx()
        repeat(4) { index ->
            val area = androidx.compose.ui.geometry.Offset(
                36.dp.toPx() + index * (size.width - 72.dp.toPx()) / 3f, areaY
            )
            drawLine(branch, root, area, strokeWidth = 3.dp.toPx())
        }
        selectedArea?.let { area ->
            val parent = androidx.compose.ui.geometry.Offset(
                36.dp.toPx() + area.ordinal * (size.width - 72.dp.toPx()) / 3f, areaY
            )
            repeat(3) { index ->
                val child = androidx.compose.ui.geometry.Offset(
                    36.dp.toPx() + index * (size.width - 72.dp.toPx()) / 2f, 86.dp.toPx()
                )
                drawLine(branch, parent, child, strokeWidth = 3.dp.toPx())
            }
        }
    }
}

@Composable
private fun TreeBall(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.size(72.dp).shadow(4.dp, CircleShape).clickable(onClick = onClick),
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
        border = if (selected) BorderStroke(3.dp, AdnColors.Green20) else null
    ) {
        Box(Modifier.padding(4.dp), contentAlignment = Alignment.Center) {
            Text(label, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary)
        }
    }
}

private fun AppArea.label() = when (this) {
    AppArea.NUTRITION -> "Nutrición"
    AppArea.SPORTS -> "Deportes"
    AppArea.FINANCE -> "Finanzas"
    AppArea.PHILOSOPHY -> "Filosofía"
}

private fun AppArea.actions(): List<TreeAction> = when (this) {
    AppArea.NUTRITION -> listOf(
        TreeAction("Buscar", Screen.FoodSearch), TreeAction("Resumen", Screen.Dashboard), TreeAction("Dieta")
    )
    AppArea.SPORTS -> listOf(
        TreeAction("Entrenos", Screen.Soon), TreeAction("Progreso", Screen.Soon), TreeAction("Objetivos", Screen.Soon)
    )
    AppArea.FINANCE -> listOf(
        TreeAction("Hucha", Screen.Soon), TreeAction("Inversiones", Screen.Soon), TreeAction("Objetivos", Screen.Soon)
    )
    AppArea.PHILOSOPHY -> listOf(
        TreeAction("Diario", Screen.Soon), TreeAction("Lecturas", Screen.Soon), TreeAction("Reflexión", Screen.Soon)
    )
}
