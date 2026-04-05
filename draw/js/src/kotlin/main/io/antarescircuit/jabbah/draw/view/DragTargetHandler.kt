package io.antarescircuit.jabbah.draw.view

import io.antarescircuit.jabbah.base.geom.Point2D
import org.w3c.dom.DragEvent

open class DragTargetHandler {

    open fun onDragEnter(event: DragEvent, viewLocation: Point2D) { }

    open fun onDragOver(event: DragEvent, viewLocation: Point2D) { }

    open fun onDrop(event: DragEvent, viewLocation: Point2D) { }
}