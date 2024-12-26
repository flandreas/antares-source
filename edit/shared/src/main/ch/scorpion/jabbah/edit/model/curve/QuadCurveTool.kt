package ch.scorpion.jabbah.edit.model.curve

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Tool
import ch.scorpion.jabbah.edit.app.DrawingAppService
import ch.scorpion.jabbah.edit.module.EditModule

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