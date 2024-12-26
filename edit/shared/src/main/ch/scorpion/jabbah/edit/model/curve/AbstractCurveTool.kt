package ch.scorpion.jabbah.edit.model.curve

import ch.scorpion.jabbah.base.Status
import ch.scorpion.jabbah.base.StatusType
import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Tool
import ch.scorpion.jabbah.edit.app.DrawingAppService
import ch.scorpion.jabbah.edit.model.AbstractComponentTool
import ch.scorpion.jabbah.edit.module.EditModule
import kotlin.properties.Delegates

abstract class AbstractCurveTool<T: AbstractCurveComponent>(
    editor: Editor,
    service: DrawingAppService = EditModule.drawingAppService,
    factory: () -> T,
    adder: (T) -> Component = { it }
) : AbstractComponentTool<T>(editor, service, factory, adder) {

    /** Holds the instantiated rectangle. Initialized in [mousePressed].*/
    protected var instance by Delegates.notNull<T>()
        private set

    /** The [Component] that is added to the [Drawing]. */
    private var addedComponent by Delegates.notNull<Component>()

    /** The number of times the user has clicked.*/
    private var clickedCount: Int = 0

    protected abstract val pointsCount: Int

    protected abstract fun createPoints(clickedLocation: Point2D, clickedCount: Int): List<Point2D>

    protected abstract fun createComponent(points: List<Point2D>): T

    protected abstract fun getStatusText(clickCount: Int): String

    protected abstract fun getMovedPointIndex(clickCount: Int): Int

    private fun terminateTool() {
        editor.toolDone()
        clickedCount = 0
    }

    /** ---- [Tool] interface */

    override fun activate() {
        editor.view.setCursor(Cursor.CROSSHAIR)
        Status.set(StatusType.Tool, getStatusText(0))
    }

    override fun mouseClicked(e: MouseEvent, x: Double, y: Double) {
        super.mouseClicked(e, x, y)

        if (e.button != Button.BUTTON1) {
            return
        }

        clickedCount++

        val location = Point2D(x, y).add(editor.snapManager.snap(x, y))
        if (clickedCount == 1) {
            instance = createComponent(createPoints(location, clickedCount))
            editor.view.selectionManager.deselectAll()
            addedComponent = getAddedComponent(instance)
            editor.drawing.add(addedComponent)
            editor.view.selectionManager.select(addedComponent)
            addedComponent.validate()
            Status.set(StatusType.Tool, getStatusText(clickedCount))
        } else if (clickedCount < pointsCount) {
            Status.set(StatusType.Tool, getStatusText(clickedCount))
            instance.points = createPoints(location, clickedCount)
        } else {
            addComponent(addedComponent)
            terminateTool()
        }
    }

    override fun mouseMoved(e: MouseEvent, x: Double, y: Double) {
        super.mouseMoved(e, x, y)

        if (clickedCount > 0) {
            val offset = editor.snapManager.snap(x, y)
            val movedPointIndex = getMovedPointIndex(clickedCount)
            instance.setPointAt(movedPointIndex, offset.add(x, y))
            instance.validate()
        }
    }

    override fun keyPressed(e: KeyEvent) {
        if (e.key == KeyEvent.VK_ESCAPE) {
            if (clickedCount > 0) {
                editor.drawing.remove(addedComponent)
                editor.drawing.validate()
            }
            terminateTool()
        }
    }
}