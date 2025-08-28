package ch.scorpion.jabbah.draw.richtext

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.drawable.RichTextDrawable
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.Icon
import javax.swing.table.DefaultTableCellRenderer
import kotlin.math.max

/**
 * TODO: Copy/paste from [RichTextLabel].
 * In addition to [RichTextLabel], also interprets horizontal alignment.
 */
open class RichTextTableCellRenderer(
	private val setPreferredSize: Boolean = true
) : DefaultTableCellRenderer() {

	private var _preferredSize = calculatePreferredSize()

	var richText: RichTextDrawable? = null
		set(value) {
			field = value
			_preferredSize = calculatePreferredSize()
		}

	init {
		border = null
	}

	override fun setIcon(icon: Icon?) {
		super.setIcon(icon)
		_preferredSize = calculatePreferredSize()
	}

	override fun getPreferredSize(): Dimension =
		if (setPreferredSize && richText != null) {
			_preferredSize
		} else {
			super.getPreferredSize()
		}

	override fun paintComponent(g: Graphics) {
		if (richText != null) {
			paintCustom(g)
		} else {
			super.paintComponent(g)
		}
	}

	private fun paintCustom(g: Graphics) {
		// Draw selection background if needed
		text = ""
		super.paintComponent(g)

		var x = when (horizontalAlignment) {
			CENTER -> insets.left + (width - _preferredSize.width) / 2
			RIGHT -> width - _preferredSize.width - insets.right
			else -> insets.left
		}

		if (icon != null) {
			icon.paintIcon(this, g, x,height / 2 - icon.iconHeight / 2)
			x += icon.iconWidth + iconTextGap
		}

		if (richText != null) {
			g.color = foreground

			richText!!.location = Point2D(x, height / 2 - richText!!.heightInt / 2)

			richText!!.draw(Graphics2DJvm(g as Graphics2D))
		}
	}

	private fun calculatePreferredSize(): Dimension {
		var w = insets.left + insets.right
		var h = insets.top + insets.bottom

		if (icon != null) {
			w  += icon.iconWidth + iconTextGap
			h  += icon.iconHeight
		}

		if (richText != null) {
			w += richText!!.widthInt
			h = max(h, richText!!.heightInt + insets.top + insets.bottom)
		}

		return Dimension(w, h)
	}
}