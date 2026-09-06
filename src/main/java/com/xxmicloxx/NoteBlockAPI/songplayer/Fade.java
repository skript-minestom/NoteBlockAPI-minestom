package com.xxmicloxx.NoteBlockAPI.songplayer;

import com.xxmicloxx.NoteBlockAPI.model.FadeType;
import com.xxmicloxx.NoteBlockAPI.utils.Interpolator;

public class Fade {

	private FadeType type;
	private byte fadeStart;
	private byte fadeTarget;
	private int fadeDuration;
	private int fadeDone = 0;

	/**
	 * Create new fade effect
	 * @param type Type of fade effect
	 * @param fadeDuration duration of fade effect in ticks
	 */
	public Fade(FadeType type, int fadeDuration) {
		this.type = type;
		this.fadeDuration = fadeDuration;
	}

	protected byte calculateFade() {
		switch (type) {
			case LINEAR:
				if (fadeDone == fadeDuration) {
					return -1;
				}
				double targetVolume = Interpolator.interpLinear(
						new double[]{0, fadeStart, fadeDuration, fadeTarget}, fadeDone);
				fadeDone++;
				return (byte) targetVolume;
			default:
				fadeDone++;
				return -1;
		}
	}

	protected int getFadeDone() {
		return fadeDone;
	}

	protected void setFadeStart(byte fadeStart) {
		this.fadeStart = fadeStart;
	}

	protected void setFadeTarget(byte fadeTarget) {
		this.fadeTarget = fadeTarget;
	}

	public FadeType getType() {
		return type;
	}

	public void setType(FadeType type) {
		this.type = type;
	}

	public int getFadeDuration() {
		return fadeDuration;
	}

	public void setFadeDuration(int fadeDuration) {
		this.fadeDuration = fadeDuration;
	}

	protected byte getFadeStart() {
		return fadeStart;
	}

	protected byte getFadeTarget() {
		return fadeTarget;
	}

	protected void setFadeDone(int fadeDone) {
		this.fadeDone = fadeDone;
	}

	public boolean isDone() {
		return fadeDone >= fadeDuration;
	}
}
