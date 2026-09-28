package com.veyra.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class CastAdapter(private val castList: List<CastMember>) : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    class CastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.castImage)
        val name: TextView = itemView.findViewById(R.id.castName)
        val character: TextView = itemView.findViewById(R.id.castCharacter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cast, parent, false)
        return CastViewHolder(view)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        val member = castList[position]
        holder.name.text = member.name
        holder.character.text = member.character
        
        if (member.getProfileUrl().isNotEmpty()) {
            holder.image.load(member.getProfileUrl()) { crossfade(true) }
        }
    }

    override fun getItemCount(): Int = castList.size
}