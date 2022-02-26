package ch.scorpion.antares.view

import ch.scorpion.antares.model.truthtable.OpenTruthTableAction
import ch.scorpion.antares.model.truthtable.TruthTableLibraryItem
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

	private val newProjectTruthTableAction = NewTruthTableAction(controller, projectOperationTarget)
	private val newLibraryTruthTableAction = NewTruthTableAction(controller, libraryOperationTarget)

	private val openTruthTableAction = OpenTruthTableAction(application.controller as GraphDataViewController, controller)

	override fun fillMainProjectDirectoryPopupMenu() {
		super.fillMainProjectDirectoryPopupMenu()
		projectDirectoryPopupMenu.add(ActionWrapperSwing(newProjectTruthTableAction))
	}

	override fun fillMainProjectRootPopupMenu() {
		super.fillMainProjectRootPopupMenu()
		projectRootMenu.add(ActionWrapperSwing(newProjectTruthTableAction))
	}

	override fun fillMainLibraryDirectoryPopupMenu() {
		super.fillMainLibraryDirectoryPopupMenu()
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(newLibraryTruthTableAction))
	}

	override fun fillMainLibraryRootPopupMenu() {
		super.fillMainLibraryRootPopupMenu()
		libraryRootMenu.add(ActionWrapperSwing(newLibraryTruthTableAction))
	}

	override fun fillMain() {
		super.fillMain()

		projectTruthTablePopupMenu.add(ActionWrapperSwing(openTruthTableAction))
		libraryTruthTablePopupMenu.add(ActionWrapperSwing(openTruthTableAction))
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
		return super.getPopupMenu(treeNode)
	}
}