package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.graph.model.Vertice

/**
 * A [ControlViewSource] is a graphical part of a [VerticeView] that can be added to a
 * [ContainerDrawing] in order to be part of a [SubGraphVerticeView] of the [GraphView] that contains
 * the [VerticeView].
 *
 * This feature allows the designer to place visual (and even interactive) elements of an inner [Graph] onto the
 * outside view of its container.
 *
 * A [GraphViewSource] has the following properties:
 * -It has a graphical representation and thus is a [Drawable]
 * -It might react to user interaction, such as a button the user can click
 * -It has a model whose state is graphically represented by the [ControlViewSource]. The model itself is part
 * of the contained [Graph]. The reference to the model might not be resolved before simulation is started
 * -It can be part of a [SubGraphVerticeView]
 */
interface ControlViewSource<T : Vertice> : VerticeView<T> {

    /**
     * Returns a unique ID that identifies this [ControlViewSource] to the system, and that is the same that is
     * returned by [ControlView]s created by this [ControlViewSource] in [ConrolView.controlId].
     * Used to associate [ControlViewSource] and corresponding [ControlView] in persistent [ContainerDrawing]s.
     */
    val controlId: String?

    /** Returns a translated text that identifies this [ControlViewSource] to the user.*/
    val controlName: String

    /** Returns the path of the icon that identifies this [ControlViewSource] graphically. */
    val iconPath: String

    /** Creates a new instance of a [ControlView] that represents this [ControlViewSource].*/
    fun createControlView(): ControlView<T>
}

/**
 * An event that is posted on the [GraphEditor]'s [EventBus] whenever a [ControlViewSource]
 * has been added or removed.
 */
class ControlViewSourceEvent(val type: Type, val source: ControlViewSource<Vertice>) {

    enum class Type {
        ADD,REMOVE
    }
}