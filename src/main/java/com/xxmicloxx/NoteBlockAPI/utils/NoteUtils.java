package com.xxmicloxx.NoteBlockAPI.utils;

import com.xxmicloxx.NoteBlockAPI.model.Note;

public final class NoteUtils {

	private static final float[] pitches = new float[2401];

	static {
		for (int i = 0; i < 2401; i++) {
			pitches[i] = (float) Math.pow(2, (i - 1200d) / 1200d);
		}
	}

	private NoteUtils() {
	}

	public static float getPitchInOctave(Note note) {
		return getPitchInOctave(note.getKey(), note.getPitch());
	}

	public static float getPitchInOctave(byte key, short pitch) {
		key = applyPitchToKey(key, pitch);
		pitch %= 100;

		if (key < 9) key -= -15;
		else if (key < 33) key -= 9;
		else if (key < 57) key -= 33;
		else if (key < 81) key -= 57;
		else if (key < 105) key -= 81;

		return pitches[key * 100 + pitch];
	}

	public static byte applyPitchToKey(byte key, short pitch) {
		key += pitch / 100;
		return key;
	}

	public static float getPitchTransposed(Note note) {
		return getPitchTransposed(note.getKey(), note.getPitch());
	}

	public static float getPitchTransposed(byte key, short pitch) {
		pitch += key * 100;

		while (pitch < 3300) pitch += 1200;
		while (pitch > 5700) pitch -= 1200;

		pitch -= 3300;

		return pitches[pitch];
	}

	public static boolean isOutOfRange(byte key, short pitch) {
		key = applyPitchToKey(key, pitch);
		return key < 33 || key >= 57;
	}
}
