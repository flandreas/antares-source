package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.io.Storable
import io.mockk.every
import io.mockk.mockk

class ApplicationDataRepositoryMockBuilder {

	val repository = mockk<ApplicationDataRepository<Savable>>(relaxed = true)
	var providedSavable: Savable? = null

	init {
		every { repository.createUndefinedSavable() } returns newSavable()
	}

	fun build(): ApplicationDataRepository<Savable> = repository

	private fun newSavable(): Savable {
		providedSavable = DefaultSavable.undefined()
		return  providedSavable!!
	}

	fun withLoadedStorable(storable: Storable): ApplicationDataRepositoryMockBuilder {
		every { repository.load(any()) } returns storable
		return this
	}
}