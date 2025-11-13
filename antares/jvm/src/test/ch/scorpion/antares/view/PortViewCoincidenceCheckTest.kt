package ch.scorpion.antares.view

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.DefaultSavable
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.edit.Look
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.health.PortViewCoincidenceCheck
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlin.test.Test

class PortViewCoincidenceCheckTest {

	companion object {
		init {
			BaseModuleJvm.require()
			AntaresTestRule.configure()
		}
	}

	@Test
	fun checkUnrotatedGateViews() {
		val metaGraph = MetaGraph.create(TranslatableText("Test"), AntaresGraphTypes.Digital)
		val appData = ApplicationData(metaGraph, DefaultSavable.undefined())

		val notGateView = LogicGateView.notGateView()
		notGateView.location = Point2D(100, 100)
		metaGraph.graph.graphView.add(notGateView)

		val andGateView = LogicGateView.andGateView()
		andGateView.location = Point2D(100 - 2 * AbstractAntaresPortView.LENGTH - 6 * Look.SCALE, 100)
		metaGraph.graph.graphView.add(andGateView)

		PortViewCoincidenceCheck.execute(appData)

		assertTrue(notGateView.getPortView(notGateView.model.getPort(1))!!.coincidenceWarning)
		assertFalse(notGateView.getPortView(notGateView.model.getPort(2))!!.coincidenceWarning)
	}

	@Test
	fun checkRotatedGateViews() {
		val metaGraph = MetaGraph.create(TranslatableText("Test"), AntaresGraphTypes.Digital)
		val appData = ApplicationData(metaGraph, DefaultSavable.undefined())

		val notGateView = LogicGateView.notGateView()
		notGateView.orientation = Direction.NORTH
		notGateView.location = Point2D(100, 100)
		metaGraph.graph.graphView.add(notGateView)

		val andGateView = LogicGateView.andGateView()
		andGateView.orientation = Direction.NORTH
		andGateView.location = Point2D(100, 100 + 2 * AbstractAntaresPortView.LENGTH + 6 * Look.SCALE)
		metaGraph.graph.graphView.add(andGateView)

		PortViewCoincidenceCheck.execute(appData)

		assertTrue(notGateView.getPortView(notGateView.model.getPort(1))!!.coincidenceWarning)
		assertFalse(notGateView.getPortView(notGateView.model.getPort(2))!!.coincidenceWarning)
	}
}