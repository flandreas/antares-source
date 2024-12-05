package ch.scorpion.antares.view

import ch.scorpion.antares.model.addressable.MemoryLibraryItem
import ch.scorpion.antares.model.expression.BooleanExpressionLibraryItem
import ch.scorpion.antares.model.truthtable.TruthTableLibraryItem
import ch.scorpion.antares.view.addressable.OpenMemoryContentsRequest
import ch.scorpion.antares.view.app.AntaresGraphViewService
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.graph.ui.GraphFrame
import ch.scorpion.jabbah.graph.ui.GraphFrameController
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

interface AntaresFrame : GraphFrame {

	fun createMemoryContentsDesktopViewItem(request: OpenMemoryContentsRequest, contextColor: CompositeColor): GraphDesktopViewItem

	fun createTruthTableDesktopViewItem(item: TruthTableLibraryItem): GraphDesktopViewItem

	fun createBooleanExpressionDesktopViewItem(item: BooleanExpressionLibraryItem): GraphDesktopViewItem

	fun createMemoryStorableGraphDesktopViewItem(item: MemoryLibraryItem): GraphDesktopViewItem

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
	companion object {
		private val LOG by logger(AntaresFrameController::class)
	}

	private val openMemoryContentsRequestHandler: EventHandler<OpenMemoryContentsRequest> = { handle(it) }
	private val defaultLightColorHandler: EventHandler<DefaultLightColorEvent> = { handle(it) }
	private val applicationDataHandler: EventHandler<ApplicationDataEvent> = { handle(it) }

	init {
		eventBus.register(ApplicationDataEvent::class, applicationDataHandler)
		eventBus.register(OpenMemoryContentsRequest::class, openMemoryContentsRequestHandler)
		eventBus.register(DefaultLightColorEvent::class, defaultLightColorHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(applicationDataHandler)
		eventBus.unregister(openMemoryContentsRequestHandler)
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

	private fun handle(event: ApplicationDataEvent) {
		when (event.newData?.content) {
			is TruthTableLibraryItem -> handleTruthTableLibraryItem(event.newData!!.content as TruthTableLibraryItem)
			is BooleanExpressionLibraryItem -> handleBooleanExpressionLibraryItem(event.newData!!.content as BooleanExpressionLibraryItem)
			is MemoryLibraryItem -> handleMemoryLibraryItem(event.newData!!.content as MemoryLibraryItem)
		}
	}

	private fun handleTruthTableLibraryItem(newItem: TruthTableLibraryItem) {
		with(graphPanelViewController.desktopController.view) {
			// Avoid creation of new view in "Close with 'Want to save changes?' = Yes" scenario
			if (mainDesktopViewItem == null || !mainDesktopViewItem!!.displays(newItem)) {
				LOG.debug("Create new TruthTableDesktopViewItem")
				graphPanelViewController.desktopController.show(view.createTruthTableDesktopViewItem(newItem))
			}
		}
	}

	private fun handleBooleanExpressionLibraryItem(newItem: BooleanExpressionLibraryItem) {
		with(graphPanelViewController.desktopController.view) {
			// Avoid creation of new view in "Close with 'Want to save changes?' = Yes" scenario
			if (mainDesktopViewItem == null || !mainDesktopViewItem!!.displays(newItem)) {
				LOG.debug("Create new BooleanExpressionDesktopViewItem")
				graphPanelViewController.desktopController.show(view.createBooleanExpressionDesktopViewItem(newItem))
			}
		}
	}

	private fun handleMemoryLibraryItem(newItem: MemoryLibraryItem) {
		with(graphPanelViewController.desktopController.view) {
			// Avoid creation of new view in "Close with 'Want to save changes?' = Yes" scenario
			if (mainDesktopViewItem == null || !mainDesktopViewItem!!.displays(newItem)) {
				LOG.debug("Create new MemoryStorableDesktopViewItem")
				graphPanelViewController.desktopController.show(view.createMemoryStorableGraphDesktopViewItem(newItem))
			}
		}
	}

	private fun handle(event: DefaultLightColorEvent) {
		if (event.graphView.defaultLightColor != null && view.shouldReplaceLightColor()) {
			(GraphViewModule.graphViewAppService as AntaresGraphViewService).replaceLightColor(event.graphView)
		}
	}
}