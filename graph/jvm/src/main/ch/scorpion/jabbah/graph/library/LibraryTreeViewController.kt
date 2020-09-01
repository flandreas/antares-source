package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.graph.project.CloseProjectAction
import ch.scorpion.jabbah.graph.project.DefaultContainerLibraryElementAction
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.project.ProjectPropertiesAction
import javax.swing.JCheckBoxMenuItem
import javax.swing.JPopupMenu
import javax.swing.tree.DefaultMutableTreeNode

enum class LibraryTreeViewType {
	Main,
	CompositionSource,
	CompositionDestination
}

class LibraryTreeViewController(
	private val view: LibraryTreeView,
	type: LibraryTreeViewType,
	application: Application
) {

	val addLibraryFolderAction = AddLibraryFolderAction(view)
	val deleteLibraryFolderAction = DeleteLibraryFolderAction(view)

	private val expandAllAction = ExpandAllAction(view)
	private val collapseAllAction = CollapseAllAction(view)
	private val newGraphAction = NewGraphAction(view)
	private val openContainerLibraryElementAction = OpenContainerLibraryElementAction(application, view)
	private val deleteLibraryElementAction = DeleteLibraryElementAction(view)
	private val editLibraryAction = EditLibraryAction(view, application)
	private val libraryFolderPropertiesAction = LibraryFolderPropertiesAction(view)

	private val desktopPopupMenu = JPopupMenu()
	private val directoryPopupMenu = JPopupMenu()
	private val projectContainerPopupMenu = JPopupMenu()
	private val projectRootMenu = JPopupMenu()
	private val libraryContainerPopupMenu = JPopupMenu()
	private val libraryRootMenu = JPopupMenu()
	private val basePopupMenu = JPopupMenu()

	init {
		fillPopupMenus(type)
	}

	fun getPopupMenu(treeNode: DefaultMutableTreeNode): JPopupMenu? {
		return when (treeNode.userObject) {
			is Project -> projectRootMenu
			is Library -> libraryRootMenu
			is LibraryFolder -> directoryPopupMenu
			is LibraryDirectory -> directoryPopupMenu
			is ContainerLibraryElement -> {
				if ((treeNode.userObject as ContainerLibraryElement).library is Project) {
					projectContainerPopupMenu
				} else {
					libraryContainerPopupMenu
				}
			}
			is BaseLibraryElement -> basePopupMenu
			is String -> desktopPopupMenu
			else -> null
		}
	}

	private fun fillPopupMenus(type: LibraryTreeViewType) {
		when(type) {
			LibraryTreeViewType.Main -> fillMain()
			LibraryTreeViewType.CompositionSource -> fillCompositionSource()
			LibraryTreeViewType.CompositionDestination -> fillCompositionDestination()
		}
	}

	private fun fillMain() {
		desktopPopupMenu.add(ActionWrapperSwing(expandAllAction))
		desktopPopupMenu.add(ActionWrapperSwing(collapseAllAction))

		projectRootMenu.add(ActionWrapperSwing(expandAllAction))
		projectRootMenu.add(ActionWrapperSwing(collapseAllAction))
		projectRootMenu.addSeparator()
		projectRootMenu.add(ActionWrapperSwing(newGraphAction))
		projectRootMenu.add(ActionWrapperSwing(addLibraryFolderAction))
		projectRootMenu.add(ActionWrapperSwing(CloseProjectAction()))
		projectRootMenu.addSeparator()
		projectRootMenu.add(ActionWrapperSwing(ProjectPropertiesAction()))

		directoryPopupMenu.add(ActionWrapperSwing(expandAllAction))
		directoryPopupMenu.add(ActionWrapperSwing(collapseAllAction))
		directoryPopupMenu.addSeparator()
		directoryPopupMenu.add(ActionWrapperSwing(newGraphAction))
		directoryPopupMenu.add(ActionWrapperSwing(addLibraryFolderAction))
		directoryPopupMenu.add(ActionWrapperSwing(deleteLibraryFolderAction))
		directoryPopupMenu.add(ActionWrapperSwing(libraryFolderPropertiesAction))

		libraryRootMenu.add(ActionWrapperSwing(expandAllAction))
		libraryRootMenu.add(ActionWrapperSwing(collapseAllAction))
		libraryRootMenu.addSeparator()
		libraryRootMenu.add(ActionWrapperSwing(addLibraryFolderAction))
		libraryRootMenu.add(ActionWrapperSwing(newGraphAction))
		libraryRootMenu.add(ActionWrapperSwing(deleteLibraryFolderAction))
		libraryRootMenu.addSeparator()
		libraryRootMenu.add(ActionWrapperSwing(editLibraryAction))
		libraryRootMenu.add(ActionWrapperSwing(LibraryPropertiesAction()))

		projectContainerPopupMenu.add(ActionWrapperSwing(openContainerLibraryElementAction))
		projectContainerPopupMenu.add(ActionWrapperSwing(deleteLibraryElementAction))
		projectContainerPopupMenu.add(JCheckBoxMenuItem(ActionWrapperSwing(DefaultContainerLibraryElementAction(view))))
		projectContainerPopupMenu.add(ActionWrapperSwing(DuplicateGraphAction(view)))

		libraryContainerPopupMenu.add(ActionWrapperSwing(openContainerLibraryElementAction))
		libraryContainerPopupMenu.add(ActionWrapperSwing(deleteLibraryElementAction))
		libraryContainerPopupMenu.add(ActionWrapperSwing(DuplicateGraphAction(view)))

		basePopupMenu.add(ActionWrapperSwing(deleteLibraryElementAction))
	}

	private fun fillCompositionSource() {
		desktopPopupMenu.add(ActionWrapperSwing(expandAllAction))
		desktopPopupMenu.add(ActionWrapperSwing(collapseAllAction))

		projectRootMenu.add(ActionWrapperSwing(expandAllAction))
		projectRootMenu.add(ActionWrapperSwing(collapseAllAction))

		directoryPopupMenu.add(ActionWrapperSwing(expandAllAction))
		directoryPopupMenu.add(ActionWrapperSwing(collapseAllAction))

		libraryRootMenu.add(ActionWrapperSwing(expandAllAction))
		libraryRootMenu.add(ActionWrapperSwing(collapseAllAction))
	}

	private fun fillCompositionDestination() {
		desktopPopupMenu.add(ActionWrapperSwing(expandAllAction))
		desktopPopupMenu.add(ActionWrapperSwing(collapseAllAction))

		projectRootMenu.add(ActionWrapperSwing(expandAllAction))
		projectRootMenu.add(ActionWrapperSwing(collapseAllAction))
		projectRootMenu.add(ActionWrapperSwing(addLibraryFolderAction))

		directoryPopupMenu.add(ActionWrapperSwing(expandAllAction))
		directoryPopupMenu.add(ActionWrapperSwing(collapseAllAction))
		directoryPopupMenu.addSeparator()
		directoryPopupMenu.add(ActionWrapperSwing(addLibraryFolderAction))
		directoryPopupMenu.add(ActionWrapperSwing(deleteLibraryFolderAction))
		directoryPopupMenu.add(ActionWrapperSwing(libraryFolderPropertiesAction))

		libraryRootMenu.add(ActionWrapperSwing(expandAllAction))
		libraryRootMenu.add(ActionWrapperSwing(collapseAllAction))
		libraryRootMenu.addSeparator()
		libraryRootMenu.add(ActionWrapperSwing(addLibraryFolderAction))
		libraryRootMenu.add(ActionWrapperSwing(deleteLibraryFolderAction))

		libraryContainerPopupMenu.add(ActionWrapperSwing(deleteLibraryElementAction))

		basePopupMenu.add(ActionWrapperSwing(deleteLibraryElementAction))
	}
}