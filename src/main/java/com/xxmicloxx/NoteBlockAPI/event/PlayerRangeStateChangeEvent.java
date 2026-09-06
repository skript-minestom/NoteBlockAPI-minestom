package com.xxmicloxx.NoteBlockAPI.event;

import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

/**
 * Called whenever a Player enters or leave the range of a stationary SongPlayer
 */
public class PlayerRangeStateChangeEvent implements Event {

	private final SongPlayer song;
	private final Player player;
	private final boolean state;

	public PlayerRangeStateChangeEvent(SongPlayer song, Player player, boolean state) {
		this.song = song;
		this.player = player;
		this.state = state;
	}

	public SongPlayer getSongPlayer() {
		return song;
	}

	public Player getPlayer() {
		return player;
	}

	/**
	 * Returns true if Player is actually in SongPlayer range
	 */
	public boolean isInRange() {
		return state;
	}
}
