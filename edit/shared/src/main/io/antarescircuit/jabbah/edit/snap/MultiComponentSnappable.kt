package io.antarescircuit.jabbah.edit.snap

import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Snappable
import io.antarescircuit.jabbah.edit.SnappableX
import io.antarescircuit.jabbah.edit.SnappableY

/**
 * Defines an artificial [Snappable] around a set of [Component]s in order to move them using snapping
 * support.
 *
 * @param components the [Component]s to be snapped
 */
class MultiComponentSnappable(components: Collection<Component>) : Snappable {

    private val snappables: List<Component> = components.toList()

    /** ---- [Snappable] interface */

    override val snappableX: Array<SnappableX>
        get() {
            val result = mutableListOf<SnappableX>()
            snappables.forEach { result.addAll(it.snappableX) }
            return result.toTypedArray()
        }

    override val snappableY: Array<SnappableY>
        get() {
            val result = mutableListOf<SnappableY>()
            snappables.forEach { result.addAll(it.snappableY) }
            return result.toTypedArray()
        }
}