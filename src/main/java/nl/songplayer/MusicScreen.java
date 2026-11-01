package nl.songplayer;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import java.util.List;

public final class MusicScreen extends Screen {
    private String filter = "";
    private int offset = 0;
    private EditBox search;

    public MusicScreen() { super(Component.literal("Song Player")); }

    private List<Identifier> filtered() {
        return SongPlayer.tracks().stream()
            .filter(id -> id.toString().toLowerCase().contains(filter.toLowerCase()))
            .toList();
    }

    @Override
    protected void init() {
        search = new EditBox(font, width / 2 - 150, 24, 300, 20, Component.literal("Search music"));
        search.setHint(Component.literal("Search Minecraft music..."));
        search.setValue(filter);
        search.setResponder(text -> {
            if (!filter.equals(text)) {
                filter = text;
                offset = 0;
                rebuildWidgets();
            }
        });
        addRenderableWidget(search);
        List<Identifier> tracks = filtered();
        int rows = Math.max(1, (height - 125) / 24);
        offset = Math.max(0, Math.min(offset, Math.max(0, tracks.size() - rows)));
        for (int i = 0; i < rows && i + offset < tracks.size(); i++) {
            Identifier id = tracks.get(i + offset);
            String label = id.getPath();
            if (label.length() > 43) label = label.substring(0, 40) + "...";
            addRenderableWidget(Button.builder(Component.literal(label), b -> SongPlayer.play(id))
                .bounds(width / 2 - 150, 52 + i * 24, 300, 20).build());
        }
        int bottom = height - 48;
        addRenderableWidget(Button.builder(Component.literal("Previous"), b -> { offset = Math.max(0, offset - rows); rebuildWidgets(); })
            .bounds(width / 2 - 150, bottom, 95, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Stop"), b -> SongPlayer.stop())
            .bounds(width / 2 - 48, bottom, 95, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Next"), b -> { offset += rows; rebuildWidgets(); })
            .bounds(width / 2 + 55, bottom, 95, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Vol -"), b -> SongPlayer.setVolume(SongPlayer.volume() - 0.1f))
            .bounds(width / 2 - 100, height - 24, 95, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Vol +"), b -> SongPlayer.setVolume(SongPlayer.volume() + 0.1f))
            .bounds(width / 2 + 5, height - 24, 95, 20).build());
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
