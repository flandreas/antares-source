package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import com.ccfraser.muirwik.components.*
import react.*

@JsModule("@material-ui/core/Slider")
@JsNonModule
private external val sliderModule: dynamic

@Suppress("UnsafeCastFromDynamic")
private val sliderComponent: RComponent<SystemSpeedSliderProps, RState> = sliderModule.default

fun RBuilder.systemSpeedSlider(
	systemSpeedCategory: CurrentSystemSpeedCategory,
	eventBus: EventBus = BaseModule.eventBus,
	handler: SystemSpeedSliderProps.() -> Unit
): ReactElement {
	return child(SystemSpeedSlider::class) {
		this.attrs.systemSpeedCategory = systemSpeedCategory
		this.attrs.eventBus = eventBus
		this.attrs(handler)
	}
}

interface SystemSpeedSliderProps : MSliderProps {
	var systemSpeedCategory: CurrentSystemSpeedCategory
	var eventBus: EventBus
}

/** A slider that allows to change the current [SystemSpeed]. */
class SystemSpeedSlider : RComponent<SystemSpeedSliderProps, RState>() {

	override fun RBuilder.render() {
		mTooltip("${Translations.getString("execution.action.speed.name")}: ${props.systemSpeedCategory.systemSpeedCategory}") {
			createStyled(sliderComponent) {
				attrs.value = props.systemSpeedCategory.systemSpeed.speed
				attrs.min = 0
				attrs.max = 100
				attrs.step = 1
				attrs.valueLabelDisplay = MSliderValueLabelDisplay.auto
				attrs.onChange = ::changeSpeed
				attrs.marks = listOf(MSliderMark(33), MSliderMark(66)).toTypedArray()
			}
		}
	}

	private fun changeSpeed(event: Any, value: Number) {
		props.systemSpeedCategory.systemSpeed.speed = value as Int
		forceUpdate()
	}
}