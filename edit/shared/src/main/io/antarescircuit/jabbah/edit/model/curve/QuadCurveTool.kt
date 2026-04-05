package io.antarescircuit.jabbah.edit.model.curve

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.Tool
import io.antarescircuit.jabbah.edit.app.DrawingAppService
import io.antarescircuit.jabbah.edit.module.EditModule

/** A [Tool] for interactively creating a [QuadCurveComponent] in a [Drawing].*/
class QuadCurveTool(
	editor: Editor,
	service: DrawingAppService = EditModule.drawingAppService,
	factory: () -> QuadCurveComponent,
	adder: (QuadCurveComponent) -> Component = { it }
) : AbstractCurveTool<QuadCurveComponent>(editor, service, factory, adder) {

	override val pointsCount: Int get() = 3

	override fun createComponent(points: List<Point2D>): QuadCurveComponent =
		QuadCurveComponent(points)


	override fun getStatusText(clickCount: Int): String =
		when (clickCount) {
			0, 1, 2 -> Translations.getString("edit.tool.quadCurve.$clickCount.text")
			else -> throw IllegalArgumentException("Invalid click count $clickCount")
		}

	override fun getMovedPointIndex(clickCount: Int): Int =
		when (clickCount) {
			1 -> 2
			2 -> 1
			else -> throw IllegalArgumentException("")
		}

	/** ---- [QuadCurveTool] */

	override fun createPoints(clickedLocation: Point2D, clickedCount: Int): List<Point2D> =
		when (clickedCount) {
			1 -> listOf(clickedLocation, clickedLocation, clickedLocation)
			2 -> listOf(instance.points[0], clickedLocation, clickedLocation)
			3 -> listOf(instance.points[0], clickedLocation, instance.points[2])
			else -> throw IllegalArgumentException("")
		}
}