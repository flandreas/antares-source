package ch.scorpion.jabbah.app

import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [SavableHistory].*/
class SavableHistoryTest {

    companion object {

        private val SAVABLE_1 = FileSavable("/Documents/savable1")
        private val SAVABLE_2 = FileSavable("/Documents/savable2")
        private val SAVABLE_3 = FileSavable("/Documents/savable3")
	    
        init {
            AppTestRule.configure()
        }
    }

    @Test
    fun lastRegisteredShouldBeMostRecentSavable() {
        val history = SavableHistory(maxSize = 2)
        history.register(SAVABLE_1)
        history.register(SAVABLE_2)

        assertEquals(2, history.size)
        assertEquals(SAVABLE_2, history.savables[0] as FileSavable)
        assertEquals(SAVABLE_1, history.savables[1] as FileSavable)
    }

    @Test
    fun shouldRestrictSize() {
        val history = SavableHistory(maxSize = 2)
        history.register(SAVABLE_1)
        history.register(SAVABLE_2)
        history.register(SAVABLE_3)

        assertEquals(2, history.size)
        assertEquals(SAVABLE_3, history.savables[0] as FileSavable)
        assertEquals(SAVABLE_2, history.savables[1] as FileSavable)
    }

    @Test
    fun shouldNotAddReferencialDuplicates() {
        val history = SavableHistory(maxSize = 2)
        history.register(SAVABLE_1)
        history.register(SAVABLE_1)

        assertEquals(1, history.size)
        assertEquals(SAVABLE_1, history.savables[0] as FileSavable)
    }

	@Test
    fun shouldNotAddStructuralDuplicates() {
		val history = SavableHistory(maxSize = 2)
		val savable1 = FileSavable("/Documents/savable1")
		history.register(SAVABLE_1)
		history.register(savable1)

		assertEquals(1, history.size)
		assertEquals(savable1, history.savables[0] as FileSavable)
    }

    @Test
    fun shouldHandleDuplicateAsMostRecent() {
        val history = SavableHistory(maxSize = 3)
        history.register(SAVABLE_1)
        history.register(SAVABLE_2)
        history.register(SAVABLE_1)

        assertEquals(2, history.size)
        assertEquals(SAVABLE_1, history.savables[0] as FileSavable)
        assertEquals(SAVABLE_2, history.savables[1] as FileSavable)
    }
}