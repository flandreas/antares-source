package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Makes the current [ApplicationVersion] accessible to objects that don't have access to
 * the current desktop [Application], such as top-level persistent objects in higher modules
 * that want to add the version to persistent files.
 */
object CurrentApplicationVersion {

    /** The element name in persistent files holding the serialized representation of an [ApplicationVersion].*/
    private const val PERSISTENT_NAME = "appVersion"

    private val DUMMY_VERSION = ApplicationVersion(ApplicationVersion.DUMMY_VERSION_ID)

    var version: ApplicationVersion = DUMMY_VERSION

    fun write(writer: StoreWriter) {
        writer.writeString(PERSISTENT_NAME, version.toString())
    }

    fun read(reader: StoreReader): ApplicationVersion =
        if (reader.hasAttribute(PERSISTENT_NAME)) {
            ApplicationVersion.parse(reader.readString(PERSISTENT_NAME))
        } else {
            DUMMY_VERSION
        }

    /**
     * Checks if the version attribute in the current element read by [reader] is not newer
     * than the [CurrentApplicationVersion]
     * @throws ApplicationTooOldException if the check fails
     */
    fun check(reader: StoreReader) {
        if (reader.hasAttribute(PERSISTENT_NAME)) {
            val dataVersion = ApplicationVersion.parse(reader.readString(PERSISTENT_NAME))
            if (dataVersion > version) {
                throw ApplicationTooOldException(dataVersion)
            }
        }
    }
}

class ApplicationTooOldException(
    dataVersion: ApplicationVersion
) : RuntimeException(
        Translations.getString("application.tooOld.text", CurrentApplicationVersion.version.toString(), dataVersion.toString())
)