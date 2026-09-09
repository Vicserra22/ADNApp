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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.graphics.Color
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
import com.adn.adnapp.feature.home.FoodSection
import com.adn.adnapp.feature.home.NutritionHomeScreen
import com.adn.adnapp.feature.home.CustomDishScreen
import com.adn.adnapp.feature.home.WellnessScreen
import com.adn.adnapp.feature.agenda.AgendaHomeScreen
import com.adn.adnapp.feature.sports.SportsHomeScreen
import com.adn.adnapp.feature.sports.SportsAnalysisScreen
import com.adn.adnapp.feature.recovery.RecoveryScreen
import com.adn.adnapp.feature.finance.FinanceHomeScreen
import com.adn.adnapp.feature.finance.FinanceAnalysisScreen
import com.adn.adnapp.feature.profile.ProfileScreen

private enum class AreaTab(val route: String, val title: String) {
    HOME("home", "Home"),
    ANALYSIS("dashboard", "Análisis"),
    FRIDGE("fridge", "Nevera"),
    SOON("soon", "Próximamente"),
    SETTINGS("settings", "Ajustes")
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onNavigateToSplash: () -> Unit, onNavigateToDietSelection: () -> Unit) {
    var activeArea by rememberSaveable { mutableStateOf(AppArea.NUTRITION) }
    var selectorOpen by rememberSaveable { mutableStateOf(false) }
    var cycleSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var lastHomeSubRoute by rememberSaveable { mutableStateOf<String?>(null) }
    var homeWasRestored by rememberSaveable { mutableStateOf(false) }
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
                    Row(
                            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(AreaTab.HOME, AreaTab.ANALYSIS, null, if (activeArea == AppArea.NUTRITION) AreaTab.FRIDGE else AreaTab.SOON, AreaTab.SETTINGS).forEach { tab ->
                                Box(if (tab == null) Modifier.width(84.dp) else Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    if (tab == null) {
                                        AreaSwitchButton(
                                            onOpen = { selectorOpen = true },
                                            onCycle = { lastHomeSubRoute = null; homeWasRestored = false; activeArea = nextArea(activeArea, areaCycle) }
                                        )
                                    } else {
                                        val selected = route == tab.route ||
                                            (tab == AreaTab.ANALYSIS && route == Screen.DayViewer.route) ||
                                            (tab == AreaTab.HOME && route in listOf(Screen.FoodSearch.route, Screen.FreshMarket.route, Screen.SavedFoods.route, Screen.CustomDish.route, Screen.Wellness.route, Screen.WellnessSun.route)) ||
                                            (tab == AreaTab.FRIDGE && route == Screen.SavedFoods.route) ||
                                            (tab == AreaTab.SETTINGS && route == Screen.Profile.route)
                                        CellNavigationButton(
                                            label = if (tab == AreaTab.SOON) "Más" else tab.title,
                                            icon = when (tab) {
                                                AreaTab.HOME -> NavigationIcons.Home
                                                AreaTab.ANALYSIS -> NavigationIcons.Analysis
                                                AreaTab.FRIDGE -> NavigationIcons.Fridge
                                                AreaTab.SOON -> NavigationIcons.Soon
                                                AreaTab.SETTINGS -> NavigationIcons.Settings
                                            },
                                            onClick = {
                                                    if (tab == AreaTab.FRIDGE) {
                                                        navController.navigate(Screen.SavedFoods.route) { launchSingleTop = true }
                                                    } else if (tab == AreaTab.HOME) {
                                                        if (homeWasRestored || route == AreaTab.HOME.route) {
                                                            // Once the remembered sub-screen is visible, Home again resets it.
                                                            homeWasRestored = false
                                                            lastHomeSubRoute = null
                                                            navController.navigate(AreaTab.HOME.route) {
                                                                popUpTo(AreaTab.HOME.route) { inclusive = true }
                                                            }
                                                        } else if (lastHomeSubRoute != null) {
                                                            navController.navigate(lastHomeSubRoute!!) { launchSingleTop = true }
                                                            homeWasRestored = true
                                                        } else navController.navigate(AreaTab.HOME.route) { launchSingleTop = true }
                                                    } else {
                                                        if (route in listOf(Screen.FoodSearch.route, Screen.FreshMarket.route, Screen.SavedFoods.route, Screen.CustomDish.route, Screen.Wellness.route, Screen.WellnessSun.route)) {
                                                            lastHomeSubRoute = route
                                                            homeWasRestored = false
                                                        }
                                                        if (tab == AreaTab.SETTINGS) {
                                                            navController.navigate(AreaTab.SETTINGS.route) {
                                                                popUpTo(navController.graph.findStartDestination().id) { saveState = false }
                                                                launchSingleTop = true
                                                                restoreState = false
                                                            }
                                                        } else navController.navigate(tab.route) {
                                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    }
                                                },
                                            modifier = Modifier.widthIn(max = 72.dp).fillMaxWidth().height(76.dp).semantics {
                                                this.selected = selected
                                                contentDescription = tab.title
                                            },
                                            variant = tab.ordinal,
                                            selected = selected
                                        )
                                    }
                                }
                            }
                            // Kept only as a migration alias for old accessibility snapshots;
                            // the visible nutrition tab is now Nevera.
                            if (activeArea == AppArea.NUTRITION) {
                                Box(Modifier.size(0.dp)) {
                                    Text("Más", Modifier.alpha(0f).wrapContentSize(unbounded = true))
                                }
                            }
                        }
                }
            }
        ) { padding ->
            CompositionLocalProvider(LocalFloatingNavigationInset provides padding.calculateBottomPadding()) {
            key(activeArea) {
                // Capture this host's area: outgoing destinations must never read a new mode.
                val hostArea = activeArea
                NavHost(navController, AreaTab.HOME.route,
                Modifier.fillMaxSize().imePadding(),
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None }
                ) {
                    composable(AreaTab.HOME.route) {
                        if (hostArea == AppArea.NUTRITION) NutritionHomeScreen(
                            onSuper = { navController.navigate(Screen.FoodSearch.route) },
                            onMarket = { navController.navigate(Screen.FreshMarket.route) },
                            onDish = { navController.navigate(Screen.CustomDish.route) },
                            onSaved = { navController.navigate(Screen.SavedFoods.route) },
                            onWellness = { navController.navigate(Screen.Wellness.route) },
                            onSun = { navController.navigate(Screen.WellnessSun.route) }
                        ) else if (hostArea == AppArea.AGENDA) AgendaHomeScreen()
                        else if (hostArea == AppArea.SPORTS) SportsHomeScreen(onOpenRecovery = { navController.navigate(Screen.Recovery.route) })
                        else if (hostArea == AppArea.FINANCE) FinanceHomeScreen()
                        else AreaLanding(hostArea, AreaTab.HOME,
                            onSearch = { navController.navigate(Screen.FoodSearch.route) },
                            onDiet = onNavigateToDietSelection,
                            onProfile = { navController.navigate(Screen.Profile.route) })
                    }
                    composable(AreaTab.ANALYSIS.route) {
                        if (hostArea == AppArea.NUTRITION) DashboardScreen(onOpenDay = {
                            navController.navigate(Screen.DayViewer.createRoute(it))
                        }) else if (hostArea == AppArea.AGENDA) AgendaAnalysisScreen(
                            onOpenNutritionDay = { navController.navigate(Screen.DayViewer.createRoute(it)) }
                        ) else if (hostArea == AppArea.SPORTS) SportsAnalysisScreen()
                        else if (hostArea == AppArea.FINANCE) FinanceAnalysisScreen()
                        else AreaLanding(hostArea, AreaTab.ANALYSIS)
                    }
                    composable(AreaTab.FRIDGE.route) {
                        if (hostArea == AppArea.NUTRITION) {
                            FoodSearchScreen(initialSection = FoodSection.SAVED, onBack = { navController.popBackStack() }, fridgeOnly = true)
                        } else AreaLanding(hostArea, AreaTab.SOON)
                    }
                    composable(AreaTab.SOON.route) { AreaLanding(hostArea, AreaTab.SOON) }
                    composable(AreaTab.SETTINGS.route) {
                        if (hostArea == AppArea.NUTRITION) NutritionSettingsScreen(
                            onDiet = onNavigateToDietSelection,
                            onProfile = { navController.navigate(Screen.Profile.route) { launchSingleTop = true } }
                        ) else AreaLanding(hostArea, AreaTab.SETTINGS,
                            onDiet = onNavigateToDietSelection,
                            onProfile = { navController.navigate(Screen.Profile.route) { launchSingleTop = true } },
                            onCycleSettings = { cycleSettingsOpen = true })
                    }
                    if (hostArea == AppArea.NUTRITION) {
                        composable(Screen.FoodSearch.route) {
                            FoodSearchScreen(initialSection = FoodSection.PRODUCTS, onBack = { navController.popBackStack() })
                        }
                        composable(Screen.FreshMarket.route) {
                            FoodSearchScreen(initialSection = FoodSection.FRESH, onBack = { navController.popBackStack() })
                        }
                        composable(Screen.SavedFoods.route) {
                            FoodSearchScreen(initialSection = FoodSection.SAVED, onBack = { navController.popBackStack() }, fridgeOnly = true)
                        }
                        composable(Screen.CustomDish.route) {
                            CustomDishScreen(onBack = { navController.popBackStack() })
                        }
                        composable(Screen.Wellness.route) {
                            WellnessScreen(onBack = { navController.popBackStack() })
                        }
                        composable(Screen.WellnessSun.route) {
                            WellnessScreen(onBack = { navController.popBackStack() }, focusSun = true)
                        }
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
                    if (hostArea == AppArea.SPORTS) {
                        composable(Screen.Recovery.route) { RecoveryScreen(onBack = { navController.popBackStack() }) }
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
                }, Modifier.padding(horizontal = 16.dp)) { Text("Configurar cambio rápido · 800 ms") }
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
                                onClick = { lastHomeSubRoute = null; homeWasRestored = false; activeArea = area; selectorOpen = false },
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
                                color = if (activeArea == area) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
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
    Scaffold { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).statusBarsPadding().verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = LocalFloatingNavigationInset.current + 20.dp),
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
                AreaTab.FRIDGE -> "Tu biblioteca de alimentos."
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

@Composable
private fun NutritionSettingsScreen(onDiet: () -> Unit, onProfile: () -> Unit) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 24.dp,
            bottom = LocalFloatingNavigationInset.current + 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Ajustes de nutrición", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Configura tu punto de partida y sigue tu evolución.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        CellSurface(onClick = onDiet, modifier = Modifier.fillMaxWidth().weight(1f)
            .semantics { contentDescription = "Personalizar dieta" }, variant = 4, selected = true) {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                FoodIllustration(FoodIllustration.LEAF, Modifier.size(92.dp))
                Text("Personalizar dieta", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Preferencias, tolerancias y objetivos", textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        CellSurface(onClick = onProfile, modifier = Modifier.fillMaxWidth().weight(1f)
            .semantics { contentDescription = "Perfil y evolución del peso" }, variant = 5, selected = true) {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(NavigationIcons.Profile, contentDescription = null, modifier = Modifier.size(92.dp))
                Text("Perfil y evolución", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Peso, medidas y progreso personal", textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
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
