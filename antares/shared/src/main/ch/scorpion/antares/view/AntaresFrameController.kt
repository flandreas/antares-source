package ch.scorpion.antares.view

import ch.scorpion.antares.model.addressable.MemoryLibraryItem
import ch.scorpion.antares.model.addressable.MemorySavable
import ch.scorpion.antares.model.expression.BooleanExpressionLibraryItem
import ch.scorpion.antares.model.expression.BooleanExpressionSavable
import ch.scorpion.antares.model.fsm.FSMLibraryItem
import ch.scorpion.antares.model.fsm.FSMSavable
import ch.scorpion.antares.model.truthtable.TruthTableLibraryItem
import ch.scorpion.antares.model.truthtable.TruthTableSavable
import ch.scorpion.antares.view.addressable.OpenMemoryContentsRequest
import ch.scorpion.antares.view.app.AntaresGraphViewService
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.graph.model.Document
import ch.scorpion.jabbah.graph.model.image.ImageIdentificationSavable
import ch.scorpion.jabbah.graph.model.image.ImageLibraryElement
import ch.scorpion.jabbah.graph.ui.GraphFrame
import ch.scorpion.jabbah.graph.ui.GraphFrameController
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import ch.scorpion.jabbah.graph.ui.documentation.OpenDocumentationRequest
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

interface AntaresFrame : GraphFrame {

	fun createMemoryContentsDesktopViewItem(request: OpenMemoryContentsRequest, contextColor: CompositeColor): GraphDesktopViewItem

	fun createTruthTableDesktopViewItem(item: TruthTableLibraryItem): GraphDesktopViewItem

	fun createBooleanExpressionDesktopViewItem(item: BooleanExpressionLibraryItem): GraphDesktopViewItem

	fun createMemoryStorableGraphDesktopViewItem(item: MemoryLibraryItem): GraphDesktopViewItem

	fun createImageGraphDesktopViewItem(element: ImageLibraryElement): GraphDesktopViewItem

	fun createFSMDesktopViewItem(item: FSMLibraryItem): GraphDesktopViewItem

	fun createDocumentationDesktopViewItem(documentation: Document, metaGraphName: String): GraphDesktopViewItem

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
	private val openDocumentationRequestHandler: EventHandler<OpenDocumentationRequest> = { handle(it) }
	private val defaultLightColorHandler: EventHandler<DefaultLightColorEvent> = { handle(it) }
	private val applicationDataHandler: EventHandler<ApplicationDataEvent> = { handle(it) }

	init {
		eventBus.register(ApplicationDataEvent::class, applicationDataHandler)
		eventBus.register(OpenMemoryContentsRequest::class, openMemoryContentsRequestHandler)
		eventBus.register(DefaultLightColorEvent::class, defaultLightColorHandler)
		eventBus.register(OpenDocumentationRequest::class, openDocumentationRequestHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(applicationDataHandler)
		eventBus.unregister(openMemoryContentsRequestHandler)
		eventBus.unregister(defaultLightColorHandler)
		eventBus.unregister(openDocumentationRequestHandler)
	}

	private fun handle(event: OpenMemoryContentsRequest) {
		if (event.newDesktopView) {
			graphPanelViewController.desktopController.openVerticeView(event.verticeView) { color,_ ->
				view.createMemoryContentsDesktopViewItem(event, color)
			}
		} else {
			LOG.userTrail("Showing contents of '${event.verticeView.type}' ${event.verticeView.id} in dialog")
			view.showMemoryContents(event)
		}
	}

	private fun handle(event: OpenDocumentationRequest) {
		graphPanelViewController.desktopController.openVerticeView(event.subGraphVerticeView) { color,_ ->
			view.createDocumentationDesktopViewItem(event.documentation, event.metaGraphName)
		}
	}

	private fun handle(event: ApplicationDataEvent) {
		when (event.newData?.savable) {
			is TruthTableSavable -> handleTruthTableLibraryItem((event.newData!!.savable as TruthTableSavable).item as TruthTableLibraryItem)
			is BooleanExpressionSavable -> handleBooleanExpressionLibraryItem((event.newData!!.savable as BooleanExpressionSavable).item as BooleanExpressionLibraryItem)
			is MemorySavable -> handleMemoryLibraryItem((event.newData!!.savable as MemorySavable).item as MemoryLibraryItem)
			is ImageIdentificationSavable -> handleImageLibraryElement((event.newData!!.savable as ImageIdentificationSavable).item as ImageLibraryElement)
            is FSMSavable -> handleFSMLibraryItem((event.newData!!.savable as FSMSavable).item as FSMLibraryItem)
		}
	}

	private fun handleTruthTableLibraryItem(newItem: TruthTableLibraryItem) {
		// Avoid creation of new view in "Close with 'Want to save changes?' = Yes" scenario
		if (graphPanelViewController.desktopController.mainDesktopViewItem == null ||
			!graphPanelViewController.desktopController.mainDesktopViewItem!!.displays(newItem.storable)
		) {
			LOG.debug("Create new TruthTableDesktopViewItem")
			graphPanelViewController.desktopController.show(view.createTruthTableDesktopViewItem(newItem))
		}
	}

	private fun handleBooleanExpressionLibraryItem(newItem: BooleanExpressionLibraryItem) {
		// Avoid creation of new view in "Close with 'Want to save changes?' = Yes" scenario
		if (graphPanelViewController.desktopController.mainDesktopViewItem == null ||
			!graphPanelViewController.desktopController.mainDesktopViewItem!!.displays(newItem.storable)
		) {
			LOG.debug("Create new BooleanExpressionDesktopViewItem")
			graphPanelViewController.desktopController.show(view.createBooleanExpressionDesktopViewItem(newItem))
		}
	}

	private fun handleFSMLibraryItem(newItem: FSMLibraryItem) {
		if (graphPanelViewController.desktopController.mainDesktopViewItem == null ||
			!graphPanelViewController.desktopController.mainDesktopViewItem!!.displays(newItem.storable)
		) {
			LOG.debug("Create new FSMLibraryItem")
			graphPanelViewController.desktopController.show(view.createFSMDesktopViewItem(newItem))
		}
	}

	private fun handleMemoryLibraryItem(newItem: MemoryLibraryItem) {
		// Avoid creation of new view in "Close with 'Want to save changes?' = Yes" scenario
		if (graphPanelViewController.desktopController.mainDesktopViewItem == null ||
			!graphPanelViewController.desktopController.mainDesktopViewItem!!.displays(newItem.storable)
		) {
			LOG.debug("Create new MemoryStorableDesktopViewItem")
			graphPanelViewController.desktopController.show(view.createMemoryStorableGraphDesktopViewItem(newItem))
		}
	}

	private fun handleImageLibraryElement(newElement: ImageLibraryElement) {
		// Avoid creation of new view in "Close with 'Want to save changes?' = Yes" scenario
		if (graphPanelViewController.desktopController.mainDesktopViewItem == null ||
			!graphPanelViewController.desktopController.mainDesktopViewItem!!.displays(newElement.storable)
		) {
			LOG.debug("Create new ImageGraphDesktopViewItem")
			graphPanelViewController.desktopController.show(view.createImageGraphDesktopViewItem(newElement))
		}
	}

	private fun handle(event: DefaultLightColorEvent) {
		if (event.graphView.defaultLightColor != null && view.shouldReplaceLightColor()) {
			(GraphViewModule.graphViewAppService as AntaresGraphViewService).replaceLightColor(event.graphView)
		}
	}
}