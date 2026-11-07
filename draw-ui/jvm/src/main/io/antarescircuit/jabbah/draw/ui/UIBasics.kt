package io.antarescircuit.jabbah.draw.ui

import io.antarescircuit.jabbah.draw.drawable.RichTextDrawable
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.draw.richtext.RichTextLabel
import javax.swing.Icon

object UIBasics {

	val HEADER_FONT = Graphics2DJvm.fromAwtFont(io.antarescircuit.jabbah.base.ui.UIBasics.HEADER_FONT)

	fun createHeaderLabel(text: String, icon: Icon? = null): RichTextLabel =
		RichTextLabel().apply {
			this.icon = icon
			richText = RichTextDrawable.of(text, HEADER_FONT)
		}
}