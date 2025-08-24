package ch.scorpion.jabbah.app.dump

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.SystemMalfunctionEvent
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import java.awt.Frame
import java.time.Duration
import java.time.LocalDateTime

/** Handles [SystemMalfunctionEvent]s posted on the system [EventBus] by any code. */
object SystemMalfunctionHandler : AbstractDumpCreator() {

	private val LOG by logger(SystemMalfunctionHandler::class)

	/** The [Duration] since the last dump upload in which new malfunctions are ignored (without restart). */
	private val MIN_DURATION = Duration.ofHours(12)

	private var currentEvent: SystemMalfunctionEvent? = null

	/** The time when the last dump upload occurred. Used to avoid uploading the same dump multiple times. */
	private var lastDateTime: LocalDateTime? = null

	override fun initialize(application: DesktopApplication) {
		super.initialize(application)
		BaseModule.eventBus.register(SystemMalfunctionEvent::class) { receive(it) }
	}

	/** Make sure that only one [SystemMalfunctionEvent] per AWT event is handled.*/
	private fun receive(event: SystemMalfunctionEvent) {
		if (currentEvent == null) {
			currentEvent = event
			System.invokeLater {
				handle()
			}
		}
	}

	private fun handle() {
		LOG.userTrail("Handling system malfunction: ${currentEvent!!.description}")

		if (lastDateTime == null || Duration.between(lastDateTime, LocalDateTime.now()) >= MIN_DURATION) {
			uploadErrorDump(currentEvent!!.description, includeWorkspace = false)
		}

		SystemMalfunctionPanel.showAsDialog(application, currentEvent!!, Frame.getFrames()[0])
		currentEvent = null
	}
}