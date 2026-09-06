package com.xxmicloxx.NoteBlockAPI;

import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Library facade for player volume, song-player registry, scheduling, and events.
 * Initializes lazily on first use; call {@link #shutdown()} when the server stops.
 */
public final class NoteBlockAPI {

	private static final Logger LOGGER = Logger.getLogger(NoteBlockAPI.class.getName());

	private static NoteBlockAPI instance;

	private final Map<UUID, ArrayList<SongPlayer>> playingSongs = new ConcurrentHashMap<>();
	private final Map<UUID, Byte> playerVolume = new ConcurrentHashMap<>();
	private final EventNode<Event> eventNode = EventNode.all("noteblockapi");
	private final ExecutorService asyncExecutor = Executors.newCachedThreadPool(r -> {
		Thread t = new Thread(r, "NoteBlockAPI-Async");
		t.setDaemon(true);
		return t;
	});

	private volatile boolean disabling = false;
	private volatile boolean initialized = false;

	private NoteBlockAPI() {
	}

	/**
	 * Ensures the library is ready. Usually unnecessary — first API use does this.
	 */
	public static synchronized void init() {
		ensure();
	}

	/**
	 * Stops async playback threads and unregisters the event node.
	 */
	public static synchronized void shutdown() {
		if (instance == null) {
			return;
		}
		instance.disabling = true;
		instance.asyncExecutor.shutdownNow();
		try {
			instance.asyncExecutor.awaitTermination(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		if (instance.initialized) {
			MinecraftServer.getGlobalEventHandler().removeChild(instance.eventNode);
			instance.initialized = false;
		}
	}

	private static NoteBlockAPI ensure() {
		if (instance == null) {
			instance = new NoteBlockAPI();
		}
		if (!instance.initialized) {
			MinecraftServer.getGlobalEventHandler().addChild(instance.eventNode);
			instance.initialized = true;
		}
		return instance;
	}

	public static boolean isReceivingSong(Player player) {
		return isReceivingSong(player.getUuid());
	}

	public static boolean isReceivingSong(UUID uuid) {
		ArrayList<SongPlayer> songs = ensure().playingSongs.get(uuid);
		return songs != null && !songs.isEmpty();
	}

	public static void stopPlaying(Player player) {
		stopPlaying(player.getUuid());
	}

	public static void stopPlaying(UUID uuid) {
		ArrayList<SongPlayer> songs = ensure().playingSongs.get(uuid);
		if (songs == null) {
			return;
		}
		for (SongPlayer songPlayer : new ArrayList<>(songs)) {
			songPlayer.removePlayer(uuid);
		}
	}

	public static void setPlayerVolume(Player player, byte volume) {
		setPlayerVolume(player.getUuid(), volume);
	}

	public static void setPlayerVolume(UUID uuid, byte volume) {
		ensure().playerVolume.put(uuid, volume);
	}

	public static byte getPlayerVolume(Player player) {
		return getPlayerVolume(player.getUuid());
	}

	public static byte getPlayerVolume(UUID uuid) {
		NoteBlockAPI api = ensure();
		return api.playerVolume.computeIfAbsent(uuid, id -> (byte) 100);
	}

	public static ArrayList<SongPlayer> getSongPlayersByPlayer(Player player) {
		return getSongPlayersByPlayer(player.getUuid());
	}

	public static ArrayList<SongPlayer> getSongPlayersByPlayer(UUID player) {
		return ensure().playingSongs.get(player);
	}

	/**
	 * Internal registry update used by {@link SongPlayer}.
	 */
	public static void registerSongPlayer(UUID player, ArrayList<SongPlayer> songs) {
		ensure().playingSongs.put(player, songs);
	}

	public static NoteBlockAPI getAPI() {
		return ensure();
	}

	public static EventNode<Event> getEventNode() {
		return ensure().eventNode;
	}

	public void doSync(Runnable runnable) {
		MinecraftServer.getSchedulerManager().scheduleNextTick(runnable);
	}

	public void doAsync(Runnable runnable) {
		asyncExecutor.execute(runnable);
	}

	public boolean isDisabling() {
		return disabling;
	}

	public void callEvent(Event event) {
		eventNode.call(event);
	}

	public static Player getOnlinePlayer(UUID uuid) {
		return MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
	}

	public static Logger getLogger() {
		return LOGGER;
	}
}
