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
import net.minestom.server.instance.block.Block;

/**
 * Plays at a note block within range.
 */
public class NoteBlockSongPlayer extends RangeSongPlayer {

	private Instance instance;
	private Pos noteBlockPos;

	public NoteBlockSongPlayer(Song song) {
		super(song);
	}

	public NoteBlockSongPlayer(Song song, Sound.Source soundSource) {
		super(song, soundSource);
	}

	public NoteBlockSongPlayer(Playlist playlist) {
		super(playlist);
	}

	public NoteBlockSongPlayer(Playlist playlist, Sound.Source soundSource) {
		super(playlist, soundSource);
	}

	public Pos getNoteBlock() {
		return noteBlockPos;
	}

	public Instance getNoteBlockInstance() {
		return instance;
	}

	public void setNoteBlock(Instance instance, Pos noteBlockPos) {
		this.instance = instance;
		this.noteBlockPos = noteBlockPos;
	}

	@Override
	public void playTick(Player player, int tick) {
		if (instance == null || noteBlockPos == null) {
			return;
		}
		Block block = instance.getBlock(noteBlockPos);
		if (!block.compare(Block.NOTE_BLOCK)) {
			return;
		}
		if (player.getInstance() == null || !player.getInstance().equals(instance)) {
			return;
		}

		byte playerVolume = NoteBlockAPI.getPlayerVolume(player);
		Pos loc = new Pos(noteBlockPos.x() + 0.5f, noteBlockPos.y() - 0.5f, noteBlockPos.z() + 0.5f);

		for (Layer layer : song.getLayerHashMap().values()) {
			Note note = layer.getNote(tick);
			if (note == null) {
				continue;
			}

			float volume = ((layer.getVolume() * (int) this.volume * (int) playerVolume * note.getVelocity()) / 100_00_00_00F)
					* ((1F / 16F) * getDistance());

			NotePlayer.play(player, loc, song, layer, note, soundSource, volume, !enable10Octave, fakeStereo);

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

	@Override
	public boolean isInRange(Player player) {
		if (noteBlockPos == null) {
			return false;
		}
		Pos loc = new Pos(noteBlockPos.x() + 0.5f, noteBlockPos.y() - 0.5f, noteBlockPos.z() + 0.5f);
		return player.getPosition().distance(loc) <= getDistance();
	}
}
