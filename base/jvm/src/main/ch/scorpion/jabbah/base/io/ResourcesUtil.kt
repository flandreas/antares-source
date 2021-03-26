package ch.scorpion.jabbah.base.io

import java.net.URI
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

object ResourcesUtil {

	/**
	 * Copies a directory in a JAR file recursively to a location in the file system.
	 * Source: https://stackoverflow.com/questions/1386809/copy-directory-from-a-jar-file
	 */
	fun copyFromJar(sourcePath: String, target: Path) {
		val pathReference = PathReference.getPath(URI(sourcePath))
		val jarPath = pathReference.path

		Files.walkFileTree(jarPath, object : SimpleFileVisitor<Path>()  {
			lateinit var currentTarget: Path

			override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes?): FileVisitResult {
				currentTarget = target.resolve(jarPath.relativize(dir).toString())
				Files.createDirectories(currentTarget)
				return FileVisitResult.CONTINUE
			}

			override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
				Files.copy(file, target.resolve(jarPath.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING)
				return FileVisitResult.CONTINUE
			}
		})
	}
}

private class PathReference(
	val path: Path,
	private val fileSystem: FileSystem?
) : AutoCloseable {

	companion object {
		fun getPath(resourcePath: URI): PathReference {
			return try {
				// first try getting a path via existing file systems
				PathReference(Paths.get(resourcePath), null)
			} catch (e: FileSystemNotFoundException) {
				// next try JAR
				val fileSystem = FileSystems.newFileSystem(resourcePath, emptyMap<String, Any>())
				PathReference(fileSystem.provider().getPath(resourcePath), fileSystem)
			}
		}
	}

	override fun close() {
		fileSystem?.close()
	}
}