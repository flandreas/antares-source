package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.io.Storable
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock

class ApplicationDataRepositoryMockBuilder {

	val repository = mock<ApplicationDataRepository<Savable>>(MockMode.autofill)
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