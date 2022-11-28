package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import java.awt.*
import java.awt.image.BufferedImage
import javax.swing.Icon

/** Defines an artificial [Icon] to be used as tree icon for [ContainerLibraryElement]s.*/
class ContainerLibraryElementIcon(private val current: Boolean = false) : Icon {

	companion object {
		private const val BOX_X = 9
		private const val BOX_Y = 5
		private const val BOX_W = 14
		private const val PIN_W = 4

		val ICON = ContainerLibraryElementIcon()

		/** Contains the [Image] version of the programmatically painted [ICON]. */
		val IMAGE by lazy {
			val image = BufferedImage(ICON.iconWidth, ICON.iconHeight, Transparency.TRANSLUCENT)
			val g = image.createGraphics()
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
			ICON.paintIcon(null, g, 0, 0)
			image
		}
	}

	override fun getIconHeight(): Int = 28

	override fun getIconWidth(): Int = 28

	override fun paintIcon(c: Component?, g: Graphics?, x: Int, y: Int) {
		g?.translate(x, y)

		g?.color = if (current) {
			Graphics2DJvm.toAwtColor(Themes.get<GraphTheme>().selection.color.backgroundColor)
		} else {
			Graphics2DJvm.toAwtColor(Themes.get<GraphTheme>().vertice.color.backgroundColor)
		}
		g?.fillRect(BOX_X, BOX_Y, BOX_W, 18)

		g?.color = if (current) {
			Graphics2DJvm.toAwtColor(Themes.get<GraphTheme>().selection.color.foregroundColor)
		} else {
			Graphics2DJvm.toAwtColor(Themes.get<GraphTheme>().vertice.color.foregroundColor)
		}
		g?.drawRect(BOX_X, BOX_Y, BOX_W, 18)

		g?.drawLine(BOX_X, BOX_Y + 5, BOX_X - PIN_W, BOX_Y + + 5)
		g?.drawLine(BOX_X, BOX_Y + 15, BOX_X - PIN_W, BOX_Y + 15)
		g?.drawLine(BOX_X + BOX_W, BOX_Y + 9, BOX_X + BOX_W + PIN_W, BOX_Y + 9)

		g?.translate(-x, -y)
	}
}