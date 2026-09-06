package com.xxmicloxx.NoteBlockAPI.event;

import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.CancellableEvent;

/**
 * Called whenever a SongPlayer is destroyed
 * @see SongPlayer
 */
public class SongDestroyingEvent implements Event, CancellableEvent {

	private final SongPlayer song;
	private boolean cancelled = false;

	public SongDestroyingEvent(SongPlayer song) {
		this.song = song;
	}

	public SongPlayer getSongPlayer() {
		return song;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
}
