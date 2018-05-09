package com.example.vvoitsekh.googlemapskyivsights

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

/**
 * Created by Vlad on 09.05.2018.
 */
class StableArrayAdapter(context: Context, textViewResourceId: Int,
                                 objects: Array<Route>) : ArrayAdapter<Route>(context, textViewResourceId, objects) {

    internal var mIdMap = HashMap<Route, Int>()

    init {
        for (i in objects.indices) {
            mIdMap.put(objects[i], i)
        }
    }


    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        return mIdMap[item]!!.toLong()
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val data = getItem(position)
        if (convertView == null) {
            val viewHolder = ViewHolder()
            var view = LayoutInflater.from(context).inflate(R.layout.listview_item, parent, false)
            viewHolder.points = view.findViewById(R.id.points)
            viewHolder.time = view.findViewById(R.id.time)
            viewHolder.points.text = "places: ${data.points.size}"
            viewHolder.time.text = "time: ${data.time.toMins()}"
            view.tag = viewHolder
            return view
        }
        return convertView


    }

    private inner class ViewHolder {
        lateinit var points: TextView
        lateinit var time: TextView
    }

    private fun Long.toMins(): String {
        return "${this / 3600} hour, ${this % 3600} min "
    }
}