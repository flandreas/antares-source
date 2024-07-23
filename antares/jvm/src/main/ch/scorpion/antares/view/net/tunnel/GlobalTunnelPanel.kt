package ch.scorpion.antares.view.net.tunnel

import ch.scorpion.jabbah.base.richtext.RichText
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.draw.drawable.RichTextDrawable
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
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
    }

    fun getUsages(tunnelName: String): List<GlobalTunnelUsage> = result[tunnelName]!!
}