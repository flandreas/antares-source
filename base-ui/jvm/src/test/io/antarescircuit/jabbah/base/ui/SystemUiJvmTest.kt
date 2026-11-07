package io.antarescircuit.jabbah.base.ui

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.time.RealTimeTimerJvm
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS
import javax.swing.SwingUtilities
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SystemUiJvmTest {

	@Test
	fun shouldDispatchInvocationsAndTimersOnTheSwingEventThread() {
		val previousInvoker = System.invoker
		val previousFactory = System.timerFactory
		val previousBrowser = System.browser
		val previousMessageHandler = System.messageHandler
		var timer: io.antarescircuit.jabbah.base.time.Timer? = null
		try {
			SystemUiJvm.install()
			val invoked = CountDownLatch(1)
			var invokedOnEventThread = false
			System.invokeLater {
				invokedOnEventThread = SwingUtilities.isEventDispatchThread()
				invoked.countDown()
			}
			assertTrue(invoked.await(5, SECONDS))
			assertTrue(invokedOnEventThread)

			val fired = CountDownLatch(1)
			var firedOnEventThread = false
			timer = System.createTimer()
			assertIs<RealTimeTimerJvm>(timer)
			timer.initialize(1, repeats = false) {
				firedOnEventThread = SwingUtilities.isEventDispatchThread()
				fired.countDown()
			}.start()
			assertTrue(fired.await(5, SECONDS))
			assertTrue(firedOnEventThread)
		} finally {
			timer?.stop()
			System.invoker = previousInvoker
			System.timerFactory = previousFactory
			System.browser = previousBrowser
			System.messageHandler = previousMessageHandler
		}
	}
}
