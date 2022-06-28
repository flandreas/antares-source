package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.AbstractBranchCountSplitter
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.view.Handedness
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

abstract class AbstractBranchCountSplitterView<T : AbstractBranchCountSplitter>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
	handedness: Handedness = Handedness.RIGHT
) : AbstractSplitterView<T>(styleProvider, model, handedness) {

	var branchCount: BranchCount
		get() = model.branchCount
		set(value) {
			if (value != branchCount) {
				invalidate()
				model.branchCount = value
				modelExchanged(model)
				invalidate()
				update()
			}
		}
}