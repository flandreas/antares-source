// Lets only one tab edit the projects stored in this browser. Otherwise two tabs would overwrite each other's
// changes, because each keeps its own copy of the files in memory.
// The editing tab holds a Web Lock. Another tab can take over: the editing tab saves, stops persisting and
// releases the lock.

const LOCK = 'antares-web-editor'
const CHANNEL = 'antares-web-tabs'
/** How long to wait for the editing tab to hand over before taking the lock anyway, e.g. if it hangs. */
const HANDOVER_TIMEOUT = 3000

export class TabLock {
	private channel = typeof BroadcastChannel === 'undefined' ? null : new BroadcastChannel(CHANNEL)
	private release: (() => void) | null = null

	/** Called in the editing tab when another tab takes over; must save and stop persisting before resolving. */
	onTakeover: () => Promise<void> = () => Promise.resolve()
	/** Called after this tab lost the lock. */
	onLost: () => void = () => {}

	constructor() {
		if (this.channel) this.channel.onmessage = (e) => {
			if (e.data === 'takeover' && this.release) void this.handOver()
		}
	}

	/** Takes the lock if no other tab holds it. Browsers without Web Locks always get it. */
	tryAcquire(): Promise<boolean> {
		return this.request({ ifAvailable: true })
	}

	/** Asks the editing tab to hand over, then takes the lock. */
	async takeOver(): Promise<void> {
		this.channel?.postMessage('takeover')
		const handedOver = this.request({})
		const timeout = new Promise<false>((resolve) => setTimeout(() => resolve(false), HANDOVER_TIMEOUT))
		if (!(await Promise.race([handedOver, timeout]))) await this.request({ steal: true })
	}

	private request(options: LockOptions): Promise<boolean> {
		if (!navigator.locks) return Promise.resolve(true)
		return new Promise((resolve) => {
			navigator.locks
				.request(LOCK, options, (lock) => {
					if (!lock) return resolve(false)
					resolve(true)
					// Hold the lock until released
					return new Promise<void>((r) => (this.release = r))
				})
				// Rejected when another tab steals the lock
				.catch(() => this.lost())
		})
	}

	private async handOver() {
		try {
			await this.onTakeover()
		} finally {
			this.release?.()
			this.lost()
		}
	}

	private lost() {
		if (!this.release) return
		this.release = null
		this.onLost()
	}
}
