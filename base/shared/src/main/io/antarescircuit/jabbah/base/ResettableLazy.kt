package io.antarescircuit.jabbah.base

/**
 * Factory method for [ResettableLazy].
 *
 * @param T the type of the [ResettableLazy]'s value.
 * @param initializer the function that initializes the value on its first usage
 */
fun <T> resettableLazy(initializer: () -> T) = ResettableLazy(initializer)

internal object UninitializedValue

/**
 * A [Lazy] implementation whose once initialized value can be [reset].
 *
 * @param T the type of the value
 * @param initializer the function that initializes the value on its first usage.
 */
class ResettableLazy<out T>(
	private val initializer: () -> T
) : Lazy<T> {

	private var _value: Any? = UninitializedValue

	override val value: T
		get() {
			if (_value === UninitializedValue) {
				_value = initializer()
			}
			@Suppress("UNCHECKED_CAST")
			return _value as T
		}

	override fun isInitialized(): Boolean = _value != UninitializedValue

	/**
	 * Resets the value of this [ResettableLazy] to [UninitializedValue].
	 * The next time the value is accessed it will be newly initialized using [initializer].
	 */
	fun reset() {
		_value = UninitializedValue
	}
}