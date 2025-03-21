package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent

class EnableInteractivePropagationDelayAction : AbstractAction("graph.action.interactivePropagationDelay") {

    init {
        updateSelected()
    }

    private fun updateSelected() {
        selected = AbstractInteractableVertice.enableInteractivePropagationDelay
    }

    override fun execute(event: ActionEvent) {
        AbstractInteractableVertice.enableInteractivePropagationDelay = !AbstractInteractableVertice.enableInteractivePropagationDelay
        updateSelected()
    }
}