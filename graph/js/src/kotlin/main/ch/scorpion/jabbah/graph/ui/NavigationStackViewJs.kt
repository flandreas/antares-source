package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.view.GraphView
import com.ccfraser.muirwik.components.mBreadcrumbs
import com.ccfraser.muirwik.components.mLink
import com.ccfraser.muirwik.components.mTypography
import org.w3c.dom.events.MouseEvent
import react.*

external interface NavigationStackViewJsProps : RProps {
	var controller: NavigationStackViewController
}

fun RBuilder.navigationStackView(handler: NavigationStackViewJsProps.() -> Unit): ReactElement {
	return child(NavigationStackViewJs::class) {
		this.attrs(handler)
	}
}

class NavigationStackViewJs(
	props: NavigationStackViewJsProps
) : RComponent<NavigationStackViewJsProps, RState>(props), NavigationStackView {

	override fun componentDidMount() {
		props.controller.view = this
	}

	override fun RBuilder.render() {
		mBreadcrumbs(">") {
			val iter = props.controller.navigationStack.iterator()
			iter.forEach { entry ->
				if (iter.hasNext()) {
					mLink(entry.graphName!!.value) {
						attrs.onClick = { event -> onClick(event, entry) }
					}
				}
				else {
					mTypography(entry.graphName!!.value)
				}
			}
		}
	}

	private fun onClick(event: MouseEvent, entry: NavigationStackEntry<GraphView>) {
		event.preventDefault()
		props.controller.navigationStack.navigateBackTo(entry, quickMode = event.metaKey)
	}

	override var editable: Boolean = true

	override var active: Boolean = true

	override fun refresh() {
		forceUpdate()
	}

	override fun dispose() { }
}