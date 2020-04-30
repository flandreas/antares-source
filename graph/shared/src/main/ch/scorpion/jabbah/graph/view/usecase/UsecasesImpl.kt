package ch.scorpion.jabbah.graph.view.usecase

import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.io.*

/** A standard implementation of the [Usecases] interface.*/
class UsecasesImpl(
	override var graphView: GraphView? = null,
	private val eventBus: EventBus = BaseModule.eventBus
) : Usecases {

	private var isLoading: Boolean = false
	private val usecases: MutableList<Usecase> by lazy { mutableListOf<Usecase>() }

	/** ---- [Usecases] interface */

	override val isEmpty: Boolean get() = usecases.isEmpty()

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
		usecases.add(index, usecase)
		eventBus.post(UsecaseAddedEvent(graphView!!, usecase))
	}

	override fun remove(usecase: Usecase) {
		usecases.remove(usecase)
		eventBus.post(UsecaseRemovedEvent(graphView!!, usecase))
	}

	override fun indexOfUsecase(usecase: Usecase): Int = usecases.indexOf(usecase)

	override fun withTests(): List<Usecase> = usecases.filter { it.hasTest }

	/** ---- [Storable] interface */

	override var storableId: Int = 0

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		// empty
	}

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

	override fun getStorableChildren(): Iterator<Storable> {
		return EmptyIterator()
	}

	/** ---- [UsecaseImpl] */

	private fun getMaxId(): Int {
		if (usecases.size == 0) {
			return 0
		}
		return usecases.maxBy { it.id }!!.id
	}
}