package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.loggerFor
import ch.scorpion.jabbah.io.module.DomXmlWriter

/**
 * A [StorableCloner] implementation for the JavaScript platform.
 * TODO Refactor: Extract methods common to JVM implementation into base class.
 */
class StorableClonerJs(private val typeMap: TypeMap) : StorableCloner {

    constructor(): this(IOModule.typeMap)

    private val LOG by loggerFor(this)

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

    override fun clone(storable: Storable, identityProvider: GlobalIdentityProvider, storableCreator: StorableCreator, referenceResolver: ReferenceResolver): Storable {
        try {
            var buffer: String? = null
            val xmlWriter = DomXmlWriter({ buffer = it})
            val writer = StoreXmlWriter(xmlWriter, typeMap, identityProvider)
            writer.writeStorable(storable)

            LOG.debug(buffer?: "empty")

            val xmlReader = DomXmlReader(buffer!!)
            val reader = StoreXmlReader(xmlReader, typeMap, storableCreator, referenceResolver)
            return reader.readStorable()
        } catch (x: Throwable) {
            LOG.error("Error while cloning Storable: ${x.message}")
            throw x
        }
    }
}