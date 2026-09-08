package com.adn.adnapp.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.*
import com.adn.adnapp.R
import com.adn.adnapp.core.navigation.Screen
import com.adn.adnapp.core.theme.AreaTheme
import com.adn.adnapp.core.theme.palette
import com.adn.adnapp.domain.model.AppArea
import com.adn.adnapp.feature.dashboard.DashboardScreen
import com.adn.adnapp.feature.dashboard.AgendaAnalysisScreen
import com.adn.adnapp.data.local.AreaCyclePreferences
import com.adn.adnapp.data.local.nextArea
import com.adn.adnapp.feature.dayviewer.DayViewerScreen
import com.adn.adnapp.feature.home.FoodSearchScreen
import com.adn.adnapp.feature.profile.ProfileScreen

private enum class AreaTab(val route: String, val title: String) {
    HOME("home", "Home"),
    ANALYSIS("dashboard", "Análisis"),
    SOON("soon", "Próximamente"),
    SETTINGS("settings", "Personalización")
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onNavigateToSplash: () -> Unit, onNavigateToDietSelection: () -> Unit) {
    var activeArea by rememberSaveable { mutableStateOf(AppArea.NUTRITION) }
    var selectorOpen by rememberSaveable { mutableStateOf(false) }
    var cycleSettingsOpen by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val cyclePreferences = remember(context) { AreaCyclePreferences(context) }
    var areaCycle by remember { mutableStateOf(cyclePreferences.load()) }
    // Each controller owns and saves its area's stack independently.
    val controllers = AppArea.entries.associateWith { area ->
        key(area.id) { rememberNavController() }
    }
    val navController = controllers.getValue(activeArea)
    AreaTheme(activeArea) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (!WindowInsets.isImeVisible) {
                    val entry by navController.currentBackStackEntryAsState()
                    val route = entry?.destination?.route
                    Surface(shadowElevation = 12.dp, tonalElevation = 3.dp) {
                        Row(
                            Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(AreaTab.HOME, AreaTab.ANALYSIS, null, AreaTab.SOON, AreaTab.SETTINGS).forEach { tab ->
                                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    if (tab == null) {
                                        AreaSwitchButton(
                                            onOpen = { selectorOpen = true },
                                            onCycle = { activeArea = nextArea(activeArea, areaCycle) }
                                        )
                                    } else {
                                        val selected = route == tab.route ||
                                            (tab == AreaTab.ANALYSIS && route == Screen.DayViewer.route) ||
                                            (tab == AreaTab.HOME && route == Screen.FoodSearch.route) ||
                                            (tab == AreaTab.SETTINGS && route == Screen.Profile.route)
                                        Column(
                                            Modifier.fillMaxWidth().heightIn(min = 64.dp)
                                                .clickable {
                                                    navController.navigate(tab.route) {
                                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }.padding(horizontal = 2.dp, vertical = 5.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                                    else MaterialTheme.colorScheme.surface
                                            ) {
                                                Icon(
                                                    when (tab) {
                                                        AreaTab.HOME -> NavigationIcons.Home
                                                        AreaTab.ANALYSIS -> NavigationIcons.Analysis
                                                        AreaTab.SOON -> NavigationIcons.Soon
                                                        AreaTab.SETTINGS -> NavigationIcons.Settings
                                                    },
                                                    null,
                                                    Modifier.padding(horizontal = 12.dp, vertical = 4.dp).size(26.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Text(
                                                tab.title, fontSize = 9.sp, maxLines = 2,
                                                textAlign = TextAlign.Center,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { padding ->
            key(activeArea) {
                // Capture this host's area: outgoing destinations must never read a new mode.
                val hostArea = activeArea
                NavHost(navController, AreaTab.HOME.route,
                    Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None }
                ) {
                    composable(AreaTab.HOME.route) {
                        AreaLanding(hostArea, AreaTab.HOME,
                            onSearch = { navController.navigate(Screen.FoodSearch.route) },
                            onDiet = onNavigateToDietSelection,
                            onProfile = { navController.navigate(Screen.Profile.route) })
                    }
                    composable(AreaTab.ANALYSIS.route) {
                        if (hostArea == AppArea.NUTRITION) DashboardScreen(onOpenDay = {
                            navController.navigate(Screen.DayViewer.createRoute(it))
                        }) else if (hostArea == AppArea.AGENDA) AgendaAnalysisScreen(
                            onOpenNutritionDay = { navController.navigate(Screen.DayViewer.createRoute(it)) }
                        ) else AreaLanding(hostArea, AreaTab.ANALYSIS)
                    }
                    composable(AreaTab.SOON.route) { AreaLanding(hostArea, AreaTab.SOON) }
                    composable(AreaTab.SETTINGS.route) {
                        AreaLanding(hostArea, AreaTab.SETTINGS,
                            onDiet = onNavigateToDietSelection,
                            onProfile = { navController.navigate(Screen.Profile.route) { launchSingleTop = true } },
                            onCycleSettings = { cycleSettingsOpen = true })
                    }
                    if (hostArea == AppArea.NUTRITION) {
                        composable(Screen.FoodSearch.route) { FoodSearchScreen() }
                        composable(Screen.Profile.route) {
                            ProfileScreen(onNavigateToSplash = onNavigateToSplash,
                                onBack = { navController.popBackStack() })
                        }
                    }
                    if (hostArea == AppArea.NUTRITION || hostArea == AppArea.AGENDA) {
                        composable(Screen.DayViewer.route,
                            arguments = listOf(navArgument("date") { type = NavType.StringType })
                        ) { entry ->
                            DayViewerScreen(entry.arguments?.getString("date") ?: return@composable,
                                onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
        if (cycleSettingsOpen) AreaCycleSettings(
            areaCycle,
            onChange = { areaCycle = it; cyclePreferences.save(it) },
            onDismiss = { cycleSettingsOpen = false }
        )
        if (selectorOpen) {
            ModalBottomSheet(
                onDismissRequest = { selectorOpen = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Text("Tu espacio, a tu ritmo", Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Elige un área", Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton({
                    selectorOpen = false
                    cycleSettingsOpen = true
                }, Modifier.padding(horizontal = 16.dp)) { Text("Configurar cambio rápido · 1,5 s") }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(140.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp),
                    contentPadding = PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(AppArea.entries, key = { it.id }) { area ->
                        val colors = area.palette()
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                onClick = { activeArea = area; selectorOpen = false },
                                shape = CircleShape,
                                color = colors.soft,
                                contentColor = colors.ink,
                                border = BorderStroke(if (activeArea == area) 3.dp else 1.dp, colors.accent),
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(116.dp).semantics {
                                    contentDescription = "Abrir " + area.label()
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    AreaIcon(area, colors.ink, Modifier.size(48.dp))
                                }
                            }
                            Text(area.label(), Modifier.padding(top = 8.dp),
                                fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            if (activeArea == area) Text("Área actual",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaLanding(
    area: AppArea,
    tab: AreaTab,
    onSearch: () -> Unit = {},
    onDiet: () -> Unit = {},
    onProfile: () -> Unit = {},
    onCycleSettings: () -> Unit = {}
) {
    Scaffold(topBar = { CompactTopBar(area.label() + " · " + tab.title) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AreaIcon(area, MaterialTheme.colorScheme.onPrimaryContainer, Modifier.size(40.dp))
                    Text(if (tab == AreaTab.HOME) "Tu espacio de " + area.label().lowercase() else tab.title,
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(when (tab) {
                        AreaTab.HOME -> when (area) {
                            AppArea.AGENDA -> "Organiza tus días, citas y tareas. Próximamente."
                            AppArea.NUTRITION -> "Alimentos y herramientas para tu alimentación."
                            AppArea.SPORTS -> "Tus sesiones, ejercicios y rutinas. Próximamente."
                            AppArea.FINANCE -> "Tu hucha, movimientos y objetivos. Próximamente."
                            AppArea.PHILOSOPHY -> "Un lugar para leer, pensar y escribir. Próximamente."
                        }
                        AreaTab.ANALYSIS -> "Aquí verás tu evolución en " + area.label().lowercase() + ". Próximamente."
                        AreaTab.SOON -> "Este espacio está reservado para nuevas funciones de " + area.label().lowercase() + "."
                        AreaTab.SETTINGS -> if (area == AppArea.NUTRITION)
                            "Adapta tu dieta, tolerancia y datos personales."
                            else "Aquí configurarás tus preferencias de " + area.label().lowercase() + ". Próximamente."
                    })
                }
            }
            if (area == AppArea.NUTRITION) {
                if (tab == AreaTab.HOME) Button(onSearch, Modifier.fillMaxWidth()) { Text("Buscar alimentos y escanear") }
                if (tab == AreaTab.SETTINGS) {
                    Button(onDiet, Modifier.fillMaxWidth()) { Text("Dieta y tolerancia") }
                    OutlinedButton(onProfile, Modifier.fillMaxWidth()) { Text("Perfil y evolución del peso") }
                }
            }
            if (tab == AreaTab.SETTINGS) OutlinedButton(onCycleSettings, Modifier.fillMaxWidth()) {
                Text("Orden y áreas del cambio rápido")
            }
        }
    }
}

private fun AppArea.label() = when (this) {
    AppArea.AGENDA -> "Agenda"
    AppArea.NUTRITION -> "Nutrición"
    AppArea.SPORTS -> "Deporte"
    AppArea.FINANCE -> "Finanzas"
    AppArea.PHILOSOPHY -> "Filosofía"
}
