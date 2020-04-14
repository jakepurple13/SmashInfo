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
        //val f = getApi("https://smashultimatespirits.com/actions/load_more.php?offset=$loadCount")
        //println(f)
        val f = SpiritApi.getSpirits(loadCount)
        println(f.joinToString("\n"))
    }
}