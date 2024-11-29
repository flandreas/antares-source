package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import java.awt.Color
import java.awt.Dimension
import javax.swing.*
import javax.swing.border.Border
import kotlin.math.max

abstract class AbstractGraphDesktopViewItemSwing : JPanel(), GraphDesktopViewItem {

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

class GraphDesktopItemHeaderPanelSwing(
	private val graphDesktopViewItem: GraphDesktopViewItem,
	private val content: JComponent,
	private val eventBus: EventBus = BaseModule.eventBus,
	allowClose: Boolean = true
) : JPanel() {

	companion object {
		const val PREF_HEIGHT = 27
		const val LEFT_INSET = 10

		val headerBackgroundColor: Color get() = UiUtil.getBackgroundDivertColor(UIManager.getColor("Panel.background"))
	}

	init {
		layout = BoxLayout(this, BoxLayout.LINE_AXIS)
		add(Box.createHorizontalStrut(LEFT_INSET))

		add(content)
		background = headerBackgroundColor

		if (allowClose) {
			add(Box.createHorizontalGlue())
			add(UiUtil.createToolBarButton(CloseAction()))
		}
	}

	override fun getPreferredSize(): Dimension =
		Dimension(super.getPreferredSize().width, max(PREF_HEIGHT, content.preferredSize.height))

	private inner class CloseAction : AbstractAction("base.action.close") {

		init {
			imagePath = "/img/close-16.png"
		}

		override fun execute(event: ActionEvent) {
			eventBus.post(graphDesktopViewItem.createCloseRequest())
		}
	}
}