package ch.scorpion.jabbah.base.swing

import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import javax.swing.Icon
import javax.swing.UIManager

/**
 * An [Icon] that renders a [Color] as a small rectangle.
 */
class ColorIcon(
    var backgroundColor: Color = DEF_BACKGROUND_COLOR,
    var foregroundColor: Color = DEF_FOREGROUND_COLOR,
    private val width: Int = DEF_SIZE,
    private val height: Int = DEF_SIZE,
	private val oval: Boolean = false
) : Icon {

    companion object {
        private val DEF_BACKGROUND_COLOR = Color.GRAY
        private val DEF_FOREGROUND_COLOR = UIManager.getColor("controlDkShadow")
        private const val DEF_SIZE = 10
    }

    /** ---- [Icon] interface */

    override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
        g.color = backgroundColor
	    if (oval) {
		    g.fillOval(x, y, width, height)
	    } else {
		    g.fillRect(x, y, width, height)

	    }
        g.color = foregroundColor
	    if (oval) {
		    g.drawOval(x, y, width, height)
	    } else {
		    g.drawRect(x, y, width, height)
	    }
    }

    override fun getIconWidth(): Int = width

    override fun getIconHeight(): Int = height
}