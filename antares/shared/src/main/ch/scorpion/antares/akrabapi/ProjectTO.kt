package ch.scorpion.antares.akrabapi

import kotlinx.serialization.Serializable

@Serializable
data class ProjectTO(
	val uuid: String,
	val name: TranslationsTO,
	val author: UUIDTO,
	val isSystem: Boolean = false,
	val description: TranslationsTO?,
	val importedLibrary: String,
	val public: Boolean
)