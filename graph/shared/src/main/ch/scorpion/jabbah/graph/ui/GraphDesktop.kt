package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawableContainerEvent
import ch.scorpion.jabbah.draw.container.DrawableContainerAdapter
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.ReferenceColorEvent
import ch.scorpion.jabbah.draw.graphics.ReferenceColorSequenceProvider
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Manages multiple open views that display [GraphView] content.
 */
interface GraphDesktop {

	val mainDesktopItem: GraphDesktopItem

	fun createSubGraphDesktopItem(
		verticeView: SubGraphVerticeView<*>,
		referenceColor: CompositeColor,
		viewManager: ViewManager,
		scheduler: Scheduler
	): GraphDesktopItem

	fun addGraphDesktopItem(item: GraphDesktopItem)

	fun closeItem(item: GraphDesktopItem)

	fun closeAll(establishSingleView: Boolean)
}

/**
 * Maintains an association between a [VerticeView] and the [GraphDesktopItem] that has been opened
 * in a [GraphDesktop], along with the [CompositeColor] that is used as a visual reference.
 */
data class Association(
	val sourceItem: GraphDesktopItem,
	val ref: VerticeView<*>,
	val item: GraphDesktopItem,
	val refColor: CompositeColor)

class GraphDesktopController(
	private val viewManager: ViewManager = DrawViewModule.viewManager,
	private val scheduler: Scheduler = ExecutionModule.scheduler,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	companion object {
		private val LOG by logger(GraphDesktopController::class)
	}

	var view: GraphDesktop
		get() = _view!!
		set(value) {
			_view = value
			viewManager.registerView(value.mainDesktopItem.drawingView!!)
		}

	private var _view: GraphDesktop? = null

	/** Associates [SubGraphVerticeView] and their open [GraphDesktopItem]s.*/
	val associations = mutableListOf<Association>()

	/** Used for determining a [CompositeColor] for referencing a [SubGraphVerticeView] and its open [GraphDesktopItem].*/
	private var referenceColorSequence = ReferenceColorSequenceProvider.provide()

	private val editedGraphViewEventHandler: EventHandler<EditedGraphViewEvent> = { handle(it) }

	private val closeRequestHandler: EventHandler<GraphDesktopItemCloseRequest> = { closeItem(it.item)}

	private val applicationDataHandler: EventHandler<ApplicationDataEvent> = { handle(it) }

	private val openRequestHandler: EventHandler<OpenSubGraphRequest> = { handle(it) }

	/** Replace reference color in all Associations */
	private val referenceColorHandler: EventHandler<ReferenceColorEvent> = { handle(it) }

	/** Closes an open [GraphDesktopItem] when the corresponding [VerticeView] has been removed.*/
	private val removeListener = RemoveListener()

	private val desktopItems: List<GraphDesktopItem> get() = associations.map { it.item }

	init {
		eventBus.register(EditedGraphViewEvent::class, editedGraphViewEventHandler)
		eventBus.register(GraphDesktopItemCloseRequest::class, closeRequestHandler)
		eventBus.register(ApplicationDataEvent::class, applicationDataHandler)
		eventBus.register(OpenSubGraphRequest::class, openRequestHandler)
		eventBus.register(ReferenceColorEvent::class, referenceColorHandler)
	}

	fun dispose() {
		eventBus.unregister(EditedGraphViewEvent::class, editedGraphViewEventHandler)
		eventBus.unregister(GraphDesktopItemCloseRequest::class, closeRequestHandler)
		eventBus.unregister(ApplicationDataEvent::class, applicationDataHandler)
		eventBus.unregister(OpenSubGraphRequest::class, openRequestHandler)
		eventBus.unregister(ReferenceColorEvent::class, referenceColorHandler)
	}

	/** ---- [GraphDesktopController] */

	fun openVerticeView(vv: VerticeView<*>, itemFactory: (CompositeColor) -> GraphDesktopItem) {
		LOG.debug("Open VerticeView in new GraphDesktopItem")
		val assoc = associations.firstOrNull { it.ref == vv }
		if (assoc != null) {
			eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = assoc.ref, messageKey = "graph.vertice.alreadyOpen.msg"))
			return
		}

		itemContaining(vv)?.let {
			val refColor = referenceColorSequence.next()
			val newItem = itemFactory.invoke(refColor)
			associations.add(Association(it, vv, newItem, refColor))

			view.addGraphDesktopItem(newItem)

			it.drawingView?.highlighter?.highlight(vv, refColor)
			it.drawingView?.repaint()
		} ?: LOG.error("VerticeView to be opened not found in open panels")
	}

	private fun handle(event: EditedGraphViewEvent) {
		event.oldGraphView?.removeDrawableContainerListener(removeListener)
		event.newGraphView?.addDrawableContainerListener(removeListener)
	}

	private fun handle(event: ApplicationDataEvent) {
		closeAll(event.newData != null)
	}

	private fun handle(request: OpenSubGraphRequest) {
		if (request.newView) {
			InvocationHandler.invoke { openSubGraphVerticeView(request.subGraphVerticeView) }
		}
	}

	private fun handle(event: ReferenceColorEvent) {
		LOG.debug("Update used ReferenceColors")
		val newAssociations = associations.map { assoc -> assoc.copy(refColor = event.getNewColorFor(assoc.refColor)!!) }
		associations.clear()
		associations.addAll(newAssociations)
		associations.forEach { assoc ->
			assoc.item.contextColor = assoc.refColor
			event.replacements.forEach { assoc.item.drawingView?.highlighter?.replaceColor(it.oldColor, it.newColor) }
		}
		event.replacements.forEach {
			view.mainDesktopItem.drawingView?.highlighter?.replaceColor(it.oldColor, it.newColor)
		}
	}

	private fun openSubGraphVerticeView(verticeView: SubGraphVerticeView<*>) {
		LOG.debug("Open SubGraphVerticeView in new GraphDesktopItem")
		openVerticeView(verticeView) { view.createSubGraphDesktopItem(verticeView, it, viewManager, scheduler)}
	}

	private fun closeItem(item: GraphDesktopItem) {
		LOG.debug(("Close single GraphDesktopItem"))
		deassociate(item)
		item.dispose()
		item.drawingView?.let { viewManager.unregisterView(it) }
		view.closeItem(item)
	}

	private fun closeAll(establishSingleView: Boolean) {
		if (establishSingleView) {
			LOG.debug("Close all child GraphDesktopItems")
		} else {
			LOG.debug("Close all GraphDesktopItems")
		}

		desktopItems.forEach {
			deassociate(it)
			it.dispose()
		}

		view.closeAll(establishSingleView)
		viewManager.activeView = null
	}

	/**
	 * Deassociate the specified open [GraphNavigationPanel] when it is being closed.
	 * Checks all existing [Association]s for the [DrawingViewContent]s that contains the associating [SubGraphVerticeView],
	 * and removes that [Association].
	 */
	private fun deassociate(item: GraphDesktopItem) {
		associationOf(item)?.let { assoc ->
			val content = assoc.sourceItem.findContent { it.drawing.contains(assoc.ref) }
			if (content != null) {
				deassociate(assoc, content)
			}
		}
	}

	private fun deassociate(assoc: Association, content: DrawingViewContent<*>?) {
		content?.let {
			it.highlighter.unhighlight(assoc.ref)
			referenceColorSequence.free(assoc.refColor)
			associations.remove(assoc)
		}
	}

	private fun associationOf(item: GraphDesktopItem): Association? =
		associations.firstOrNull { assoc -> assoc.item == item }

	/** Finds the [GraphNavigationPanel] that contains the specified [VerticeView]. */
	private fun itemContaining(vv: VerticeView<*>): GraphDesktopItem? {
		if (view.mainDesktopItem.drawingView!!.drawing.contains(vv)) {
			return view.mainDesktopItem
		}
		return desktopItems.firstOrNull { it.drawingView?.drawing?.contains(vv) ?: false }
	}

	private inner class RemoveListener : DrawableContainerAdapter<GraphElementView<*>>() {
		override fun drawableRemoved(event: DrawableContainerEvent<GraphElementView<*>>) {
			associations.firstOrNull { it.ref === event.child }?.let { assoc ->
				{
					closeItem(assoc.item)
					deassociate(assoc, assoc.sourceItem.drawingView?.content)
				}.invoke()
			}
		}
	}
}