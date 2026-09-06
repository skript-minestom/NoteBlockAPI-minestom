package com.xxmicloxx.NoteBlockAPI.utils;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import com.xxmicloxx.NoteBlockAPI.model.CustomInstrument;
import com.xxmicloxx.NoteBlockAPI.model.Layer;
import com.xxmicloxx.NoteBlockAPI.model.Note;
import com.xxmicloxx.NoteBlockAPI.model.Song;

/**
 * Reads Open Note Block Studio {@code .nbs} files.
 */
public final class NBSDecoder {

	private NBSDecoder() {
	}

	public static Song parse(Path path) throws IOException {
		try (InputStream in = Files.newInputStream(path)) {
			return parse(in, path.toFile());
		}
	}

	public static Song parse(File songFile) throws IOException {
		try (InputStream in = Files.newInputStream(songFile.toPath())) {
			return parse(in, songFile);
		}
	}

	public static Song parse(InputStream inputStream) throws IOException {
		return parse(inputStream, null);
	}

	private static Song parse(InputStream inputStream, File songFile) throws IOException {
		HashMap<Integer, Layer> layerHashMap = new HashMap<>();
		boolean isStereo = false;
		try {
			DataInputStream dataInputStream = new DataInputStream(inputStream);
			short length = readShort(dataInputStream);
			int firstCustomInstrument = InstrumentUtils.getCustomInstrumentFirstIndex();
			int nbsversion = 0;
			if (length == 0) {
				nbsversion = dataInputStream.readByte();
				firstCustomInstrument = dataInputStream.readUnsignedByte();
				if (nbsversion >= 3) {
					length = readShort(dataInputStream);
				}
			}
			// Remap notes if the song was authored against fewer vanilla instruments
			int instrumentShift = InstrumentUtils.getCustomInstrumentFirstIndex() - firstCustomInstrument;

			short songHeight = readShort(dataInputStream);
			String title = readString(dataInputStream);
			String author = readString(dataInputStream);
			String originalAuthor = readString(dataInputStream);
			String description = readString(dataInputStream);
			float speed = readShort(dataInputStream) / 100f;
			dataInputStream.readBoolean(); // auto-save
			dataInputStream.readByte(); // auto-save duration
			dataInputStream.readByte(); // time signature
			readInt(dataInputStream); // minutes spent
			readInt(dataInputStream); // left clicks
			readInt(dataInputStream); // right clicks
			readInt(dataInputStream); // blocks added
			readInt(dataInputStream); // blocks removed
			readString(dataInputStream); // .mid/.schematic name
			if (nbsversion >= 4) {
				dataInputStream.readByte(); // loop
				dataInputStream.readByte(); // max loop count
				readShort(dataInputStream); // loop start
			}

			short tick = -1;
			while (true) {
				short jumpTicks = readShort(dataInputStream);
				if (jumpTicks == 0) {
					break;
				}
				tick += jumpTicks;
				short layer = -1;
				while (true) {
					short jumpLayers = readShort(dataInputStream);
					if (jumpLayers == 0) {
						break;
					}
					layer += jumpLayers;
					byte instrument = dataInputStream.readByte();
					if (instrumentShift > 0 && instrument >= firstCustomInstrument) {
						instrument += instrumentShift;
					}

					byte key = dataInputStream.readByte();
					byte velocity = 100;
					int panning = 100;
					short pitch = 0;
					if (nbsversion >= 4) {
						velocity = dataInputStream.readByte();
						panning = 200 - dataInputStream.readUnsignedByte();
						pitch = readShort(dataInputStream);
					}
					if (panning != 100) {
						isStereo = true;
					}
					setNote(layer, tick, new Note(instrument, key, velocity, panning, pitch), layerHashMap);
				}
			}

			if (nbsversion > 0 && nbsversion < 3) {
				length = tick;
			}

			for (int i = 0; i < songHeight; i++) {
				Layer layer = layerHashMap.get(i);
				String name = readString(dataInputStream);
				if (nbsversion >= 4) {
					dataInputStream.readByte(); // layer lock
				}
				byte volume = dataInputStream.readByte();
				int panning = 100;
				if (nbsversion >= 2) {
					panning = 200 - dataInputStream.readUnsignedByte();
				}
				if (panning != 100) {
					isStereo = true;
				}
				if (layer != null) {
					layer.setName(name);
					layer.setVolume(volume);
					layer.setPanning(panning);
				}
			}

			byte customAmnt = dataInputStream.readByte();
			CustomInstrument[] customInstruments = new CustomInstrument[customAmnt];
			for (int index = 0; index < customAmnt; index++) {
				customInstruments[index] = new CustomInstrument((byte) index,
						readString(dataInputStream), readString(dataInputStream));
				dataInputStream.readByte(); // pitch
				dataInputStream.readByte(); // key
			}

			int resolvedFirstCustom = firstCustomInstrument + Math.max(0, instrumentShift);

			return new Song(speed, layerHashMap, songHeight, length, title,
					author, originalAuthor, description, songFile, resolvedFirstCustom, customInstruments, isStereo);
		} catch (EOFException e) {
			String name = songFile != null ? songFile.getName() : "stream";
			throw new IOException("Song is corrupted: " + name, e);
		}
	}

	private static void setNote(int layerIndex, int ticks, Note note, HashMap<Integer, Layer> layerHashMap) {
		Layer layer = layerHashMap.get(layerIndex);
		if (layer == null) {
			layer = new Layer();
			layerHashMap.put(layerIndex, layer);
		}
		layer.setNote(ticks, note);
	}

	private static short readShort(DataInputStream dataInputStream) throws IOException {
		int byte1 = dataInputStream.readUnsignedByte();
		int byte2 = dataInputStream.readUnsignedByte();
		return (short) (byte1 + (byte2 << 8));
	}

	private static int readInt(DataInputStream dataInputStream) throws IOException {
		int byte1 = dataInputStream.readUnsignedByte();
		int byte2 = dataInputStream.readUnsignedByte();
		int byte3 = dataInputStream.readUnsignedByte();
		int byte4 = dataInputStream.readUnsignedByte();
		return byte1 + (byte2 << 8) + (byte3 << 16) + (byte4 << 24);
	}

	private static String readString(DataInputStream dataInputStream) throws IOException {
		int length = readInt(dataInputStream);
		StringBuilder builder = new StringBuilder(length);
		for (; length > 0; --length) {
			char c = (char) dataInputStream.readByte();
			if (c == 0x0D) {
				c = ' ';
			}
			builder.append(c);
		}
		return builder.toString();
	}
}
