package io.antarescircuit.jabbah.base

import io.antarescircuit.jabbah.base.geom.AffineTransform
import io.antarescircuit.jabbah.base.geom.AffineTransformJvm
import io.antarescircuit.jabbah.base.geom.Path
import io.antarescircuit.jabbah.base.geom.Path2DJvm
import io.antarescircuit.jabbah.base.time.JvmTimer
import io.antarescircuit.jabbah.base.time.Timer
import org.apache.commons.lang3.SystemUtils
import java.awt.Desktop
import java.awt.Toolkit
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.reflect.KClass

/** Implements the [System] interface on the Java virtual machine platform.*/
actual object System {

	/** ---- [System] interface */

	actual var invoker: (() -> Unit) -> Unit = { it.invoke() }

	/** Desktop implementations are installed by BaseModuleUiJvm. */
	var timerFactory: () -> Timer = { JvmTimer() }

	var browser: (String, String) -> Unit = { url, _ ->
		check(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			"Opening a browser is not supported"
		}
		Desktop.getDesktop().browse(URI(url))
	}

	var messageHandler: (String, String, Boolean) -> Unit = { _, _, _ ->
		throw UnsupportedOperationException("Modal messages require the desktop UI")
	}

	actual fun beep() = Toolkit.getDefaultToolkit().beep()

	actual fun createTimer(): Timer = timerFactory()

	actual fun currentTimeMillis(): Long = java.lang.System.currentTimeMillis()

	actual fun getClassName(clazz: KClass<*>): String = clazz.qualifiedName!!

	actual fun getClassName(obj: Any): String = obj::class.qualifiedName!!

	actual fun commonSuperClass(classes: Collection<KClass<*>>): KClass<*>? = Inheritance.commonSuperClass(classes)

	actual fun getClass(obj: Any): KClass<*> = obj.javaClass.kotlin

	actual fun identityHashCode(obj: Any): Int = java.lang.System.identityHashCode(obj)

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
		browser(url, actionName)
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

	actual fun showModalMessage(title: String, message: String, isError: Boolean) {
		messageHandler(title, message, isError)
	}
}
