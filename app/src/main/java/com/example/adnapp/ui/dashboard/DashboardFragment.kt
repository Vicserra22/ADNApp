package com.example.adnapp.ui.dashboard

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
                Log.d("DashboardFragment", "Fecha seleccionada: $fechaSeleccionada")

                viewModel.loadDataForDate(fechaSeleccionada)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.combinado.observe(viewLifecycleOwner) { (consumo, objetivos) ->
            Log.d("DashboardFragment", "Datos recibidos: consumo=$consumo, objetivos=$objetivos")
            //updateProgressBar(consumo, objetivos)
            updateUI(consumo, objetivos)
        }
    }

    // Muestra los valores del xml
    private fun updateUI(
        consumo: Map<String, Double>,
        objetivos: Map<String, Double>
    ) {
        updateBarAndText(
            binding.progressCalorias,
            binding.dataCalorias,
            consumo["calorias"],
            objetivos["calorias"],
            "kcal"
        )
        updateBarAndText(
            binding.progressProteinas,
            binding.dataProteinas,
            consumo["proteinas"],
            objetivos["proteinas"],
            "g"
        )
        updateBarAndText(
            binding.progressCarbohidratos,
            binding.dataCarbohidratos,
            consumo["carbos"],
            objetivos["carbos"],
            "g"
        )
        updateBarAndText(
            binding.progressGrasas,
            binding.dataGrasas,
            consumo["grasas"],
            objetivos["grasas"],
            "g"
        )
        updateBarAndText(
            binding.progressAzucar,
            binding.dataAzucar,
            consumo["azucar"],
            objetivos["azucar"],
            "g"
        )
    }

    private fun updateBarAndText(
        progressBar: android.widget.ProgressBar,
        textView: android.widget.TextView,
        actual: Double?,
        objetivo: Double?,
        unidad: String
    ) {
        if (actual != null && objetivo != null && objetivo > 0) {
            val actualInt = actual.toInt()
            val objetivoInt = objetivo.toInt()
            val porcentaje = ((actual / objetivo) * 100).toInt()

            progressBar.progress = porcentaje
            textView.text = "$actualInt / $objetivoInt $unidad"
            updateColor(progressBar, porcentaje)
        } else {
            progressBar.progress = 0
            textView.text = "0 / 0 $unidad"
            updateColor(progressBar, 0)
        }
    }

    private fun updateColor(progressBar: android.widget.ProgressBar, porcentaje: Int) {

        val color = when {
            porcentaje <= 25 -> Color.parseColor("#FF6B6B") // rojo suave
            porcentaje <= 50 -> Color.parseColor("#FFA94D") // naranja claro
            porcentaje <= 65 -> Color.parseColor("#FFD93D") // amarillo
            porcentaje <= 75 -> Color.parseColor("#C8E36A") // amarillo suave
            porcentaje <= 85 -> Color.parseColor("#B2F969") // verde limón
            porcentaje <= 100 -> Color.parseColor("#4CAF50") // verde fuerte
            porcentaje <= 110 -> Color.parseColor("#4CAF50") // verde fuerte
            porcentaje <= 120 -> Color.parseColor("#F9A825") // amarillo oscuro
            porcentaje <= 130 -> Color.parseColor("#FB8C00") // naranja fuerte
            porcentaje <= 150 -> Color.parseColor("#E53935") // rojo intenso
            else -> Color.parseColor("#8B0000") // rojo intenso
        }

        progressBar.progressDrawable.setTint(color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
