package io.antarescircuit.jabbah.edit.ui

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.draw.CanvasMockBuilder
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.editor.EditorImpl
import io.antarescircuit.jabbah.edit.model.DrawingImpl
import io.antarescircuit.jabbah.edit.properties.ComponentPropertyPanelController
import io.antarescircuit.jabbah.edit.select.SelectionManagerImpl
import io.antarescircuit.jabbah.edit.select.SelectionModelFactoryMockBuilder
import io.antarescircuit.jabbah.edit.select.SimpleSelectionModelProvider
import io.antarescircuit.jabbah.edit.view.DrawingViewImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ComponentPropertyPanelControllerTest {

	private val selectionModelFactorMockBuilder = SelectionModelFactoryMockBuilder()
	private val selectionManagerFactory: SelectionManagerFactory = { SelectionManagerImpl(it, SimpleSelectionModelProvider(selectionModelFactorMockBuilder.build())) }
	private val drawing: DrawingImpl<Component>
	private val view: DrawingViewImpl<Component, Drawing<Component>>
	private val editor: EditorImpl
	private val controller: ComponentPropertyPanelController
	private val component = ComponentMockBuilder().withType("TestType").build()

	init {
		EditTestRule.configure()
		drawing = DrawingImpl<Component>()
		view = DrawingViewImpl(drawing, selectionManagerFactory = selectionManagerFactory)
		editor = EditorImpl(view as DrawingView<Component, Drawing<Component>>)
		controller = ComponentPropertyPanelController(editor)

		view.canvas = CanvasMockBuilder().withView(view).build()

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