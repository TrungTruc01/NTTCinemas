package com.example.nttcinemas.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.nttcinemas.Models.Seat
import com.example.nttcinemas.R
import com.example.nttcinemas.databinding.SeatItemBinding

class SeatListAdapter(
    private val danhSachGhe: List<Seat>,
    private val context: Context,
    private val khiGheDuocChon: (Seat, Boolean) -> Unit
) : RecyclerView.Adapter<SeatListAdapter.SeatViewHolder>() {

    class SeatViewHolder(val binding: SeatItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        return SeatViewHolder(
            SeatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
        val ghe = danhSachGhe[position]

        // Cập nhật giao diện ghế
        updateSeatAppearance(holder, ghe)

        // Xử lý sự kiện click vào ghế
        holder.binding.seatOverlay.setOnClickListener {
            when (ghe.status) {
                Seat.SeatStatus.AVAILABLE -> {
                    ghe.status = Seat.SeatStatus.SELECTED
                    khiGheDuocChon(ghe, true)
                }
                Seat.SeatStatus.SELECTED -> {
                    ghe.status = Seat.SeatStatus.AVAILABLE
                    khiGheDuocChon(ghe, false)
                }
                Seat.SeatStatus.UNAVAILABLE -> {
                    Toast.makeText(context, "Ghế này đã được đặt!", Toast.LENGTH_SHORT).show()
                }
            }
            // Cập nhật chỉ mục hiện tại
            notifyItemChanged(position)
        }
    }

    // Hàm cập nhật giao diện ghế
    private fun updateSeatAppearance(holder: SeatViewHolder, ghe: Seat) {
        holder.binding.seat.text = ghe.name

        when (ghe.status) {
            Seat.SeatStatus.AVAILABLE -> {
                holder.binding.seat.setBackgroundResource(
                    when (ghe.type) {
                        Seat.SeatType.NORMAL -> R.color.gray
                        Seat.SeatType.VIP -> R.color.dark_red
                        Seat.SeatType.DOUBLE -> R.color.pink
                    }
                )
                holder.binding.seat.setTextColor(context.getColor(R.color.white))
            }
            Seat.SeatStatus.SELECTED -> {
                holder.binding.seat.setBackgroundResource(R.drawable.ic_corner_indicator)
                holder.binding.seat.setTextColor(context.getColor(R.color.black))
            }
            Seat.SeatStatus.UNAVAILABLE -> {
                holder.binding.seat.setBackgroundResource(R.drawable.ic_seat_unavailable)
                holder.binding.seat.setTextColor(context.getColor(R.color.grey))
            }
        }
    }

    override fun getItemCount(): Int = danhSachGhe.size
}
