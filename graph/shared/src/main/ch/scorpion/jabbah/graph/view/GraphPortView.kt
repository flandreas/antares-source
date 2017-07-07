package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.graph.model.GraphPort

/**
 * A graphical representation of a [GraphPort].
 */
interface GraphPortView<T : GraphPort<Any>> : VerticeView<T> {

    /** Returns the path of the icon that identifies this [GraphPortView] graphically. */
    val iconPath: String
}