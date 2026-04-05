package io.antarescircuit.jabbah.graph.model.vertice

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.event.ActionEvent

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