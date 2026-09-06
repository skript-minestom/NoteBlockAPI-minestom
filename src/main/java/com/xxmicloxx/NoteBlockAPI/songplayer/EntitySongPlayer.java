package com.xxmicloxx.NoteBlockAPI.songplayer;

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import com.xxmicloxx.NoteBlockAPI.event.PlayerRangeStateChangeEvent;
import com.xxmicloxx.NoteBlockAPI.model.Layer;
import com.xxmicloxx.NoteBlockAPI.model.Note;
import com.xxmicloxx.NoteBlockAPI.model.Playlist;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

/**
 * Plays following an entity within range.
 */
public class EntitySongPlayer extends RangeSongPlayer {

	private Entity entity;

	public EntitySongPlayer(Song song) {
		super(song);
	}

	public EntitySongPlayer(Song song, Sound.Source soundSource) {
		super(song, soundSource);
	}

	public EntitySongPlayer(Playlist playlist) {
		super(playlist);
	}

	public EntitySongPlayer(Playlist playlist, Sound.Source soundSource) {
		super(playlist, soundSource);
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public Entity getEntity() {
		return entity;
	}

	@Override
	public boolean isInRange(Player player) {
		if (entity == null) {
			return false;
		}
		return player.getPosition().distance(entity.getPosition()) <= getDistance();
	}

	@Override
	public void playTick(Player player, int tick) {
		if (entity == null) {
			return;
		}
		if (entity.isRemoved()) {
			if (autoDestroy) {
				destroy();
			} else {
				setPlaying(false);
			}
			return;
		}
		if (player.getInstance() == null || !player.getInstance().equals(entity.getInstance())) {
			return;
		}

		byte playerVolume = NoteBlockAPI.getPlayerVolume(player);
		Pos playPos = entity.getPosition();

		for (Layer layer : song.getLayerHashMap().values()) {
			Note note = layer.getNote(tick);
			if (note == null) {
				continue;
			}

			float volume = ((layer.getVolume() * (int) this.volume * (int) playerVolume * note.getVelocity()) / 100_00_00_00F)
					* ((1F / 16F) * getDistance());

			NotePlayer.play(player, playPos, song, layer, note, soundSource, volume, !enable10Octave, fakeStereo);

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
	}
}
