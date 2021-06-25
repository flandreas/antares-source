package ch.scorpion.antares.model.input

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.input.PeriodOrFrequencyParser.parse
import ch.scorpion.antares.model.input.PeriodOrFrequencyUnit.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PeriodOrFrequencyTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	/** ---- Parsing tests */

	@Test
	fun shouldThrowExceptionIfCannotParse() {
		assertFailsWith<IllegalArgumentException> { parse("0") }
		assertFailsWith<IllegalArgumentException> { parse("Test") }
		assertFailsWith<IllegalArgumentException> { parse("ms") }
		assertFailsWith<IllegalArgumentException> { parse("ms 100") }
		assertFailsWith<IllegalArgumentException> { parse("") }
		assertFailsWith<IllegalArgumentException> { parse("1.5 s") }
		assertFailsWith<IllegalArgumentException> { parse("-5 s") }
		assertFailsWith<IllegalArgumentException> { parse("+5 s") }
	}

	@Test
	fun shouldTrimWhitespace() {
		assertEquals(PeriodOrFrequency(1, Nanosecond), parse(" 1 "))
	}

	@Test
	fun shouldDefaultToNanosecondsWithoutUnit() {
		assertEquals(PeriodOrFrequency(1, Nanosecond), parse("1"))
		assertEquals(PeriodOrFrequency(123456, Nanosecond), parse("123456"))
	}

	@Test
	fun shouldParseSeconds() {
		assertEquals(PeriodOrFrequency(1, Second), parse("1 s"))
		assertEquals(PeriodOrFrequency(1, Second), parse("1 S"))
		assertEquals(PeriodOrFrequency(100, Second), parse("100s"))
	}

	@Test
	fun shouldParseMilliseconds() {
		assertEquals(PeriodOrFrequency(10, Millisecond), parse("10 ms"))
		assertEquals(PeriodOrFrequency(345, Millisecond), parse("345ms"))
	}

	@Test
	fun shouldParseMicroseconds() {
		assertEquals(PeriodOrFrequency(4, Microsecond), parse("4 us"))
		assertEquals(PeriodOrFrequency(4, Microsecond), parse("4 µs"))
		assertEquals(PeriodOrFrequency(1000, Microsecond), parse("1000us"))
	}

	@Test
	fun shouldParseNanoseconds() {
		assertEquals(PeriodOrFrequency(999, Nanosecond), parse("999 ns"))
		assertEquals(PeriodOrFrequency(56789, Nanosecond), parse("56789ns"))
	}

	@Test
	fun shouldParseHertz() {
		assertEquals(PeriodOrFrequency(1, Hertz), parse("1 Hz"))
		assertEquals(PeriodOrFrequency(7, Hertz), parse("7hz"))
	}

	@Test
	fun shouldParseKiloHertz() {
		assertEquals(PeriodOrFrequency(3, KiloHertz), parse("3 kHz"))
		assertEquals(PeriodOrFrequency(7, KiloHertz), parse("7Khz"))
	}

	@Test
	fun shouldParseMegaHertz() {
		assertEquals(PeriodOrFrequency(1, MegaHertz), parse("1 MHz"))
		assertEquals(PeriodOrFrequency(7, MegaHertz), parse("7mhz"))
	}

	@Test
	fun shouldParseGigaHertz() {
		assertEquals(PeriodOrFrequency(1, GigaHertz), parse("1 GHz"))
		assertEquals(PeriodOrFrequency(7, GigaHertz), parse("7gHZ"))
	}

	@Test
	fun shouldConvertFrequencyToPeriod() {
		assertEquals(PeriodOrFrequency(1_000_000_000 / 4, Nanosecond), PeriodOrFrequency(4, Hertz).asNanoseconds)
		assertEquals(PeriodOrFrequency(1_000_000 / 3, Nanosecond), PeriodOrFrequency(3, KiloHertz).asNanoseconds)
		assertEquals(PeriodOrFrequency(1_000 / 2, Nanosecond), PeriodOrFrequency(2, MegaHertz).asNanoseconds)
		assertEquals(PeriodOrFrequency(1, Nanosecond), PeriodOrFrequency(1, GigaHertz).asNanoseconds)
	}


	/** ---- Other tests */

	@Test
	fun shouldFormat() {
		assertEquals("100 s", PeriodOrFrequency(100, Second).toString())
		assertEquals("1234567 ms", PeriodOrFrequency(1234567, Millisecond).toString())
	}

	@Test
	fun shouldConvertToNanoseconds() {
		assertEquals(PeriodOrFrequency(2_000_000_000, Nanosecond), PeriodOrFrequency(2, Second).asNanoseconds)
		assertEquals(PeriodOrFrequency(17_000_000, Nanosecond), PeriodOrFrequency(17, Millisecond).asNanoseconds)
		assertEquals(PeriodOrFrequency(9_000, Nanosecond), PeriodOrFrequency(9, Microsecond).asNanoseconds)
		assertEquals(PeriodOrFrequency(42, Nanosecond), PeriodOrFrequency(42, Nanosecond).asNanoseconds)
	}

	@Test
	fun shouldCreatePeriodFromNanoseconds() {
		assertEquals(PeriodOrFrequency(2_100, Microsecond), PeriodOrFrequency.fromNanoseconds(2_100_000, Microsecond))
		assertEquals(PeriodOrFrequency(2_345, Microsecond), PeriodOrFrequency.fromNanoseconds(2_345_678, Microsecond))
	}

	@Test
	fun shouldCreateFrequencyFromNanoseconds() {
		assertEquals(PeriodOrFrequency(500_000_000, Hertz), PeriodOrFrequency.fromNanoseconds(2, Hertz))
		assertEquals(PeriodOrFrequency(500_000, KiloHertz), PeriodOrFrequency.fromNanoseconds(2, KiloHertz))
		assertEquals(PeriodOrFrequency(500, MegaHertz), PeriodOrFrequency.fromNanoseconds(2, MegaHertz))
		assertEquals(PeriodOrFrequency(1, GigaHertz), PeriodOrFrequency.fromNanoseconds(1, GigaHertz))
	}
}