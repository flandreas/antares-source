package io.antarescircuit.jabbah.app.rating

import io.antarescircuit.jabbah.app.AppTestRule
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.module.BaseModuleJvm
import io.antarescircuit.jabbah.base.time.ControlledTimeService
import io.antarescircuit.jabbah.edit.auth.DesktopUser
import io.antarescircuit.jabbah.edit.auth.DesktopUserHolder
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import java.time.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RailwayRatingServiceTest {

	private val timeService = ControlledTimeService()
	private val service = RailwayRatingService(timeService = timeService)

	@BeforeTest
	fun setup() {
		BaseModuleJvm.require()
		AppTestRule.configure()
		EditAuthModule.userHolder = DesktopUserHolder(DesktopUser.anybody)

		// Set now
		BaseModule.timeService = timeService
		BaseModule.settings.remove(RailwayRatingService.PROP_NEXT_RATING_DATE)
		timeService.setTimeMillis(System.currentTimeMillis())
	}

	@Test
	fun shouldRequireRatingWithoutNextRatingDate() {
		assertTrue(service.requiresRating())
	}

	@Test
	fun shouldNotRequireRatingBeforeNextRatingDate() {
		storeNextRatingDate(LocalDate.now().plusDays(1L))
		assertFalse(service.requiresRating())
	}

	@Test
	fun shouldRequireRatingAfterNextRatingDate() {
		storeNextRatingDate(LocalDate.now().minusDays(1L))
		assertTrue(service.requiresRating())
	}

	@Test
	fun shouldAskLater() {
		assertTrue(service.requiresRating())
		service.askLater()
		assertFalse(service.requiresRating())

		timeService.setTimeMillis(timeService.nowMillis() + (RailwayRatingService.ASK_ME_LATER_DAYS + 1) * 1_000 * 60 * 60 * 24)
		assertTrue(service.requiresRating())
	}

	private fun storeNextRatingDate(date: LocalDate) {
		BaseModule.settings.set(RailwayRatingService.PROP_NEXT_RATING_DATE, RailwayRatingService.DATE_FORMATTER.format(date))
	}
}