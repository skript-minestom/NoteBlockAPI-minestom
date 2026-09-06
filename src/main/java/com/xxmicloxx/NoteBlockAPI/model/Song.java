package com.xxmicloxx.NoteBlockAPI.model;

import com.xxmicloxx.NoteBlockAPI.utils.InstrumentUtils;

import java.io.File;
import java.util.HashMap;

/**
 * A parsed Note Block Studio song.
 */
public class Song implements Cloneable {

	private final HashMap<Integer, Layer> layerHashMap;
	private final short songHeight;
	private final short length;
	private final String title;
	private final File path;
	private final String author;
	private final String originalAuthor;
	private final String description;
	private final float speed;
	private final float delay;
	private final CustomInstrument[] customInstruments;
	private final int firstCustomInstrumentIndex;
	private final boolean isStereo;

	public Song(Song other) {
		this(other.getSpeed(), other.getLayerHashMap(), other.getSongHeight(),
				other.getLength(), other.getTitle(), other.getAuthor(), other.getOriginalAuthor(),
				other.getDescription(), other.getPath(), other.getFirstCustomInstrumentIndex(),
				other.getCustomInstruments(), other.isStereo());
	}

	public Song(float speed, HashMap<Integer, Layer> layerHashMap,
				short songHeight, short length, String title, String author, String originalAuthor,
				String description, File path, int firstCustomInstrumentIndex,
				CustomInstrument[] customInstruments, boolean isStereo) {
		this.speed = speed;
		this.delay = 20 / speed;
		this.layerHashMap = layerHashMap;
		this.songHeight = songHeight;
		this.length = length;
		this.title = title;
		this.author = author;
		this.originalAuthor = originalAuthor;
		this.description = description;
		this.path = path;
		this.firstCustomInstrumentIndex = firstCustomInstrumentIndex;
		this.customInstruments = customInstruments != null ? customInstruments : new CustomInstrument[0];
		this.isStereo = isStereo;
	}

	public Song(float speed, HashMap<Integer, Layer> layerHashMap,
				short songHeight, short length, String title, String author, String originalAuthor,
				String description, File path, boolean isStereo) {
		this(speed, layerHashMap, songHeight, length, title, author, originalAuthor, description, path,
				InstrumentUtils.getCustomInstrumentFirstIndex(), new CustomInstrument[0], isStereo);
	}

	public HashMap<Integer, Layer> getLayerHashMap() {
		return layerHashMap;
	}

	public short getSongHeight() {
		return songHeight;
	}

	public short getLength() {
		return length;
	}

	public String getTitle() {
		return title;
	}

	public String getAuthor() {
		return author;
	}

	public String getOriginalAuthor() {
		return originalAuthor;
	}

	public File getPath() {
		return path;
	}

	public String getDescription() {
		return description;
	}

	public float getSpeed() {
		return speed;
	}

	public float getDelay() {
		return delay;
	}

	public CustomInstrument[] getCustomInstruments() {
		return customInstruments;
	}

	@Override
	public Song clone() {
		return new Song(this);
	}

	public int getFirstCustomInstrumentIndex() {
		return firstCustomInstrumentIndex;
	}

	public boolean isStereo() {
		return isStereo;
	}
}
