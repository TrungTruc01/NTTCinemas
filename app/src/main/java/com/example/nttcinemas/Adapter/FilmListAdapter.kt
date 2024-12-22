package com.example.nttcinemas.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.nttcinemas.Activity.FilmDetailActivity
import com.example.nttcinemas.Models.Film
import com.example.nttcinemas.databinding.ViewholderFilmBinding

class FilmListAdapter(
    private val items: ArrayList<Film>
) : RecyclerView.Adapter<FilmListAdapter.Viewholder>() {
    private var context: Context? = null

    inner class Viewholder(private val binding: ViewholderFilmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(film: Film) {
            // Thiết lập hình ảnh với Glide
            val requestOptions = RequestOptions()
                .transform(CenterCrop(), RoundedCorners(30))

            Glide.with(context!!)
                .load(film.poster) // Poster phim
                .apply(requestOptions)
                .into(binding.pic)

            // Khi click vào poster, mở chi tiết phim
            binding.root.setOnClickListener {
                val intent = Intent(context, FilmDetailActivity::class.java)
                intent.putExtra("object", film) // Truyền đối tượng phim sang Activity khác
                context!!.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        context = parent.context
        val binding = ViewholderFilmBinding.inflate(LayoutInflater.from(context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        // Lấy vị trí thực tế trong danh sách (cuộn vô hạn)
        holder.bind(items[position % items.size]) // Vị trí được tính toán bằng mod (%)
    }

    override fun getItemCount(): Int = Int.MAX_VALUE // Số lượng phần tử cực lớn để hỗ trợ cuộn vô hạn
}
