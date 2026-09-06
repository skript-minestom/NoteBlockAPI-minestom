package com.xxmicloxx.NoteBlockAPI.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Playlist {

	ArrayList<Song> songs = new ArrayList<>();

	public Playlist(Song... songs) {
		if (songs.length == 0) {
			throw new IllegalArgumentException("Cannot create empty playlist");
		}
		checkNull(songs);
		this.songs.addAll(Arrays.asList(songs));
	}

	public void add(Song... songs) {
		if (songs.length == 0) {
			return;
		}
		checkNull(songs);
		this.songs.addAll(Arrays.asList(songs));
	}

	public void insert(int index, Song... songs) {
		if (songs.length == 0) {
			return;
		}
		if (index > this.songs.size()) {
			throw new IllegalArgumentException("Index is higher than playlist size");
		}
		checkNull(songs);
		this.songs.addAll(index, Arrays.asList(songs));
	}

	private void checkNull(Song... songs) {
		List<Song> songList = Arrays.asList(songs);
		if (songList.contains(null)) {
			throw new IllegalArgumentException("Cannot add null to playlist");
		}
	}

	public void remove(Song... songs) {
		ArrayList<Song> songsTemp = new ArrayList<>(this.songs);
		songsTemp.removeAll(Arrays.asList(songs));
		if (songsTemp.size() > 0) {
			this.songs = songsTemp;
		} else {
			throw new IllegalArgumentException("Cannot remove all songs from playlist");
		}
	}

	public Song get(int songNumber) {
		return songs.get(songNumber);
	}

	public int getCount() {
		return songs.size();
	}

	public boolean hasNext(int songNumber) {
		return songs.size() > (songNumber + 1);
	}

	public boolean exist(int songNumber) {
		return songs.size() > songNumber;
	}

	public int getIndex(Song song) {
		return songs.indexOf(song);
	}

	public boolean contains(Song song) {
		return songs.contains(song);
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Song> getSongList() {
		return (ArrayList<Song>) songs.clone();
	}
}
