package io.antarescircuit.antares

import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.jabbah.edit.auth.Authorizer
import io.antarescircuit.jabbah.edit.auth.Operation.View
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import io.antarescircuit.jabbah.graph.model.GraphType

object AntaresAuthorizations {

	fun define() {
		Authorizer.authorize().currentUser().to(View).data(::digitalGraphs)
		Authorizer.authorize().currentUser().to(View).data(::digitalSystemLibraries)
		Authorizer.authorize().currentUser().to(View).data(::analogGraphs)
		Authorizer.authorize().currentUser().to(View).data(::allLibraries)
	}

	private fun digitalGraphs(data: Any): Boolean =
		if (data is GraphType) data === AntaresGraphTypes.Digital else false

	private fun analogGraphs(data: Any): Boolean =
		if (data is GraphType) data === AntaresGraphTypes.Analog else false

	private fun digitalSystemLibraries(data: Any): Boolean =
		if (data is LibraryDictionaryEntry) {
			!AntaresApplication.ANALOG_LIBRARY_IDS.contains(data.uuid.id)
		} else false

	private fun allLibraries(data: Any): Boolean = data is LibraryDictionaryEntry
}