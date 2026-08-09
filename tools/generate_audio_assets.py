"""Generate original Blood Moon demo music and combat SFX with deterministic synthesis."""

from __future__ import annotations

import math
import random
import wave
from array import array
from pathlib import Path

SAMPLE_RATE = 22_050
RNG = random.Random(0xB100D)
OUTPUT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res" / "raw"


def midi(note: int) -> float:
    return 440.0 * 2.0 ** ((note - 69) / 12.0)


def envelope(t: float, duration: float, attack: float, release: float) -> float:
    if t < 0.0 or t >= duration:
        return 0.0
    head = min(1.0, t / max(attack, 1e-5))
    tail = min(1.0, (duration - t) / max(release, 1e-5))
    return head * tail


def oscillator(phase: float, shape: str) -> float:
    if shape == "triangle":
        return 2.0 / math.pi * math.asin(math.sin(phase))
    if shape == "saw":
        return 2.0 * ((phase / (2.0 * math.pi)) % 1.0) - 1.0
    return math.sin(phase)


def add_note(buffer: list[float], start: float, duration: float, frequency: float,
             amplitude: float, shape: str = "sine", attack: float = 0.01,
             release: float = 0.08, sweep: float = 0.0, vibrato: float = 0.0) -> None:
    first = max(0, int(start * SAMPLE_RATE))
    last = min(len(buffer), int((start + duration) * SAMPLE_RATE))
    phase = 0.0
    for index in range(first, last):
        t = (index - first) / SAMPLE_RATE
        frequency_at_t = max(20.0, frequency + sweep * (t / max(duration, 1e-5)))
        frequency_at_t *= 1.0 + math.sin(t * math.tau * 5.2) * vibrato
        phase += math.tau * frequency_at_t / SAMPLE_RATE
        env = envelope(t, duration, attack, release)
        buffer[index] += oscillator(phase, shape) * amplitude * env


def add_noise(buffer: list[float], start: float, duration: float, amplitude: float,
              decay: float = 7.0, tone: float = 0.22) -> None:
    first = max(0, int(start * SAMPLE_RATE))
    last = min(len(buffer), int((start + duration) * SAMPLE_RATE))
    filtered = 0.0
    for index in range(first, last):
        t = (index - first) / SAMPLE_RATE
        filtered += (RNG.uniform(-1.0, 1.0) - filtered) * tone
        buffer[index] += filtered * amplitude * math.exp(-decay * t)


def add_kick(buffer: list[float], start: float, amplitude: float = 0.24) -> None:
    duration = 0.34
    first = int(start * SAMPLE_RATE)
    last = min(len(buffer), first + int(duration * SAMPLE_RATE))
    phase = 0.0
    for index in range(first, last):
        t = (index - first) / SAMPLE_RATE
        frequency = 46.0 + 92.0 * math.exp(-22.0 * t)
        phase += math.tau * frequency / SAMPLE_RATE
        buffer[index] += math.sin(phase) * amplitude * math.exp(-11.0 * t)


def finalize(samples: list[float], target: float = 0.88) -> list[float]:
    shaped = [math.tanh(value * 1.35) for value in samples]
    peak = max(1e-9, max(abs(value) for value in shaped))
    scale = target / peak
    return [max(-1.0, min(1.0, value * scale)) for value in shaped]


def write_wav(name: str, samples: list[float], target: float = 0.88) -> None:
    OUTPUT.mkdir(parents=True, exist_ok=True)
    pcm = array("h", (round(value * 32767.0) for value in finalize(samples, target)))
    with wave.open(str(OUTPUT / name), "wb") as stream:
        stream.setnchannels(1)
        stream.setsampwidth(2)
        stream.setframerate(SAMPLE_RATE)
        stream.writeframes(pcm.tobytes())


def make_bgm() -> None:
    bpm = 92.0
    beat = 60.0 / bpm
    bars = 16
    duration = bars * 4.0 * beat
    music = [0.0] * int(duration * SAMPLE_RATE)
    chords = [
        (38, (50, 53, 57)),  # D minor
        (34, (46, 50, 53)),  # B-flat major
        (41, (53, 57, 60)),  # F major
        (36, (48, 52, 55)),  # C major
    ]
    melody = (74, 77, 76, 72, 74, 69, 72, 76, 77, 81, 79, 76, 74, 72, 69, 72)

    for bar in range(bars):
        root, chord = chords[bar % len(chords)]
        bar_start = bar * beat * 4.0
        for note in chord:
            add_note(music, bar_start, beat * 3.92, midi(note), 0.034,
                     "triangle", 0.24, 0.42, vibrato=0.0018)
            add_note(music, bar_start, beat * 3.92, midi(note - 12), 0.017,
                     "sine", 0.32, 0.45)
        for step in range(8):
            note = chord[(step * 2 + bar) % 3] + 12
            add_note(music, bar_start + step * beat * 0.5, beat * 0.42,
                     midi(note), 0.052, "triangle", 0.008, 0.17)
        for pulse in range(4):
            add_note(music, bar_start + pulse * beat, beat * 0.7,
                     midi(root), 0.092, "triangle", 0.006, 0.25)
            if pulse in (0, 2):
                add_kick(music, bar_start + pulse * beat, 0.21 if pulse == 0 else 0.16)
            add_noise(music, bar_start + (pulse + 0.5) * beat, 0.09, 0.027, 24.0, 0.55)
        if bar % 2 == 0:
            add_note(music, bar_start + beat * 2.0, beat * 1.5,
                     midi(melody[bar]), 0.075, "sine", 0.01, 0.55, vibrato=0.003)
            add_note(music, bar_start + beat * 2.0, beat * 1.5,
                     midi(melody[bar] + 12), 0.018, "sine", 0.005, 0.65)

    # Blood-moon pulse and a quiet continuous floor keep the loop cohesive.
    for beat_index in range(bars * 4):
        if beat_index % 8 == 0:
            add_note(music, beat_index * beat, beat * 1.4, midi(26), 0.13,
                     "sine", 0.005, 0.7, sweep=-12.0)
    add_note(music, 0.0, duration, midi(26), 0.022, "sine", 0.45, 0.45)

    crossfade = int(0.24 * SAMPLE_RATE)
    first = music[:crossfade]
    for offset in range(crossfade):
        mix = offset / max(1, crossfade - 1)
        index = len(music) - crossfade + offset
        music[index] = music[index] * (1.0 - mix) + first[offset] * mix
    write_wav("bgm_blood_road.wav", music, 0.72)


def make_hit(name: str, heavy: bool) -> None:
    duration = 0.46 if heavy else 0.24
    sound = [0.0] * int(duration * SAMPLE_RATE)
    add_note(sound, 0.0, duration, 62.0 if heavy else 92.0,
             0.7 if heavy else 0.52, "sine", 0.001, duration * 0.72,
             sweep=-28.0 if heavy else -18.0)
    add_note(sound, 0.004, duration * 0.42, 780.0 if heavy else 1280.0,
             0.3, "triangle", 0.001, duration * 0.34,
             sweep=-420.0 if heavy else -680.0)
    add_noise(sound, 0.0, duration * 0.7, 0.58 if heavy else 0.42,
              12.0 if heavy else 20.0, 0.34)
    if heavy:
        add_kick(sound, 0.015, 0.5)
    write_wav(name, sound)


def make_dash() -> None:
    duration = 0.38
    sound = [0.0] * int(duration * SAMPLE_RATE)
    add_noise(sound, 0.0, duration, 0.48, 7.0, 0.08)
    add_note(sound, 0.0, duration, 620.0, 0.32, "sine", 0.003, 0.16, sweep=-500.0)
    add_note(sound, 0.05, 0.28, 105.0, 0.42, "sine", 0.002, 0.19, sweep=-45.0)
    write_wav("sfx_dash.wav", sound, 0.8)


def make_spear() -> None:
    duration = 0.52
    sound = [0.0] * int(duration * SAMPLE_RATE)
    add_note(sound, 0.0, 0.34, 260.0, 0.35, "saw", 0.004, 0.15, sweep=1050.0)
    add_note(sound, 0.11, 0.4, 74.0, 0.52, "sine", 0.002, 0.26, sweep=-22.0)
    add_noise(sound, 0.08, 0.36, 0.36, 10.0, 0.13)
    write_wav("sfx_blood_spear.wav", sound)


def make_siphon() -> None:
    duration = 0.76
    sound = [0.0] * int(duration * SAMPLE_RATE)
    add_note(sound, 0.0, duration, 130.0, 0.34, "sine", 0.08, 0.28, sweep=180.0)
    add_note(sound, 0.05, 0.6, 260.0, 0.22, "triangle", 0.12, 0.24, sweep=-100.0,
             vibrato=0.008)
    add_kick(sound, 0.45, 0.38)
    write_wav("sfx_siphon.wav", sound, 0.82)


def make_nova() -> None:
    duration = 1.08
    sound = [0.0] * int(duration * SAMPLE_RATE)
    add_note(sound, 0.0, 0.48, 85.0, 0.44, "sine", 0.12, 0.08, sweep=210.0)
    add_note(sound, 0.18, 0.9, 58.0, 0.72, "sine", 0.004, 0.65, sweep=-24.0)
    add_note(sound, 0.22, 0.52, 420.0, 0.28, "saw", 0.002, 0.34, sweep=-260.0)
    add_noise(sound, 0.2, 0.82, 0.58, 5.8, 0.16)
    add_kick(sound, 0.2, 0.75)
    write_wav("sfx_blood_nova.wav", sound)


def make_ui_and_level() -> None:
    tap = [0.0] * int(0.12 * SAMPLE_RATE)
    add_note(tap, 0.0, 0.1, 880.0, 0.45, "sine", 0.001, 0.08, sweep=180.0)
    write_wav("sfx_ui_tap.wav", tap, 0.62)

    hurt = [0.0] * int(0.38 * SAMPLE_RATE)
    add_note(hurt, 0.0, 0.36, 94.0, 0.62, "saw", 0.001, 0.28, sweep=-52.0)
    add_noise(hurt, 0.0, 0.3, 0.46, 12.0, 0.3)
    write_wav("sfx_player_hurt.wav", hurt)

    level = [0.0] * int(1.2 * SAMPLE_RATE)
    for index, note in enumerate((62, 65, 69, 74, 77)):
        add_note(level, index * 0.14, 0.62, midi(note), 0.24,
                 "sine", 0.004, 0.35)
        add_note(level, index * 0.14, 0.55, midi(note + 12), 0.08,
                 "triangle", 0.003, 0.28)
    write_wav("sfx_level_up.wav", level, 0.76)


def main() -> None:
    make_bgm()
    make_hit("sfx_hit_light.wav", False)
    make_hit("sfx_hit_heavy.wav", True)
    make_dash()
    make_spear()
    make_siphon()
    make_nova()
    make_ui_and_level()
    for path in sorted(OUTPUT.glob("*.wav")):
        print(f"{path.name}: {path.stat().st_size} bytes")


if __name__ == "__main__":
    main()
