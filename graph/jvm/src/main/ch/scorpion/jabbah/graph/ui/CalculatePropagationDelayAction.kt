package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.model.graph.GraphPropagationDelayCalculator
import java.awt.Frame
import javax.swing.JOptionPane

class CalculatePropagationDelayAction(
    applicationDataHolder: ApplicationDataHolder,
    applicationModeHolder: ApplicationModeHolder,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractApplicationDataEditModeAction(
"graph.action.calculatePropagationDelay",
    applicationDataHolder,
    applicationModeHolder,
    eventBus
) {
    override val opensDialog: Boolean get() = true

    override fun execute(event: ActionEvent) {
        (applicationDataHolder.data?.content as MetaGraph?)?.let {
            val propDelay = GraphPropagationDelayCalculator().calculate(it.graph.model!!)
            if (propDelay < 0L) {
                JOptionPane.showMessageDialog(
                    Frame.getFrames()[0],
                    Translations.getString("graph.action.calculatePropagationDelay.undefined.text"),
                    name,
                    JOptionPane.WARNING_MESSAGE)
            } else {
                JOptionPane.showMessageDialog(
                    Frame.getFrames()[0],
                    "${Translations.getString("graph.property.propagationDelay.name")}: $propDelay",
                    name,
                    JOptionPane.INFORMATION_MESSAGE
                )
            }
        }
    }

    override fun calculateEnabled(): Boolean =
        super.calculateEnabled() && applicationDataHolder.data?.content is MetaGraph
}