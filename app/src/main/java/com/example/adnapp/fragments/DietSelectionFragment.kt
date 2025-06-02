package com.example.adnapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adnapp.DietSelectionData
import com.example.adnapp.SharedViewModel
import com.example.adnapp.adapters.DietAdapter
import com.example.adnapp.databinding.FragmentDietSelectionBinding
import com.example.adnapp.interfaces.OnDietSelectionCompleteListener
import com.example.adnapp.models.Diet
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DietSelectionFragment : Fragment() {

    private var _binding: FragmentDietSelectionBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val dietList = mutableListOf<Diet>()
    private lateinit var adapter: DietAdapter
    private var selectedDiet: Diet? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDietSelectionBinding.inflate(inflater, container, false)

        // Configuración del RecyclerView
        adapter = DietAdapter(dietList) { diet ->
            selectedDiet = diet
        }

        binding.rvDiets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDiets.adapter = adapter

        // Se cargan las dietas desde Firebase
        loadDietsFromFirestore()

        binding.btnNextDiet.setOnClickListener {
            if (selectedDiet != null) {
                sharedViewModel.dietSelectionData = DietSelectionData(selectedDiet)
                (activity as? OnDietSelectionCompleteListener)?.onDietSelectionComplete()
            } else {
                Toast.makeText(requireContext(), "Selecciona una dieta", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadDietsFromFirestore() {
        val db = Firebase.firestore
        db.collection("dietas") // Collección en Firebase
            .get()
            .addOnSuccessListener { result ->
                dietList.clear()
                for (document in result) {
                    val diet = document.toObject(Diet::class.java)
                    dietList.add(diet)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("DietSelection", "Error al cargar dietas", exception)
                Toast.makeText(requireContext(), "Error al cargar dietas", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
