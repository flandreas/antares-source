package ch.scorpion.jabbah.app

import org.hamcrest.Matchers.`is`
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TestRule

/** Unit tests for [SavableHistory].*/
class SavableHistoryTest {

    companion object {

        private val SAVABLE_1 = FileSavable("/Documents/savable1")
        private val SAVABLE_2 = FileSavable("/Documents/savable2")
        private val SAVABLE_3 = FileSavable("/Documents/savable3")

        @ClassRule
        @Suppress("JoinDeclarationAndAssignment")
        lateinit var testRule: TestRule

        init {
            testRule = AppTestRule()
        }
    }

    @Test
    fun lastRegisteredShouldBeMostRecentSavable() {
        val history = SavableHistory(maxSize = 2)
        history.register(SAVABLE_1)
        history.register(SAVABLE_2)

        assertThat(history.size, `is`(2))
        assertThat(history.savables[0] as FileSavable, `is`(SAVABLE_2))
        assertThat(history.savables[1] as FileSavable, `is`(SAVABLE_1))
    }

    @Test
    fun shouldRestrictSize() {
        val history = SavableHistory(maxSize = 2)
        history.register(SAVABLE_1)
        history.register(SAVABLE_2)
        history.register(SAVABLE_3)

        assertThat(history.size, `is`(2))
        assertThat(history.savables[0] as FileSavable, `is`(SAVABLE_3))
        assertThat(history.savables[1] as FileSavable, `is`(SAVABLE_2))
    }

    @Test
    fun shouldNotAddDuplicates() {
        val history = SavableHistory(maxSize = 2)
        history.register(SAVABLE_1)
        history.register(SAVABLE_1)

        assertThat(history.size, `is`(1))
        assertThat(history.savables[0] as FileSavable, `is`(SAVABLE_1))
    }

    @Test
    fun shouldHandleDuplicateAsMostRecent() {
        val history = SavableHistory(maxSize = 3)
        history.register(SAVABLE_1)
        history.register(SAVABLE_2)
        history.register(SAVABLE_1)

        assertThat(history.size, `is`(2))
        assertThat(history.savables[0] as FileSavable, `is`(SAVABLE_1))
        assertThat(history.savables[1] as FileSavable, `is`(SAVABLE_2))
    }
}