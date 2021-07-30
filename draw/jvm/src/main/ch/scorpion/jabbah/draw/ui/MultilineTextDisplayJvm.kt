package ch.scorpion.jabbah.draw.ui

import ch.scorpion.jabbah.base.text.StyledText
import ch.scorpion.jabbah.draw.drawable.MultilineText
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
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
			it.draw(jg)
		}
	}

	private fun updateMultilineText() {
		if (styledText != null) {
			text = MultilineText(styledText!!, Graphics2DJvm.fromAwtFont(font!!), width.toDouble())
			invalidate()
			repaint()
		} else {
			text = null
		}
	}
}