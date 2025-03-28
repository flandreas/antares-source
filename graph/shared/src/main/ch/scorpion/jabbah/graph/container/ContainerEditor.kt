package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.ZoomStrategy
import ch.scorpion.jabbah.draw.ZoomStrategyType
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.editor.EditorImpl
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.vertice.DeepVerticeLink
import ch.scorpion.jabbah.graph.view.ControlViewSourceEvent
import ch.scorpion.jabbah.graph.view.editor.GraphPortViewEvent
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * An [Editor] for editing the outside view of a [SubGraphVerticeView] as a [ContainerDrawing].
 *
 * @property view the [DrawingView] containing the [ContainerDrawing]
 * @property mainDrawingView the [DrawingView] containing the main [Drawing] whose symbol is edited by this [ContainerEditor].
 * Used to check whether received update events are relevant for this [ContainerEditor].
 */
open class ContainerEditor(
	view: DrawingView<Drawing<Component>>,
	protected val mainDrawingView: DrawingView<Drawing<Component>>,
	protected val eventBus: EventBus = BaseModule.eventBus
) : EditorImpl(view, name = "containerEditor") {

	companion object {
		private val LOG by logger(ContainerEditor::class)

		/** The name of the [Float] property in [Properties] defining the default zoom factor in the [ContainerEditor].*/
		const val PROP_DEFAULT_ZOOM_FACTOR = "graph.container.defaultZoomFactor"
	}

	private val graphPortViewHandler: EventHandler<GraphPortViewEvent> = {
		if (it.type == GraphPortViewEvent.Type.REMOVE) {
			removePortViewComponent(it.graphPortView.model.name!!)
		}
	}

	private val controlViewSourceHandler: EventHandler<ControlViewSourceEvent> = {
		when (it.type) {
			ControlViewSourceEvent.Type.CHANGE -> {
				val cvc = getControlViewComponent(it.source.model.id)
				if (cvc != null && it.source !== cvc.controlView) {
					LOG.trace("ContainerEditor: handling properties of ControlViewSource changed")
					cvc.controlView.sourcePropertiesChanged(it.source)
				}
			}
			ControlViewSourceEvent.Type.REMOVE -> {
				getControlViewComponent(it.source.model.id)?.let { c -> getContainerDrawing().remove(c) }
			}
			ControlViewSourceEvent.Type.ADD -> {
				// nothing to do for ADD
			}
		}
	}

	private val preferencesChangedHandler: EventHandler<PreferencesChangedEvent> = { configureDefaultZoomFactor() }

	init {
		configureDefaultZoomFactor()

		eventBus.register(GraphPortViewEvent::class, graphPortViewHandler)
		eventBus.register(ControlViewSourceEvent::class, controlViewSourceHandler)
		eventBus.register(PreferencesChangedEvent::class, preferencesChangedHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(graphPortViewHandler)
		eventBus.unregister(controlViewSourceHandler)
		eventBus.unregister(preferencesChangedHandler)
	}

	private fun configureDefaultZoomFactor() {
		view.defaultZoomStrategy = ZoomStrategy(ZoomStrategyType.VALUE, BaseModule.properties.getFloat(PROP_DEFAULT_ZOOM_FACTOR).toDouble())
	}

	protected fun getContainerDrawing(): ContainerDrawing {
		return drawing as ContainerDrawing
	}

	private fun getControlViewComponent(verticeId: Int): ControlViewComponent? {
		return getContainerDrawing().getControlViewComponent(DeepVerticeLink(verticeId))
	}

	/** Removes the [PortViewComponent] for the [Port] with the specified name from the [ContainerDrawing].*/
	private fun removePortViewComponent(name: String) {
		for (c in view.drawing.frontToBackIterator()) {
			if (c is PortViewComponent<*> && c.port.name == name) {
				view.drawing.remove(c)
				view.drawing.validate()
				return
			}
		}
	}
}