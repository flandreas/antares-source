package ch.scorpion.jabbah.graph.view.usecase

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.help.HelpId
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.dsl.UsecaseActionExternalFunctions
import ch.scorpion.jabbah.graph.view.Usecase

/**
 * Records certain [MouseEvent] and [KeyEvent] from the user during [Usecase] recording.
 */
class UsecaseRecorder(
	private val applicationModeHolder: ApplicationModeHolder,
	private val statementCollector: (String) -> Unit,
	private val signalHandler: SignalHandler,
	private val viewManager: ContentViewManager = DrawViewModule.viewManager
) {

	companion object {

		val HELP_ID = HelpId("usecaseRecording")

		/** The name of the [Int] value (in ms) in [Properties] for the default delay between 'press' and 'release' events. */
		const val PROP_DEF_DELAY_MS = "graph.usecase.record.defaultDelay"

		/** The name of the [Int] value (in ms) in [Properties] for the default time between clicks.*/
		const val PROP_DEF_TIME_BETWEEN_CLICKS_MS = "graph.usecase.record.defaultTimeBetweenClicks"

		private val LOG by logger(UsecaseRecorder::class)
	}

	private val view: View<*>? get() = viewManager.activeView?.view

	private val mouseObserver = MouseObserver()

	private val keyObserver = KeyObserver()

	private var realtime: Boolean = false

	/** The time (in ns) recorded between 'pressed' and 'released' events. Used independent of [realtime].*/
	private var releaseDelay: Int = 0

	/** The time (in ns) recorded between two actions. Only used if [realtime] is `false`. */
	private var clickDistance: Int = 0

	/** The time (in ns) for which actions are recorded to be executed later. Only used if [realtime] is `false`. */
	private var time: Long = 0

	private var isFirstEvent = true

	var isRecording: Boolean = false
		private set


	fun start(realtime: Boolean, releaseDelay: Int, clickDistance: Int) {
		this.realtime = realtime
		this.releaseDelay = releaseDelay
		this.clickDistance = clickDistance

		LOG.userTrail("Start recording usecase")
		view?.addMouseListener(mouseObserver)
		view?.addKeyListener(keyObserver)
		isRecording = true
		isFirstEvent = true
		applicationModeHolder.setMode(ApplicationMode.EXECUTE)
	}

	fun stop() {
		LOG.userTrail("Stop recording usecase")
		view?.removeMouseListener(mouseObserver)
		view?.removeKeyListener(keyObserver)
		isRecording = false
		applicationModeHolder.setMode(ApplicationMode.EDIT)
	}

	private fun collect(statement: String) {
		statementCollector(statement)
		time += clickDistance
	}

	private fun checkFirstEvent() {
		if (isFirstEvent) {
			time = signalHandler.executionTime + clickDistance
			isFirstEvent = false
		}
	}

	private val executionTime: Long get() = if (realtime) {
		signalHandler.executionTime
	} else {
		time
	}

	private inner class MouseObserver : MouseAdapter() {
		override fun mousePressed(e: MouseEvent) {
			if (e.button == Button.BUTTON1) {
				view?.viewToModel(e.location)?.let { loc ->
					LOG.debug("Record leftMousePressed at ${loc.x}/${loc.y}")
					checkFirstEvent()
					collect(UsecaseActionExternalFunctions.clickMouseStatement(executionTime, loc.x.toInt(), loc.y.toInt(), releaseDelay))
				}
			}
		}
	}

	private inner class KeyObserver : KeyAdapter() {
		override fun keyPressed(e: KeyEvent) {
			LOG.debug("Record keyPressed ${e.key}")
			checkFirstEvent()
			collect(UsecaseActionExternalFunctions.pressKeyStatement(executionTime, e.key, releaseDelay))
		}
	}
}