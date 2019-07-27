package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.draw.*

/**
 * Forwards interaction initiating input events to the [Drawable] at the current mouse location of a
 * [DrawableContainer].
 */
open class DrawableContainerInputEventHandler<T : Drawable, C : InputEventContext>() : InputEventHandlerAdapter<C>() {

	private var _container: DrawableContainer<T>? = null

	protected val container: DrawableContainer<T> get() = _container!!

	fun useFor(container: DrawableContainer<T>) {
		_container = container
	}

	override fun mouseMoved(context: C): InputEventHandler<C>? {
		val drawable = container.getDrawableAt(context.x, context.y)
		if (drawable != null) {
			val localContext = localContext(context)
			return drawable.getInputEventHandler(localContext).mouseMoved(localContext)
		}
		return null
	}

	override fun mousePressed(context: C): InputEventHandler<C>? {
		val drawable = container.getDrawableAt(context.x, context.y)
		if (drawable != null) {
			val localContext = localContext(context)
			return drawable.getInputEventHandler(localContext).mousePressed(localContext)
		}
		return null
	}

	override fun mouseDragged(context: C): InputEventHandler<C>? {
		val drawable = container.getDrawableAt(context.x, context.y)
		if (drawable != null) {
			val localContext = localContext(context)
			return drawable.getInputEventHandler(localContext).mouseDragged(localContext)
		}
		return null
	}

	override fun mouseReleased(context: C): InputEventHandler<C>? {
		val drawable = container.getDrawableAt(context.x, context.y)
		if (drawable != null) {
			val localContext = localContext(context)
			return drawable.getInputEventHandler(localContext).mouseReleased(localContext)
		}
		return null
	}


	override fun mouseClicked(context: C): InputEventHandler<C>? {
		val drawable = container.getDrawableAt(context.x, context.y)
		if (drawable != null) {
			val localContext = localContext(context)
			return drawable.getInputEventHandler(localContext).mouseClicked(localContext)
		}
		return null
	}

	@Suppress("UNCHECKED_CAST")
	private fun localContext(c: C): C = c.withXY(c.location.subtract(container.location)) as C
}