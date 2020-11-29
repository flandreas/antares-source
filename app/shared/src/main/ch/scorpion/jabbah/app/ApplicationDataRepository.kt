package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.io.Storable

/**
 * A repository for creating, loading and storing [Storable]s associated with a [Savable]
 * @param T the type of [Savable] handled by this [ApplicationDataRepository]
 */
interface ApplicationDataRepository<T: Savable> {

	/**
	 * Creates a new [Savable] that is undefined in terms of [Savable.defined].
	 * Saving a [Storable] with that [Savable] will require interaction with user.
	 */
	fun createUndefinedSavable(): T

	fun load(savable: T): Storable

	fun store(savable: T, storable: Storable)
}

class UnimplementedApplicationDataRepository : ApplicationDataRepository<Savable> {

	override fun createUndefinedSavable(): Savable {
		throw UnsupportedOperationException("not implemented")
	}

	override fun load(savable: Savable): Storable {
		throw UnsupportedOperationException("not implemented")
	}

	override fun store(savable: Savable, storable: Storable) {
		throw UnsupportedOperationException("not implemented")
	}
}