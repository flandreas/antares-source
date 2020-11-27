package ch.scorpion.jabbah.base.ui

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus

/**
 * Displays a data model and allows the user to operate on that data.
 *
 * A [UIView] is implemented for a specific platform, such as Swing or JS/browser,
 * and are therefore part of Kotlin platform-specific modules.
 *
 * A constructor of a [UIView] implementation will typically receive a reference to the
 * [UIController] that controls this [UIView], and will use that reference to register itself
 * as [UIView] of that [UIController]. It could also use that reference to get [Actions][Action]
 * and offer them to the user for execution using menu items or buttons. However, a [UIView]
 * typically doesn't keep a reference to the [UIController].
 *
 * Unlike in other architectures that contain a UI for only one single platform, where a [UIView]
 * would listen for model changes by itself, it is the responsibility of the [UIController] to
 * listen for such changes and then ask the [UIView] to update accordingly. This eliminates the
 * need to implement this kind of logic in all [UIView] implementations on the different platforms.
 * However, this is only a rule of thumb. A [UIView] might decide to observe the model by itself
 * in order to implement more fine-grained update or refresh scenarios.
 */
interface UIView {

	/**
	 * Called by the owning [UIController] when this [UIView] is not used any more.
	 * Typical implementations will unregister any [EventBus] registrations.
	 */
	fun dispose()
}

/**
 * Controls a data model and a [UIView] that displays the data.
 *
 * [UIControllers][UIController] are platform-agnostic and therefore implemented in
 * Kotlin common code modules.
 *
 * A [UIController] can instantiate and contain other [UIController] for inner sub-views.
 * This allows isolated testing of a [UIController] without involvement of its concrete [UIView].
 * The outermost [UIController] in a [UIView] hierarchy is typically
 * instantiated by the application object.
 *
 * @param T the type of view controlled by this [UIController].
 */
interface UIController<T : UIView> {

	/**
	 * The [UIView] controlled by this [UIController]. This reference is primarily used to set
	 * the model to be displayed by the [UIView].
	 */
	var view: T

	/**
	 * Called by the owning [UIController] if this [UIController] and its [UIView] is not used any more.
	 * Typical implementations will unregister any [EventBus] registrations and call [UIController.dispose] on
	 * all inner [UIControllers][UIController], as well as
	 */
	fun dispose()
}

abstract class AbstractUIController<T : UIView> : UIController<T> {

	private var _view: T? = null

	override var view: T
		get() = _view ?: throw IllegalStateException("access to uninitialized view property")
		set(value) {
			_view = value
			onViewInitialized()
		}

	override fun dispose() {
		view.dispose()
	}

	/** Called by this class after [view] has been set.*/
	protected open fun onViewInitialized() { }
}