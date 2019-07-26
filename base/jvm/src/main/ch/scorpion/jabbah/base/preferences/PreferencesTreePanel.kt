package ch.scorpion.jabbah.base.preferences

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.module.BaseModule
import java.awt.*
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer

/**
 * A [JPanel] that displays the tree of [PreferenceGroup]s on the left and the [Preference]s of the currently
 * selected [PreferenceGroup] in the [PreferencesPanel] on the right.
 *
 * @property origPreferences the [Properties] object that stores the editable preferences
 */
class PreferencesTreePanel(
	private val origPreferences: Properties = BaseModule.properties,
	private val eventBus: EventBus = BaseModule.eventBus
) : JPanel() {

	companion object {
		private val LOG by logger(PreferencesTreePanel::class)
		const val PROP_CHANGED = "changed"
	}

	private val tree: JTree = JTree()

	private val holder: JPanel = JPanel(BorderLayout())

	/** Maps a [PreferenceGroup] to its lazily created [PreferencesPanel].*/
	private val panels = mutableMapOf<PreferenceGroup, PreferencesPanel>()

	private val selectedGroup: PreferenceGroup?
		get() {
			val path = tree.selectionPath ?: return null
			return (path.lastPathComponent as DefaultMutableTreeNode).userObject as PreferenceGroup?
		}

	private val localPreferences = ChangeInterceptingPreferences()

	private val changeSupport = PropertyChangeSupport<Boolean>(this)

	/** Determines whether the uses has changed a [Preference].*/
	var changed: Boolean = false
		private set(value) {
			if (field != value) {
				val oldValue = field
				field = value
				changeSupport.fire(PROP_CHANGED, oldValue, field)
			}
		}

	init {
		buildUI()
	}

	fun addPropertyChangeListener(l: PropertyChangeListener<Boolean>) {
		changeSupport.add(l)
	}

	fun removePropertyChangeListener(l: PropertyChangeListener<Boolean>) {
		changeSupport.remove(l)
	}

	fun applyChanges() {
		LOG.debug("applying ${localPreferences.size} changes")
		localPreferences.flush()
		changed = false
		eventBus.post(PreferencesChangedEvent(origPreferences))
	}

	private fun buildUI() {
		layout = BorderLayout()

		tree.cellRenderer = TreeCellRenderer()
		tree.model = PreferencesTreeModelBuilder().build()
		tree.isRootVisible = false
		tree.showsRootHandles = true
		tree.addTreeSelectionListener { updateGroup() }
		tree.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

		val treeScroll = JScrollPane(tree)
		treeScroll.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
		treeScroll.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED

		val holderScroll = JScrollPane(holder)
		treeScroll.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
		treeScroll.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED

		val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
		splitPane.dividerLocation = 200
		splitPane.add(treeScroll)
		splitPane.add(holderScroll)

		add(splitPane, BorderLayout.CENTER)
	}

	private fun updateGroup() {
		val group = selectedGroup
		holder.removeAll()
		if (group != null) {
			val content = getPanelFor(group)
			content.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
			holder.add(content, BorderLayout.CENTER)
			invalidate()
			revalidate()
			repaint()
		}
	}

	private fun getPanelFor(group: PreferenceGroup): PreferencesPanel {
		return panels.getOrPut(group) {
			val panel = PreferencesPanel(group, localPreferences)
			panel.load()
			panel
		}
	}

	private class TreeCellRenderer : DefaultTreeCellRenderer() {
		override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
			val renderer = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as JLabel
			renderer.icon = null
			return renderer
		}
	}

	/**
	 * Intercepts all changes of [Preference]s, stores them locally, and forwards them to the original [Properties] store
	 * if requested.
	 */
	private inner class ChangeInterceptingPreferences : PropertiesProxy(origPreferences) {

		/** Accumulates all changed preferences.*/
		private val accumulator = Properties()

		override fun getOptionalEntry(name: String): Entry? {
			return accumulator.getOptionalEntry(name) ?: super.getOptionalEntry(name)
		}

		override fun customize(name: String, value: Any) {
			LOG.debug("customizing property '$name' with '$value'")
			accumulator.customize(name, value)
			changed = true
		}

		/** Forwards all accumulated preference changes to the original [Properties] and removes them locally.*/
		fun flush() {
			accumulator.copyTo(origPreferences)
			accumulator.clear()
		}
	}
}

/** An action for showing [PreferencesDialogPanel] within a dialog.*/
class PreferencesAction : AbstractAction("base.preferences.action") {
	override fun execute(event: ActionEvent) {
		PreferencesDialogPanel.showAsDialog()
	}
}

class PreferencesDialogPanel(
	private val treePanel: PreferencesTreePanel = PreferencesTreePanel(),
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {

		private val LOG by logger(PreferencesDialogPanel::class)

		fun showAsDialog(
			parent: Frame = Frame.getFrames()[0],
			treePanel: PreferencesTreePanel = PreferencesTreePanel()
		) {
			val dialog = JDialog(parent, true)
			dialog.title = Translations.getString("base.preferences.title.name")
			dialog.contentPane.add(PreferencesDialogPanel(treePanel) { dialog.dispose() })
			dialog.preferredSize = Dimension(800, 600)
			dialog.pack()
			dialog.setLocationRelativeTo(parent)
			dialog.isVisible = true
		}
	}

	private val closeAction = object : AbstractAction("base.action.close") {
		override fun execute(event: ActionEvent) {
			LOG.debug("closeAction")
			closeHandler.invoke()
		}
	}

	private val applyAction = object : AbstractAction("base.action.apply") {
		override fun execute(event: ActionEvent) {
			treePanel.applyChanges()
		}
	}

	init {
		applyAction.enabled = false
		treePanel.addPropertyChangeListener(object : PropertyChangeListener<Boolean> {
			override fun propertyChanged(e: PropertyChangeEvent<Boolean>) {
				applyAction.enabled = treePanel.changed
			}
		})
		buildUI()
	}

	private fun buildUI() {
		layout = BorderLayout()
		add(treePanel, BorderLayout.CENTER)
		add(buildButtonPanel(), BorderLayout.SOUTH)
	}

	private fun buildButtonPanel(): JPanel {
		val panel = JPanel(FlowLayout(FlowLayout.RIGHT))
		panel.add(JButton(ActionWrapperSwing(applyAction)))
		panel.add(JButton(ActionWrapperSwing(closeAction)))
		return panel
	}
}