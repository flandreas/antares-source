package ch.scorpion.antares.view

import ch.scorpion.antares.view.addressable.OpenMemoryContentsRequest
import ch.scorpion.antares.view.app.DigitalGraphViewService
import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.ui.GraphDesktopViewItem
import ch.scorpion.jabbah.graph.ui.GraphFrame
import ch.scorpion.jabbah.graph.ui.GraphFrameController
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

interface AntaresFrame : GraphFrame {

	fun createMemoryContentsDesktopViewItem(request: OpenMemoryContentsRequest, contextColor: CompositeColor): GraphDesktopViewItem

	fun showMemoryContents(request: OpenMemoryContentsRequest)

	fun shouldReplaceLightColor(): Boolean
}

class AntaresFrameController(
	applicationDataHolder: ApplicationDataHolder,
	private val eventBus: EventBus = BaseModule.eventBus,
	editor: Editor = GraphViewModule.graphEditorFactory.invoke(eventBus),
	viewManager: ViewManager = DrawViewModule.viewManager,
	scheduler: Scheduler = ExecutionModule.scheduler,
	properties: Properties = BaseModule.properties
) : GraphFrameController<AntaresFrame>(
	applicationDataHolder, eventBus, editor, viewManager, scheduler, properties
) {

	private val openMemoryContentsRequestHandler: EventHandler<OpenMemoryContentsRequest> = { handle(it) }
	private val defaultLightColorHandler: EventHandler<DefaultLightColorEvent> = { handle(it) }

	init {
		eventBus.register(OpenMemoryContentsRequest::class, openMemoryContentsRequestHandler)
		eventBus.register(DefaultLightColorEvent::class, defaultLightColorHandler)
	}

	override fun dispose() {
		super.dispose()
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

	private fun handle(event: DefaultLightColorEvent) {
		if (event.graphView.defaultLightColor != null && view.shouldReplaceLightColor()) {
			(GraphViewModule.graphViewAppService as DigitalGraphViewService).replaceLightColor(event.graphView)
		}
	}
}