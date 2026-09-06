package com.xxmicloxx.NoteBlockAPI.utils;

/**
 * Vanilla note-block instrument → sound key mapping (current protocol, 20 instruments).
 */
public final class InstrumentUtils {

	public static final byte CUSTOM_INSTRUMENT_FIRST_INDEX = 20;

	private InstrumentUtils() {
	}

	/**
	 * Add suffix to vanilla instrument to use sound outside 2 octave range.
	 */
	public static String warpNameOutOfRange(byte instrument, byte key, short pitch) {
		return warpNameOutOfRange(getSoundName(instrument), key, pitch);
	}

	/**
	 * Add suffix to qualified name to use sound outside 2 octave range.
	 */
	public static String warpNameOutOfRange(String name, byte key, short pitch) {
		key = NoteUtils.applyPitchToKey(key, pitch);
		if (key < 9) name += "_-2";
		else if (key < 33) name += "_-1";
		else if (key < 57) ;
		else if (key < 81) name += "_1";
		else if (key < 105) name += "_2";
		return name;
	}

	public static String getSoundName(byte instrument) {
		return switch (instrument) {
			case 1 -> "minecraft:block.note_block.bass";
			case 2 -> "minecraft:block.note_block.basedrum";
			case 3 -> "minecraft:block.note_block.snare";
			case 4 -> "minecraft:block.note_block.hat";
			case 5 -> "minecraft:block.note_block.guitar";
			case 6 -> "minecraft:block.note_block.flute";
			case 7 -> "minecraft:block.note_block.bell";
			case 8 -> "minecraft:block.note_block.chime";
			case 9 -> "minecraft:block.note_block.xylophone";
			case 10 -> "minecraft:block.note_block.iron_xylophone";
			case 11 -> "minecraft:block.note_block.cow_bell";
			case 12 -> "minecraft:block.note_block.didgeridoo";
			case 13 -> "minecraft:block.note_block.bit";
			case 14 -> "minecraft:block.note_block.banjo";
			case 15 -> "minecraft:block.note_block.pling";
			case 16 -> "minecraft:block.note_block.trumpet";
			case 17 -> "minecraft:block.note_block.trumpet_exposed";
			case 18 -> "minecraft:block.note_block.trumpet_weathered";
			case 19 -> "minecraft:block.note_block.trumpet_oxidized";
			default -> "minecraft:block.note_block.harp";
		};
	}

	public static boolean isCustomInstrument(byte instrument) {
		return instrument >= CUSTOM_INSTRUMENT_FIRST_INDEX;
	}

	public static byte getCustomInstrumentFirstIndex() {
		return CUSTOM_INSTRUMENT_FIRST_INDEX;
	}
}
