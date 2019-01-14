package com.daman.solarcalculator

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.daman.solarcalculator.data.entities.UserLocations

class FavoriteAdapter (
    private val context: Context,
    list: ArrayList<UserLocations>,
    private val listener: FavoriteListener
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    private var list = ArrayList<UserLocations>()

    init {
        this.list = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val userLocation = list[position]
        val latitude = String.format("%.4f", userLocation.latitude).toDouble()
        val longitude = String.format("%.4f", userLocation.longitude).toDouble()
        holder.locationNameTextView.text = userLocation.locality
        holder.latlonTextView.text = "($latitude, $longitude)"

        holder.itemView.setOnClickListener {
            listener.onItemClicked(userLocation)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val locationNameTextView: TextView = itemView.findViewById(R.id.locationNameTextView)
        val latlonTextView: TextView = itemView.findViewById(R.id.latlonTextView)
    }

    interface FavoriteListener {
        fun onItemClicked(userLocations: UserLocations)
    }
}