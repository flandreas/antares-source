package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import java.awt.Component
import java.awt.Graphics
import javax.swing.Icon

/** Defines an artificial [Icon] to be used as tree icon for [ContainerLibraryElement]s.*/
class ContainerLibraryElementIcon : Icon {

    override fun getIconHeight(): Int = 28

    override fun getIconWidth(): Int = 28

    override fun paintIcon(c: Component?, g: Graphics?, x: Int, y: Int) {
        g?.color = Graphics2DJvm.toAwtColor(Themes.getUITheme<GraphTheme>().vertice.color.backgroundColor)
        g?.fillRect(5, 5, 14, 18)
        g?.color = Graphics2DJvm.toAwtColor(Themes.getUITheme<GraphTheme>().vertice.color.foregroundColor)
        g?.drawRect(5, 5, 14, 18)

        g?.drawLine(5, 10, 1, 10)
        g?.drawLine(5, 20, 1, 20)
        g?.drawLine(21, 14, 25, 14)
    }
}