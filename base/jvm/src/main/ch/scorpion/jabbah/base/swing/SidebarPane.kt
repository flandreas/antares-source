package ch.scorpion.jabbah.base.swing

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.logger
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

/**
 * A [JPanel] that stacks multiple collapsed views at the particular [Location], allowing the user to display one of
 * them on demand.
 *
 * @property location the relative location of this [SidebarPane] within its containing [JPanel]. This defines
 * the orientation of the title and the arrow button for closing the [SidebarPane].
 * @property isOpenChangeHandler a callback called by [SidebarPane] whenever the [isOpen] property has changed,
 * which allows the owner of this [SidebarPane] to adjust its view, if necessary (such as using this [SidebarPane]
 * within a [JSplitPane], if it is open).
 */
class SidebarPane(
	private val location: Location,
	private val isOpenChangeHandler: () -> Unit
) : JPanel() {

    companion object {
        private val LOG by logger(SidebarPane::class)
    }

    enum class Location {

	    /** The [SidebarPane] is displayed at the bottom of the main content.*/
        Bottom {

		    override val iconPath: String = "/img/double-arrow-down-16.png"

	        override fun createLabel(name: String, icon: Icon): JLabel {
		        return JLabel(name, icon, SwingConstants.LEADING)
	        }

	        override fun initUI(panel: JPanel, labelPanel: JPanel, contentPanel: JPanel) {
		        labelPanel.layout = BoxLayout(labelPanel, BoxLayout.X_AXIS)
		        panel.add(labelPanel, BorderLayout.SOUTH)
		        panel.add(contentPanel, BorderLayout.CENTER)
	        }
        },

	    /** The [SidebarPane] is displayed at the right side of the main content.*/
        Right {

		    override val iconPath: String = "/img/double-arrow-right-16.png"

	        override fun createLabel(name: String, icon: Icon): JLabel {
		        return VerticalLabel.create(name, icon)
	        }

	        override fun initUI(panel: JPanel, labelPanel: JPanel, contentPanel: JPanel) {
		        labelPanel.layout = BoxLayout(labelPanel, BoxLayout.Y_AXIS)
		        panel.add(labelPanel, BorderLayout.EAST)
		        panel.add(contentPanel, BorderLayout.CENTER)
	        }
        },

	    /** The [SidebarPane] is displayed at the left side of the main content.*/
	    Left {

		    override val iconPath: String = "/img/double-arrow-left-16.png"

	        override fun createLabel(name: String, icon: Icon): JLabel {
		        return VerticalLabel.create(name, icon, clockwise = false)
	        }

	        override fun initUI(panel: JPanel, labelPanel: JPanel, contentPanel: JPanel) {
		        labelPanel.layout = BoxLayout(labelPanel, BoxLayout.Y_AXIS)
		        panel.add(labelPanel, BorderLayout.WEST)
		        panel.add(contentPanel, BorderLayout.CENTER)
	        }
        };

	    abstract fun initUI(panel: JPanel, labelPanel: JPanel, contentPanel: JPanel)
	    abstract fun createLabel(name: String, icon: Icon): JLabel
	    abstract val iconPath: String
    }

    /** Determines whether this [SidebarPane] is currently open, i.e. whether it displays one if its content views.*/
    val isOpen: Boolean get() = current != null

	/** Returns the index of the currently open content, of -1 if none is open.*/
	val openIndex: Int get() {
		if (current == null) {
			return -1
		}
		return entries.indexOf(current!!)
	}

    /** The [JPanel] at the right side (or top side) containing the vertical (or horizontal) [JLabel]s. */
    private val labelPanel = JPanel()

    /** Contains the content of the current [Entry] in the center and the title bar in the north. */
    private val contentPanel = JPanel()

	/** Contains all [Entries][Entry] registered with [add].*/
    private val entries = mutableListOf<Entry>()

    private var current: Entry? = null

    private val labelListener = VerticalLabelListener()

	private val collapseAction = CollapseAction()

	init {
        initUI()
    }

    /** Adds a new content view to this [SidebarPane].*/
    fun add(content: SidebarPaneContent) {
	    val entry = Entry(content)
        entries.add(entry)
        entry.label.addMouseListener(labelListener)
        labelPanel.add(entry.label)
    }

	/**
	 * Opens the content with the specified index.
	 * @param index the index of the content, or `-1` to close all content.
	 */
	fun open(index: Int) {
		if (index < 0) {
			activate(null)
		} else {
			activate(entries[index])
		}
	}

    private fun initUI() {
        contentPanel.layout = BorderLayout()

        layout = BorderLayout()
	    location.initUI(this, labelPanel, contentPanel)
    }

	private inner class Entry(private val content: SidebarPaneContent) : PropertyChangeListener<Any> {

		val label: JLabel

		val name: String get() = content.name

		val component: JComponent get() = content.component

		/** Displays the name of the current content in the title bar.*/
		private val titleLabel = JLabel()

		val headerPanel = JPanel()

		init {
			label = location.createLabel(name, content.icon)
			label.border = BorderFactory.createEmptyBorder(5, 10, 5, 10)
			label.isOpaque = true
			label.verticalAlignment = SwingConstants.CENTER

			fillHeaderPanel()

			content.addListener(this)
		}

		fun update() {
			titleLabel.text = name
		}

		private fun fillHeaderPanel() {
			headerPanel.layout = BoxLayout(headerPanel, BoxLayout.X_AXIS)
			headerPanel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
			headerPanel.add(titleLabel)
			headerPanel.add(Box.createGlue())
			headerPanel.background = getBackgroundDivertColor()

			content.actions.forEach {
				headerPanel.add(createButton(it))
				headerPanel.add(Box.createHorizontalStrut(5))
			}
			headerPanel.add(createButton(collapseAction))
		}

		private fun createButton(action: Action): JButton {
			val button = JButton(ActionWrapperSwing(action))
			button.border = BorderFactory.createEmptyBorder()
			button.icon = ImageIcon(SidebarPane::class.java.getResource(action.imagePath))
			button.text = null
			button.toolTipText = action.name
			return button
		}

		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			label.icon = content.icon
			label.text = content.name
		}
	}

    private fun getEntry(label: JLabel): Entry = entries.first { it.label === label }

    private fun activate(entry: Entry?) {
        LOG.debug("SidebarPanel: activate entry ${entry?.name}")
        val changed = current != entry

        if (current != null) {
            contentPanel.remove(current!!.headerPanel)
            contentPanel.remove(current!!.component)
            current!!.label.background = background
        }
        current = entry
        if (current != null) {
	        current!!.update()
            contentPanel.add(current!!.headerPanel, BorderLayout.NORTH)
            contentPanel.add(current!!.component, BorderLayout.CENTER)

	        current!!.label.background = Color(175, 175, 175)
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

	private inner class CollapseAction : AbstractAction("graph.action.collapse") {

		init {
			imagePath = location.iconPath
		}

		override fun execute(event: ActionEvent) {
			collapse()
		}
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
            val clickedEntry = getEntry(e!!.source as JLabel)
            if (isOpen && clickedEntry == current) {
                activate(null)
            } else {
                activate(clickedEntry)
            }
        }

        private fun isCurrent(e: MouseEvent): Boolean {
            return current != null && current!!.label === e.source
        }
    }
}