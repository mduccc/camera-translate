package com.indieteam.cameratranslate.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.indieteam.cameratranslate.R
import com.indieteam.cameratranslate.collection.TranslateCollection
import kotlinx.android.synthetic.main.item_layout.view.*

class RecyclerViewAdpater(val context: Context, val data: ArrayList<TranslateCollection>): RecyclerView.Adapter<RecyclerViewAdpater.MyViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.item_layout, p0, false)

        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
        val view = p0.itemView
        view.apply {
            word_raw.text = data[p1].word_raw
            word_translate.text = data[p1].word_translate
        }
    }

    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view)
}