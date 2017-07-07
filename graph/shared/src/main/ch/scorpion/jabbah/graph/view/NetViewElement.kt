package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.graph.model.Net

/**
 * An element of a [NetView].
 * @param T the type of signal that the [Net] of this [NetViewElement] forwards.
 */
interface NetViewElement<T: Any> : GraphElementView<Net<T>> {

    /** Returns the [Net] that this [NetViewElement] displays. Only `null`during deserialization.*/
    val net: Net<T>?

    /** The [NetView] to which this [NetViewElement] belongs. Only `null` during deserialization.*/
    var netView: NetView<T>?

    /**
     * Informs this [NetViewElement] that the [NetViewStyle] of this [NetViewElement]'s
     * [NetView] has changed.
     *
     * Implementations should react by using the [NetViewStyle] to create new [EdgeViewStyling]s and
     * [NodeViewStyling]s instances.
     */
    fun handleNetViewStyleChanged()
}