package io.antarescircuit.antares.view

import io.antarescircuit.antares.model.gate.CurrentDefaultPropagationDelay
import io.antarescircuit.antares.model.gate.UndefinedGateInputBehavior
import io.antarescircuit.antares.model.input.CurrentSwitchPropagationDelay
import io.antarescircuit.antares.view.module.AntaresViewModule
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.library.GraphLibraryFactory
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryProperties

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