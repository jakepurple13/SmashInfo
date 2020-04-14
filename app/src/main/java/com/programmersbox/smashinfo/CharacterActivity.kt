package com.programmersbox.smashinfo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.programmersbox.gsonutils.getObjectExtra
import kotlinx.android.synthetic.main.activity_character.*

class CharacterActivity : AppCompatActivity() {

    private var characters: SmashCharacters? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character)

        characters = intent.getObjectExtra<SmashCharacters>("character", null)

        Glide.with(this)
            .load(characters?.MainImageUrl)
            .into(charInfo)

    }
}