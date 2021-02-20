package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.geom.Point2D
import org.w3c.dom.DragEvent

open class DragTargetHandler {

	open fun onDragEnter(event: DragEvent, viewLocation: Point2D) { }

	open fun onDragOver(event: DragEvent, viewLocation: Point2D) { }

	open fun onDrop(event: DragEvent, viewLocation: Point2D) { }
}