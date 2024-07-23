package ch.scorpion.antares.view.net.tunnel

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

    private var result: GlobalTunnelCollectionResult = emptyMap()

    var filteredTunnelNames: List<String> = emptyList()
        private set

    var allRichTextTunnelNames: Map<String, RichTextDrawable> = emptyMap()
        private set

    fun load() {
        result = GlobalTunnelCollector().collect()

        allRichTextTunnelNames = filteredTunnelNames.associateBy(
            { it },
            { RichTextDrawable.of(it, font) }
        )
        filterTunnelNames(null)

        view.updateResult()
    }

    fun filterTunnelNames(text: String?) {
        filteredTunnelNames = result.keys
            .filter { text == null || it.contains(text, ignoreCase = true) }
            .sorted()
        view.updateResult()
    }

    fun getUsages(tunnelName: String): List<GlobalTunnelUsage> = result[tunnelName]!!
}