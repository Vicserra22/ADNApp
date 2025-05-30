package com.example.adnapp

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adnapp.adapters.DietAdapter
import com.example.adnapp.databinding.DialogDietSelectionBinding
import com.example.adnapp.models.Diet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DietSelectionDialogFragment(
    private val onDietChanged: (Diet) -> Unit
) : DialogFragment() {

    private var _binding: DialogDietSelectionBinding? = null
    private val binding get() = _binding!!

    private val dietList = mutableListOf<Diet>()
    private lateinit var adapter: DietAdapter
    private var selectedDiet: Diet? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogDietSelectionBinding.inflate(LayoutInflater.from(context))

        adapter = DietAdapter(dietList) { diet ->
            selectedDiet = diet
        }

        binding.rvDiets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDiets.adapter = adapter

        loadDietsFromFirestore()

        binding.btnNextDiet.setOnClickListener {
            selectedDiet?.let { diet ->
                updateUserDietInFirestore(diet)
            } ?: Toast.makeText(context, "Selecciona una dieta", Toast.LENGTH_SHORT).show()
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun loadDietsFromFirestore() {
        val db = Firebase.firestore
        db.collection("dietas")
            .get()
            .addOnSuccessListener { result ->
                dietList.clear()
                for (document in result) {
                    val diet = document.toObject(Diet::class.java)
                    dietList.add(diet)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar dietas", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserDietInFirestore(diet: Diet) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = Firebase.firestore.collection("usuarios").document(userId)

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

        userRef.update("dieta", dietaMap)
            .addOnSuccessListener {
                if (isAdded && context != null && !requireActivity().isFinishing) {
                    Toast.makeText(context, "Dieta actualizada", Toast.LENGTH_SHORT).show()
                    onDietChanged(diet)
                    dismiss()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al actualizar dieta", Toast.LENGTH_SHORT).show()
            }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
