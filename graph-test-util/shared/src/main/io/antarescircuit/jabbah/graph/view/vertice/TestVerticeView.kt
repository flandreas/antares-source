package io.antarescircuit.jabbah.graph.view.vertice

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.model.TestVertice
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition
import io.antarescircuit.jabbah.graph.view.port.TestPortView
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * Contains an input [io.antarescircuit.jabbah.graph.view.port.TestPortView] directed to [io.antarescircuit.jabbah.base.geom.Direction.WEST] and an output [io.antarescircuit.jabbah.graph.view.port.TestPortView]
 * directed to [io.antarescircuit.jabbah.base.geom.Direction.EAST].
 */
class TestVerticeView(
    name: String = "",
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    vertice: TestVertice = TestVertice(name = name),
    loc: Point2D = Point2D.Companion.ZERO,
    private var inputDirection: Direction = Direction.WEST,
    private var outputDirection: Direction = Direction.EAST,
    private var portViewLength: Int? = null,
    width: Int = 0,
    height: Int = 0
) : AbstractRectangularVerticeView<TestVertice>(styleProvider, vertice, loc.x, loc.y, width.toDouble(), height.toDouble()) {

	companion object {
		const val DEF_SIZE = 20

		fun createEastOutputVerticeView(name: String, x: Int, y: Int): TestVerticeView =
			TestVerticeView(name = name, loc = Point2D(
                x,
                y
            ), inputDirection = Direction.WEST, outputDirection = Direction.EAST, width = DEF_SIZE)

		fun createSouthInputVerticeView(name: String, x: Int, y: Int): TestVerticeView =
			TestVerticeView(name, loc = Point2D(x, y), inputDirection = Direction.SOUTH, outputDirection = Direction.NORTH, width = 0, height = DEF_SIZE)
	}

	init {
		location = loc
		modelExchanged(null)
	}

	override fun toString(): String = "TestVerticeView ${model.name}"

	override fun modelExchanged(oldModel: TestVertice?) {
		super.modelExchanged(oldModel)
		addPortView(
			TestPortView(
				model.getInput(),
				inputDirection,
				PortLabelPosition.INTERNAL,
				portViewLength,
				Point2D.Companion.ZERO
			)
		)
		addPortView(TestPortView(model.getOutput(), outputDirection, PortLabelPosition.INTERNAL, portViewLength,
            Point2D(width, -height)
        ))
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("width", widthInt)
		writer.writeInt("height", heightInt)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		width = reader.readInt("width").toDouble()
		height = reader.readInt("height").toDouble()
	}
}