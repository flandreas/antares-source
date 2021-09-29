package ch.scorpion.jabbah.graph.view.usecase

import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.io.*

/** A standard implementation of the [Usecases] interface.*/
class UsecasesImpl(
	graphView: GraphView? = null,
	private val eventBus: EventBus = BaseModule.eventBus
) : Usecases {

	private var isLoading: Boolean = false
	private val usecases: MutableList<Usecase> by lazy { mutableListOf() }

	/** ---- [Usecases] interface */

	override val isEmpty: Boolean get() = usecases.isEmpty()

	override var graphView: GraphView? = graphView
		set(value) {
			field = value
			usecases.forEach { it.graphView = value }
		}

	override fun executionStart(graphView: GraphView, signalHandler: SignalHandler) {
		usecases.forEach { it.executionStart(graphView, signalHandler) }
	}

	override fun dispose() {
		usecases.forEach { it.dispose() }
		usecases.clear()
	}

	override fun getUsecases(): Iterable<Usecase> {
		return usecases.toImmutableList()
	}

	override fun get(id: Int): Usecase {
		return usecases.first { it.id == id }
	}

	override fun add(name: String) {
		add(UsecaseImpl(name))
	}

	override fun add(usecase: Usecase) {
		add(usecase, usecases.size)
	}

	override fun add(usecase: Usecase, index: Int) {
		if (!isLoading) {
			usecase.id = getMaxId() + 1
		}
		usecase.graphView = graphView
		usecases.add(index, usecase)
		eventBus.post(UsecaseAddedEvent(graphView!!, usecase))
	}

	override fun remove(usecase: Usecase) {
		usecase.graphView = null
		usecases.remove(usecase)
		eventBus.post(UsecaseRemovedEvent(graphView!!, usecase))
	}

	override fun remove(usecaseId: Int) {
		remove(get(usecaseId))
	}

	override fun indexOfUsecase(usecase: Usecase): Int = usecases.indexOf(usecase)

	override fun withTests(): List<Usecase> = usecases.filter { it.hasTest }

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeStorables("usecases", usecases.iterator())
	}

	override fun read(reader: StoreReader) {
		try {
			isLoading = true

			// Backward compatibility: Usecase.id was introduced after version 0.1
			reader.readStorables<Usecase>("usecases").forEach {
				if (it.id == 0) {
					it.id = getMaxId() + 1
				}
				usecases.add(it)
			}
		} finally {
			isLoading = false
		}
	}

	/** ---- [UsecaseImpl] */

	private fun getMaxId(): Int {
		if (usecases.size == 0) {
			return 0
		}
		return usecases.maxByOrNull { it.id }!!.id
	}
}