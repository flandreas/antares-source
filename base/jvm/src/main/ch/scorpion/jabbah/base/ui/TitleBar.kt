package ch.scorpion.jabbah.base.ui

import java.awt.Color
import javax.swing.*

class TitleBar(
    text: String
) : JPanel() {

    private val title = JLabel(text)

    init {
        title.font = UIBasics.TITLE_FONT
        buildUI()
    }

    private fun buildUI() {
        layout = BoxLayout(this, BoxLayout.LINE_AXIS)
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.lightGray),
            BorderFactory.createEmptyBorder(6, 5, 6, 5)
        )
        isOpaque = true
        add(title)
        add(Box.createHorizontalGlue())
    }
}