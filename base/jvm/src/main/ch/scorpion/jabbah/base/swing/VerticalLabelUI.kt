package ch.scorpion.jabbah.base.swing

import java.awt.*
import javax.swing.*
import javax.swing.plaf.basic.BasicLabelUI
import kotlin.math.PI

class VerticalLabel(
	text: String = "",
	icon: Icon? = null,
	horizontalAlignment: Int = SwingConstants.LEFT,
	val clockwise: Boolean
) : JLabel(text, icon, horizontalAlignment) {

    companion object {
		const val UI_CLASS_ID = "VerticalLabelUI"
    }

	private fun setUI(ui: VerticalLabelUI) {
		super.setUI(ui)
	}

	override fun getUI(): VerticalLabelUI = ui as VerticalLabelUI

	override fun getUIClassID(): String = UI_CLASS_ID

	override fun updateUI() {
		if (UIManager.get(uiClassID) != null) {
			setUI(UIManager.getUI(this) as VerticalLabelUI)
		} else {
			throw IllegalStateException("Could not set VerticalLabelUI")
		}
	}
}

class VerticalLabelUI : BasicLabelUI() {

    companion object {
        private val paintIconRect = Rectangle()
        private val paintTextRect = Rectangle()
        private val paintViewRect = Rectangle()
        private val paintViewInsets = Insets(0, 0, 0, 0)

	    @Suppress("unused") // Reflection
	    @JvmStatic
	    fun createUI(@Suppress("UNUSED_PARAMETER") c: JComponent): VerticalLabelUI {
		    return VerticalLabelUI()
	    }
    }

    override fun getPreferredSize(c: JComponent?): Dimension =
		rotateSize(super.getPreferredSize(c))

	private fun rotateSize(dim: Dimension): Dimension =
		Dimension(dim.height, dim.width)

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

        val clippedText = layoutCL(label, fm, text, icon, paintViewRect, paintIconRect, paintTextRect)

        val g2 = g as Graphics2D
        val oldTransform = g2.transform
        if ((c as VerticalLabel).clockwise) {
            g2.rotate(PI / 2)
            g2.translate(-0, -c.getWidth())
        } else {
            g2.rotate(-PI / 2)
            g2.translate(-c.getHeight(), 0)
        }

        icon?.paintIcon(c, g, paintIconRect.x, paintIconRect.y + 3)

        if (text != null) {
            val textX = paintTextRect.x
            val textY = paintTextRect.y + fm.ascent + 3

            if (label.isEnabled) {
                paintEnabledText(label, g, clippedText, textX, textY)
            } else {
                paintDisabledText(label, g, clippedText, textX, textY)
            }
        }

        g2.transform = oldTransform
    }
}