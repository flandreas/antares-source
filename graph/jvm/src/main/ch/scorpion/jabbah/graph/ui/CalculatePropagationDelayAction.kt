package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractContainerLibraryElementAction
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.model.graph.GraphPropagationDelayCalculator
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Frame
import javax.swing.JOptionPane

class CalculatePropagationDelayAction(
    controller: LibraryTreeViewController
) : AbstractContainerLibraryElementAction(
"graph.action.calculatePropagationDelay",
    operation = Operation.View,
    controller
) {
    override fun execute(event: ActionEvent) {
        (controller.selectedItem as ContainerLibraryElement).metaGraph?.graph?.model?.let {
            val propDelay = GraphPropagationDelayCalculator().calculate(it)
            JOptionPane.showMessageDialog(
                Frame.getFrames()[0],
                "${Translations.getString("graph.property.propagationDelay.name")}: $propDelay",
                name,
                JOptionPane.INFORMATION_MESSAGE)
        }
    }
}