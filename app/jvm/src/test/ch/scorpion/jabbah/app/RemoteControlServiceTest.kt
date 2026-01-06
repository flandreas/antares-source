package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.base.Properties
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RemoteControlServiceTest {

	private val newVersionReader = mock<(String) -> String>()
	private val properties = Properties()
	private val service = RemoteControlService(properties, newVersionReader)

	init {
		AppTestRule.configure()
		AppModuleJvm.require()
		AppModuleJvm.remotePropertiesUrl = "bla"
	}

	@Test
	fun shouldOfferNewVersion() {
		properties.set(RemoteControlService.PROP_IGNORED_VERSION, "0.0.0")
		every { newVersionReader.invoke(any()) } returns "app.currentVersion=0.4.0"

		val newerVersion = service.checkForNewerVersion(ApplicationVersion("0.3.1"))

		assertEquals("0.4.0", newerVersion.toString())
	}

	@Test
	fun shouldNotOfferIgnoredVersion() {
		properties.set(RemoteControlService.PROP_IGNORED_VERSION, "0.4.0")
		every { newVersionReader.invoke(any()) } returns "app.currentVersion=0.4.0"

		val newerVersion = service.checkForNewerVersion(ApplicationVersion("0.3.1"))

		assertNull(newerVersion)
	}

	@Test
	fun shouldNotComplainUponConnectionError() {
		properties.set(RemoteControlService.PROP_IGNORED_VERSION, "0.0.0")
		every { newVersionReader.invoke(any()) } throws RuntimeException("network error")

		val newerVersion = service.checkForNewerVersion(ApplicationVersion("0.3.1"))

		assertNull(newerVersion)
	}

	@Test
	fun shouldReturnCustomBooleanTrueProperty() {
		every { newVersionReader.invoke(any()) } returns """
			app.currentVersion=0.4.0
			myApp.property=true
		""".trimIndent()

		assertTrue(service.getBoolean("myApp.property"))
	}

	@Test
	fun shouldReturnCustomBooleanFalseProperty() {
		every { newVersionReader.invoke(any()) } returns """
			app.currentVersion=0.4.0
			myApp.property=false
		""".trimIndent()

		assertFalse(service.getBoolean("myApp.property"))
	}

	@Test
	fun shouldReturnDefaultForAbsentBooleanParameter() {
		every { newVersionReader.invoke(any()) } throws RuntimeException("network error")
		assertFalse(service.getBoolean("myApp.property"))
	}
}