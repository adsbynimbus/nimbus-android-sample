package com.adsbynimbus.android.sample.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.android.sample.databinding.AdapterSampleAppBinding

class SampleAppAdapter<T>(
    private val prefix: String,
    private val dataSet: Array<T>,
    private val onClick: (T) -> Unit,
) : RecyclerView.Adapter<SampleAppAdapter.ViewHolder>() where T : Enum<T>, T : Describable {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.adapter_sample_app, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        AdapterSampleAppBinding.bind(holder.itemView).apply {
            val data = dataSet[position]
            itemName.text = data.description
            itemName.contentDescription = "$prefix${data.identifier}AdapterItemTextView"
            itemContainer.setOnClickListener {
                onClick(data)
            }
        }
        holder.itemView.contentDescription = "$prefix${dataSet[position].identifier}AdapterItem"
    }

    override fun getItemCount() = dataSet.size
}
