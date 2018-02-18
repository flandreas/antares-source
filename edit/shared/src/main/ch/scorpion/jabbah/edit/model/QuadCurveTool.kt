package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.geom.Geometry
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Tool
import ch.scorpion.jabbah.edit.editor.AddCommand
import ch.scorpion.jabbah.edit.model.curve.QuadCurveComponent
import kotlin.properties.Delegates

/** A [Tool] for interactively creating a [QuadCurveComponent] in a [Drawing].*/
class QuadCurveTool(
	editor: Editor,
	factory: () -> QuadCurveComponent,
	adder: (QuadCurveComponent) -> Component = { it }
	) : AbstractComponentTool<QuadCurveComponent>(editor, factory, adder) {

	/** Holds the instantiated rectangle. Initialized in [mousePressed].*/
	private var instance by Delegates.notNull<QuadCurveComponent>()

	/** The [Component] that is added to the [Drawing]. */
	private var addedComponent by Delegates.notNull<Component>()

	/** The number of times the user has clicked.*/
	private var clickedCount: Int = 0

	/** ---- [Tool] interface */

	override fun activate() {
		editor.view.setCursor(Cursor.CROSSHAIR)
	}

	override fun mouseClicked(e: MouseEvent, x: Double, y: Double) {
		super.mouseClicked(e, x, y)

		if (e.button != Button.BUTTON1) {
			return
		}

		clickedCount++

		val location = Point2D(x, y).add(editor.snapManager.snap(x, y))
		if (clickedCount == 1) {
			instance = QuadCurveComponent(createPoints(location))
			addedComponent = getAddedComponent(instance)
			editor.drawing.add(addedComponent)
			addedComponent.validate()
		} else {
			instance.points = createPoints(location)
		}

		if (clickedCount == 3) {
			editor.commandManager.register(AddCommand(editor, addedComponent))
			editor.view.selectionManager.select(addedComponent)
			editor.toolDone()
			clickedCount = 0
		}
	}

	override fun mouseMoved(e: MouseEvent, x: Double, y: Double) {
		if (clickedCount > 0) {
			val offset = editor.snapManager.snap(x, y)
			var movedPointIndex = when(clickedCount) {
				1 -> 2
				2 -> 1
				else -> throw IllegalArgumentException("")
			}
			instance.setPointAt(movedPointIndex, offset.add(x, y))
			instance.validate()
		}
	}

	override fun keyTyped(e: KeyEvent) {
		if (e.key == KeyEvent.VK_ESCAPE && clickedCount > 0) {
			editor.drawing.remove(addedComponent)
			editor.drawing.validate()
		}
		editor.toolDone()
	}

	/** ---- [QuadCurveTool] */

	private fun createPoints(clickedLocation: Point2D): List<Point2D> {
		return when(clickedCount) {
			1 -> listOf(clickedLocation, clickedLocation, clickedLocation)
			2 -> listOf(instance.points[0], clickedLocation, clickedLocation)
			3 -> listOf(instance.points[0], clickedLocation, instance.points[2])
			else -> throw IllegalArgumentException("")
		}
	}
}