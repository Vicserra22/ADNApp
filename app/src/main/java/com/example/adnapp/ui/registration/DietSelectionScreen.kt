package com.example.adnapp.ui.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.adnapp.R
import com.example.adnapp.models.Diet

@Composable
fun DietSelectionScreen(
    diets: List<Diet>,
    onNext: (Diet) -> Unit
) {
    var selectedDiet by remember { mutableStateOf<Diet?>(null) }
    val adnDarkGreen = Color(0xFF398F60)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(diets) { diet ->
                DietItem(
                    diet = diet,
                    isSelected = selectedDiet == diet,
                    onClick = { selectedDiet = diet }
                )
            }
        }

        Button(
            onClick = { selectedDiet?.let { onNext(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            enabled = selectedDiet != null,
            colors = ButtonDefaults.buttonColors(containerColor = adnDarkGreen)
        ) {
            Text(stringResource(R.string.siguiente), color = Color.White)
        }
    }
}

@Composable
fun DietItem(
    diet: Diet,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val adnLightGreen = Color(0xFFC8E6C9)
    val adnBlack = Color(0xFF000000)
    val placeholderColor = Color(0xFF4E4E4E)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(2.dp, Color.Gray, RoundedCornerShape(8.dp)) else Modifier
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) adnLightGreen else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(
                    text = diet.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = adnBlack
                )
                Text(
                    text = diet.description,
                    fontSize = 13.sp,
                    color = placeholderColor,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            AsyncImage(
                model = diet.imageUrl,
                contentDescription = "Imagen dieta",
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DietSelectionScreenPreview() {
    val sampleDiets = listOf(
        Diet(id = "1", name = "Keto", description = "Baja en carbohidratos"),
        Diet(id = "2", name = "Mediterránea", description = "Sana y equilibrada")
    )
    MaterialTheme {
        DietSelectionScreen(diets = sampleDiets, onNext = {})
    }
}
