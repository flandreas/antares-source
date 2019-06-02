package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.style.EditTheme
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import java.awt.Component
import java.awt.Graphics
import javax.swing.Icon

/** Defines an artificial [Icon] to be used as tree icon for [ContainerLibraryElement]s.*/
class ContainerLibraryElementIcon(private val current: Boolean = false) : Icon {

	override fun getIconHeight(): Int = 28

	override fun getIconWidth(): Int = 28

	override fun paintIcon(c: Component?, g: Graphics?, x: Int, y: Int) {

		if (current) {
			g?.color = Graphics2DJvm.toAwtColor(Themes.get<EditTheme>().selection.color.foregroundColor)
			g?.fillRect(0, 0, iconWidth, iconHeight)
		}

		g?.color = Graphics2DJvm.toAwtColor(Themes.getUITheme<GraphTheme>().vertice.color.backgroundColor)
		g?.fillRect(5, 3, 14, 18)
		g?.color = Graphics2DJvm.toAwtColor(Themes.getUITheme<GraphTheme>().vertice.color.foregroundColor)
		g?.drawRect(5, 3, 14, 18)

		g?.drawLine(5, 8, 1, 8)
		g?.drawLine(5, 18, 1, 18)
		g?.drawLine(21, 12, 25, 12)
	}
}