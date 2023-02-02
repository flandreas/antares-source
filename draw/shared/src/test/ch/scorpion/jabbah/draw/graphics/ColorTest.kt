package ch.scorpion.jabbah.draw.graphics

import kotlin.test.Test
import kotlin.test.assertEquals

class ColorTest {

	@Test
	fun betweenBlackAndWhiteShouldBeGray() {
		assertEquals(Color(128, 128, 128), Color.BLACK.between(Color.WHITE))
	}

	@Test
	fun betweenWhiteAndBlackShouldBeGray() {
		assertEquals(Color(128, 128, 128), Color.WHITE.between(Color.BLACK))
	}

	@Test
	fun quarterBetweenWhiteAndBlackShouldBeLightGray() {
		assertEquals(Color(191, 191, 191), Color.WHITE.between(Color.BLACK, 0.25f))
	}

	@Test
	fun quarterBetweenBlackAndWhiteShouldBeDarkGray() {
		assertEquals(Color(64, 64, 64), Color.BLACK.between(Color.WHITE, 0.25f))
	}
}