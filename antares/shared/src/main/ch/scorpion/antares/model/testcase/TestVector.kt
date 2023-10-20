package ch.scorpion.antares.model.testcase

class TestVector(
	val description: String,
	private val _values: Array<Value>
) : Iterable<Value> {

	val values: List<Value> get() = _values.toList()

	fun getValue(index: Int): Value = _values[index]

	fun setValue(index: Int, value: Value) {
		_values[index] = value
	}

	fun isFailed(index: Int): Boolean = _values[index].state == Value.State.FAILED

	override fun iterator(): Iterator<Value> = _values.iterator()
}