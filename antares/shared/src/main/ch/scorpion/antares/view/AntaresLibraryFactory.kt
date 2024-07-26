package ch.scorpion.antares.view

import ch.scorpion.antares.model.gate.CurrentDefaultPropagationDelay
import ch.scorpion.antares.model.gate.UndefinedGateInputBehavior
import ch.scorpion.antares.model.input.CurrentSwitchPropagationDelay
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.library.GraphLibraryFactory
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryProperties

class AntaresLibraryFactory : GraphLibraryFactory() {

	override fun createBaseLibrary(properties: LibraryProperties): Library {
		val library = createEmptyLibrary(properties)
		AntaresViewModule.fillBaseElementLibrary(library)
		return library
	}

	override fun fillPreferences(library: Library) {
		super.fillPreferences(library)

		library.preferences.set(
			UndefinedGateInputBehavior.PROP_UNDEFINED_GATE_INPUT_BEHAVIOR,
			BaseModule.properties.getString(UndefinedGateInputBehavior.PROP_UNDEFINED_GATE_INPUT_BEHAVIOR))

		library.preferences.set(
			CurrentDefaultPropagationDelay.PROP_DEFAULT_PROPAGATION_DELAY,
			BaseModule.properties.getInt(CurrentDefaultPropagationDelay.PROP_DEFAULT_PROPAGATION_DELAY))

		library.preferences.set(
			CurrentSwitchPropagationDelay.PROP_DEFAULT_DELAY,
			BaseModule.properties.getInt(CurrentSwitchPropagationDelay.PROP_DEFAULT_DELAY))
	}
}