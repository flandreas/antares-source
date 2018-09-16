package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.io.StorableCreator

/**
 * Represents a link to a [Vertice] in a [Graph], potentially crossing several [SubGraphVertice]s.
 * Contains an ordered list of the IDs of all [Vertice]s that are visited along the path to the referenced [Vertice].
 * Designed to be immutable.
 */
class DeepVerticeLink(verticeIds: List<Int>) {

	constructor(id: Int): this(listOf(id))
	constructor(): this(listOf())

	companion object {

		/** The delimiter that separates individual IDs in the store format.*/
		private const val DELIMITER = '/'

		val EMPTY = DeepVerticeLink(listOf())

		fun fromStoreFormat(s: String): DeepVerticeLink {
			return if (StringUtils.isEmpty(s)) EMPTY else DeepVerticeLink(s.split(DELIMITER).map { it.toInt() })
		}

		fun toStoreFormat(link: DeepVerticeLink): String {
			return link.toStoreFormat()
		}
	}

	private val verticeIds: List<Int> = verticeIds.toList()

	val size: Int get() = verticeIds.size

	val empty: Boolean get() = verticeIds.isEmpty()

	val first: Int get() = verticeIds.first()

	val last: Int get() = verticeIds.last()

	fun toStoreFormat(): String {
		return verticeIds.joinToString("$DELIMITER")
	}

	/** Returns a copy of this [DeepVerticeLink] that contains the entire path without the first ID.*/
	fun withoutFirst(): DeepVerticeLink {
		return DeepVerticeLink(verticeIds.subList(1, size))
	}

	/** Appends the specified ID at the end of path and returns the result as a new [DeepVerticeLink].*/
	fun append(id: Int): DeepVerticeLink {
		return DeepVerticeLink(listOf(*verticeIds.toTypedArray(), id))
	}

	/**
	 * Returns the [Vertice] that this [DeepVerticeLink] is pointing to, starting with the specified [Graph].
	 * @param startGraph the [Graph] where resolving is started
	 * @param repository used for instantiation of sub [Graph]s
	 * @param storableCreator used for instantiation of sub [Graph]s
	 * @throws IllegalArgumentException if any of the [Vertice]s in the referencing path cannot be resolved
	 */
	fun getLinkedVertice(startGraph: Graph, repository: MetaGraphRepository, storableCreator: StorableCreator): Vertice {
		var link = this
		var graph = startGraph
		while (link.size > 1) {
			val element = graph.withId(link.first) as SubGraphVertice
			graph = element.getGraph(repository, storableCreator)
			link = link.withoutFirst()
		}
		return graph.withId(link.first) as Vertice
	}

	/** ---- [Any] */

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is DeepVerticeLink) return false

		if (verticeIds != other.verticeIds) return false

		return true
	}

	override fun hashCode(): Int {
		return verticeIds.hashCode()
	}
}