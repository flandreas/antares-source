package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.base.geom.AffineTransformJvm
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Path2DJvm
import ch.scorpion.jabbah.base.time.RealTimeTimerJvm
import ch.scorpion.jabbah.base.time.Timer
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.SystemUtils
import java.awt.Desktop
import java.awt.Frame
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import kotlin.reflect.KClass

/** Implements the [System] interface on the Java virtual machine platform.*/
actual object System {

	/** ---- [System] interface */

	actual var invoker: (() -> Unit) -> Unit = { invocable -> SwingUtilities.invokeLater { invocable.invoke() } }

	actual fun createTimer(): Timer = RealTimeTimerJvm()

	actual fun currentTimeMillis(): Long = java.lang.System.currentTimeMillis()

	actual fun getClassName(clazz: KClass<*>): String = clazz.qualifiedName!!

	actual fun getClassName(obj: Any): String = obj::class.qualifiedName!!

	actual fun commonSuperClass(classes: Collection<KClass<*>>): KClass<*>? = Inheritance.commonSuperClass(classes)

	actual fun getClass(obj: Any): KClass<*> = obj.javaClass.kotlin

	actual fun <T : Any> instantiate(clazz: KClass<T>): T = clazz.java.getDeclaredConstructor().newInstance()

	actual fun createAffineTransform(): AffineTransform = AffineTransformJvm()

	actual fun createPath(): Path = Path2DJvm()

	actual fun createUUID(uuid: String?): UUID {
		if (uuid == null) {
			return UUID(java.util.UUID.randomUUID().toString())
		}
		return UUID(uuid)
	}

	actual fun invokeLater(invocable: () -> Unit) {
		invoker.invoke(invocable)
	}

	actual fun getActionAcceleratorKey(baseName: String): String =
		if (SystemUtils.IS_OS_MAC) {
			"$baseName.accelerator.osx"
		} else {
			"$baseName.accelerator"
		}

	actual fun currentLanguage(): Language {
		val code = java.lang.System.getProperty("user.language")
		if (Language.supports(code)) {
			return Language.withCode(code)
		}
		return Language.DEFAULT
	}

	actual fun browse(url: String, actionName: String) {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			Desktop.getDesktop().browse(URI(url))
		} else {
			JOptionPane.showConfirmDialog(
				Frame.getFrames()[0],
				Translations.getString("application.cannotOpenUrl.name"),
				actionName,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.ERROR_MESSAGE)
		}
	}

	actual fun printStackTrace() {
		RuntimeException().printStackTrace()
	}

	actual fun breakpoint(condition: () -> Boolean) {
		if (condition()) {
			println("break")
		}
	}

	actual fun getFileContents(path: String): String? {
		return Files.readString(Paths.get(path))
	}
}