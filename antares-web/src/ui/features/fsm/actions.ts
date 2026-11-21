// User-facing FSM operations with their dialogs.
import { engine } from '../../../core/engine'
import { askNewItemName } from '../../projectActions'
import { fsmApi } from './api'

/** Asks for a name and creates a new state machine (desktop: NewFSMAction). */
export async function newStateMachine(): Promise<void> {
	const name = await askNewItemName('New state machine', 'fsm', 'State machine', 'Create state machine')
	if (name) fsmApi.create(name)
}

/**
 * Creates a truth table implementing the open state machine and opens it
 * (desktop: FSMPanelController.createTruthTable). Validation errors are shown instead.
 */
export async function createTruthTable(): Promise<void> {
	engine.save()
	const error = fsmApi.kotlin.truthTableError()
	if (error) {
		engine.message('warning', 'Can’t create a truth table', error)
		return
	}
	const fsmName = engine.state.document?.name ?? 'State machine'
	const name = await askNewItemName('Create truth table', 'truthTable', fsmName, 'Create truth table')
	if (!name) return
	fsmApi.kotlin.createTruthTable(name)
	engine.refresh()
}
