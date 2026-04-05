package io.antarescircuit.antares.model.testcase

class TestVector(
	val type: Type,
	val description: String,
	private val _values: Array<Value>
) : Iterable<Value> {

	enum class Type {
		Top,
		RunFirst,
		RunLine,
		RunLast
	}

	val values: List<Value> get() = _values.toList()

	fun getValue(index: Int): Value = _values[index]

	fun setValue(index: Int, value: Value) {
		_values[index] = value
	}

	fun isFailed(index: Int): Boolean = _values[index].state == Value.State.FAILED

	override fun iterator(): Iterator<Value> = _values.iterator()
}