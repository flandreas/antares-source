package io.antarescircuit.antares.ai

import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import java.awt.geom.RoundRectangle2D
import javax.swing.Icon
import javax.swing.UIManager

/**
 * A speech bubble drawn with Java2D.
 *
 * Painted instead of shipped as an image so that it always picks up the foreground color of the
 * current theme, which the application switches between light and dark at runtime.
 */
class AiChatIcon(private val size: Int = 16) : Icon {

	override fun getIconWidth(): Int = size

	override fun getIconHeight(): Int = size

	override fun paintIcon(c: Component?, g: Graphics?, x: Int, y: Int) {
		val g2 = g?.create() as? Graphics2D ?: return
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
			g2.color = c?.foreground ?: UIManager.getColor("Label.foreground") ?: Color.GRAY
			g2.translate(x, y)

			val s = size.toDouble()
			val bubbleHeight = s * 0.72
			g2.fill(RoundRectangle2D.Double(s * 0.06, s * 0.1, s * 0.88, bubbleHeight, s * 0.3, s * 0.3))

			val tail = Path2D.Double()
			tail.moveTo(s * 0.28, s * 0.72)
			tail.lineTo(s * 0.28, s * 0.96)
			tail.lineTo(s * 0.52, s * 0.76)
			tail.closePath()
			g2.fill(tail)

			// Three dots suggesting a conversation
			g2.color = c?.background ?: UIManager.getColor("Panel.background") ?: Color.WHITE
			val dot = s * 0.13
			val dotY = s * 0.1 + bubbleHeight / 2 - dot / 2
			listOf(0.22, 0.44, 0.66).forEach { fraction ->
				g2.fill(Ellipse2D.Double(s * fraction, dotY, dot, dot))
			}
		} finally {
			g2.dispose()
		}
	}
}
