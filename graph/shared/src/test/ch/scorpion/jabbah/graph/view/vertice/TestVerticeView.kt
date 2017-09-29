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
        private val name: String = "",
        styleProvider: StyleProvider = DrawStyleModule.styleProvider,
        vertice: TestVertice = TestVertice(),
        loc: Point2D = Point2D(),
        private val inputDirection: Direction = Direction.WEST,
        private val outputDirection: Direction = Direction.EAST,
        private val portViewLength: Int = 0
) : AbstractRectangularVerticeView<TestVertice>(styleProvider, "test", vertice){

    init {
        location = loc
        modelExchanged(null)
    }

    override fun toString(): String {
        return "TestVerticeView $name"
    }

    override fun modelExchanged(oldModel: TestVertice?) {
        super.modelExchanged(oldModel)
        addPortView(TestPortView(model!!.getInput<Boolean>(), inputDirection, PortLabelPosition.INTERNAL, portViewLength))
        addPortView(TestPortView(model!!.getOutput<Boolean>(), outputDirection, PortLabelPosition.INTERNAL, portViewLength))
    }
}