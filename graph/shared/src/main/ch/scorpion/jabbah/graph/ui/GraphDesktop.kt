package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ui.AbstractUIController
import ch.scorpion.jabbah.app.ui.UIView
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawableContainerEvent
import ch.scorpion.jabbah.draw.container.DrawableContainerAdapter
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.ReferenceColor
import ch.scorpion.jabbah.draw.graphics.ReferenceColorEvent
import ch.scorpion.jabbah.draw.graphics.ReferenceColorSequenceProvider
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.project.CurrentProjectEvent
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Displays a main [GraphDesktopViewItem] and multiple additional [GraphDesktopViewItem] that
 * are associated with [VerticeView]s in the main [GraphDesktopViewItem]. A typical implementation
 * might diaplys the main [GraphDesktopViewItem] in a large area at the left side, and additional
 * [GraphDesktopViewItem]s below each other in a area at the right side.
 */
interface GraphDesktopView : UIView {

	val mainDesktopViewItem: GraphDesktopViewItem

	fun createSubGraphDesktopItem(
		verticeView: SubGraphVerticeView<*>,
		referenceColor: CompositeColor,
		viewManager: ViewManager
	): GraphDesktopViewItem

	fun addGraphDesktopItem(item: GraphDesktopViewItem)

	fun closeItem(item: GraphDesktopViewItem)

	fun closeAll(establishSingleView: Boolean)
}

/** Displays a [DrawingView] within a separate [GraphDesktopViewItem].*/
interface GraphDesktopViewItem {

	val drawingView: DrawingView<GraphView>?

	var contextColor: CompositeColor?

	fun dispose()

	fun findContent(condition: (DrawingViewContent<GraphView>) -> Boolean): DrawingViewContent<*>?
}

data class GraphDesktopViewItemCloseRequest(val item: GraphDesktopViewItem)

/** Posted on [EventBus] when the currently (one and only) edited root [GraphView] changes. */
class EditedGraphViewEvent(
	val oldGraphView: GraphView?,
	val newGraphView: GraphView?
)

/**
 * Controls a [GraphDesktopView] and manages additional [GraphDesktopViewItem] displayed when
 * the user opens [VerticeView]s in separate views.
 * The main [GraphDesktopViewItem] is opened by the [GraphDesktopView] itself upon instantiation.
 *
 * Uses [ReferenceColor]s to visually connect [VerticeView]s and [GraphDesktopViewItem]s that the user
 * opens to show the contents of a [VerticeView].
 *
 * Listens for [OpenSubGraphRequest] and opens a new [GraphDesktopViewItem] for the specified
 * [SubGraphVerticeView].
 *
 * Listens for [GraphElementView]s being removed from the main [GraphView] and closes any
 * [GraphDesktopViewItem] associated with the removed [GraphElementView].
 *
 * Listens for [GraphDesktopViewItemCloseRequest]s and closes the referenced [GraphDesktopViewItem].
 * If the main [GraphDesktopViewItem] is closed, all associated [GraphDesktopViewItem]s are closed as well.
 *
 * Listens for [CurrentProjectEvent] and closes all open [GraphDesktopViewItem]s.
 */
class GraphDesktopViewController(
	private val viewManager: ViewManager = DrawViewModule.viewManager,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<GraphDesktopView>() {

	companion object {
		private val LOG by logger(GraphDesktopViewController::class)
		private const val REF_COLOR_ALPHA = 144

		private fun displayedReferenceColor(referenceColor: ReferenceColor): CompositeColor {
			return referenceColor.onBackground.exchange().withAlpha(REF_COLOR_ALPHA)
		}
	}

	/**
	 * TODO How to register the DrawingView of the mainDesktopItem with ViewManager
	 * when using 'lateinit' for [view]?
	 */

	/** Associates [SubGraphVerticeView] and their open [GraphDesktopViewItem]s.*/
	private val associations = mutableListOf<Association>()

	/** Used for determining a [CompositeColor] for referencing a [SubGraphVerticeView] and its open [GraphDesktopViewItem].*/
	private var referenceColorSequence = ReferenceColorSequenceProvider.provide()

	private val editedGraphViewEventHandler: EventHandler<EditedGraphViewEvent> = { handle(it) }

	private val closeRequestHandler: EventHandler<GraphDesktopViewItemCloseRequest> = { closeItem(it.item)}

	private val openRequestHandler: EventHandler<OpenSubGraphRequest> = { handle(it) }

	/** Replace reference color in all Associations */
	private val referenceColorHandler: EventHandler<ReferenceColorEvent> = { handle(it) }

	private val currentProjectHandler: EventHandler<CurrentProjectEvent> = { handle(it) }

	/** Closes an open [GraphDesktopViewItem] when the corresponding [VerticeView] has been removed.*/
	private val removeListener = RemoveListener()

	val additionalDesktopItems: List<GraphDesktopViewItem> get() = associations.map { it.item }

	init {
		eventBus.register(EditedGraphViewEvent::class, editedGraphViewEventHandler)
		eventBus.register(GraphDesktopViewItemCloseRequest::class, closeRequestHandler)
		eventBus.register(OpenSubGraphRequest::class, openRequestHandler)
		eventBus.register(ReferenceColorEvent::class, referenceColorHandler)
		eventBus.register(CurrentProjectEvent::class, currentProjectHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(EditedGraphViewEvent::class, editedGraphViewEventHandler)
		eventBus.unregister(GraphDesktopViewItemCloseRequest::class, closeRequestHandler)
		eventBus.unregister(OpenSubGraphRequest::class, openRequestHandler)
		eventBus.unregister(ReferenceColorEvent::class, referenceColorHandler)
		eventBus.unregister(CurrentProjectEvent::class, currentProjectHandler)
	}

	/** ---- [GraphDesktopViewController] */

	/** Closes all additional [GraphDesktopViewItem]s and shows only the main [GraphDesktopViewItem].*/
	fun showMainOnly() {
		closeAll(true)
	}

	/**
	 * Creates and opens a new [GraphDesktopViewItem] that shows the contents of a [VerticeView].
	 *
	 * @param vv the [VerticeView] whose content is to be shown.
	 * @param itemFactory creates the [GraphDesktopViewItem] using the specified [CompositeColor] as reference
	 * between the [VerticeView] and the [GraphDesktopViewItem]
	 */
	fun openVerticeView(vv: VerticeView<*>, itemFactory: (CompositeColor) -> GraphDesktopViewItem) {
		LOG.debug("Open VerticeView in new GraphDesktopItem")
		val assoc = associations.firstOrNull { it.ref == vv }
		if (assoc != null) {
			eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = assoc.ref, messageKey = "graph.vertice.alreadyOpen.msg"))
			return
		}

		itemContaining(vv)?.let {
			val refColor = referenceColorSequence.next()
			val displayedColor = displayedReferenceColor(refColor)
			val newItem = itemFactory.invoke(displayedColor)
			associations.add(Association(it, vv, newItem, refColor))

			view.addGraphDesktopItem(newItem)

			it.drawingView?.highlighter?.highlight(vv, displayedColor)
			it.drawingView?.repaint()
		} ?: LOG.error("VerticeView to be opened not found in open panels")
	}

	private fun handle(event: EditedGraphViewEvent) {
		event.oldGraphView?.removeDrawableContainerListener(removeListener)
		event.newGraphView?.addDrawableContainerListener(removeListener)
	}

	private fun handle(@Suppress("UNUSED_PARAMETER") event: CurrentProjectEvent) {
		closeAll(false)
	}

	private fun handle(request: OpenSubGraphRequest) {
		if (request.newView) {
			System.invokeLater { openSubGraphVerticeView(request.subGraphVerticeView) }
		}
	}

	private fun handle(event: ReferenceColorEvent) {
		LOG.debug("Update used ReferenceColors")
		val newAssociations = associations.map { assoc -> assoc.copy(refColor = event.getNewColorFor(assoc.refColor)!!) }
		associations.clear()
		associations.addAll(newAssociations)
		associations.forEach { assoc ->
			assoc.item.contextColor = displayedReferenceColor(assoc.refColor)
			event.replacements.forEach {
				assoc.item.drawingView?.highlighter?.replaceColor(
					displayedReferenceColor(it.oldColor),
					displayedReferenceColor(it.newColor))
			}
		}
		event.replacements.forEach {
			view.mainDesktopViewItem.drawingView?.highlighter?.replaceColor(displayedReferenceColor(it.oldColor), displayedReferenceColor(it.newColor))
		}
	}

	private fun openSubGraphVerticeView(verticeView: SubGraphVerticeView<*>) {
		LOG.debug("Open SubGraphVerticeView in new GraphDesktopItem")
		openVerticeView(verticeView) { view.createSubGraphDesktopItem(verticeView, it, viewManager)}
	}

	private fun closeItem(item: GraphDesktopViewItem) {
		LOG.debug(("Close single GraphDesktopItem"))
		if (item === view.mainDesktopViewItem) {
			closeAll(false)
		} else {
			deassociate(item)
			item.dispose()
			item.drawingView?.let { viewManager.unregisterView(it) }
			view.closeItem(item)
		}
	}

	fun closeAll(establishSingleView: Boolean = false) {
		if (establishSingleView) {
			LOG.debug("Close all child GraphDesktopItems")
		} else {
			LOG.debug("Close all GraphDesktopItems")
		}

		additionalDesktopItems.forEach {
			deassociate(it)
			it.dispose()
		}

		view.closeAll(establishSingleView)
		viewManager.activeView = null
	}

	/**
	 * Deassociate the specified open [GraphDesktopViewItem] when it is being closed.
	 * Checks all existing [Association]s for the [DrawingViewContent]s that contains the associating [SubGraphVerticeView],
	 * and removes that [Association].
	 */
	private fun deassociate(item: GraphDesktopViewItem) {
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

	private fun associationOf(item: GraphDesktopViewItem): Association? =
		associations.firstOrNull { assoc -> assoc.item == item }

	/** Finds the [GraphDesktopViewItem] that contains the specified [VerticeView]. */
	private fun itemContaining(vv: VerticeView<*>): GraphDesktopViewItem? {
		if (view.mainDesktopViewItem.drawingView!!.drawing.contains(vv)) {
			return view.mainDesktopViewItem
		}
		return additionalDesktopItems.firstOrNull { it.drawingView?.drawing?.contains(vv) ?: false }
	}

	private inner class RemoveListener : DrawableContainerAdapter<GraphElementView<*>>() {
		override fun drawableRemoved(event: DrawableContainerEvent<GraphElementView<*>>) {
			associations.firstOrNull { it.ref === event.child }?.let { assoc ->

				// Explicitly call deassociate() with Content because deassociate() in close() wouldn't
				// find the Content, because the VerticeView has already been deleted
				deassociate(assoc, assoc.sourceItem.drawingView?.content)

				closeItem(assoc.item)
			}
		}
	}

	/**
	 * Maintains an association between a [VerticeView] and the [GraphDesktopViewItem] that has been opened
	 * in a [GraphDesktopView], along with the [ReferenceColor] that is used as a visual reference.
	 */
	data class Association(
		val sourceItem: GraphDesktopViewItem,
		val ref: VerticeView<*>,
		val item: GraphDesktopViewItem,
		val refColor: ReferenceColor)
}