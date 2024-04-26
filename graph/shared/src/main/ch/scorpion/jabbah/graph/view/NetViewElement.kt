package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.net.node.NodeViewStyling
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewStyling
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.net.netview.NetViewTraversal
import kotlin.reflect.KClass

/**
 * An element of a [NetView].
 * @param T the type of signal that the [Net] of this [NetViewElement] forwards.
 */
interface NetViewElement<T : Any> : GraphElementView<Net<T>> {

	/** Returns the [Net] that this [NetViewElement] displays. Only `null`during deserialization.*/
	var net: Net<T>?

	/** The [NetView] to which this [NetViewElement] belongs. Only `null` during deserialization.*/
	var netView: NetView<T>?

	/**
	 * Traverses a [NetView] starting with this [NetViewElement] and collects all reachable [Port]s.
	 */
	fun traverse(traversal: NetViewTraversal<T>)

	/**
	 * Returns `true` if this [NetViewElement] is connected with any of the [Port]s in [ports].
	 */
	fun isConnectedWithAnyPort(ports: Set<Port<T>>): Boolean

	/**
	 * Informs this [NetViewElement] that the [NetViewStyle] of this [NetViewElement]'s
	 * [NetView] has changed.
	 *
	 * Implementations should react by using the [NetViewStyle] to create new [EdgeViewStyling]s and
	 * [NodeViewStyling]s instances.
	 */
	fun handleNetViewStyleChanged()

	/**
	 * Collects all [Components][Component] of the specified type that are connected
	 * to this [NetViewElement].
	 * @param result the [MutableSet] to which the [Components][Component] are to be added
	 */
	fun collectConnectedVerticeViews(type: KClass<*>, result: MutableSet<Component>)
}