package com.xxmicloxx.NoteBlockAPI.songplayer;

import com.xxmicloxx.NoteBlockAPI.model.CustomInstrument;
import com.xxmicloxx.NoteBlockAPI.model.Layer;
import com.xxmicloxx.NoteBlockAPI.model.Note;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.utils.InstrumentUtils;
import com.xxmicloxx.NoteBlockAPI.utils.NoteUtils;
import com.xxmicloxx.NoteBlockAPI.utils.Sounds;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;

/**
 * Resolves instrument sound keys and plays a note with optional NBS / fake stereo panning.
 */
final class NotePlayer {

	private static final float FAKE_STEREO_DISTANCE = 2f;
	private static final float MAX_PAN_DISTANCE = 2f;

	private NotePlayer() {
	}

	static void play(Player player, Pos location, Song song, Layer layer, Note note,
					 Sound.Source source, float volume, boolean transpose, boolean fakeStereo) {
		String soundKey = resolveSoundKey(song, note, transpose);
		float pitch = transpose ? NoteUtils.getPitchTransposed(note) : NoteUtils.getPitchInOctave(note);

		if (fakeStereo && !song.isStereo()) {
			Sounds.play(player, location, soundKey, source, volume, pitch, FAKE_STEREO_DISTANCE);
			Sounds.play(player, location, soundKey, source, volume, pitch, -FAKE_STEREO_DISTANCE);
			return;
		}

		float pan = 0;
		if (song.isStereo()) {
			if (layer.getPanning() == 100) {
				pan = ((note.getPanning() - 100) / 100f) * MAX_PAN_DISTANCE;
			} else {
				pan = ((layer.getPanning() - 100 + note.getPanning() - 100) / 200f) * MAX_PAN_DISTANCE;
			}
		}

		Sounds.play(player, location, soundKey, source, volume, pitch, pan);
	}

	private static String resolveSoundKey(Song song, Note note, boolean transpose) {
		if (InstrumentUtils.isCustomInstrument(note.getInstrument())) {
			int index = note.getInstrument() - InstrumentUtils.getCustomInstrumentFirstIndex();
			CustomInstrument[] customs = song.getCustomInstruments();
			if (index < 0 || index >= customs.length) {
				return InstrumentUtils.getSoundName((byte) 0);
			}
			CustomInstrument instrument = customs[index];
			if (!transpose) {
				return InstrumentUtils.warpNameOutOfRange(instrument.getSoundFileName(), note.getKey(), note.getPitch());
			}
			return instrument.getSoundFileName();
		}

		if (!transpose && NoteUtils.isOutOfRange(note.getKey(), note.getPitch())) {
			return InstrumentUtils.warpNameOutOfRange(note.getInstrument(), note.getKey(), note.getPitch());
		}
		return InstrumentUtils.getSoundName(note.getInstrument());
	}
}
