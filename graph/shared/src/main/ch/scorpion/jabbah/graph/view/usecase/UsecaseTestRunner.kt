package ch.scorpion.jabbah.graph.view.usecase

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.actor.ActorImpl
import ch.scorpion.jabbah.execution.actor.SimpleActorData
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.script.Script
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Usecase
import kotlin.math.max

class UsecaseTestRunner(
	usecases: List<Usecase>,
	val graphView: GraphView,
	private val scheduler: Scheduler,
	private val applicationModeHolder: ApplicationModeHolder,
	private val gateway: ScriptGateway = ScriptModule.scriptGateway,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val throwFailureException: Boolean = false
) {

	companion object {
		private val LOG by logger(UsecaseTestRunner::class)
	}

	private var _usecase: Usecase? = null

	private var _script: Script? = null
	val script get() = _script!!

	private var maxAssertionTime: Long = 0

	private val nextUsecases: MutableList<Usecase> = usecases.toMutableList()

	private val issues = mutableListOf<Issue>()

	fun run() {
		runNext()
	}

	private fun runNext() {
		if (nextUsecases.isNotEmpty()) {
			val usecase = nextUsecases.first()
			nextUsecases.removeAt(0)

			LOG.trace("Running test of usecase '${usecase.name.value}'")

			_usecase = usecase
			_script = Script(usecase.testScript!!, usecase.name.value, Translations.getString("usecaseTest.issueContext.name"))

			val usecaseActionRunner = UsecaseRunner(usecase, graphView, scheduler, applicationModeHolder, gateway)
			applicationModeHolder.setMode(ApplicationMode.EXEC_USECASE) {
				gateway.usecaseAction(usecaseActionRunner.script, usecaseActionRunner, scheduler)
				gateway.usecaseTest(script, this)
				scheduler.requestActingAfter(FinishTestActor(), maxAssertionTime + 1, SimpleActorData())
			}
		}
	}

	/** ---- Methods used by the DSL gateway */

	fun assert(time: Long, condition: () -> Boolean, description: String? = null) {
		maxAssertionTime = max(maxAssertionTime, time)
		scheduler.requestActingAfter(UsecaseTestActor(_usecase!!, condition), delay(time), SimpleActorData(description))
	}

	private fun delay(time: Long): Long {
		return time - scheduler.executionTime
	}

	private inner class UsecaseTestActor(
		private val usecase: Usecase,
		private val condition: () -> Boolean
	) : ActorImpl() {
		override fun act(signalHandler: SignalHandler, data: ActorData) {
			if (!condition.invoke()) {
				LOG.debug("Test '${usecase.name.value}' failed")
				rememberTestFailure(data.dataToString())
				if (throwFailureException) {
					throw UsecaseTestFailureException(usecase)
				}
			}
			super.act(signalHandler, data)
		}
	}

	private inner class FinishTestActor : ActorImpl() {
		override fun act(signalHandler: SignalHandler, data: ActorData) {
			scheduler.isActive = false
			if (nextUsecases.isEmpty()) {
				if (issues.isEmpty()) {
					eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = null, messageKey = "usecaseTest.dsl.testSucceeded.name"))
				} else {
					postIssues()
					eventBus.post(ComponentMessage(type = ComponentMessageType.Error, source = null, messageKey = "usecaseTest.dsl.testFailed.name"))
				}
			} else {
				runNext()
			}
		}
	}

	private fun rememberTestFailure(description: String?) {
		issues.add(IssueImpl(
			severity = IssueSeverity.Warning,
			name = Translations.getString("usecaseTest.dsl.testFailed.name"),
			description = description,
			origin = script.origin,
			context = script.context
		))
	}

	private fun postIssues() {
		issues.forEach { eventBus.post(it) }
	}

}

data class UsecaseTestFailureException(val usecase: Usecase) : Throwable()
