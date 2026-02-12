package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.draw.CanvasMockBuilder
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.editor.EditorImpl
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanelController
import ch.scorpion.jabbah.edit.select.SelectionManagerImpl
import ch.scorpion.jabbah.edit.select.SelectionModelFactoryMockBuilder
import ch.scorpion.jabbah.edit.select.SimpleSelectionModelProvider
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ComponentPropertyPanelControllerTest {

	private val selectionModelFactorMockBuilder = SelectionModelFactoryMockBuilder()
	private val selectionManagerFactory: SelectionManagerFactory = { SelectionManagerImpl(it, SimpleSelectionModelProvider(selectionModelFactorMockBuilder.build())) }
	private val drawing: DrawingImpl<Component>
	private val view: DrawingViewImpl<Drawing<Component>>
	private val editor: EditorImpl
	private val controller: ComponentPropertyPanelController
	private val component = ComponentMockBuilder().withType("TestType").build()

	init {
		EditTestRule.configure()
		drawing = DrawingImpl<Component>()
		view = DrawingViewImpl(drawing, selectionManagerFactory = selectionManagerFactory)
		editor = EditorImpl(view as DrawingView<Drawing<Component>>)
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