package io.antarescircuit.antares.view

import io.antarescircuit.antares.model.addressable.MemoryLibraryItem
import io.antarescircuit.antares.model.addressable.MemorySavable
import io.antarescircuit.antares.model.expression.BooleanExpressionLibraryItem
import io.antarescircuit.antares.model.expression.BooleanExpressionSavable
import io.antarescircuit.antares.model.fsm.FSMLibraryItem
import io.antarescircuit.antares.model.fsm.FSMSavable
import io.antarescircuit.antares.model.truthtable.TruthTableLibraryItem
import io.antarescircuit.antares.model.truthtable.TruthTableSavable
import io.antarescircuit.antares.view.addressable.OpenMemoryContentsRequest
import io.antarescircuit.antares.view.app.AntaresGraphViewService
import io.antarescircuit.jabbah.app.ApplicationDataEvent
import io.antarescircuit.jabbah.app.ApplicationDataViewController
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.graph.model.Document
import io.antarescircuit.jabbah.graph.model.image.ImageIdentificationSavable
import io.antarescircuit.jabbah.graph.model.image.ImageLibraryElement
import io.antarescircuit.jabbah.graph.ui.GraphFrame
import io.antarescircuit.jabbah.graph.ui.GraphFrameController
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem
import io.antarescircuit.jabbah.graph.ui.documentation.OpenDocumentationRequest
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule

interface AntaresFrame : GraphFrame {

	fun createMemoryContentsDesktopViewItem(request: OpenMemoryContentsRequest, contextColor: CompositeColor): GraphDesktopViewItem

	fun createTruthTableDesktopViewItem(item: TruthTableLibraryItem): GraphDesktopViewItem

	fun createBooleanExpressionDesktopViewItem(item: BooleanExpressionLibraryItem): GraphDesktopViewItem

	fun createMemoryStorableGraphDesktopViewItem(item: MemoryLibraryItem): GraphDesktopViewItem

	fun createImageGraphDesktopViewItem(element: ImageLibraryElement): GraphDesktopViewItem

	fun createFSMDesktopViewItem(item: FSMLibraryItem): GraphDesktopViewItem

	fun createDocumentationDesktopViewItem(documentation: Document, metaGraphName: String): GraphDesktopViewItem

	fun showMemoryContents(request: OpenMemoryContentsRequest)
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
	private val applicationDataHandler: EventHandler<ApplicationDataEvent> = { handle(it) }

	init {
		eventBus.register(ApplicationDataEvent::class, applicationDataHandler)
		eventBus.register(OpenMemoryContentsRequest::class, openMemoryContentsRequestHandler)
		eventBus.register(OpenDocumentationRequest::class, openDocumentationRequestHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(applicationDataHandler)
		eventBus.unregister(openMemoryContentsRequestHandler)
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
}