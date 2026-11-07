package io.antarescircuit.antares.view.analysis

import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.analysis.CircuitAnalysisError
import io.antarescircuit.antares.model.analysis.CircuitAnalysisService
import io.antarescircuit.antares.model.module.AntaresModelModule
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.graph.library.AbstractContainerLibraryElementAction
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.model.graph.FeedbackLoopChecker
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
import io.antarescircuit.jabbah.io.StorableCloner
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
		val element = selectedItem as ContainerLibraryElement

		if (FeedbackLoopChecker.hasFeedbackLoop(element.storable!!.graph.model!!)) {
			JOptionPane.showConfirmDialog(
				Frame.getFrames()[0],
				Translations.getString("antares.circuitAnalysis.cycles.msg"),
				name,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE
			)
			return
		}

		InvocationHandler.invoke {
			try {

				if (element.storable!!.graph.model !is DigitalGraph) {
					throw CircuitAnalysisError(Translations.getString("antares.circuitAnalysis.onlyDigital.msg"))
				}

				// Clone circuit to git rid of the GraphView acting as ActorListener.
				// Clone the entire MetaGraph and not only the DigitalGraph so that propagation delay expressions
				// get evaluated (GitHub #1146).
				val clone = StorableCloner.clone(element.storable!!)

				val truthTable = service.analyse(clone.graph.model as DigitalGraph)

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