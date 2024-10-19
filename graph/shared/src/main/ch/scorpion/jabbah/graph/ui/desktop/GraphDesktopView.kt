package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.app.ApplicationDataContentEstablishedEvent
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.draw.DrawableContainerEvent
import ch.scorpion.jabbah.draw.container.DrawableContainerAdapter
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.ReferenceColor
import ch.scorpion.jabbah.draw.graphics.ReferenceColorEvent
import ch.scorpion.jabbah.draw.graphics.ReferenceColorSequenceProvider
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.CurrentLibraryEvent
import ch.scorpion.jabbah.graph.ui.graphpanel.EditedGraphViewEvent
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.vertice.OpenHierarchySubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Displays an optional main [GraphDesktopViewItem] and multiple additional [GraphDesktopViewItem] that
 * are associated with [VerticeView]s in the main [GraphDesktopViewItem]. A typical implementation
 * might display the main [GraphDesktopViewItem] in a large area at the left side, and additional
 * [GraphDesktopViewItem]s below each other in a area at the right side.
 */
interface GraphDesktopView : UIView {

	val mainDesktopViewItem: GraphDesktopViewItem?

	fun createSubGraphDesktopItem(
		verticeView: SubGraphVerticeView<*>,
		referenceColor: CompositeColor?,
		isParentDetached: Boolean,
		viewManager: ContentViewManager
	): GraphDesktopViewItem

	fun addGraphDesktopItem(item: GraphDesktopViewItem)

	fun closeItem(item: GraphDesktopViewItem)

	/** Closes all open [GraphDesktopViewItem]s. */
	fun closeAll()

	/** Closes all open [GraphDesktopViewItem]s and shows [item] as the main [GraphDesktopViewItem]. */
	fun show(item: GraphDesktopViewItem)
}

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
 * Listens for [CurrentLibraryEvent] and closes all open [GraphDesktopViewItem]s.
 */
class GraphDesktopViewController(
	val applicationContextHolder: GraphApplicationContextHolder,
	private val viewManager: ContentViewManager = DrawViewModule.viewManager,
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

	private val closeRequestHandler: EventHandler<GraphDesktopViewItemCloseRequest> = {
		closeItem(it.item)
	}

	private val openRequestHandler: EventHandler<OpenSubGraphRequest> = { handle(it) }

	/** Replace reference color in all Associations */
	private val referenceColorHandler: EventHandler<ReferenceColorEvent> = { handle(it) }

	private val currentLibraryHandler: EventHandler<CurrentLibraryEvent> = { handle(it) }

	private val openHierarchyHandler: EventHandler<OpenHierarchySubGraphRequest> = { handle(it) }

	private val appDataContentHandler: EventHandler<ApplicationDataContentEstablishedEvent> = { handle(it) }

	/** Closes an open [GraphDesktopViewItem] when the corresponding [VerticeView] has been removed.*/
	private val removeListener = RemoveListener()

	val additionalDesktopItems: List<GraphDesktopViewItem> get() = associations.map { it.item }

	init {
		eventBus.register(EditedGraphViewEvent::class, editedGraphViewEventHandler)
		eventBus.register(GraphDesktopViewItemCloseRequest::class, closeRequestHandler)
		eventBus.register(OpenSubGraphRequest::class, openRequestHandler)
		eventBus.register(ReferenceColorEvent::class, referenceColorHandler)
		eventBus.register(CurrentLibraryEvent::class, currentLibraryHandler)
		eventBus.register(OpenHierarchySubGraphRequest::class, openHierarchyHandler)
		eventBus.register(ApplicationDataContentEstablishedEvent::class, appDataContentHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(EditedGraphViewEvent::class, editedGraphViewEventHandler)
		eventBus.unregister(GraphDesktopViewItemCloseRequest::class, closeRequestHandler)
		eventBus.unregister(OpenSubGraphRequest::class, openRequestHandler)
		eventBus.unregister(ReferenceColorEvent::class, referenceColorHandler)
		eventBus.unregister(CurrentLibraryEvent::class, currentLibraryHandler)
		eventBus.unregister(openRequestHandler)
		eventBus.unregister(appDataContentHandler)
	}

	/** ---- [GraphDesktopViewController] */

	/**
	 * Creates and opens a new [GraphDesktopViewItem] that shows the contents of a [VerticeView].
	 *
	 * @param vv the [VerticeView] whose content is to be shown.
	 * @param itemFactory creates the [GraphDesktopViewItem] using the specified [CompositeColor] as reference
	 * between the [VerticeView] and the [GraphDesktopViewItem]
	 */
	fun openVerticeView(
		vv: VerticeView<*>,
		itemFactory: (CompositeColor,isParentDetached: Boolean) -> GraphDesktopViewItem
	) {
		if (associations.any { it.contains(vv) }) {
			eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = vv, messageKey = "graph.vertice.alreadyOpen.msg"))
			return
		}

		// If no GraphDesktopViewItem found, the VerticeView is contained in another top-level view
		// and doesn't need to be opened in this GraphDesktopView
		itemContaining(vv)?.let {
			val refColor = referenceColorSequence.next()
			val displayedColor = displayedReferenceColor(refColor)
			val newItem = itemFactory.invoke(displayedColor, it.isDetached)
			associations.add(Association(it, vv.id, newItem, refColor))

			view.addGraphDesktopItem(newItem)
			newItem.view?.view?.requestFocus()

			it.drawingView?.highlighter?.highlight(vv, displayedColor)
			it.drawingView?.repaint()
		}
	}

	private fun refreshHighlightsInMain() {
		System.invokeLater {
			view.mainDesktopViewItem?.let { item ->
				associations
					.filter { it.sourceItem == item }
					.forEach { assoc ->
						val component = item.drawingView?.drawing?.getWithId(assoc.refId)
						if (component != null && assoc.refColor != null) {
							item.drawingView?.highlighter?.highlight(component, displayedReferenceColor(assoc.refColor))
						}
					}
			}
		}
	}

	private fun openHierarchySubGraph(subGraphVerticeView: SubGraphVerticeView<*>, rootGraphView: GraphView) {
		if (view.mainDesktopViewItem?.drawingView?.drawing === rootGraphView) {
			val newItem = view.createSubGraphDesktopItem(subGraphVerticeView, null, false, viewManager)
			associations.add(Association(null, subGraphVerticeView.id, newItem, null))
			view.addGraphDesktopItem(newItem)
		}
	}

	private fun handle(event: EditedGraphViewEvent) {
		event.oldGraphView?.removeDrawableContainerListener(removeListener)
		event.newGraphView?.addDrawableContainerListener(removeListener)
	}

	private fun handle(@Suppress("UNUSED_PARAMETER") event: CurrentLibraryEvent) {
		closeAll()
	}

	private fun handle(request: OpenSubGraphRequest) {
		if (request.newView) {
			if (request.notifyIfBroken(eventBus)) {
				return
			}

			System.invokeLater {
				InvocationHandler.invoke {
					openSubGraphVerticeView(request.subGraphVerticeView)
				}
			}
		}
	}

	private fun handle(request: OpenHierarchySubGraphRequest) {
		System.invokeLater { openHierarchySubGraph(request.subGraphVerticeView, request.rootGraphView) }
	}

	private fun handle(event: ReferenceColorEvent) {
		LOG.trace("Update used ReferenceColors")
		val newAssociations = associations.map { assoc ->
			if (assoc.refColor != null) {
				assoc.copy(refColor = event.getNewColorFor(assoc.refColor)!!)
			} else {
				assoc
			}
		}
		associations.clear()
		associations.addAll(newAssociations)
		associations.forEach { assoc ->
			if (assoc.refColor != null) {
				assoc.item.contextColor = displayedReferenceColor(assoc.refColor)
			}
			event.replacements.forEach {
				assoc.item.drawingView?.highlighter?.replaceColor(
					displayedReferenceColor(it.oldColor),
					displayedReferenceColor(it.newColor)
				)
			}
		}
		event.replacements.forEach {
			view.mainDesktopViewItem?.drawingView?.highlighter?.replaceColor(displayedReferenceColor(it.oldColor), displayedReferenceColor(it.newColor))
		}
	}

	private fun handle(@Suppress("UNUSED_PARAMETER") event: ApplicationDataContentEstablishedEvent) {
		if (event.data.content is MetaGraph) {
			refreshHighlightsInMain()
		}
	}

	private fun openSubGraphVerticeView(verticeView: SubGraphVerticeView<*>) {
		openVerticeView(verticeView) { color, isParentDetached ->
			view.createSubGraphDesktopItem(verticeView, color, isParentDetached, viewManager) }
		LOG.userTrail("Open '${verticeView.model.getGraphIfPresent()?.name?.value}' in new desktop item")
	}

	fun closeItem(item: GraphDesktopViewItem) {
		LOG.userTrail("Close single desktop item")
		if (item === view.mainDesktopViewItem) {
			closeAll()
		} else {
			deassociate(item)
			item.disposeItem()
			view.closeItem(item)
		}
	}

	fun show(item: GraphDesktopViewItem) {
		deassociateAdditional()
		viewManager.activeView = item
		view.show(item)
	}

	fun closeAll() {
		deassociateAdditional()
		view.closeAll()
		viewManager.activeView = null
	}

	private fun deassociateAdditional() {
		additionalDesktopItems.forEach {
			deassociate(it)
			it.disposeItem()
		}
	}

	/**
	 * Deassociate the specified open [GraphDesktopViewItem] when it is being closed.
	 * Checks all existing [Association]s for the [DrawingViewContent]s that contains the associating [SubGraphVerticeView],
	 * and removes that [Association].
	 */
	private fun deassociate(item: GraphDesktopViewItem) {
		associationOf(item)?.let { assoc ->
			val content = assoc.sourceItem?.findContent { assoc.verticeView != null && it.drawing.contains(assoc.verticeView!!) }
			if (content != null) {
				deassociate(assoc, content, assoc.verticeView)
			}
		}
	}

	private fun deassociate(assoc: Association, content: DrawingViewContent<*>?, verticeView: VerticeView<*>?) {
		content?.let {
			if (verticeView != null) {
				it.highlighter.unhighlight(verticeView)
			}
			if (assoc.refColor != null) {
				referenceColorSequence.free(assoc.refColor)
			}
			associations.remove(assoc)
		}
	}

	private fun associationOf(item: GraphDesktopViewItem): Association? =
		associations.firstOrNull { assoc -> assoc.item == item }

	/** Finds the [GraphDesktopViewItem] that contains the specified [VerticeView]. */
	private fun itemContaining(vv: VerticeView<*>): GraphDesktopViewItem? {
		if (view.mainDesktopViewItem?.drawingView?.drawing?.contains(vv) == true) {
			return view.mainDesktopViewItem
		}
		return additionalDesktopItems.firstOrNull { it.drawingView?.drawing?.contains(vv) ?: false }
	}

	private inner class RemoveListener : DrawableContainerAdapter<GraphElementView<*>>() {
		override fun drawableRemoved(event: DrawableContainerEvent<GraphElementView<*>>) {
			if (event.child is VerticeView<*>) {
				associations.firstOrNull { it.refId == (event.child as VerticeView<*>).id }?.let { assoc ->

					// Explicitly call deassociate() with Content because deassociate() in close() wouldn't
					// find the Content, because the VerticeView has already been deleted
					if (assoc.sourceItem != null) {
						deassociate(assoc, assoc.sourceItem.drawingView?.content, event.child as VerticeView<*>)
					}

					closeItem(assoc.item)
				}
			}
		}
	}

	/**
	 * Maintains an association between a [VerticeView] and the [GraphDesktopViewItem] that has been opened
	 * in a [GraphDesktopView], along with the [ReferenceColor] that is used as a visual reference.
	 */
	private data class Association(
		val sourceItem: GraphDesktopViewItem?,
		val refId: Int,
		val item: GraphDesktopViewItem,
		val refColor: ReferenceColor?
	) {
		val verticeView: VerticeView<*>? get() = sourceItem?.drawingView?.drawing?.getWithId(refId) as VerticeView<*>?

		fun contains(verticeView: VerticeView<*>): Boolean = this.verticeView == verticeView
	}
}