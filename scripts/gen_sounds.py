#!/usr/bin/env python3
"""Génère les effets sonores du jeu (WAV 44.1 kHz mono 16 bits).

Tout est synthétisé : aucune banque de sons sous licence, aucun fichier externe.
Les mêmes WAV servent à Android (res/raw) et au runner desktop (resources).
"""
from __future__ import annotations

import math
import os
import random
import struct
import wave

RATE = 44100
TAU = 2.0 * math.pi
OUT_ANDROID = os.path.join("app", "src", "main", "res", "raw")
OUT_DESKTOP = os.path.join("desktop", "src", "main", "resources", "sounds")


def envelope(t: float, duration: float, attack: float = 0.004, decay: float = 4.0) -> float:
    a = min(1.0, t / attack) if attack > 0 else 1.0
    d = math.exp(-decay * (t / duration))
    return a * d


def tone(freq: float, t: float, phase: float = 0.0) -> float:
    return math.sin(TAU * freq * t + phase)


def soft_square(freq: float, t: float) -> float:
    s = math.sin(TAU * freq * t)
    return 0.65 * s + 0.25 * math.sin(TAU * 2 * freq * t) + 0.10 * math.sin(TAU * 3 * freq * t)


def write(name: str, samples: list[float], peak: float = 0.85) -> None:
    top = max(1e-6, max(abs(s) for s in samples))
    gain = peak / top
    data = b"".join(
        struct.pack("<h", max(-32767, min(32767, int(s * gain * 32767))))
        for s in samples
    )
    for folder in (OUT_ANDROID, OUT_DESKTOP):
        os.makedirs(folder, exist_ok=True)
        with wave.open(os.path.join(folder, name + ".wav"), "wb") as f:
            f.setnchannels(1)
            f.setsampwidth(2)
            f.setframerate(RATE)
            f.writeframes(data)
    print(f"{name}.wav  {len(samples) / RATE * 1000:.0f} ms")


def render(duration: float, fn) -> list[float]:
    n = int(RATE * duration)
    return [fn(i / RATE, i) for i in range(n)]


def place() -> None:
    """Petit « toc » mat quand une pièce se pose."""
    random.seed(4)

    def fn(t, i):
        click = (random.random() * 2 - 1) * math.exp(-t * 220) * 0.25
        body = tone(196, t) * math.exp(-t * 26)
        sub = tone(98, t) * math.exp(-t * 18) * 0.5
        return click + body * 0.7 + sub

    write("place", render(0.09, fn), peak=0.6)


def clear() -> None:
    """Pop clair pour une ou deux lignes."""
    def fn(t, i):
        freq = 620 + 260 * min(1.0, t / 0.1)
        return (
            tone(freq, t) * envelope(t, 0.15, decay=7)
            + tone(freq * 1.5, t) * envelope(t, 0.15, decay=9) * 0.35
        )

    write("clear", render(0.16, fn), peak=0.75)


def clear_big() -> None:
    """Arpège pour un triple, un quadruple ou un plateau vidé."""
    notes = [523.25, 659.25, 783.99, 1046.5]

    def fn(t, i):
        total = 0.0
        for index, note in enumerate(notes):
            start = index * 0.045
            if t >= start:
                total += soft_square(note, t - start) * envelope(t - start, 0.16, decay=8) * 0.5
        return total

    write("clear_big", render(0.30, fn), peak=0.8)


def combo() -> None:
    """Montée de combo : glissando vers le haut."""
    def fn(t, i):
        progress = min(1.0, t / 0.22)
        freq = 420 * (2.0 ** (progress * 1.35))
        return tone(freq, t) * envelope(t, 0.24, decay=5) + tone(freq * 2, t) * 0.2 * envelope(t, 0.2, decay=9)

    write("combo", render(0.24, fn), peak=0.65)


def win() -> None:
    notes = [523.25, 659.25, 783.99, 1046.5, 1318.5]

    def fn(t, i):
        total = 0.0
        for index, note in enumerate(notes):
            start = index * 0.075
            if t >= start:
                total += tone(note, t - start) * envelope(t - start, 0.28, decay=5) * 0.45
                total += tone(note * 0.5, t - start) * envelope(t - start, 0.3, decay=6) * 0.18
        return total

    write("win", render(0.62, fn), peak=0.8)


def lose() -> None:
    def fn(t, i):
        progress = min(1.0, t / 0.34)
        freq = 330 * (2.0 ** (-progress * 0.9))
        return tone(freq, t) * envelope(t, 0.38, decay=3.5) + tone(freq * 0.5, t) * 0.3 * envelope(t, 0.4, decay=3)

    write("lose", render(0.42, fn), peak=0.7)


def ui() -> None:
    def fn(t, i):
        return tone(1180, t) * math.exp(-t * 95) * 0.8 + tone(1760, t) * math.exp(-t * 130) * 0.25

    write("ui", render(0.05, fn), peak=0.4)


if __name__ == "__main__":
    place()
    clear()
    clear_big()
    combo()
    win()
    lose()
    ui()
