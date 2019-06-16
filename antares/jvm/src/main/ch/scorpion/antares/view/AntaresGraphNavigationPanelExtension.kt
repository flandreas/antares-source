package ch.scorpion.antares.view

import ch.scorpion.antares.view.gate.GateMnemonicsEvent
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyleChangedEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.ui.GraphNavigationPanel
import ch.scorpion.jabbah.graph.ui.GraphNavigationPanelExtension
import ch.scorpion.jabbah.graph.view.CurrentGraphAnimationTypeEvent

class AntaresGraphNavigationPanelExtension(
	private val panel: GraphNavigationPanel,
	private val eventBus: EventBus = BaseModule.eventBus
) : GraphNavigationPanelExtension {

	private val graphAnimator = GraphViewAnimator(panel.drawingView)

	private val currentGraphAnimationTypeHandler: EventHandler<CurrentGraphAnimationTypeEvent> = { panel.drawingView.repaint() }

	private val currentSymbolStyleHandler: EventHandler<CurrentSymbolStyleChangedEvent> = { panel.drawingView.repaint() }

	private val gateMnemonicHandler: EventHandler<GateMnemonicsEvent> = { panel.drawingView.repaint() }

	init {
		eventBus.register(CurrentGraphAnimationTypeEvent::class, currentGraphAnimationTypeHandler)
		eventBus.register(CurrentSymbolStyleChangedEvent::class, currentSymbolStyleHandler)
		eventBus.register(GateMnemonicsEvent::class, gateMnemonicHandler)
	}

	override fun dispose(panel: GraphNavigationPanel) {
		eventBus.unregister(currentGraphAnimationTypeHandler)
		eventBus.unregister(currentSymbolStyleHandler)
		eventBus.unregister(gateMnemonicHandler)
	}
}