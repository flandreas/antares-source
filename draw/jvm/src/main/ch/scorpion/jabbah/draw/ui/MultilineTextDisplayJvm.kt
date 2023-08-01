package ch.scorpion.jabbah.draw.ui

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.text.StyledText
import ch.scorpion.jabbah.draw.drawable.RichTextDrawable
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.graphics.PhysicalFontFamily
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JPanel
import kotlin.math.abs

/**
 * Renders a [StyledText] by dynamically recalculating its width when this
 * [MultilineTextDisplayJvm]'s width changes.
 */
class MultilineTextDisplayJvm: JPanel() {

	private var textDrawable: RichTextDrawable? = null

	private val g2Font = FontImpl(
		PhysicalFontFamily(font.name),
		font.style,
		font.size)

	var plainText: String? = null
		set(value) {
			field = value
			updateTextDrawable()
		}

	init {
		addComponentListener(object : ComponentAdapter() {
			override fun componentResized(e: ComponentEvent?) {
				updateTextDrawable()
			}
		})
	}

	override fun paintComponent(g: Graphics) {
		super.paintComponent(g)
		textDrawable?.let {
			val jg = Graphics2DJvm(g as Graphics2D)
			jg.antialiasing = true
			it.draw(jg)
		}
	}

	private fun updateTextDrawable() {
		if (StringUtils.isBlank(plainText)) {
			textDrawable = null
		} else {
			textDrawable = RichTextDrawable.multiline(plainText!!, g2Font, width.toDouble())
			textDrawable!!.moveBy(0.0, abs(textDrawable!!.baselineRect.y))
			invalidate()
			repaint()
		}
	}
}