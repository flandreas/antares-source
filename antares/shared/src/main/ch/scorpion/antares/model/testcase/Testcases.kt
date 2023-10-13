package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.io.*

data class TestcaseAddedEvent(val graph: DigitalGraph, val testcase: Testcase)
data class TestcaseRemovedEvent(val graph: DigitalGraph, val testcase: Testcase)

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
	 * Used for sending events when adding or removing [Testcase]s.
	 */
	var graph: DigitalGraph? = graph

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
		_testcases.add(index, testcase)
		eventBus.post(TestcaseAddedEvent(graph!!, testcase))
	}

	fun remove(testcase: Testcase) {
		_testcases.remove(testcase)
		eventBus.post(TestcaseRemovedEvent(graph!!, testcase))
	}

	fun remove(testcaseId: Int) {
		remove(get(testcaseId))
	}

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