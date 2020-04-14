package com.programmersbox.smashinfo

import com.programmersbox.gsonutils.getApi
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        println(getCharacters())
    }

    @Test
    fun other() {
        println(getCharacters())
    }

    @Test
    fun other2() {
        var loadCount = 0
        val f = SpiritApi.getSpirits(loadCount)
        val f1 = (0..10).flatMap { SpiritApi.getSpirits(it) }
        val f2 = f1.groupBy(Spirit::game)
        println(f2.entries.joinToString("\n") { "${it.key} has ${it.value.size}" })
    }

    @Test
    fun other3() {
        val f = SpiritApi.getAllSpirits()
        //println(f?.joinToString("\n"))
        val f1 = f?.groupBy { it.series }
        println(f1?.entries?.joinToString("\n") { "${it.key} has ${it.value.size}" })
    }
}