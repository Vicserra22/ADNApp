package com.example.adnapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.adnapp.R
import com.example.adnapp.models.Diet
import com.squareup.picasso.Picasso

class DietAdapter(
    private val diets: List<Diet>,
    private val onItemClick: (Diet) -> Unit
) : RecyclerView.Adapter<DietAdapter.DietViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class DietViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvDietTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvDietDesc)
        val ivImage: ImageView = itemView.findViewById(R.id.ivDietImage)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    notifyItemChanged(selectedPosition)
                    selectedPosition = position
                    notifyItemChanged(selectedPosition)
                    onItemClick(diets[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DietViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_diet, parent, false)
        return DietViewHolder(view)
    }

    override fun onBindViewHolder(holder: DietViewHolder, position: Int) {
        val diet = diets[position]
        holder.tvTitle.text = diet.title
        holder.tvDesc.text = diet.description

        // Carga la imagen con Picasso o Glide, aquí uso Picasso como ejemplo:
        Picasso.get()
            .load(diet.imageUrl)
            .placeholder(R.drawable.ic_placeholder) // Pon un placeholder en drawable
            .into(holder.ivImage)

        // Cambia el fondo si está seleccionado
        holder.itemView.isSelected = (position == selectedPosition)
        holder.itemView.alpha = if (position == selectedPosition) 1f else 0.7f
    }

    override fun getItemCount() = diets.size
}
