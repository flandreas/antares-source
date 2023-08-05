package ch.scorpion.jabbah.draw.graphics

import kotlin.test.Test
import kotlin.test.assertTrue

class FontImplText {

	@Test
	fun shouldDeriveBoldItalic() {
		val font = FontImpl().deriveFont(FontStyle.BOLD).deriveFont(FontStyle.ITALIC)
		assertTrue(font.isBold())
		assertTrue(font.isItalic())
	}
}