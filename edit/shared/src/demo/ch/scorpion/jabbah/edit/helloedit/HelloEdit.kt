package ch.scorpion.jabbah.edit.helloedit

import ch.scorpion.jabbah.draw.polyline.ArrowHead
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.polyline.PolylineComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent

/** Builds a [Drawing] filled with some demo [Component]s. */
fun buildDrawing(): Drawing<Component> {
    val drawing = DrawingImpl<Component>()

    val rect = RectangleComponent(x = 100.0, y = 100.0, w = 200.0, h = 100.0)
    rect.filled = true
    rect.preferredSelectionDrawingStrategy = SelectionDrawingStrategy.ABOVE
    drawing.add(rect)

    val rect2 = RectangleComponent(x = 400.0, y = 300.0, w = 100.0, h = 60.0)
    rect2.filled = true
    rect2.preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW
    drawing.add(rect2)

    val polyline = PolylineComponent()
    polyline.addPoint(100.0, 400.0).addPoint(200.0, 500.0).addPoint(300.0, 300.0)
    polyline.endLineTerminator = ArrowHead()
    drawing.add(polyline)

    return drawing
}