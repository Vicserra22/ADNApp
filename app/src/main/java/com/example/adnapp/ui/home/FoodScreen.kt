package com.example.adnapp.ui.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.tooling.preview.Preview
import com.example.adnapp.models.Nutriments
import com.example.adnapp.R
import com.example.adnapp.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FoodScreen(viewModel: FoodViewModel) {
    val resultados by viewModel.resultados.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    FoodScreenContent(
        resultados = resultados,
        isLoading = isLoading,
        error = error,
        onSearch = { viewModel.buscarAlimentos(it) }
    )
}

@Composable
fun FoodScreenContent(
    resultados: List<Product>,
    isLoading: Boolean,
    error: String?,
    onSearch: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showQuantityDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6FFE1))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.a_ade_los_alimentos_consumidos),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.buscar_producto)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { if (searchQuery.isNotBlank()) onSearch(searchQuery) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (resultados.isNotEmpty()) {
            Text(
                text = "${resultados.size} resultados",
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                EmptyState(message = error, imageRes = R.drawable.adn5)
            } else if (resultados.isEmpty()) {
                EmptyState(
                    message = "¡Bienvenido!\n\n1. Busca productos desde el buscador.\n\n2. Añade la cantidad consumida\n\n3. Visualiza tu progreso en el Dashboard",
                    imageRes = R.drawable.adn_logo
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(resultados) { product ->
                        FoodItem(
                            product = product,
                            isSelected = selectedProduct == product,
                            onClick = { selectedProduct = product }
                        )
                    }
                }
            }
        }

        Button(
            onClick = { showQuantityDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedProduct != null,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(stringResource(R.string.a_adir_alimento), color = Color.White)
        }
    }

    if (showQuantityDialog && selectedProduct != null) {
        QuantityDialog(
            product = selectedProduct!!,
            onDismiss = { showQuantityDialog = false },
            onConfirm = { cantidad ->
                showQuantityDialog = false
            }
        )
    }
}

@Composable
fun EmptyState(message: String, imageRes: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
    }
}

@Composable
fun FoodItem(product: Product, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFC8E6C9) else Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = product.name ?: "Desconocido",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = "Calorías: ${product.nutriments?.calories?.toInt() ?: 0} kcal")
                Text(text = "Proteínas: ${product.nutriments?.proteins?.toInt() ?: 0} g")
                Row {
                    Text(text = "Carbos: ${product.nutriments?.carbs?.toInt() ?: 0} g")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Grasas: ${product.nutriments?.fat?.toInt() ?: 0} g")
                }
            }
        }
    }
}

@Composable
fun QuantityDialog(product: Product, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var quantity by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cantidad a añadir (g)") },
        text = {
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Gramos") }
            )
        },
        confirmButton = {
            Button(onClick = {
                val qty = quantity.toDoubleOrNull()
                if (qty != null && qty > 0) {
                    onConfirm(qty)
                    saveIntoFirebase(product, qty, context)
                } else {
                    Toast.makeText(context, "Introduce una cantidad válida", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun saveIntoFirebase(product: Product, cantidad: Double, context: android.content.Context) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val nutriments = product.nutriments
    val alimentoMap = mapOf(
        "nombre" to (product.name ?: "Desconocido"),
        "imagen" to product.imageUrl,
        "fecha" to fecha,
        "cantidad" to cantidad,
        "calorias" to ((nutriments?.calories ?: 0.0) * cantidad / 100),
        "proteinas" to ((nutriments?.proteins ?: 0.0) * cantidad / 100),
        "carbohidratos" to ((nutriments?.carbs ?: 0.0) * cantidad / 100),
        "grasas" to ((nutriments?.fat ?: 0.0) * cantidad / 100),
        "azucar" to ((nutriments?.sugars ?: 0.0) * cantidad / 100)
    )

    val db = Firebase.firestore
    db.collection("usuarios").document(userId)
        .collection("alimentosIngeridos").add(alimentoMap)
        .addOnSuccessListener {
            Toast.makeText(context, "Alimento añadido", Toast.LENGTH_SHORT).show()

            val docRef = db.collection("usuarios")
                .document(userId)
                .collection("consumoDiario")
                .document(fecha)

            docRef.get().addOnSuccessListener { document ->
                val nuevosDatos = mapOf(
                    "calorias" to (nutriments?.calories ?: 0.0) * cantidad / 100,
                    "proteinas" to (nutriments?.proteins ?: 0.0) * cantidad / 100,
                    "carbos" to (nutriments?.carbs ?: 0.0) * cantidad / 100,
                    "grasas" to (nutriments?.fat ?: 0.0) * cantidad / 100,
                    "azucar" to (nutriments?.sugars ?: 0.0) * cantidad / 100
                )
                if (document.exists()) {
                    val actuales = document.data ?: return@addOnSuccessListener
                    val sumados = nuevosDatos.mapValues { (key, value) ->
                        (actuales[key] as? Double ?: 0.0) + value
                    }
                    docRef.set(sumados)
                } else {
                    docRef.set(nuevosDatos)
                }
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
        }
}

@Preview(showBackground = true)
@Composable
fun FoodScreenPreview() {
    val sampleProducts = listOf(
        Product(
            code = "1",
            name = "Manzana",
            imageUrl = "https://example.com/apple.jpg",
            nutriments = Nutriments(calories = 52.0, fat = 0.2, proteins = 0.3, carbs = 14.0, sugars = 10.0)
        ),
        Product(
            code = "2",
            name = "Plátano",
            imageUrl = "https://example.com/banana.jpg",
            nutriments = Nutriments(calories = 89.0, fat = 0.3, proteins = 1.1, carbs = 22.8, sugars = 12.2)
        )
    )
    MaterialTheme {
        FoodScreenContent(
            resultados = sampleProducts,
            isLoading = false,
            error = null,
            onSearch = {}
        )
    }
}
