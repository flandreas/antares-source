package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.DrawableMockBuilder
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import dev.mokkery.*
import dev.mokkery.verify.VerifyMode.Companion.exactly
import kotlin.test.*

class DrawableContainerImplTest {

	private lateinit var container: DrawableContainerImpl<Drawable>
	private lateinit var context: DrawContext

	@BeforeTest
	fun setup() {
		BaseModule.require()
		container = DrawableContainerImpl()
		context = DrawContext(mock<Graphics2D>())
	}

	@Test
	fun shouldAddDrawable() {
		val drawable = DrawableMockBuilder().build()
		container.add(drawable)
		assertTrue(container.contains(drawable))
	}

	@Test
	fun shouldNotContainUnAddedDrawable() {
		val drawable = DrawableMockBuilder().build()
		assertFalse(container.contains(drawable))
	}

	@Test
	fun shouldNotDrawInvisibleDrawables() {
		val drawable = DrawableMockBuilder()
			.withBoundingBox(Rectangle2D(0, 0, 100, 100))
			.invisible()
			.build()
		container.add(drawable)
		context.modelClip = Rectangle2D(0, 0, 50, 50)

		container.draw(context)

		verify(exactly(0)) { drawable.draw(context) }
	}

	@Test
	fun shouldIterateBackToFront() {
		val drawable1 = DrawableMockBuilder().build()
		val drawable2 = DrawableMockBuilder().build()
		val drawable3 = DrawableMockBuilder().build()
		container.add(drawable1).add(drawable2).add(drawable3)

		val iter = container.backToFrontIterator()

		assertTrue(iter.hasNext())
		assertSame(iter.next(), drawable1)
		assertSame(iter.next(), drawable2)
		assertSame(iter.next(), drawable3)
	}

	@Test
	fun shouldDirectlyContainAt() {
		container.add(TestRectangle(Rectangle2D(100, 100, 10, 10)))
		assertTrue(container.contains(105.0, 105.0))
		assertFalse(container.contains(5.0, 5.0))
	}

	@Test
	fun shouldGetDrawableAt() {
		val rect = TestRectangle(Rectangle2D(100, 100, 10, 10))
		container.add(rect)
		assertEquals(rect, container.getDrawableAt(105.0, 105.0) as TestRectangle)
	}

	@Test
	fun shouldUpdateBoundingBox() {
		container.add(DrawableMockBuilder().withBoundingBox(Rectangle2D(0, 0, 10, 10)).build())
		container.add(DrawableMockBuilder().withBoundingBox(Rectangle2D(10, 10, 10, 10)).build())
		assertEquals(Rectangle2D(0, 0, 20, 20), container.boundingBox)
	}

	@Test
	fun shouldNotAddInvisibleToBoundingBox() {
		container.add(DrawableMockBuilder().withBoundingBox(Rectangle2D(0, 0, 10, 10)).invisible().build())
		container.add(DrawableMockBuilder().withBoundingBox(Rectangle2D(10, 10, 10, 10)).build())
		assertEquals(Rectangle2D(10, 10, 10, 10), container.boundingBox)
	}

	@Test
	fun shouldClip() {
		val rect1 = spy<Drawable>(TestRectangle(Rectangle2D(0, 0, 10, 10)))
		val rect2 = spy<Drawable>(TestRectangle(Rectangle2D(90, 90, 10, 10)))
		container.add(rect1)
		container.add(rect2)
		context.modelClip = Rectangle2D(5, 5, 10, 10)

		container.draw(context)

		verify(exactly(1)) { rect1.draw(context) }
		verify(exactly (0)) { rect2.draw(context) }
	}
}