package com.example.nttcinemas.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nttcinemas.Models.Ticket
import com.example.nttcinemas.R

class TicketAdapter(
    private val tickets: List<Ticket>,
    private val onItemClick: (Ticket) -> Unit
) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    // ViewHolder đại diện cho từng item trong danh sách
    inner class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tvMovieTitle)
        private val dateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        private val cinema: TextView = itemView.findViewById(R.id.tvCinema)
        private val totalPrice: TextView = itemView.findViewById(R.id.tvTotalPrice)

        // Gán dữ liệu từ Ticket vào giao diện
        @SuppressLint("SetTextI18n")
        fun bind(ticket: Ticket) {
            title.text = ticket.movieTitle
            dateTime.text = ticket.dateTime
            cinema.text = "Rạp: ${ticket.cinema}" // Hiển thị tên rạp
            totalPrice.text = "Tổng tiền: ${ticket.price} đ" // Hiển thị tổng tiền với đơn vị

            // Xử lý sự kiện click
            itemView.setOnClickListener { onItemClick(ticket) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        // Inflate layout cho từng item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        holder.bind(tickets[position]) // Gán dữ liệu cho ViewHolder
    }

    override fun getItemCount(): Int = tickets.size // Số lượng item trong danh sách
}
