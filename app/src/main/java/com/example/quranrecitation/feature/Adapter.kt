package com.example.quranrecitation.feature

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quranrecitation.R
import com.example.quranrecitation.room.AudioRecord
import java.text.SimpleDateFormat
import java.util.Date

class Adapter(var records : ArrayList<AudioRecord>, var listener: OnItemClickListener) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener{
        var tvFileName : TextView = itemView.findViewById(R.id.tvFilename)
        var tvMeta : TextView = itemView.findViewById(R.id.tvMeta)
        var checkbox : CheckBox = itemView.findViewById(R.id.checkbox)

        init{
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }
        override fun onClick(p0: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                listener.onItemClickListener(position)
            }
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION)
                listener.onItemLongClickListener(position)

            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Adapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itemview_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: Adapter.ViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION){
            var record = records[position]

            var sdf = SimpleDateFormat("dd/MM/yyyy")
            var date = Date(record.timestamp)
            var strDate = sdf.format(date)

            holder.tvFileName.text = record.filename
            holder.tvMeta.text = "${record.duration} $strDate"
        }
    }

    override fun getItemCount(): Int {
        return records.size
    }

}