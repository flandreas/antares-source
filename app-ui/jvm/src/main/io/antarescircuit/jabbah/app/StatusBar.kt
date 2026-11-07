package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.base.Status
import io.antarescircuit.jabbah.base.StatusEvent
import io.antarescircuit.jabbah.base.StatusType
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import java.awt.Dimension
import javax.swing.*

class StatusBar(
	private val eventBus: EventBus = BaseModule.eventBus
) : JPanel() {

	private val largeLabel = JLabel(" ")
	private val toolLabel = JLabel(" ", null, JLabel.TRAILING)
	private val smallLabel = JLabel(" ", null, JLabel.TRAILING)
	private val statusHandler: EventHandler<StatusEvent> = { handle(it) }

	init {
		eventBus.register(StatusEvent::class, statusHandler)

		buildUI()

		smallLabel.preferredSize = Dimension(200, smallLabel.preferredSize.height)

		SwingUtilities.invokeLater {
			largeLabel.text = Status[StatusType.Large]
			smallLabel.text = Status[StatusType.Small]
		}
	}

	fun dispose() {
		eventBus.unregister(statusHandler)
	}

	private fun buildUI() {
		border = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"), 1),
			BorderFactory.createEmptyBorder(2, 5, 2, 5)
		)
		toolLabel.border = BorderFactory.createEmptyBorder(0, 20, 0, 20)

		layout = BoxLayout(this, BoxLayout.LINE_AXIS)
		add(largeLabel)
		add(toolLabel)
		add(Box.createHorizontalGlue())
		add(smallLabel)
	}

	private fun handle(event: StatusEvent) {
		when(event.type) {
			StatusType.Large -> largeLabel.text = event.status
			StatusType.Small -> smallLabel.text = event.status
			StatusType.Tool -> toolLabel.text = event.status
		}
	}
}