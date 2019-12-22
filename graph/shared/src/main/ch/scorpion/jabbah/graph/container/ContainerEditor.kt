package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.draw.ZoomStrategy
import ch.scorpion.jabbah.draw.ZoomStrategyType
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.editor.EditorImpl
import ch.scorpion.jabbah.graph.model.GraphPortNameChanged
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.vertice.DeepVerticeLink
import ch.scorpion.jabbah.graph.view.ControlViewSourceEvent
import ch.scorpion.jabbah.graph.view.editor.GraphPortViewEvent
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * An [Editor] for editing the outside view of a [SubGraphVerticeView] as a [ContainerDrawing].
 */
open class ContainerEditor(
	view: DrawingView<Drawing<Component>>,
	eventBus: EventBus
) : EditorImpl(view) {

	companion object {
		private val LOG by logger(ContainerEditor::class)

		/** The name of the [Float] property in [Properties] defining the default zoom factor in the [ContainerEditor].*/
		const val PROP_DEFAULT_ZOOM_FACTOR = "graph.container.defaultZoomFactor"
	}

	init {
		configureDefaultZoomFactor()
		view.drawing = ContainerDrawing()

		eventBus.register(GraphPortViewEvent::class) {
			if (it.type == GraphPortViewEvent.Type.REMOVE) {
				removePortViewComponent(it.graphPortView.model!!.name!!)
			}
		}

		eventBus.register(GraphPortNameChanged::class) {
			if (StringUtils.isNotEmpty(it.oldName)) {
				val pvc = getContainerDrawing().getPortViewComponent(it.oldName!!)
				if (pvc != null) {
					pvc.portView!!.setPortName(it.newName!!)
				}
			}
		}

		eventBus.register(ControlViewSourceEvent::class) {
			when (it.type) {
				ControlViewSourceEvent.Type.CHANGE -> {
					LOG.debug("ContainerEditor: handling properties of ControlViewSource changed")
					val cvc = getControlViewComponent(it.source.id)
					if (cvc != null && it.source !== cvc.controlView) {
						cvc.controlView?.sourcePropertiesChanged(it.source)
					}
				}
				ControlViewSourceEvent.Type.REMOVE -> {
					getControlViewComponent(it.source.id)?.let { getContainerDrawing().remove(it) }
				}
				ControlViewSourceEvent.Type.ADD -> {
					// nothing to do for ADD
				}
			}
		}

		eventBus.register(PreferencesChangedEvent::class) { configureDefaultZoomFactor() }
	}

	private fun configureDefaultZoomFactor() {
		view.defaultZoomStrategy = ZoomStrategy(ZoomStrategyType.VALUE, BaseModule.properties.getFloat(PROP_DEFAULT_ZOOM_FACTOR).toDouble())
	}

	protected fun getContainerDrawing(): ContainerDrawing {
		return drawing as ContainerDrawing
	}

	private fun getControlViewComponent(id: Int): ControlViewComponent? {
		return getContainerDrawing().getControlViewComponent(DeepVerticeLink(id))
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