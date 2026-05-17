package com.example.adnapp.ui.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.adnapp.R
import com.example.adnapp.UserInfoData

@Composable
fun UserInfoScreen(
    onNext: (UserInfoData) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    val adnDarkGreen = Color(0xFF398F60)
    val backgroundLight = Color(0xFFF8FFF6)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.ingrese_sus_datos),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(top = 12.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Nombre
        Label(text = stringResource(R.string.nombre_usuario))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.ingrese_su_nombre_o_alias)) }
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Edad
        Label(text = stringResource(R.string.edad))
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(stringResource(R.string.introduce_tu_edad)) }
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Sexo
        Label(text = stringResource(R.string.sexo))
        GenderRadioGroup(
            selectedGender = gender,
            onGenderSelected = { gender = it }
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Altura
        Label(text = stringResource(R.string.altura_cm))
        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(stringResource(R.string.introduce_tu_altura)) }
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Peso
        Label(text = stringResource(R.string.peso_kg))
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(stringResource(R.string.introduce_tu_peso)) }
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                val ageInt = age.toIntOrNull()
                val weightInt = weight.toIntOrNull()
                val heightInt = height.toIntOrNull()
                if (name.isNotBlank() && ageInt != null && weightInt != null && heightInt != null && gender.isNotBlank()) {
                    onNext(UserInfoData(name, ageInt, weightInt, heightInt, gender))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = adnDarkGreen)
        ) {
            Text(stringResource(R.string.siguiente), color = Color.White)
        }
    }
}

@Composable
fun Label(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun GenderRadioGroup(
    selectedGender: String,
    onGenderSelected: (String) -> Unit
) {
    val options = listOf(stringResource(R.string.hombre), stringResource(R.string.mujer))
    Row(Modifier.selectableGroup()) {
        options.forEach { text ->
            Row(
                Modifier
                    .selectable(
                        selected = (text == selectedGender),
                        onClick = { onGenderSelected(text) },
                        role = Role.RadioButton
                    )
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedGender),
                    onClick = null // null recommended for accessibility with screen readers
                )
                Text(
                    text = text,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserInfoScreenPreview() {
    MaterialTheme {
        UserInfoScreen(onNext = {})
    }
}
