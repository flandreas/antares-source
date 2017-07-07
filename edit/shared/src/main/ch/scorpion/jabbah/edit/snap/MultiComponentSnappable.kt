package ch.scorpion.jabbah.edit.snap

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Snappable

/**
 * Defines an artificial [Snappable] around a set of [Component]s in order to move them using snapping
 * support.
 *
 * @param components the [Component]s to be snapped
 */
class MultiComponentSnappable(components: Collection<Component>) : Snappable {

    private val snappables: List<Component> = components.toList()
    private val snapX: DoubleArray
    private val snapY: DoubleArray

    init {
        var sizeX = 0
        var sizeY = 0

        for (component in components) {
            sizeX += component.snappableX.size
            sizeY += component.snappableY.size
        }
        snapX = DoubleArray(sizeX)
        snapY = DoubleArray(sizeY)
    }

    /** ---- [Snappable] interface */

    override val snappableX: DoubleArray
        get() {
            var i = 0
            for (component in snappables) {
                val sX = component.snappableX
                for (d in sX) {
                    snapX[i] = d
                    i++
                }
            }
            return snapX
        }

    override val snappableY: DoubleArray
        get() {
            var i = 0
            for (component in snappables) {
                val sY = component.snappableY
                for (d in sY) {
                    snapY[i] = d
                    i++
                }
            }
            return snapY
        }
}