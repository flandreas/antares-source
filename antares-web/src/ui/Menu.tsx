import { useEffect, useLayoutEffect, useRef, useState, type ReactNode } from 'react'
import { createPortal } from 'react-dom'
import { formatShortcut } from './hooks'

export type MenuEntry =
	| {
			label: string
			icon?: ReactNode
			shortcut?: string
			disabled?: boolean
			danger?: boolean
			checked?: boolean
			onSelect: () => void
	  }
	| 'separator'
	| { heading: string }

interface MenuProps {
	entries: MenuEntry[]
	/** Viewport position of the menu's top-left corner (adjusted to stay on screen). */
	x: number
	y: number
	onClose: () => void
	minWidth?: number
}

/** A floating menu, used for dropdowns and context menus. */
export function Menu({ entries: rawEntries, x, y, onClose, minWidth = 200 }: MenuProps) {
	// Drop leading, trailing and repeated separators that come from combining entry groups
	const entries: MenuEntry[] = []
	for (const e of rawEntries) {
		if (e === 'separator' && (entries.length === 0 || entries[entries.length - 1] === 'separator')) continue
		entries.push(e)
	}
	if (entries[entries.length - 1] === 'separator') entries.pop()
	const ref = useRef<HTMLDivElement>(null)
	const [pos, setPos] = useState({ x, y })
	const [active, setActive] = useState(-1)

	useLayoutEffect(() => {
		const el = ref.current
		if (!el) return
		const rect = el.getBoundingClientRect()
		setPos({
			x: Math.max(8, Math.min(x, window.innerWidth - rect.width - 8)),
			y: Math.max(8, Math.min(y, window.innerHeight - rect.height - 8)),
		})
		el.focus()
	}, [x, y])

	useEffect(() => {
		const onPointer = (e: PointerEvent) => {
			if (!ref.current?.contains(e.target as Node)) onClose()
		}
		const onBlur = () => onClose()
		window.addEventListener('pointerdown', onPointer, true)
		window.addEventListener('blur', onBlur)
		return () => {
			window.removeEventListener('pointerdown', onPointer, true)
			window.removeEventListener('blur', onBlur)
		}
	}, [onClose])

	const selectable = entries
		.map((e, i) => (typeof e === 'object' && 'onSelect' in e && !e.disabled ? i : -1))
		.filter((i) => i >= 0)

	const onKeyDown = (e: React.KeyboardEvent) => {
		e.stopPropagation()
		if (e.key === 'Escape') {
			e.preventDefault()
			onClose()
		} else if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
			e.preventDefault()
			const idx = selectable.indexOf(active)
			const next = e.key === 'ArrowDown' ? idx + 1 : idx - 1
			setActive(selectable[(next + selectable.length) % selectable.length] ?? -1)
		} else if (e.key === 'Enter' && active >= 0) {
			e.preventDefault()
			const entry = entries[active]
			if (typeof entry === 'object' && 'onSelect' in entry) {
				onClose()
				entry.onSelect()
			}
		}
	}

	return createPortal(
		<div
			ref={ref}
			className="menu"
			role="menu"
			tabIndex={-1}
			style={{ left: pos.x, top: pos.y, minWidth }}
			onKeyDown={onKeyDown}
			onContextMenu={(e) => e.preventDefault()}
		>
			{entries.map((entry, i) => {
				if (entry === 'separator') return <div key={i} className="menu-separator" role="separator" />
				if ('heading' in entry)
					return (
						<div key={i} className="menu-heading">
							{entry.heading}
						</div>
					)
				return (
					<button
						key={i}
						type="button"
						role="menuitem"
						className={`menu-item${entry.danger ? ' danger' : ''}${active === i ? ' active' : ''}`}
						disabled={entry.disabled}
						onPointerEnter={() => setActive(i)}
						onClick={() => {
							onClose()
							entry.onSelect()
						}}
					>
						<span className="menu-icon">{entry.checked ? '✓' : entry.icon}</span>
						<span className="menu-label">{entry.label}</span>
						{entry.shortcut && <kbd className="menu-shortcut">{formatShortcut(entry.shortcut)}</kbd>}
					</button>
				)
			})}
		</div>,
		document.body,
	)
}

/** A button that opens a dropdown [Menu] below itself. */
export function MenuButton({
	entries,
	children,
	className = 'btn',
	title,
	align = 'left',
}: {
	entries: () => MenuEntry[]
	children: ReactNode
	className?: string
	title?: string
	align?: 'left' | 'right'
}) {
	const ref = useRef<HTMLButtonElement>(null)
	const [open, setOpen] = useState<{ x: number; y: number } | null>(null)
	return (
		<>
			<button
				ref={ref}
				type="button"
				className={`${className}${open ? ' pressed' : ''}`}
				title={title}
				aria-haspopup="menu"
				aria-expanded={!!open}
				onClick={() => {
					if (open) return setOpen(null)
					const r = ref.current!.getBoundingClientRect()
					setOpen({ x: align === 'left' ? r.left : r.right - 220, y: r.bottom + 4 })
				}}
			>
				{children}
			</button>
			{open && <Menu entries={entries()} x={open.x} y={open.y} onClose={() => setOpen(null)} minWidth={220} />}
		</>
	)
}
