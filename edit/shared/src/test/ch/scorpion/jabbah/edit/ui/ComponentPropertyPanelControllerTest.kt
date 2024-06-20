package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.editor.EditorImpl
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanelController
import ch.scorpion.jabbah.edit.select.SelectionManagerImpl
import ch.scorpion.jabbah.edit.select.SelectionModelFactoryMockBuilder
import ch.scorpion.jabbah.edit.select.SimpleSelectionModelProvider
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ComponentPropertyPanelControllerTest {

	companion object {
		init {
			EditTestRule.configure()
		}
	}

	private val selectionModelFactorMockBuilder = SelectionModelFactoryMockBuilder()
	private val selectionManagerFactory: SelectionManagerFactory = { SelectionManagerImpl(it, SimpleSelectionModelProvider(selectionModelFactorMockBuilder.build())) }
	private val drawing = DrawingImpl<Component>()
	private val view = DrawingViewImpl(drawing, selectionManagerFactory = selectionManagerFactory)
	private val editor = EditorImpl(view as DrawingView<Drawing<Component>>)
	private val controller = ComponentPropertyPanelController(editor)
	private val component = ComponentMockBuilder().withType("TestType").build()

	init {
		val canvas = mock<Canvas>(MockMode.autofill)
		every { canvas.dimension } returns Dimension2D(1000, 1000)
		every { canvas.devicePixelRatio } returns 1
		view.canvas = canvas
		ComponentPropertyPanelMockBuilder(controller)
		drawing.add(component)
		editor.active = true
	}

	@Test
	fun shouldDisplaySelectedComponentProperties() {
		view.selectionManager.select(component)

		assertSame(component, controller.bean)
		assertEquals(Translations.getString("edit.property.bean", "TestType"), controller.title)
	}

	@Test
	fun shouldDisplayDrawingProperties() {
		view.selectionManager.select(component)
		view.selectionManager.deselectAll()
		assertSame(editor.drawing, controller.bean)
	}
}