package io.antarescircuit.antares.standardlibrary

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.testcase.TestcaseService
import io.antarescircuit.jabbah.app.AbstractDesktopApplication
import io.antarescircuit.jabbah.app.CurrentApplicationVersion
import io.antarescircuit.jabbah.graph.library.LibraryModule
import junit.framework.TestCase.assertTrue
import kotlin.test.BeforeTest
import kotlin.test.Test

/** Runs all [Testcase]s in all [DigitalGraph]s of the "Standard Library".*/
class StandardLibraryTest {

	@BeforeTest
	fun setup() {
		CurrentApplicationVersion.version = AbstractDesktopApplication.readVersion()
		AntaresTestRule.configure()
		AbstractStandardLibraryBasedCircuitTest.setupLibrary()
	}

	@Test
	fun test() {
		val results = TestcaseService.runAllLibraryTests(LibraryModule.libraryHolder.library)

		results.forEach {
			if (it.failed) {
				println("Failed tests in '${it.source.name.value}' (UUID ${it.source.uuid.id})")
			}
		}

		assertTrue(results.all { !it.failed })
	}
}