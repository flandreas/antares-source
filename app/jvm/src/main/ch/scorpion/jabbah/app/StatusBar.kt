package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.Status
import ch.scorpion.jabbah.base.StatusEvent
import ch.scorpion.jabbah.base.StatusType
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class StatusBar(
	private val eventBus: EventBus = BaseModule.eventBus
) : JPanel() {

	private val largeLabel = JLabel(" ")
	private val smallLabel = JLabel(" ", null, JLabel.TRAILING)
	private val statusHandler: EventHandler<StatusEvent> = { handle(it) }

	init {
		eventBus.register(StatusEvent::class, statusHandler)

		buildUI()

		smallLabel.preferredSize = Dimension(200, smallLabel.preferredSize.height)

		SwingUtilities.invokeLater {
			largeLabel.text = Status.get(StatusType.Large)
			smallLabel.text = Status.get(StatusType.Small)
		}
	}

	fun dispose() {
		eventBus.unregister(statusHandler)
	}

	private fun buildUI() {
		layout = BorderLayout()
		border = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"), 1),
			BorderFactory.createEmptyBorder(2, 5, 2, 5)
		)

		add(largeLabel, BorderLayout.CENTER)
		add(smallLabel, BorderLayout.EAST)
	}

	private fun handle(event: StatusEvent) {
		when(event.type) {
			StatusType.Large -> largeLabel.text = event.status
			StatusType.Small -> smallLabel.text = event.status
		}
	}
}