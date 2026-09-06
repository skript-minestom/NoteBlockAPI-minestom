package com.xxmicloxx.NoteBlockAPI.event;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.CancellableEvent;

/**
 * Called when a song is about to loop. Cancel to stop looping (same semantics as Bukkit NoteBlockAPI).
 */
public class SongLoopEvent implements Event, CancellableEvent {

	private final SongPlayer song;
	private boolean cancelled = false;

	public SongLoopEvent(SongPlayer song) {
		this.song = song;
	}

	/**
	 * Returns SongPlayer which {@link Song} ends and is going to start again
	 */
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
