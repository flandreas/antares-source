package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.*

/**
 * Forwards interaction initiating input events to the [Drawable] at the current mouse location of a
 * [DrawableContainer].
 *
 * Keeps itself in front of interested target [InputEventHandler]s in order to relocate mouse event
 * coordinates if the corresponding [DrawableContainer] is not located at [Point2D.ZERO].
 */
open class DrawableContainerInputEventHandler<T : Drawable, C : InputEventContext> : InputEventHandlerAdapter<C>() {

	private var target: InputEventHandler<C>? = null

	private var _container: DrawableContainer<T>? = null

	protected val container: DrawableContainer<T> get() = _container!!

	/** ---- [InputEventHandler] interface */

	override fun mouseMoved(context: C): InputEventHandler<C>? =
		onTargetOrContainer(context, { h, c -> h.mouseMoved(c) } )

	override fun mousePressed(context: C): InputEventHandler<C>? =
		onTargetOrContainer(context, { h, c -> h.mousePressed(c) })

	override fun mouseDragged(context: C): InputEventHandler<C>? =
		onTargetOrContainer(context, { h, c -> h.mouseDragged(c) })

	override fun mouseReleased(context: C): InputEventHandler<C>? =
		onTargetOrContainer(context, { h, c -> h.mouseReleased(c) })

	override fun mouseClicked(context: C): InputEventHandler<C>? =
		onTargetOrContainer(context, { h, c -> h.mouseClicked(c) })

	/** ---- [DrawableContainerInputEventHandler] */

	fun useFor(container: DrawableContainer<T>) {
		_container = container
		target = null
	}

	private fun mightRemember(handler: InputEventHandler<C>?): InputEventHandler<C>? {
		this.target = handler
		return if (handler != null && container.location != Point2D.ZERO) this else handler
	}

	private fun onTargetOrContainer(context: C, handler: (InputEventHandler<C>, C) -> InputEventHandler<C>?): InputEventHandler<C>? {
		val localContext = getLocalContext(context)

		if (target != null) {
			return mightRemember(handler(target!!, localContext))
		}

		return getDrawableAt(context.location)?.let { drawable ->
			mightRemember(handler(handlerOfDrawable(drawable, localContext), localContext))
		} ?: mightRemember(null)
	}

	private fun getLocalContext(context: C): C {
		return if (container.location == Point2D.ZERO) {
			context
		} else {
			context.withXY(context.location.subtract(container.location)) as C
		}
	}

	/**
	 * Returns the [Drawable] of the [DrawableContainer] at the specified location.
	 * Defaults to the corresponding default method of [DrawableContainer].
	 * Subclasses might choose to override this method e.g. to restrict the set of possible [Drawable]
	 * to those of more specific types.
	 */
	protected open fun getDrawableAt(location: Point2D): Drawable? {
		return container.getDrawableAt(location)
	}

	/**
	 * Returns the [InputEventHandler] to be used for a particular target [Drawable].
	 * This implementation returns the [InputEventHandler] of the [Drawable] itself per default.
	 * Subclasses might choose to return other, e.g. inner [InputEventHandlers][InputEventHandler],
	 * inheriting the benefit of remembering the target for coordinate relocation done by this class.
	 */
	protected open fun handlerOfDrawable(drawable: Drawable, context: C): InputEventHandler<C> {
		return drawable.getInputEventHandler(context)
	}
}