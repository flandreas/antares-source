package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.app.ApplicationDataHolder
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolder
import io.antarescircuit.jabbah.graph.model.graph.GraphPropagationDelayCalculator
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