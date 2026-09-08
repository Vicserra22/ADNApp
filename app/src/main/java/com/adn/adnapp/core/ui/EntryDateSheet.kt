package com.adn.adnapp.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.adn.adnapp.domain.model.EntryDateChoice

@Composable
fun EntryDateSheet(choice: EntryDateChoice, onChoose: (String) -> Unit, onDismiss: () -> Unit) {
    AppBottomSheet(onDismiss) {
        Text("¿En qué día lo añadimos?", style = MaterialTheme.typography.titleLarge)
        Text("La fecha de esta pantalla no coincide con la fecha actual. Elige dónde guardar la entrada.")
        OutlinedButton({ onChoose(choice.screenDate) }, Modifier.fillMaxWidth()) {
            Text("Día de la pantalla · " + choice.screenDate.readableDate())
        }
        Button({ onChoose(choice.today) }, Modifier.fillMaxWidth()) {
            Text("Día actual · " + choice.today.readableDate())
        }
        TextButton(onDismiss, Modifier.fillMaxWidth()) { Text("Seguir editando") }
    }
}

fun String.readableDate(): String = runCatching {
    LocalDate.parse(this).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}.getOrDefault(this)

