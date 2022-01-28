package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.graph.project.*
import ch.scorpion.jabbah.graph.ui.graphviewer.NewGraphViewerAction
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewActions
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewType
import javax.swing.JCheckBoxMenuItem
import javax.swing.JPopupMenu
import javax.swing.tree.DefaultMutableTreeNode

class LibraryTreeViewActionsSwing(
	controller: LibraryTreeViewController,
	type: LibraryTreeViewType,
	application: Application
) : LibraryTreeViewActions(controller, application) {

	private val libraryOperationTarget: () -> Any = { LibraryModule.libraryHolder.library }
	private val projectOperationTarget: () -> Any? = { ProjectModule.projectHolder.project }

	private val expandAllAction = ExpandAllAction(controller)
	private val collapseAllAction = CollapseAllAction(controller)
	private val exportMetaGraphAction = ExportMetaGraphAction(controller)
	private val newGraphViewerAction = NewGraphViewerAction(application.displayName, controller)

	val addLibraryFolderAction = AddLibraryFolderAction(controller, libraryOperationTarget)
	val deleteLibraryFolderAction = DeleteLibraryFolderAction(controller, libraryOperationTarget)
	private val newLibraryGraphAction = NewGraphAction(controller, libraryOperationTarget)
	private val libraryFolderPropertiesAction = LibraryFolderPropertiesAction(controller, libraryOperationTarget)
	private val deleteLibraryElementAction = DeleteLibraryElementAction(controller, libraryOperationTarget)
	private val duplicateLibraryGraphAction = DuplicateGraphAction(controller, libraryOperationTarget)
	private val importLibraryMetaGraphAction = ImportMetaGraphAction(controller, libraryOperationTarget)
	private val renameLibraryMetaGraphAction = RenameMetaGraphAction(controller, libraryOperationTarget)

	private val addProjectFolderAction = AddLibraryFolderAction(controller, projectOperationTarget)
	private val deleteProjectFolderAction = DeleteLibraryFolderAction(controller, projectOperationTarget)
	private val newProjectGraphAction = NewGraphAction(controller, operationTarget = projectOperationTarget)
	private val projectFolderPropertiesAction = LibraryFolderPropertiesAction(controller, projectOperationTarget)
	private val deleteProjectElementAction = DeleteLibraryElementAction(controller, projectOperationTarget)
	private val defaultProjectElementAction = DefaultContainerLibraryElementAction(controller, projectOperationTarget)
	private val duplicateProjectGraphAction = DuplicateGraphAction(controller, projectOperationTarget)
	private val importProjectMetaGraphAction = ImportMetaGraphAction(controller, projectOperationTarget)
	private val renameProjectMetaGraphAction = RenameMetaGraphAction(controller, projectOperationTarget)

	private val uploadProjectAction = UploadProjectAction(controller, projectOperationTarget)

	private val editLibraryAction = EditLibraryAction(controller, application)

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
		projectDirectoryPopupMenu.add(ActionWrapperSwing(importProjectMetaGraphAction))
		projectDirectoryPopupMenu.addSeparator()
		projectDirectoryPopupMenu.add(ActionWrapperSwing(projectFolderPropertiesAction))

		projectRootMenu.add(ActionWrapperSwing(expandAllAction))
		projectRootMenu.add(ActionWrapperSwing(collapseAllAction))
		projectRootMenu.addSeparator()
		projectRootMenu.add(ActionWrapperSwing(newProjectGraphAction))
		projectRootMenu.add(ActionWrapperSwing(addProjectFolderAction))
		projectRootMenu.add(ActionWrapperSwing(importProjectMetaGraphAction))
		projectRootMenu.addSeparator()
		projectRootMenu.add(ActionWrapperSwing(ProjectPropertiesAction()))
		if (EditAuthModule.userHolder.user.isDeveloper) {
			projectRootMenu.add(ActionWrapperSwing(uploadProjectAction))
		}
		projectRootMenu.add(ActionWrapperSwing(CloseProjectAction()))

		projectContainerPopupMenu.add(ActionWrapperSwing(openContainerLibraryElementAction))
		projectContainerPopupMenu.add(ActionWrapperSwing(deleteProjectElementAction))
		projectContainerPopupMenu.add(JCheckBoxMenuItem(ActionWrapperSwing(defaultProjectElementAction)))
		projectContainerPopupMenu.add(ActionWrapperSwing(renameProjectMetaGraphAction))
		projectContainerPopupMenu.add(ActionWrapperSwing(duplicateProjectGraphAction))
		projectContainerPopupMenu.add(ActionWrapperSwing(exportMetaGraphAction))
		projectContainerPopupMenu.add(ActionWrapperSwing(newGraphViewerAction))

		projectBasePopupMenu.add(ActionWrapperSwing(deleteProjectElementAction))

		// Library actions

		libraryDirectoryPopupMenu.add(ActionWrapperSwing(expandAllAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(collapseAllAction))
		libraryDirectoryPopupMenu.addSeparator()
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(newLibraryGraphAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(addLibraryFolderAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(deleteLibraryFolderAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(importLibraryMetaGraphAction))
		libraryDirectoryPopupMenu.addSeparator()
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(libraryFolderPropertiesAction))

		libraryRootMenu.add(ActionWrapperSwing(expandAllAction))
		libraryRootMenu.add(ActionWrapperSwing(collapseAllAction))
		libraryRootMenu.addSeparator()
		libraryRootMenu.add(ActionWrapperSwing(addLibraryFolderAction))
		libraryRootMenu.add(ActionWrapperSwing(newLibraryGraphAction))
		libraryRootMenu.add(ActionWrapperSwing(deleteLibraryFolderAction))
		libraryRootMenu.add(ActionWrapperSwing(editLibraryAction))
		libraryRootMenu.add(ActionWrapperSwing(importLibraryMetaGraphAction))
		libraryRootMenu.addSeparator()
		libraryRootMenu.add(ActionWrapperSwing(LibraryPropertiesAction()))

		libraryContainerPopupMenu.add(ActionWrapperSwing(openContainerLibraryElementAction))
		libraryContainerPopupMenu.add(ActionWrapperSwing(deleteLibraryElementAction))
		libraryContainerPopupMenu.add(ActionWrapperSwing(renameLibraryMetaGraphAction))
		libraryContainerPopupMenu.add(ActionWrapperSwing(duplicateLibraryGraphAction))
		libraryContainerPopupMenu.add(ActionWrapperSwing(exportMetaGraphAction))
		libraryContainerPopupMenu.add(ActionWrapperSwing(newGraphViewerAction))

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