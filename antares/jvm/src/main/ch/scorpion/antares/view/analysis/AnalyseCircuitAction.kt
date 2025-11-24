package ch.scorpion.antares.view.analysis

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.analysis.CircuitAnalysisError
import ch.scorpion.antares.model.analysis.CircuitAnalysisService
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractContainerLibraryElementAction
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import ch.scorpion.jabbah.io.StorableCloner
import java.awt.Frame
import javax.swing.JOptionPane

class AnalyseCircuitAction(
	controller: LibraryTreeViewController,
	private val service: CircuitAnalysisService = AntaresModelModule.circuitAnalysisService
) : AbstractContainerLibraryElementAction(
	actionBaseName = "library.action.analyseCircuit",
	operation = Operation.View,
	controller
) {
	override val opensDialog: Boolean get() = true

	init {
	    updateEnabled()
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled()
			&& (selectedItem as ContainerLibraryElement).graphType == AntaresGraphTypes.Digital

	override fun execute(event: ActionEvent) {
		InvocationHandler.invoke {
			try {
				// Close circuit to git rid of the GraphView acting as ActorListener
				val element = selectedItem as ContainerLibraryElement

				if (element.storable!!.graph.model !is DigitalGraph) {
					throw CircuitAnalysisError(Translations.getString("antares.circuitAnalysis.onlyDigital.msg"))
				}

				val clone = StorableCloner.clone(element.storable!!.graph.model as DigitalGraph)
				val truthTable = service.analyse(clone)

				AnalyseCircuitPanel.showAsDialog(Frame.getFrames()[0], element, truthTable)
			} catch (e: CircuitAnalysisError) {
				JOptionPane.showConfirmDialog(
					Frame.getFrames()[0],
					e.message,
					Translations.getString("antares.circuitAnalysis.title"),
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.ERROR_MESSAGE)
			}
		}
	}
}