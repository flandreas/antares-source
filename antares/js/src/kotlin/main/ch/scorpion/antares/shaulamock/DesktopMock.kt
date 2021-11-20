package ch.scorpion.antares.shaulamock

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonSize
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import kotlinx.browser.document
import kotlinx.browser.window
import react.Props
import react.dom.render
import react.fc
import styled.styledDiv

external interface DesktopMockProps : Props {
	var projectName: String
	var projectUuid: String
	var returnUri: String
}

class DesktopMock {
	fun show(returnUri: String) {
		render(document.getElementById("root")) {
			child(desktopMock) {
				attrs.returnUri = returnUri
			}
		}
	}
}

val desktopMock = fc<DesktopMockProps> { props ->
	mCssBaseline()
	styledDiv {
		mAppBar(position = MAppBarPosition.static) {
			mToolbar {
				mToolbarTitle("Project \"${props.projectName}\"")
				mButton("Save", color = MColor.inherit, variant = MButtonVariant.outlined, size = MButtonSize.small,
					onClick = { window.location.href = props.returnUri })
			}
		}
	}
}