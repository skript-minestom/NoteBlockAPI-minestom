package com.xxmicloxx.NoteBlockAPI.event;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import net.minestom.server.event.Event;

/**
 * Called when a Song ends
 * or when no players are listening and auto destroy is enabled
 * @see SongPlayer
 */
public class SongEndEvent implements Event {

	private final SongPlayer song;

	public SongEndEvent(SongPlayer song) {
		this.song = song;
	}

	/**
	 * Returns SongPlayer which {@link Song} ends
	 */
	public SongPlayer getSongPlayer() {
		return song;
	}
}
