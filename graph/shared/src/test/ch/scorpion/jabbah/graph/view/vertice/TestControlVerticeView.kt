package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.TestControlVertice
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.port.TestPortView
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition

/** A [ControlView] implementation used for unit and integration testing.*/
class TestControlVerticeView(
	vertice: TestControlVertice = TestControlVertice(),
	styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangularVerticeView<TestControlVertice>(styleProvider, vertice),
	ControlView<TestControlVertice>,
	ControlViewSource<TestControlVertice>
{

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: TestControlVertice?) {
		super.modelExchanged(oldModel)
		addPortView(TestPortView(model!!.getInput<Boolean>(), Direction.WEST, PortLabelPosition.INTERNAL, 0))
	}

	/** ---- [ControlView] */

	override val controlId: String? get() = "$type \"${model!!.name}\""

	override fun bindToModel(model: TestControlVertice) {
		this.model = model
	}

	/** ---- [ControlViewSource] */

	override val controlName: String get() = "control:${model!!.id}"

	override val iconPath: String get() = "not used"

	override fun createControlView(): ControlView<TestControlVertice> {
		return TestControlVerticeView(model!!, styleProvider)
	}
}