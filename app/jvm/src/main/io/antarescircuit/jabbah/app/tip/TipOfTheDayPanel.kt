package io.antarescircuit.jabbah.app.tip

import io.antarescircuit.jabbah.base.StringUtils
import java.awt.Dimension
import javax.swing.*

/**
 * Displays [TipOfTheDay.content] and (if present) the image referenced by [TipOfTheDay.imagePath].
 */
class TipOfTheDayPanel(private val tip: TipOfTheDay) : JPanel() {

    companion object {
        private const val CONTENT_WIDTH = 300
    }

    init {
        buildUI()
    }

    private fun buildUI() {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)

        add(createContentPane())

        if (StringUtils.isNotEmpty(tip.imagePath)) {
            add(Box.createVerticalStrut(20))
            add(createImagePane())
        }

        add(Box.createVerticalGlue())
    }

    private fun createContentPane(): JComponent {
        val textPane = JTextPane()
        textPane.isEditable = false
        textPane.contentType = "text/html"
        textPane.text = tip.content
        textPane.isFocusable = false
        textPane.alignmentX = LEFT_ALIGNMENT

        // JTextPane calculates the height of HTML content from its current width. Set the
        // width before reading preferredSize, otherwise the height is calculated for the
        // component's default width and the wrapped content is clipped after pack().
        textPane.size = Dimension(CONTENT_WIDTH, Int.MAX_VALUE)
        val contentHeight = textPane.preferredSize.height
        textPane.preferredSize = Dimension(CONTENT_WIDTH, contentHeight)
        textPane.maximumSize = Dimension(Int.MAX_VALUE, contentHeight)
        return textPane
    }

    private fun createImagePane(): JComponent {
        val imageIcon = ImageIcon(TipOfTheDayPanel::class.java.getResource(tip.imagePath))
        return JLabel(null, imageIcon, JLabel.LEFT).apply {
            alignmentX = LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
        }
    }
}
