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
class SimpleRectangle(bounds: Rectangle2D, val fillColor: Color) : AbstractRectangle(bounds), Locatable {

    @Suppress("unused") constructor(bounds: Rectangle2D): this(bounds, Color(255, 255, 255))

    override val lineWidth: Double
        get() = 1.0

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

class Model(dim: Dimension2D, rectCount: Int) {
    val container = DrawableContainerImpl<Drawable>()
    val ball = SimpleRectangle(Rectangle2D(dim.width / 2, dim.height / 2, 10.0, 10.0), Color(0, 0, 0))
    val step = 1.0
    var dirX = step
    var dirY = step

    init {
        for(i in 1..rectCount) {
            container.add(SimpleRectangle(Rectangle2D(
                    Math.random(0.0, dim.width),
                    Math.random(0.0, dim.height),
                    Math.random(50.0, 300.0),
                    Math.random(50.0, 200.0)),
                    Color(
                            Math.random(0.0, 255.0).toInt(),
                            Math.random(0.0, 255.0).toInt(),
                            Math.random(0.0, 255.0).toInt(),
                            Math.random(0.0, 255.0).toInt()))
            )
        }
        container.add(ball)
    }

    fun moveBall(canvas: Canvas) {
        val zoomFactor = canvas.view.zoomFactor
        val ballBoundsM = ball.bounds
        val viewBoundsM = Dimension2D(canvas.dimension.width / zoomFactor, canvas.dimension.height / zoomFactor)

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
        if (x + dirX > viewBoundsM.width - ball.bounds.width / 2) {
            x = viewBoundsM.width - ballBoundsM.width / 2
            dirX = -step
        }
        if (y + dirY > viewBoundsM.height - ball.bounds.height / 2) {
            y = viewBoundsM.height - ballBoundsM.height / 2
            dirY = -step
        }

        ball.location = Point2D(x + dirX, y + dirY)
        container.validate()
    }

    fun moveBallTo(p: Point2D) {
        ball.location = Point2D(p.x + dirX, p.y + dirY)
    }
}