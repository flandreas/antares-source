package ch.scorpion.jabbah.app.rating

import ch.scorpion.jabbah.edit.Bean
import kotlinx.serialization.Serializable

/**
 * An aspect of the application to be rated by a user.
 *
 * [RatingAspects][RatingAspect] are configured in the backend and fetched to be presented
 * to and chosen by the user.
 *
 * @property id the ID of the [RatingAspect], e.g. "1"
 * @property positive the positive formulation of this [RatingAspect], e.g. "Few bugs"
 * @property negative the negative formulation of this [RatingAspect], e.g. "Many bugs"
 */
@Serializable
data class RatingAspect(
	val id: Int,
	val positive: String,
	val negative: String
) : Bean

@Serializable
data class RatingAspects(
	val aspects: List<RatingAspect>
) : Bean