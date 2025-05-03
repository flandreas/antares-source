package ch.scorpion.jabbah.graph.documentation

import ch.scorpion.jabbah.app.ApplicationDataContentEvent
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphDocumentationEvent
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryServiceCallback
import ch.scorpion.jabbah.graph.model.Document

interface DocumentationPanelView : UIView {

    /** The text currently displayed in the view, possibly after being edited by the user.*/
    val viewText: String

    /** Notifies this view that the model data has changed and the text in the view should be updated.*/
    fun notifyModelDataChanged()

    /** Notifies this view that the editability has changed. The view should disable the documentation editor.*/
    fun notifyEditabilityChanged()

    fun refreshPreview()
}

/**
 * Displays the documentation [Document] of an [ApplicationDataHolder]'s application data interpreted
 * as [MetaGraph].
 */
class DocumentationPanelController(
    private val applicationDataHolder: ApplicationDataHolder,
    private val eventBus: EventBus = BaseModule.eventBus,
    private val commandManager: CommandManager = EditModule.commandManager,
) : AbstractUIController<DocumentationPanelView>(), LibraryServiceCallback {

    companion object {
        private val LOG by logger(DocumentationPanelController::class)
    }

    private val applicationDataHandler: EventHandler<ApplicationDataEvent> = { handle(it) }

    private val applicationDataContentHandler: EventHandler<ApplicationDataContentEvent> = { handle(it) }

    private val documentationHandler: EventHandler<MetaGraphDocumentationEvent> = { handle(it) }

    private var command: DocumentCommand? = null

    private var inDocumentChangeBegin = false

    var text: String? = null
        private set(value) {
            field = value
            if (!inDocumentChangeBegin) {
                view.notifyModelDataChanged()
            }
        }

    val refreshAction: Action = RefreshAction()

    var editable: Boolean = false
       private set(value) {
           if (field != value) {
               field = value
               view.notifyEditabilityChanged()
           }
       }

    init {
        eventBus.register(ApplicationDataEvent::class, applicationDataHandler)
        eventBus.register(ApplicationDataContentEvent::class, applicationDataContentHandler)
        eventBus.register(MetaGraphDocumentationEvent::class, documentationHandler)
        LibraryModule.libraryServiceCallbacks.add(this)
        updateEditability(false)
    }

    override fun dispose() {
        super.dispose()
        eventBus.unregister(applicationDataHandler)
        eventBus.unregister(applicationDataContentHandler)
        eventBus.unregister(documentationHandler)
        LibraryModule.libraryServiceCallbacks.remove(this)
    }

    /** ---- [LibraryServiceCallback] ---- */

    override fun beforeStoreMetaGraph(metaGraph: MetaGraph) {
        LOG.trace("beforeStoreMetaGraph")
        metaGraph.documentation = metaGraph.documentation?.withText(view.viewText)
        command = null
    }

    /** ---- [DocumentationPanelController] */

    /**
     * Called by the [DocumentationPanelView] when the user has finished changing the text, and this
     * [DocumentationPanelController] should update its model accordingly and in an undoable manner.
     */
    fun documentChangeBegin() {
        try {
            LOG.trace("documentChangeBegin")
            inDocumentChangeBegin = true
            command = DocumentCommand(applicationDataHolder, view.viewText, text)
            commandManager.execute(command!!)
        } finally {
            inDocumentChangeBegin = false
        }
    }

    fun documentChangeEnd() {
        LOG.trace("documentChangeEnd")
        command?.let {
            val newText = view.viewText
            // Update MetaGraph
            val metaGraph: MetaGraph = applicationDataHolder.data!!.content as MetaGraph
            metaGraph.documentation = metaGraph.documentation?.withText(newText) ?: Document(text = newText)
            // Update Command
            it.newValue = newText
        }
        command = null
    }

    private fun updateEditability(editable: Boolean) {
        refreshAction.enabled = editable
        this.editable = editable
    }

    private fun handle(event: ApplicationDataEvent) {
        if (event.newData?.content is MetaGraph?) {
            consumeTextOfMetaGraph(event.newData?.content as MetaGraph?)
        } else {
            text = null
        }
        updateEditability(event.newData?.savable?.editable ?: false)
    }

    private fun handle(event: ApplicationDataContentEvent) {
        if (event.data.content is MetaGraph?) {
            consumeTextOfMetaGraph(event.data.content as MetaGraph?)
        } else {
            text = null
        }
        updateEditability(event.data.savable.editable)
    }

    private fun handle(event: MetaGraphDocumentationEvent) {
        if (event.metaGraph === applicationDataHolder.data?.content) {
            consumeTextOfMetaGraph(event.metaGraph)
        }
    }

    private fun consumeTextOfMetaGraph(metaGraph: MetaGraph?) {
        text = metaGraph?.documentation?.text
    }

    private class DocumentCommand(
        val dataHolder: ApplicationDataHolder,
        var newValue: String,
        val oldValue: String? = null
    ) : AbstractCommand("graph.documentation.title"), Undoable {

        private val metaGraph: MetaGraph get() = dataHolder.data!!.content as MetaGraph

        override fun execute() {
            metaGraph.documentation = metaGraph.documentation?.withText(newValue)
                ?: Document(text = newValue)
        }

        override fun undo() {
            if (oldValue != null) {
                metaGraph.documentation = metaGraph.documentation?.withText(oldValue)
                    ?: Document(text = oldValue)
            } else {
                metaGraph.documentation = null
            }
        }
    }

    private inner class RefreshAction : AbstractAction("graph.documentation.refresh") {
        override fun execute(event: ActionEvent) {
            view.refreshPreview()
        }
    }
}