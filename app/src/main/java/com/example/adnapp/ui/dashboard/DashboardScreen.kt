package com.example.adnapp.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.adnapp.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    DashboardContent(
        uiState = uiState,
        onDateSelected = { date ->
            viewModel.handleIntent(DashboardIntent.LoadDataForDate(date))
        }
    )
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onDateSelected: (CalendarDay) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6FFE1))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.tus_objetivos_diarios),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            MacroProgressBar(
                label = stringResource(R.string.calor_as),
                actual = uiState.consumo["calorias"] ?: 0.0,
                objetivo = uiState.objetivos["calorias"] ?: 0.0,
                unidad = "kcal"
            )

            MacroProgressBar(
                label = stringResource(R.string.prote_nas),
                actual = uiState.consumo["proteinas"] ?: 0.0,
                objetivo = uiState.objetivos["proteinas"] ?: 0.0,
                unidad = "g"
            )

            MacroProgressBar(
                label = stringResource(R.string.carbohidratos),
                actual = uiState.consumo["carbos"] ?: 0.0,
                objetivo = uiState.objetivos["carbos"] ?: 0.0,
                unidad = "g"
            )

            MacroProgressBar(
                label = stringResource(R.string.grasas),
                actual = uiState.consumo["grasas"] ?: 0.0,
                objetivo = uiState.objetivos["grasas"] ?: 0.0,
                unidad = "g"
            )

            MacroProgressBar(
                label = stringResource(R.string.az_car),
                actual = uiState.consumo["azucar"] ?: 0.0,
                objetivo = uiState.objetivos["azucar"] ?: 0.0,
                unidad = "g"
            )

            Spacer(modifier = Modifier.height(24.dp))

            CalendarComponent(
                selectedDate = uiState.selectedDate,
                decoraciones = uiState.decoraciones,
                onDateSelected = onDateSelected
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MacroProgressBar(label: String, actual: Double, objetivo: Double, unidad: String) {
    val porcentaje = if (objetivo > 0) (actual / objetivo).toFloat() else 0f
    val displayPorcentaje = (porcentaje * 100).toInt()

    val color = when {
        displayPorcentaje <= 25 -> Color(0xFFFF6B6B)
        displayPorcentaje <= 50 -> Color(0xFFFFA94D)
        displayPorcentaje <= 65 -> Color(0xFFFFD93D)
        displayPorcentaje <= 75 -> Color(0xFFC8E36A)
        displayPorcentaje <= 85 -> Color(0xFFB2F969)
        displayPorcentaje <= 110 -> Color(0xFF4CAF50)
        displayPorcentaje <= 120 -> Color(0xFFF9A825)
        displayPorcentaje <= 130 -> Color(0xFFFB8C00)
        displayPorcentaje <= 150 -> Color(0xFFE53935)
        else -> Color(0xFF8B0000)
    }

    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Text(
                text = "${actual.toInt()} / ${objetivo.toInt()} $unidad",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { porcentaje.coerceAtMost(1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
            color = color,
            trackColor = Color(0xFFCCCCCC)
        )
    }
}

@Composable
fun CalendarComponent(
    selectedDate: CalendarDay,
    decoraciones: Map<CalendarDay, Int>,
    onDateSelected: (CalendarDay) -> Unit
) {
    AndroidView(
        factory = { context ->
            MaterialCalendarView(context).apply {
                setOnDateChangedListener { _, date, selected ->
                    if (selected) {
                        onDateSelected(date)
                    }
                }
            }
        },
        update = { view ->
            view.setDateSelected(selectedDate, true)
            view.removeDecorators()

            decoraciones.entries.groupBy { it.value }.forEach { (color, dias) ->
                val diasSet = dias.map { it.key }.toSet()
                view.addDecorator(DateDecorator(diasSet, color))
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    )
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    val sampleUiState = DashboardUiState(
        selectedDate = CalendarDay.today(),
        consumo = mapOf(
            "calorias" to 1500.0,
            "proteinas" to 80.0,
            "carbos" to 200.0,
            "grasas" to 50.0,
            "azucar" to 30.0
        ),
        objetivos = mapOf(
            "calorias" to 2000.0,
            "proteinas" to 120.0,
            "carbos" to 250.0,
            "grasas" to 70.0,
            "azucar" to 50.0
        ),
        decoraciones = emptyMap()
    )
    MaterialTheme {
        DashboardContent(
            uiState = sampleUiState,
            onDateSelected = {}
        )
    }
}
