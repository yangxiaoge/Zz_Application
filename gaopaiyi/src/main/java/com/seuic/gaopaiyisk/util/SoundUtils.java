package com.seuic.gaopaiyisk.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseIntArray;

import com.seuic.gaopaiyisk.R;

import java.security.InvalidParameterException;

public class SoundUtils {

	public static final int START_SOUND_ID = 0;
	public static final int START_SOUND_ID_RAINBOW = 1;
	private AudioManager mAudioManager;
	private Context context;
	private SoundPool mSoundPool;
	private SparseIntArray mSoundPoolMap;

	public SoundUtils(Context ctx) {
		this.context = ctx;
		this.mSoundPoolMap = new SparseIntArray();
		this.mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		this.mAudioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
	}

	public void init() {
		//this.mSoundPoolMap.put(Integer.valueOf(START_SOUND_ID), Integer.valueOf(this.mSoundPool.load(this.context, R.raw.scan, 1)));
		this.mSoundPoolMap.put(Integer.valueOf(START_SOUND_ID_RAINBOW), Integer.valueOf(this.mSoundPool.load(this.context, R.raw.scan_success, 1)));
	}

	public void playSound(int audioType, int rate) {
		if (audioType != START_SOUND_ID && audioType != START_SOUND_ID_RAINBOW) {
			throw new InvalidParameterException("audioType");
		}

		float vol = (this.mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 1.0f) / this.mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		Integer soundId = this.mSoundPoolMap.get(Integer.valueOf(audioType));

		if ((this.mSoundPool != null) && (this.mSoundPoolMap != null) && (this.mSoundPoolMap.size() > 0)) {
			this.mSoundPool.play(soundId, vol, vol, 1, 0, rate);
		}
	}

	public void release() {
		this.mSoundPool.release();
	}

}
