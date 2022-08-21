package com.adsbynimbus.android.sample.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.android.sample.databinding.AdapterSampleAppSectionBinding

class SampleAppSectionAdapter(
    private val prefix: String,
    private val identifier: String,
    private val sectionTitle: String,
) : RecyclerView.Adapter<SampleAppSectionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_sample_app_section, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        AdapterSampleAppSectionBinding.bind(holder.itemView).apply {
            root.contentDescription = "$prefix${identifier}SectionAdapterItem"
            title.text = sectionTitle
            title.contentDescription = "$prefix${identifier}SectionAdapterTextView"
        }
    }

    override fun getItemCount(): Int = 1
}
