package io.antarescircuit.antares

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.antarescircuit.jabbah.app.Environment
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.graph.view.connect.ConnectMethod
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class AntaresModuleJvmTest {

	@Test
	fun shouldKeepSetPointsAfterChangingThemeAndRestarting() {
		val firstStart = startup(isFirstUsage = true)
		assertConnectMethod(ConnectMethod.SetPoints, firstStart)
		firstStart.customize(Themes.PROP_THEME, "Darcula")

		val secondStart = startup(isFirstUsage = false, savedPreferences = savePreferences(firstStart))
		assertEquals("Darcula", secondStart.getString(Themes.PROP_THEME))
		assertConnectMethod(ConnectMethod.SetPoints, secondStart)

		val thirdStart = startup(isFirstUsage = false, savedPreferences = savePreferences(secondStart))
		assertConnectMethod(ConnectMethod.SetPoints, thirdStart)
	}

	@Test
	fun shouldKeepSetPointsAfterRestartingWithoutChangingPreferences() {
		val firstStart = startup(isFirstUsage = true)

		val secondStart = startup(isFirstUsage = false, savedPreferences = savePreferences(firstStart))

		assertConnectMethod(ConnectMethod.SetPoints, secondStart)
	}

	@Test
	fun shouldKeepAutoLayoutForExistingUsersWithoutAnExplicitPreference() {
		val preferences = java.util.Properties()
		preferences.setProperty(Themes.PROP_THEME, "Darcula")

		val properties = startup(isFirstUsage = false, savedPreferences = preferences)

		assertConnectMethod(ConnectMethod.AutoLayout, properties)
	}

	@Test
	fun shouldRestoreExplicitConnectMethodPreferences() {
		for (method in ConnectMethod.entries) {
			val firstStart = startup(isFirstUsage = true)
			firstStart.customize(ConnectMethod.PROP_CONNECT_METHOD, method.customName)

			val secondStart = startup(isFirstUsage = false, savedPreferences = savePreferences(firstStart))

			assertConnectMethod(method, secondStart)
		}
	}

	private fun startup(isFirstUsage: Boolean, savedPreferences: java.util.Properties = java.util.Properties()): Properties {
		val properties = Properties()
		properties.set(ConnectMethod.PROP_CONNECT_METHOD, ConnectMethod.SetPoints.customName)
		properties.set(Themes.PROP_THEME, "Light")
		val app = mock<AntaresDesktop>()
		every { app.environment } returns Environment.Production
		every { app.isFirstUsage } returns isFirstUsage
		AntaresModuleJvm(app).customizeProperties(properties)
		for (key in savedPreferences.stringPropertyNames()) {
			properties.load(key, savedPreferences.getProperty(key))
		}
		return properties
	}

	private fun savePreferences(properties: Properties): java.util.Properties {
		val savedPreferences = java.util.Properties()
		for (key in properties.getCustomizedKeys()) {
			savedPreferences.setProperty(key, properties.getEntry(key).stringValue)
		}
		val output = ByteArrayOutputStream()
		savedPreferences.store(output, null)
		return java.util.Properties().apply { load(output.toByteArray().inputStream()) }
	}

	private fun assertConnectMethod(expected: ConnectMethod, properties: Properties) {
		assertEquals(expected.customName, properties.getString(ConnectMethod.PROP_CONNECT_METHOD))
	}
}
