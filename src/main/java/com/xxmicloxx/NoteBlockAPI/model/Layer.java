package com.xxmicloxx.NoteBlockAPI.model;

import java.util.HashMap;

/**
 * Represents a series of notes in Note Block Studio.
 * A Layer can have a maximum of one note per tick (20 ticks a second)
 */
public class Layer {

	private HashMap<Integer, Note> notesAtTicks = new HashMap<>();
	private byte volume = 100;
	private int panning = 100;
	private String name = "";

	public HashMap<Integer, Note> getNotesAtTicks() {
		return notesAtTicks;
	}

	public void setNotesAtTicks(HashMap<Integer, Note> notesAtTicks) {
		this.notesAtTicks = notesAtTicks;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Note getNote(int tick) {
		return notesAtTicks.get(tick);
	}

	public void setNote(int tick, Note note) {
		notesAtTicks.put(tick, note);
	}

	public byte getVolume() {
		return volume;
	}

	public void setVolume(byte volume) {
		this.volume = volume;
	}

	public int getPanning() {
		return panning;
	}

	public void setPanning(int panning) {
		this.panning = panning;
	}
}
