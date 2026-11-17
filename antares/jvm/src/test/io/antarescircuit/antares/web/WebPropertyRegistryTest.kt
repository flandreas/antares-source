package io.antarescircuit.antares.web

import io.antarescircuit.antares.AntaresJvmTestRule
import io.antarescircuit.antares.standardlibrary.AbstractStandardLibraryBasedCircuitTest
import io.antarescircuit.antares.view.DigitalGraphView
import io.antarescircuit.antares.view.app.AntaresGraphViewService
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingViewMockBuilder
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.edit.properties.AbstractBeanInfo
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.param.GraphParamValuePropertySwing
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeView
import com.l2fprod.common.propertysheet.Property
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import java.io.File
import java.lang.reflect.Modifier
import java.util.zip.ZipFile
import kotlin.reflect.full.primaryConstructor
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Compares the web editor's [WebPropertyRegistry] with the desktop's BeanInfos, so that properties added to or
 * removed from a BeanInfo fail here until the web table follows (or the difference is listed below).
 *
 * Beans are the components of all standard library circuits and symbols (realistic states), plus a default
 * instance of each other bean class that has a BeanInfo. Only property names/keys are compared, not their order.
 */
class WebPropertyRegistryTest {

	companion object {
		private const val PARAM = "param.*"

		/** BeanInfos whose beans are not shown in the web inspector, with the reason. */
		private val NOT_COMPARED = mapOf(
			"MultiSelectionBeanInfo" to "multi-selections are combined from the single beans' properties by WebProperties",
			"MemoryStorableBeanInfo" to "memory items: name via library rename, widths and contents in WebMemory",
			"BooleanExpressionStorableBeanInfo" to "Boolean expressions are edited in WebTruthTables",
			"TruthTableBeanInfo" to "truth tables are edited in WebTruthTables",
			"TestcaseBeanInfo" to "test cases are edited in WebTestcases",
			"ScenarioImplBeanInfo" to "scenarios are edited in WebScenarios",
			"ScenarioStepImplBeanInfo" to "scenario steps are edited in WebScenarios",
			"UsecaseImplBeanInfo" to "use cases are edited in WebScenarios",
			"ImageIdentificationBeanInfo" to "images are renamed as library items (WebLibraries.rename)",
		)

		/**
		 * Intentional differences per bean class (simple name): keys only the desktop shows (`-key`)
		 * or only the web shows (`+key`).
		 */
		private val EXPECTED_DIFFERENCES = mapOf(
			// Parameter definitions are edited in the circuit parameters dialog (WebParams), not in the inspector
			"DigitalGraphView" to setOf("-graph.parameterDefinitions"),
			"AnalogGraphView" to setOf("-graph.parameterDefinitions"),
			"GraphViewImpl" to setOf("-graph.parameterDefinitions"),
			// The stored location of a transition is unused: its geometry follows the connected states
			"FSMTransition" to setOf("-location"),
		)
	}

	init {
		AntaresJvmTestRule.configure()
		EditModule.drawingAppService = AntaresGraphViewService()
		AbstractStandardLibraryBasedCircuitTest.setupLibrary()
	}

	@Test
	fun shouldOfferTheDesktopPropertiesOfAllBeans() {
		val instances = libraryBeans()
		val differences = mutableMapOf<String, Set<String>>()
		val notCovered = mutableListOf<String>()

		beanInfoClassNames().forEach { beanInfoName ->
			if (beanInfoName.substringAfterLast('.') in NOT_COMPARED) return@forEach
			val beanClass = try {
				Class.forName(beanInfoName.removeSuffix("BeanInfo"))
			} catch (_: ClassNotFoundException) {
				null
			}
			// Base BeanInfos (e.g. VerticeViewBeanInfo) are compared through their subclasses' beans
			if (beanClass == null || beanClass.isInterface || Modifier.isAbstract(beanClass.modifiers)) return@forEach
			val beans = instances[beanClass] ?: listOfNotNull(defaultInstance(beanClass))
			if (beans.isEmpty()) {
				notCovered.add(beanInfoName)
				return@forEach
			}
			@Suppress("UNCHECKED_CAST")
			val beanInfo = Class.forName(beanInfoName).kotlin.primaryConstructor!!.call() as AbstractBeanInfo<Any>
			val desktop = beans.flatMap { (bean, drawing) -> beanInfo.getProperties(bean, editorFor(drawing)).map(::desktopKey) }.toSet()
			val web = beans.flatMap { (bean, _) -> WebPropertyRegistry.propertiesOf(bean).map { webKey(it.key) } }.toSet()
			val difference = (desktop - web).map { "-$it" }.toSet() + (web - desktop).map { "+$it" }
			if (difference.isNotEmpty()) differences[beanClass.simpleName] = difference
		}

		assertEquals(emptyList(), notCovered, "BeanInfos without bean instance: add them to NOT_COMPARED with a reason")
		assertEquals(EXPECTED_DIFFERENCES.toSortedMap(), differences.toSortedMap())
	}

	/** The components (and drawings) of all standard library circuits and symbols, by bean class. */
	private fun libraryBeans(): Map<Class<*>, List<Pair<Any, Drawing<*>>>> {
		val library = LibraryModule.libraryHolder.library
		return library.metaGraphIds
			.map { library.getMetaGraph(it) }
			.flatMap { listOf(it.graph.graphView, it.containerDrawing) }
			.flatMap { drawing -> (listOf<Any>(drawing) + drawing.getDrawables { true }.map { (it as Component).propertyOwner }).map { it to drawing } }
			.groupBy { it.first.javaClass }
	}

	/** A bean created by its default constructor, placed on a digital circuit. */
	private fun defaultInstance(beanClass: Class<*>): Pair<Any, Drawing<*>>? {
		val drawing = DigitalGraphView()
		val bean = if (beanClass == OscilloscopeView::class.java) {
			// Created within a circuit, an oscilloscope has the circuit's type
			OscilloscopeView(drawing.graph!!.type)
		} else {
			beanClass.kotlin.constructors.firstOrNull { c -> c.parameters.all { it.isOptional } }?.callBy(emptyMap()) ?: return null
		}
		if (bean is GraphElementView<*>) drawing.add(bean)
		return bean to drawing
	}

	private fun editorFor(drawing: Drawing<*>): Editor {
		val editor = mock<Editor>(MockMode.autofill)
		every { editor.view } returns DrawingViewMockBuilder().build<Component, Drawing<Component>>()
		every { editor.active } returns true
		@Suppress("UNCHECKED_CAST")
		every { editor.drawing } returns drawing as Drawing<Component>
		every { editor.commandManager } returns mock<CommandManager>(MockMode.autofill)
		return editor
	}

	/** Generic parameter values are named by type on the desktop and `param.<name>` on the web. */
	private fun desktopKey(property: Property): String = if (property is GraphParamValuePropertySwing<*>) PARAM else property.name

	private fun webKey(key: String): String = if (key.startsWith("param.")) PARAM else key

	/** All BeanInfo classes of the project on the test classpath. */
	private fun beanInfoClassNames(): List<String> = System.getProperty("java.class.path").split(File.pathSeparator)
		.map(::File)
		.flatMap { entry ->
			when {
				entry.isDirectory -> entry.walk().filter { it.isFile }.map { it.relativeTo(entry).invariantSeparatorsPath }.toList()
				entry.name.endsWith(".jar") && entry.isFile -> ZipFile(entry).use { zip -> zip.entries().toList().map { it.name } }
				else -> emptyList()
			}
		}
		.filter { it.startsWith("io/antarescircuit/") && it.endsWith("BeanInfo.class") }
		.map { it.removeSuffix(".class").replace('/', '.') }
		.distinct()
}
