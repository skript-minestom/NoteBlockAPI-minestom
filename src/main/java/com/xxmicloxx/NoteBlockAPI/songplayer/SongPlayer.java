package com.xxmicloxx.NoteBlockAPI.songplayer;

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import com.xxmicloxx.NoteBlockAPI.event.*;
import com.xxmicloxx.NoteBlockAPI.model.*;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Plays a {@link Song} for a set of players.
 */
public abstract class SongPlayer {

	protected Song song;
	protected Playlist playlist;
	protected int actualSong = 0;

	protected boolean playing = false;
	protected boolean fading = false;
	protected short tick = -1;
	protected final Map<UUID, Boolean> playerList = new ConcurrentHashMap<>();

	protected boolean autoDestroy = false;
	protected boolean destroyed = false;

	protected byte volume = 100;
	protected Fade fadeIn;
	protected Fade fadeOut;
	protected Fade fadeTemp = null;
	protected RepeatMode repeat = RepeatMode.NO;
	protected boolean random = false;

	protected final Map<Song, Boolean> songQueue = new ConcurrentHashMap<>();

	private final Lock lock = new ReentrantLock();
	private final Random rng = new Random();

	protected Sound.Source soundSource = Sound.Source.MASTER;
	protected boolean enable10Octave = false;
	protected boolean fakeStereo = false;

	public SongPlayer(Song song) {
		this(new Playlist(song), Sound.Source.MASTER, false);
	}

	public SongPlayer(Song song, Sound.Source soundSource) {
		this(new Playlist(song), soundSource, false);
	}

	public SongPlayer(Song song, Sound.Source soundSource, boolean random) {
		this(new Playlist(song), soundSource, random);
	}

	public SongPlayer(Playlist playlist) {
		this(playlist, Sound.Source.MASTER, false);
	}

	public SongPlayer(Playlist playlist, Sound.Source soundSource) {
		this(playlist, soundSource, false);
	}

	public SongPlayer(Playlist playlist, Sound.Source soundSource, boolean random) {
		this.playlist = playlist;
		this.random = random;
		this.soundSource = soundSource == null ? Sound.Source.MASTER : soundSource;

		fadeIn = new Fade(FadeType.NONE, 60);
		fadeIn.setFadeStart((byte) 0);
		fadeIn.setFadeTarget(volume);

		fadeOut = new Fade(FadeType.NONE, 60);
		fadeOut.setFadeStart(volume);
		fadeOut.setFadeTarget((byte) 0);

		if (random) {
			checkPlaylistQueue();
			actualSong = rng.nextInt(playlist.getCount());
		}
		this.song = playlist.get(actualSong);

		start();
	}

	public boolean isEnable10Octave() {
		return enable10Octave;
	}

	/**
	 * When true, notes outside the vanilla 2-octave range use resource-pack sound suffixes.
	 * When false (default), notes are transposed into that range.
	 */
	public void setEnable10Octave(boolean enable10Octave) {
		this.enable10Octave = enable10Octave;
	}

	/**
	 * When true and the song is not stereo, play each note from left and right offsets (fake stereo).
	 * Stereo NBS songs always use their own panning.
	 */
	public void setFakeStereo(boolean fakeStereo) {
		this.fakeStereo = fakeStereo;
	}

	public boolean isFakeStereo() {
		return fakeStereo;
	}

	public Sound.Source getSoundSource() {
		return soundSource;
	}

	public void setSoundSource(Sound.Source soundSource) {
		this.soundSource = soundSource == null ? Sound.Source.MASTER : soundSource;
	}

	private void start() {
		NoteBlockAPI api = NoteBlockAPI.getAPI();
		api.doAsync(() -> {
			while (!destroyed) {
				long startTime = System.currentTimeMillis();
				lock.lock();
				try {
					if (destroyed || NoteBlockAPI.getAPI().isDisabling()) {
						break;
					}

					if (playing || fading) {
						if (fadeTemp != null) {
							if (fadeTemp.isDone()) {
								fadeTemp = null;
								fading = false;
								if (!playing) {
									SongStoppedEvent event = new SongStoppedEvent(this);
									api.doSync(() -> api.callEvent(event));
									volume = fadeIn.getFadeTarget();
									continue;
								}
							} else {
								int fade = fadeTemp.calculateFade();
								if (fade != -1) {
									volume = (byte) fade;
								}
							}
						} else if (tick < fadeIn.getFadeDuration()) {
							int fade = fadeIn.calculateFade();
							if (fade != -1) {
								volume = (byte) fade;
							}
						} else if (tick >= song.getLength() - fadeOut.getFadeDuration()) {
							int fade = fadeOut.calculateFade();
							if (fade != -1) {
								volume = (byte) fade;
							}
						}

						tick++;
						if (tick > song.getLength()) {
							tick = -1;
							fadeIn.setFadeDone(0);
							fadeOut.setFadeDone(0);
							volume = fadeIn.getFadeTarget();
							if (repeat == RepeatMode.ONE) {
								SongLoopEvent event = new SongLoopEvent(this);
								api.callEvent(event);
								if (!event.isCancelled()) {
									continue;
								}
							} else if (random) {
								songQueue.put(song, true);
								checkPlaylistQueue();
								ArrayList<Song> left = new ArrayList<>();
								for (Map.Entry<Song, Boolean> entry : songQueue.entrySet()) {
									if (!entry.getValue()) {
										left.add(entry.getKey());
									}
								}

								if (left.isEmpty()) {
									left.addAll(songQueue.keySet());
									for (Song s : songQueue.keySet()) {
										songQueue.put(s, false);
									}
									song = left.get(rng.nextInt(left.size()));
									actualSong = playlist.getIndex(song);
									if (repeat == RepeatMode.ALL) {
										SongLoopEvent event = new SongLoopEvent(this);
										api.callEvent(event);
										if (!event.isCancelled()) {
											continue;
										}
									}
								} else {
									song = left.get(rng.nextInt(left.size()));
									actualSong = playlist.getIndex(song);
									api.callEvent(new SongNextEvent(this));
									continue;
								}
							} else if (playlist.hasNext(actualSong)) {
								actualSong++;
								song = playlist.get(actualSong);
								api.callEvent(new SongNextEvent(this));
								continue;
							} else {
								actualSong = 0;
								song = playlist.get(actualSong);
								if (repeat == RepeatMode.ALL) {
									SongLoopEvent event = new SongLoopEvent(this);
									api.callEvent(event);
									if (!event.isCancelled()) {
										continue;
									}
								}
							}
							playing = false;
							SongEndEvent event = new SongEndEvent(this);
							api.doSync(() -> api.callEvent(event));
							if (autoDestroy) {
								destroy();
							}
							continue;
						}

						final short tickToPlay = tick;
						api.doSync(() -> {
							try {
								for (UUID uuid : playerList.keySet()) {
									Player player = NoteBlockAPI.getOnlinePlayer(uuid);
									if (player == null) {
										continue;
									}
									playTick(player, tickToPlay);
								}
							} catch (Exception e) {
								NoteBlockAPI.getLogger().log(java.util.logging.Level.SEVERE,
										"Error during playback of " + describeSong(), e);
							}
						});
					}
				} catch (Exception e) {
					NoteBlockAPI.getLogger().log(java.util.logging.Level.SEVERE,
							"Error during playback of " + describeSong(), e);
				} finally {
					lock.unlock();
				}

				if (destroyed) {
					break;
				}

				long duration = System.currentTimeMillis() - startTime;
				float delayMillis = song.getDelay() * 50;
				if (duration < delayMillis) {
					try {
						Thread.sleep((long) (delayMillis - duration));
					} catch (InterruptedException ignored) {
					}
				}
			}
		});
	}

	private String describeSong() {
		if (song == null) {
			return "null";
		}
		return song.getPath() + " (" + song.getAuthor() + " - " + song.getTitle() + ")";
	}

	private void checkPlaylistQueue() {
		songQueue.keySet().removeIf(s -> !playlist.contains(s));
		for (Song s : playlist.getSongList()) {
			songQueue.putIfAbsent(s, false);
		}
	}

	public Fade getFadeIn() {
		return fadeIn;
	}

	public Fade getFadeOut() {
		return fadeOut;
	}

	public Set<UUID> getPlayerUUIDs() {
		return Collections.unmodifiableSet(new HashSet<>(playerList.keySet()));
	}

	public SongPlayer addPlayer(Player player) {
		return addPlayer(player.getUuid());
	}

	public SongPlayer addPlayer(UUID player) {
		lock.lock();
		try {
			if (!playerList.containsKey(player)) {
				playerList.put(player, false);
				ArrayList<SongPlayer> songs = NoteBlockAPI.getSongPlayersByPlayer(player);
				if (songs == null) {
					songs = new ArrayList<>();
				}
				songs.add(this);
				NoteBlockAPI.registerSongPlayer(player, songs);
			}
		} finally {
			lock.unlock();
		}
		return this;
	}

	public boolean getAutoDestroy() {
		lock.lock();
		try {
			return autoDestroy;
		} finally {
			lock.unlock();
		}
	}

	public void setAutoDestroy(boolean autoDestroy) {
		lock.lock();
		try {
			this.autoDestroy = autoDestroy;
		} finally {
			lock.unlock();
		}
	}

	public abstract void playTick(Player player, int tick);

	public void destroy() {
		lock.lock();
		try {
			SongDestroyingEvent event = new SongDestroyingEvent(this);
			NoteBlockAPI.getAPI().callEvent(event);
			if (event.isCancelled()) {
				return;
			}
			destroyed = true;
			playing = false;
			tick = -1;
		} finally {
			lock.unlock();
		}
	}

	public boolean isPlaying() {
		return playing;
	}

	public SongPlayer setPlaying(boolean playing) {
		return setPlaying(playing, (Fade) null);
	}

	public SongPlayer setPlaying(boolean playing, boolean fade) {
		return setPlaying(playing, fade ? (playing ? fadeIn : fadeOut) : null);
	}

	public SongPlayer setPlaying(boolean playing, Fade fade) {
		if (this.playing == playing) {
			return this;
		}

		this.playing = playing;
		if (fade != null && fade.getType() != FadeType.NONE) {
			fadeTemp = new Fade(fade.getType(), fade.getFadeDuration());
			fadeTemp.setFadeStart(playing ? 0 : volume);
			fadeTemp.setFadeTarget(playing ? volume : 0);
			fading = true;
		} else {
			fading = false;
			fadeTemp = null;
			volume = fadeIn.getFadeTarget();
			if (!playing) {
				SongStoppedEvent event = new SongStoppedEvent(this);
				NoteBlockAPI.getAPI().doSync(() -> NoteBlockAPI.getAPI().callEvent(event));
			}
		}
		return this;
	}

	public short getTick() {
		return tick;
	}

	public void setTick(short tick) {
		this.tick = tick;
	}

	public void removePlayer(Player player) {
		removePlayer(player.getUuid());
	}

	public void removePlayer(UUID uuid) {
		lock.lock();
		try {
			playerList.remove(uuid);
			ArrayList<SongPlayer> songs = NoteBlockAPI.getSongPlayersByPlayer(uuid);
			if (songs == null) {
				return;
			}
			ArrayList<SongPlayer> copy = new ArrayList<>(songs);
			copy.remove(this);
			NoteBlockAPI.registerSongPlayer(uuid, copy);
			if (playerList.isEmpty() && autoDestroy) {
				SongEndEvent event = new SongEndEvent(this);
				NoteBlockAPI.getAPI().doSync(() -> NoteBlockAPI.getAPI().callEvent(event));
				destroy();
			}
		} finally {
			lock.unlock();
		}
	}

	public byte getVolume() {
		return volume;
	}

	public void setVolume(byte volume) {
		if (volume > 100) {
			volume = 100;
		} else if (volume < 0) {
			volume = 0;
		}
		this.volume = volume;

		fadeIn.setFadeTarget(volume);
		fadeOut.setFadeStart(volume);
		if (fadeTemp != null) {
			if (playing) {
				fadeTemp.setFadeTarget(volume);
			} else {
				fadeTemp.setFadeStart(volume);
			}
		}
	}

	public Song getSong() {
		return song;
	}

	public Playlist getPlaylist() {
		return playlist;
	}

	public void setPlaylist(Playlist playlist) {
		this.playlist = playlist;
	}

	public int getPlayedSongIndex() {
		return actualSong;
	}

	public void playSong(int index) {
		lock.lock();
		try {
			if (playlist.exist(index)) {
				song = playlist.get(index);
				actualSong = index;
				tick = -1;
				fadeIn.setFadeDone(0);
				fadeOut.setFadeDone(0);
			}
		} finally {
			lock.unlock();
		}
	}

	public void playNextSong() {
		lock.lock();
		try {
			tick = song.getLength();
		} finally {
			lock.unlock();
		}
	}

	public void setRepeatMode(RepeatMode repeatMode) {
		this.repeat = repeatMode;
	}

	public RepeatMode getRepeatMode() {
		return repeat;
	}

	public void setRandom(boolean random) {
		this.random = random;
	}

	public boolean isRandom() {
		return random;
	}
}
