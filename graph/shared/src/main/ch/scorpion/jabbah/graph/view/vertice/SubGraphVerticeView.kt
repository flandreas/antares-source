package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.library.Library

/**
 * A graphical representation of a [SubGraphVertice] that can be added to a [GraphView].
 */
interface SubGraphVerticeView<T : SubGraphVertice> : VerticeView<T> {

    /** Returns the model of this [SubGraphVerticeView].*/
    val subGraphVertice: SubGraphVertice?

	/** Contains the script code that customized the visual look while execution mode.*/
	var drawExecScript: String?

	val hasCustomizedContainerDrawing: Boolean

	/** Creates a new [GraphView] of the references sub [Graph].*/
    fun createSubGraphView(): GraphView<GraphElementView<T>>

    /** Adds a [Drawable] to be part of the graphical representation of this [SubGraphVerticeView] */
    fun addDrawable(drawable: Drawable)

    /**
     * Returns the [ContainerDrawing] that can be customized by the user, which is either the one of the reference
     * [Library] [MetaGraph] (if the user hasn't customized it yet), or the previously customized
     * [ContainerDrawing].
     */
    fun getEditableContainerDrawing(): ContainerDrawing

    /**
     * Sets or resets the [ContainerDrawing] that has been customized by the user.
     * @param containerDrawing the customized [ContainerDrawing], or `null` if the customization is to be
     * deleted and the [SubGraphVerticeView] should use the standard [ContainerDrawing] from the [Library].
     */
    fun setEditedContainerDrawing(containerDrawing: ContainerDrawing?)

}