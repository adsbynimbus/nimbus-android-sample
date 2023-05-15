package com.adsbynimbus.android.sample

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NavigationAdapter(val items: List<NavItem>) : RecyclerView.Adapter<NavigationAdapter.ViewHolder>() {

    class ViewHolder(val view: TextView) : RecyclerView.ViewHolder(view)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, VERTICAL))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false) as TextView).apply {
            view.setOnClickListener { it.findNavController().navigate(items[bindingAdapterPosition].text) }
        }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.text = items[position].text
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is Header -> R.layout.layout_navigation_header
        else -> R.layout.layout_navigation
    }
}

sealed interface NavItem {
    val text: String
}

@JvmInline value class Destination(val route: String) : NavItem {
    override val text: String get() = route
}

@JvmInline value class Header(val name: String) : NavItem {
    override val text: String get() = name
}
