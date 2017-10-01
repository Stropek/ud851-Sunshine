package com.example.android.sunshine

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by p.s.curzytek on 10/1/2017.
 */
class ForecastAdapter : RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder>() {
    var mWaetherData : Array<String>? = null

    override fun onBindViewHolder(holder: ForecastAdapterViewHolder?, position: Int) {
        holder?.mWeatherTextView?.text = mWaetherData!![position]
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ForecastAdapterViewHolder {
        var inflater = LayoutInflater.from(parent?.context)

        var view = inflater.inflate(R.layout.forecast_list_item, parent, false)
        return ForecastAdapterViewHolder(view)
    }

    override fun getItemCount(): Int = if (mWaetherData == null) 0 else mWaetherData?.size as Int

    fun setWaetherData(data: Array<String>) {
        mWaetherData = data
        notifyDataSetChanged()
    }

    class ForecastAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mWeatherTextView : TextView? = null

        init {
            mWeatherTextView = itemView.findViewById(R.id.tv_wather_data) as TextView
        }
    }
}
