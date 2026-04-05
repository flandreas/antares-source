package io.antarescircuit.antares.view

import io.antarescircuit.antares.model.addressable.MemoryLibraryItem
import io.antarescircuit.antares.model.expression.BooleanExpressionLibraryItem
import io.antarescircuit.antares.model.expression.OpenBooleanExpressionAction
import io.antarescircuit.antares.model.fsm.FSMLibraryItem
import io.antarescircuit.antares.view.fsm.OpenFSMAction
import io.antarescircuit.antares.model.testcase.RunLibraryTestcasesAction
import io.antarescircuit.antares.model.truthtable.OpenTruthTableAction
import io.antarescircuit.antares.model.truthtable.TruthTableLibraryItem
import io.antarescircuit.antares.view.addressable.NewMemoryStorableAction
import io.antarescircuit.antares.view.addressable.OpenMemoryStorableAction
import io.antarescircuit.antares.view.expression.NewBooleanExpressionAction
import io.antarescircuit.antares.view.fsm.NewFSMAction
import io.antarescircuit.antares.view.net.tunnel.GlobalTunnelAction
import io.antarescircuit.antares.view.synthesis.CreateCircuitFromTruthTableAction
import io.antarescircuit.antares.view.truthtable.NewTruthTableAction
import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.graph.library.LibraryTreeViewActionsSwing
import io.antarescircuit.jabbah.graph.project.Project
import io.antarescircuit.jabbah.graph.ui.GraphDataViewController
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewType
import javax.swing.JPopupMenu
import javax.swing.tree.DefaultMutableTreeNode

class DigitalLibraryTreeViewActionsSwing(
	controller: LibraryTreeViewController,
	type: LibraryTreeViewType,
	application: Application
) : LibraryTreeViewActionsSwing(controller, type, application) {

	private val projectTruthTablePopupMenu = JPopupMenu()
	private val libraryTruthTablePopupMenu = JPopupMenu()

	private val projectExpressionPopupMenu = JPopupMenu()
	private val libraryExpressionPopupMenu = JPopupMenu()

	private val projectMemoryPopup = JPopupMenu()
	private val libraryMemoryPopup = JPopupMenu()

	private val projectFSMPopupMenu = JPopupMenu()
	private val libraryFSMPopupMenu = JPopupMenu()

	private val newTruthTableAction = register(NewTruthTableAction(controller))
	private val openTruthTableAction = register(OpenTruthTableAction(application.controller as GraphDataViewController, controller))
	private val createCircuitAction = register(CreateCircuitFromTruthTableAction(controller))
	private val newBooleanExpressionAction = register(NewBooleanExpressionAction(controller))
	private val openBooleanExpressionAction = register(OpenBooleanExpressionAction(application.controller as GraphDataViewController, controller))
	private val runTestsAction = register(RunLibraryTestcasesAction(controller))
	private val globalTunnelAction = register(GlobalTunnelAction())
	private val newMemoryStorableAction = register(NewMemoryStorableAction(controller))
	private val openMemoryStorableAction = register(OpenMemoryStorableAction(application.controller as GraphDataViewController, controller))
	private val newFSMAction = register(NewFSMAction(controller))
	private val openFSMAction = register(OpenFSMAction(application.controller as GraphDataViewController, controller))

	override fun fillMainProjectDirectoryCreateActions() {
		super.fillMainProjectDirectoryCreateActions()
		projectDirectoryPopupMenu.add(ActionWrapperSwing(newTruthTableAction))
		projectDirectoryPopupMenu.add(ActionWrapperSwing(newBooleanExpressionAction))
		projectDirectoryPopupMenu.add(ActionWrapperSwing(newMemoryStorableAction))
		projectDirectoryPopupMenu.add(ActionWrapperSwing(newFSMAction))
	}

	override fun fillMainProjectRootCreateActions() {
		super.fillMainProjectRootCreateActions()
		projectRootMenu.add(ActionWrapperSwing(newTruthTableAction))
		projectRootMenu.add(ActionWrapperSwing(newBooleanExpressionAction))
		projectRootMenu.add(ActionWrapperSwing(newMemoryStorableAction))
		projectRootMenu.add(ActionWrapperSwing(newFSMAction))
	}

	override fun fillMainProjectRootExecuteActions() {
		super.fillMainProjectRootExecuteActions()
		projectRootMenu.add(ActionWrapperSwing(runTestsAction))
	}

	override fun fillMainLibraryDirectoryCreateActions() {
		super.fillMainLibraryDirectoryCreateActions()
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(newTruthTableAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(newBooleanExpressionAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(newMemoryStorableAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(newFSMAction))
	}

	override fun fillMainLibraryRootCreateActions() {
		super.fillMainLibraryRootCreateActions()
		libraryRootMenu.add(ActionWrapperSwing(newTruthTableAction))
		libraryRootMenu.add(ActionWrapperSwing(newBooleanExpressionAction))
		libraryRootMenu.add(ActionWrapperSwing(newMemoryStorableAction))
		libraryRootMenu.add(ActionWrapperSwing(newFSMAction))
	}

	override fun fillMainLibraryRootExecuteActions() {
		super.fillMainLibraryRootExecuteActions()
		libraryRootMenu.add(ActionWrapperSwing(runTestsAction))
	}

	override fun fillMain() {
		super.fillMain()

		desktopPopupMenu.add(ActionWrapperSwing(globalTunnelAction))

		projectTruthTablePopupMenu.add(ActionWrapperSwing(openTruthTableAction))
		projectTruthTablePopupMenu.add(ActionWrapperSwing(deleteProjectItemAction))
		projectTruthTablePopupMenu.add(ActionWrapperSwing(createCircuitAction))

		libraryTruthTablePopupMenu.add(ActionWrapperSwing(openTruthTableAction))
		libraryTruthTablePopupMenu.add(ActionWrapperSwing(deleteLibraryItemAction))
		libraryTruthTablePopupMenu.add(ActionWrapperSwing(createCircuitAction))

		projectExpressionPopupMenu.add(ActionWrapperSwing(openBooleanExpressionAction))
		projectExpressionPopupMenu.add(ActionWrapperSwing(deleteProjectItemAction))

		libraryExpressionPopupMenu.add(ActionWrapperSwing(openBooleanExpressionAction))
		libraryExpressionPopupMenu.add(ActionWrapperSwing(deleteLibraryItemAction))

		projectMemoryPopup.add(ActionWrapperSwing(openMemoryStorableAction))
		projectMemoryPopup.add(ActionWrapperSwing(deleteProjectItemAction))

		libraryMemoryPopup.add(ActionWrapperSwing(openMemoryStorableAction))
		libraryMemoryPopup.add(ActionWrapperSwing(deleteLibraryItemAction))

		projectFSMPopupMenu.add(ActionWrapperSwing(openFSMAction))
		projectFSMPopupMenu.add(ActionWrapperSwing(deleteProjectItemAction))

		libraryFSMPopupMenu.add(ActionWrapperSwing(openFSMAction))
		libraryFSMPopupMenu.add(ActionWrapperSwing(deleteLibraryItemAction))

	}

	override fun getPopupMenu(treeNode: DefaultMutableTreeNode): JPopupMenu? {
		if (!isFilled) {
			fillPopupMenus()
		}
		if (treeNode.userObject is TruthTableLibraryItem) {
			return if ((treeNode.userObject as TruthTableLibraryItem).library is Project) {
				projectTruthTablePopupMenu
			} else {
				libraryTruthTablePopupMenu
			}
		}
		if (treeNode.userObject is BooleanExpressionLibraryItem) {
			return if ((treeNode.userObject as BooleanExpressionLibraryItem).library is Project) {
				projectExpressionPopupMenu
			} else {
				libraryExpressionPopupMenu
			}
		}
		if (treeNode.userObject is MemoryLibraryItem) {
			return if ((treeNode.userObject as MemoryLibraryItem).library is Project) {
				projectMemoryPopup
			} else {
				libraryMemoryPopup
			}
		}
		if (treeNode.userObject is FSMLibraryItem) {
			return if ((treeNode.userObject as FSMLibraryItem).library is Project) {
				projectFSMPopupMenu
			} else {
				libraryFSMPopupMenu
			}
		}
		return super.getPopupMenu(treeNode)
	}
}