package ch.scorpion.antares.view

import ch.scorpion.antares.view.truthtable.NewTruthTableAction
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.graph.library.LibraryTreeViewActionsSwing
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewType

class DigitalLibraryTreeViewActionsSwing(
	controller: LibraryTreeViewController,
	type: LibraryTreeViewType,
	application: Application
) : LibraryTreeViewActionsSwing(controller, type, application) {

	private val newProjectTruthTableAction = NewTruthTableAction(controller, projectOperationTarget)
	private val newLibraryTruthTableAction = NewTruthTableAction(controller, libraryOperationTarget)

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
}