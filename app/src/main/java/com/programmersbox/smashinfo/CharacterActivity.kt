package com.programmersbox.smashinfo

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.programmersbox.dragswipe.DragSwipeAdapter
import com.programmersbox.flowutils.RecyclerViewScroll
import com.programmersbox.flowutils.clicks
import com.programmersbox.flowutils.collectOnUi
import com.programmersbox.flowutils.scrollReached
import com.programmersbox.helpfulutils.setCustomTitle
import com.programmersbox.loggingutils.Loged
import com.programmersbox.loggingutils.f
import kotlinx.android.synthetic.main.activity_character.*
import kotlinx.android.synthetic.main.character_custom_title.view.*
import kotlinx.android.synthetic.main.spirit_item.view.*
import kotlinx.android.synthetic.main.spirit_loading.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CharacterActivity : AppCompatActivity() {

    private val adapter = SpiritAdapter(mutableListOf())
    private var page = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character)

        spiritRV.adapter = adapter

        spiritRV
            .scrollReached()
            .collectOnUi { if (it == RecyclerViewScroll.END) loadSpirits() }

        loadSpirits()

    }

    private fun loadSpirits() {
        GlobalScope.launch {
            val spirits = SpiritApi.getSpirits(page)
            runOnUiThread {
                adapter.addItems(spirits)
                page++
            }
        }
    }

    inner class SpiritAdapter(dataList: MutableList<Spirit>) : DragSwipeAdapter<Spirit, RenderHolder>(dataList) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RenderHolder =
            layoutInflater.inflate(viewType, parent, false).let {
                if (viewType == R.layout.spirit_loading) LoadingHolder(it)
                else SpiritHolder(it)
            }

        override fun getItemCount(): Int = super.getItemCount() + 1
        override fun getItemViewType(position: Int): Int = if (dataList.getOrNull(position) != null) R.layout.spirit_item else R.layout.spirit_loading
        override fun RenderHolder.onBind(item: Spirit, position: Int) = load(item)
        override fun onBindViewHolder(holder: RenderHolder, position: Int) = holder.load(dataList.getOrNull(position))

    }

    abstract class RenderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun load(item: Spirit?)
    }

    class LoadingHolder(itemView: View) : RenderHolder(itemView) {
        val progress = itemView.progressBar!!
        override fun load(item: Spirit?) {
            Loged.f("Here I am")
        }
    }

    class SpiritHolder(itemView: View) : RenderHolder(itemView) {
        private val image = itemView.spiritImage!!
        val name = itemView.spiritName!!
        val id = itemView.spiritId!!
        private val icon = itemView.spiritIcon!!

        override fun load(item: Spirit?) {
            name.text = item?.name
            id.text = "${item?.id}"
            Glide.with(itemView)
                .load(item?.imageUrl)
                .into(image)
            Glide.with(itemView)
                .load(item?.iconUrl)
                .into(icon)
            itemView
                .clicks()
                .collectOnUi {
                    MaterialAlertDialogBuilder(itemView.context)
                        .setCustomTitle(R.layout.character_custom_title) { charName.text = item?.name }
                        .setMessage(item?.description)
                        .show()
                }
        }
    }

}