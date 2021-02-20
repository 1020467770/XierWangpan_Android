package com.example.xierlogintest.adapter.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xierlogintest.R
import kotlinx.android.synthetic.main.basefile_item_layout.view.*

class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val fileLayout: LinearLayout = view.findViewById(R.id.fileLL)
    val iconImage: ImageView = view.findViewById(R.id.fileImage)
    val fileName: TextView = view.findViewById(R.id.fileText)
}

