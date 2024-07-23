package ch.scorpion.antares.view.net.tunnel

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.richtext.RichText
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.draw.drawable.RichTextDrawable
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.OpenContainerLibraryElementRequest
import javax.swing.UIManager

interface GlobalTunnelPanel : UIView {
    fun updateResult()
}

class GlobalTunnelPanelController : AbstractUIController<GlobalTunnelPanel>() {

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

    var selectedUsage: GlobalTunnelUsage? = null
        set(value) {
            field = value
            update()
        }

    init {
        update()
    }

    private fun update() {
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

        view.updateResult()
    }

    fun filterTunnelNames(text: String?) {
        filteredTunnelNames = result.keys
            .filter { text == null || it.contains(text, ignoreCase = true) }
            .sortedBy { allPlainTextTunnelNames[it] }
        view.updateResult()
        update()
    }

    fun getUsages(tunnelName: String): List<GlobalTunnelUsage> = result[tunnelName]!!

    private inner class OpenCircuitAction : AbstractAction("antares.globalTunnels.action.openCircuit") {
        override fun execute(event: ActionEvent) {
            InvocationHandler.invoke {
                LibraryModule.libraryHolder.library.getContainerLibraryElement(selectedUsage!!.graphUUID)?.let {
                    BaseModule.eventBus.post(OpenContainerLibraryElementRequest(it))
                }
            }
        }
    }
}