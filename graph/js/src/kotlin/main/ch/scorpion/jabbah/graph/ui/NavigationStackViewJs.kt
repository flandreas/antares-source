package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.view.GraphView
import com.ccfraser.muirwik.components.mBreadcrumbs
import com.ccfraser.muirwik.components.mLink
import com.ccfraser.muirwik.components.mTypography
import kotlinx.css.background
import org.w3c.dom.events.MouseEvent
import react.*
import styled.css

external interface NavigationStackViewJsProps : Props {
	var controller: NavigationStackViewController
	var backgroundColor: String?
}

fun RBuilder.navigationStackView(handler: NavigationStackViewJsProps.() -> Unit) {
	child(NavigationStackViewJs::class) {
		this.attrs(handler)
	}
}

class NavigationStackViewJs(
	props: NavigationStackViewJsProps
) : RComponent<NavigationStackViewJsProps, State>(props), NavigationStackView {

	override fun componentDidMount() {
		props.controller.view = this
	}

	override fun RBuilder.render() {
		mBreadcrumbs(">") {
			css {
				props.backgroundColor?.let { background = it }
			}
			val iter = props.controller.navigationStack.iterator()
			iter.forEach { entry ->
				if (iter.hasNext()) {
					mLink(entry.name) {
						attrs.onClick = { event -> onClick(event, entry) }
					}
				}
				else {
					mTypography(entry.name)
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