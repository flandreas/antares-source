import { memo, useEffect, useRef, useState } from 'react'
import { Box, Cpu } from 'lucide-react'
import { engine } from '../core/engine'
import { useThemeName } from './hooks'

const WIDTH = 36
const HEIGHT = 26

/** Rendered thumbnails by theme and key; empty string = not renderable. */
const cache = new Map<string, string>()
const queue: Array<() => void> = []
let scheduled = false

/** Renders queued thumbnails in idle time, a few at a time, to keep the UI responsive. */
function schedule() {
	if (scheduled) return
	scheduled = true
	const idle = window.requestIdleCallback ?? ((cb: () => void) => window.setTimeout(cb, 16))
	idle(() => {
		scheduled = false
		const start = performance.now()
		while (queue.length > 0 && performance.now() - start < 12) queue.shift()!()
		if (queue.length > 0) schedule()
	})
}

export const Thumbnail = memo(function Thumbnail({ itemKey, kind }: { itemKey: string; kind: string }) {
	const ref = useRef<HTMLSpanElement>(null)
	const theme = useThemeName()
	const cacheKey = `${theme}|${itemKey}`
	const [src, setSrc] = useState(() => cache.get(cacheKey))

	useEffect(() => {
		setSrc(cache.get(cacheKey))
		if (cache.has(cacheKey) || !ref.current) return
		let cancelled = false
		const observer = new IntersectionObserver((entries) => {
			if (!entries.some((e) => e.isIntersecting)) return
			observer.disconnect()
			queue.push(() => {
				if (cancelled) return
				if (!cache.has(cacheKey)) cache.set(cacheKey, engine.thumbnail(itemKey, WIDTH, HEIGHT))
				setSrc(cache.get(cacheKey))
			})
			schedule()
		})
		observer.observe(ref.current)
		return () => {
			cancelled = true
			observer.disconnect()
		}
	}, [cacheKey, itemKey])

	return (
		<span ref={ref} className="thumb" aria-hidden="true">
			{src ? (
				<img src={src} width={WIDTH} height={HEIGHT} alt="" draggable={false} />
			) : src === '' ? (
				kind === 'circuit' ? <Cpu size={15} /> : <Box size={15} />
			) : null}
		</span>
	)
})
