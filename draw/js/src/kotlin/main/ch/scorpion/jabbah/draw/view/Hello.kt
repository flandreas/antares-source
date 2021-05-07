package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.mreact.useResizeDetector
import react.RProps
import react.dom.div
import react.functionalComponent
import react.useEffect

external interface HelloProps : RProps {
	var name: String
}

val hello = functionalComponent<HelloProps> { props ->
	val resizeDetectionResult = useResizeDetector<Any>()
	useEffect(null) {

	}
	div {
		ref = resizeDetectionResult.ref
		+"Hello, ${props.name}. ${resizeDetectionResult.width}x${resizeDetectionResult.height}"
	}
}
