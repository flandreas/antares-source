package io.antarescircuit.jabbah.graph.model.vertice

import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.Vertice

/**
 * Represents a link to a [Vertice] in a [Graph], potentially crossing several [SubGraphVertice]s.
 * Contains an ordered list of the IDs of all [Vertice]s that are visited along the path to the referenced [Vertice].
 * Designed to be immutable.
 */
class DeepVerticeLink(verticeIds: List<Int>): VerticeLink {

	constructor(verticeId: Int): this(listOf(verticeId))
	constructor(): this(listOf())

	companion object {

		private val LOG by logger(DeepVerticeLink::class)

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

	/** Returns a copy of this [DeepVerticeLink] that contains the entire path without the last ID.*/
	fun withoutLast(): DeepVerticeLink {
		return DeepVerticeLink(verticeIds.subList(0, size - 1))
	}

	/** Appends the specified ID at the end of path and returns the result as a new [DeepVerticeLink].*/
	fun append(id: Int): DeepVerticeLink =
		DeepVerticeLink(listOf(*verticeIds.toTypedArray(), id))

	/** Prepends the specified ID at the beginning of the paths and returns the result as a new [DeepVerticeLink] */
	fun prepend(id: Int): DeepVerticeLink =
		DeepVerticeLink(listOf(id, *verticeIds.toTypedArray()))

	override fun getLinkedObject(startGraph: Graph?): Vertice {
		var link = this
		var graph = startGraph
		while (link.size > 1) {
			val id = link.first
			val element = graph?.withId(id) as SubGraphVertice?
			if (element == null) {
				LOG.warn("DeepVerticeLink broken: Cannot find element $id in graph '${graph?.name}'")
				throw IllegalArgumentException()
			}
			graph = element.getGraph()
			link = link.withoutFirst()
		}
		val id = link.first
		val vertice = graph?.withId(id) as Vertice?
		if (vertice == null) {
			LOG.warn("DeepVerticeLink broken: Cannot find referenced Control with model ID $id")
			throw IllegalArgumentException()
		}
		return vertice
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