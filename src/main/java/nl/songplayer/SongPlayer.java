package nl.songplayer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class SongPlayer implements ClientModInitializer {
    private static SoundInstance currentSound;
    private static Identifier currentTrack;
    private static float volume = 1.0f;
    private static boolean customPlaying = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (customPlaying) client.getMusicManager().stopPlaying();
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(literal("music").executes(ctx -> {
                Minecraft.getInstance().execute(() -> Minecraft.getInstance().gui.setScreen(new MusicScreen()));
                return 1;
            }))
        );
    }

    public static List<Identifier> tracks() {
        List<Identifier> tracks = new ArrayList<>();
        for (SoundEvent sound : BuiltInRegistries.SOUND_EVENT) {
            Identifier id = sound.location();
            String p = id.getPath();
            if (id.getNamespace().equals("minecraft") &&
                (p.startsWith("music.") || p.startsWith("music_disc.") || p.startsWith("record."))) {
                tracks.add(id);
            }
        }
        tracks.sort(Comparator.comparing(Identifier::toString));
        return tracks;
    }

    public static void play(Identifier id) {
        stop();
        Minecraft.getInstance().getMusicManager().stopPlaying();
        currentTrack = id;
        customPlaying = true;
        currentSound = new SimpleSoundInstance(id, SoundSource.MUSIC, volume, 1f,
            net.minecraft.util.RandomSource.create(), false, 0,
            SoundInstance.Attenuation.NONE, 0, 0, 0, true);
        Minecraft.getInstance().getSoundManager().play(currentSound);
    }

    public static void stop() {
        if (currentSound != null) Minecraft.getInstance().getSoundManager().stop(currentSound);
        currentSound = null;
        currentTrack = null;
        customPlaying = false;
    }
    public static Identifier current() { return currentTrack; }
    public static float volume() { return volume; }
    public static void setVolume(float v) {
        volume = Math.max(0f, Math.min(1f, v));
        if (currentTrack != null) play(currentTrack);
    }
}
