package ch.scorpion.jabbah.base.swing.taskpane

import org.jdesktop.swingx.plaf.basic.BasicTaskPaneContainerUI
import java.awt.LayoutManager
import javax.swing.JComponent

class JabbahTaskPaneContainerUI : BasicTaskPaneContainerUI() {

    companion object {
        @Suppress("unused") // Reflection
        @JvmStatic
        fun createUI(c: JComponent): JabbahTaskPaneContainerUI = JabbahTaskPaneContainerUI()
    }

    override fun createDefaultLayout(): LayoutManager? = VerticalLayoutUIResource(0)
}