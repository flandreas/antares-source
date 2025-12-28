package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.border.Border

abstract class AbstractGraphDesktopViewItemSwing(
	override val reusable: Boolean
) : JPanel(), GraphDesktopViewItem {

	companion object {
		private const val BORDER_THICKNESS = 5
	}

	override var contextColor: CompositeColor? = null
		set(value) {
			if (field == value) {
				return
			}

			when {
				field == null -> addContextColorBorder(value!!.foregroundColor)
				value == null -> removeContextColorBorder()
				else -> addContextColorBorder(value.foregroundColor)
			}

			field = value
			revalidate()
			repaint()
		}

	override val isDetached: Boolean = false

	protected abstract fun addContextColorBorder(color: ch.scorpion.jabbah.draw.graphics.Color)

	protected abstract fun removeContextColorBorder()

	protected fun createContextColorBorder(contextColor: ch.scorpion.jabbah.draw.graphics.Color): Border =
		BorderFactory.createLineBorder(Graphics2DJvm.toAwtColor(contextColor), BORDER_THICKNESS, true)
}