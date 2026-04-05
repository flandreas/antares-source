package io.antarescircuit.antares.view.fsm

import io.antarescircuit.antares.model.fsm.FSMState
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.antares.model.fsm.FSMTransition
import io.antarescircuit.jabbah.base.geom.Geometry
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.draw.polyline.ArrowHead
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlight
import io.antarescircuit.jabbah.graph.view.style.GraphStyleType

/**
 * A ghost [Drawable] used as a transient preview for a [FSMTransition] under interactive construction.
 */
class TransitionGhost(
    private val originState: FSMState
) : AbstractDrawable() {

    companion object {
        private val STROKE = DrawStyleModule.styleProvider.getStyle(GraphStyleType.EDGE).stroke
    }

    private val arrowHead = ArrowHead()

    private var _destinationState: FSMState? = null

    var destinationState: FSMState?
        get() = _destinationState
        set(value) {
            invalidate()
            _destinationState = value
            updateGeometry()
            invalidate()
            validate()
        }

    var draggedPoint: Point2D? = null
        set(value) {
            invalidate()
            _destinationState = null
            field = value
            updateGeometry()
            invalidate()
            validate()
        }

    private var originPoint: Point2D = Point2D.ZERO
    private var destinationPoint: Point2D = Point2D.ZERO

    private val bbox = Rectangle2D()

    override val boundingBox: RectangularShape get() {
        updateGeometry()
        return bbox
    }

    override fun draw(context: DrawContext) {
        context.g.stroke = STROKE
        context.g.color = DrawModule.properties.getColor(ConnectionPointHighlight.PROP_COLOR)
        context.g.drawLine(originPoint, destinationPoint)
        arrowHead.draw(context)
    }

    override fun contains(x: Double, y: Double): Boolean = bbox.contains(x, y)

    private fun updateGeometry() {
        if (destinationState != null) {
            originPoint = Geometry.circleLineIntersection(originState.center, originState.radius, destinationState!!.center)
            destinationPoint = Geometry.circleLineIntersection(destinationState!!.center, destinationState!!.radius, originState.center)
        } else if (draggedPoint != null) {
            originPoint = Geometry.circleLineIntersection(originState.center, originState.radius, draggedPoint!!)
            destinationPoint = draggedPoint!!
        } else {
            originPoint = originState.center
            destinationPoint = originState.center
        }
        arrowHead.setLocation(destinationPoint, originPoint)
        updateBbox()
    }

    private fun updateBbox() {
        bbox.setFrame(originPoint.x, originPoint.y, 0.0, 0.0)
        bbox.add(destinationPoint)
        bbox.add(arrowHead.boundingBox)
    }
}