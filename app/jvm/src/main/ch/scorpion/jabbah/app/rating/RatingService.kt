package ch.scorpion.jabbah.app.rating

import ch.scorpion.jabbah.app.railway.AbstractRailwayAppService
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Settings
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.TimeService
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

open class RailwayRatingService(
	properties: Properties = BaseModule.properties,
	settings: Settings = BaseModule.settings,
	private val timeService: TimeService = BaseModule.timeService
) : AbstractRailwayAppService(properties, settings), RatingService {

	private val client: HttpClient by lazy {
		HttpClient(Java) {
			install(JsonFeature) {
				val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
				serializer = KotlinxSerializer(json)
			}
		}
	}

	companion object {
		private val LOG by logger(RailwayRatingService::class)

		// Public for testing
		val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

		/** The number of days to wait after the user says "Ask me later". */
		const val ASK_ME_LATER_DAYS = 14L

		/** The number of days after the user is asked for a new rating.*/
		private const val NEXT_RATING_DAYS = 180L

		const val PROP_ASPECTS_URL = "ch.scorpion.jabbah.app.rating-aspects.url"
		const val PROP_RATING_URL = "ch.scorpion.jabbah.app.rating.url"
		const val PROP_NEXT_RATING_DATE = "ch.scorpion.jabbah.app.rating.nextDate"
	}

	override fun requiresRating(): Boolean =
		!EditAuthModule.userHolder.user.isDeveloper && (readNextRatingDate()?.isBefore(today()) ?: true)

	override suspend fun retrieveAspects(applicationId: String?): List<RatingAspect> {
		try {
			val projectId = getProjectId(applicationId)
			val url = "${properties.getString(PROP_ASPECTS_URL)}/$projectId"

			LOG.debug("Retrieving rating aspects from $url")

			val aspects: RatingAspects = client.get(url)
			return aspects.aspects
		} catch (e: Throwable) {
			LOG.error("Error while retrieving rating aspects", e)
			throw e
		}
	}

	override suspend fun sendRating(rating: Rating, applicationId: String?, userIdentifier: String?): Boolean {
		return try {
			val status = sendRatingImpl(rating, applicationId, userIdentifier)
			if (status == HttpStatusCode.Created) {
				storeNextRatingDate(today().plusDays(NEXT_RATING_DAYS))
				true
			} else {
				LOG.error("Received status code ${status.value} while sending rating")
				false
			}
		} catch (e: Throwable) {
			LOG.error("Error while sending rating", e)
			throw e
		}
	}

	override fun askLater() {
		storeNextRatingDate(today().plusDays(ASK_ME_LATER_DAYS))
	}

	protected open suspend fun sendRatingImpl(rating: Rating, applicationId: String?, userIdentifier: String?): HttpStatusCode {
		val url = properties.getString(PROP_RATING_URL)
		LOG.debug("Sending rating to $url")

		val response: HttpResponse =  client.post(url) {
			contentType(ContentType.Application.Json)
			body = createRatingRequest(rating, applicationId, userIdentifier)
		}
		return response.status
	}

	private fun readNextRatingDate(): LocalDate? =
		if (settings.containsKey(PROP_NEXT_RATING_DATE)) {
			LocalDate.parse(settings.get(PROP_NEXT_RATING_DATE), DATE_FORMATTER)
		} else {
			null
		}

	private fun storeNextRatingDate(date: LocalDate) {
		settings.set(PROP_NEXT_RATING_DATE, date.format(DATE_FORMATTER))
	}

	private fun today(): LocalDate =
		Instant.ofEpochMilli(timeService.nowMillis()).atZone(ZoneId.systemDefault()).toLocalDate()

	private fun createRatingRequest(rating: Rating, applicationId: String?, userIdentifier: String?): RatingRequest =
		RatingRequest(
			project = getProjectId(applicationId),
			identifier = userIdentifier ?: getIdentifier(),
			rating = rating.overallRating,
			comment = rating.remark,
			most = rating.likeMost.id,
			least = rating.likeLeast.id)
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