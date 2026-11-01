package nl.songplayer;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MusicScreen extends Screen {
    private static final Map<String, String> COMPOSERS = Map.ofEntries(
        Map.entry("bromeliad", "Aaron Cherof"), Map.entry("crescent_dunes", "Aaron Cherof"),
        Map.entry("echo_in_the_wind", "Aaron Cherof"), Map.entry("a_familiar_room", "Aaron Cherof"),
        Map.entry("featherfall", "Aaron Cherof"), Map.entry("komorebi", "Kumi Tanioka"),
        Map.entry("left_to_bloom", "Lena Raine"), Map.entry("one_more_day", "Lena Raine"),
        Map.entry("infinite_amethyst", "Lena Raine"), Map.entry("ancestry", "Lena Raine"),
        Map.entry("stand_tall", "Lena Raine"), Map.entry("otherside", "Lena Raine"),
        Map.entry("pigstep", "Lena Raine"), Map.entry("creator", "Lena Raine"),
        Map.entry("precipice", "Lena Raine"), Map.entry("deeper", "Lena Raine"),
        Map.entry("rubedo", "Lena Raine"), Map.entry("chrysopoeia", "Lena Raine"),
        Map.entry("so_below", "Lena Raine"), Map.entry("floating_dream", "Kumi Tanioka"),
        Map.entry("comforting_memories", "Kumi Tanioka"), Map.entry("an_ordinary_day", "Kumi Tanioka"),
        Map.entry("subwoofer_lullaby", "C418"), Map.entry("living_mice", "C418"),
        Map.entry("mice_on_venus", "C418"), Map.entry("sweden", "C418"),
        Map.entry("minecraft", "C418"), Map.entry("wet_hands", "C418"),
        Map.entry("dry_hands", "C418"), Map.entry("clark", "C418"),
        Map.entry("haggstrom", "C418"), Map.entry("aria_math", "C418"),
        Map.entry("taswell", "C418"), Map.entry("dreiton", "C418"),
        Map.entry("cat", "C418"), Map.entry("blocks", "C418"),
        Map.entry("chirp", "C418"), Map.entry("far", "C418"),
        Map.entry("mall", "C418"), Map.entry("mellohi", "C418"),
        Map.entry("stal", "C418"), Map.entry("strad", "C418"),
        Map.entry("wait", "C418"), Map.entry("ward", "C418")
    );

    private String query = "";
    private int page = 0;
    private int rows;
    private EditBox search;
    private final List<Button> songButtons = new ArrayList<>();
    private List<Identifier> matches = List.of();
    private Button prev, next, status;

    public MusicScreen() { super(Component.literal("Song Player  •  Minecraft Music")); }

    private static String title(Identifier id) {
        String path = id.getPath();
        String name = path.substring(path.lastIndexOf('.') + 1).replace('_', ' ');
        if (name.isEmpty()) return path;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static String composer(Identifier id) {
        String path = id.getPath();
        String name = path.substring(path.lastIndexOf('.') + 1);
        return COMPOSERS.getOrDefault(name, "Minecraft soundtrack");
    }

    private static String category(Identifier id) {
        return id.getPath().startsWith("music_disc.") ? "Music Disc" : "Background Music";
    }

    private void updateMatches() {
        String q = query.strip().toLowerCase(Locale.ROOT);
        matches = SongPlayer.tracks().stream()
            .filter(id -> q.isEmpty() || (title(id) + " " + composer(id) + " " + category(id) + " " + id)
                .toLowerCase(Locale.ROOT).contains(q))
            .sorted(Comparator.comparing((Identifier id) -> title(id).toLowerCase(Locale.ROOT)))
            .toList();
        page = Math.max(0, Math.min(page, Math.max(0, (matches.size() - 1) / rows)));
        refresh();
    }

    private void refresh() {
        if (songButtons.isEmpty()) return;
        int start = page * rows;
        for (int i = 0; i < songButtons.size(); i++) {
            Button b = songButtons.get(i);
            int index = start + i;
            boolean exists = index < matches.size();
            b.visible = exists;
            b.active = exists;
            if (exists) {
                Identifier id = matches.get(index);
                String label = (id.equals(SongPlayer.current()) ? "▶  " : "♫  ")
                    + title(id) + "  ·  " + composer(id);
                if (label.length() > 51) label = label.substring(0, 48) + "...";
                b.setMessage(Component.literal(label));
            }
        }
        prev.active = page > 0;
        next.active = (page + 1) * rows < matches.size();
        status.setMessage(Component.literal(matches.size() + " songs  ·  Page " + (page + 1)
            + "  ·  Volume " + Math.round(SongPlayer.volume() * 100) + "%"));
    }

    @Override
    protected void init() {
        songButtons.clear();
        rows = Math.max(1, (height - 135) / 23);
        int w = Math.min(430, width - 28);
        int x = (width - w) / 2;
        search = new EditBox(font, x, 24, w, 20, Component.literal("Search songs"));
        search.setHint(Component.literal("Search title, composer (C418, Lena Raine...), or music disc"));
        search.setValue(query);
        search.setResponder(value -> { query = value; page = 0; updateMatches(); });
        addRenderableWidget(search);
        for (int i = 0; i < rows; i++) {
            final int row = i;
            Button b = Button.builder(Component.literal(""), button -> {
                int index = page * rows + row;
                if (index < matches.size()) {
                    SongPlayer.play(matches.get(index));
                    refresh();
                }
            }).bounds(x, 50 + i * 23, w, 20).build();
            songButtons.add(addRenderableWidget(b));
        }
        int bottom = height - 75;
        prev = addRenderableWidget(Button.builder(Component.literal("◀ Previous"), b -> { page--; refresh(); })
            .bounds(x, bottom, 100, 20).build());
        next = addRenderableWidget(Button.builder(Component.literal("Next ▶"), b -> { page++; refresh(); })
            .bounds(x + w - 100, bottom, 100, 20).build());
        status = addRenderableWidget(Button.builder(Component.literal(""), b -> {})
            .bounds(x + 104, bottom, w - 208, 20).build());
        status.active = false;
        addRenderableWidget(Button.builder(Component.literal("■ Stop"), b -> { SongPlayer.stop(); refresh(); })
            .bounds(x, height - 49, w / 3 - 2, 20).build());
        addRenderableWidget(Button.builder(Component.literal("− Volume"), b -> {
            SongPlayer.setVolume(SongPlayer.volume() - 0.1f); refresh();
        }).bounds(x + w / 3 + 1, height - 49, w / 3 - 2, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+ Volume"), b -> {
            SongPlayer.setVolume(SongPlayer.volume() + 0.1f); refresh();
        }).bounds(x + (w * 2) / 3 + 1, height - 49, w / 3 - 2, 20).build());
        updateMatches();
        setFocused(search);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
