package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.Addressable
import ch.scorpion.antares.model.addressable.MemoryLibraryItem
import ch.scorpion.antares.model.addressable.MemoryStorable
import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.draw.CloseViewRequest
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.vertice.ObjectLink
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import ch.scorpion.jabbah.graph.ui.desktop.AbstractGraphDesktopItemPanelSwing
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopItemHeaderPanelSwing
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItemCloseRequest
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout

class MemoryStorableGraphDesktopItemSwing(
    item: MemoryLibraryItem,
    private val applicationDataHolder: ApplicationDataHolder,
    applicationContextHolder: GraphApplicationContextHolder,
    commandManager: CommandManager,
    private val eventBus: EventBus = BaseModule.eventBus
) : AbstractGraphDesktopItemPanelSwing() {

    private val closeViewRequestHandler: EventHandler<CloseViewRequest> = { handle(it) }

    private val headerPanel = GraphDesktopItemHeaderPanelSwing(
        this,
        UIBasics.createHeaderLabel("${Translations.getString("library.element.memory.name")} \"${item.memoryStorable.name.getTranslation()}\""),
        allowClose = true)

    private val contentPanel = AddressableContentsPanel(null, applicationContextHolder, MemoryStorableLink(), commandManager)

    init {
        eventBus.register(CloseViewRequest::class, closeViewRequestHandler)
        buildUI()
    }

    private fun buildUI() {
        layout = BorderLayout()
        add(headerPanel, BorderLayout.NORTH)
        add(contentPanel, BorderLayout.CENTER)
    }

    /** ---- [GraphDesktopViewItem] */

    override fun addContextColorBorder(color: Color) {}

    override fun removeContextColorBorder() {}

    override val drawingView: DrawingView<GraphView>? get() = null

    override fun disposeItem() {
        eventBus.unregister(closeViewRequestHandler)
        contentPanel.dispose()
    }

    override fun findContent(condition: (DrawingViewContent<GraphView>) -> Boolean): DrawingViewContent<*>? = null

    override fun createCloseRequest(): Any = CloseViewRequest(this)

    /** ---- [MemoryStorableGraphDesktopItemSwing] */

    private fun handle(request: CloseViewRequest) {
        if (request.view === this) {
            eventBus.postTwoPhase(
                prepareEvent = GraphDesktopViewItemCloseRequest(this, isRoot = true),
                execEvent = GraphDesktopViewItemCloseRequest(this, isRoot = true)
            )
        }
    }

    private inner class MemoryStorableLink : ObjectLink<Addressable> {
        override fun getLinkedObject(startGraph: Graph?): Addressable {
            return applicationDataHolder.data!!.content as MemoryStorable
        }
    }
}