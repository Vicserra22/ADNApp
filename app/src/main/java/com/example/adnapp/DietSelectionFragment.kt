package com.example.adnapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adnapp.adapters.DietAdapter
import com.example.adnapp.databinding.FragmentDietSelectionBinding
import com.example.adnapp.models.Diet

class DietSelectionFragment : Fragment() {

    private var _binding: FragmentDietSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var dietAdapter: DietAdapter

    private val diets = listOf(
            Diet("1", "Dieta Mediterránea", "Alta en frutas, verduras, aceite de oliva", "https://example.com/mediterranea.jpg"),
            Diet("2", "Dieta Keto", "Bajo en carbohidratos, alto en grasas", "https://example.com/keto.jpg"),
            Diet("3", "Dieta Vegana", "Sin productos animales", "https://example.com/vegana.jpg")
    )

    private var selectedDiet: Diet? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDietSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dietAdapter = DietAdapter(diets) { diet ->
                selectedDiet = diet
            binding.btnNextDiet.isEnabled = true
        }

        binding.rvDiets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dietAdapter
        }

        binding.btnNextDiet.setOnClickListener {
            // Aquí pasas la dieta seleccionada al siguiente fragmento o guardas
            selectedDiet?.let {
                // TODO: guarda o navega con la info
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
