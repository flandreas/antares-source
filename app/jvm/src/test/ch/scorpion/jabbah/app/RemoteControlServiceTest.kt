package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.base.Properties
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RemoteControlServiceTest {

	companion object {
		init {
			AppTestRule.configure()
			AppModuleJvm.require()
		}
	}

	private val newVersionReader = mockk<(String) -> String>()
	private val properties = mockk<Properties>()
	private val service = RemoteControlService(properties, newVersionReader)

	init {
		AppModuleJvm.remotePropertiesUrl = "bla"
	}

	@Test
	fun shouldOfferNewVersion() {
		every { properties.getString(eq(RemoteControlService.PROP_IGNORED_VERSION)) } returns "0.0.0"
		every { newVersionReader.invoke(any()) } returns "app.currentVersion=0.4.0"

		val newerVersion = service.checkForNewerVersion(ApplicationVersion("0.3.1"))

		assertEquals("0.4.0", newerVersion.toString())
	}

	@Test
	fun shouldNotOfferIgnoredVersion() {
		every { properties.getString(eq(RemoteControlService.PROP_IGNORED_VERSION)) } returns "0.4.0"
		every { newVersionReader.invoke(any()) } returns "app.currentVersion=0.4.0"

		val newerVersion = service.checkForNewerVersion(ApplicationVersion("0.3.1"))

		assertNull(newerVersion)
	}

	@Test
	fun shouldNotComplainUponConnectionError() {
		every { properties.getString(eq(RemoteControlService.PROP_IGNORED_VERSION)) } returns "0.0.0"
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