package io.antarescircuit.antares.view.net.tunnel

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.Clipboard
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.draw.drawable.RichTextDrawable
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.draw.view.find.SearchRequest
import io.antarescircuit.jabbah.graph.SearchInMetaGraphRequest
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.OpenContainerLibraryElementRequest
import javax.swing.UIManager

interface GlobalTunnelPanel : UIView {

    /** Notifies this [GlobalTunnelUsage] that the set of tunnel names has changed.*/
    fun updateTunnelNames()

    /** Notifies this [GlobalTunnelUsage] that the set of [GlobalTunnelUsage]s has changed.*/
    fun updateUsages()
}

class GlobalTunnelPanelController(
    private val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<GlobalTunnelPanel>() {

    private val font = Graphics2DJvm.fromAwtFont(UIManager.getFont("Table.font"))

    /** The search result with ALL rich text tunnel names and their corresponding [GlobalTunnelUsage]s.*/
    private var result: GlobalTunnelCollectionResult = emptyMap()

    /** The rich text tunnel names after filtering. */
    var filteredTunnelNames: List<String> = emptyList()
        private set

    /** Maps all rich text tunnel names to the corresponding [RichTextDrawable] used for displaying.*/
    var allRichTextTunnelNames: Map<String, RichTextDrawable> = emptyMap()
        private set

    /** Maps all rich text tunnel names to the corresponding plain text names used for filtering.*/
    private var allPlainTextTunnelNames: Map<String, String> = emptyMap()

    val openCircuitAction: Action = OpenCircuitAction()

    val copyToClipboardAction: Action = CopyToClipboardAction()

    var selectedTunnelName: String? = null
        set(value) {
            field = value
            view.updateUsages()
            update()
        }

    var selectedUsage: GlobalTunnelUsage? = null
        set(value) {
            field = value
            update()
        }

    init {
        update()
    }

    private fun update() {
        copyToClipboardAction.enabled = selectedTunnelName != null
        openCircuitAction.enabled = selectedUsage != null
    }

    fun load() {
        result = GlobalTunnelCollector().collect()

        allRichTextTunnelNames = result.keys.associateBy(
            { it },
            { RichTextDrawable.of(it, font) }
        )
        allPlainTextTunnelNames = result.keys.associateBy(
            { it },
            { RichText.stripToPlainText(it) }
        )

        filterTunnelNames(null)

        view.updateTunnelNames()
    }

    fun filterTunnelNames(text: String?) {
        filteredTunnelNames = result.keys
            .filter { text == null || it.contains(text, ignoreCase = true) }
            .sortedBy { allPlainTextTunnelNames[it] }
        view.updateTunnelNames()
        update()
    }

    fun getUsages(): List<GlobalTunnelUsage> =
        if (selectedTunnelName == null) {
            emptyList()
        } else {
            result[selectedTunnelName]!!
        }

    fun openSelectedUsage() {
        if (selectedUsage != null) {
            InvocationHandler.invoke {
                LibraryModule.libraryHolder.library.getContainerLibraryElement(selectedUsage!!.graphUUID)?.let {
                    eventBus.post(OpenContainerLibraryElementRequest(it))
                    System.invokeLater {
                        eventBus.post(SearchInMetaGraphRequest(selectedUsage!!.graphUUID, SearchRequest(selectedTunnelName!!)))
                    }
                }
            }
        }
    }

    private inner class OpenCircuitAction : AbstractAction("antares.globalTunnels.action.openCircuit") {
        override fun execute(event: ActionEvent) {
            openSelectedUsage()
        }
    }

    private inner class CopyToClipboardAction : AbstractAction("antares.globalTunnels.action.copyToClipboard") {
        override fun execute(event: ActionEvent) {
            selectedTunnelName?.let {
                Clipboard.setStringContents(it)
            }
        }
    }
}