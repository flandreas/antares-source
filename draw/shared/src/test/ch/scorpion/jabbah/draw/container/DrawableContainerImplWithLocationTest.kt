package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DrawableContainerImplWithLocationTest {

	private lateinit var container: DrawableContainerImpl<Drawable>
	private lateinit var context: DrawContext

	@BeforeTest
	fun setup() {
		BaseModule.require()
		container = DrawableContainerImpl(Point2D(100, 100), useLocation = true)
		context = DrawContext(mock<Graphics2D>(MockMode.autofill))
	}

	@Test
	fun shouldCalculateBoundingBox() {
		container.add(TestRectangle(Rectangle2D(0, 0, 10, 10)))
		container.add(TestRectangle(Rectangle2D(50, 50, 20, 20)))
		assertEquals(Rectangle2D(100, 100, 70, 70), container.boundingBox)
	}
}