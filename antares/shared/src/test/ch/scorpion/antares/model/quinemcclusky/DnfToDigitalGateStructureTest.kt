package ch.scorpion.antares.model.quinemcclusky

import ch.scorpion.antares.model.quinemccluskey.DNF
import ch.scorpion.antares.model.quinemccluskey.DnfToDigitalGateStructure
import kotlin.test.*

class DnfToDigitalGateStructureTest {

	@Test
	fun shouldBuildXOR() {
		// AB' + A'B
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))

		val orTerms = DnfToDigitalGateStructure(dnf).build()

		assertEquals(2, orTerms.size)

		orTerms[0].let { term ->
			assertEquals(2, term.factors.size)
			term.factors[0].let { factor ->
				assertEquals(0, factor.inputIndex)
				assertFalse(factor.inverted!!)
				assertNull(factor.constant)
			}
			term.factors[1].let { factor ->
				assertEquals(1, factor.inputIndex)
				assertTrue(factor.inverted!!)
				assertNull(factor.constant)
			}
		}

		orTerms[1].let { term ->
			assertEquals(2, term.factors.size)
			term.factors[0].let { factor ->
				assertEquals(0, factor.inputIndex)
				assertTrue(factor.inverted!!)
				assertNull(factor.constant)
			}
			term.factors[1].let { factor ->
				assertEquals(1, factor.inputIndex)
				assertFalse(factor.inverted!!)
				assertNull(factor.constant)
			}
		}
	}

	@Test
	fun shouldBuildConstantTrue() {
		val dnf: DNF = listOf(listOf())

		val orTerms = DnfToDigitalGateStructure(dnf).build()

		assertEquals(1, orTerms.size)
		orTerms[0].let { term ->
			assertEquals(1, term.factors.size)
			term.factors[0].let { factor ->
				assertNull(factor.inputIndex)
				assertNull(factor.inverted)
				assertEquals(true, factor.constant)
			}
		}
	}

	@Test
	fun shouldBuildConstantFalse() {
		val dnf: DNF = listOf()

		val orTerms = DnfToDigitalGateStructure(dnf).build()

		assertEquals(1, orTerms.size)
		orTerms[0].let { term ->
			assertEquals(1, term.factors.size)
			term.factors[0].let { factor ->
				assertNull(factor.inputIndex)
				assertNull(factor.inverted)
				assertEquals(false, factor.constant)
			}
		}
	}
}