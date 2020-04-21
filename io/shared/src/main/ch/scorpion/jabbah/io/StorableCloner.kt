package ch.scorpion.jabbah.io

/**
 * A utility class for cloning a [Storable]s by writing it to a buffer using a [StoreWriter]
 * and reading it back using a [StoreReader].
 */
expect object StorableCloner {

	fun serialize(storable: Storable): String

	fun deserialize(s: String): Storable

	fun <T: Storable> clone(storable: T): T

	fun <T: Storable> clonePreservingIdentities(storable: T, storableCreator: StorableCreator): T

	fun <T: Storable> cloneUsingCreator(storable: T, storableCreator: StorableCreator): T

	fun <T: Storable> clone(
		storable: T,
		identityProvider: GlobalIdentityProvider,
		storableCreator: StorableCreator,
		referenceResolver: ReferenceResolver
	): T
}