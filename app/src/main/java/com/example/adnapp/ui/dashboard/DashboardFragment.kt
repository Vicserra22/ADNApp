package com.example.adnapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.adnapp.databinding.FragmentDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val uid = FirebaseAuth.getInstance().currentUser?.uid


    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        observeViewModel()
        viewModel.loadDataDaily()

        return binding.root
    }

    private fun observeViewModel() {
        viewModel.consumoDiario.observe(viewLifecycleOwner) { consumo ->
            viewModel.objetivosDieta.observe(viewLifecycleOwner) { objetivos ->
                updateProgressBar(consumo, objetivos)
            }
        }
    }

    private fun updateProgressBar(consumo: Map<String, Double>, objetivos: Map<String, Double>) {
        updateBar(binding.progressCalorias, consumo["calorias"], objetivos["calorias"])
        updateBar(binding.progressProteinas, consumo["proteinas"], objetivos["proteinas"])
        updateBar(binding.progressCarbohidratos, consumo["carbohidratos"], objetivos["carbohidratos"])
        updateBar(binding.progressGrasas, consumo["grasas"], objetivos["grasas"])
        updateBar(binding.progressAzucar, consumo["azucar"], objetivos["azucar"])
    }

    private fun updateBar(bar: ProgressBar, valor: Double?, objetivo: Double?) {
        if (valor != null && objetivo != null && objetivo > 0) {
            val porcentaje = ((valor / objetivo) * 100).toInt().coerceAtMost(100)
            bar.progress = porcentaje
        } else {
            bar.progress = 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}