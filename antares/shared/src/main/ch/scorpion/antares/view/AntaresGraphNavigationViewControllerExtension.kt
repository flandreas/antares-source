package ch.scorpion.antares.view

import ch.scorpion.antares.view.gate.GateMnemonicsEvent
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyleChangedEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.ui.GraphNavigationViewController
import ch.scorpion.jabbah.graph.ui.GraphNavigationViewControllerExtension
import ch.scorpion.jabbah.graph.view.CurrentGraphAnimationTypeEvent

/**
 * Initiates repainting the [DrawingView] upon certain Antares specific events.
 */
class AntaresGraphNavigationViewControllerExtension(
	private val controller: GraphNavigationViewController,
	private val eventBus: EventBus = BaseModule.eventBus
) : GraphNavigationViewControllerExtension {

	private val currentGraphAnimationTypeHandler: EventHandler<CurrentGraphAnimationTypeEvent> = { controller.drawingView.repaint() }

	private val currentSymbolStyleHandler: EventHandler<CurrentSymbolStyleChangedEvent> = { controller.drawingView.repaint() }

	private val gateMnemonicHandler: EventHandler<GateMnemonicsEvent> = { controller.drawingView.repaint() }

	init {
		eventBus.register(CurrentGraphAnimationTypeEvent::class, currentGraphAnimationTypeHandler)
		eventBus.register(CurrentSymbolStyleChangedEvent::class, currentSymbolStyleHandler)
		eventBus.register(GateMnemonicsEvent::class, gateMnemonicHandler)
	}

	override fun dispose(controller: GraphNavigationViewController) {
		eventBus.unregister(currentGraphAnimationTypeHandler)
		eventBus.unregister(currentSymbolStyleHandler)
		eventBus.unregister(gateMnemonicHandler)
	}
}