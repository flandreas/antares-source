package io.antarescircuit.jabbah.app

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class AbstractDesktopApplicationTest {
	@get:Rule
	val temporaryFolder = TemporaryFolder()

	@Test
	fun shouldUseXdgDataHomeOnLinux() {
		val home = temporaryFolder.newFolder("home").toPath()
		val dataHome = temporaryFolder.newFolder("user applications").toPath()

		assertEquals(dataHome.toString(), AbstractDesktopApplication.getLinuxAppDataDirectory(home.toString(), dataHome.toString()))
	}

	@Test
	fun shouldUseLocalShareWhenXdgDataHomeIsUnset() {
		val home = temporaryFolder.newFolder("home").toPath()

		assertEquals(home.resolve(".local/share").toString(), AbstractDesktopApplication.getLinuxAppDataDirectory(home.toString(), null))
	}

	@Test
	fun shouldUseLocalShareWhenXdgDataHomeIsEmpty() {
		val home = temporaryFolder.newFolder("home").toPath()

		assertEquals(home.resolve(".local/share").toString(), AbstractDesktopApplication.getLinuxAppDataDirectory(home.toString(), ""))
	}

	@Test
	fun shouldIgnoreRelativeXdgDataHome() {
		val home = temporaryFolder.newFolder("home").toPath()

		assertEquals(home.resolve(".local/share").toString(), AbstractDesktopApplication.getLinuxAppDataDirectory(home.toString(), "relative/data"))
	}

	@Test
	fun shouldKeepExistingLinuxDataDirectory() {
		val home = temporaryFolder.newFolder("home").toPath()
		val legacyPath = Files.createDirectory(home.resolve("Antares"))

		assertEquals(legacyPath, AbstractDesktopApplication.determineLinuxUserDataDirectoryPath("Antares", home.toString(), null))
	}

	@Test
	fun shouldPreferExistingLinuxDataDirectoryEvenWhenXdgDirectoryExists() {
		val home = temporaryFolder.newFolder("home").toPath()
		val legacyPath = Files.createDirectory(home.resolve("Antares"))
		val xdgDataHome = temporaryFolder.newFolder("data").toPath()
		Files.createDirectory(xdgDataHome.resolve("Antares"))

		assertEquals(legacyPath, AbstractDesktopApplication.determineLinuxUserDataDirectoryPath("Antares", home.toString(), xdgDataHome.toString()))
	}

	@Test
	fun shouldUseLocalShareForNewLinuxUsersAndSubsequentStarts() {
		val home = temporaryFolder.newFolder("home").toPath()
		val expected = home.resolve(".local/share/Antares")

		assertEquals(expected, AbstractDesktopApplication.determineLinuxUserDataDirectoryPath("Antares", home.toString(), null))
		Files.createDirectories(expected)
		assertEquals(expected, AbstractDesktopApplication.determineLinuxUserDataDirectoryPath("Antares", home.toString(), null))
	}

	@Test
	fun shouldUseXdgDataHomeForNewLinuxUsers() {
		val home = temporaryFolder.newFolder("home").toPath()
		val xdgDataHome = temporaryFolder.newFolder("data").toPath()

		assertEquals(xdgDataHome.resolve("Antares"), AbstractDesktopApplication.determineLinuxUserDataDirectoryPath("Antares", home.toString(), xdgDataHome.toString()))
	}

	@Test
	fun shouldIgnoreLegacyPathWhenItIsAFile() {
		val home = temporaryFolder.newFolder("home").toPath()
		Files.createFile(home.resolve("Antares"))

		assertEquals(home.resolve(".local/share/Antares"), AbstractDesktopApplication.determineLinuxUserDataDirectoryPath("Antares", home.toString(), null))
	}

	@Test
	fun shouldHonorExplicitAppDirectory() {
		val appDir = temporaryFolder.newFolder("custom").toPath()
		val options = AbstractDesktopApplication.defineOptions(Options())
		val commandLine = DefaultParser().parse(options, arrayOf("--appDir", appDir.toString()))
		val previousLogFile = System.getProperty("system.logFile")
		try {
			assertEquals(appDir, AbstractDesktopApplication.determineAppDataDirectoryPath(commandLine, "Antares"))
			assertEquals(appDir.resolve("Antares.log").toString(), System.getProperty("system.logFile"))
		} finally {
			if (previousLogFile == null) {
				System.clearProperty("system.logFile")
			} else {
				System.setProperty("system.logFile", previousLogFile)
			}
		}
	}
}
