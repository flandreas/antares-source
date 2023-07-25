package ch.scorpion.jabbah.draw.richtext

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.richtext.RichText
import ch.scorpion.jabbah.draw.drawable.RichTextDrawable
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.tree.DefaultTreeCellRenderer
import kotlin.math.max

/**
 * A [JLabel] implementation that can draw its text as rich text using
 * [RichTextDrawable]. If [richText] is `null`, it is falling back to normal
 * [paintComponents] logic.
 *
 * THis special implementation is necessary because Swing's 3.2 HTML support
 * is not capable of rendering the [RichText] features, e.g. custom CSS style
 * "text-decoration:overline" seems not to be supported, and <span> does not
 * support "style=border-top".
 */
open class RichTextLabel : DefaultTreeCellRenderer() {

	companion object {
		private const val HORIZONTAL_INSET = 3
		private const val VERTICAL_INSET = 0
		private const val DIST = 5
	}

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

	override fun getPreferredSize(): Dimension = if (richText != null) {
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
		var x = HORIZONTAL_INSET

		if (icon != null) {
			icon.paintIcon(this, g, x,height / 2 - icon.iconHeight / 2)
			x += icon.iconWidth + DIST
		}

		if (richText != null) {
			g.color = foreground

			richText!!.location = Point2D(x, height / 2 - richText!!.heightInt / 2)

			richText!!.draw(Graphics2DJvm(g as Graphics2D))
		}
	}

	private fun calculatePreferredSize(): Dimension {
		var w = 0
		var h = 0

		if (icon != null) {
			w  += icon.iconWidth + HORIZONTAL_INSET + DIST
			h  += icon.iconHeight + 2 * VERTICAL_INSET
		} else {
			w += HORIZONTAL_INSET
		}

		if (richText != null) {
			w += richText!!.widthInt + HORIZONTAL_INSET
			h = max(h, richText!!.heightInt + 2 * VERTICAL_INSET)
		}

		return Dimension(w, h)
	}
}