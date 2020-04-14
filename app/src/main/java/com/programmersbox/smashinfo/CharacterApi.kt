package com.programmersbox.smashinfo

import com.programmersbox.gsonutils.getJsonApi

data class SmashCharacters(
    val InstanceId: String? = null, val Name: String? = null, val OwnerId: Number? = null, val FullUrl: String? = null,
    val MainImageUrl: String? = null, val ThumbnailUrl: String? = null, val ColorTheme: String? = null,
    val DisplayName: String? = null, val Game: String? = null, val Related: Related? = null, val Links: List<Links>? = null
)

data class Links(val Rel: String?, val Href: String?)

data class Related(val Smash4: Smash4?, val Ultimate: Ultimate?)

data class Smash4(val Self: String?, val Moves: String?, val Movements: String?, val Attributes: String?, val UniqueProperties: String?)

data class Ultimate(val Self: String?, val Moves: String?, val Movements: String?, val Attributes: String?, val UniqueProperties: String?)

fun getCharacters(game: Game = Game.ULTIMATE) = getJsonApi<List<SmashCharacters>>("https://api.kuroganehammer.com/api/characters?game=${game.param}")

enum class Game(val param: String) { SMASH4("smash4"), ULTIMATE("ultimate") }