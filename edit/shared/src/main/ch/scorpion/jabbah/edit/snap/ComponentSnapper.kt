package ch.scorpion.jabbah.edit.snap

import ch.scorpion.jabbah.draw.DrawProperties
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Snappable
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.module.DrawModule

/**
 * Snaps at the [Component]s of a [Drawing] by using their [Snappable] interface for querying
 * their desired snap locations.
 */
class ComponentSnapper(val editor: Editor, snapEnabled: Boolean) : AbstractSnapper(snapEnabled) {

    @Suppress("unused")
    constructor(editor: Editor) : this(editor, true)

    companion object {

        /** The name of the [Color] property in [DrawProperties] for the highlight color.  */
        val PROP_SNAP_HIGHLIGHT_COLOR = "edit.snap.highlight.color"

        /** The name of the [BasicStroke] property in [DrawProperties] for the hightlight stroke.  */
        val PROP_SNAP_HIGHLIGHT_STROKE = "edit.snap.highlight.stroke"
    }

    private val LOG by logger(ComponentSnapper::class)

    private val GRAVITY = 15.0
    private val EMPTY_BBOX = Rectangle2D()

    /** ---- State  */

    /** Highlights the currently snapped x coordinate. Lazy initialized.  */
    private var highlightX: SnapHighlightX? = null

    /** Highlights the currently snapped y coordinate. Lazy initialized.  */
    private var highlightY: SnapHighlightY? = null

    override val boundingBox: Rectangle2D = EMPTY_BBOX

    @Suppress("UNUSED_PARAMETER")
    override fun draw(context: DrawContext) {
        // empty
    }

    @Suppress("UNUSED_PARAMETER")
    override fun contains(x: Double, y: Double): Boolean {
        return false
    }

    /** ---- {@link Snapper} interface */

    /**
     * Calculates the x snapping offset according to the [Component] that yields the smallest
     * snappable distance from the specified location.
     */
    override fun doSnapX(x: Double): Double {
        var minSnapDX = Double.MAX_VALUE
        var minSnapX = Double.MAX_VALUE
        var snappableX: DoubleArray
        var dx: Double

        val iter = editor.drawing.frontToBackIterator()
        while (iter.hasNext()) {
            val comp = iter.next()

            if (editor.view.selectionManager.isSelected(comp)) {
                continue
            }

            snappableX = comp.snappableX
            for (i in snappableX.indices) {
                if (snappableX[i] < x - GRAVITY || snappableX[i] > x + GRAVITY) {
                    continue
                }

//                if (editor.view.selectionManager.isSelected(comp)) {
//                    continue
//                }

                dx = snappableX[i] - x
                if (Math.abs(dx) < Math.abs(minSnapDX)) {
                    minSnapDX = dx
                    minSnapX = snappableX[i]
                }
            }
        }

        return minSnapX
    }

    /**
     * Calculates the y snapping offset according to the [Component] that yields the smallest
     * snappable distance from the specified location.
     */
    override fun doSnapY(y: Double): Double {
        var minSnapDY = Double.MAX_VALUE
        var minSnapY = Double.MAX_VALUE
        var snappableY: DoubleArray
        var dy: Double

        val iter = editor.drawing.frontToBackIterator()
        while (iter.hasNext()) {
            val comp = iter.next()

            if (editor.view.selectionManager.isSelected(comp)) {
                continue
            }

            snappableY = comp.snappableY
            for (i in snappableY.indices) {
                if (snappableY[i] < y - GRAVITY || snappableY[i] > y + GRAVITY) {
                    continue
                }

//                if (editor.view.selectionManager.isSelected(comp)) {
//                    continue
//                }

                dy = snappableY[i] - y
                if (Math.abs(dy) < Math.abs(minSnapDY)) {
                    minSnapDY = dy
                    minSnapY = snappableY[i]
                }
            }
        }

        return minSnapY
    }

    override fun getSnapHighlightX(x: Double, y: Double): Unzoomable? {
        LOG.debug("getSnapHighlightX for $x")
        if (highlightX == null) {
            highlightX = SnapHighlightX()
        }
        highlightX!!.setPositionX(x)
        return highlightX
    }

    override fun getSnapHighlightY(x: Double, y: Double): Unzoomable? {
        LOG.debug("getSnapHighlightX for $y")
        if (highlightY == null) {
            highlightY = SnapHighlightY()
        }
        highlightY!!.setPositionY(y)
        return highlightY
    }

    /** ---- [ComponentSnapper]  */

    internal inner class SnapHighlightX : AbstractDrawable(), Unzoomable {

        /** ---- State  */

        /** Holds the snapped x coordinate in model space.  */
        private var positionX: Double = 0.toDouble()

        override var zoomPan: ZoomPan? = null

        override val boundingBox: Rectangle2D = Rectangle2D()

        /** ---- [Drawable] interface  */

        override fun draw(context: DrawContext) {
            val oldColor = context.g.color
            val oldStroke = context.g.stroke
            context.g.color = DrawModule.properties.getColor(PROP_SNAP_HIGHLIGHT_COLOR)
            context.g.stroke = DrawModule.properties.getStroke(PROP_SNAP_HIGHLIGHT_STROKE)

            val begin = editor.view.modelToView(Point2D(positionX, 0.0))
            context.g.drawLine(
                    begin.x.toInt(),
                    0,
                    begin.x.toInt(),
                    editor.view.height)

            context.g.color = oldColor
            context.g.stroke = oldStroke
        }

        override fun contains(x: Double, y: Double): Boolean {
            return false
        }

        /** ---- [SnapHighlightX]  */

        /**
         * Sets the snapped x coordinate.
         */
        fun setPositionX(x: Double) {
            val lineWidth = DrawModule.properties.getStroke(PROP_SNAP_HIGHLIGHT_STROKE).width
            invalidate()

            positionX = x
            boundingBox.setFrame(
                    positionX - lineWidth / 2,
                    editor.view.viewToModelY(0.0),
                    lineWidth.toDouble(),
                    editor.view.viewToModelY(editor.view.height.toDouble()) - editor.view.viewToModelY(0.0))

            invalidate()
        }
    }

    internal inner class SnapHighlightY : AbstractDrawable(), Unzoomable {

        /** ---- State  */

        /** Holds the snapped y coordinate in model space.  */
        private var positionY: Double = 0.toDouble()

        override var zoomPan: ZoomPan? = null

        override val boundingBox = Rectangle2D()

        /** ---- [Drawable] interface  */

        override fun draw(context: DrawContext) {
            val oldColor = context.g.color
            val oldStroke = context.g.stroke
            context.g.color = DrawModule.properties.getColor(PROP_SNAP_HIGHLIGHT_COLOR)
            context.g.stroke = DrawModule.properties.getStroke(PROP_SNAP_HIGHLIGHT_STROKE)

            val begin = editor.view.modelToView(Point2D(0.0, positionY))
            context.g.drawLine(
                    0,
                    begin.y.toInt(),
                    editor.view.width,
                    begin.y.toInt())

            context.g.color = oldColor
            context.g.stroke = oldStroke
        }

        override fun contains(x: Double, y: Double): Boolean {
            return false
        }

        /** ---- [SnapHighlightX]  */

        /**
         * Sets the snapped x coordinate.
         */
        fun setPositionY(y: Double) {
            val lineWidth = DrawModule.properties.getStroke(PROP_SNAP_HIGHLIGHT_STROKE).width
            invalidate()

            positionY = y
            boundingBox.setFrame(
                    editor.view.viewToModelX(0.0),
                    positionY - lineWidth / 2,
                    editor.view.viewToModelX(editor.view.width.toDouble()) - editor.view.viewToModelX(0.0),
                    lineWidth.toDouble())

            invalidate()
        }
    }
}