package ch.scorpion.jabbah.base.swing

import javax.swing.plaf.basic.BasicLabelUI
import java.awt.*
import javax.swing.*

class VerticalLabel {

    companion object {

        fun create(
                text: String = "",
                icon: Icon? = null,
                horizontalAlignment: Int = SwingConstants.LEFT
        ): JLabel {
            val label = JLabel(text, icon, horizontalAlignment)
            label.ui = VerticalLabelUI.labelUI
            return label
        }
    }
}

class VerticalLabelUI(private val clockwise: Boolean = true) : BasicLabelUI() {

    companion object {
        val labelUI = VerticalLabelUI()
        private val paintIconRect = Rectangle()
        private val paintTextRect = Rectangle()
        private val paintViewRect = Rectangle()
        private val paintViewInsets = Insets(0, 0, 0, 0)
    }

    override fun getPreferredSize(c: JComponent?): Dimension {
        val dim = super.getPreferredSize(c)
        return Dimension(dim.height, dim.width)
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        val label: JLabel = c as JLabel
        val text = label.text
        val icon = if (label.isEnabled) label.icon else label.disabledIcon

        if (icon == null && text == null) {
            return
        }

        val fm = g!!.fontMetrics
        val paintViewInsets = c.getInsets(paintViewInsets)

        paintViewRect.x = paintViewInsets.left
        paintViewRect.y = paintViewInsets.top

        paintViewRect.height = c.getWidth() - (paintViewInsets.left + paintViewInsets.right)
        paintViewRect.width = c.getHeight() - (paintViewInsets.top + paintViewInsets.bottom)

        paintIconRect.height = 0
        paintIconRect.width = 0
        paintIconRect.y = 0
        paintIconRect.x = 0
        paintIconRect.height = 0
        paintIconRect.width = 0
        paintIconRect.y = 0
        paintIconRect.x = 0

        val clippedText = layoutCL(label, fm, text, icon, paintViewRect, paintIconRect, paintIconRect)

        val g2 = g as Graphics2D
        val oldTransform = g2.transform
        if (clockwise) {
            g2.rotate(Math.PI / 2)
            g2.translate(0, -c.getWidth())
        } else {
            g2.rotate(-Math.PI / 2)
            g2.translate(-c.getHeight(), 0)
        }

        icon?.paintIcon(c, g, paintIconRect.x, paintIconRect.y)

        if (text != null) {
            val textX = paintTextRect.x + paintViewInsets.left
            val textY = paintTextRect.y + paintViewInsets.top + fm.ascent

            if (label.isEnabled) {
                paintEnabledText(label, g, clippedText, textX, textY)
            } else {
                paintDisabledText(label, g, clippedText, textX, textY)
            }
        }

        g2.transform = oldTransform
    }
}