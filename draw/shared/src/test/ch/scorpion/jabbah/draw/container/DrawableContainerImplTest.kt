package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.DrawableMockBuilder
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.*

class DrawableContainerImplTest {

	private lateinit var container: DrawableContainerImpl<Drawable>
	private lateinit var context: DrawContext

	@BeforeTest
	fun setup() {
		BaseModule.require()
		container = DrawableContainerImpl()
		context = mockk(relaxed = true)
	}

	@Test
	fun shouldAddDrawable() {
		val drawable = DrawableMockBuilder().build()
		container.add(drawable)
		assertTrue(container.contains(drawable))
	}

	@Test
	fun shouldNotContainUnaddedDrawable() {
		val drawable = DrawableMockBuilder().build()
		assertFalse(container.contains(drawable))
	}

	@Test
	fun shouldDrawVisibleDrawables() {
		val drawable = DrawableMockBuilder().build()
		container.add(drawable)
		container.draw(context)
		verify(atLeast = 1) { drawable.draw(context) }
	}

	@Test
	fun shouldNotDrawInvisibleDrawables() {
		val drawable = DrawableMockBuilder().invisible().build()
		container.add(drawable)
		container.draw(context)
		verify(exactly = 0) { drawable.draw(context) }
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
}