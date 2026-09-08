package com.xxmicloxx.NoteBlockAPI.model;

import com.xxmicloxx.NoteBlockAPI.utils.SoundKeyResolver;

/**
 * Custom instrument defined in an NBS file.
 */
public class CustomInstrument {

	private final byte index;
	private final String name;
	private final String soundFileName;

	public CustomInstrument(byte index, String name, String soundFileName) {
		this.index = index;
		this.name = name;
		this.soundFileName = SoundKeyResolver.resolve(soundFileName, name);
	}

	public byte getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	/**
	 * Playable sound event / key (OpenNBS file paths are resolved to vanilla events when known).
	 */
	public String getSoundFileName() {
		return soundFileName;
	}
}
