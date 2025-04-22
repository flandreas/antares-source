package ch.scorpion.jabbah.base

import kotlin.test.Test
import kotlin.test.assertEquals

class LongValueImplTest {

    @Test
    fun shouldBeEqual() {
        assertEquals(LongValueImpl(44), LongValueImpl(44))
    }
}