package io.antarescircuit.antares.model.gate

import io.antarescircuit.antares.model.Logic
import io.antarescircuit.jabbah.base.help.HelpId
import io.antarescircuit.jabbah.graph.model.Vertice

interface LogicGateType {
	val outputLogic: Logic
	val calculator: AbstractLogicGateCalculator
	val helpId: HelpId
	val baseResourceKey: String

	fun getName(gate: Vertice): String

	fun getDescription(gate: Vertice): String?
}