package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.Status
import ch.scorpion.jabbah.base.StatusEvent
import ch.scorpion.jabbah.base.StatusType
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities

class StatusBar(
	eventBus: EventBus = BaseModule.eventBus
) : JPanel() {

	private val largeLabel = JLabel(" ")
	private val smallLabel = JLabel(" ")

	init {
		eventBus.register(StatusEvent::class) { handle(it) }

		buildUI()

		smallLabel.preferredSize = Dimension(100, smallLabel.preferredSize.height)

		SwingUtilities.invokeLater {
			largeLabel.text = Status.get(StatusType.Large)
			smallLabel.text = Status.get(StatusType.Small)
		}
	}

	private fun buildUI() {
		layout = BorderLayout()
		border = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
			BorderFactory.createEmptyBorder(2, 5, 2, 0)
		)

		largeLabel.foreground = Color.DARK_GRAY
		smallLabel.foreground = Color.DARK_GRAY

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