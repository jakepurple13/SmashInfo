package com.programmersbox.smashinfo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.programmersbox.dragswipe.DragSwipeAdapter
import com.programmersbox.dragswipe.shuffleItems
import com.programmersbox.flowutils.clicks
import com.programmersbox.flowutils.collectOnUi
import com.programmersbox.flowutils.longClicks
import com.programmersbox.flowutils.textChange
import com.programmersbox.helpfulutils.*
import com.programmersbox.loggingutils.Loged
import kotlinx.android.synthetic.main.activity_spirit.*
import kotlinx.android.synthetic.main.character_custom_title.view.*
import kotlinx.android.synthetic.main.spirit_game_item.view.*
import kotlinx.android.synthetic.main.spirit_game_list_layout.view.*
import kotlinx.android.synthetic.main.spirit_item.view.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class SpiritActivity : AppCompatActivity() {

    private val adapter = SpiritGameAdapter(mutableListOf(), this)
    private val searchAdapter = SpiritAdapter(mutableListOf(), this)
    private var spiritList: List<Spirit>? = null

    @FlowPreview
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spirit)
        requestPermissions(Manifest.permission.INTERNET) { if (it.isGranted) Loged.r("Permissions granted") }
        spiritGameRV.adapter = adapter
        loadSpirits()
        search_info
            .textChange()
            .debounce(500)
            .collectOnUi { search ->
                if (search.isNullOrBlank()) {
                    spiritGameRV.adapter = adapter
                    searchAdapter.setListNotify(emptyList())
                } else {
                    if (spiritGameRV.adapter != searchAdapter) spiritGameRV.adapter = searchAdapter
                    spiritList
                        ?.filter { it.name.contains(search, true) || it.game.contains(search, true) || "${it.id}".contains(search) }
                        ?.let(searchAdapter::setListNotify)
                }
            }

        sortLayout.isSingleSelection = true
        sortLayout.isSingleLine = true

        sizeSort.setOnCheckedChangeListener { _, b -> if (b) adapter.setListNotify(adapter.dataList.sortedByDescending { it.second.size }) }
        idSort.setOnCheckedChangeListener { _, b -> if (b) adapter.setListNotify(adapter.dataList.sortedBy { it.second.first().id }) }
        nameSort.setOnCheckedChangeListener { _, b -> if (b) adapter.setListNotify(adapter.dataList.sortedBy { it.first.name }) }
        randomSort.setOnCheckedChangeListener { _, b -> if (b) adapter.shuffleItems() }
    }

    private fun loadSpirits() {
        GlobalScope.launch {
            val spirits = SpiritApi.getAllSpirits()
                ?.map(Spirits::toSpirit)?.also { spiritList = it }
                ?.groupBy { GameType(it.game, it.iconUrl) }
                .orEmpty()
                .toList()
            runOnUiThread { adapter.addItems(spirits) }
            runOnUiThread { sortLayout.check(idSort.id) }
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------
    data class GameType(val name: String, val icon: String)

    class SpiritGameAdapter(dataList: MutableList<Pair<GameType, List<Spirit>>>, private val context: Context) :
        DragSwipeAdapter<Pair<GameType, List<Spirit>>, SpiritGameHolder>(dataList) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpiritGameHolder =
            SpiritGameHolder(context.layoutInflater.inflate(R.layout.spirit_game_item, parent, false))

        @SuppressLint("SetTextI18n")
        override fun SpiritGameHolder.onBind(item: Pair<GameType, List<Spirit>>, position: Int) {
            name.text = item.first.name
            sizeText.text = item.second.size.toString()
            ids.text = item.second.let { "${it.first().id}-${it.last().id}" }
            Glide.with(itemView)
                .load(item.first.icon)
                .into(icon)
            itemView
                .longClicks()
                .collectOnUi {
                    MaterialAlertDialogBuilder(context)
                        .setCustomTitle(R.layout.character_custom_title) { charName.text = item.first.name }
                        .setView(R.layout.spirit_game_list_layout) { spiritGameListRV.adapter = SpiritAdapter(item.second.toMutableList(), context) }
                        .show()
                }
            itemView
                .clicks()
                .collectOnUi {
                    val itemList = item.second.map(Spirit::id).nonBreakingRanges().toMutableList()
                    if (itemList.size != 1) itemList.add(item.second.map(Spirit::id).toMutableList())
                    MaterialAlertDialogBuilder(context)
                        .setTitle("${item.first.name} Spirit Ids")
                        .setItems(itemList.map { "${it.first()}-${it.last()}" }.toTypedArray()) { _, index ->
                            MaterialAlertDialogBuilder(context)
                                .setCustomTitle(R.layout.character_custom_title) { charName.text = item.first.name }
                                .setView(R.layout.spirit_game_list_layout) {
                                    spiritGameListRV.adapter = SpiritAdapter(item.second.filter { it.id in itemList[index] }.toMutableList(), context)
                                }
                                .show()
                        }
                        .show()
                }
        }

        private fun List<Int>.nonBreakingRanges() = let { list ->
            var lastRange = mutableListOf<Int>()
            list.map {
                val previousElement = lastRange.lastOrNull() ?: it
                if (it == previousElement + 1) {
                    lastRange.add(it)
                } else {
                    lastRange = mutableListOf(it)
                }
                lastRange
            }.distinct()
        }
    }

    class SpiritGameHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.spiritGameName!!
        val icon = itemView.spiritGameIcon!!
        val sizeText = itemView.spiritGameSize!!
        val ids = itemView.spiritGameIds!!
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------
    class SpiritAdapter(dataList: MutableList<Spirit>, private val context: Context) : DragSwipeAdapter<Spirit, SpiritHolder>(dataList) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpiritHolder =
            SpiritHolder(context.layoutInflater.inflate(R.layout.spirit_item, parent, false))

        override fun SpiritHolder.onBind(item: Spirit, position: Int) = load(item)
    }

    class SpiritHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image = itemView.spiritImage!!
        private val name = itemView.spiritName!!
        private val id = itemView.spiritId!!
        private val icon = itemView.spiritIcon!!
        private val card = itemView.spiritCard!!
        private var imageColor: Int? = null
        fun load(item: Spirit?) {
            icon.gone()
            name.text = item?.name
            id.text = "${item?.id}"
            Glide.with(itemView).clear(image)
            image.setImageDrawable(null)
            Glide.with(itemView)
                .asBitmap()
                .load(item?.imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onLoadCleared(placeholder: Drawable?) {}
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        image.setImageBitmap(resource)
                        Palette.from(resource).generate().dominantSwatch?.rgb?.let {
                            card.setCardBackgroundColor(it)
                            imageColor = it
                        }
                    }
                })
            itemView
                .clicks()
                .collectOnUi {
                    MaterialAlertDialogBuilder(itemView.context)
                        .setCustomTitle(R.layout.character_custom_title) {
                            charName.text = item?.name
                            imageColor?.let { it1 -> setBackgroundColor(it1) }
                        }
                        .setMessage(item?.description)
                        .show()
                }
        }
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------
}