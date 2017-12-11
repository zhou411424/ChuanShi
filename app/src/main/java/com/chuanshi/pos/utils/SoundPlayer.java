package com.chuanshi.pos.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zhouliancheng on 2017/12/9.
 * 播放较短音效片段，支持多个声音同时播放
 */
public class SoundPlayer {
    public static final String TAG = "SoundPlayer";
    private SoundPool mSoundPool;
    private Context mContext;
    private static final int MAXSTREAMS = 3;
    private int mCurSoundId;
    private boolean mIsLoaded = false;
    private float mVolume;
    private AudioManager mAudioManager;
    private float mMaxVolume;
    private int mCurLoadSoundId;
    private Map<Integer, Integer> mMap = new HashMap<>();

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SoundPlayer(Context context) {
        mContext = context;
        preload();
    }

    /**
     * 预加载
     */
    public void preload() {
        Log.d(TAG, "preload...");
        // 先释放资源
        if (mSoundPool != null) {
            mSoundPool.stop(mCurSoundId);
            mSoundPool.release();
            mSoundPool = null;
            mIsLoaded = false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// sdk>=21
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            mSoundPool = new SoundPool.Builder().setMaxStreams(MAXSTREAMS).setAudioAttributes(audioAttributes).build();
        } else {
            mSoundPool = new SoundPool(MAXSTREAMS, AudioManager.STREAM_MUSIC, 0);
        }

        // 调整当前音量
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.d(TAG, "onLoadComplete==>sampleId=" + sampleId + ", status=" + status);
                /**
                 * 如果SoundPool刚调完加载资源load函数之后，直接调用play函数可能出现error "sample 1 not READY"
                 * 所以在这个监听到资源加载结束之后，播放音频文件
                 */
                if (status == 0) {
                    float curStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    mVolume = curStreamVolume / mMaxVolume;// 0 < mVolume < 1.0f
                    Log.d(TAG, "play the sound, the soundId:" + sampleId + "the mVolume:" + mVolume);
                    if(mSoundPool != null) {
                        mCurSoundId = mSoundPool.play(sampleId, mVolume, mVolume, 0, 0, 1.0f);
                    }
//                    mMap.put(mCurLoadSoundId, sampleId);

//                    mCurSoundId = soundPool.play(sampleId, mVolume, mVolume, 0, 0, 1.0f);
                }
            }
        });

        mIsLoaded = true;
    }

    /**
     * 重新加载
     */
    public void reload() {
        Log.d(TAG, "reload...mIsLoaded="+mIsLoaded);
        if (!mIsLoaded) {
            preload();
        }
    }

    /**
     * 播放声音
     */
    public void playSound(int soundId) {
        Observable.just("").observeOn(Schedulers.newThread()).doOnNext(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.d(TAG, "thread:"+ Thread.currentThread().getName());
                if (mSoundPool != null) {
                    reload();
                }
            }
        }).subscribe();
    }

    /**
     * 播放声音完毕需停掉，否则下次无法播放声音
     */
    public void stopSound() {
        if (mSoundPool != null) {
            mSoundPool.stop(mCurSoundId);
            mIsLoaded = false;
        }
    }

    /**
     * 释放音效资源
     */
    public void release() {
        if (mSoundPool != null) {
            mSoundPool.release();
            mSoundPool = null;
            mIsLoaded = false;
        }
    }
}
