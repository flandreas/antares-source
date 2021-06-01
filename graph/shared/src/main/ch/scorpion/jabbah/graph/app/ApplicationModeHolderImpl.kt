package ch.scorpion.jabbah.graph.app

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.view.GraphView

/** An aggregatable implementation of [ApplicationModeHolder].*/
class ApplicationModeHolderImpl(
	val editor: Editor,
	private val viewManager: ViewManager = DrawViewModule.viewManager,
	private val scheduler: Scheduler = ExecutionModule.scheduler,
	private val eventBus: EventBus = BaseModule.eventBus
) : ApplicationModeHolder {

	companion object {
		private val LOG by logger(ApplicationModeHolderImpl::class)
	}

	private val rootGraphView: GraphView get() = editor.drawing as GraphView

	private val editorViewListener = EditorViewListener()

	init {
		editor.view.addPropertyChangeListener(editorViewListener)
	}

	override fun dispose() {
		editor.view.removePropertyChangeListener(editorViewListener)
	}

	override var currentMode: ApplicationMode  = ApplicationMode.EDIT
		private set

	override fun setMode(mode: ApplicationMode, after: () -> Unit) {
		setMode(mode, init = false, after)
	}

	override fun updateEditorEditability() {
		val editable =
			viewManager.activeView === editor.view
				&& !scheduler.isActive

		LOG.trace("Setting editor active=$editable")
		editor.active = editable
	}

	private fun setMode(mode: ApplicationMode, init: Boolean, after: () -> Unit = {}) {
		if (mode == currentMode) {
			return
		}
		LOG.trace("Entering mode $mode")
		when (mode) {
			ApplicationMode.EDIT -> enterEditMode(init)
			ApplicationMode.EXECUTE, ApplicationMode.EXEC_USECASE -> enterExecMode(mode, after)
		}
	}

	private fun enterEditMode(init: Boolean) {
		LOG.debug("Enter edit mode mode")

		currentMode = ApplicationMode.EDIT
		if (!init) {
			scheduler.isActive = false
		}
		updateEditorEditability()
		eventBus.post(ApplicationModeEvent(currentMode))
		Status.set(StatusType.Large, Translations.getString("graph.status.edit"))
	}

	private fun enterExecMode(mode: ApplicationMode, after: () -> Unit) {
		LOG.debug("Enter execution mode (deep = ${scheduler.isDeepExecution})")

		eventBus.post(ApplicationModeBeginEvent(mode))

		if (rootGraphView.checkDesign()) {
			currentMode = mode
			System.invokeLater {
				scheduler.isActive = true
				updateEditorEditability()
				eventBus.post(ApplicationModeEvent(currentMode))
				Status.set(StatusType.Large, Translations.getString("graph.status.execute"))
				after.invoke()
			}
		} else {
			eventBus.post(ComponentMessage(type = ComponentMessageType.Error, source = null, messageKey = "graph.designError.msg"))
			LOG.debug("execution not started due to design errors")
		}
	}

	private inner class EditorViewListener : PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			if (e.name == DrawingView.PROP_EDITABLE) {
				updateEditorEditability()
			}
		}
	}
}