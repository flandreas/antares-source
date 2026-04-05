package io.antarescircuit.jabbah.edit

import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.edit.model.text.description.Namable
import io.antarescircuit.jabbah.edit.model.text.description.Name

/**
 * A [Drawing] is a container of editable [Component]s.
 */
interface Drawing<T : Component> : ComponentContainer<T>, Bean, Namable {

    override var name: Name

    /**
     * Notifies this [Drawing] that the editability of its context (e.g. the view in which it is displayed)
     * has changed. The default implementation forwards to all [Components][Component].
     */
    fun notifyEditable(editable: Boolean) {
        drawables.forEach { it.notifyEditable(editable) }
    }
}