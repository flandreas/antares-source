package io.antarescircuit.antares.view

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.DefaultSavable
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModuleJvm
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.health.PortViewCoincidenceCheck
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlin.test.Test

class PortViewCoincidenceCheckTest {

	init {
		BaseModuleJvm.require()
		AntaresTestRule.configure()
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