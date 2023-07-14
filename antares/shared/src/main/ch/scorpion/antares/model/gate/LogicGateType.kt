package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.Logic
import ch.scorpion.jabbah.base.help.HelpId
import ch.scorpion.jabbah.graph.model.Vertice

interface LogicGateType {
	val outputLogic: Logic
	val calculator: AbstractLogicGateCalculator
	val helpId: HelpId
	val baseResourceKey: String

	fun getName(gate: Vertice): String

	fun getDescription(gate: Vertice): String?
}