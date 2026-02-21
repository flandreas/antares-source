package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.io.*

data class TestcaseAddedEvent(val graph: DigitalGraph, val testcase: Testcase)
data class TestcaseRemovedEvent(val graph: DigitalGraph, val testcase: Testcase)
data class TestcaseMovedEvent(val graph: DigitalGraph, val testcase: Testcase, val index: Int)

/**
 * A collection of [Testcase]s owned by a [DigitalGraph].
 */
class Testcases(
	graph: DigitalGraph? = null,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractStorable() {

	private var isLoading: Boolean = false

	private val _testcases: MutableList<Testcase> by lazy { mutableListOf() }
	val testcases: List<Testcase> get() = _testcases

	val isEmpty: Boolean get() = _testcases.isEmpty()

	/**
	 * The [DigitalGraph] owning this [Testcases]. Can be `null` in order to be instantiated by deserialization.
	 * Used for sending events when adding or removing [Testcase]s, and for promoting it to the inner
	 * [Testcase]s.
	 */
	var graph: DigitalGraph? = graph
		set(value) {
			field = value
			_testcases.forEach { it.graph = value }
		}

	fun get(id: Int): Testcase {
		return testcases.first { it.id == id }
	}

	fun add(testcase: Testcase) {
		add(testcase, testcases.size)
	}

	fun add(testcase: Testcase, index: Int) {
		if (!isLoading) {
			testcase.id = getMaxId() + 1
		}
		testcase.graph = graph
		_testcases.add(index, testcase)
		eventBus.post(TestcaseAddedEvent(graph!!, testcase))
	}

	fun remove(testcase: Testcase) {
		testcase.graph = null
		_testcases.remove(testcase)
		eventBus.post(TestcaseRemovedEvent(graph!!, testcase))
	}

	fun remove(testcaseId: Int) {
		remove(get(testcaseId))
	}

	fun move(testcaseId: Int, index: Int) {
		val testcase = get(testcaseId)
		val oldIndex = _testcases.indexOf(testcase)
		val effIndex = if (oldIndex <= index) index - 1 else index
		_testcases.remove(testcase)
		_testcases.add(effIndex, testcase)
		eventBus.post(TestcaseMovedEvent(graph!!, testcase, index))
	}

	fun indexOfTestcase(testcaseId: Int): Int = testcases.indexOfFirst { it.id == testcaseId }

	private fun getMaxId(): Int {
		if (testcases.isEmpty()) {
			return 0
		}
		return testcases.maxByOrNull { it.id }!!.id
	}

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeStorables("testcases", _testcases.iterator())
	}

	override fun read(reader: StoreReader) {
		try {
			isLoading = true

			reader.readStorables<Testcase>("testcases").forEach {
				_testcases.add(it)
			}
		} finally {
			isLoading = false
		}
	}
}