package io.antarescircuit.jabbah.graph

import io.antarescircuit.jabbah.edit.auth.Authorizer
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.edit.auth.Operation.Change
import io.antarescircuit.jabbah.edit.auth.Operation.View
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.project.Project

object GraphAuthorizations {

	fun define() {
		Authorizer.authorize().currentUser().to(View).data(::anyLibrary)
		Authorizer.authorize().currentUser().to(Change).data(::libraryOwnedByHim)

		Authorizer.authorize().currentUser().to(View).data(::anyProject)
		Authorizer.authorize().currentUser().to(Change).data(::projectOwnedByHim)
	}

	private fun anyLibrary(data: Any): Boolean = data is Library

	private fun libraryOwnedByHim(data: Any): Boolean =
		if (data is Library) data.author == EditAuthModule.userHolder.user.identity else false

	private fun anyProject(data: Any): Boolean = data is Project

	private fun projectOwnedByHim(data: Any): Boolean =
		if (data is Project) data.author == EditAuthModule.userHolder.user.identity else false
}