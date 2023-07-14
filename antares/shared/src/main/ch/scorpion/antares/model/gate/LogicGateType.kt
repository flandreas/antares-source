package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.Logic
import ch.scorpion.jabbah.base.help.HelpId

interface LogicGateType {
	val outputLogic: Logic
	val calculator: AbstractLogicGateCalculator
	val helpId: HelpId
}