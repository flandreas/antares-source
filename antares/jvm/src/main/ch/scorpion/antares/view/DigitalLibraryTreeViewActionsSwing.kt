package ch.scorpion.antares.view

import ch.scorpion.antares.model.expression.BooleanExpressionLibraryItem
import ch.scorpion.antares.model.expression.OpenBooleanExpressionAction
import ch.scorpion.antares.model.testcase.RunLibraryTestcasesAction
import ch.scorpion.antares.model.truthtable.OpenTruthTableAction
import ch.scorpion.antares.model.truthtable.TruthTableLibraryItem
import ch.scorpion.antares.view.expression.NewBooleanExpressionAction
import ch.scorpion.antares.view.synthesis.CreateCircuitFromTruthTableAction
import ch.scorpion.antares.view.truthtable.NewTruthTableAction
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.graph.library.LibraryTreeViewActionsSwing
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.ui.GraphDataViewController
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewType
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

	private val newTruthTableAction = register(NewTruthTableAction(controller))
	private val openTruthTableAction = register(OpenTruthTableAction(application.controller as GraphDataViewController, controller))
	private val createCircuitAction = register(CreateCircuitFromTruthTableAction(controller))
	private val newBooleanExpressionAction = register(NewBooleanExpressionAction(controller))
	private val openBooleanExpressionAction = register(OpenBooleanExpressionAction(application.controller as GraphDataViewController, controller))
	private val runTestsAction = register(RunLibraryTestcasesAction(controller))

	override fun fillMainProjectDirectoryCreateActions() {
		super.fillMainProjectDirectoryCreateActions()
		projectDirectoryPopupMenu.add(ActionWrapperSwing(newTruthTableAction))
		projectDirectoryPopupMenu.add(ActionWrapperSwing(newBooleanExpressionAction))
	}

	override fun fillMainProjectRootCreateActions() {
		super.fillMainProjectRootCreateActions()
		projectRootMenu.add(ActionWrapperSwing(newTruthTableAction))
		projectRootMenu.add(ActionWrapperSwing(newBooleanExpressionAction))
	}

	override fun fillMainProjectRootExecuteActions() {
		super.fillMainProjectRootExecuteActions()
		projectRootMenu.add(ActionWrapperSwing(runTestsAction))
	}

	override fun fillMainLibraryDirectoryCreateActions() {
		super.fillMainLibraryDirectoryCreateActions()
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(newTruthTableAction))
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(newBooleanExpressionAction))
	}

	override fun fillMainLibraryRootCreateActions() {
		super.fillMainLibraryRootCreateActions()
		libraryRootMenu.add(ActionWrapperSwing(newTruthTableAction))
		libraryRootMenu.add(ActionWrapperSwing(newBooleanExpressionAction))
	}

	override fun fillMain() {
		super.fillMain()

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
		return super.getPopupMenu(treeNode)
	}
}