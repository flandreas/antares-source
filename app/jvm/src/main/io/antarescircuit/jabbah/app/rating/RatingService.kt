package io.antarescircuit.jabbah.app.rating

import io.antarescircuit.jabbah.base.Bean
import kotlinx.serialization.Serializable

/**
 * A service allowing users to rate the current application.
 */
interface RatingService {

	/**
	 * Determines whether a rating is currently required. Depends on the date of the last
	 * rating and the configured interval between multiple ratings.
	 */
	fun requiresRating(): Boolean

	/**
	 * Retrieves the configured [RatingAspect] for the current application from the server.
	 */
	suspend fun retrieveAspects(applicationId: String? = null): List<RatingAspect>

	/**
	 * Sends a [Rating] the user has given to the server.
	 * @return `true` if sending was successful
	 */
	suspend fun sendRating(rating: Rating, applicationId: String? = null, userIdentifier: String? = null): Boolean

	/**
	 * Handles the decision of the user to be asked later for a [Rating].
	 */
	fun askLater()
}

@Serializable
data class RatingRequest(
	val project: String,
	val identifier: String,
	val rating: Int,
	val comment: String? = null,
	val most: Int,
	val least: Int
) : Bean
