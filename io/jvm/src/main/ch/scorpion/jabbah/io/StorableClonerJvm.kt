package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.io.GlobalIdentityCreator
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * A [StorableCloner] implementation for the JVM target.
 */
class StorableClonerJvm(val typeMap: TypeMap) : StorableCloner {

    constructor(): this(IOModule.typeMap)

    private val LOG by logger()

    /** ---- [StorableCloner] interface */

    override fun clone(storable: Storable): Storable {
        return clone(storable, GlobalIdentityCreator(), IOModule.storableCreator, ReferenceResolverImpl())
    }

    override fun clonePreservingIdentities(storable: Storable, storableCreator: StorableCreator): Storable {
        return clone(storable, GlobalIdentityReflector(), storableCreator, ReferenceResolverImpl())
    }

    override fun cloneUsingCreator(storable: Storable, storableCreator: StorableCreator): Storable {
        return clone(storable, GlobalIdentityCreator(), storableCreator, ReferenceResolverImpl())
    }

    override fun clone(
        storable: Storable,
        identityProvider: GlobalIdentityProvider,
        storableCreator: StorableCreator,
        referenceResolver: ReferenceResolver
    ): Storable {

        try {
            val buffer = ByteArrayOutputStream()
            val xmlWriter = ElectricXmlWriter(buffer)
            val writer = StoreXmlWriter(xmlWriter, typeMap, identityProvider)
            writer.writeStorable(storable)

            LOG.debug(buffer.toString())

            val xmlReader = ElectricXmlReader(ByteArrayInputStream(buffer.toByteArray()))
            val reader = StoreXmlReader(xmlReader, typeMap, storableCreator, referenceResolver)
            return reader.readStorable()
        } catch (x: Throwable) {
            LOG.error("Error while cloning Storable: ${x.message}")
            throw x
        }
    }
}