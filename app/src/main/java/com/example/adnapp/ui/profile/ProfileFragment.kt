package com.example.adnapp.ui.profile

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.adnapp.DietSelectionDialogFragment
import com.example.adnapp.R
import com.example.adnapp.WelcomeActivity
import com.example.adnapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var tvNameLabel: TextView
    private lateinit var tvNameValue: TextView
    private lateinit var btnEditName: ImageButton

    private lateinit var tvGenderLabel: TextView
    private lateinit var tvGenderValue: TextView
    private lateinit var btnEditGender: ImageButton

    private lateinit var tvAgeLabel: TextView
    private lateinit var tvAgeValue: TextView
    private lateinit var btnEditAge: ImageButton

    private lateinit var tvWeighLabel: TextView
    private lateinit var tvWeightValue: TextView
    private lateinit var btnEditWeight: ImageButton

    private lateinit var tvHeightLabel: TextView
    private lateinit var tvHeightValue: TextView
    private lateinit var btnEditHeight: ImageButton

    private lateinit var tvDietLabel: TextView
    private lateinit var tvDietValue: TextView
    private lateinit var btnEditDiet: ImageButton

    private lateinit var btnLogOut: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val blockName = view.findViewById<View>(R.id.blockName)
        tvNameLabel = blockName.findViewById(R.id.tvFieldLabel)
        tvNameValue = blockName.findViewById(R.id.tvFieldValue)
        btnEditName = blockName.findViewById(R.id.btnEditField)

        val blockGender = view.findViewById<View>(R.id.blockGender)
        tvGenderLabel = blockGender.findViewById(R.id.tvFieldLabel)
        tvGenderValue = blockGender.findViewById(R.id.tvFieldValue)
        btnEditGender = blockGender.findViewById(R.id.btnEditField)

        val blockAge = view.findViewById<View>(R.id.blockAge)
        tvAgeLabel = blockAge.findViewById(R.id.tvFieldLabel)
        tvAgeValue = blockAge.findViewById(R.id.tvFieldValue)
        btnEditAge = blockAge.findViewById(R.id.btnEditField)

        val blockWeight = view.findViewById<View>(R.id.blockWeight)
        tvWeighLabel = blockWeight.findViewById(R.id.tvFieldLabel)
        tvWeightValue = blockWeight.findViewById(R.id.tvFieldValue)
        btnEditWeight = blockWeight.findViewById(R.id.btnEditField)

        val blockHeight = view.findViewById<View>(R.id.blockHeight)
        tvHeightLabel = blockHeight.findViewById(R.id.tvFieldLabel)
        tvHeightValue = blockHeight.findViewById(R.id.tvFieldValue)
        btnEditHeight = blockHeight.findViewById(R.id.btnEditField)

        val blockDiet = view.findViewById<View>(R.id.blockDiet)
        tvDietLabel = blockDiet.findViewById(R.id.tvFieldLabel)
        tvDietValue = blockDiet.findViewById(R.id.tvFieldValue)
        btnEditDiet = blockDiet.findViewById(R.id.btnEditField)

        btnLogOut = view.findViewById(R.id.btnLogOut)

        loadUserDataFromFirebase()

        // Edit buttons
        btnEditName.setOnClickListener {
            showEditDialog("Name", tvNameValue.text.toString()) { newValue ->
                updateFieldInFirebase("info.nombre", newValue)
            }
        }

        btnEditGender.setOnClickListener {
            showEditDialog("Gender", tvGenderValue.text.toString()) { newValue ->
                updateFieldInFirebase("info.genero", newValue)
            }
        }

        btnEditAge.setOnClickListener {
            showEditDialog("Age", tvAgeValue.text.toString()) { newValue ->
                updateFieldInFirebase("info.edad", newValue)
            }
        }

        btnEditWeight.setOnClickListener {
            showEditDialog("Weight", tvWeightValue.text.toString()) { newValue ->
                updateFieldInFirebase("info.peso", newValue)
            }
        }

        btnEditHeight.setOnClickListener {
            showEditDialog("Height", tvHeightValue.text.toString()) { newValue ->
                updateFieldInFirebase("info.altura", newValue)
            }
        }

        btnEditDiet.setOnClickListener {
            DietSelectionDialogFragment { nuevaDieta ->
                binding.blockDiet.tvFieldValue.text = nuevaDieta.name
            }.show(parentFragmentManager, "DietSelectionDialog")
        }

        btnLogOut.setOnClickListener{
            logOut(requireContext())
        }

        tvNameLabel.text = getString(R.string.nombre)
        tvGenderLabel.text = getString(R.string.g_nero)
        tvAgeLabel.text = getString(R.string.edad)
        tvWeighLabel.text = getString(R.string.peso_kg)
        tvHeightLabel.text = getString(R.string.altura_cm)
        tvDietLabel.text = getString(R.string.dieta_seleccionada)



    }
    private fun logOut(context: Context) {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(context, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    private fun loadUserDataFromFirebase() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val info = document.get("info") as? Map<*, *>
                    val dieta = document.get("dieta") as? Map<*, *>
                    if (info != null && dieta != null) {
                        tvNameValue.text = info["nombre"]?.toString() ?: "No name"
                        tvGenderValue.text = info["genero"]?.toString() ?: "Not defined"
                        tvAgeValue.text = (info["edad"] as? Long)?.toString() ?: "0"
                        tvWeightValue.text = (info["peso"] as? Long)?.toString() ?: "0"
                        tvHeightValue.text = (info["altura"] as? Long)?.toString() ?: "0"
                        tvDietValue.text = dieta["nombre"]?.toString() ?: "Not assigned"
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error loading data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateFieldInFirebase(fieldPath: String, newValue: String) {
        val userId = auth.currentUser?.uid ?: return

        val valueToSave: Any = when {
            fieldPath.endsWith("edad") || fieldPath.endsWith("peso") || fieldPath.endsWith("altura") -> newValue.toIntOrNull()
                ?: 0

            else -> newValue
        }

        db.collection("usuarios").document(userId)
            .update(fieldPath, valueToSave)
            .addOnSuccessListener {
                when (fieldPath) {
                    "info.nombre" -> tvNameValue.text = newValue
                    "info.genero" -> tvGenderValue.text = newValue
                    "info.edad" -> tvAgeValue.text = newValue
                    "info.peso" -> tvWeightValue.text = newValue
                    "info.altura" -> tvHeightValue.text = newValue
                    "dieta.nombre" -> tvDietValue.text = newValue
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditDialog(title: String, currentValue: String, onSave: (String) -> Unit) {
        val editText = EditText(requireContext())
        editText.setText(currentValue)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit $title")
            .setView(editText)
            .setPositiveButton("Save") { dialog, _ ->
                val newValue = editText.text.toString().trim()
                if (newValue.isNotEmpty()) {
                    onSave(newValue)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
