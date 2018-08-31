package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Unit tests for [Thousands].*/
class ThousandsTest {

	@Before
	fun setup() {
		BaseModuleJvm.require()
	}

	@Test
	fun shouldRound() {
		assertThat(Thousands.round(0.9, 2), `is`("0.9"))
		assertThat(Thousands.round(1.0, 2), `is`("1"))
		assertThat(Thousands.round(1.2, 2), `is`("1.2"))
		assertThat(Thousands.round(1.23, 2), `is`("1.23"))
		assertThat(Thousands.round(1.234, 2), `is`("1.23"))
	}

	@Test
	fun shouldConvertHundreds() {
		assertThat(Thousands.convert(4), `is`("4"))
		assertThat(Thousands.convert(38), `is`("38"))
		assertThat(Thousands.convert(999), `is`("999"))
	}

	@Test
	fun shouldConvertThousands() {
		assertThat(Thousands.convert(1_000), `is`("1K"))
		assertThat(Thousands.convert(1_001), `is`("1K"))
		assertThat(Thousands.convert(1_010), `is`("1.01K"))
		assertThat(Thousands.convert(1_100), `is`("1.1K"))
		assertThat(Thousands.convert(1_111), `is`("1.11K"))
		assertThat(Thousands.convert(11_111), `is`("11.1K"))
		assertThat(Thousands.convert(99_999), `is`("99.9K"))
		assertThat(Thousands.convert(100_000), `is`("100K"))
		assertThat(Thousands.convert(999_999), `is`("999K"))
	}

	@Test
	fun shouldConvertMillions() {
		assertThat(Thousands.convert(1_000_000), `is`("1M"))
		assertThat(Thousands.convert(13_333_424), `is`("13.3M"))
	}

	@Test
	fun shouldConvertGigas() {
		assertThat(Thousands.convert(1_000_000_000), `is`("1G"))
		assertThat(Thousands.convert(54_123_456_789), `is`("54.1G"))
	}

	@Test
	fun shouldConvertTeras() {
		assertThat(Thousands.convert(1_000_000_000_000), `is`("1T"))
		assertThat(Thousands.convert(99_010_000_000_000), `is`("99T"))
		assertThat(Thousands.convert(123_456_123_777_998), `is`("123T"))
	}
}