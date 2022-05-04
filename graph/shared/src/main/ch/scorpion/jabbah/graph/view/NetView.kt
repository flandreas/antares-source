package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.io.Storable

/**
 * A [NetView] is a graphical representation of a [Net] and consists of individual [NetViewElement]s.
 *
 * [NetView] aren't [GraphElementView]s like the [NetViewElement]s of which they consist.
 * [NetView]s are rather logical objects that exist for holding properties that are common to all
 * [NetViewElement]s of which they consist. As a consequence, [NetView]s are not part of a [Drawing],
 * but are separately managed by a [GraphView] implementation. It is the responsibility of the [GraphView]
 * to make sure that individual [NetViewElement]s of a [GraphView] are added to and removed from the
 * [NetView] with which they share a common [Net].
 *
 * @param T the type of signal that the [Net] of this [NetView] forwards.
 */
interface NetView<T : Any> : Storable, Bean {

    val net: Net<T>

	val size: Int

    val isEmpty: Boolean

    var style: NetViewStyle

    var customColor: PredefinedColor?

	fun clone(): NetView<T>

    fun add(elem: NetViewElement<T>)

    fun remove(elem: NetViewElement<T>)

    fun getElements(): ImmutableList<NetViewElement<T>>

	/**
	 * Creates a new [NetView] of the same type as this [NetView], creates a new underlying [Net],
	 * and moves all [NetViewElement] of this [NetView] that are connected to any of [ports] to the
	 * newly created [NetView], including reconnecting these [Port]s to the new [Net].
	 */
	fun splitOff(ports: Set<Port<T>>): NetView<T>

	/**
	 * Combines this [NetView] with the [other] [NetView] by combining their [Nets][Net]
	 * and moving all [NetViewElement] of [other] to this [NetView].
	 *
	 * Does NOT remove [other] from its owning [GraphView] and NOT remove [other]'s [Net] from its owning [Graph].
	 * This is responsibility of objects calling this method.
	 */
	fun combine(other: NetView<T>)
}