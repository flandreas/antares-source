package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.graph.project.*
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


	private val libraryOperationTarget: () -> Any = { LibraryModule.libraryHolder.library }
	private val projectOperationTarget: () -> Any? = { ProjectModule.projectHolder.project }

	private val expandAllAction = ExpandAllAction(view)
	private val collapseAllAction = CollapseAllAction(view)

	val addLibraryFolderAction = AddLibraryFolderAction(view, libraryOperationTarget)
	val deleteLibraryFolderAction = DeleteLibraryFolderAction(view, libraryOperationTarget)
	private val newLibraryGraphAction = NewGraphAction(view, libraryOperationTarget)
	private val libraryFolderPropertiesAction = LibraryFolderPropertiesAction(view, libraryOperationTarget)
	private val deleteLibraryElementAction = DeleteLibraryElementAction(view, libraryOperationTarget)
	private val duplicateLibraryGraphAction = DuplicateGraphAction(view, libraryOperationTarget)

	private val addProjectFolderAction = AddLibraryFolderAction(view, projectOperationTarget)
	private val deleteProjectFolderAction = DeleteLibraryFolderAction(view, projectOperationTarget)
	private val newProjectGraphAction = NewGraphAction(view, operationTarget = projectOperationTarget)
	private val projectFolderPropertiesAction = LibraryFolderPropertiesAction(view, projectOperationTarget)
	private val deleteProjectElementAction = DeleteLibraryElementAction(view, projectOperationTarget)
	private val defaultProjectElementAction = DefaultContainerLibraryElementAction(view, projectOperationTarget)
	private val duplicateProjectGraphAction = DuplicateGraphAction(view, projectOperationTarget)

	private val openContainerLibraryElementAction = OpenContainerLibraryElementAction(application, view)
	private val editLibraryAction = EditLibraryAction(view, application)

	private val desktopPopupMenu = JPopupMenu()
	private val projectDirectoryPopupMenu = JPopupMenu()
	private val projectContainerPopupMenu = JPopupMenu()
	private val projectRootMenu = JPopupMenu()
	private val projectBasePopupMenu = JPopupMenu()

	private val libraryDirectoryPopupMenu = JPopupMenu()
	private val libraryContainerPopupMenu = JPopupMenu()
	private val libraryRootMenu = JPopupMenu()
	private val libraryBasePopupMenu = JPopupMenu()

	init {
		fillPopupMenus(type)
	}

	fun getPopupMenu(treeNode: DefaultMutableTreeNode): JPopupMenu? {
		return when (treeNode.userObject) {
			is Project -> projectRootMenu
			is Library -> libraryRootMenu
			is LibraryFolder -> {
				if ((treeNode.userObject as LibraryFolder).library is Project) {
					projectDirectoryPopupMenu
				} else {
					libraryDirectoryPopupMenu
				}
			}
			is LibraryDirectory -> {
				if ((treeNode.userObject as LibraryDirectory).library is Project) {
					projectDirectoryPopupMenu
				} else {
					libraryDirectoryPopupMenu
				}
			}
			is ContainerLibraryElement -> {
				if ((treeNode.userObject as ContainerLibraryElement).library is Project) {
					projectContainerPopupMenu
				} else {
					libraryContainerPopupMenu
				}
			}
			is BaseLibraryElement -> {
				if ((treeNode.userObject as BaseLibraryElement).library is Project) {
					projectBasePopupMenu
				} else {
					libraryBasePopupMenu
				}
			}
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

		// Project actions

		projectDirectoryPopupMenu.add(ActionWrapperSwing(expandAllAction))
		projectDirectoryPopupMenu.add(ActionWrapperSwing(collapseAllAction))
		projectDirectoryPopupMenu.addSeparator()
		projectDirectoryPopupMenu.add(ActionWrapperSwing(newProjectGraphAction))
		projectDirectoryPopupMenu.add(ActionWrapperSwing(addProjectFolderAction))
		projectDirectoryPopupMenu.add(ActionWrapperSwing(deleteProjectFolderAction))
		projectDirectoryPopupMenu.add(ActionWrapperSwing(projectFolderPropertiesAction))

		projectRootMenu.add(ActionWrapperSwing(expandAllAction))
		projectRootMenu.add(ActionWrapperSwing(collapseAllAction))
		projectRootMenu.addSeparator()
		projectRootMenu.add(ActionWrapperSwing(newProjectGraphAction))
		projectRootMenu.add(ActionWrapperSwing(addProjectFolderAction))
		projectRootMenu.add(ActionWrapperSwing(CloseProjectAction()))
		projectRootMenu.addSeparator()
		projectRootMenu.add(ActionWrapperSwing(ProjectPropertiesAction()))

		projectContainerPopupMenu.add(ActionWrapperSwing(openContainerLibraryElementAction))
		projectContainerPopupMenu.add(ActionWrapperSwing(deleteProjectElementAction))
		projectContainerPopupMenu.add(JCheckBoxMenuItem(ActionWrapperSwing(defaultProjectElementAction)))
		projectContainerPopupMenu.add(ActionWrapperSwing(duplicateProjectGraphAction))

		projectBasePopupMenu.add(ActionWrapperSwing(deleteProjectElementAction))

		// Library actions

		libraryDirectoryPopupMenu.add(ActionWrapperSwing(expandAllAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(collapseAllAction))
		libraryDirectoryPopupMenu.addSeparator()
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(newLibraryGraphAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(addLibraryFolderAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(deleteLibraryFolderAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(libraryFolderPropertiesAction))

		libraryRootMenu.add(ActionWrapperSwing(expandAllAction))
		libraryRootMenu.add(ActionWrapperSwing(collapseAllAction))
		libraryRootMenu.addSeparator()
		libraryRootMenu.add(ActionWrapperSwing(addLibraryFolderAction))
		libraryRootMenu.add(ActionWrapperSwing(newLibraryGraphAction))
		libraryRootMenu.add(ActionWrapperSwing(deleteLibraryFolderAction))
		libraryRootMenu.addSeparator()
		libraryRootMenu.add(ActionWrapperSwing(editLibraryAction))
		libraryRootMenu.add(ActionWrapperSwing(LibraryPropertiesAction()))

		libraryContainerPopupMenu.add(ActionWrapperSwing(openContainerLibraryElementAction))
		libraryContainerPopupMenu.add(ActionWrapperSwing(deleteLibraryElementAction))
		libraryContainerPopupMenu.add(ActionWrapperSwing(duplicateLibraryGraphAction))

		libraryBasePopupMenu.add(ActionWrapperSwing(deleteLibraryElementAction))
	}

	private fun fillCompositionSource() {
		desktopPopupMenu.add(ActionWrapperSwing(expandAllAction))
		desktopPopupMenu.add(ActionWrapperSwing(collapseAllAction))

		projectRootMenu.add(ActionWrapperSwing(expandAllAction))
		projectRootMenu.add(ActionWrapperSwing(collapseAllAction))

		libraryDirectoryPopupMenu.add(ActionWrapperSwing(expandAllAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(collapseAllAction))

		libraryRootMenu.add(ActionWrapperSwing(expandAllAction))
		libraryRootMenu.add(ActionWrapperSwing(collapseAllAction))
	}

	private fun fillCompositionDestination() {
		desktopPopupMenu.add(ActionWrapperSwing(expandAllAction))
		desktopPopupMenu.add(ActionWrapperSwing(collapseAllAction))

		projectRootMenu.add(ActionWrapperSwing(expandAllAction))
		projectRootMenu.add(ActionWrapperSwing(collapseAllAction))
		projectRootMenu.add(ActionWrapperSwing(addLibraryFolderAction))

		libraryDirectoryPopupMenu.add(ActionWrapperSwing(expandAllAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(collapseAllAction))
		libraryDirectoryPopupMenu.addSeparator()
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(addLibraryFolderAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(deleteLibraryFolderAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(libraryFolderPropertiesAction))

		libraryRootMenu.add(ActionWrapperSwing(expandAllAction))
		libraryRootMenu.add(ActionWrapperSwing(collapseAllAction))
		libraryRootMenu.addSeparator()
		libraryRootMenu.add(ActionWrapperSwing(addLibraryFolderAction))
		libraryRootMenu.add(ActionWrapperSwing(deleteLibraryFolderAction))

		libraryContainerPopupMenu.add(ActionWrapperSwing(deleteLibraryElementAction))

		libraryBasePopupMenu.add(ActionWrapperSwing(deleteLibraryElementAction))
	}
}