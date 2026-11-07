package io.antarescircuit.jabbah.graph.project

interface ProjectAkrabClientService {

	/**
	 * Creates a ZIP file with all [Project] data and uploads it to the Akrab server for storing
	 * and making it available in Web front-ends. Overwrites any [Project] data already present at the
	 * server, therefore the user must have confirmed this action.
	 *
	 * @throws AkrabApiException containing an [AkrabApiError] in case of an error
	 */
	suspend fun upload(project: Project)
}
