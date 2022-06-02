package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.ControlViewComponent
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A [VerticeView] that is created by a [ControlViewSource] and used to be added to a [SubGraphVerticeView].
 * @param <T> the type of the model
 */
interface ControlView<T : Vertice> : Component, Transparent, ActorView {

    val controlId: String?

	/** Returns a translated text that identifies this [ControlView] to the user.*/
	val controlName: String

	/** The [Vertice] displayed by this [ControlView]. */
	val model: T

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
     */
    fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, model: T)

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