package io.github.iineineno03k.aaa

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AaaTest {
    @Test
    fun `arranged value is the receiver of act and assert`() =
        arrange {
            "Test User"
        }.act {
            length
        }.assert { result ->
            assertEquals(9, result)
            assertEquals("Test User", this)
        }

    @Test
    fun `anonymous object exposes several arranged values by name`() =
        arrange {
            object {
                val name = "Test User"
                val age = 30
                val permissions = listOf("read", "write")
            }
        }.act {
            "$name ($age)" to permissions.size
        }.assert { (displayName, permissionCount) ->
            assertEquals("Test User (30)", displayName)
            assertEquals(permissions.size, permissionCount)
        }

    @Test
    fun `act can start a test without arrange`() =
        act {
            1 + 2
        }.assert { result ->
            assertEquals(3, result)
        }

    @Test
    fun `phases run once each, in order`() {
        val calls = mutableListOf<String>()

        arrange {
            calls += "arrange"
        }.act {
            calls += "act"
        }.assert {
            calls += "assert"
        }

        assertEquals(listOf("arrange", "act", "assert"), calls)
    }

    @Test
    fun `act runs even when assert is not chained`() {
        var acted = false

        arrange { }.act { acted = true }

        assertTrue(acted)
    }

    @Test
    fun `suspend functions can be called in every phase inside runTest`() =
        runTest {
            arrange {
                delay(1)
                21
            }.act {
                delay(1)
                this * 2
            }.assert { result ->
                delay(1)
                assertEquals(42, result)
            }
        }

    @Test
    fun `a thrown exception is asserted through runCatching`() =
        act {
            runCatching { error("boom") }
        }.assert { result ->
            assertIs<IllegalStateException>(result.exceptionOrNull())
        }
}
