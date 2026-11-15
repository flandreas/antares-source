package io.antarescircuit.jabbah.graph.ui

import com.formdev.flatlaf.FlatLightLaf
import dev.mokkery.MockMode.autofill
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.event.EventBusImpl
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.graphics.FontImpl
import io.antarescircuit.jabbah.edit.DrawingViewContent
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.module.GraphViewModuleJvm
import java.awt.Container
import java.awt.event.ContainerAdapter
import java.awt.event.ContainerEvent
import java.awt.event.FocusEvent
import java.awt.event.KeyEvent
import javax.swing.JRootPane
import javax.swing.JTextField
import javax.swing.LookAndFeel
import javax.swing.SwingUtilities
import javax.swing.UIManager
import kotlin.test.*

class NavigationStackViewNameEditorTest {

	private lateinit var previousLookAndFeel: LookAndFeel

	@BeforeTest
	fun setup() {
		SwingUtilities.invokeAndWait {
			previousLookAndFeel = UIManager.getLookAndFeel()
			FlatLightLaf.setup()
		}
	}

	@AfterTest
	fun teardown() {
		SwingUtilities.invokeAndWait {
			UIManager.setLookAndFeel(previousLookAndFeel)
		}
	}

	@Test
	fun shouldReleaseViewAfterSavingUnchangedName() = withView { view, glassPane ->
		val drawing = GraphViewBuilder<Boolean>("Original").build()
		val content = mock<DrawingViewContent<GraphElementView<*>, GraphView>>(autofill)
		every { content.drawing } returns drawing
		view.controller.navigationStack.push(NavigationStackEntry(content = content))
		val editor = startEditor(view, glassPane)
		pressKey(editor, KeyEvent.VK_ENTER)
		assertStopped(editor, glassPane)
	}

	@Test
	fun shouldReleaseViewAfterEscape() = withView { view, glassPane ->
		val editor = startEditor(view, glassPane)
		pressKey(editor, KeyEvent.VK_ESCAPE)
		assertStopped(editor, glassPane)
	}

	@Test
	fun shouldReleaseViewAfterSavingBlankName() = withView { view, glassPane ->
		val editor = startEditor(view, glassPane)
		editor.text = " "
		pressKey(editor, KeyEvent.VK_ENTER)
		assertStopped(editor, glassPane)
	}

	@Test
	fun shouldCancelOnOwnerDisposalEvenAfterViewIsDetached() = withView { view, glassPane ->
		val editor = startEditor(view, glassPane)
		view.parent.remove(view)
		// Removing the editor can trigger focus loss while cancellation is in progress.
		glassPane.addContainerListener(object : ContainerAdapter() {
			override fun componentRemoved(e: ContainerEvent) {
				editor.focusListeners.forEach { it.focusLost(FocusEvent(editor, FocusEvent.FOCUS_LOST)) }
			}
		})
		view.dispose()
		view.dispose()
		assertStopped(editor, glassPane)
	}

	@Test
	fun shouldKeepEditingWhenAnotherViewIsDisposed() = withView { view, glassPane ->
		val editor = startEditor(view, glassPane)
		val otherController = NavigationStackViewController(EventBusImpl())
		try {
			NavigationStackViewSwing(otherController).dispose()
			assertSame(glassPane, editor.parent)
			assertTrue(glassPane.isVisible)
			assertSame(view, editedView())
		} finally {
			otherController.dispose()
		}
	}

	@Test
	fun shouldRemovePreviousOverlayWhenEditingAnotherView() = withView { view, glassPane ->
		val editor = startEditor(view, glassPane)
		withView { otherView, otherGlassPane ->
			assertSame(editor, startEditor(otherView, otherGlassPane))
			assertFalse(glassPane.isVisible)
			assertTrue(glassPane.mouseListeners.isEmpty())
			assertTrue(glassPane.mouseMotionListeners.isEmpty())
			view.dispose()
			assertSame(otherGlassPane, editor.parent)
			assertSame(otherView, editedView())
		}
		assertStopped(editor, glassPane)
	}

	private fun withView(block: (NavigationStackViewSwing, Container) -> Unit) {
		val run = {
			GraphViewModuleJvm.require()
			val controller = NavigationStackViewController(EventBusImpl())
			val view = NavigationStackViewSwing(controller)
			val rootPane = JRootPane()
			rootPane.contentPane.add(view)
			try {
				block(view, rootPane.glassPane as Container)
			} finally {
				controller.dispose()
			}
		}
		if (SwingUtilities.isEventDispatchThread()) run() else SwingUtilities.invokeAndWait(run)
	}

	private fun startEditor(view: NavigationStackViewSwing, glassPane: Container): JTextField {
		NavigationStackViewNameEditor.startEditor(view, UUID("test"), "Original", Point2D.ZERO, FontImpl())
		assertSame(view, editedView())
		return glassPane.components.filterIsInstance<JTextField>().single()
	}

	private fun pressKey(editor: JTextField, code: Int) {
		val event = KeyEvent(editor, KeyEvent.KEY_PRESSED, 0, 0, code, KeyEvent.CHAR_UNDEFINED)
		editor.keyListeners.forEach { it.keyPressed(event) }
	}

	private fun assertStopped(editor: JTextField, glassPane: Container) {
		assertNull(editedView())
		assertNull(editor.parent)
		assertFalse(glassPane.isVisible)
		assertTrue(glassPane.mouseListeners.isEmpty())
		assertTrue(glassPane.mouseMotionListeners.isEmpty())
	}

	private fun editedView(): Any? = NavigationStackViewNameEditor::class.java.getDeclaredField("view").run {
		isAccessible = true
		get(NavigationStackViewNameEditor)
	}
}
