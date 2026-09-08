package com.xxmicloxx.NoteBlockAPI.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Maps OpenNBS custom-instrument sound file paths to Minecraft sound event IDs.
 */
public final class SoundKeyResolver {

	private static final String INDEX_RESOURCE = "/sound-file-index.properties";

	private static volatile Map<String, String> index;

	private SoundKeyResolver() {
	}

	/**
	 * Resolve an NBS sound-file string (and optional instrument name) to a playable sound key.
	 *
	 * @param soundFileName sound file field from the NBS custom instrument
	 * @param instrumentName display name from the NBS custom instrument (may be a sound event)
	 * @return sound event id or the stripped original string if unknown
	 */
	public static String resolve(String soundFileName, String instrumentName) {
		String stripped = stripExtension(soundFileName);
		if (stripped == null || stripped.isEmpty()) {
			return fallbackName(instrumentName, "block.note_block.harp");
		}
		if (looksLikeEvent(stripped)) {
			return stripped;
		}

		String path = normalizePath(stripped);
		String event = index().get(path);
		if (event != null) {
			return event;
		}

		String nameFallback = fallbackName(instrumentName, null);
		if (nameFallback != null) {
			return nameFallback;
		}
		return stripped;
	}

	static boolean looksLikeEvent(String value) {
		return value.indexOf('.') >= 0 && value.indexOf('/') < 0 && value.indexOf('\\') < 0;
	}

	static String stripExtension(String value) {
		if (value == null) {
			return null;
		}
		String result = value.trim();
		String lower = result.toLowerCase(Locale.ROOT);
		for (String ext : new String[]{".ogg", ".wav", ".mp3"}) {
			if (lower.endsWith(ext)) {
				result = result.substring(0, result.length() - ext.length());
				lower = result.toLowerCase(Locale.ROOT);
			}
		}
		return result;
	}

	static String normalizePath(String value) {
		String path = value.replace('\\', '/');
		while (path.startsWith("/")) {
			path = path.substring(1);
		}
		if (path.regionMatches(true, 0, "minecraft/", 0, "minecraft/".length())) {
			path = path.substring("minecraft/".length());
		}
		if (path.regionMatches(true, 0, "sounds/", 0, "sounds/".length())) {
			path = path.substring("sounds/".length());
		}
		return path;
	}

	private static String fallbackName(String instrumentName, String defaultValue) {
		String stripped = stripExtension(instrumentName);
		if (stripped != null && !stripped.isEmpty() && looksLikeEvent(stripped)) {
			return stripped;
		}
		return defaultValue;
	}

	private static Map<String, String> index() {
		Map<String, String> local = index;
		if (local == null) {
			synchronized (SoundKeyResolver.class) {
				local = index;
				if (local == null) {
					local = loadIndex();
					index = local;
				}
			}
		}
		return local;
	}

	private static Map<String, String> loadIndex() {
		InputStream in = SoundKeyResolver.class.getResourceAsStream(INDEX_RESOURCE);
		if (in == null) {
			return Collections.emptyMap();
		}
		Map<String, String> map = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				int eq = line.indexOf('=');
				if (eq <= 0) {
					continue;
				}
				map.put(line.substring(0, eq), line.substring(eq + 1));
			}
		} catch (IOException e) {
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(map);
	}
}
