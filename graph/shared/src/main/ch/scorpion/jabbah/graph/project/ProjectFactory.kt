package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.graph.library.LibraryProperties

/** A factory for creating new [Project]s.*/
interface ProjectFactory {

	fun create(name: String): Project

	fun create(properties: LibraryProperties): Project
}

/** Null pattern.*/
class UnimplementedProjectFactory : ProjectFactory {

	override fun create(name: String): Project {
		throw UnsupportedOperationException("not implemented")
	}

	override fun create(properties: LibraryProperties): Project {
		throw UnsupportedOperationException("not implemented")
	}
}