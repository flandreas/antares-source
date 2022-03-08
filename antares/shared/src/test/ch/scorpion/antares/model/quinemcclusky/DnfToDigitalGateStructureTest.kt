package ch.scorpion.antares.model.quinemcclusky

import ch.scorpion.antares.model.quinemccluskey.DNF
import ch.scorpion.antares.model.quinemccluskey.DnfToDigitalGateStructure
import kotlin.test.*

class DnfToDigitalGateStructureTest {

	@Test
	fun `Should build AB' + AB'`() {
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))

		val andTerms = DnfToDigitalGateStructure(dnf).build()

		assertEquals(2, andTerms.size)

		andTerms[0].let { term ->
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

		andTerms[1].let { term ->
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
}