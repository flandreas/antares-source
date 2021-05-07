@file:JsModule("react-resize-detector")
@file:JsNonModule
package ch.scorpion.jabbah.base.mreact

import react.RMutableRef

external interface ResizeDetectorProps {
	var onResize: ((Int, Int) -> Unit)?
}

external interface ResizeDetectorFunctionProps<T> : ResizeDetectorProps {
	var targetRef: RMutableRef<T>
}

external interface UseResizeDetectorResult<T> {
	val ref: RMutableRef<T>
	val width: Int
	val height: Int
}

external fun <T> useResizeDetector(props: ResizeDetectorFunctionProps<T>? = definedExternally): UseResizeDetectorResult<T>