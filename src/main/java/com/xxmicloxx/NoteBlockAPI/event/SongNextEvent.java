package com.xxmicloxx.NoteBlockAPI.event;

import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import net.minestom.server.event.Event;

public class SongNextEvent implements Event {

	private final SongPlayer song;

	public SongNextEvent(SongPlayer song) {
		this.song = song;
	}

	/**
	 * Returns SongPlayer which is going to play next song in playlist
	 */
	public SongPlayer getSongPlayer() {
		return song;
	}
}
