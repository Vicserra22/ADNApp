package com.example.adnapp.ui.home

import android.annotation.SuppressLint
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
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adnapp.R
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
    private val viewModel: FoodViewModel by activityViewModels()
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
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        if (viewModel.resultados.value.isNullOrEmpty()) {
            binding.clRecycledView.visibility = View.INVISIBLE
            binding.clEmptyState.visibility = View.VISIBLE
        } else {
            binding.clRecycledView.visibility = View.VISIBLE
            products.clear()
            viewModel.resultados.value?.let { products.addAll(it) }
            adapter.notifyDataSetChanged()
            binding.tvResultCount.text = "${products.size} resultado${if (products.size == 1) "" else "s"}"
            binding.tvResultCount.visibility = View.VISIBLE
            binding.recyclerViewResults.visibility = View.VISIBLE
            binding.clEmptyState.visibility = View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                LoadingFragment.show(requireActivity().supportFragmentManager)
                binding.recyclerViewResults.visibility = View.GONE
                binding.tvResultCount.visibility = View.GONE
                binding.clEmptyState.visibility = View.GONE
            } else {
                LoadingFragment.dismiss(requireActivity().supportFragmentManager)
            }
        }
    }

    private fun setupListeners() {
        binding.buttonSearch.setOnClickListener {
            val query = binding.editTextSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                adapter.clearSelection()
                binding.btnAddFood.isEnabled = false
                viewModel.buscarAlimentos(query)
                binding.clRecycledView.visibility = View.VISIBLE
            } else {
                Toast.makeText(requireContext(), "Escribe algo para buscar", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAddFood.setOnClickListener {
            val productoSeleccionado = adapter.getSelectedProduct()
            if (productoSeleccionado != null) {
                showQuantityDialog(productoSeleccionado)
            } else {
                Toast.makeText(requireContext(), "Selecciona un producto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun observeViewModel() {
        viewModel.resultados.observe(viewLifecycleOwner) { lista ->
            products.clear()
            products.addAll(lista)
            adapter.notifyDataSetChanged()

            val count = lista.size
            if (count > 0) {
                binding.recyclerViewResults.visibility = View.VISIBLE
                binding.tvResultCount.visibility = View.VISIBLE
                binding.tvResultCount.text = "$count resultado${if (count == 1) "" else "s"}"
                binding.clEmptyState.visibility = View.GONE
            } else {
                binding.recyclerViewResults.visibility = View.GONE
                binding.tvResultCount.visibility = View.GONE
                binding.clEmptyState.visibility = View.VISIBLE
            }

            binding.btnAddFood.isEnabled = false
        }

        viewModel.error.observe(viewLifecycleOwner) { mensajeError ->
            if (!mensajeError.isNullOrEmpty()) {
                showEmptyState(mensajeError, R.drawable.adn5)
            }
        }
    }

    private fun showEmptyState(mensaje: String, imagenResId: Int) {
        binding.clEmptyState.visibility = View.VISIBLE
        binding.tvTextState.text = mensaje
        binding.ivImageState.setImageResource(imagenResId)
    }

    private fun showQuantityDialog(product: Product) {
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
                    saveIntoFirebase(product, cantidad)
                } else {
                    Toast.makeText(requireContext(), "Introduce una cantidad válida", Toast.LENGTH_SHORT).show()
                    showQuantityDialog(product)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveIntoFirebase(product: Product, cantidad: Double) {
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
                Toast.makeText(requireContext(), "Alimento añadido", Toast.LENGTH_SHORT).show()

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
                Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRecyclerView() {
        adapter = FoodAdapter(products) {
            binding.btnAddFood.isEnabled = true
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
