package io.antarescircuit.jabbah.draw.graphics

/**
 * Platform-specific interface for loading an [Image] from external storage
 * and instantiating the appropriate implementing object, depending on the platform
 * and "raster" vs "vector" images.
 *
 * Depending on whether the [Image] is one provided by the system, or one that can be imported
 * and changed by the user, the [Image] might be stored in a different location. For example,
 * on the JVM platform, immutable system images are typically stored in a JAR file, while
 * user images are stored in a file system directory.
 *
 * Interpretation of the `path` parameters in the `load` methods depend on the platform
 * and the physical properties of the external storage.
 *
 * The `type` parameter can be used create the corresponding [Image] object,
 * e.g. distinguishing between raster and vector images.
 */
interface ImageLoader {

    fun loadSystemImage(path: String, type: ImageType): Image

    fun loadUserImage(path: String, type: ImageType): Image
}