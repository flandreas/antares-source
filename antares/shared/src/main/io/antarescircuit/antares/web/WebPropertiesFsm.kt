package io.antarescircuit.antares.web

import io.antarescircuit.antares.model.fsm.FSMDrawing
import io.antarescircuit.antares.model.fsm.FSMState
import io.antarescircuit.antares.model.fsm.FSMStateType
import io.antarescircuit.antares.model.fsm.FSMTransition

/**
 * Finite state machine properties (FSMDrawingBeanInfo, FSMStateBeanInfo, FSMTransitionBeanInfo).
 * Registered after the figure properties, because [FSMState] is a `RectangularComponent` whose figure
 * properties the desktop doesn't show for states.
 */
internal fun WebPropertyRegistry.registerFsmProperties() {
	define<FSMDrawing> {
		name({ it.name }, { b, v -> b.name = v })
	}
	define<FSMState> {
		remove(
			"filled", "stroked", "shadow", "styleType", "customColor", "customStroke", "text",
			"verticalAlignment", "horizontalAlignment",
		)
		enum("stateType", "antares.fsm.state.type", FSMStateType.entries, { it.stateType }, { b, v -> b.stateType = v })
		int("stateNumber", "antares.fsm.state.number", { it.stateNumber }, { b, v -> b.stateNumber = v }, min = 0)
		name({ it.name }, { b, v -> b.name = v })
		text("output", "antares.fsm.state.output", { it.output }, { b, v -> b.output = v })
		description({ it.description }, { b, v -> b.description = v })
	}
	define<FSMTransition> {
		remove("location")
		text("condition", "antares.fsm.transition.condition", { it.condition }, { b, v -> b.condition = v })
		text("output", "antares.fsm.transition.output", { it.output }, { b, v -> b.output = v })
		description({ it.description }, { b, v -> b.description = v })
	}
}
