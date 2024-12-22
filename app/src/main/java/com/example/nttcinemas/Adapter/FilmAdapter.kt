package com.example.nttcinemas.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nttcinemas.Models.Film
import com.example.nttcinemas.R

class FilmAdapter(
    private val films: List<Film>,
    private val onEditClick: (Film) -> Unit = {},
    private val onDeleteClick: (Film) -> Unit = {}
) : RecyclerView.Adapter<FilmAdapter.FilmViewHolder>() {

    class FilmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val filmPoster: ImageView = itemView.findViewById(R.id.filmPoster)
        val filmTitle: TextView = itemView.findViewById(R.id.filmTitle)
        val filmTime: TextView = itemView.findViewById(R.id.filmTime)
        val filmReleaseDate: TextView = itemView.findViewById(R.id.filmReleaseDate)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_film, parent, false)
        return FilmViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        val film = films[position]
        holder.filmTitle.text = film.title
        holder.filmTime.text = "Thời lượng: ${film.time}"
        holder.filmReleaseDate.text = "Khởi chiếu: ${film.releaseDate}"

        // Load poster bằng Glide
        Glide.with(holder.itemView.context)
            .load(film.poster)
            .placeholder(R.drawable.ic_avatar)
            .error(R.drawable.ic_error)
            .into(holder.filmPoster)

        // Xử lý nút sửa và xóa
        holder.btnEdit.setOnClickListener { onEditClick(film) }
        holder.btnDelete.setOnClickListener { onDeleteClick(film) }
    }

    override fun getItemCount() = films.size
}
