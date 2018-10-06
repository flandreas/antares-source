package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.style.Style

/**
 * Thickness: thin, normal, thick
 * Style: dotted, solid, dashed
 */
enum class PredefinedStrokeIdentity(val id: String) {
	ThinDotted("thin-dotted"),
	ThinSolid("thin-solid"),
	ThinDashed("thin-dashed"),
	NormalDotted("normal-dotted"),
	NormalSolid("normal-solid"),
	NormalDashed("normal-dashed"),
	ThickDotted("thick-dotted"),
	ThickSolid("thick-solid"),
	ThickDashed("thick-dashed");

	companion object {
		fun withId(id: String): PredefinedStrokeIdentity = PredefinedStrokeIdentity.values().first { it.id == id }
	}
}

/**
 * A [PredefinedStroke] is a [Stroke] with a particual ID that has been predefined by the developer of an
 * application and that is part of a set of harmonic strokes from which the user can choose to
 * override a figure's [Style] stroke.
 */
data class PredefinedStroke(val identity: PredefinedStrokeIdentity, val stroke: Stroke)

/** Provides access to [PredefinedStroke]s. */
interface PredefinedStrokeProvider {

	/** Returns the [List] of all known [PredefinedStroke]s. */
	fun provideAll(): List<PredefinedStroke>

	/** Returns the [PredefinedStroke] with the specified [PredefinedStrokeIdentity] id, or `null` if not available.*/
	fun withId(id: String): PredefinedStroke?

	/** Returns the [PredefinedStroke] with the specified [PredefinedStrokeIdentity], or `null` if not available.*/
	fun withIdentity(identity: PredefinedStrokeIdentity): PredefinedStroke?
}

/**
 * An implementation of a [PredefinedStrokeProvider] that allows to register [PredefinedStroke]s programmatically.
 * Currently implemented as a singleton object because no multi-target DI container is available.
 */
object PredefinedStrokeRepository : PredefinedStrokeProvider {

	private val LOG by logger(PredefinedColorRepository::class)

	/** Contains all registered [PredefinedStroke]s in a stable sort order. */
	private val strokesList: MutableList<PredefinedStroke> by lazy { mutableListOf<PredefinedStroke>() }

	/** Maps all registered [PredefinedStroke]s for fast access by [PredefinedStrokeIdentity]. */
	private val strokes: MutableMap<PredefinedStrokeIdentity, PredefinedStroke> by lazy { mutableMapOf<PredefinedStrokeIdentity, PredefinedStroke>() }


	/** ---- [PredefinedStrokeProvider] interface */

	override fun provideAll(): List<PredefinedStroke> {
		return strokesList.toImmutableList()
	}

	override fun withId(id: String): PredefinedStroke? {
		return strokes[PredefinedStrokeIdentity.withId(id)]
	}

	override fun withIdentity(identity: PredefinedStrokeIdentity): PredefinedStroke? {
		return strokes[identity]
	}

	/** ---- [PredefinedColorRepository] */

	fun register(stroke: PredefinedStroke) {
		if (containsId(stroke.identity.id)) {
			LOG.warn("PredefinedStroke with id ${stroke.identity.id} already registered, replacing")
			strokesList[strokesList.indexOf(withId(stroke.identity.id))] = stroke
		} else {
			strokesList.add(stroke)
		}
		strokes[stroke.identity] = stroke
	}

	private fun containsId(id: String): Boolean = withId(id) != null

}