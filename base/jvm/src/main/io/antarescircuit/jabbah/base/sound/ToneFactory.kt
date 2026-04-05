package io.antarescircuit.jabbah.base.sound

import io.antarescircuit.jabbah.base.logger
import java.io.ByteArrayInputStream
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import kotlin.experimental.and
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

actual object ToneFactory {
	actual fun create(params: ToneParams): Tone = ToneImpl(params)
}

private fun interface ToneAmplitude {
	fun value(i: Double, hz: Double, pw: Double): Double
}

// Used by reflection
@Suppress("unused")
private enum class ToneWaveform(
	val waveformType: WaveformType,
	val amplitude: ToneAmplitude
) {
	Sine( WaveformType.Sine, ToneAmplitude { i, hz, _ -> kotlin.math.sin(i * hz * 2 * kotlin.math.PI)}),
	Square( WaveformType.Square, ToneAmplitude { i, hz, pw -> if ((hz * i) % 1 < pw) 1.0 else -1.0 }),
	Triangle( WaveformType.Triangle, ToneAmplitude { i, hz, _ -> kotlin.math.asin(kotlin.math.sin(i * hz * 2 * kotlin.math.PI)) * 2.0 / kotlin.math.PI }),
	Sawtooth( WaveformType.Sawtooth, ToneAmplitude { i, hz, _ -> 2 * ((hz * i) % 1) - 1 }),
	Noise( WaveformType.Noise, ToneAmplitude { _, _, _ -> kotlin.random.Random.nextDouble() * 2 - 1 });

	companion object {
		fun ofType(type: WaveformType): ToneWaveform =
			values().firstOrNull { it.waveformType == type } ?: throw IllegalArgumentException("unsupported WaveformType $type")
	}
}

private class ToneImpl(
	private var params: ToneParams
) : Tone, Runnable {

	companion object {
		private val LOG by logger(ToneImpl::class)
		private val executor: ThreadPoolExecutor by lazy { Executors.newCachedThreadPool() as ThreadPoolExecutor }
	}

	private val isOn = AtomicBoolean(false)
	private val isUpdateRequired = AtomicBoolean(true)

	private var sampleRate: Int = calculateSampleRate(params)
	private var clipHolder: ClipHolder? = null

	/** ---- [Tone] interface */

	override fun play() {
		LOG.trace("Play tone")
		isOn.set(true)
		executor.submit(::run)
	}

	override fun update(params: ToneParams) {
		LOG.trace("Update tone")
		this.params = params
		sampleRate = calculateSampleRate(params)
		isUpdateRequired.set(true)
	}

	override fun stop() {
		LOG.trace("Stop tone")
		isOn.set(false)
	}

	/** ---- [Runnable] interface */

	override fun run() {
		try {
			while (isOn.get()) {
				if (isUpdateRequired.get()) {
					replaceClipHolder()
					clipHolder?.clip?.loop(Clip.LOOP_CONTINUOUSLY)
					isUpdateRequired.set(false)
				}
			}
		} catch (e: Throwable) {
			LOG.error("Tone thread died: ${e.message}")
		} finally {
			LOG.trace("Tone thread ended")
			clipHolder?.close()
		}
	}

	private fun replaceClipHolder() {
		clipHolder?.let {
			it.clip.close()
			it.audioInputStream.close()
		}
		clipHolder = createClipHolder()
	}

	/** ---- [ToneImpl] interface */

	private data class ClipHolder(
		val audioInputStream: AudioInputStream,
		val clip: Clip
	) {
		fun close() {
			audioInputStream.close()
			clip.close()
		}
	}

	private fun calculateSampleRate(params: ToneParams): Int =
		(44100 / params.frequency) * params.frequency

	private fun createClipHolder(): ClipHolder {
		var audioInputStream: AudioInputStream? = null
		try {
			audioInputStream = createAudioInputStream()
			val clip = AudioSystem.getClip()
			clip.open(audioInputStream)
			return ClipHolder(audioInputStream, clip)
		} catch (e: Throwable) {
			LOG.error("Error while creating audio stream: ${e.message}")
			audioInputStream?.close()
			throw e
		}
	}

	private fun createAudioFormat(): AudioFormat =
		AudioFormat(sampleRate.toFloat(), 16, 2, true, false)

	private fun createAudioInputStream(): AudioInputStream {
		val audioFormat = createAudioFormat()

		val waveform = ToneWaveform.ofType(params.waveformType)

		val cycle = max(1, sampleRate / params.frequency)
		val values = Array(4 * cycle) {
			waveform.amplitude.value(it / sampleRate.toDouble(), params.frequency.toDouble(), 0.5)
		}

		/*
		if (waveform !== ToneWaveform.Sine && params.smoothLevel > 0 && params.smoothWidth > 0) {
			// (Optional): Smooth
		}
		*/

		val rvalues = Array(sampleRate) { 0.0 }
		for (i in 0 until sampleRate step cycle) {
			System.arraycopy(values, 2 * cycle, rvalues, i, min(cycle, sampleRate - i))
		}

		val buf = ByteArray(4 * sampleRate) { 0 }
		var i = 0
		var j = 0
		while (i < buf.size) {
			val value = round(rvalues[j] * params.volume).toInt().toShort()
			if (params.leftChannel) {
				buf[i] = value.and(255.toShort()).toByte()
				buf[i + 1] = value.toInt().shr(8).toByte()
			}
			if (params.rightChannel) {
				buf[i + 2] = value.and(255.toShort()).toByte()
				buf[i + 3] = value.toInt().shr(8).toByte()
			}
			i += 4
			j++
		}

		return AudioInputStream(ByteArrayInputStream(buf), audioFormat, buf.size.toLong())
	}
}