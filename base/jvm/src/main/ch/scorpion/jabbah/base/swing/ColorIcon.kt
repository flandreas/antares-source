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
    var backgroundColor: Color,
    var foregroundColor: Color,
    private val width: Int,
    private val height: Int
) : Icon {

    constructor(backgroundColor: Color): this(backgroundColor, DEF_FOREGROUND_COLOR, DEF_SIZE, DEF_SIZE)
    constructor(): this(DEF_BACKGROUND_COLOR)

    companion object {
        private val DEF_BACKGROUND_COLOR = Color.GRAY
        private val DEF_FOREGROUND_COLOR = UIManager.getColor("controlDkShadow")
        private val DEF_SIZE = 10
    }

    /** ---- [Icon] interface */

    override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
        g.color = backgroundColor
        g.fillRect(x, y, width, height)
        g.color = foregroundColor
        g.drawRect(x, y, width, height)
    }

    override fun getIconWidth(): Int {
        return width
    }

    override fun getIconHeight(): Int {
        return height
    }
}