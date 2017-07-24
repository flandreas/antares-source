package ch.scorpion.jabbah.edit.snap

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.MouseAdapter
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.editor.DropEvent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger

/**
 * Standard implementation of the [SnapManager] interface.
 */
class SnapManagerImpl(val editor: Editor, eventBus: EventBus) : SnapManager {

    constructor(editor: Editor): this(editor, BaseModule.eventBus)

    private val LOG by logger(SnapManagerImpl::class)
    private val ZERO_OFFSET = Point2D()

    /** The [Snapper]s that are orchestrated by this [SnapManager]. The [Snapper] at index 0 gets involved first.*/
    private val snappers: MutableList<Snapper> = mutableListOf<Snapper>()

    /** Used for cummulating snap rsults that are produces by the individual [Snapper]s.*/
    private val result = SnapResult()

    /** The [Drawable] that highlights the currently snapped x coordinate, if any.*/
    private var highlightX: Unzoomable? = null

    /** The [Drawable] that highlights the currently snapped y coordinate, if any.*/
    private var highlightY: Unzoomable? = null

    init {
        // Listens for mouse releases in order to remove highlighters from the [View]
        editor.view.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                removeAllHighights()
            }
        })

        eventBus.register(DropEvent::class, { removeAllHighights() })
    }

    /** ---- [SnapManager] interface */

    override var snapEnabled: Boolean = true

    override var highlightEnabled: Boolean = true

    override fun addSnapper(snapper: Snapper) {
        if (!snappers.contains(snapper)) {
            LOG.debug("SnapMangerImpl: adding snapper '${ch.scorpion.jabbah.base.System.SYSTEM!!.getClassName(snapper)}'")
            snappers.add(snapper)
        }
    }

    override fun snap(x: Double, y: Double): Point2D {
        if (!snapEnabled) {
            ZERO_OFFSET.setLocation(0.0, 0.0)
            return ZERO_OFFSET
        }

        var snapX = x
        var snapY = y
        var index = 0
        var done = false

        result.reset()

        // downwards
        while (!done && index < snappers.size) {
            if (result.snappedX && result.snappedY) {
                // x and y already snapped
                done = true
            } else if (!result.snappedX && !result.snappedY) {
                // nothing snapped yet
                snapX += result.dx
                snapY += result.dy
                snappers[index].snap(snapX, snapY, result)
            } else if (!result.snappedX) {
                // only x needs snapping
                snapX += result.dx
                snappers[index].snapX(snapX, result)
            } else if (!result.snappedY) {
                // only y needs snapping
                snapY += result.dy
                snappers[index].snapY(snapY, result)
            }
            index++
        }

        // snap upwards
        index -= 2
        while (index >= 0) {
            snapX = x + result.dx
            snapY = y + result.dy
            snappers[index].snap(snapX, snapY, result)
            index--
        }

        if (highlightEnabled) {
            highlightSnapX(y)
            highlightSnapY(x)
            editor.view.ghostContainer.validate()
        }

        return Point2D(result.dx, result.dy)
    }

    override fun snapX(x: Double, y: Double): Double {
        if (!snapEnabled) {
            return 0.0
        }

        var snapX = x
        var index = 0
        var done = false

        result.reset()

        // snap downwards
        while (!done && index < snappers.size) {
            val snapper = snappers[index]
            if (result.snappedX) {
                // x already snapped
                done = true
            } else {
                // x needs snapping
                snapX += result.dx
                snapper.snapX(snapX, result)
            }
            index++
        }

        // snap upwards
        snapX = x
        index -= 2
        while (index >= 0) {
            val snapper = snappers[index]
            snapX += result.dx
            snapper.snapX(snapX, result)
            index--
        }

        if (highlightEnabled) {
            highlightSnapX(y)
            editor.view.ghostContainer.validate()
        }

        return result.dx
    }

    override fun snapY(x: Double, y: Double): Double {
        if (!snapEnabled) {
            return 0.0
        }

        var snapY = y
        var index = 0
        var done = false

        result.reset()

        // downwards
        while (!done && index < snappers.size) {
            val snapper = snappers[index]
            if (result.snappedY) {
                // y already snapped
                done = true
            } else {
                // y needs snapping
                snapY += result.dy
                snapper.snapY(snapY, result)
            }
            index++
        }

        // snap upwards
        snapY = y
        index -= 2
        while (index >= 0) {
            val snapper = snappers[index]
            snapY += result.dy
            snapper.snapY(snapY, result)
            index--
        }

        if (highlightEnabled) {
            highlightSnapY(x)
            editor.view.ghostContainer.validate()
        }

        return result.dy
    }

    override fun snap(snappable: Snappable, dx: Double, dy: Double): Point2D {
        if (!snapEnabled) {
            return Point2D()
        }

        var snapDX = dx
        var snapDY = dy
        var index = 0
        var done = false

        result.reset()

        // snap downwards
        while (!done && index < snappers.size) {
            val snapper = snappers[index]
            if (result.snappedX && result.snappedY) {
                // x and y already snapped
                done = true
            } else if (!result.snappedX && !result.snappedY) {
                // nothing snapped yet
                snapDX += result.dx
                snapDY += result.dy
                snapper.snap(snappable, snapDX, snapDY, result)
            } else if (!result.snappedX) {
                // only x needs snapping
                snapDX += result.dx
                snapper.snapX(snappable, snapDX, result)
            } else if (!result.snappedY) {
                // only y needs snapping
                snapDY += result.dy
                snapper.snapY(snappable, snapDY, result)
            }
            index++
        }

        // snap upwards
        index -= 2
        while (index >= 0) {
            val snapper = snappers[index]
            snapDX = dx + result.dx
            snapDY = dy + result.dy
            snapper.snap(snappable, snapDX, snapDY, result)
            index--
        }

        if (highlightEnabled) {
            highlightSnapX(0.0)
            highlightSnapY(0.0)
            editor.view.ghostContainer.validate()
        }

        return Point2D(result.dx, result.dy)
    }

    /** ---- [SnapManagerImpl] */

    private fun highlightSnapX(y: Double) {
        var newHighlightX: Unzoomable? = null

        if (result.snappedX) {
            newHighlightX = result.snapperX!!.getSnapHighlightX(result.x, if (result.snappedY) result.y else y)
        }

        if (highlightX != null && (newHighlightX == null || newHighlightX !== highlightX)) {
            editor.view.ghostContainer.remove(highlightX!!)
        }
        if (newHighlightX != null && result.snappedX) {
            LOG.debug("Highlight x coordinate at ${result.x}")
            editor.view.ghostContainer.add(newHighlightX)
        }
        highlightX = newHighlightX
    }

    private fun highlightSnapY(x: Double) {
        var newHighlightY: Unzoomable? = null

        if (result.snappedY) {
            newHighlightY = result.snapperY!!.getSnapHighlightY(if (result.snappedX) result.x else x, result.y)
        }

        if (highlightY != null && (newHighlightY == null || newHighlightY !== highlightY)) {
            editor.view.ghostContainer.remove(highlightY!!)
        }
        if (newHighlightY != null && result.snappedY) {
            LOG.debug("Highlight y coordinate at ${result.y}")
            editor.view.ghostContainer.add(newHighlightY)
        }
        highlightY = newHighlightY
    }

    private fun removeAllHighights() {
        highlightX?.let {
            LOG.debug("SnapManagerImpl: removing highlightX from view")
            editor.view.ghostContainer.remove(highlightX!!)
            highlightX = null
        }
        highlightY?.let {
            LOG.debug("SnapManagerImpl: removing highlightY from view")
            editor.view.ghostContainer.remove(highlightY!!)
            highlightY = null
        }
        editor.view.ghostContainer.validate()
    }
}