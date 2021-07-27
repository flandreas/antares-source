package ch.scorpion.jabbah.base

import kotlin.test.Test
import kotlin.test.assertEquals

private class TestData {
	var value = 0
	val cachedData = resettableLazy { value }
}

class ResettableLazyTest {

	private val data = TestData()

	@Test
	fun shouldYieldCachedData() {
		assertEquals(0, data.cachedData.value)
		data.value = 1
		assertEquals(0, data.cachedData.value)
	}

	@Test
	fun shouldYieldNewDataAfterReset() {
		assertEquals(0, data.cachedData.value)
		data.value = 1
		data.cachedData.reset()
		assertEquals(1, data.cachedData.value)
	}
}