package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.graph.model.image.ImageLibraryElement
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.project.*
import ch.scorpion.jabbah.graph.ui.GraphDataViewController
import ch.scorpion.jabbah.graph.ui.MetaGraphEmbedAction
import ch.scorpion.jabbah.graph.ui.graphviewer.NewGraphViewerAction
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewActions
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewType
import javax.swing.JCheckBoxMenuItem
import javax.swing.JPopupMenu
import javax.swing.tree.DefaultMutableTreeNode

class LibraryTreeViewActionsParams(
	val controller: LibraryTreeViewController,
	val type: LibraryTreeViewType,
	val application: Application
)

open class LibraryTreeViewActionsSwing(
	controller: LibraryTreeViewController,
	private val type: LibraryTreeViewType,
	application: Application
) : LibraryTreeViewActions(controller, application) {

	protected val libraryOperationTarget: () -> Any? = { LibraryModule.libraryHolder.l }
	protected val projectOperationTarget: () -> Any? = { LibraryModule.libraryHolder.l }

	private val helpLibraryItemAction = register(HelpLibraryItemAction(controller))

	private val showLibraryMetaGraphHistoryAction = register(ShowMetaGraphHistoryAction(application.controller as GraphDataViewController, controller))

	private val addLibraryToDesktopAction = register(AddLibraryToDesktopAction(controller))
	private val removeLibraryAction = register(RemoveLibraryAction(controller))
	private val expandAllAction = register(ExpandAllAction(controller))
	private val collapseAllAction = register(CollapseAllAction(controller))
	private val exportMetaGraphAction = register(ExportMetaGraphAction(controller))
	private val newGraphViewerAction = register(NewGraphViewerAction(application.displayName, controller))
	private val embedMetaGraphAction = register(MetaGraphEmbedAction(controller))

	private val libraryPropertiesAction = register(LibraryPropertiesAction(controller))
	val addLibraryFolderAction = register(AddLibraryFolderAction(controller, libraryOperationTarget))
	val deleteLibraryFolderAction = register(DeleteLibraryFolderAction(controller, libraryOperationTarget))
	private val newLibraryGraphAction = register(NewGraphAction(controller))
	private val libraryFolderPropertiesAction = register(LibraryFolderPropertiesAction(controller, libraryOperationTarget))
	protected val deleteLibraryItemAction = register(DeleteLibraryItemAction(controller, libraryOperationTarget))
	private val duplicateLibraryGraphAction = register(DuplicateGraphAction(controller, libraryOperationTarget))
	private val importLibraryMetaGraphAction = register(ImportMetaGraphAction(controller, libraryOperationTarget))
	private val importImageAction = register(ImportImageAction(controller, libraryOperationTarget))
	private val renameLibraryMetaGraphAction = register(RenameMetaGraphAction(controller, libraryOperationTarget))
	private val closeLibraryAction = register(CloseLibraryAction())

	private val projectPropertiesAction = register(ProjectPropertiesAction(controller))
	private val addProjectFolderAction = register(AddLibraryFolderAction(controller, projectOperationTarget))
	private val deleteProjectFolderAction = register(DeleteLibraryFolderAction(controller, projectOperationTarget))
	private val newProjectGraphAction = register(NewGraphAction(controller))
	private val projectFolderPropertiesAction = register(LibraryFolderPropertiesAction(controller, projectOperationTarget))
	protected val deleteProjectItemAction = register(DeleteLibraryItemAction(controller, projectOperationTarget))
	private val defaultProjectElementAction = register(DefaultContainerLibraryElementAction(controller, projectOperationTarget))
	private val duplicateProjectGraphAction = register(DuplicateGraphAction(controller, projectOperationTarget))
	private val importProjectMetaGraphAction = register(ImportMetaGraphAction(controller, projectOperationTarget))
	private val renameProjectMetaGraphAction = register(RenameMetaGraphAction(controller, projectOperationTarget))

	private val closeProjectAction = register(CloseProjectAction())
	private val uploadProjectAction = if (GraphModuleJvm.supportWeb) {
		register(UploadProjectAction(controller, projectOperationTarget))
	} else {
		null
	}

	private val editLibraryAction = register(EditLibraryAction(controller, application))

	private val desktopPopupMenu = JPopupMenu()
	protected val projectDirectoryPopupMenu = JPopupMenu()
	private val projectContainerPopupMenu = JPopupMenu()
	protected val projectRootMenu = JPopupMenu()
	private val projectBasePopupMenu = JPopupMenu()

	protected val libraryDirectoryPopupMenu = JPopupMenu()
	private val libraryContainerPopupMenu = JPopupMenu()
	protected val libraryRootMenu = JPopupMenu()
	private val libraryBasePopupMenu = JPopupMenu()

	protected var isFilled = false
		private set

	open fun getPopupMenu(treeNode: DefaultMutableTreeNode): JPopupMenu? {
		if (!isFilled) {
			fillPopupMenus()
		}

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
			is ImageLibraryElement -> {
				if ((treeNode.userObject as ImageLibraryElement).library is Project) {
					projectBasePopupMenu
				} else {
					libraryBasePopupMenu
				}
			}
			is String -> desktopPopupMenu
			else -> null
		}
	}

	protected fun fillPopupMenus() {
		when (type) {
			LibraryTreeViewType.Main -> fillMain()
			LibraryTreeViewType.CompositionSource -> fillCompositionSource()
			LibraryTreeViewType.CompositionDestination -> fillCompositionDestination()
		}
		isFilled = true
	}

	private fun fillMainProjectDirectoryPopupMenu() {
		projectDirectoryPopupMenu.add(ActionWrapperSwing(expandAllAction))
		projectDirectoryPopupMenu.add(ActionWrapperSwing(collapseAllAction))
		projectDirectoryPopupMenu.addSeparator()
		fillMainProjectDirectoryCreateActions()
		projectDirectoryPopupMenu.addSeparator()
		projectDirectoryPopupMenu.add(ActionWrapperSwing(deleteProjectFolderAction))
		projectDirectoryPopupMenu.add(ActionWrapperSwing(importProjectMetaGraphAction))
		projectDirectoryPopupMenu.add(ActionWrapperSwing(importImageAction))
		projectDirectoryPopupMenu.addSeparator()
		projectDirectoryPopupMenu.add(ActionWrapperSwing(projectFolderPropertiesAction))
	}

	protected open fun fillMainProjectDirectoryCreateActions() {
		projectDirectoryPopupMenu.add(ActionWrapperSwing(newProjectGraphAction))
		projectDirectoryPopupMenu.add(ActionWrapperSwing(addProjectFolderAction))
	}

	private fun fillMainProjectRootPopupMenu() {
		projectRootMenu.add(ActionWrapperSwing(expandAllAction))
		projectRootMenu.add(ActionWrapperSwing(collapseAllAction))
		projectRootMenu.addSeparator()
		fillMainProjectRootCreateActions()
		projectRootMenu.addSeparator()
		fillMainProjectRootExecuteActions()
		projectRootMenu.addSeparator()
		projectRootMenu.add(ActionWrapperSwing(projectPropertiesAction))
		if (uploadProjectAction != null) {
			projectRootMenu.add(ActionWrapperSwing(uploadProjectAction))
		}
		projectRootMenu.add(ActionWrapperSwing(closeProjectAction))
	}

	protected open fun fillMainProjectRootCreateActions() {
		projectRootMenu.add(ActionWrapperSwing(newProjectGraphAction))
		projectRootMenu.add(ActionWrapperSwing(addProjectFolderAction))
	}

	protected open fun fillMainProjectRootExecuteActions() {
		projectRootMenu.add(ActionWrapperSwing(importProjectMetaGraphAction))
		projectRootMenu.add(ActionWrapperSwing(importImageAction))
	}

	private fun fillMainLibraryDirectoryPopupMenu() {
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(expandAllAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(collapseAllAction))
		libraryDirectoryPopupMenu.addSeparator()
		fillMainLibraryDirectoryCreateActions()
		libraryDirectoryPopupMenu.addSeparator()
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(deleteLibraryFolderAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(importLibraryMetaGraphAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(importImageAction))
		libraryDirectoryPopupMenu.addSeparator()
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(libraryFolderPropertiesAction))
	}

	protected open fun fillMainLibraryDirectoryCreateActions() {
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(newLibraryGraphAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(addLibraryFolderAction))
	}

	private fun fillMainLibraryRootPopupMenu() {
		libraryRootMenu.add(ActionWrapperSwing(expandAllAction))
		libraryRootMenu.add(ActionWrapperSwing(collapseAllAction))
		libraryRootMenu.addSeparator()
		fillMainLibraryRootCreateActions()
		libraryRootMenu.addSeparator()
		fillMainLibraryRootExecuteActions()
		libraryRootMenu.addSeparator()
		libraryRootMenu.add(ActionWrapperSwing(libraryPropertiesAction))
		libraryRootMenu.add(ActionWrapperSwing(closeLibraryAction))
	}

	protected open fun fillMainLibraryRootCreateActions() {
		libraryRootMenu.add(ActionWrapperSwing(addLibraryFolderAction))
		libraryRootMenu.add(ActionWrapperSwing(newLibraryGraphAction))
	}

	protected open fun fillMainLibraryRootExecuteActions() {
		libraryRootMenu.add(ActionWrapperSwing(deleteLibraryFolderAction))
		libraryRootMenu.add(ActionWrapperSwing(editLibraryAction))
		libraryRootMenu.add(ActionWrapperSwing(importLibraryMetaGraphAction))
		libraryRootMenu.add(ActionWrapperSwing(removeLibraryAction))
		libraryRootMenu.add(ActionWrapperSwing(importImageAction))
	}

	private fun fillMainProjectContainerPopupMenu() {
		projectContainerPopupMenu.add(ActionWrapperSwing(openContainerLibraryElementAction))
		projectContainerPopupMenu.add(ActionWrapperSwing(deleteProjectItemAction))
		projectContainerPopupMenu.add(JCheckBoxMenuItem(ActionWrapperSwing(defaultProjectElementAction)))
		projectContainerPopupMenu.add(ActionWrapperSwing(renameProjectMetaGraphAction))
		projectContainerPopupMenu.add(ActionWrapperSwing(duplicateProjectGraphAction))
		projectContainerPopupMenu.add(ActionWrapperSwing(exportMetaGraphAction))
		projectContainerPopupMenu.add(ActionWrapperSwing(newGraphViewerAction))
		projectContainerPopupMenu.add(ActionWrapperSwing(showLibraryMetaGraphHistoryAction))
		if (GraphModuleJvm.supportWeb) {
			projectContainerPopupMenu.add(ActionWrapperSwing(embedMetaGraphAction))
		}
	}

	private fun fillMainLibraryContainerPopupMenu() {
		libraryContainerPopupMenu.add(ActionWrapperSwing(openContainerLibraryElementAction))
		libraryContainerPopupMenu.add(ActionWrapperSwing(deleteLibraryItemAction))
		libraryContainerPopupMenu.add(ActionWrapperSwing(renameLibraryMetaGraphAction))
		libraryContainerPopupMenu.add(ActionWrapperSwing(duplicateLibraryGraphAction))
		libraryContainerPopupMenu.add(ActionWrapperSwing(exportMetaGraphAction))
		libraryContainerPopupMenu.add(ActionWrapperSwing(newGraphViewerAction))
		libraryContainerPopupMenu.add(ActionWrapperSwing(showLibraryMetaGraphHistoryAction))
	}

	protected open fun fillMain() {
		desktopPopupMenu.add(ActionWrapperSwing(addLibraryToDesktopAction))
		desktopPopupMenu.add(ActionWrapperSwing(expandAllAction))
		desktopPopupMenu.add(ActionWrapperSwing(collapseAllAction))

		// Project actions

		fillMainProjectDirectoryPopupMenu()
		fillMainProjectRootPopupMenu()
		fillMainProjectContainerPopupMenu()

		projectBasePopupMenu.add(ActionWrapperSwing(deleteProjectItemAction))

		// Library actions

		fillMainLibraryDirectoryPopupMenu()
		fillMainLibraryRootPopupMenu()
		fillMainLibraryContainerPopupMenu()

		libraryBasePopupMenu.add(ActionWrapperSwing(deleteLibraryItemAction))
		libraryBasePopupMenu.add(ActionWrapperSwing(helpLibraryItemAction))
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

		libraryContainerPopupMenu.add(ActionWrapperSwing(deleteLibraryItemAction))

		libraryBasePopupMenu.add(ActionWrapperSwing(deleteLibraryItemAction))
	}
}