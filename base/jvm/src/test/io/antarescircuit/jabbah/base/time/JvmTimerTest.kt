package io.antarescircuit.jabbah.base.time

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JvmTimerTest {

	@Test
	fun shouldFireOnceAndAllowRestart() {
		val ticks = AtomicInteger()
		var fired = CountDownLatch(1)
		val timer = JvmTimer().initialize(0, repeats = false) {
			ticks.incrementAndGet()
			fired.countDown()
		}
		try {
			timer.start()
			assertTrue(fired.await(5, SECONDS))
			assertFalse(timer.isRunning())
			assertEquals(1, ticks.get())

			fired = CountDownLatch(1)
			timer.start()
			assertTrue(fired.await(5, SECONDS))
			assertEquals(2, ticks.get())
		} finally {
			timer.stop()
		}
	}

	@Test
	fun shouldStopRepeatingFromTheHandler() {
		val fired = CountDownLatch(1)
		val ticks = AtomicInteger()
		val timer = JvmTimer()
		timer.initialize(0) {
			if (ticks.incrementAndGet() == 3) {
				timer.stop()
				fired.countDown()
			}
		}
		try {
			timer.start()
			assertTrue(fired.await(5, SECONDS))
			assertFalse(timer.isRunning())
			assertEquals(3, ticks.get())
		} finally {
			timer.stop()
		}
	}

	@Test
	fun shouldCancelPendingTickWhenStopped() {
		val ticks = AtomicInteger()
		val timer = JvmTimer().initialize(60_000) { ticks.incrementAndGet() }
		timer.start()
		assertTrue(timer.isRunning())
		timer.stop()
		assertFalse(timer.isRunning())
		assertEquals(0, ticks.get())
	}

	@Test
	fun shouldAllowRestartAfterAHandlerFailure() {
		val failed = CountDownLatch(1)
		val fired = CountDownLatch(1)
		val failNext = AtomicBoolean(true)
		val timer = JvmTimer()
		timer.initialize(0) {
			if (failNext.getAndSet(false)) {
				Thread.currentThread().uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, _ ->
					failed.countDown()
				}
				throw IllegalStateException("handler failed")
			}
			timer.stop()
			fired.countDown()
		}
		try {
			timer.start()
			assertTrue(failed.await(5, SECONDS))
			assertFalse(timer.isRunning())
			timer.start()
			assertTrue(fired.await(5, SECONDS))
		} finally {
			timer.stop()
		}
	}

	@Test
	fun shouldRejectInvalidLifecycleAndIntervals() {
		val timer = JvmTimer()
		assertFailsWith<IllegalStateException> { timer.start() }
		assertFailsWith<IllegalArgumentException> { timer.initialize(-1) {} }
		timer.initialize(10) {}
		assertFailsWith<IllegalStateException> { timer.initialize(10) {} }
		assertFailsWith<IllegalArgumentException> { timer.interval = -1 }
	}
}
