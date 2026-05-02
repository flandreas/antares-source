package io.antarescircuit.jabbah.graph.container

import io.antarescircuit.jabbah.base.PreferencesChangedEvent
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.event.VetoException
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.ZoomStrategy
import io.antarescircuit.jabbah.draw.ZoomStrategyType
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.app.DeleteQuestion
import io.antarescircuit.jabbah.edit.editor.EditorImpl
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.vertice.DeepVerticeLink
import io.antarescircuit.jabbah.graph.ui.GraphFrameController
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import io.antarescircuit.jabbah.graph.view.ControlViewSourceEvent
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.editor.GraphPortViewEvent
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * An [Editor] for editing the outside view of a [SubGraphVerticeView] as a [ContainerDrawing].
 *
 * @property view the [DrawingView] containing the [ContainerDrawing]
 * @property mainDrawingView the [DrawingView] containing the main [Drawing] whose symbol is edited by this [ContainerEditor].
 * Used to check whether received update events are relevant for this [ContainerEditor].
 */
open class ContainerEditor(
	view: DrawingView<Component, Drawing<Component>>,
	protected val mainDrawingView: DrawingView<GraphElementView<*>, GraphView>,
	protected val eventBus: EventBus = BaseModule.eventBus
) : EditorImpl(view, name = GraphFrameController.CONTAINER_EDITOR_NAME) {

	companion object {
		private val LOG by logger(ContainerEditor::class)

		/** The name of the [Float] property in [Properties] defining the default zoom factor in the [ContainerEditor].*/
		const val PROP_DEFAULT_ZOOM_FACTOR = "graph.container.defaultZoomFactor"
	}

	var preventDeletingPortViewComponents = false

	private val graphPortViewHandler: EventHandler<GraphPortViewEvent> = {
		if (it.type == GraphPortViewEvent.Type.REMOVE) {
			removePortViewComponent(it.graphPortView.model.name!!)
		}
	}

	private val controlViewSourceHandler: EventHandler<ControlViewSourceEvent> = {
		when (it.type) {
			ControlViewSourceEvent.Type.CHANGE -> {
				if (mainDrawingView.drawing.contains(it.source)) {
					val cvc = getControlViewComponent(it.source)
					if (cvc != null && it.source !== cvc.controlView) {
						LOG.trace("ContainerEditor: handling properties of ControlViewSource changed")
						cvc.controlView.sourcePropertiesChanged(it.source)
					}
				}
			}
			ControlViewSourceEvent.Type.REMOVE -> {
				getControlViewComponent(it.source)?.let { c -> getContainerDrawing().remove(c) }
			}
			ControlViewSourceEvent.Type.ADD -> {
				// nothing to do for ADD
			}
		}
	}

	private val deleteQuestionHandler: EventHandler<DeleteQuestion> = { question ->
		if (preventDeletingPortViewComponents) {
			question.components.firstOrNull { c -> c is PortViewComponent }?.let { source ->
				throw VetoException("", source)
			}
		}
	}

	private val preferencesChangedHandler: EventHandler<PreferencesChangedEvent> = { configureDefaultZoomFactor() }

	init {
		configureDefaultZoomFactor()

		eventBus.register(GraphPortViewEvent::class, graphPortViewHandler)
		eventBus.register(ControlViewSourceEvent::class, controlViewSourceHandler)
		eventBus.register(PreferencesChangedEvent::class, preferencesChangedHandler)
		eventBus.register(DeleteQuestion::class, deleteQuestionHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(graphPortViewHandler)
		eventBus.unregister(controlViewSourceHandler)
		eventBus.unregister(preferencesChangedHandler)
		eventBus.unregister(deleteQuestionHandler)
	}

	private fun configureDefaultZoomFactor() {
		view.defaultZoomStrategy = ZoomStrategy(ZoomStrategyType.VALUE, BaseModule.properties.getFloat(PROP_DEFAULT_ZOOM_FACTOR).toDouble())
	}

	protected fun getContainerDrawing(): ContainerDrawing {
		return drawing as ContainerDrawing
	}

	private fun getControlViewComponent(controlViewSource: ControlViewSource<*>): ControlViewComponent? =
		getContainerDrawing().getControlViewComponent(DeepVerticeLink(controlViewSource.model.id))

	/** Removes the [PortViewComponent] for the [Port] with the specified name from the [ContainerDrawing].*/
	private fun removePortViewComponent(name: String) {
		for (c in view.drawing.frontToBackIterator()) {
			if (c is PortViewComponent && c.port.name == name) {
				view.drawing.remove(c)
				view.drawing.validate()
				return
			}
		}
	}
}