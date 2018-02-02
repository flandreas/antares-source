package ch.scorpion.jabbah.draw.hellographics

import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.MouseAdapter
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D

/**
 * A simple rectangular [Drawable].
 */
class SimpleRectangle(bounds: Rectangle2D, private val fillColor: Color) : AbstractRectangle(bounds), Locatable {

    @Suppress("unused") constructor(bounds: Rectangle2D): this(bounds, Color(255, 255, 255))

    override val lineWidth: Double get() = 1.0

    override var location: Point2D
        set(value) {
            setBounds(value.x - width / 2, value.y - height / 2, width, height)
        }
        get() {
            return Point2D(bounds.centerX, bounds.centerY)
        }

    override fun draw(context: DrawContext) {
        drawRectangle(context, Color.BLACK, fillColor, Stroke(width = 1.0f))
    }

    override fun moveBy(dx: Double, dy: Double) {
        location = Point2D(location.x + dx, location.y + dy)
    }
}

class Controller(val canvas: Canvas, val model: Model) {
    init {
        canvas.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.button == Button.BUTTON1) {
                    model.moveBallTo(canvas.view.viewToModel(e.location))
                }
            }
        })
    }
}

class Model(
    private val dim: Dimension2D = Dimension2D(800, 600),
    rectangleCount: Int = 100
) {
    companion object {
        private val BALL_SIZE = 10.0
        private val BALL_COLOR = Color.BLACK
        private val MIN_WIDTH = 50.0
        private val MAX_WIDTH = 300.0
        private val MIN_HEIGHT = 50.0
        private val MAX_HEIGHT = 300.0
    }

    val container = DrawableContainerImpl<Drawable>()
    private val ball = SimpleRectangle(Rectangle2D(dim.width / 2, dim.height / 2, BALL_SIZE, BALL_SIZE), BALL_COLOR)
    private val step = 1.0
    private var dirX = step
    private var dirY = step

    init {
        addBorder()
        addContent(rectangleCount)
        addBall(ball)
    }

    private fun addBorder() {
        container.add(SimpleRectangle(Rectangle2D(0.0, 0.0, dim.width, dim.height), Color.WHITE))
    }

    private fun addContent(rectCount: Int) {
        for (i in 1..rectCount) {
            val width = Math.random(MIN_WIDTH, MAX_WIDTH)
            val height = Math.random(MIN_HEIGHT, MAX_HEIGHT)
            val centerX = Math.random(width / 2, dim.width - width / 2)
            val centerY = Math.random(height / 2, dim.height - height / 2)

            val color = Color(
                    Math.random(0.0, 255.0).toInt(),
                    Math.random(0.0, 255.0).toInt(),
                    Math.random(0.0, 255.0).toInt(),
                    Math.random(0.0, 255.0).toInt())

            container.add(SimpleRectangle(Rectangle2D(centerX - width / 2, centerY - height / 2, width, height), color))
        }
    }

    private fun addBall(ball: Drawable) {
        container.add(ball)
    }

    fun animateBall(canvas: Canvas) {
        var x = ball.location.x
        var y = ball.location.y

        if (x + dirX  < ball.bounds.width / 2) {
            x = 0.0 + ball.bounds.width / 2
            dirX = step
        }
        if (y + dirY < ball.bounds.height / 2) {
            y = 0.0 + ball.bounds.height / 2
            dirY = step
        }
        if (x + dirX > dim.width - ball.bounds.width / 2) {
            x = dim.width - ball.bounds.width / 2
            dirX = -step
        }
        if (y + dirY > dim.height - ball.bounds.height / 2) {
            y = dim.height - ball.bounds.height / 2
            dirY = -step
        }

        ball.location = Point2D(x + dirX, y + dirY)
        container.validate()
    }

    fun moveBallTo(p: Point2D) {
        ball.location = Point2D(p.x + dirX, p.y + dirY)
    }
}