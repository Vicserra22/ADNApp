package com.example.adnapp.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adnapp.adapters.FoodAdapter
import com.example.adnapp.databinding.FragmentHomeBinding
import com.example.adnapp.fragments.LoadingFragment
import com.example.adnapp.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class FoodFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: FoodViewModel by viewModels()
    private lateinit var adapter: FoodAdapter
    private val products = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnAddFood.isEnabled = true
        setupRecyclerView()
        setupListeners()
        binding.tvResultCount.visibility = View.INVISIBLE
        observeViewModel()
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                LoadingFragment.show(requireActivity().supportFragmentManager)
            } else {
                LoadingFragment.dismiss(requireActivity().supportFragmentManager)
            }
        }
    }

    private fun setupListeners() {
        binding.buttonSearch.setOnClickListener {
            val query = binding.editTextSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                viewModel.buscarAlimentos(query)
            } else {
                Toast.makeText(requireContext(), "Escribe algo para buscar", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.btnAddFood.setOnClickListener {
            val productoSeleccionado = adapter.getSelectedProduct()
            if (productoSeleccionado != null) {
                mostrarDialogoCantidad(productoSeleccionado)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Busca un producto y seleccionalo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.resultados.observe(viewLifecycleOwner) { lista ->
            products.clear()
            products.addAll(lista)
            val count = lista.size
            binding.tvResultCount.text = "$count resultado${if (count == 1) "" else "s"}"
            binding.tvResultCount.visibility = View.VISIBLE

            adapter.notifyDataSetChanged()
        }
    }

    private fun mostrarDialogoCantidad(product: Product) {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Cantidad en gramos"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Cantidad a añadir (g)")
            .setView(input)
            .setPositiveButton("Añadir") { _, _ ->
                val cantidad = input.text.toString().toDoubleOrNull()
                if (cantidad != null && cantidad > 0) {
                    guardarEnFirebase(product, cantidad)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Introduce una cantidad válida",
                        Toast.LENGTH_SHORT
                    ).show()
                    mostrarDialogoCantidad(product)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun guardarEnFirebase(product: Product, cantidad: Double) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Cálculo por cantidad en gramos (base 100g)
        val calories = product.nutriments?.calories ?: 0.0
        val proteins = product.nutriments?.proteins ?: 0.0
        val carbs = product.nutriments?.carbs ?: 0.0
        val lipids = product.nutriments?.fat ?: 0.0
        val sugar = product.nutriments?.sugars ?: 0.0

        val caloriasCalculadas = calories * cantidad / 100
        val proteinasCalculadas = proteins * cantidad / 100
        val carbohidratosCalculados = carbs * cantidad / 100
        val grasasCalculadas = lipids * cantidad / 100
        val azucarCalculado = sugar * cantidad / 100

        val alimentoMap = mapOf(
            "nombre" to (product.name ?: "Desconocido"),
            "imagen" to product.imageUrl,
            "fecha" to fecha,
            "cantidad" to cantidad,
            "calorias" to caloriasCalculadas,
            "proteinas" to proteinasCalculadas,
            "carbohidratos" to carbohidratosCalculados,
            "grasas" to grasasCalculadas,
            "azucar" to azucarCalculado
        )


        Firebase.firestore
            .collection("usuarios").document(userId)
            .collection("alimentosIngeridos").add(alimentoMap)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Alimento añadido", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }


    // Configuración gradiante
    private fun setupRecyclerView() {
        adapter = FoodAdapter(products) { product ->

        }

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewResults.layoutManager = layoutManager
        binding.recyclerViewResults.adapter = adapter

        binding.recyclerViewResults.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                val totalItemCount = adapter.itemCount

                binding.gradient.isVisible = lastVisibleItem < totalItemCount - 1
            }
        })
    }


}
