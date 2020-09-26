package ch.scorpion.jabbah.base.swing

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.swing.SidebarPane.Location
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.AbstractBorder

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
	val openIndex: Int
		get() {
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
		labelPanel.border = SelectionBorder()

		layout = BorderLayout()
		location.initUI(this, labelPanel, contentPanel)
	}

	private inner class SelectionBorder(private val halfThickness: Int = 2) : AbstractBorder() {

		private val stroke = BasicStroke((2 * halfThickness).toFloat())

		override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
			val g2 = g as Graphics2D

			val oldColor = g2.color
			val oldStroke = g2.stroke

			g2.color = UIManager.getColor("TabbedPane.disabledUnderlineColor")
			g2.stroke = stroke

			for (entry in entries) {
				if (entry === current) {
					val bounds = entry.label.bounds
					when (location) {
						Location.Bottom -> g2.drawLine(bounds.x, halfThickness, bounds.x + bounds.width, halfThickness)
						Location.Right -> g2.drawLine(halfThickness, bounds.y, halfThickness, bounds.y + bounds.height)
						Location.Left -> g2.drawLine(width - halfThickness, bounds.y, width - halfThickness, bounds.y + bounds.height)
					}
				}
			}

			g2.color = oldColor
			g2.stroke = oldStroke
		}

		override fun getBorderInsets(c: Component, insets: Insets): Insets {
			when(location) {
				Location.Bottom -> insets.set(2 * halfThickness, 0, 0, 0)
				Location.Right -> insets.set(0, 2 * halfThickness, 0, 0)
				Location.Left -> insets.set(0, 0, 0, 2 * halfThickness)
			}

			return insets
		}
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
			headerPanel.border = BorderFactory.createEmptyBorder(2, 5, 2, 5)
			headerPanel.add(titleLabel)
			headerPanel.add(Box.createGlue())
			headerPanel.background = UiUtil.getBackgroundDivertColor(this@SidebarPane)

			content.actions.forEach {
				headerPanel.add(UiUtil.createToolBarButton(it))
				headerPanel.add(Box.createHorizontalStrut(5))
			}
			headerPanel.add(UiUtil.createToolBarButton(collapseAction))
		}

		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			label.icon = content.icon
			label.text = content.name
		}
	}

	private fun getEntry(label: JLabel): Entry = entries.first { it.label === label }

	private fun activate(entry: Entry?) {
		LOG.debug("activate entry ${entry?.name}")
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

			current!!.label.background = background
		}
		if (changed) {
			isOpenChangeHandler.invoke()
		}
		revalidate()
		repaint()
	}

	private fun collapse() {
		LOG.debug("collapse")
		activate(null)
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
				(e.source as JComponent).background = UiUtil.getBackgroundDivertColor(this@SidebarPane)
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