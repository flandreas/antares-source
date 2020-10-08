package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.graphics.Icon
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

/**
 * Calls the specified action when the user clicks the icon while editing.
 */
open class IconButton<C: InputEventContext>(
	icon: Icon,
	private val action: () -> Unit,
	location: Point2D = Point2D.ZERO,
	tooltipKey: String? = null,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractIconButton(icon, location, tooltipKey, styleProvider) {

	private val handler = createEditInteractionHandler()

	/** ---- [AbstractDrawable] */

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> =
		handler as InputEventHandler<T>

	/** ---- [IconButton] */

	protected open fun createEditInteractionHandler(): InputEventHandler<C> = EditInteractionHandler()

	protected open inner class EditInteractionHandler : InputEventHandlerAdapter<C>() {

		override fun mouseMoved(context: C): InputEventHandler<C>? {
			return if (keepMouseMoved(context.location)) this else null
		}

		override fun mousePressed(context: C): InputEventHandler<C>? {
			// avoid involvement of surrounding SelectionTool
			return this
		}

		override fun mouseClicked(context: C): InputEventHandler<C>? {
			if (enabled) {
				context.mouseEvent?.consume()
				isHovering = false
				action.invoke()
			}
			return null
		}
	}
}