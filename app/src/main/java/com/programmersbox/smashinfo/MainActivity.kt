package com.programmersbox.smashinfo

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.programmersbox.dragswipe.Direction
import com.programmersbox.dragswipe.DragSwipeAdapter
import com.programmersbox.dragswipe.DragSwipeUtils
import com.programmersbox.flowutils.clicks
import com.programmersbox.flowutils.collectOnUi
import com.programmersbox.flowutils.longClicks
import com.programmersbox.gsonutils.getObjectExtra
import com.programmersbox.helpfulutils.requestPermissions
import com.programmersbox.helpfulutils.setEnumSingleChoiceItems
import com.programmersbox.helpfulutils.setView
import com.programmersbox.loggingutils.Loged
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.character_custom_title.view.*
import kotlinx.android.synthetic.main.character_full_info.view.*
import kotlinx.android.synthetic.main.character_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val adapter = CharacterAdapter(mutableListOf(), Game.values().random())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(Manifest.permission.INTERNET) {
            if (it.isGranted) Loged.r("Permissions granted")
        }

        characterRV.adapter = adapter

        DragSwipeUtils.setDragSwipeUp(adapter, characterRV, listOf(Direction.UP, Direction.DOWN))

        loadGames(intent.getObjectExtra("gameType", adapter.type))

        gameType
            .clicks()
            .collectOnUi {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Choose a game type")
                    .setEnumSingleChoiceItems(Game.values().map(Game::name).toTypedArray(), adapter.type) { game, d ->
                        gameTypes(game)
                        d.dismiss()
                    }
                    .show()
            }

    }

    private fun loadGames(game: Game) {
        GlobalScope.launch { getCharacters(game)?.sortedBy(SmashCharacters::DisplayName)?.let { runOnUiThread { adapter.addItems(it) } } }
    }

    private fun gameTypes(game: Game) {
        adapter.type = game
        adapter.setListNotify(emptyList())
        loadGames(game)
    }

    inner class CharacterAdapter(dataList: MutableList<SmashCharacters>, var type: Game) : DragSwipeAdapter<SmashCharacters, SmashHolder>(dataList) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmashHolder = SmashHolder(layoutInflater.inflate(viewType, parent, false))

        override fun getItemViewType(position: Int): Int = when (type) {
            Game.SMASH4 -> R.layout.character_item_simple
            Game.ULTIMATE -> R.layout.character_item
        }

        @SuppressLint("InflateParams")
        override fun SmashHolder.onBind(item: SmashCharacters, position: Int) {
            name.text = item.DisplayName
            item.ColorTheme?.toColorInt()?.let(card::setCardBackgroundColor)

            val glide = Glide.with(this@MainActivity).load(item.ThumbnailUrl).listener(LoggingListener<Drawable>())

            when (type) {
                Game.SMASH4 -> glide.into(character)
                Game.ULTIMATE -> glide.into(object : CustomTarget<Drawable>() {
                    override fun onLoadCleared(placeholder: Drawable?) = Unit
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) = run { card.background = resource }
                })
            }

            itemView
                .clicks()
                .collectOnUi {
                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setCustomTitle(layoutInflater.inflate(R.layout.character_custom_title, null, false).apply {
                            item.ColorTheme?.toColorInt()?.let(this::setBackgroundColor)
                            charName.text = item.DisplayName
                        })
                        .setView(R.layout.character_full_info) {
                            item.ColorTheme?.toColorInt()?.let(this::setBackgroundColor)
                            Glide.with(this@MainActivity)
                                .asBitmap()
                                .load(item.MainImageUrl)
                                .listener(LoggingListener<Bitmap>())
                                .into(object : CustomTarget<Bitmap>() {
                                    override fun onLoadCleared(placeholder: Drawable?) = Unit
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) =
                                        fullCharInfo.setImageBitmap(BitmapUtils.CropBitmapTransparency(resource))
                                })
                        }
                        .show()
                }

            itemView
                .longClicks()
                .collectOnUi { Loged.r(item) }
        }
    }

    class SmashHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val character = itemView.characterImage!!
        val name = itemView.characterName!!
        val card = itemView.characterCard!!

        init {
            setIsRecyclable(false)
        }
    }

    class LoggingListener<R> : RequestListener<R> {
        override fun onResourceReady(resource: R, model: Any?, target: Target<R>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean = false
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<R>?, isFirstResource: Boolean): Boolean = false
    }

}