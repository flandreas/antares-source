package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.DrawableMockBuilder
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

/**
 * Unit tests for [DrawableContainerImpl].
 */
class DrawableContainerImplTest {

    private lateinit var container: DrawableContainerImpl<Drawable>
    private lateinit var context: DrawContext

    @Before
    fun setup() {
        BaseModuleJvm.require()
        container = DrawableContainerImpl()
        context = Mockito.mock(DrawContext::class.java)
    }

    @Test
    fun shouldAddDrawable() {
        val drawable = DrawableMockBuilder().build()
        container.add(drawable)
        Assert.assertTrue(container.contains(drawable))
    }

    @Test
    fun shouldNotContainUnaddedDrawable() {
        val drawable = DrawableMockBuilder().build()
        Assert.assertFalse(container.contains(drawable))
    }

    @Test
    fun shouldDrawVisibleDrawables() {
        val drawable = DrawableMockBuilder().build()
        container.add(drawable)
        container.draw(context)
        Mockito.verify(drawable, Mockito.atLeastOnce()).draw(context)
    }

    @Test
    fun shouldNotDrawInvisibleDrawables() {
        val drawable = DrawableMockBuilder().invisible().build()
        container.add(drawable)
        container.draw(context)
        Mockito.verify(drawable, Mockito.never()).draw(context)
    }

    @Test
    fun shouldIterateBackToFront() {
        val drawable1 = DrawableMockBuilder().build()
        val drawable2 = DrawableMockBuilder().build()
        val drawable3 = DrawableMockBuilder().build()
        container.add(drawable1).add(drawable2).add(drawable3)

        val iter = container.backToFrontIterator()

        assertThat(iter.hasNext(), `is`(true))
        assertThat(iter.next(), `is`(`sameInstance`(drawable1)))
        assertThat(iter.next(), `is`(`sameInstance`(drawable2)))
        assertThat(iter.next(), `is`(`sameInstance`(drawable3)))
    }

    @Test
    fun shouldSetStackingOrderPositionLower() {
        val d1 = DrawableMockBuilder().build()
        val d2 = DrawableMockBuilder().build()
        val d3 = DrawableMockBuilder().build()
        container.add(d3).add(d2).add(d1)

        container.setStackingOrderPosition(1, d3)

        assertThat(container.getStackingOrderPosition(d1), `is`(0))
        assertThat(container.getStackingOrderPosition(d3), `is`(1))
        assertThat(container.getStackingOrderPosition(d2), `is`(2))
    }

    @Test
    fun shouldSetStackingOrderPositionHigher() {
        val d1 = DrawableMockBuilder().build()
        val d2 = DrawableMockBuilder().build()
        val d3 = DrawableMockBuilder().build()
        container.add(d3).add(d2).add(d1)

        container.setStackingOrderPosition(2, d2)

        assertThat(container.getStackingOrderPosition(d1), `is`(0))
        assertThat(container.getStackingOrderPosition(d3), `is`(1))
        assertThat(container.getStackingOrderPosition(d2), `is`(2))
    }

    @Test
    fun shouldSetStackingOrderPositionUnchanged() {
        val d1 = DrawableMockBuilder().build()
        val d2 = DrawableMockBuilder().build()
        val d3 = DrawableMockBuilder().build()
        container.add(d3).add(d2).add(d1)

        container.setStackingOrderPosition(1, d2)

        assertThat(container.getStackingOrderPosition(d1), `is`(0))
        assertThat(container.getStackingOrderPosition(d2), `is`(1))
        assertThat(container.getStackingOrderPosition(d3), `is`(2))
    }

    @Test
    fun shouldBringToFront() {
        val d1 = DrawableMockBuilder().build()
        val d2 = DrawableMockBuilder().build()
        val d3 = DrawableMockBuilder().build()
        container.add(d3).add(d2).add(d1)

        container.toFront(listOf(d3))

        assertThat(container.getStackingOrderPosition(d3), `is`(0))
        assertThat(container.getStackingOrderPosition(d1), `is`(1))
        assertThat(container.getStackingOrderPosition(d2), `is`(2))
    }

    @Test
    fun shouldBringToBack() {
        val d1 = DrawableMockBuilder().build()
        val d2 = DrawableMockBuilder().build()
        val d3 = DrawableMockBuilder().build()
        container.add(d3).add(d2).add(d1)

        container.toBack(listOf(d1, d2))

        assertThat(container.getStackingOrderPosition(d3), `is`(0))
        assertThat(container.getStackingOrderPosition(d1), `is`(1))
        assertThat(container.getStackingOrderPosition(d2), `is`(2))
    }
}