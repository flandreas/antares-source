package ch.scorpion.jabbah.app.rating

import ch.scorpion.jabbah.app.railway.AbstractRailwayAppService
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Settings
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.TimeService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

interface RatingService {

	fun requiresRating(): Boolean

	suspend fun retrieveAspects(applicationId: String? = null): List<RatingAspect>

	suspend fun sendRating(rating: Rating, applicationId: String? = null, userIdentifier: String? = null)

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
		readNextRatingDate()?.isBefore(today()) ?: true

	override suspend fun retrieveAspects(applicationId: String?): List<RatingAspect> {
		val projectId = getProjectId(applicationId)
		val url = "${properties.getString(PROP_ASPECTS_URL)}/$projectId"

		LOG.info("Fetching rating aspects from $url")

		val response: HttpResponse = client.get(url)
		val body: ByteArray = response.receive()

		println(body)

		return emptyList()
	}

	override suspend fun sendRating(rating: Rating, applicationId: String?, userIdentifier: String?) {
		try {
			sendRatingImpl(rating, applicationId, userIdentifier)
			storeNextRatingDate(today().plusDays(NEXT_RATING_DAYS))
		} catch (e: Throwable) {
			throw e
		}
	}

	override fun askLater() {
		storeNextRatingDate(today().plusDays(ASK_ME_LATER_DAYS))
	}

	protected open fun sendRatingImpl(rating: Rating, applicationId: String?, userIdentifier: String?) {
		TODO()
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
}

class DummyRatingService : RailwayRatingService() {

	override suspend fun retrieveAspects(applicationId: String?): List<RatingAspect> {
		return listOf(
			RatingAspect("Bugs", "Few bugs", "Many bugs"),
			RatingAspect("Features", "Rich feature set", "Missing features"),
			RatingAspect("Performance", "Good performance", "Bad performance"),
			RatingAspect("Usability", "Easy to use", "Awkward to use"),
			RatingAspect("Other", "Other", "Other")
		)
	}

	override fun sendRatingImpl(rating: Rating, applicationId: String?, userIdentifier: String?) {
		// Do nothing
	}
}