package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.help.HelpId
import ch.scorpion.jabbah.base.help.HelpIdProvider
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.style.GraphStyleType

/**
 * Represents a graphical representation of a [Vertice].
 * After completion of construction, a [VerticeView] must have at least one [PortView], because features
 * like snapping try to determine connection points, which are derived from [PortView].
 *
 * @param T the type of [Vertice] that this [VerticeView] graphically represents.
 */
interface VerticeView<T : Vertice>
	: GraphElementView<T>, Describable, ConnectableView, ActorView, HelpIdProvider {

	override val helpId: HelpId? get() = this::class.simpleName?.let { HelpId(it) }

	val vertice: Vertice get() = model

	/** Returns the number of [PortView]s of this [VerticeView].*/
	val portViewCount: Int

	/** Determines whether this [VerticeView] shows its [PortView]s or not.*/
	var isShowPortViews: Boolean

	/** Adds the specified [PortView] to this [VerticeView]. */
	fun addPortView(portView: PortView<*>)

	/** Removes the specified [PortView] from this [VerticeView]. */
	fun removePortView(portView: PortView<*>)

	/** Returns the [PortView]s of this [VerticeView].*/
	fun getPortViews(): ImmutableList<PortView<*>>

	/** Returns the [PortView] that contains the specified absolute location, also respecting the label of the [PortView].*/
	fun getPortViewAt(x: Double, y: Double): PortView<*>?

	/**
	 * Returns the [PortView] whose connection point is at the specified absolute location.
	 * @return the [PortView] at `(x, y)`, if any.
	 */
	fun getPortViewAtConnectionPoint(x: Double, y: Double): PortView<*>?

	fun getPortViewAtConnectionPoint(p: Point2D): PortView<*>? = getPortViewAtConnectionPoint(p.x, p.y)

	/**
	 * Draws visual primitives to become obvious that this [VerticeView]'s current state let
	 * information flow from the [InputPort] with name [inputName] to the [OutputPort] with name [outputName].
	 */
	fun drawDataFlow(inputName: String, outputName: String, context: DrawContext)

	fun drawFocus(context: DrawContext) {}

	/**
	 * Returns the foreground [CompositeColor] in which to draw a [PortView] during editing (not during execution).
	 * By default, the color of [GraphStyleType.EDGE] is returned. Implementors can override this
	 * if they are "wire-type" components that can draw themselves in a custom color, and like
	 * to apply this color also to their [PortView]s.
	 */
	fun getEditPortViewColor(styleProvider: StyleProvider): CompositeColor =
		styleProvider.getStyle(GraphStyleType.EDGE).color
}