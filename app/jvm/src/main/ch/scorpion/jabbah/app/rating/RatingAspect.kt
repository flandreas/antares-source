package ch.scorpion.jabbah.app.rating

import kotlinx.serialization.Serializable

/**
 * An aspect of the application to be rated by a user.
 *
 * [RatingAspects][RatingAspect] are configured in the backend and fetched to be presented
 * to and chosen by the user.
 *
 * @property name the name of the [RatingAspect], e.g. "Bugs"
 * @property positive the positive formulation of this [RatingAspect], e.g. "Few bugs"
 * @property negative the negative formulation of this [RatingAspect], e.g. "Many bugs"
 */
@Serializable
data class RatingAspect(
	val name: String,
	val positive: String,
	val negative: String
)