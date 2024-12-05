package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Disposable
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.draw.CloseViewRequest
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.model.text.description.NameChangedEvent
import ch.scorpion.jabbah.graph.library.UndoableStateLibraryItem
import ch.scorpion.jabbah.graph.ui.desktop.*
import ch.scorpion.jabbah.graph.view.GraphView
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
    private val applicationDataHolder: ApplicationDataHolder,
    private val eventBus: EventBus = BaseModule.eventBus,
    actions: List<Action> = emptyList()
) : AbstractGraphDesktopViewItemSwing() {

    private val closeViewRequestHandler: EventHandler<CloseViewRequest> = { handle(it) }

    private val nameChangedHandler: EventHandler<NameChangedEvent> = { handle(it) }

    private val headerLabel = UIBasics.createHeaderLabel(initialTitle)

    private val headerPanel = GraphDesktopItemHeaderPanelSwing(this, headerLabel, allowClose = true, actions = actions)

    protected abstract fun createHeaderText(): String

    init {
        eventBus.register(CloseViewRequest::class, closeViewRequestHandler)
        eventBus.register(NameChangedEvent::class, nameChangedHandler)
        buildUI()
    }

    /** ---- [GraphDesktopViewItem] */

    override fun addContextColorBorder(color: Color) {}

    override fun removeContextColorBorder() {}

    override val drawingView: DrawingView<GraphView>? get() = null

    override fun disposeItem() {
        eventBus.unregister(closeViewRequestHandler)
        eventBus.unregister(nameChangedHandler)
        if (contentPanel is Disposable) {
            contentPanel.dispose()
        }
    }

    override fun findContent(condition: (DrawingViewContent<GraphView>) -> Boolean): DrawingViewContent<*>? = null

    override fun createCloseRequest(): Any = CloseViewRequest(this)

    /** ---- [AbstractTitledGraphDesktopViewItemSwing] */

    private fun buildUI() {
        layout = BorderLayout()
        add(headerPanel, BorderLayout.NORTH)
        add(contentPanel, BorderLayout.CENTER)
    }

    private fun handle(request: CloseViewRequest) {
        if (request.view === this) {
            eventBus.postTwoPhase(
                prepareEvent = GraphDesktopViewItemCloseQuestion(this, isRoot = true),
                execEvent = GraphDesktopViewItemCloseRequest(this, isRoot = true)
            )
        }
    }

    private fun handle(event: NameChangedEvent) {
        val content = applicationDataHolder.data?.content
        if (content is UndoableStateLibraryItem<*> && content.storable === event.owner) {
            headerLabel.text = createHeaderText()
        }
    }
}