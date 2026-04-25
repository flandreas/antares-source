package io.antarescircuit.jabbah.graph

import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.ApplicationDataHolder
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.Disposable
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.UIBasics
import io.antarescircuit.jabbah.draw.CloseViewRequest
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewContent
import io.antarescircuit.jabbah.edit.model.text.description.NameChangedEvent
import io.antarescircuit.jabbah.graph.library.UndoableStateLibraryItem
import io.antarescircuit.jabbah.graph.ui.desktop.*
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * Displays top-level data as a [contentPanel] and a title whose text is derived from
 * the current [ApplicationData].
 *
 * Listens for [NameChangedEvent]s from the current [ApplicationData]'s content to update
 * the header text.
 */
abstract class AbstractTitledGraphDesktopViewItemSwing(
    initialTitle: String,
    protected val contentPanel: JPanel,
    private val applicationDataHolder: ApplicationDataHolder?,
    private val eventBus: EventBus = BaseModule.eventBus,
    private val isRoot: Boolean = true,
    actions: List<Action> = emptyList()
) : AbstractGraphDesktopViewItemSwing(reusable = false) {

    private val closeViewRequestHandler: EventHandler<CloseViewRequest> = { handle(it) }

    private val nameChangedHandler: EventHandler<NameChangedEvent> = { handle(it) }

    private val headerLabel = UIBasics.createHeaderLabel(initialTitle)

    private val headerPanel = GraphDesktopItemHeaderPanelSwing(this, headerLabel, { headerLabel.text }, allowClose = true, actions = actions)

    protected abstract fun createHeaderText(): String

    /**
     * The object referenced by [CloseViewRequest] potentially handled by this object.
     * Returns typically `this`, but could also be some inner class, such as a [View].
     */
    protected open val closeTarget: Any get() = this

    init {
        eventBus.register(CloseViewRequest::class, closeViewRequestHandler)
        eventBus.register(NameChangedEvent::class, nameChangedHandler)
        buildUI()
    }

    /** ---- [GraphDesktopViewItem] */

    override fun addContextColorBorder(color: Color) {}

    override fun removeContextColorBorder() {}

    override val drawingView: DrawingView<GraphElementView<*>, GraphView>? get() = null

    override fun disposeItem() {
        eventBus.unregister(closeViewRequestHandler)
        eventBus.unregister(nameChangedHandler)
        if (contentPanel is Disposable) {
            contentPanel.dispose()
        }
    }

    override fun findContent(condition: (DrawingViewContent<GraphElementView<*>, GraphView>) -> Boolean): DrawingViewContent<*,*>? = null

    override fun createCloseRequest(): Any = CloseViewRequest(closeTarget)

    /** ---- [AbstractTitledGraphDesktopViewItemSwing] */

    private fun buildUI() {
        layout = BorderLayout()
        add(headerPanel, BorderLayout.NORTH)
        add(contentPanel, BorderLayout.CENTER)
    }

    private fun handle(request: CloseViewRequest) {
        if (request.view === closeTarget) {
            eventBus.postTwoPhase(
                prepareEvent = GraphDesktopViewItemCloseQuestion(this, isRoot = isRoot),
                execEvent = GraphDesktopViewItemCloseRequest(this, isRoot = isRoot),
            )
        }
    }

    private fun handle(event: NameChangedEvent) {
        val content = applicationDataHolder?.data?.content
        if (content is UndoableStateLibraryItem<*> && content.storable === event.owner) {
            headerLabel.text = createHeaderText()
        }
    }
}