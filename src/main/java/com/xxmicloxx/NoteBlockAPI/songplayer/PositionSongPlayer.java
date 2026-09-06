package com.xxmicloxx.NoteBlockAPI.songplayer;

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import com.xxmicloxx.NoteBlockAPI.event.PlayerRangeStateChangeEvent;
import com.xxmicloxx.NoteBlockAPI.model.Layer;
import com.xxmicloxx.NoteBlockAPI.model.Note;
import com.xxmicloxx.NoteBlockAPI.model.Playlist;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

/**
 * Plays at a fixed position within range.
 */
public class PositionSongPlayer extends RangeSongPlayer {

	private Instance instance;
	private Pos targetPosition;

	public PositionSongPlayer(Song song) {
		super(song);
	}

	public PositionSongPlayer(Song song, Sound.Source soundSource) {
		super(song, soundSource);
	}

	public PositionSongPlayer(Playlist playlist) {
		super(playlist);
	}

	public PositionSongPlayer(Playlist playlist, Sound.Source soundSource) {
		super(playlist, soundSource);
	}

	public Instance getInstance() {
		return instance;
	}

	public Pos getTargetPosition() {
		return targetPosition;
	}

	public void setTargetLocation(Instance instance, Pos position) {
		this.instance = instance;
		this.targetPosition = position;
	}

	@Override
	public void playTick(Player player, int tick) {
		if (instance == null || targetPosition == null) {
			return;
		}
		if (player.getInstance() == null || !player.getInstance().equals(instance)) {
			return;
		}

		byte playerVolume = NoteBlockAPI.getPlayerVolume(player);

		for (Layer layer : song.getLayerHashMap().values()) {
			Note note = layer.getNote(tick);
			if (note == null) {
				continue;
			}

			float volume = ((layer.getVolume() * (int) this.volume * (int) playerVolume * note.getVelocity()) / 100_00_00_00F)
					* ((1F / 16F) * getDistance());

			NotePlayer.play(player, targetPosition, song, layer, note, soundSource, volume, !enable10Octave, fakeStereo);
			updateRangeState(player);
		}
	}

	private void updateRangeState(Player player) {
		if (isInRange(player)) {
			if (!Boolean.TRUE.equals(playerList.get(player.getUuid()))) {
				playerList.put(player.getUuid(), true);
				NoteBlockAPI.getAPI().callEvent(new PlayerRangeStateChangeEvent(this, player, true));
			}
		} else if (Boolean.TRUE.equals(playerList.get(player.getUuid()))) {
			playerList.put(player.getUuid(), false);
			NoteBlockAPI.getAPI().callEvent(new PlayerRangeStateChangeEvent(this, player, false));
		}
	}

	@Override
	public boolean isInRange(Player player) {
		if (targetPosition == null) {
			return false;
		}
		return player.getPosition().distance(targetPosition) <= getDistance();
	}
}
