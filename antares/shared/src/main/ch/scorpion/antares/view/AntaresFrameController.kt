package ch.scorpion.antares.view

import ch.scorpion.antares.model.expression.ShowBooleanExpressionItemRequest
import ch.scorpion.antares.model.truthtable.ShowTruthTableItemRequest
import ch.scorpion.antares.view.addressable.OpenMemoryContentsRequest
import ch.scorpion.antares.view.app.AntaresGraphViewService
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.graph.ui.GraphFrame
import ch.scorpion.jabbah.graph.ui.GraphFrameController
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

interface AntaresFrame : GraphFrame {

	fun createMemoryContentsDesktopViewItem(request: OpenMemoryContentsRequest, contextColor: CompositeColor): GraphDesktopViewItem

	fun createTruthTableDesktopViewItem(request: ShowTruthTableItemRequest): GraphDesktopViewItem

	fun createBooleanExpressionDesktopViewItem(request: ShowBooleanExpressionItemRequest): GraphDesktopViewItem

	fun showMemoryContents(request: OpenMemoryContentsRequest)

	fun shouldReplaceLightColor(): Boolean
}

class AntaresFrameController(
	appDataViewController: ApplicationDataViewController,
	private val eventBus: EventBus = BaseModule.eventBus,
) : GraphFrameController<AntaresFrame>(
	appDataViewController,
	eventBus
) {

	private val openMemoryContentsRequestHandler: EventHandler<OpenMemoryContentsRequest> = { handle(it) }
	private val openTruthTableRequestHandler: EventHandler<ShowTruthTableItemRequest> = { handle(it) }
	private val openBooleanExpressionRequestHandler: EventHandler<ShowBooleanExpressionItemRequest> = { handle(it) }
	private val defaultLightColorHandler: EventHandler<DefaultLightColorEvent> = { handle(it) }

	init {
		eventBus.register(OpenMemoryContentsRequest::class, openMemoryContentsRequestHandler)
		eventBus.register(ShowTruthTableItemRequest::class, openTruthTableRequestHandler)
		eventBus.register(ShowBooleanExpressionItemRequest::class, openBooleanExpressionRequestHandler)
		eventBus.register(DefaultLightColorEvent::class, defaultLightColorHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(openMemoryContentsRequestHandler)
		eventBus.unregister(openTruthTableRequestHandler)
		eventBus.unregister(defaultLightColorHandler)
	}

	private fun handle(event: OpenMemoryContentsRequest) {
		if (event.newDesktopView) {
			graphPanelViewController.desktopController.openVerticeView(event.verticeView) { color,_ ->
				view.createMemoryContentsDesktopViewItem(event, color)
			}
		} else {
			view.showMemoryContents(event)
		}
	}

	private fun handle(event: ShowTruthTableItemRequest) {
		graphPanelViewController.desktopController.show(view.createTruthTableDesktopViewItem(event))
	}

	private fun handle(event: ShowBooleanExpressionItemRequest) {
		graphPanelViewController.desktopController.show(view.createBooleanExpressionDesktopViewItem(event))
	}

	private fun handle(event: DefaultLightColorEvent) {
		if (event.graphView.defaultLightColor != null && view.shouldReplaceLightColor()) {
			(GraphViewModule.graphViewAppService as AntaresGraphViewService).replaceLightColor(event.graphView)
		}
	}
}