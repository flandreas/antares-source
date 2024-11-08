package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.draw.drawable.Unzoomable

interface UnzoomableContainerIF<T : Unzoomable> : DrawableContainer<T>, Unzoomable

/**
 * Implementation of a [DrawableContainer] that contains [Unzoomable]s.
 */
class UnzoomableContainer<T : Unzoomable> : DrawableContainerImpl<T>(), UnzoomableContainerIF<T> {

    override var zoomPan: ZoomPan? = null
        set(value) {
            field = value
            val iter = frontToBackIterator()
            while (iter.hasNext()) {
                iter.next().zoomPan = value
            }
        }

    override fun add(drawable: T, index: Int): DrawableContainer<T> {
        super.add(drawable, index)
        drawable.zoomPan = zoomPan
        return this
    }
}