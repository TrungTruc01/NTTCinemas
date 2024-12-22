package com.example.nttcinemas.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nttcinemas.Models.Ticket
import com.example.nttcinemas.R

class ViewPagerAdapter(
    private val pages: List<List<Ticket>>,
    private val onItemClick: (Ticket) -> Unit
) : RecyclerView.Adapter<ViewPagerAdapter.PageViewHolder>() {

    inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_layout, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.recyclerView.layoutManager = LinearLayoutManager(holder.recyclerView.context)
        holder.recyclerView.adapter = TicketAdapter(pages[position], onItemClick)
    }

    override fun getItemCount(): Int = pages.size
}
