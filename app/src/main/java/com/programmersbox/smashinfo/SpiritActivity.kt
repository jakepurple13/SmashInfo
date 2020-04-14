package com.programmersbox.smashinfo

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
import com.programmersbox.flowutils.clicks
import com.programmersbox.flowutils.collectOnUi
import com.programmersbox.helpfulutils.gone
import com.programmersbox.helpfulutils.layoutInflater
import com.programmersbox.helpfulutils.setCustomTitle
import com.programmersbox.helpfulutils.setView
import kotlinx.android.synthetic.main.activity_spirit.*
import kotlinx.android.synthetic.main.character_custom_title.view.*
import kotlinx.android.synthetic.main.spirit_game_item.view.*
import kotlinx.android.synthetic.main.spirit_game_list_layout.view.*
import kotlinx.android.synthetic.main.spirit_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SpiritActivity : AppCompatActivity() {

    private val adapter = SpiritGameAdapter(mutableListOf(), this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spirit)
        spiritGameRV.adapter = adapter
        loadSpirits()
    }

    private fun loadSpirits() {
        GlobalScope.launch {
            val spirits = SpiritApi.getAllSpirits()
                ?.map(Spirits::toSpirit)
                ?.groupBy { GameType(it.game, it.iconUrl) }
                .orEmpty()
                .toList()
            runOnUiThread { adapter.addItems(spirits) }
        }
    }

    data class GameType(val name: String, val icon: String)

    class SpiritGameAdapter(dataList: MutableList<Pair<GameType, List<Spirit>>>, private val context: Context) :
        DragSwipeAdapter<Pair<GameType, List<Spirit>>, SpiritGameHolder>(dataList) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpiritGameHolder =
            SpiritGameHolder(context.layoutInflater.inflate(R.layout.spirit_game_item, parent, false))

        override fun SpiritGameHolder.onBind(item: Pair<GameType, List<Spirit>>, position: Int) {
            name.text = item.first.name
            sizeText.text = item.second.size.toString()
            Glide.with(itemView)
                .load(item.first.icon)
                .into(icon)
            itemView
                .clicks()
                .collectOnUi {
                    MaterialAlertDialogBuilder(context)
                        .setCustomTitle(R.layout.character_custom_title) { charName.text = item.first.name }
                        .setView(R.layout.spirit_game_list_layout) { spiritGameListRV.adapter = SpiritAdapter(item.second.toMutableList(), context) }
                        .show()
                }
        }

    }

    class SpiritGameHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.spiritGameName!!
        val icon = itemView.spiritGameIcon!!
        val sizeText = itemView.spiritGameSize!!
    }

    class SpiritAdapter(dataList: MutableList<Spirit>, private val context: Context) : DragSwipeAdapter<Spirit, SpiritHolder>(dataList) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpiritHolder =
            SpiritHolder(context.layoutInflater.inflate(R.layout.spirit_item, parent, false))

        override fun SpiritHolder.onBind(item: Spirit, position: Int) = load(item)
    }

    class SpiritHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image = itemView.spiritImage!!
        val name = itemView.spiritName!!
        val id = itemView.spiritId!!
        private val icon = itemView.spiritIcon!!
        private val card = itemView.spiritCard!!
        private var imageColor: Int? = null

        fun load(item: Spirit?) {
            icon.gone()
            name.text = item?.name
            id.text = "${item?.id}"
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
}