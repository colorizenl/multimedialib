//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.stage.Audio;
import nl.colorize.util.ResourceFile;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Plays OGG audio files using LWJGL, which uses OpenAL for audio playback
 * with hardware acceleration.
 * <p>
 * The LWJGL openAL subsystem is automatically initialized when the first
 * audio clip is played. This subsystem is typically terminated by the
 * renderer, although it is also possible to do this manually from
 * application code by calling {@link #terminate()}.
 * <p>
 * Although the Java2D renderer generally intended to only rely on the Java
 * standard library, this is simply not possible when it comes to
 * cross-platform support for playing OGG audio, hence the dependency on
 * LWJGL.
 */
public class LWJGLAudio implements Audio {

    private ResourceFile origin;
    private byte[] oggData;
    @Getter private int masterVolume;
    @Getter private float duration;

    private int bufferId;
    private int sourceId;

    private static AudioSubSystem audioSubSystem;

    public LWJGLAudio(ResourceFile origin) {
        this.origin = origin;
        this.oggData = origin.readBytes();
        this.masterVolume = 100;
        this.duration = 0f;

        this.bufferId = -1;
        this.sourceId = -1;
    }

    private void prepareBuffer() {
        Preconditions.checkState(audioSubSystem != null, "LWJGL OpenAL subsystem not initialized");

        if (sourceId == -1) {
            ByteBuffer buffer = MemoryUtil.memAlloc(oggData.length);
            buffer.put(oggData).flip();

            ShortBuffer pcm;
            int channels;
            int sampleRate;

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer channelBuffer = stack.mallocInt(1);
                IntBuffer sampleRateBuffer = stack.mallocInt(1);
                pcm = STBVorbis.stb_vorbis_decode_memory(buffer, channelBuffer, sampleRateBuffer);
                channels = channelBuffer.get(0);
                sampleRate = sampleRateBuffer.get(0);
            }

            bufferId = AL10.alGenBuffers();
            sourceId = AL10.alGenSources();
            duration = (pcm.limit() / (float) channels) / (float) sampleRate;

            AL10.alBufferData(bufferId, getFormat(channels), pcm, sampleRate);
            AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufferId);
        }
    }

    private int getFormat(int channels) {
        return channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
    }

    @Override
    public void play(boolean loop) {
        initializeAudioSubSystem();
        prepareBuffer();

        stop();
        AL10.alSourcei(sourceId, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
        AL10.alSourcef(sourceId, AL10.AL_GAIN, masterVolume / 100f);
        AL10.alSourcePlay(sourceId);
    }

    @Override
    public void stop() {
        initializeAudioSubSystem();
        prepareBuffer();

        AL10.alSourceStop(sourceId);
    }

    @Override
    public void changeVolume(int volume) {
        initializeAudioSubSystem();
        prepareBuffer();

        masterVolume = Math.clamp(volume, 0, 100);
    }

    @Override
    public String toString() {
        return origin.toString();
    }

    /**
     * Initializes the LWJGL OpenAL subsystem. This is done automatically the
     * first time an audio clip is played.
     */
    private static void initializeAudioSubSystem() {
        if (audioSubSystem == null) {
            long deviceId = ALC10.alcOpenDevice((ByteBuffer) null);
            long contextId = ALC10.alcCreateContext(deviceId, (IntBuffer) null);
            ALC10.alcMakeContextCurrent(contextId);
            AL.createCapabilities(ALC.createCapabilities(deviceId));
            audioSubSystem = new AudioSubSystem(deviceId, contextId);
        }
    }

    /**
     * Terminates the OpenAL subsystem. After calling this method, it is no
     * longer possible to play audio clips using LWJGL. This method is
     * normally called by the renderer when it is terminated.
     */
    public static void terminate() {
        if (audioSubSystem != null) {
            ALC10.alcDestroyContext(audioSubSystem.contextId);
            ALC10.alcCloseDevice(audioSubSystem.deviceId);
            audioSubSystem = null;
        }
    }

    /**
     * Represents the LWJGL OpenAL subsystem. This does not (normally) need
     * to be managed by application code, it is initialized and terminated
     * automatically based on the renderer.
     */
    private record AudioSubSystem(long deviceId, long contextId) {
    }
}
