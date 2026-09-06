package com.xxmicloxx.NoteBlockAPI.songplayer;

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import com.xxmicloxx.NoteBlockAPI.model.Layer;
import com.xxmicloxx.NoteBlockAPI.model.Note;
import com.xxmicloxx.NoteBlockAPI.model.Playlist;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;

/**
 * Plays for every added player regardless of position.
 */
public class RadioSongPlayer extends SongPlayer {

	public RadioSongPlayer(Song song) {
		super(song);
	}

	public RadioSongPlayer(Song song, Sound.Source soundSource) {
		super(song, soundSource);
	}

	public RadioSongPlayer(Playlist playlist) {
		super(playlist);
	}

	public RadioSongPlayer(Playlist playlist, Sound.Source soundSource) {
		super(playlist, soundSource);
	}

	/**
	 * Create a radio player, add the given players, and start playback.
	 */
	public static RadioSongPlayer play(Song song, Player... players) {
		RadioSongPlayer rsp = new RadioSongPlayer(song);
		for (Player player : players) {
			rsp.addPlayer(player);
		}
		rsp.setPlaying(true);
		return rsp;
	}

	@Override
	public void playTick(Player player, int tick) {
		byte playerVolume = NoteBlockAPI.getPlayerVolume(player);
		Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);

		for (Layer layer : song.getLayerHashMap().values()) {
			Note note = layer.getNote(tick);
			if (note == null) {
				continue;
			}

			float volume = (layer.getVolume() * (int) this.volume * (int) playerVolume * note.getVelocity()) / 100_00_00_00F;
			NotePlayer.play(player, eye, song, layer, note, soundSource, volume, !enable10Octave, fakeStereo);
		}
	}
}
