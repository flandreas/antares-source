package ch.scorpion.jabbah.base

import kotlin.test.Test
import kotlin.test.assertEquals

class SystemTest {

	@Test
	fun shouldBuildToolTipTextWithTitle() {
		val tooltip = System.buildToolTipText("Title", "Text", "Subtext")
		assertEquals("<html><strong>Title:&nbsp;</strong>Text<br><br>Subtext</html>", tooltip)
	}

	@Test
	fun shouldBuildToolTipTextWithoutTitle() {
		val tooltip = System.buildToolTipText(null, "Text", "Subtext")
		assertEquals("<html>Text<br><br>Subtext</html>", tooltip)
	}
}