package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * Makes the current [ApplicationVersion] accessible to objects that don't have access to
 * the current desktop [Application], such as top-level persistent objects in higher modules
 * that want to add the version to persistent files.
 */
object CurrentApplicationVersion {

    /** The element name in persistent files holding the serialized representation of an [ApplicationVersion].*/
    const val OLD_PERSISTENT_NAME = "appVersion"

    const val NEW_PERSISTENT_NAME = "dataVersion"

    private val DUMMY_VERSION = ApplicationVersion(ApplicationVersion.DUMMY_VERSION_ID)

    var codeVersion: ApplicationVersion = DUMMY_VERSION

    var dataVersion: ApplicationVersion = DUMMY_VERSION

    fun write(writer: StoreWriter) {
        writer.writeString(NEW_PERSISTENT_NAME, dataVersion.toString())
    }

    fun read(reader: StoreReader): ApplicationVersion =
        if (reader.hasAttribute(NEW_PERSISTENT_NAME)) {
            ApplicationVersion.parse(reader.readString(NEW_PERSISTENT_NAME))
        } else if (reader.hasAttribute(OLD_PERSISTENT_NAME)) {
            ApplicationVersion.parse(reader.readString(OLD_PERSISTENT_NAME))
        } else {
            DUMMY_VERSION
        }

    /**
     * Checks if the version attribute in the current element read by [reader] is not newer
     * than the [CurrentApplicationVersion]
     * @throws ApplicationTooOldException if the check fails
     */
    fun check(reader: StoreReader) {
        if (reader.hasAttribute(NEW_PERSISTENT_NAME)) {
            check(ApplicationVersion.parse(reader.readString(NEW_PERSISTENT_NAME)))
        } else if (reader.hasAttribute(OLD_PERSISTENT_NAME)) {
            check(ApplicationVersion.parse(reader.readString(OLD_PERSISTENT_NAME)))
        }
    }

    private fun check(dataVersion: ApplicationVersion) {
        if (dataVersion > this.dataVersion) {
            throw ApplicationTooOldException(dataVersion)
        }
    }
}

class ApplicationTooOldException(
    dataVersion: ApplicationVersion
) : RuntimeException(
        Translations.getString("application.tooOld.text", CurrentApplicationVersion.codeVersion.toString(), dataVersion.toString())
)