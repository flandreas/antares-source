@file:JsModule("react-resize-detector")
@file:JsNonModule
package ch.scorpion.jabbah.base.mreact

import react.MutableRefObject

external interface ResizeDetectorProps {
	var onResize: ((Int, Int) -> Unit)?
}

external interface ResizeDetectorFunctionProps<T : Any> : ResizeDetectorProps {
	var targetRef: MutableRefObject<T>?
}

external interface UseResizeDetectorResult<T : Any> {
	val ref: MutableRefObject<T>
	val width: Int
	val height: Int
}

external fun <T : Any> useResizeDetector(props: ResizeDetectorFunctionProps<T>? = definedExternally): UseResizeDetectorResult<T>