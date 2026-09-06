package com.xxmicloxx.NoteBlockAPI.model;

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
		this.soundFileName = soundFileName.replace(".ogg", "");
	}

	public byte getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	/**
	 * Sound file / event name without {@code .ogg}.
	 */
	public String getSoundFileName() {
		return soundFileName;
	}
}
