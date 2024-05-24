package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.standardlibrary.AbstractStandardLibraryBasedCircuitTest
import ch.scorpion.jabbah.graph.library.LibraryModule
import junit.framework.TestCase.assertTrue
import kotlin.test.BeforeTest
import kotlin.test.Test

/** Runs all [Testcase]s in all [DigitalGraph]s of the "Standard Library".*/
class StandardLibraryTest {

	@BeforeTest
	fun setup() {
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