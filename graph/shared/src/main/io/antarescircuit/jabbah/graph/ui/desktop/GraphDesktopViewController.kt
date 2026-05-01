package io.antarescircuit.jabbah.graph.ui.desktop

import io.antarescircuit.jabbah.app.ApplicationDataContentEstablishedEvent
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.event.PropertyChangeEvent
import io.antarescircuit.jabbah.base.event.PropertyOwner
import io.antarescircuit.jabbah.base.event.PropertyOwnerImpl
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.draw.DrawableContainerEvent
import io.antarescircuit.jabbah.draw.container.DrawableContainerAdapter
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.graphics.ReferenceColor
import io.antarescircuit.jabbah.draw.graphics.ReferenceColorEvent
import io.antarescircuit.jabbah.draw.graphics.ReferenceColorSequenceProvider
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.DrawingViewContent
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.edit.model.ComponentMessageType
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.CurrentLibraryEvent
import io.antarescircuit.jabbah.graph.ui.graphpanel.EditedGraphViewEvent
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.vertice.OpenHierarchySubGraphRequest
import io.antarescircuit.jabbah.graph.view.vertice.OpenSubGraphRequest
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.getValue

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
    private val eventBus: EventBus = BaseModule.eventBus,
    propertyOwnerImpl: PropertyOwner<Any> = PropertyOwnerImpl(),
) : AbstractUIController<GraphDesktopView>(), PropertyOwner<Any> by propertyOwnerImpl {

    companion object {
        private val LOG by logger(GraphDesktopViewController::class)

        private const val REF_COLOR_ALPHA = 144

        private fun displayedReferenceColor(referenceColor: ReferenceColor): CompositeColor {
            return referenceColor.onBackground.exchange().withAlpha(REF_COLOR_ALPHA)
        }

        /** The name of [PropertyChangeEvent] sent if [mainDesktopViewItem] changes. */
        const val PROP_MAIN_DESKTOP_VIEW_ITEM = "mainDesktopViewItem"
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

    var mainDesktopViewItem: GraphDesktopViewItem? = null
        private set(value) {
            val oldValue = field
            field = value
            fire(PROP_MAIN_DESKTOP_VIEW_ITEM, oldValue, value)
        }

    val additionalDesktopItems: List<GraphDesktopViewItem> get() = associations.map { it.item }

    init {
        propertyOwnerImpl.source = this
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
            LOG.userTrail("Open '${vv.type}' ${vv.id} in new desktop item")

            val refColor = referenceColorSequence.next()
            val displayedColor = displayedReferenceColor(refColor)
            val newItem = itemFactory.invoke(displayedColor, it.isDetached)
            val elementRef = it.createElementRef(vv.id)
            associations.add(Association(it, elementRef, newItem, refColor))

            view.showChildItem(newItem)
            newItem.view?.view?.requestFocus()

            it.drawingView?.highlighter?.highlight(vv, displayedColor)
            it.drawingView?.repaint()
        }
    }

    private fun refreshHighlightsInMain() {
        System.invokeLater {
            mainDesktopViewItem?.let { item ->
                associations
                    .filter { it.sourceItem == item }
                    .forEach { assoc ->
                        val component = item.findElementWithRef(assoc.sourceElementRef)
                        if (component != null && assoc.refColor != null) {
                            item.drawingView?.highlighter?.highlight(component, displayedReferenceColor(assoc.refColor))
                        }
                    }
            }
        }
    }

    private fun openHierarchySubGraph(subGraphVerticeView: SubGraphVerticeView<*>, rootGraphView: GraphView) {
        if (mainDesktopViewItem?.drawingView?.drawing === rootGraphView) {
            val newItem = view.createSubGraphDesktopItem(subGraphVerticeView, null, false, viewManager)
            associations.add(Association(null, GraphDesktopViewItemElementDepthRef(subGraphVerticeView.id, 0), newItem, null))
            view.showChildItem(newItem)
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
            mainDesktopViewItem?.drawingView?.highlighter?.replaceColor(displayedReferenceColor(it.oldColor), displayedReferenceColor(it.newColor))
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
        LOG.userTrail("Open '${verticeView.model.getGraphIfPresent()?.name?.value}' ${verticeView.id} in new desktop item")
    }

    /**
     * Closes the specified [GraphDesktopViewItem]. If it is the main view, all other views are
     * closed as well.
     */
    fun closeItem(item: GraphDesktopViewItem) {
        LOG.userTrail("Close desktop item ${item::class.simpleName}")
        if (item === mainDesktopViewItem) {
            closeAll()
        } else {
            deassociate(item)
            freeItem(item)
            view.closeChildItem(item)
        }
    }

    private fun freeItem(item: GraphDesktopViewItem) {
        if (!item.reusable) {
            item.disposeItem()
        }
    }

    /**
     * Closes all open [GraphDesktopViewItem] and shows [item] as the main view.
     */
    fun show(item: GraphDesktopViewItem) {
        if (mainDesktopViewItem != null) {
            freeItem(mainDesktopViewItem!!)
        }
        deassociateAdditional()

        viewManager.activeView = item
        mainDesktopViewItem = item
        view.showMainItem(item)
    }

    fun closeAll() {
        if (mainDesktopViewItem != null) {
            freeItem(mainDesktopViewItem!!)
        }
        deassociateAdditional()

        associations.clear()
        referenceColorSequence.reset()

        mainDesktopViewItem = null
        view.closeAll()
        viewManager.activeView = null
    }

    private fun deassociateAdditional() {
        additionalDesktopItems.forEach {
            deassociate(it)
            freeItem(it)
        }
    }

    /**
     * Deassociate the specified open [GraphDesktopViewItem] when it is being closed.
     * Checks all existing [Association]s for the [DrawingViewContent]s that contains the associating [SubGraphVerticeView],
     * and removes that [Association].
     */
    private fun deassociate(item: GraphDesktopViewItem) {
        associationOf(item)?.let { assoc ->
            val content = assoc.sourceItem?.findContent {
                val vv = assoc.verticeView
                vv != null && it.drawing.contains(vv)
            }
            if (content != null) {
                deassociate(assoc, content, assoc.verticeView)
            }
        }
    }

    private fun deassociate(assoc: Association, content: DrawingViewContent<*,*>?, verticeView: VerticeView<*>?) {
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
        if (mainDesktopViewItem?.drawingView?.drawing?.contains(vv) == true) {
            return mainDesktopViewItem
        }
        return additionalDesktopItems.firstOrNull { it.drawingView?.drawing?.contains(vv) ?: false }
    }

    private inner class RemoveListener : DrawableContainerAdapter<GraphElementView<*>>() {
        override fun drawableRemoved(event: DrawableContainerEvent<GraphElementView<*>>) {
            if (event.child is VerticeView<*>) {
                associations.firstOrNull { it.sourceElementRef.verticeViewId == (event.child as VerticeView<*>).id }?.let { assoc ->

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
        val sourceElementRef: GraphDesktopViewItemElementRef,
        val item: GraphDesktopViewItem,
        val refColor: ReferenceColor?
    ) {
        val verticeView: VerticeView<*>? get() = sourceItem?.findElementWithRef(sourceElementRef)

        fun contains(verticeView: VerticeView<*>): Boolean = this.verticeView == verticeView
    }
}