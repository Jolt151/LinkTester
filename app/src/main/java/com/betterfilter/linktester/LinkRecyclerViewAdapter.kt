package com.betterfilter.linktester

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_link.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class LinkRecyclerViewAdapter(val links: List<Link>, val onDeleteButtonClicked: (Link) -> (Unit)): RecyclerView.Adapter<LinkRecyclerViewAdapter.LinkHolder>() {
    class LinkHolder(v: View): RecyclerView.ViewHolder(v)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkHolder {
        return LinkHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_link, parent, false))
    }

    override fun getItemCount(): Int {
        return links.size
    }

    override fun onBindViewHolder(holder: LinkHolder, position: Int) {
        holder.itemView.linkTV.text = links[position].link
        holder.itemView.time.text = links[position].time?.toString() ?: "time"
        holder.itemView.progressBar.visibility =
            if (links[position].isLoading) View.VISIBLE
            else View.GONE

        holder.itemView.deleteButton.onClick {
            onDeleteButtonClicked(links[position])
        }
    }

}