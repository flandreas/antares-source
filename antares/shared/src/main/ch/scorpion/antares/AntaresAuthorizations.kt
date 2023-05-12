package ch.scorpion.antares

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation.View
import ch.scorpion.jabbah.graph.model.GraphType

object AntaresAuthorizations {

	fun define() {
		Authorizer.authorize().currentUser().to(View).data(::digitalGraphs)
		Authorizer.authorize().developer().to(View).data(::analogGraphs)
	}

	private fun digitalGraphs(data: Any): Boolean =
		if (data is GraphType) data === AntaresGraphTypes.Digital else false

	private fun analogGraphs(data: Any): Boolean =
		if (data is GraphType) data === AntaresGraphTypes.Analog else false
}