package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import java.awt.Component
import java.awt.Graphics
import javax.swing.Icon

/** Defines an artificial [Icon] to be used as tree icon for [ContainerLibraryElement]s.*/
class ContainerLibraryElementIcon(private val current: Boolean = false) : Icon {

	companion object {
		private const val BOX_X = 9
		private const val BOX_W = 14
		private const val PIN_W = 4

		val ICON = ContainerLibraryElementIcon()
	}

	override fun getIconHeight(): Int = 28

	override fun getIconWidth(): Int = 28

	override fun paintIcon(c: Component?, g: Graphics?, x: Int, y: Int) {
		g?.color = if (current) {
			Graphics2DJvm.toAwtColor(Themes.get<GraphTheme>().selection.color.backgroundColor)
		} else {
			Graphics2DJvm.toAwtColor(Themes.get<GraphTheme>().vertice.color.backgroundColor)
		}
		g?.fillRect(BOX_X, 3, BOX_W, 18)

		g?.color = if (current) {
			Graphics2DJvm.toAwtColor(Themes.get<GraphTheme>().selection.color.foregroundColor)
		} else {
			Graphics2DJvm.toAwtColor(Themes.get<GraphTheme>().vertice.color.foregroundColor)
		}
		g?.drawRect(BOX_X, 3, BOX_W, 18)

		g?.drawLine(BOX_X, 8, BOX_X - PIN_W, 8)
		g?.drawLine(BOX_X, 18, BOX_X - PIN_W, 18)
		g?.drawLine(BOX_X + BOX_W, 12, BOX_X + BOX_W + PIN_W, 12)
	}
}