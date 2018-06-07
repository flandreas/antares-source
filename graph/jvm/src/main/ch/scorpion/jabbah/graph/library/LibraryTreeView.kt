package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.ApplicationModeEvent
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.project.*
import ch.scorpion.jabbah.graph.ui.ContainerLibraryElementIcon
import java.awt.Component
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.InputEvent
import javax.swing.*
import javax.swing.tree.*

/**
 * Displays the {@link Library} as a tree.
 *
 * Instances of this class post the following events on the {@link T2Bus}:
 * - A [LibrarySelectionChangedEvent] when the user selects a tree item
 * - A [OpenContainerLibraryElementRequest] when the user double clicks on a [ContainerLibraryElement]
 */
class LibraryTreeView(
    private val eventBus: EventBus = BaseModule.eventBus,
    val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
    val projectHolder: ProjectHolder = ProjectModule.projectHolder
) : JTree(createLibraryTreeModel(libraryHolder.library, projectHolder.project)) {

    private val directoryPopupMenu = JPopupMenu()

    private val containerPopupMenu = JPopupMenu()

	private var currentSavable: Savable? = null
		set(value) {
			if (field != value) {
				field = value
				invalidate()
				repaint()
			}
		}

    init {
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        selectionModel.addTreeSelectionListener { setupPopupMenu(it.newLeadSelectionPath) }

        transferHandler = LibraryElementTransferHandler()

        setRowHeight(24)
	    isRootVisible = false
        setCellRenderer(Renderer())
        addTreeSelectionListener({ eventBus.post(LibrarySelectionChangedEvent(this)) })
        addMouseListener(DoubleClickListener())

        dragEnabled = true
        dropMode = DropMode.ON

        // TODO Update TreeModel selectively
        eventBus.register(LibraryItemAddedEvent::class, { updateLibrary() })
        eventBus.register(LibraryItemRemovedEvent::class, { updateLibrary() })
        eventBus.register(LibraryItemUpdatedEvent::class, { updateLibrary() })
	    eventBus.register(ProjectEvent::class, { updateLibrary() })

        eventBus.register(ApplicationModeEvent::class, { dragEnabled = it.applicationMode === ApplicationMode.EDIT })
	    eventBus.register(CurrentSavableEvent::class, { handle(it) })

        directoryPopupMenu.add(ActionWrapperSwing(AddLibraryFolderAction()))
        directoryPopupMenu.add(ActionWrapperSwing(NewGraphAction()))
        directoryPopupMenu.add(ActionWrapperSwing(AddGraphToLibraryAction()))

        containerPopupMenu.add(ActionWrapperSwing(DeleteContainerLibraryElementAction()))

	    expandRow(0)
    }

    companion object {

        fun createLibraryTreeModel(library: Library, project: Project?): TreeModel {
	        val root = DefaultMutableTreeNode()

	        if (project != null) {
		        val projectNode = DefaultMutableTreeNode(project)
		        addItems(projectNode, project)
		        root.add(projectNode)
	        }

	        val libraryNode = DefaultMutableTreeNode(library)
            addItems(libraryNode, library)
	        root.add(libraryNode)

            return DefaultTreeModel(root)
        }

        private fun addItems(parent: DefaultMutableTreeNode, directory: LibraryDirectory) {
            // TODO Sort
            for (item in directory.getItems()) {
                val node = DefaultMutableTreeNode(item)
                parent.add(node)
                if (item is LibraryDirectory) {
                    addItems(node, item)
                }
            }
        }
    }


    /** ---- [LibraryTreeView] */

    fun getSelectedItem(): LibraryItem? {
        val path = selectionPath ?: return null
        return (path.lastPathComponent as DefaultMutableTreeNode).userObject as LibraryItem
    }

    private fun updateLibrary() {
        SwingUtilities.invokeLater {
            model = createLibraryTreeModel(libraryHolder.library, projectHolder.project)
        }
    }

    private inner class DoubleClickListener : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            if (e.clickCount == 2 && getSelectedItem() is ContainerLibraryElement) {
                eventBus.post(OpenContainerLibraryElementRequest(getSelectedItem() as ContainerLibraryElement))
            }
        }
    }

    /** Setup the popup menu according to the currently selected [TreeNode]' user object.*/
    private fun setupPopupMenu(newSelectionPath: TreePath?) {
        if (newSelectionPath == null) {
            componentPopupMenu = null
            return
        }
        componentPopupMenu = when ((newSelectionPath.lastPathComponent as DefaultMutableTreeNode).userObject) {
            is LibraryFolder -> directoryPopupMenu
            is LibraryDirectory -> directoryPopupMenu
            else -> containerPopupMenu
        }
    }

	private fun handle(event: CurrentSavableEvent) {
		if (event.savable is LibrarySavable || event.savable is ProjectSavable) {
			currentSavable = event.savable
		} else {
			currentSavable = null
		}
	}

    private inner class Renderer : DefaultTreeCellRenderer() {

        private val iconCache: MutableMap<String, Icon> = mutableMapOf()
        private val containerLibElemIcon = ContainerLibraryElementIcon()
	    private val currentContainerLibElemIcon = ContainerLibraryElementIcon(current = true)

        override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
            val component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as JLabel
	        if ((value as DefaultMutableTreeNode).userObject != null) {
		        val iconPath = (value.userObject as LibraryItem).iconPath
		        component.font = this@LibraryTreeView.font
		        if (StringUtils.isNotEmpty(iconPath)) {
			        component.icon = getIcon(iconPath!!)
		        } else if (value.userObject is ContainerLibraryElement) {
			        component.icon = containerLibElemIcon
			        if (
				        currentSavable is LibrarySavable && (currentSavable as LibrarySavable).element == value.userObject ||
				        currentSavable is ProjectSavable && (currentSavable as ProjectSavable).element == value.userObject
			        ) {
				        component.icon = currentContainerLibElemIcon
			        }
		        }
	        }
            return component
        }

        private fun getIcon(iconPath: String): Icon {
            return iconCache.getOrPut(iconPath, { ImageIcon(LibraryTreeView::class.java.getResource(iconPath)) })
        }
    }
}

/**
 * Posted on a [LibraryTreeView]'s [EventBus] if the current selection in this [LibraryTreeView] has changed.
 */
data class LibrarySelectionChangedEvent(val libraryTreeView: LibraryTreeView)

/**
 * Represents a request to open the [MetaGraph] of a [ContainerLibraryElement].
 * This request is posted by [LibraryTreeView]. It is up to higher level application classes to decide how the
 * [MetaGraph] of the selected [ContainerLibraryElement] is to be presented to the user.
 *
 * @property element the [ContainerLibraryElement] whose [MetaGraph] is to be opened
 * @property inputEvent the [InputEvent] with which the user tried to open the [ContainerLibraryElement]. Can be used to
 * implement different application level UI strategies based e.g. on the pressed key.
 */
data class OpenContainerLibraryElementRequest(val element: ContainerLibraryElement)