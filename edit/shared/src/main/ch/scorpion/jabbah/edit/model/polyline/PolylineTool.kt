package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Tool
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.editor.AddCommand
import ch.scorpion.jabbah.edit.model.AbstractComponentTool
import ch.scorpion.jabbah.draw.polyline.Polyline
import ch.scorpion.jabbah.draw.graphics.Cursor
import kotlin.properties.Delegates

@Suppress("unused")
/**
 * A [Tool] for interactively creating a [PolylineComponent].
 *
 * Each time the user clicks with the mouse, a new point is added to the [PolylineComponent] under construction. A
 * double click finishes shaping the [PolylineComponent], but at least two points are required to form a valid
 * [Polyline].
 */
class PolylineTool(
        editor: Editor,
        factory: () -> PolylineComponent,
        adder: (PolylineComponent) -> Component
) : AbstractComponentTool<PolylineComponent>(editor, factory, adder) {

    constructor(editor: Editor, factory: () -> PolylineComponent): this(editor, factory, { it })

    /** Holds the instantiated rectangle. Initialized in [mousePressed].*/
    private var instance: PolylineComponent? = null

    /** The [Component] that is added to the [Drawing]. */
    private var addedComponent by Delegates.notNull<Component>()

    /** ---- [Tool] interface */

    override fun activate() {
        editor.view.setCursor(Cursor.CROSSHAIR)
    }

    override fun mouseClicked(e: MouseEvent, x: Double, y: Double) {
        super.mouseClicked(e, x, y)

        if (e.button != Button.BUTTON1) {
            return
        }

        if (e.clickCount == 1) {
            val offset = editor.snapManager.snap(x, y)
            if (instance == null) {
                instance = createComponent()
                instance!!.addPoint(x + offset.x, y + offset.y)
                addedComponent = getAddedComponent(instance as PolylineComponent)
                editor.drawing.add(addedComponent)
            }
            // add the dangling point that will be moved around
            instance!!.addPoint(x + offset.x, y + offset.y)
        } else if (e.clickCount == 2) {
            instance!!.removePoint(instance!!.pointsCount - 1)
            editor.commandManager.beginTransaction(AddCommand(editor, addedComponent))
            editor.commandManager.commitTransaction()
            editor.toolDone()
            editor.view.selectionManager.select(addedComponent)
            instance = null
        }
    }

    override fun mouseMoved(e: MouseEvent, x: Double, y: Double) {
        super.mouseMoved(e, x, y)
        if (instance != null) {
            val offset = editor.snapManager.snap(x, y)
            instance!!.setPointAt(instance!!.pointsCount - 1, x + offset.x, y + offset.y)
            instance!!.validate()
        }
    }
}