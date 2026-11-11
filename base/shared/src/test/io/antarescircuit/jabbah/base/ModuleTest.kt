package io.antarescircuit.jabbah.base

import kotlin.test.Test
import kotlin.test.assertEquals

class ModuleTest {

	@Test
	fun shouldInitializeOnlyOnceUntilReset() {
		val module = TestModule()
		module.require()
		module.require()
		assertEquals(1, module.initializationCount)

		module.reset()
		module.require()
		assertEquals(2, module.initializationCount)
	}

	@Test
	fun shouldResetDependencies() {
		val dependency = TestModule()
		val module = TestModule(dependency)
		module.require()

		module.reset()
		module.require()

		assertEquals(2, module.initializationCount)
		assertEquals(2, dependency.initializationCount)
	}

	private class TestModule(private vararg val dependencies: AbstractModule) : AbstractModule() {
		var initializationCount = 0
			private set

		override fun initialize() {
			dependencies.forEach { it.require() }
			initializationCount++
		}

		override fun resetDependencies() {
			dependencies.forEach { it.reset() }
		}
	}
}
