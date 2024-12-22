package com.example.nttcinemas.Admin

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nttcinemas.Models.Banner
import com.example.nttcinemas.R

class BannerAdapter(
    private val banners: List<Banner>,
    private val onEditClick: (Banner) -> Unit,
    private val onDeleteClick: (Banner) -> Unit
) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bannerImage: ImageView = itemView.findViewById(R.id.bannerImage)
        val bannerName: TextView = itemView.findViewById(R.id.bannerName)
        val bannerDateAdded: TextView = itemView.findViewById(R.id.bannerDateAdded)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val banner = banners[position]
        holder.bannerName.text = banner.name
        holder.bannerDateAdded.text = "Ngày thêm: ${banner.dateAdded}"
        Glide.with(holder.itemView.context).load(banner.image).into(holder.bannerImage)

        holder.btnEdit.setOnClickListener { onEditClick(banner) }
        holder.btnDelete.setOnClickListener { onDeleteClick(banner) }
    }

    override fun getItemCount() = banners.size
}
