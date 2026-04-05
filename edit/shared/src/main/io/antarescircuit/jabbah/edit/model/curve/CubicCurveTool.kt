package io.antarescircuit.jabbah.edit.model.curve

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.app.DrawingAppService
import io.antarescircuit.jabbah.edit.module.EditModule

class CubicCurveTool(
    editor: Editor,
    service: DrawingAppService = EditModule.drawingAppService,
    factory: () -> CubicCurveComponent,
    adder: (CubicCurveComponent) -> Component = { it }
) : AbstractCurveTool<CubicCurveComponent>(editor, service, factory, adder) {

    override val pointsCount: Int get() = 4

    override fun getStatusText(clickCount: Int): String =
        when (clickCount) {
            0, 1, 2, 3 -> Translations.getString("edit.tool.cubicCurve.$clickCount.text")
            else -> throw IllegalArgumentException("Invalid click count $clickCount")
        }

    override fun createComponent(points: List<Point2D>): CubicCurveComponent =
        CubicCurveComponent(points)

    override fun getMovedPointIndex(clickCount: Int): Int =
        when (clickCount) {
            1 -> 3
            2 -> 1
            3 -> 2
            else -> throw IllegalArgumentException("")
        }

    override fun createPoints(clickedLocation: Point2D, clickedCount: Int): List<Point2D> =
        when (clickedCount) {
            1 -> listOf(clickedLocation, clickedLocation, clickedLocation, clickedLocation)
            2 -> listOf(instance.points[0], instance.points[0], clickedLocation, clickedLocation)
            3 -> listOf(instance.points[0], clickedLocation, instance.points[2], instance.points[3])
            4 -> listOf(instance.points[0], instance.points[1], clickedLocation, instance.points[3])
            else -> throw IllegalArgumentException("")
        }
}