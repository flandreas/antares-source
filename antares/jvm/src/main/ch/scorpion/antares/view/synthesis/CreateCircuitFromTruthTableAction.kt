package ch.scorpion.antares.view.synthesis

import ch.scorpion.antares.AntaresModuleJvm
import ch.scorpion.antares.model.truthtable.TruthTableLibraryItem
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractLibraryAction
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Frame

class CreateCircuitFromTruthTableAction(
	controller: LibraryTreeViewController,
	private val service: CreateCircuitFromTruthTableService = AntaresModuleJvm.createCircuitFromTruthTableService
) : AbstractLibraryAction("antares.synthesis.createCircuitFromTruthTable.action", Operation.Change, controller) {

	override val authorizationTarget: Any? get() = if (selectedItem is TruthTableLibraryItem) selectedItem!!.library else null

	override val opensDialog: Boolean get() = true

	init {
		updateEnabled()
	}

	override fun execute(event: ActionEvent) {
		val item = selectedItem as TruthTableLibraryItem
		CreateCircuitFromTruthTablePanel.showAsDialog(
			Frame.getFrames()[0],
			item.storable,
			item,
			service)
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && selectedItem is TruthTableLibraryItem
}