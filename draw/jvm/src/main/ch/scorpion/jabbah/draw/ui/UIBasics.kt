package ch.scorpion.jabbah.draw.ui

import ch.scorpion.jabbah.draw.drawable.RichTextDrawable
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.richtext.RichTextLabel
import javax.swing.Icon

object UIBasics {

	val HEADER_FONT = Graphics2DJvm.fromAwtFont(ch.scorpion.jabbah.base.ui.UIBasics.HEADER_FONT)

	fun createHeaderLabel(text: String, icon: Icon? = null): RichTextLabel =
		RichTextLabel().apply {
			this.icon = icon
			richText = RichTextDrawable.of(text, HEADER_FONT)
		}
}