package io.antarescircuit.antares.ai

import io.antarescircuit.jabbah.base.Properties
import kotlin.test.Test
import kotlin.test.assertEquals

class OpenRouterConfigTest {

	@Test
	fun shouldRegisterDefaultModel() {
		val properties = Properties()

		OpenRouterConfig.fillProperties(properties)

		assertEquals(OpenRouterConfig.MODEL, OpenRouterConfig.model(properties))
	}

	@Test
	fun shouldReturnConfiguredModelWithoutSurroundingWhitespace() {
		val properties = Properties()
		OpenRouterConfig.fillProperties(properties)
		properties.set(OpenRouterConfig.PROP_MODEL, "  anthropic/claude-sonnet-4.5  ")

		assertEquals("anthropic/claude-sonnet-4.5", OpenRouterConfig.model(properties))
	}

	@Test
	fun shouldUseDefaultModelForBlankPreference() {
		val properties = Properties()
		OpenRouterConfig.fillProperties(properties)
		properties.set(OpenRouterConfig.PROP_MODEL, "  ")

		assertEquals(OpenRouterConfig.MODEL, OpenRouterConfig.model(properties))
	}
}
