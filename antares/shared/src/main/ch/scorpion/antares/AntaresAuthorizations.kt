package ch.scorpion.antares

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation.View
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import ch.scorpion.jabbah.graph.model.GraphType

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