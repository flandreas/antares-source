package io.antarescircuit.jabbah.edit.model.rectangle

import io.antarescircuit.jabbah.base.geom.MutableRectangularShape
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.edit.Component

/** Geometry contract shared by rectangle handles and their undo command. */
interface ResizableRectangularComponent : Component {

	val shape: MutableRectangularShape
	val maintainAspectRation: Boolean get() = false
	val minimumWidth: Double get() = 0.0
	val minimumHeight: Double get() = 0.0
	val width: Double get() = shape.width
	val height: Double get() = shape.height

	fun handleToWorld(p: Point2D) = p

	fun worldToHandle(p: Point2D) = p

	/** Applies a handle drag in the rectangle geometry coordinate system. */
	fun resizeFrame(x: Double, y: Double, width: Double, height: Double) = setFrame(x, y, width, height)

	fun setFrame(x: Double, y: Double, width: Double, height: Double)

	fun setFrame(rect: RectangularShape) =
		setFrame(rect.x, rect.y, rect.width, rect.height)
}
