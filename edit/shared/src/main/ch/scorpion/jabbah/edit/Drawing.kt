package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.Bean
import ch.scorpion.jabbah.edit.model.text.description.Namable
import ch.scorpion.jabbah.edit.model.text.description.Name

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