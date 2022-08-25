package ch.scorpion.jabbah.draw.ui

import ch.scorpion.jabbah.base.text.StyledText
import ch.scorpion.jabbah.draw.drawable.MultilineText
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.graphics.PhysicalFontFamily
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JPanel

/**
 * Renders a [StyledText] by dynamically recalculating its width when this [MultilineTextDisplayJvm]'s width
 * changes.
 */
class MultilineTextDisplayJvm: JPanel() {

	private var text: MultilineText? = null

	private val g2Font = FontImpl(
		PhysicalFontFamily(font.name),
		font.style,
		font.size)

	var styledText: StyledText? = null
		set(value) {
			field = value
			updateMultilineText()
		}

	init {
		addComponentListener(object : ComponentAdapter() {
			override fun componentResized(e: ComponentEvent?) {
				updateMultilineText()
			}
		})
	}

	override fun paintComponent(g: Graphics) {
		super.paintComponent(g)
		text?.let {
			val jg = Graphics2DJvm(g as Graphics2D)
			jg.antialiasing = true
			jg.font = g2Font
			it.draw(jg)
		}
	}

	private fun updateMultilineText() {
		if (styledText != null) {
			text = MultilineText(styledText!!, g2Font, width.toDouble())
			invalidate()
			repaint()
		} else {
			text = null
		}
	}
}