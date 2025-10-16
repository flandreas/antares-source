package ch.scorpion.jabbah.base.swing.taskpane

import ch.scorpion.jabbah.base.logger
import com.formdev.flatlaf.ui.FlatEmptyBorder
import org.jdesktop.swingx.JXTaskPaneContainer
import javax.swing.UIManager

open class JabbahTaskPaneContainer : JXTaskPaneContainer() {

    companion object {
        private val LOG by logger(JabbahTaskPaneContainer::class)
        const val UI_CLASS_ID = "JabbahTaskPaneContainerUI"
    }

    init {
        border = FlatEmptyBorder()
    }

    private fun setUI(ui: JabbahTaskPaneContainerUI) {
        super.setUI(ui)
    }

    override fun getUI(): JabbahTaskPaneContainerUI = ui as JabbahTaskPaneContainerUI

    override fun getUIClassID(): String? = UI_CLASS_ID

    override fun updateUI() {
        if (UIManager.get(uiClassID) != null) {
            setUI(UIManager.getUI(this) as JabbahTaskPaneContainerUI)
        } else {
            LOG.error("Could not set JabbahTaskPaneContainerUI")
        }
    }
}