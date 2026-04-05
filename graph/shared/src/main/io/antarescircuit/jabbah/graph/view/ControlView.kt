package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.drawable.Transparent
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.execution.actor.ActorView
import io.antarescircuit.jabbah.graph.container.ContainerDrawing
import io.antarescircuit.jabbah.graph.container.ControlViewComponent
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeLink
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * A [GraphElementView] that is created by a [ControlViewSource] and used to be added to a [SubGraphVerticeView].
 * Cannot just be a [Drawable] because it needs to be selectable when editing it within a [ContainerDrawing].
 * Cannot just be a [Component] because its model must be cloned correctly when added to a [ContainerDrawing].
 * @param <T> the type of the model
 */
interface ControlView<T : Vertice> : GraphElementView<T>, Transparent, ActorView {

    val controlId: String?

	/** Returns a translated text that identifies this [ControlView] to the user.*/
	val controlName: String

	/**
	 * Used by [ControlView] that share implementation with [VerticeView]. Called by wrapping objects
	 * to make sure that such [ControlView]s don't show their [PortView]s. Corresponds with
	 * [VerticeView.isShowPortViews].
	 */
	var isShowPortViews: Boolean

	/**
	 * Objects that effectively operate as [ControlView] return `true`.
	 * [VerticeView] that also implement [ControlView] will return `false` by default.
	 */
	var isActiveControlView: Boolean

	/** The dislocation of the location's x coordinate when mirroring this [ControlView] horizontally.*/
	val mirrorWidth: Double get() = 0.0

	/** The dislocation of the location's y coordinate when mirroring this [ControlView] vertically.*/
	val mirrorHeight: Double get() = 0.0

	/**
     * Binds this [ControlView] to the corresponding [Vertice] of the [Graph] that is contained in the
     * [SubGraphVerticeView] that owns this [ControlView]. Used for establishing a process to update this [ControlView]
     * whenever the corresponding [Vertice] changes.
	 * Implementations will use [link] to retrieve the [Vertice] and set it as their own model.
     */
	fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph)

	/**
	 * Called by the editor environment to inform this [ControlView] that some properties of the [ControlViewSource]
	 * that created this [ControlView] have changed. Depending on the type of [ControlView], it will copy some
	 * or all of the [ControlViewSource]'s properties.
	 */
	fun sourcePropertiesChanged(source: ControlViewSource<T>)

	/**
	 * Called by [ControlViewComponent] to ask this [ControlView] to write current values of its model [GraphElement].
	 * This is necessary when storing [ControlView]s in [ContainerDrawing]s, which don't have a model layer.
	 * Typical [ControlView]s will store properties like the name of the model.
	 */
	fun writeModelProperties(writer: StoreWriter)

	/** Reads model properties that have been stored by [writeModelProperties].*/
	fun readModelProperties(reader: StoreReader)

}