package ch.scorpion.antares.view.graph

import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import java.awt.BasicStroke
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.Icon
import javax.swing.UIManager

open class AntaresMetaGraphIcon(
	private val current: Boolean = false,
	private val scripted: Boolean = false
) : Icon {

	companion object {
		const val BOX_X = 9
		const val BOX_Y = 5
		const val BOX_W = 14
		const val BOX_H = 18
		const val PIN_W = 4
		private const val SCRIPTED_HALF_SIZE = 5

		private val SCRIPTED_STROKE = BasicStroke(1.0f)
	}

	private val backgroundColor by lazy {
		if (current) {
			Graphics2DJvm.toAwtColor(Themes.get<GraphTheme>().selection.color.backgroundColor)
		} else {
			Graphics2DJvm.toAwtColor(Themes.get<GraphTheme>().vertice.color.backgroundColor)
		}
	}

	private val foregroundColor by lazy {
		if (current) {
			Graphics2DJvm.toAwtColor(Themes.get<GraphTheme>().selection.color.foregroundColor)
		} else {
			Graphics2DJvm.toAwtColor(Themes.get<GraphTheme>().vertice.color.foregroundColor)
		}
	}

	private val treeBackgroundColor = UIManager.getColor("Tree.background")

	override fun getIconHeight(): Int = 28

	override fun getIconWidth(): Int = 28

	override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
		g.translate(x, y)

		g.color = backgroundColor
		g.fillRect(BOX_X, BOX_Y, BOX_W, BOX_H)

		g.color = foregroundColor
		g.drawRect(BOX_X, BOX_Y, BOX_W, BOX_H)

		// Pins
		g.drawLine(BOX_X, BOX_Y + 5, BOX_X - PIN_W, BOX_Y + + 5)
		g.drawLine(BOX_X, BOX_Y + 15, BOX_X - PIN_W, BOX_Y + 15)
		g.drawLine(BOX_X + BOX_W, BOX_Y + 9, BOX_X + BOX_W + PIN_W, BOX_Y + 9)

		customize(g)

		if (scripted) {
			g.color = treeBackgroundColor
			g.fillOval(BOX_X + BOX_W - SCRIPTED_HALF_SIZE - 2, BOX_Y + BOX_H - SCRIPTED_HALF_SIZE - 2, 2 * SCRIPTED_HALF_SIZE, 2 * SCRIPTED_HALF_SIZE)

			g.color = backgroundColor
			g.fillOval(BOX_X + BOX_W - SCRIPTED_HALF_SIZE - 2 + 1, BOX_Y + BOX_H - SCRIPTED_HALF_SIZE - 2 + 1, 2 * SCRIPTED_HALF_SIZE - 2, 2 * SCRIPTED_HALF_SIZE - 2)

			g.color = foregroundColor
			(g as Graphics2D).stroke = SCRIPTED_STROKE
			g.drawOval(BOX_X + BOX_W - SCRIPTED_HALF_SIZE - 2 + 1, BOX_Y + BOX_H - SCRIPTED_HALF_SIZE - 2 + 1, 2 * SCRIPTED_HALF_SIZE - 2, 2 * SCRIPTED_HALF_SIZE - 2)
		}

		g.translate(-x, -y)
	}

	protected open fun customize(g: Graphics?) { }
}

class AnalogMetaGraphIcon(current: Boolean) : AntaresMetaGraphIcon(current) {

	companion object {
		private const val INSET = 4
	}

	override fun customize(g: Graphics?) {
		g?.drawLine(BOX_X + INSET, BOX_Y + BOX_H - INSET, BOX_X + BOX_W - INSET, BOX_Y + INSET)
	}
}