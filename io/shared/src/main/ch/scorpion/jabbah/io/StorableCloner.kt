package ch.scorpion.jabbah.io

/**
 * A utility class for cloning a [Storable]s by writing it to a buffer using a [StoreWriter]
 * and reading it back using a [StoreReader].
 */
interface StorableCloner {

	fun serialize(storable: Storable): String

	fun deserialize(s: String): Storable

	fun clone(storable: Storable): Storable

	fun clonePreservingIdentities(storable: Storable, storableCreator: StorableCreator): Storable

	fun cloneUsingCreator(storable: Storable, storableCreator: StorableCreator): Storable

	fun clone(
		storable: Storable,
		identityProvider: GlobalIdentityProvider,
		storableCreator: StorableCreator,
		referenceResolver: ReferenceResolver
	): Storable
}