package ch.scorpion.jabbah.base

fun <T> resettableLazy(initializer: () -> T) = ResettableLazy(initializer)

internal object UninitializedValue

class ResettableLazy<out T>(private val initializer: () -> T) : Lazy<T> {
	private var _value: Any? = UninitializedValue

	override val value: T
		get() {
			if (_value === UninitializedValue) {
				_value = initializer()
			}
			return _value as T
		}

	override fun isInitialized(): Boolean = _value != UninitializedValue

	fun reset() {
		_value = UninitializedValue
	}
}