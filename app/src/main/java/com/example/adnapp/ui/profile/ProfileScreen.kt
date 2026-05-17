package com.example.adnapp.ui.profile

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.adnapp.R
import com.example.adnapp.WelcomeActivity
import com.example.adnapp.models.Diet
import com.example.adnapp.ui.registration.DietItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val userId = auth.currentUser?.uid

    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var dietData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var editingField by remember { mutableStateOf<Pair<String, String>?>(null) } // Label, Path
    var editingValue by remember { mutableStateOf("") }

    var showDietDialog by remember { mutableStateOf(false) }
    var dietList by remember { mutableStateOf<List<Diet>>(emptyList()) }

    LaunchedEffect(userId) {
        if (userId != null) {
            // Load user data
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        userData = document.get("info") as? Map<String, Any>
                        dietData = document.get("dieta") as? Map<String, Any>
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error loading data", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }

            // Load diets for the dialog
            Firebase.firestore.collection("dietas").get()
                .addOnSuccessListener { result ->
                    dietList = result.map { it.toObject(Diet::class.java) }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6FFE1))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Perfil",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            ProfileField(
                label = stringResource(R.string.nombre),
                value = userData?.get("nombre")?.toString() ?: "No name",
                onEdit = { 
                    editingField = "Nombre" to "info.nombre"
                    editingValue = userData?.get("nombre")?.toString() ?: ""
                }
            )
            ProfileField(
                label = stringResource(R.string.g_nero),
                value = userData?.get("genero")?.toString() ?: "Not defined",
                onEdit = { 
                    editingField = "Género" to "info.genero"
                    editingValue = userData?.get("genero")?.toString() ?: ""
                }
            )
            ProfileField(
                label = stringResource(R.string.edad),
                value = userData?.get("edad")?.toString() ?: "0",
                onEdit = { 
                    editingField = "Edad" to "info.edad"
                    editingValue = userData?.get("edad")?.toString() ?: ""
                }
            )
            ProfileField(
                label = stringResource(R.string.peso_kg),
                value = userData?.get("peso")?.toString() ?: "0",
                onEdit = { 
                    editingField = "Peso" to "info.peso"
                    editingValue = userData?.get("peso")?.toString() ?: ""
                }
            )
            ProfileField(
                label = stringResource(R.string.altura_cm),
                value = userData?.get("altura")?.toString() ?: "0",
                onEdit = { 
                    editingField = "Altura" to "info.altura"
                    editingValue = userData?.get("altura")?.toString() ?: ""
                }
            )
            ProfileField(
                label = stringResource(R.string.dieta_seleccionada),
                value = dietData?.get("nombre")?.toString() ?: "Not assigned",
                onEdit = { 
                    showDietDialog = true
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { logOut(context) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cerrar Sesión", color = Color.White)
            }
        }
    }

    if (showDietDialog) {
        DietSelectionDialog(
            diets = dietList,
            currentDietId = dietData?.get("id")?.toString() ?: "",
            onDismiss = { showDietDialog = false },
            onDietSelected = { diet ->
                if (userId != null) {
                    val dietaMap = mapOf(
                        "id" to diet.id,
                        "nombre" to diet.name,
                        "descripcion" to diet.description,
                        "imagen" to diet.imageUrl,
                        "calorias" to diet.calories,
                        "proteinas" to diet.proteins,
                        "carbos" to diet.carbs,
                        "grasas" to diet.lipids,
                        "azucar" to diet.sugar,
                        "vitD" to diet.vitD,
                        "agua" to diet.water
                    )
                    db.collection("usuarios").document(userId)
                        .update("dieta", dietaMap)
                        .addOnSuccessListener {
                            dietData = dietaMap
                            showDietDialog = false
                            Toast.makeText(context, "Dieta actualizada", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        )
    }

    if (editingField != null) {
        AlertDialog(
            onDismissRequest = { editingField = null },
            title = { Text("Editar ${editingField!!.first}") },
            text = {
                OutlinedTextField(
                    value = editingValue,
                    onValueChange = { editingValue = it },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val fieldPath = editingField!!.second
                    val valueToSave: Any = when {
                        fieldPath.endsWith("edad") || fieldPath.endsWith("peso") || fieldPath.endsWith("altura") -> 
                            editingValue.toIntOrNull() ?: 0
                        else -> editingValue
                    }
                    if (userId != null) {
                        db.collection("usuarios").document(userId)
                            .update(fieldPath, valueToSave)
                            .addOnSuccessListener {
                                // Refresh local state
                                when (fieldPath) {
                                    "info.nombre" -> userData = userData?.toMutableMap()?.apply { put("nombre", editingValue) }
                                    "info.genero" -> userData = userData?.toMutableMap()?.apply { put("genero", editingValue) }
                                    "info.edad" -> userData = userData?.toMutableMap()?.apply { put("edad", editingValue.toIntOrNull() ?: 0) }
                                    "info.peso" -> userData = userData?.toMutableMap()?.apply { put("peso", editingValue.toIntOrNull() ?: 0) }
                                    "info.altura" -> userData = userData?.toMutableMap()?.apply { put("altura", editingValue.toIntOrNull() ?: 0) }
                                }
                                editingField = null
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
                            }
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingField = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DietSelectionDialog(
    diets: List<Diet>,
    currentDietId: String,
    onDismiss: () -> Unit,
    onDietSelected: (Diet) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = "Selecciona una dieta",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(diets) { diet ->
                        DietItem(
                            diet = diet,
                            isSelected = diet.id == currentDietId,
                            onClick = { onDietSelected(diet) }
                        )
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
fun ProfileField(label: String, value: String, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = label, color = Color.Gray, fontSize = 14.sp)
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2E7D32))
            }
        }
    }
}

private fun logOut(context: Context) {
    FirebaseAuth.getInstance().signOut()
    val intent = Intent(context, WelcomeActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(intent)
}
