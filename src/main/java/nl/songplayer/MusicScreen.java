package nl.songplayer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import java.util.List;

public final class MusicScreen extends Screen {
    private String filter = "";
    private int offset = 0;
    private List<Identifier> all;

    public MusicScreen() { super(Component.literal("Song Player")); }

    @Override
    protected void init() {
        all = SongPlayer.tracks();
        EditBox search = new EditBox(font, width / 2 - 150, 42, 300, 20, Component.literal("Search tracks"));
        search.setHint(Component.literal("Search Minecraft songs..."));
        search.setValue(filter);
        search.setResponder(text -> { filter = text; offset = 0; });
        addRenderableWidget(search);
        addRenderableWidget(Button.builder(Component.literal("Stop"), b -> SongPlayer.stop())
            .bounds(width / 2 - 150, height - 38, 95, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Volume -"), b -> SongPlayer.setVolume(SongPlayer.volume() - 0.1f))
            .bounds(width / 2 - 48, height - 38, 95, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Volume +"), b -> SongPlayer.setVolume(SongPlayer.volume() + 0.1f))
            .bounds(width / 2 + 55, height - 38, 95, 20).build());
    }

    private List<Identifier> filtered() {
        return all.stream().filter(id -> id.toString().toLowerCase().contains(filter.toLowerCase())).toList();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 17, 0xFFFFFF);
        List<Identifier> tracks = filtered();
        int rows = Math.max(1, (height - 125) / 23);
        offset = Math.max(0, Math.min(offset, Math.max(0, tracks.size() - rows)));
        int x = width / 2 - 150;
        for (int i = 0; i < rows && i + offset < tracks.size(); i++) {
            Identifier id = tracks.get(i + offset);
            int y = 70 + i * 23;
            boolean hover = mouseX >= x && mouseX < x + 300 && mouseY >= y && mouseY < y + 21;
            int bg = id.equals(SongPlayer.current()) ? 0xAA176B65 : hover ? 0xAA45546A : 0xAA222936;
            graphics.fill(x, y, x + 300, y + 21, bg);
            graphics.drawString(font, id.getPath(), x + 8, y + 6, 0xFFFFFF, false);
        }
        graphics.drawCenteredString(font, "Tracks: " + tracks.size() + "  |  Volume: " + Math.round(SongPlayer.volume() * 100) + "%", width / 2, height - 56, 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        int x = width / 2 - 150;
        int rows = Math.max(1, (height - 125) / 23);
        if (button == 0 && mouseX >= x && mouseX < x + 300 && mouseY >= 70 && mouseY < 70 + rows * 23) {
            int i = ((int) mouseY - 70) / 23 + offset;
            List<Identifier> tracks = filtered();
            if (i >= 0 && i < tracks.size()) { SongPlayer.play(tracks.get(i)); return true; }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(net.minecraft.client.input.MouseScrollEvent event) {
        double vertical = event.verticalScroll();
        offset = Math.max(0, offset - (int) Math.signum(vertical) * 3);
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
