package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.editor.GraphEditor
import ch.scorpion.jabbah.graph.model.Graph

/**
 * A [ControlViewSource] is a graphical part of a [VerticeView] that can be added to a
 * [ContainerDrawing] in order to be part of a [SubGraphVerticeView] of the [GraphView] that contains
 * the [VerticeView].
 *
 * This feature allows the designer to place visual (and even interactive) elements of an inner [Graph] onto the
 * outside view of its container.
 *
 * A [ControlViewSource] has the following properties:
 * -It has a graphical representation and thus is a [Drawable]
 * -It might react to user interaction, such as a button the user can click
 * -It has a model whose state is graphically represented by the [ControlViewSource]. The model itself is part
 * of the contained [Graph]. The reference to the model might not be resolved before simulation is started
 * -It can be part of a [SubGraphVerticeView]
 */
interface ControlViewSource<T : Vertice> : VerticeView<T> {

    /**
     * Returns a unique ID that identifies this [ControlViewSource] to the system, and that is the same that is
     * returned by [ControlView]s created by this [ControlViewSource] in [ControlView.controlId].
     * Used to associate [ControlViewSource] and corresponding [ControlView] in persistent [ContainerDrawing]s.
     *
     * Don't use [GraphElementView.id] as part of the [controlId], because that one might change
     * when [ControlView]s (even as part of a wrapping [Component]) are added to a [Drawing]
     */
    val controlId: String?

    /** Returns a translated text that identifies this [ControlViewSource] to the user.*/
    val controlName: String

    /** Returns the path of the icon that identifies this [ControlViewSource] graphically. */
    val iconPath: String

    /** Creates a new instance of a [ControlView] that represents this [ControlViewSource].*/
    fun createControlView(): ControlView<T>

	/**
	 * Posts a [ControlViewSourceEvent] on the specified [EventBus] to indicate that this
	 * [ControlViewSource] has changed some of its property, which might be of interest
	 * for [ControlView]s created by this [ControlViewSource].
	 */
	fun postControlViewSourceChangeEvent(eventBus: EventBus) {
		eventBus.post(ControlViewSourceEvent(ControlViewSourceEvent.Type.CHANGE, this as ControlViewSource<Vertice>))
	}
}

/**
 * An event that is posted on the [GraphEditor]'s [EventBus] whenever a [ControlViewSource]
 * has been added to or removed from the main [GraphView], or when a [ControlViewSource]'s property
 * that is reflected by a created [ControlView] has changed, in which case the [ControlView] that receives
 * the event will copy all relevant properties from its source [ControlViewSource].
 */
class ControlViewSourceEvent(val type: Type, val source: ControlViewSource<Vertice>) {

    enum class Type {
        ADD,
	    REMOVE,
	    CHANGE
    }
}