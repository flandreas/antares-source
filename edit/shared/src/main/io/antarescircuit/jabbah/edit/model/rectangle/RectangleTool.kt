package io.antarescircuit.jabbah.edit.model.rectangle

import io.antarescircuit.jabbah.base.Status
import io.antarescircuit.jabbah.base.StatusType
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.base.event.MouseEvent
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.Tool
import io.antarescircuit.jabbah.edit.app.DrawingAppService
import io.antarescircuit.jabbah.edit.model.AbstractComponentTool
import io.antarescircuit.jabbah.edit.module.EditModule
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
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
	service: DrawingAppService = EditModule.drawingAppService,
	factory: () -> T,
	adder: (T) -> Component = { it },
	private val defaultWidth: Double = DEF_WIDTH,
	private val defaultHeight: Double = DEF_HEIGHT
) : AbstractComponentTool<T>(editor, service, factory, adder) {

	companion object {

		/** The default rectangle width used if dragging is omitted.*/
		const val DEF_WIDTH = 200.0

		/** The default rectangle height used if dragging is omitted.*/
		const val DEF_HEIGHT = 100.0

		/** The minimal width or height used to determine whether dragging is omitted.*/
		const val MINIMAL_SIZE = 3
	}

	/** Holds the instantiated rectangle. Initialized in [mousePressed].*/
	private var instance: T? = null

	/** The [Component] that is added to the [Drawing]. */
	private var addedComponent by Delegates.notNull<Component>()

	/** The location where the mouse is initially pressed.*/
	private var anchorLocation = Point2D.ZERO

	/** ---- [Tool] interface */

	override fun activate() {
		editor.view.setCursor(Cursor.CROSSHAIR)
		Status.set(StatusType.Tool, Translations.getString("edit.tool.rectangle.0.text"))
	}

	override fun mousePressed(e: MouseEvent, x: Double, y: Double) {
		super.mousePressed(e, x, y)

		instance = createComponent()

		val offset = editor.snapManager.snap(x, y)
		anchorLocation = Point2D(x + offset.x, y + offset.y)
		instance!!.setFrame(anchorLocation.x, anchorLocation.y, 0.0, 0.0)

		editor.view.selectionManager.deselectAll()
		addedComponent = getAddedComponent(instance!!)
		editor.drawing.add(addedComponent)
		editor.view.selectionManager.select(addedComponent)

		Status.set(StatusType.Tool, Translations.getString("edit.tool.rectangle.1.text"))
	}

	override fun mouseDragged(e: MouseEvent, x: Double, y: Double) {
		super.mouseDragged(e, x, y)

		val offset = editor.snapManager.snap(x, y)
		var width = x + offset.x - anchorLocation.x
		var height = y + offset.y - anchorLocation.y

		if (e.isShiftDown) {
			val size = max(width, height)
			width = size
			height = size
		}

		instance!!.setFrame(
			min(anchorLocation.x, anchorLocation.x + width),
			min(anchorLocation.y, anchorLocation.y + height),
			abs(width),
			abs(height)
		)

		instance!!.validate()

		reportSize()
	}

	override fun mouseReleased(e: MouseEvent, x: Double, y: Double) {
		super.mouseReleased(e, x, y)

		if (instance!!.width < MINIMAL_SIZE || instance!!.height < MINIMAL_SIZE) {
			instance!!.setFrame(anchorLocation.x, anchorLocation.y, defaultWidth, defaultHeight)
		}

		addComponent(addedComponent)

		editor.toolDone()
		instance = null
	}

	override fun keyPressed(e: KeyEvent) {
		if (e.key == KeyEvent.VK_ESCAPE) {
			cancel()
		}
	}

	private fun cancel() {
		if (instance != null) {
			instance = null
			editor.drawing.remove(addedComponent)
			editor.drawing.validate()
		}
		editor.toolDone()
	}

	private fun reportSize() {
		Status.set(StatusType.Small, "w=${instance!!.width.toInt()}, h=${instance!!.height.toInt()}")
	}
}