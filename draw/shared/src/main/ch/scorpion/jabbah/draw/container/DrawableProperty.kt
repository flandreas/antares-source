package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.draw.Drawable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A base delegate property class for implementing  [Drawable] properties
 * that performs the necessary invalidation and validation when a value changes.
 *
 * @property afterSet the additional code to be executed after a value has changed,
 * but before the second invalidation takes place. Used to call custom code that changes
 * the bounding box of the [Drawable].
 */
abstract class AbstractDrawableProperty<V : Drawable, T>(
	initialValue: T,
	private val afterSet: (() -> Unit)? = null
) : ReadWriteProperty<V, T> {

	private var value = initialValue

	override fun getValue(thisRef: V, property: KProperty<*>): T = value

	override fun setValue(thisRef: V, property: KProperty<*>, value: T) {
		if (this.value != value) {
			thisRef.invalidate()
			this.value = value
			afterSet?.invoke()
			thisRef.invalidate()
			thisRef.validate()

			afterChange(thisRef, property, value)
		}
	}

	protected abstract fun afterChange(thisRef: V, property: KProperty<*>, value: T)
}

open class DrawableProperty<V : Drawable, T>(
	initialValue: T,
	afterSet: (() -> Unit)? = null
) : AbstractDrawableProperty<V, T>(initialValue, afterSet) {

	override fun afterChange(thisRef: V, property: KProperty<*>, value: T) { }
}

/**
 * An [AbstractDrawableProperty] that calls [Drawable.update] after a value has changed.
 */
open class DrawableGeometryProperty<V : Drawable, T>(
	initialValue: T,
	afterSet: (() -> Unit)? = null
) : AbstractDrawableProperty<V, T>(initialValue, afterSet) {

	override fun afterChange(thisRef: V, property: KProperty<*>, value: T) {
		thisRef.update()
	}
}