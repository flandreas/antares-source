package io.antarescircuit.jabbah.app

import dev.mokkery.MockMode
import dev.mokkery.mock
import io.antarescircuit.jabbah.app.module.AppModuleJvm
import io.antarescircuit.jabbah.base.event.EventBusImpl
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.edit.properties.applicationDataBeanProvider
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.apache.commons.lang3.SystemUtils
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopApplicationWorkspaceTest {

	@get:Rule
	val temporaryFolder = TemporaryFolder()

	@Test
	fun shouldUseMacApplicationSupportDirectoryOnFirstStart() {
		assumeTrue(SystemUtils.IS_OS_MAC)
		val home = temporaryFolder.newFolder("home").toPath()
		val previousHome = System.getProperty("user.home")
		try {
			System.setProperty("user.home", home.toString())
			withInitialWorkspaceSetting("") {
				val expected = home.resolve("Library/Application Support/Antares")
				assertFalse(Files.exists(expected))

				val application = createApplication()

				assertEquals(expected, application.defaultUserDataDirectoryPath)
				assertWorkspace(expected)
			}
		} finally {
			System.setProperty("user.home", previousHome)
		}
	}

	@Test
	fun shouldCreateDefaultWorkspaceWhenSettingIsMissing() {
		val appDir = temporaryFolder.root.toPath().resolve("new/app data")
		withInitialWorkspaceSetting(null) {
			createApplication("--appDir", appDir.toString())

			assertWorkspace(appDir)
		}
	}

	@Test
	fun shouldUseDefaultWorkspaceWhenSavedSettingIsBlank() {
		val appDir = temporaryFolder.newFolder("app data").toPath()
		storeWorkspaceSetting(appDir, " \t")

		withInitialWorkspaceSetting("") {
			createApplication("--appDir", appDir.toString())

			assertWorkspace(appDir)
		}
	}

	@Test
	fun shouldKeepSavedWorkspace() {
		val appDir = temporaryFolder.newFolder("app data").toPath()
		val workspace = temporaryFolder.newFolder("saved workspace").toPath()
		storeWorkspaceSetting(appDir, workspace.toString())

		withInitialWorkspaceSetting("") {
			createApplication("--appDir", appDir.toString())

			assertWorkspace(workspace)
		}
	}

	@Test
	fun shouldPreferExplicitWorkspaceOverSavedWorkspace() {
		val appDir = temporaryFolder.newFolder("app data").toPath()
		val savedWorkspace = temporaryFolder.newFolder("saved workspace").toPath()
		val explicitWorkspace = temporaryFolder.newFolder("explicit workspace").toPath()
		storeWorkspaceSetting(appDir, savedWorkspace.toString())

		withInitialWorkspaceSetting("") {
			createApplication("--appDir", appDir.toString(), "--userDir", explicitWorkspace.toString())

			assertWorkspace(explicitWorkspace)
		}
	}

	private fun createApplication(vararg args: String): AbstractDesktopApplication {
		val commandLine = DefaultParser().parse(AbstractDesktopApplication.defineOptions(Options()), args)
		val controller = ApplicationDataViewController(
			commandManager = mock<CommandManager>(MockMode.autofill),
			newStorableProvider = { error("Unused during startup") },
			repository = mock<ApplicationDataRepository<Savable>>(),
			eventBus = EventBusImpl(),
		)
		return object : AbstractDesktopApplication(commandLine, controller) {
			override val displayName: String get() = "Antares"
			override val fileExtension: String get() = "cir"
			override val isFirstUsage: Boolean get() = true
			override val codeVersion: ApplicationVersion get() = CurrentApplicationVersion.codeVersion
			override val dataVersion: ApplicationVersion get() = CurrentApplicationVersion.dataVersion
			override fun start() {}
			override fun shutdownUI() {}
		}
	}

	private fun assertWorkspace(expected: Path) {
		assertTrue(Files.isDirectory(expected))
		assertEquals(expected.toAbsolutePath().toString(), AppModuleJvm.workspaceHolder.userDataDirectoryPath)
		assertEquals(expected.toAbsolutePath().toString(), BaseModule.settings.get(WorkspaceHolder.PROP_WORKSPACE))
	}

	private fun storeWorkspaceSetting(appDir: Path, workspace: String) {
		val properties = Properties()
		properties.setProperty(WorkspaceHolder.PROP_WORKSPACE, workspace)
		Files.newOutputStream(appDir.resolve("Antares.ini")).use { properties.store(it, null) }
	}

	private fun withInitialWorkspaceSetting(value: String?, block: () -> Unit) {
		val holder = AppModuleJvm.workspaceHolder
		val previousWorkspace = holder.workspace
		val previousSetting = BaseModule.settings.getString(WorkspaceHolder.PROP_WORKSPACE, "")
		val hadSetting = BaseModule.settings.containsKey(WorkspaceHolder.PROP_WORKSPACE)
		val previousLogFile = System.getProperty("system.logFile")
		val previousUserHolder = runCatching { EditAuthModule.userHolder }.getOrNull()
		val previousBeanProvider = applicationDataBeanProvider
		try {
			if (value == null) {
				BaseModule.settings.remove(WorkspaceHolder.PROP_WORKSPACE)
			} else {
				BaseModule.settings.set(WorkspaceHolder.PROP_WORKSPACE, value)
			}
			block()
		} finally {
			holder.initializeWorkspace(previousWorkspace)
			if (hadSetting) {
				BaseModule.settings.set(WorkspaceHolder.PROP_WORKSPACE, previousSetting)
			} else {
				BaseModule.settings.remove(WorkspaceHolder.PROP_WORKSPACE)
			}
			if (previousLogFile == null) {
				System.clearProperty("system.logFile")
			} else {
				System.setProperty("system.logFile", previousLogFile)
			}
			previousUserHolder?.let { EditAuthModule.userHolder = it }
			applicationDataBeanProvider = previousBeanProvider
		}
	}
}
