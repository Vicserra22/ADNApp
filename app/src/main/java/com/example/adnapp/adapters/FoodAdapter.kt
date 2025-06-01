package com.example.adnapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.adnapp.R
import com.example.adnapp.databinding.ItemFoodBinding
import com.example.adnapp.models.Product
import com.squareup.picasso.Picasso

class FoodAdapter(
    private val alimentos: List<Product>,
    private val onItemSelected: (Product) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class FoodViewHolder(private val binding: ItemFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product, isSelected: Boolean) {
            fun Double?.format1Dec(): String = String.format("%.1f", this ?: 0.0)

            binding.tvFoodName.text = product.name ?: "Sin nombre"
            binding.tvFoodCalories.text = "Calorías:\n ${product.nutriments?.calories.format1Dec()} kcal"
            binding.tvFoodProteins.text = "Proteínas:\n ${product.nutriments?.proteins.format1Dec()} g"
            binding.tvFoodCarbs.text = "Carbohidratos:\n ${product.nutriments?.carbs.format1Dec()} g"
            binding.tvFoodFats.text = "Grasas:\n ${product.nutriments?.fat.format1Dec()} g"

            if (!product.imageUrl.isNullOrEmpty()) {
                Picasso.get().load(product.imageUrl).into(binding.ivFoodImage)
            } else {
                binding.ivFoodImage.setImageResource(android.R.drawable.ic_menu_report_image)
            }

            val bgRes = if (isSelected) {
                R.drawable.bg_diet_selected // crea este fondo con borde
            } else {
                R.drawable.bg_diet_default // fondo por defecto sin borde
            }
            binding.root.setBackgroundResource(bgRes)

            binding.root.setOnClickListener {
                val prevPos = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(prevPos)
                notifyItemChanged(adapterPosition)
                onItemSelected(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(alimentos[position], position == selectedPosition)
    }

    override fun getItemCount() = alimentos.size

    fun getSelectedProduct(): Product? = if (selectedPosition != RecyclerView.NO_POSITION) {
        alimentos[selectedPosition]
    } else null
}
