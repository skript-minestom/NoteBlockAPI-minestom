package com.xxmicloxx.NoteBlockAPI.songplayer;

import com.xxmicloxx.NoteBlockAPI.model.Playlist;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;

/**
 * SongPlayer that only plays within a distance of a source.
 */
public abstract class RangeSongPlayer extends SongPlayer {

	private int distance = 16;

	public RangeSongPlayer(Song song) {
		super(song);
	}

	public RangeSongPlayer(Song song, Sound.Source soundSource) {
		super(song, soundSource);
	}

	public RangeSongPlayer(Playlist playlist) {
		super(playlist);
	}

	public RangeSongPlayer(Playlist playlist, Sound.Source soundSource) {
		super(playlist, soundSource);
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public int getDistance() {
		return distance;
	}

	public abstract boolean isInRange(Player player);
}
