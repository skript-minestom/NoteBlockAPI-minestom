package com.xxmicloxx.NoteBlockAPI.utils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;

/**
 * Plays Minecraft / resource-pack sounds for note playback.
 */
public final class Sounds {

	private Sounds() {
	}

	/**
	 * @param panOffset stereo offset in blocks (negative = left, positive = right); 0 = no pan
	 */
	public static void play(Player player, Pos location, String soundKey, Sound.Source source,
							float volume, float pitch, float panOffset) {
		Pos playAt = panOffset == 0 ? location : MathUtils.stereoPan(location, panOffset);
		SoundEvent soundEvent = SoundEvent.of(Key.key(normalizeKey(soundKey)), 16f);
		Sound adventureSound = Sound.sound(soundEvent, source == null ? Sound.Source.MASTER : source, volume, pitch);
		player.playSound(adventureSound, playAt.x(), playAt.y(), playAt.z());
	}

	private static String normalizeKey(String sound) {
		if (sound == null || sound.isEmpty()) {
			return "minecraft:block.note_block.harp";
		}
		if (sound.contains(":")) {
			return sound;
		}
		return "minecraft:" + sound;
	}
}
