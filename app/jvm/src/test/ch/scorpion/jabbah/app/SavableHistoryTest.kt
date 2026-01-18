package ch.scorpion.jabbah.app

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SavableHistoryTest {

	private val savable1 = FileSavable("/Documents/savable1")
	private val savable2 = FileSavable("/Documents/savable2")
	private val savable3 = FileSavable("/Documents/savable3")

	@BeforeTest
	fun setup() {
		AppTestRule.configure()
	}

	@Test
	fun lastRegisteredShouldBeMostRecentSavable() {
		val history = SavableHistory(maxSize = 2)
		history.register(savable1)
		history.register(savable2)

		assertEquals(2, history.size)
		assertEquals(savable2, history.savables[0] as FileSavable)
		assertEquals(savable1, history.savables[1] as FileSavable)
	}

	@Test
	fun shouldRestrictSize() {
		val history = SavableHistory(maxSize = 2)
		history.register(savable1)
		history.register(savable2)
		history.register(savable3)

		assertEquals(2, history.size)
		assertEquals(savable3, history.savables[0] as FileSavable)
		assertEquals(savable2, history.savables[1] as FileSavable)
	}

	@Test
	fun shouldNotAddReferentialDuplicates() {
		val history = SavableHistory(maxSize = 2)
		history.register(savable1)
		history.register(savable1)

		assertEquals(1, history.size)
		assertEquals(savable1, history.savables[0] as FileSavable)
	}

	@Test
	fun shouldNotAddStructuralDuplicates() {
		val history = SavableHistory(maxSize = 2)
		val savable1 = FileSavable("/Documents/savable1")
		history.register(this@SavableHistoryTest.savable1)
		history.register(savable1)

		assertEquals(1, history.size)
		assertEquals(savable1, history.savables[0] as FileSavable)
	}

	@Test
	fun shouldHandleDuplicateAsMostRecent() {
		val history = SavableHistory(maxSize = 3)
		history.register(savable1)
		history.register(savable2)
		history.register(savable1)

		assertEquals(2, history.size)
		assertEquals(savable1, history.savables[0] as FileSavable)
		assertEquals(savable2, history.savables[1] as FileSavable)
	}
}