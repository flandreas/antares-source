package ch.scorpion.antares.view

import ch.scorpion.antares.model.truthtable.OpenTruthTableAction
import ch.scorpion.antares.model.truthtable.TruthTableLibraryItem
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

	private val newTruthTableAction = NewTruthTableAction(controller)
	private val openTruthTableAction = OpenTruthTableAction(application.controller as GraphDataViewController, controller)
	private val createCircuitAction = CreateCircuitFromTruthTableAction(controller)

	override fun fillMainProjectDirectoryPopupMenu() {
		super.fillMainProjectDirectoryPopupMenu()
		projectDirectoryPopupMenu.add(ActionWrapperSwing(newTruthTableAction))
	}

	override fun fillMainProjectRootPopupMenu() {
		super.fillMainProjectRootPopupMenu()
		projectRootMenu.add(ActionWrapperSwing(newTruthTableAction))
	}

	override fun fillMainLibraryDirectoryPopupMenu() {
		super.fillMainLibraryDirectoryPopupMenu()
		libraryDirectoryPopupMenu.add(ActionWrapperSwing(newTruthTableAction))
	}

	override fun fillMainLibraryRootPopupMenu() {
		super.fillMainLibraryRootPopupMenu()
		libraryRootMenu.add(ActionWrapperSwing(newTruthTableAction))
	}

	override fun fillMain() {
		super.fillMain()

		projectTruthTablePopupMenu.add(ActionWrapperSwing(openTruthTableAction))
		projectTruthTablePopupMenu.add(ActionWrapperSwing(deleteProjectItemAction))
		projectTruthTablePopupMenu.add(ActionWrapperSwing(createCircuitAction))

		libraryTruthTablePopupMenu.add(ActionWrapperSwing(openTruthTableAction))
		libraryTruthTablePopupMenu.add(ActionWrapperSwing(deleteLibraryItemAction))
		libraryTruthTablePopupMenu.add(ActionWrapperSwing(createCircuitAction))
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