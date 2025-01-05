package ch.scorpion.jabbah.base.ui

import ch.scorpion.jabbah.base.swing.UiUtil
import javax.swing.*

class TitleBar(
    text: String
) : JPanel() {

    private val title = JLabel(text)

    var text: String
        get() = title.text
        set(value) {
            title.text = value
        }

    init {
        title.font = UIBasics.TITLE_FONT
        buildUI()
    }

    private fun buildUI() {
        layout = BoxLayout(this, BoxLayout.LINE_AXIS)
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UiUtil.getBackgroundDivertColor(this)),
            BorderFactory.createEmptyBorder(6, 5, 6, 5)
        )
        isOpaque = true
        add(title)
        add(Box.createHorizontalGlue())
    }
}