package ch.scorpion.jabbah.base.swing

import java.awt.*
import javax.swing.JTextField
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class PlaceholderTextField(
	placeholder: String = "",
	columns: Int = 30,
	private val showClearButton: Boolean = false
) : JTextField(columns) {

	companion object {
		private val CLEAR_STROKE = BasicStroke(1.5f)
	}

	var placeholder: String = placeholder

	init {
		if (showClearButton) {
			addMouseListener(ClearClickListener())
		}
	}

	override fun paintComponent(g: Graphics?) {
		super.paintComponent(g)

		if (placeholder.isNotBlank() && text.isEmpty()) {
			drawPlaceholder(g as Graphics2D)
		}

		if (showClearButton && text.isNotEmpty()) {
			drawClear(g as Graphics2D)
		}
	}

	private fun drawPlaceholder(g2: Graphics2D) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
		g2.color = disabledTextColor
		g2.drawString(placeholder, insets.left, g2.fontMetrics.maxAscent + insets.top)
	}

	private fun drawClear(g2: Graphics2D) {
		val rect = getClearIconRectangle()

		g2.color = disabledTextColor
		g2.fillOval(rect.x, rect.y, rect.width, rect.height)

		g2.color = background
		g2.stroke = CLEAR_STROKE
		g2.drawLine(
			(rect.x + rect.width * 0.3 + 1).toInt(), (rect.y + rect.height * 0.3 + 1).toInt(),
			(rect.x + rect.width * 0.7 + 1).toInt(), (rect.y + rect.height * 0.7 + 1).toInt())
		g2.drawLine(
			(rect.x + rect.width * 0.3 + 1).toInt(), (rect.y + rect.height * 0.7 + 1).toInt(),
			(rect.x + rect.width * 0.7 + 1).toInt(), (rect.y + rect.height * 0.3 + 1).toInt())
	}

	private fun getClearIconRectangle(): Rectangle {
		val size = height - insets.top - insets.bottom
		return Rectangle(width - insets.right - size, insets.top, size, size)
	}

	private inner class ClearClickListener : MouseAdapter() {

		override fun mouseClicked(e: MouseEvent?) {
			if (getClearIconRectangle().contains(e!!.point)) {
				text = ""
			}
		}
	}
}