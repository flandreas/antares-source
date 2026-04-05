package io.antarescircuit.antares.view

import io.antarescircuit.antares.view.gate.GateMnemonicsEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.graph.ui.GraphNavigationViewController
import io.antarescircuit.jabbah.graph.ui.GraphNavigationViewControllerExtension
import io.antarescircuit.jabbah.graph.view.CurrentGraphAnimationTypeEvent

/**
 * Initiates repainting the [DrawingView] upon certain Antares specific events.
 */
class AntaresGraphNavigationViewControllerExtension(
	private val controller: GraphNavigationViewController,
	private val eventBus: EventBus = BaseModule.eventBus
) : GraphNavigationViewControllerExtension {

	private val currentGraphAnimationTypeHandler: EventHandler<CurrentGraphAnimationTypeEvent> = { controller.drawingView.repaint() }

	private val gateMnemonicHandler: EventHandler<GateMnemonicsEvent> = { controller.drawingView.repaint() }

	init {
		eventBus.register(CurrentGraphAnimationTypeEvent::class, currentGraphAnimationTypeHandler)
		eventBus.register(GateMnemonicsEvent::class, gateMnemonicHandler)
	}

	override fun dispose(controller: GraphNavigationViewController) {
		eventBus.unregister(currentGraphAnimationTypeHandler)
		eventBus.unregister(gateMnemonicHandler)
	}
}