package com.xxmicloxx.NoteBlockAPI.event;

import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import net.minestom.server.event.Event;

/**
 * Called whenever a SongPlayer is stopped
 * @see SongPlayer
 */
public class SongStoppedEvent implements Event {

	private final SongPlayer songPlayer;

	public SongStoppedEvent(SongPlayer songPlayer) {
		this.songPlayer = songPlayer;
	}

	public SongPlayer getSongPlayer() {
		return songPlayer;
	}
}
