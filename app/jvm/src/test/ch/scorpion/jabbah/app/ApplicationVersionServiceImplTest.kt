package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.base.Properties
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ApplicationVersionServiceImplTest {

	companion object {
		init {
			AppTestRule.configure()
			AppModuleJvm.require()
		}
	}

	private val newVersionReader = mockk<(String) -> String>()
	private val properties = mockk<Properties>()
	private val service = ApplicationVersionServiceImpl(properties, newVersionReader)

	init {
		every { properties.getOptional<String?>(eq(ApplicationVersionServiceImpl.PROP_VERSION_FILE_URL)) } returns "bla"
	}

	@Test
	fun shouldOfferNewVersion() {
		every { properties.getString(eq(ApplicationVersionServiceImpl.PROP_IGNORED_VERSION)) } returns "0.0.0"
		every { newVersionReader.invoke(any()) } returns "0.4.0"

		val newerVersion = service.checkForNewerVersion(ApplicationVersion("0.3.1"))

		assertEquals("0.4.0", newerVersion.toString())
	}

	@Test
	fun shouldNotOfferIgnoredVersion() {
		every { properties.getString(eq(ApplicationVersionServiceImpl.PROP_IGNORED_VERSION)) } returns "0.4.0"
		every { newVersionReader.invoke(any()) } returns "0.4.0"

		val newerVersion = service.checkForNewerVersion(ApplicationVersion("0.3.1"))

		assertNull(newerVersion)
	}

	@Test
	fun shouldNotComplainUponConnectionError() {
		every { properties.getString(eq(ApplicationVersionServiceImpl.PROP_IGNORED_VERSION)) } returns "0.0.0"
		every { newVersionReader.invoke(any()) } throws RuntimeException("network error")

		val newerVersion = service.checkForNewerVersion(ApplicationVersion("0.3.1"))

		assertNull(newerVersion)
	}
}