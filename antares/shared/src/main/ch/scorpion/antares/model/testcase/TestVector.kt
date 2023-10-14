package ch.scorpion.antares.model.testcase

class TestVector(private val _values: Array<Value>) : Iterable<Value> {

	val values: List<Value> get() = _values.toList()

	fun getValue(index: Int): Value = _values[index]

	fun setValue(index: Int, value: Value) {
		_values[index] = value
	}

	override fun iterator(): Iterator<Value> = _values.iterator()
}