package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.view.TestPortView
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition

/**
 * Contains an input [TestPortView] directed to [Direction.WEST] and an output [TestPortView]
 * directed to [Direction.EAST].
 */
class TestVerticeView(
        styleProvider: StyleProvider,
        vertice: TestVertice,
        loc: Point2D,
        val inputDirection: Direction,
        val portViewLength: Int
        ) : AbstractRectangularVerticeView<TestVertice>(styleProvider, "test", vertice){

    constructor(styleProvider: StyleProvider): this(styleProvider, TestVertice(), Point2D(), Direction.WEST, 0)
    constructor(vertice: TestVertice): this(DrawStyleModule.styleProvider, vertice, Point2D(), Direction.WEST, 0)
    constructor(location: Point2D): this(DrawStyleModule.styleProvider, TestVertice(), location, Direction.WEST, 0)
    constructor(): this(TestVertice())

    init {
        location = loc
        modelExchanged(null)
    }

    override fun modelExchanged(oldModel: TestVertice?) {
        super.modelExchanged(oldModel)
        addPortView(TestPortView(model!!.getInput<Boolean>(), inputDirection, PortLabelPosition.INTERNAL, portViewLength))
        addPortView(TestPortView(model!!.getOutput<Boolean>(), Direction.EAST, PortLabelPosition.INTERNAL, portViewLength))
    }
}