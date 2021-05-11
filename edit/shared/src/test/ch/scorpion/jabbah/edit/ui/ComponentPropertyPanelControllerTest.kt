package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.editor.EditorImpl
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.select.SelectionManagerImpl
import ch.scorpion.jabbah.edit.select.SelectionModelFactoryMockBuilder
import ch.scorpion.jabbah.edit.select.SimpleSelectionModelProvider
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
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
	private val selectionManagerFactory = object : SelectionManagerFactory {
		override fun create(content: DrawingViewContent<*>): SelectionManager {
			return SelectionManagerImpl(content, SimpleSelectionModelProvider(selectionModelFactorMockBuilder.build()))
		}
	}
	private val drawing = DrawingImpl<Component>()
	private val view = DrawingViewImpl(drawing, selectionManagerFactory = selectionManagerFactory)
	private val editor = EditorImpl(view as DrawingView<Drawing<Component>>)
	private val controller = ComponentPropertyPanelController(editor)
	private val component = ComponentMockBuilder().withType("TestType").build()

	init {
		ComponentPropertyPanelMockBuilder(controller)
		drawing.add(component)
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
		assertSame(drawing, controller.bean)
	}
}