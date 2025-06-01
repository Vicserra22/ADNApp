package com.example.adnapp.ui.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.adnapp.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        viewModel.loadDataDaily()  // Datos del día actual

        observeViewModel()

        return binding.root
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.materialCalendarView.setOnDateChangedListener { _, date, selected ->
            if (selected) {
                val fechaSeleccionada = String.format(
                    "%04d-%02d-%02d", date.year, date.month + 1, date.day
                )
                Toast.makeText(requireContext(), "Fecha: $fechaSeleccionada", Toast.LENGTH_SHORT).show()
                Log.d("DashboardFragment", "Fecha seleccionada: $fechaSeleccionada")

                viewModel.loadDataForDate(fechaSeleccionada)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.combinado.observe(viewLifecycleOwner) { (consumo, objetivos) ->
            Log.d("DashboardFragment", "Datos recibidos: consumo=$consumo, objetivos=$objetivos")
            updateProgressBar(consumo, objetivos)
        }
    }

    private fun updateProgressBar(
        consumo: Map<String, Double>,
        objetivos: Map<String, Double>
    ) {
        updateBar(binding.progressCalorias, consumo["calorias"], objetivos["calorias"])
        updateBar(binding.progressProteinas, consumo["proteinas"], objetivos["proteinas"])
        updateBar(binding.progressCarbohidratos, consumo["carbos"], objetivos["carbos"])
        updateBar(binding.progressGrasas, consumo["grasas"], objetivos["grasas"])
        updateBar(binding.progressAzucar, consumo["azucar"], objetivos["azucar"])
    }

    private fun updateBar(bar: View, actual: Double?, objetivo: Double?) {
        val progressBar = bar as? android.widget.ProgressBar ?: return
        if (actual != null && objetivo != null && objetivo > 0) {
            val porcentaje = ((actual / objetivo) * 100).toInt().coerceAtMost(100)
            progressBar.progress = porcentaje
        } else {
            progressBar.progress = 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
