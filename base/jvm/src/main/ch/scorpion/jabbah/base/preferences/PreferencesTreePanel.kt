package ch.scorpion.jabbah.base.preferences

import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.event.PropertyChangeSupport
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import java.awt.BorderLayout
import java.awt.Component
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
	private val messageDisplay: PreferencesMessageDisplay,
	private val origPreferences: Properties = BaseModule.properties,
	private val eventBus: EventBus = BaseModule.eventBus
) : JPanel() {

	companion object {
		private val LOG by logger(PreferencesTreePanel::class)
		const val PROP_CHANGED = "changed"
	}

	private val tree: JTree = JTree()

	private val contentHolder: JPanel = JPanel(BorderLayout())

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
		tree.setSelectionRow(0)
	}

	fun addPropertyChangeListener(l: PropertyChangeListener<Boolean>) {
		changeSupport.add(l)
	}

	fun removePropertyChangeListener(l: PropertyChangeListener<Boolean>) {
		changeSupport.remove(l)
	}

	fun applyChanges() {
		LOG.userTrail("Applying ${localPreferences.size} changes")

		if (localPreferences.needsRestart) {
			showRestartRequiredMessage()
		}

		localPreferences.flush()
		changed = false
		eventBus.post(PreferencesChangedEvent(origPreferences))
	}

	private fun showRestartRequiredMessage() {
		JOptionPane.showConfirmDialog(
			this@PreferencesTreePanel,
			Translations.getOptionalString("base.preferences.restart.msg"),
			Translations.getString("base.action.apply.name"),
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.INFORMATION_MESSAGE
		)
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

		val holderScroll = JScrollPane(contentHolder)
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
		contentHolder.removeAll()
		if (group != null) {
			val content = getPanelFor(group)
			content.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
			contentHolder.add(content, BorderLayout.CENTER)
			invalidate()
			revalidate()
			repaint()
		}
		messageDisplay.hideMessage()
	}

	private fun getPanelFor(group: PreferenceGroup): PreferencesPanel =
		panels.getOrPut(group) {
			val panel = PreferencesPanel(group, localPreferences, messageDisplay)
			panel.load()
			panel
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
	private inner class ChangeInterceptingPreferences : Preferences(origPreferences) {

		/** Accumulates all changed preferences.*/
		private val accumulator = Properties()

		var needsRestart: Boolean = false
			private set

		override fun getOptionalEntry(name: String): Entry? {
			return accumulator.getOptionalEntry(name) ?: super.getOptionalEntry(name)
		}

		override fun customize(preference: Preference, value: Any) {
			if (origPreferences.getOptional<Any>(preference.id) != value) {
				LOG.trace("customizing property '${preference.id}' with '$value'")
				changed = true
				needsRestart = needsRestart || preference.needsRestart
				accumulator.customize(preference.id, value)
			}
		}

		/** Forwards all accumulated preference changes to the original [Properties] and removes them locally.*/
		fun flush() {
			accumulator.copyTo(origPreferences)
			accumulator.clear()
			needsRestart = false
		}
	}
}