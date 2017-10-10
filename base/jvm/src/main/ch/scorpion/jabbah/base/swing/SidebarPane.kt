package ch.scorpion.jabbah.base.swing

import ch.scorpion.jabbah.base.logger
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

/**
 * A [JPanel] that stacks multiple collapsed views at the right side, allowing the user to display one of
 * them on demand.
 *
 * @property isOpenChangeHandler a callback called by [SidebarPane] whenever the [isOpen] property has changed,
 * which allows the owner of this [SidebarPane] to adjust its view, if necessary (such as using this [SidebarPane]
 * within a [JSplitPane], if it is open).
 */
class SidebarPane(private val isOpenChangeHandler: () -> Unit) : JPanel() {

    companion object {
        private val LOG by logger(SidebarPane::class)
    }

    /** Determines whether this [SidebarPane] is currently open, i.e. whether it displayes one if its content views.*/
    val isOpen: Boolean get() = current != null

    /** The [JPanel] at the right side containing the vertical [JLabel]s. */
    private val labelPanel = JPanel()

    /** Contains the content of the current [Entry] in the center and the title bar in the north. */
    private val contentPanel = JPanel()

    /** Displays the name of the current content in the title bar.*/
    private val titleLabel = JLabel()

    private val headerPanel = JPanel()

    private val entries = mutableListOf<Entry>()

    private var current: Entry? = null

    private val labelListener = VerticalLabelListener()

    init {
        initUI()
    }

    /**
     * Adds a new content view to this [SidebarPane].
     * @param name the translated name of the content to be displayed in the vertical button (if the content is closed)
     * or in the title bar (if the content is closed).
     */
    fun add(name: String, iconPath: String, content: JComponent) {
        val entry = Entry(name, ImageIcon(SidebarPane::class.java.getResource(iconPath)), content)
        entries.add(entry)
        entry.label.addMouseListener(labelListener)
        labelPanel.add(entry.label)
    }

    private fun initUI() {
        headerPanel.layout = BoxLayout(headerPanel, BoxLayout.X_AXIS)
        headerPanel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        headerPanel.add(titleLabel)
        headerPanel.add(Box.createGlue())

        val collapseButton = JButton()
        collapseButton.addActionListener({ collapse() })
        collapseButton.icon = ImageIcon(SidebarPane::class.java.getResource("/img/double-arrow-right-16.png"))
        collapseButton.border = BorderFactory.createEmptyBorder()
        collapseButton.toolTipText = "Hide"
        headerPanel.add(collapseButton)
        headerPanel.background = getBackgroundDivertColor()

        contentPanel.layout = BorderLayout()

        labelPanel.layout = BoxLayout(labelPanel, BoxLayout.Y_AXIS)
        layout = BorderLayout()
        add(labelPanel, BorderLayout.EAST)
        add(contentPanel, BorderLayout.CENTER)
    }

    private data class Entry(val name: String, private val icon: Icon, val content: JComponent) {
        val label: JLabel = VerticalLabel.create(name, icon)

        init {
            label.border = BorderFactory.createEmptyBorder(5, 10, 5, 10)
            label.isOpaque = true
        }
    }

    private fun getEntry(label: JLabel): Entry = entries.first { it.label === label }

    private fun activate(entry: Entry?) {
        LOG.debug("SidebarPanel: activate entry ${entry?.name}")
        val changed = current != entry

        if (current != null) {
            contentPanel.remove(headerPanel)
            contentPanel.remove(current!!.content)
            current!!.label.background = background
        }
        current = entry
        if (current != null) {
            titleLabel.text = current!!.name
            contentPanel.add(headerPanel, BorderLayout.NORTH)
            contentPanel.add(current!!.content, BorderLayout.CENTER)
            current!!.label.background = background.darker()
        }
        if (changed) {
            isOpenChangeHandler.invoke()
        }
        revalidate()
        repaint()
    }

    private fun collapse() {
        LOG.debug("SidebarPanel: collapse")
        activate(null)
    }

    private fun getBackgroundDivertColor(): Color {
        val bg = this@SidebarPane.background
        return Color(bg.red - 24, bg.green - 24, bg.blue - 24)
    }

    private inner class VerticalLabelListener : MouseAdapter() {

        override fun mouseEntered(e: MouseEvent?) {
            if (!isCurrent(e!!)) {
                (e.source as JComponent).background = getBackgroundDivertColor()
            }
        }

        override fun mouseExited(e: MouseEvent?) {
            if (!isCurrent(e!!)) {
                (e.source as JComponent).background = this@SidebarPane.background
            }
        }

        override fun mouseClicked(e: MouseEvent?) {
            activate(getEntry(e!!.source as JLabel))
        }

        private fun isCurrent(e: MouseEvent): Boolean {
            return current != null && current!!.label === e.source
        }
    }
}