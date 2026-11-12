package io.antarescircuit.antares.model

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.model.graph.FeedbackLoopChecker
import io.antarescircuit.jabbah.graph.view.VerticeView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeedbackLoopCheckerTest {

	@BeforeTest
	fun beforeTest() {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldDetectCrossCoupledGates() {
		val builder = TestCircuitBuilder("latch")
		val upper = builder.addVerticeView(LogicGateView.norGateView())
		val lower = builder.addVerticeView(LogicGateView.norGateView())
		builder.connect(builder.addInput("S"), upper, upper.vertice.getInput(1))
		builder.connect(builder.addInput("R"), lower, lower.vertice.getInput(2))
		builder.connect(upper, lower, lower.vertice.getInput(1))
		builder.connect(lower, upper, upper.vertice.getInput(2))
		fanOut(upper, builder.addOutput("Q"))

		assertTrue(FeedbackLoopChecker.hasFeedbackLoop(builder.graph))
	}

	@Test
	fun shouldCheckRejoiningPathsWithoutEnumeratingThem() {
		// 30 layers whose gates both feed both gates of the next layer form 2^30 paths, but no loop
		val builder = TestCircuitBuilder("ladder")
		var previous: List<VerticeView<out Vertice>> = listOf(builder.addInput("A"), builder.addInput("B"))
		repeat(30) {
			val layer = listOf(builder.addVerticeView(LogicGateView.andGateView()), builder.addVerticeView(LogicGateView.orGateView()))
			for (input in 1..2) {
				builder.connect(previous[input - 1], layer[0], layer[0].vertice.getInput(input))
				fanOut(previous[input - 1], layer[1], input)
			}
			previous = layer
		}
		builder.connect(previous[0], builder.addOutput("O"))

		assertFalse(FeedbackLoopChecker.hasFeedbackLoop(builder.graph))
	}

	/** Connects [to] to the existing net of [from]'s output, since the builder creates one net per connection. */
	private fun fanOut(from: VerticeView<out Vertice>, to: VerticeView<out Vertice>, input: Int = 1) {
		from.vertice.getOutput<DigitalSignal>().net!!.connect(to.vertice.getInput<DigitalSignal>(input))
	}
}
