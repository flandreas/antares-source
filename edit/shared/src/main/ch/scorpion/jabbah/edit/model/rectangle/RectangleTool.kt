package ch.scorpion.jabbah.edit.model.rectangle

import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Tool
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.editor.AddCommand
import ch.scorpion.jabbah.edit.model.AbstractComponentTool
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.base.Math
import kotlin.properties.Delegates

@Suppress("unused")
/**
 * A [Tool] for interactively creating a [RectangularComponent] in a [Drawing].
 *
 * @param editor the [Editor] that uses this [Tool]
 * @property factory creates the [RectangularComponent] to be added to the [Drawing]
 */
class RectangleTool<T : RectangularComponent>(
    editor: Editor,
    factory: () -> T,
    adder: (T) -> Component,
    val defaultWidth: Double,
    val defaultHeight: Double
) : AbstractComponentTool<T>(editor, factory, adder) {

    constructor(editor: Editor, factory: () -> T): this(editor, factory, { it })
    constructor(editor: Editor, factory: () -> T, adder: (T) -> Component): this(editor, factory, adder, DEF_WIDTH, DEF_HEIGHT)

    companion object {

        /** The default rectangle width used if dragging is omitted.*/
        val DEF_WIDTH = 200.0

        /** The default rectangle height used if dragging is omitted.*/
        val DEF_HEIGHT = 100.0

        /** The minimal width or height used to determine whether dragging is omitted.*/
        val MINIMAL_SIZE = 3
    }

    /** Holds the instantiated rectangle. Initialized in [mousePressed].*/
    private var instance by Delegates.notNull<T>()

    /** The [Component] that is added to the [Drawing]. */
    private var addedComponent by Delegates.notNull<Component>()

    /** The location where the mouse is initially pressed.*/
    private val anchorLocation = Point2D()

    /** ---- [Tool] interface */

    override fun activate() {
        editor.view.setCursor(Cursor.CROSSHAIR)
    }

    override fun mousePressed(e: MouseEvent, x: Double, y: Double) {
        super.mousePressed(e, x, y)

        instance = createComponent()

        val offset = editor.snapManager.snap(x, y)
        anchorLocation.setLocation(x + offset.x, y + offset.y)
        instance.setFrame(anchorLocation.x, anchorLocation.y, 0.0, 0.0)

        editor.view.selectionManager.deselectAll()
        addedComponent = getAddedComponent(instance)
        editor.drawing.add(addedComponent)
        editor.view.selectionManager.select(addedComponent)
    }

    override fun mouseDragged(e: MouseEvent, x: Double, y: Double) {
        super.mouseDragged(e, x, y)

        val offset = editor.snapManager.snap(x, y)
        val width = x + offset.x - anchorLocation.x
        val height = y + offset.y - anchorLocation.y

        instance.setFrame(
            Math.min(anchorLocation.x, anchorLocation.x + width),
            Math.min(anchorLocation.y, anchorLocation.y + height),
            Math.abs(width),
            Math.abs(height)
        )

        instance.validate()
    }

    override fun mouseReleased(e: MouseEvent, x: Double, y: Double) {
        super.mouseReleased(e, x, y)

        if (instance.width < MINIMAL_SIZE || instance.height < MINIMAL_SIZE) {
            instance.setFrame(anchorLocation.x, anchorLocation.y, defaultWidth, defaultHeight)
        }

        editor.view.selectionManager.select(addedComponent)
        editor.commandManager.beginTransaction(AddCommand(editor, addedComponent))
        editor.commandManager.commitTransaction()
        editor.toolDone()
    }
}