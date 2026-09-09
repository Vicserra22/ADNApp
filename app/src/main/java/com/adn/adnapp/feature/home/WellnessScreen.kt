package com.adn.adnapp.feature.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import com.adn.adnapp.core.ui.OrganicBackButton
import com.adn.adnapp.core.ui.LocalFloatingNavigationInset
import com.adn.adnapp.data.local.SunLog
import com.adn.adnapp.data.local.WaterLog
import java.util.Locale
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun WellnessScreen(
    viewModel: WellnessViewModel = koinViewModel(),
    onBack: () -> Unit,
    focusSun: Boolean = false
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var sunPosition by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    LaunchedEffect(focusSun, sunPosition) {
        if (focusSun && sunPosition > 0) {
            scrollState.animateScrollTo((sunPosition - with(density) { 86.dp.toPx() }).toInt().coerceAtLeast(0))
        }
    }
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(scrollState)
                .padding(start = 16.dp, end = 16.dp, top = 78.dp, bottom = LocalFloatingNavigationInset.current + 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Rituales diarios", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Registra lo que haces y conserva el historial. La escala del sol describe tiempo, no una recomendación médica.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            WaterCard(state, viewModel)
            SunCard(state, viewModel, Modifier.onGloballyPositioned { sunPosition = it.positionInParent().y.toInt() })
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) }
        }
        OrganicBackButton(onBack, Modifier.align(Alignment.TopStart).padding(start = 10.dp, top = 8.dp))
    }
}

@Composable
private fun WaterCard(state: WellnessUiState, viewModel: WellnessViewModel) {
    val progress = (state.waterTotalMl / state.waterGoalMl).toFloat().coerceIn(0f, 1f)
    Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Agua", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${state.waterTotalMl.pretty()} / ${state.waterGoalMl.pretty()} ml", fontWeight = FontWeight.Bold)
            }
            Box(Modifier.fillMaxWidth().height(350.dp), contentAlignment = Alignment.Center) { Bottle(progress, viewModel::addQuickWater) }
            Text("Toca la botella para añadir 250 ml", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = viewModel::addQuickWater, modifier = Modifier.fillMaxWidth()) { Text("Añadir un vaso · 250 ml") }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.waterInput,
                    onValueChange = viewModel::onWaterInputChanged,
                    label = { Text("Cantidad exacta (ml)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = viewModel::addWaterFromInput) { Text("Añadir") }
            }
            state.waterLogs.take(4).forEach { log -> WaterHistoryRow(log, viewModel::undoWater) }
        }
    }
}

@Composable
private fun Bottle(progress: Float, onTap: () -> Unit) {
    val animated by animateFloatAsState(progress, label = "water-level")
    val bottleGlass = Color(0xFFD9F5FF)
    val bottleInk = Color(0xFF39AEE5)
    val capColor = Color(0xFF176BBA)
    val labelColor = Color(0xFFFFD166)
    Box(Modifier.size(220.dp, 310.dp).semantics {
        contentDescription = "Botella de agua al ${(animated * 100).toInt()} por ciento"
        role = Role.Button
        onClick { onTap(); true }
    }) {
        Canvas(Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures(onTap = { onTap() })
        }) {
            val body = Size(size.width * .7f, size.height * .74f)
            val left = size.width * .15f
            val top = size.height * .2f
            drawRoundRect(bottleGlass, Offset(left, top), body, CornerRadius(34f, 34f))
            drawRoundRect(bottleInk.copy(alpha = .3f), Offset(left, top), body, CornerRadius(34f, 34f), style = Stroke(7f, cap = StrokeCap.Round))
            drawRoundRect(capColor, Offset(size.width * .34f, 0f), Size(size.width * .32f, size.height * .2f), CornerRadius(15f, 15f))
            drawRoundRect(capColor.copy(alpha = .8f), Offset(size.width * .29f, size.height * .14f), Size(size.width * .42f, size.height * .1f), CornerRadius(10f, 10f))
            val fillTop = top + body.height * (1f - animated)
            if (animated > 0f) {
                drawRoundRect(bottleInk.copy(alpha = .84f), Offset(left + 7f, fillTop), Size(body.width - 14f, top + body.height - fillTop - 7f), CornerRadius(27f, 27f))
                drawArc(Color(0xFF8DE5FF), 180f, 180f, false, Offset(left + 7f, fillTop - 10f), Size(body.width - 14f, 20f), style = Stroke(4f))
            }
            drawRoundRect(labelColor, Offset(left + body.width * .14f, top + body.height * .39f), Size(body.width * .72f, body.height * .2f), CornerRadius(18f, 18f))
            drawCircle(Color.White.copy(alpha = .75f), 5f, Offset(left + body.width * .31f, top + body.height * .47f))
            drawLine(bottleInk.copy(alpha = .65f), Offset(left + body.width * .21f, top + 18f), Offset(left + body.width * .21f, top + body.height - 24f), 6f, StrokeCap.Round)
        }
    }
}

@Composable
private fun WaterHistoryRow(log: WaterLog, onUndo: (WaterLog) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("${log.amountMl.pretty()} ml", modifier = Modifier.weight(1f))
        IconButton(onClick = { onUndo(log) }, modifier = Modifier.semantics { contentDescription = "Deshacer ${log.amountMl.pretty()} ml" }) {
            Text("↶", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun SunCard(state: WellnessUiState, viewModel: WellnessViewModel, modifier: Modifier = Modifier) {
    var selectedMinutes by remember(state.sunTotalMinutes) { mutableIntStateOf((state.sunTotalMinutes % 60).coerceIn(0, 60)) }
    Card(modifier = modifier, shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sol", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${state.sunTotalMinutes} min", fontWeight = FontWeight.Bold)
            }
            Text("Escala visual de registro · 12 soles de 5 minutos", color = MaterialTheme.colorScheme.onSurfaceVariant)
            SunScale(selectedMinutes) { selectedMinutes = it }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.sunInput,
                    onValueChange = viewModel::onSunInputChanged,
                    label = { Text("Minutos exactos (opcional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = viewModel::addSunFromInput) { Text("Añadir") }
            }
            Button(onClick = { if (selectedMinutes > 0) viewModel.addSun(selectedMinutes) }, enabled = selectedMinutes > 0, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar ${selectedMinutes} min")
            }
            state.sunLogs.take(4).forEach { log -> SunHistoryRow(log, viewModel::undoSun) }
        }
    }
}

@Composable
private fun SunScale(selectedMinutes: Int, onMinutesChanged: (Int) -> Unit) {
    Column(
        Modifier.fillMaxWidth().heightIn(min = 168.dp).pointerInput(Unit) {
            detectDragGestures { change, _ ->
                change.consume()
                onMinutesChanged(((change.position.x / size.width) * 60f).toInt().coerceIn(0, 60).let { (it / 5) * 5 })
            }
        },
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        (0 until 3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                (1..4).forEach { column ->
                    val index = row * 4 + column
                    SunDot(index * 5 <= selectedMinutes, index * 5, onMinutesChanged)
                }
            }
        }
    }
}

@Composable
private fun SunDot(filled: Boolean, minutes: Int, onMinutesChanged: (Int) -> Unit) {
    val unfilled = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .45f)
    val color = if (filled) Color(0xFFF3B63F) else unfilled
    Box(Modifier.size(64.dp).clickable { onMinutesChanged(minutes) }.semantics { contentDescription = "Sol de $minutes minutos" }) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(color, size.minDimension * .24f, center)
            repeat(8) { ray ->
                val angle = Math.toRadians((ray * 45).toDouble())
                val inner = size.minDimension * .37f
                val outer = size.minDimension * .47f
                drawLine(color, center + Offset((kotlin.math.cos(angle) * inner).toFloat(), (kotlin.math.sin(angle) * inner).toFloat()), center + Offset((kotlin.math.cos(angle) * outer).toFloat(), (kotlin.math.sin(angle) * outer).toFloat()), 2f, StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun SunHistoryRow(log: SunLog, onUndo: (SunLog) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("${log.minutes} min", modifier = Modifier.weight(1f))
        IconButton(onClick = { onUndo(log) }, modifier = Modifier.semantics { contentDescription = "Deshacer ${log.minutes} minutos" }) {
            Text("↶", style = MaterialTheme.typography.titleLarge)
        }
    }
}

private fun Double.pretty() = if (this % 1.0 == 0.0) toInt().toString() else String.format(Locale.getDefault(), "%.1f", this)
