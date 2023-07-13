package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.Logic

interface LogicGateType {
	val outputLogic: Logic
	val calculator: AbstractLogicGateCalculator
}