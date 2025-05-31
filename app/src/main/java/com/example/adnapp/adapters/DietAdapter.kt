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
        val tvDesc: TextView = itemView.findViewById(R.id.tvDietDescription)
        val ivImage: ImageView = itemView.findViewById(R.id.ivDietImage)
        val layoutItem: View = itemView.findViewById(R.id.layoutDietItem)

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
        holder.tvTitle.text = diet.name
        holder.tvDesc.text = diet.description

        Picasso.get()
            .load(diet.imageUrl)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.ivImage)

        val bgRes = if (position == selectedPosition) {
            R.drawable.bg_diet_selected
        } else {
            R.drawable.bg_diet_default
        }
        holder.layoutItem.setBackgroundResource(bgRes)
    }

    override fun getItemCount() = diets.size
}
