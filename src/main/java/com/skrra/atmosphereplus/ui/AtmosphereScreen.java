package com.skrra.atmosphereplus.ui;

import com.skrra.atmosphereplus.client.AtmospherePlusClient;
import com.skrra.atmosphereplus.config.ConfigManager;
import com.skrra.atmosphereplus.keybind.AtmosphereKeybinds;
import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;
import com.skrra.atmosphereplus.ui.widgets.AtmosphereWidget;
import com.skrra.atmosphereplus.ui.widgets.CategoryButton;
import com.skrra.atmosphereplus.ui.widgets.SliderWidget;
import com.skrra.atmosphereplus.ui.widgets.ToggleWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AtmosphereScreen extends Screen {
    private final List<AtmosphereWidget> widgets = new ArrayList<>();

    private UiCategory selected = UiCategory.HOME;
    private String searchQuery = "";
    private boolean searchFocused = false;
    private int searchResultCount = 0;
    private int searchOverflowCount = 0;

    private int windowX;
    private int windowY;
    private int windowW;
    private int windowH;

    private int searchX;
    private int searchY;
    private int searchW;
    private int searchH;

    private int closeX;
    private int closeY;
    private int closeSize;

    public AtmosphereScreen() {
        super(Text.literal("Atmosphere+"));
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        widgets.clear();
        searchResultCount = 0;
        searchOverflowCount = 0;

        windowW = Math.min(920, width - 24);
        windowH = Math.min(520, height - 24);
        windowX = (width - windowW) / 2;
        windowY = (height - windowH) / 2;

        searchW = Math.min(260, windowW / 3);
        searchH = 20;
        searchX = windowX + windowW / 2 - searchW / 2;
        searchY = windowY + 14;

        closeSize = 20;
        closeX = windowX + windowW - 36;
        closeY = windowY + 14;

        int sidebarX = windowX + 12;
        int sidebarY = windowY + 106;
        int sidebarW = 174;

        int i = 0;
        for (UiCategory category : visibleCategories()) {
            widgets.add(new CategoryButton(sidebarX, sidebarY + i * 29, sidebarW, category, () -> selected, c -> {
                selected = c;
                searchFocused = false;
                rebuildWidgets();
            }));
            i++;
        }

        int contentX = windowX + 224;
        int contentY = windowY + 116;
        int contentW = windowW - 260;

        if (isSearching()) {
            addSearchResultWidgets(contentX, contentY, contentW);
            return;
        }

        switch (selected) {
            case WEATHER -> {
                widgets.add(new ToggleWidget(contentX, contentY, contentW, "Override server weather visually", () -> ConfigManager.get().weatherOverride, v -> {
                    ConfigManager.get().weatherOverride = v;
                    ConfigManager.save();
                }));

                widgets.add(new SliderWidget(contentX, contentY + 50, contentW, "Rain intensity", 0f, 1f, () -> ConfigManager.get().rainIntensity, v -> {
                    ConfigManager.get().rainIntensity = v;
                    ConfigManager.save();
                }));

                widgets.add(new ToggleWidget(contentX, contentY + 108, contentW, "Thunder sounds", () -> ConfigManager.get().thunderSounds, v -> {
                    ConfigManager.get().thunderSounds = v;
                    ConfigManager.save();
                }));
            }

            case TIME -> {
                widgets.add(new ToggleWidget(contentX, contentY, contentW, "Override visual time", () -> ConfigManager.get().timeOverride, v -> {
                    ConfigManager.get().timeOverride = v;
                    ConfigManager.save();
                }));

                widgets.add(new ToggleWidget(contentX, contentY + 50, contentW, "Freeze visual time", () -> ConfigManager.get().freezeVisualTime, v -> {
                    ConfigManager.get().freezeVisualTime = v;
                    ConfigManager.save();
                }));
            }

            case LIGHTING -> {
                widgets.add(new ToggleWidget(contentX, contentY, contentW, "Fullbright", () -> ConfigManager.get().fullbright, v -> {
                    ConfigManager.get().fullbright = v;
                    ConfigManager.save();
                }));

                widgets.add(new SliderWidget(contentX, contentY + 50, contentW, "Gamma", 0f, 2f, () -> ConfigManager.get().gamma, v -> {
                    ConfigManager.get().gamma = v;
                    ConfigManager.save();
                }));
            }

            case FOG -> widgets.add(new SliderWidget(contentX, contentY, contentW, "Fog distance", 0f, 2f, () -> ConfigManager.get().fogDistance, v -> {
                ConfigManager.get().fogDistance = v;
                ConfigManager.save();
            }));

            case PARTICLES -> widgets.add(new SliderWidget(contentX, contentY, contentW, "Particle amount", 0f, 2f, () -> ConfigManager.get().particleAmount, v -> {
                ConfigManager.get().particleAmount = v;
                ConfigManager.save();
            }));

            case THEMES -> {
                int themeW = (contentW - 10) / 2;
                int index = 0;

                for (String themeId : ThemeManager.all().keySet()) {
                    int col = index % 2;
                    int row = index / 2;
                    int x = contentX + col * (themeW + 10);
                    int y = contentY + row * 44;
                    String label = ThemeManager.all().get(themeId).displayName();

                    widgets.add(new ToggleWidget(x, y, themeW, label, () -> ConfigManager.get().theme.equals(themeId), v -> {
                        if (v) {
                            ThemeManager.setTheme(themeId);
                        }
                    }));

                    index++;
                }
            }

            default -> {
            }
        }
    }

    private void addSearchResultWidgets(int contentX, int contentY, int contentW) {
        int y = contentY;
        int rowGap = 50;
        int maxBottom = windowY + windowH - 26;

        y = addSearchToggle(y, maxBottom, rowGap, contentX, contentW,
                "Weather · Override server weather visually",
                "weather override server visual rain sunny thunder atmosphere",
                () -> ConfigManager.get().weatherOverride,
                v -> {
                    ConfigManager.get().weatherOverride = v;
                    ConfigManager.save();
                });

        y = addSearchSlider(y, maxBottom, rowGap + 8, contentX, contentW,
                "Weather · Rain intensity",
                "weather rain intensity strength opacity storm water",
                0f,
                1f,
                () -> ConfigManager.get().rainIntensity,
                v -> {
                    ConfigManager.get().rainIntensity = v;
                    ConfigManager.save();
                });

        y = addSearchToggle(y, maxBottom, rowGap, contentX, contentW,
                "Weather · Thunder sounds",
                "weather thunder sound lightning storm audio",
                () -> ConfigManager.get().thunderSounds,
                v -> {
                    ConfigManager.get().thunderSounds = v;
                    ConfigManager.save();
                });

        y = addSearchToggle(y, maxBottom, rowGap, contentX, contentW,
                "Time · Override visual time",
                "time day night visual override sunrise sunset noon midnight",
                () -> ConfigManager.get().timeOverride,
                v -> {
                    ConfigManager.get().timeOverride = v;
                    ConfigManager.save();
                });

        y = addSearchToggle(y, maxBottom, rowGap, contentX, contentW,
                "Time · Freeze visual time",
                "time freeze pause stop day night cycle visual",
                () -> ConfigManager.get().freezeVisualTime,
                v -> {
                    ConfigManager.get().freezeVisualTime = v;
                    ConfigManager.save();
                });

        y = addSearchToggle(y, maxBottom, rowGap, contentX, contentW,
                "Lighting · Fullbright",
                "lighting fullbright brightness cave dark gamma light",
                () -> ConfigManager.get().fullbright,
                v -> {
                    ConfigManager.get().fullbright = v;
                    ConfigManager.save();
                });

        y = addSearchSlider(y, maxBottom, rowGap + 8, contentX, contentW,
                "Lighting · Gamma",
                "lighting gamma brightness exposure cave dark light",
                0f,
                2f,
                () -> ConfigManager.get().gamma,
                v -> {
                    ConfigManager.get().gamma = v;
                    ConfigManager.save();
                });

        y = addSearchSlider(y, maxBottom, rowGap + 8, contentX, contentW,
                "Fog · Fog distance",
                "fog distance density view mist haze nether end water lava",
                0f,
                2f,
                () -> ConfigManager.get().fogDistance,
                v -> {
                    ConfigManager.get().fogDistance = v;
                    ConfigManager.save();
                });

        y = addSearchSlider(y, maxBottom, rowGap + 8, contentX, contentW,
                "Particles · Particle amount",
                "particles particle amount rain snow smoke explosion fire bubbles",
                0f,
                2f,
                () -> ConfigManager.get().particleAmount,
                v -> {
                    ConfigManager.get().particleAmount = v;
                    ConfigManager.save();
                });

        for (String themeId : ThemeManager.all().keySet()) {
            String name = ThemeManager.all().get(themeId).displayName();
            y = addSearchToggle(y, maxBottom, rowGap, contentX, contentW,
                    "Theme · " + name,
                    "theme ui color accent dark black purple " + name + " " + themeId,
                    () -> ConfigManager.get().theme.equals(themeId),
                    v -> {
                        if (v) {
                            ThemeManager.setTheme(themeId);
                        }
                    });
        }
    }

    private int addSearchToggle(int y, int maxBottom, int rowGap, int x, int width, String label, String keywords,
                                java.util.function.Supplier<Boolean> getter,
                                java.util.function.Consumer<Boolean> setter) {
        if (!matchesSearch(label, keywords)) {
            return y;
        }

        if (y + 38 > maxBottom) {
            searchOverflowCount++;
            return y;
        }

        widgets.add(new ToggleWidget(x, y, width, label, getter, setter));
        searchResultCount++;
        return y + rowGap;
    }

    private int addSearchSlider(int y, int maxBottom, int rowGap, int x, int width, String label, String keywords,
                                float min,
                                float max,
                                java.util.function.Supplier<Float> getter,
                                java.util.function.Consumer<Float> setter) {
        if (!matchesSearch(label, keywords)) {
            return y;
        }

        if (y + 46 > maxBottom) {
            searchOverflowCount++;
            return y;
        }

        widgets.add(new SliderWidget(x, y, width, label, min, max, getter, setter));
        searchResultCount++;
        return y + rowGap;
    }

    private boolean isSearching() {
        return !searchQuery.trim().isEmpty();
    }

    private boolean matchesSearch(String... values) {
        String query = searchQuery.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return false;
        }

        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            builder.append(value).append(' ');
        }

        String haystack = builder.toString().toLowerCase(Locale.ROOT);

        for (String word : query.split("\\s+")) {
            if (!haystack.contains(word)) {
                return false;
            }
        }

        return true;
    }

    private List<UiCategory> visibleCategories() {
        String query = searchQuery.trim().toLowerCase(Locale.ROOT);
        List<UiCategory> result = new ArrayList<>();

        for (UiCategory category : UiCategory.values()) {
            if (query.isEmpty()
                    || category.title.toLowerCase(Locale.ROOT).contains(query)
                    || category.description.toLowerCase(Locale.ROOT).contains(query)) {
                result.add(category);
            }
        }

        return result;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackdrop(context);

        Theme theme = ThemeManager.current();

        UiRender.borderedRect(context, windowX, windowY, windowW, windowH, theme.background(), theme.border());
        UiRender.gradientHorizontal(context, windowX + 1, windowY + 1, windowW - 2, 48, theme.panel(), theme.panelAlt());
        UiRender.rect(context, windowX + 1, windowY + 48, windowW - 2, 1, theme.border());

        drawBranding(context, theme);
        drawSearchBar(context, theme);
        drawTopButtons(context, theme, mouseX, mouseY);
        drawSidebar(context, theme);
        drawContentHeader(context, theme);
        renderContentBackground(context, theme);

        for (AtmosphereWidget widget : widgets) {
            widget.render(context, textRenderer, mouseX, mouseY, delta);
        }
    }

    private void renderBackdrop(DrawContext context) {
        context.fill(0, 0, this.width, this.height, 0xBB05070D);

        int gridColor = 0x10000000 | 0x00101018;
        for (int x = 0; x < width; x += 32) {
            context.fill(x, 0, x + 1, height, gridColor);
        }
        for (int y = 0; y < height; y += 32) {
            context.fill(0, y, width, y + 1, gridColor);
        }

        for (int i = 0; i < 20; i++) {
            int x = (i * 67 + 31) % Math.max(1, this.width);
            int y = (i * 43 + 19) % Math.max(1, this.height);
            int size = 1 + (i % 3);
            context.fill(x, y, x + size, y + size, 0x224F8BFF);
        }
    }

    private void drawBranding(DrawContext context, Theme theme) {
        int logoX = windowX + 16;
        int logoY = windowY + 13;

        UiRender.borderedRect(context, logoX, logoY, 24, 24, theme.accentSoft(), theme.accent());
        IconRenderer.drawCentered(context, IconType.WEATHER, logoX + 12, logoY + 12, 18);

        UiRender.text(context, textRenderer, AtmospherePlusClient.MOD_NAME, windowX + 48, windowY + 12, theme.text());
        UiRender.text(context, textRenderer, "visual control suite", windowX + 48, windowY + 27, theme.mutedText());
    }

    private void drawSearchBar(DrawContext context, Theme theme) {
        int border = searchFocused ? theme.accent() : theme.border();
        UiRender.borderedRect(context, searchX, searchY, searchW, searchH, theme.panelAlt(), border);

        String displayed = searchQuery.isEmpty() ? "Search settings..." : searchQuery;
        int textColor = searchQuery.isEmpty() ? theme.mutedText() : theme.text();

        UiRender.text(context, textRenderer, displayed, searchX + 9, searchY + 6, textColor);

        if (!searchQuery.isEmpty()) {
            UiRender.text(context, textRenderer, "×", searchX + searchW - 14, searchY + 6, theme.mutedText());
        } else {
            UiRender.text(context, textRenderer, "⌕", searchX + searchW - 16, searchY + 6, theme.mutedText());
        }
    }

    private void drawTopButtons(DrawContext context, Theme theme, int mouseX, int mouseY) {
        int versionW = 112;
        int versionX = closeX - versionW - 8;

        UiRender.borderedRect(context, versionX, closeY, versionW, closeSize, theme.panelAlt(), theme.border());
        UiRender.centeredText(context, textRenderer, AtmospherePlusClient.VERSION, versionX + versionW / 2, closeY + 6, theme.mutedText());

        boolean closeHover = UiRender.hovered(mouseX, mouseY, closeX, closeY, closeSize, closeSize);
        UiRender.borderedRect(context, closeX, closeY, closeSize, closeSize, closeHover ? theme.accentSoft() : theme.panelAlt(), closeHover ? theme.accent() : theme.border());
        UiRender.centeredText(context, textRenderer, "×", closeX + closeSize / 2, closeY + 6, closeHover ? theme.text() : theme.mutedText());
    }

    private void drawSidebar(DrawContext context, Theme theme) {
        int x = windowX + 12;
        int y = windowY + 58;
        int w = 174;
        int h = windowH - 70;

        UiRender.panel(context, x, y, w, h, theme.panel(), theme.border(), theme.accent());

        UiRender.text(context, textRenderer, isSearching() ? "Categories" : "Navigation", x + 14, y + 12, theme.mutedText());
        UiRender.rect(context, x + 14, y + 29, w - 28, 1, theme.border());

        if (visibleCategories().isEmpty()) {
            UiRender.centeredText(context, textRenderer, "No category matches", x + w / 2, y + 64, theme.mutedText());
        }
    }

    private void drawContentHeader(DrawContext context, Theme theme) {
        int x = windowX + 204;
        int y = windowY + 58;
        int w = windowW - 218;

        UiRender.borderedRect(context, x, y, w, 38, theme.panel(), theme.border());

        if (isSearching()) {
            UiRender.borderedRect(context, x + 10, y + 8, 22, 22, theme.accentSoft(), theme.accent());
            IconRenderer.drawCentered(context, IconType.ADVANCED, x + 21, y + 19, 18);
            UiRender.text(context, textRenderer, "Search Results", x + 42, y + 8, theme.text());
            UiRender.text(context, textRenderer, searchResultCount + " direct setting result" + (searchResultCount == 1 ? "" : "s") + " for \"" + searchQuery + "\"", x + 42, y + 22, theme.mutedText());
            return;
        }

        UiRender.borderedRect(context, x + 10, y + 8, 22, 22, theme.accentSoft(), theme.accent());
        IconRenderer.drawCentered(context, selected.icon, x + 21, y + 19, 18);

        UiRender.text(context, textRenderer, selected.title, x + 42, y + 8, theme.text());
        UiRender.text(context, textRenderer, selected.description, x + 42, y + 22, theme.mutedText());
    }

    private void renderContentBackground(DrawContext context, Theme theme) {
        int x = windowX + 204;
        int y = windowY + 104;
        int w = windowW - 218;
        int h = windowH - 116;

        UiRender.panel(context, x, y, w, h, theme.panel(), theme.border(), theme.accent());

        if (isSearching()) {
            if (searchResultCount == 0) {
                UiRender.centeredText(context, textRenderer, "No direct settings found", x + w / 2, y + 112, theme.text());
                UiRender.centeredText(context, textRenderer, "Try searching for weather, fog distance, gamma, fullbright, particles, or a theme name.", x + w / 2, y + 134, theme.mutedText());
            } else if (searchOverflowCount > 0) {
                UiRender.centeredText(context, textRenderer, "+" + searchOverflowCount + " more result" + (searchOverflowCount == 1 ? "" : "s") + " hidden. Refine your search.", x + w / 2, y + h - 28, theme.mutedText());
            }
            return;
        }

        if (selected == UiCategory.HOME) {
            renderHome(context, theme, x, y, w, h);
        } else if (selected == UiCategory.PRESETS) {
            renderPresetPlaceholder(context, theme, x, y, w, h);
        } else if (selected == UiCategory.SKY || selected == UiCategory.ADVANCED) {
            renderComingSoon(context, theme, x, y, w, h);
        }
    }

    private void renderHome(DrawContext context, Theme theme, int x, int y, int w, int h) {
        UiRender.centeredText(context, textRenderer, "Welcome to Atmosphere+", x + w / 2, y + 48, theme.text());
        UiRender.centeredText(context, textRenderer, "A client-side atmosphere and visual customization suite.", x + w / 2, y + 68, theme.mutedText());

        int cardW = (w - 54) / 3;
        int cardY = y + 112;

        drawMiniCard(context, theme, x + 16, cardY, cardW, "Weather", "Override visuals", IconType.WEATHER);
        drawMiniCard(context, theme, x + 27 + cardW, cardY, cardW, "Time", "Control day/night", IconType.TIME);
        drawMiniCard(context, theme, x + 38 + cardW * 2, cardY, cardW, "Themes", "Style the UI", IconType.THEMES);

        UiRender.centeredText(context, textRenderer, "Tip: search for a setting like fog distance and edit it instantly.", x + w / 2, y + h - 58, theme.mutedText());
        UiRender.centeredText(context, textRenderer, "Pick a category on the left to start tuning your world.", x + w / 2, y + h - 40, theme.accent());
    }

    private void drawMiniCard(DrawContext context, Theme theme, int x, int y, int w, String title, String description, IconType icon) {
        UiRender.card(context, x, y, w, 76, theme.panelAlt(), theme.border());
        UiRender.borderedRect(context, x + 12, y + 12, 22, 22, theme.accentSoft(), theme.accent());
        IconRenderer.drawCentered(context, icon, x + 23, y + 23, 18);
        UiRender.text(context, textRenderer, title, x + 44, y + 13, theme.text());
        UiRender.text(context, textRenderer, description, x + 44, y + 28, theme.mutedText());
        UiRender.rect(context, x + 12, y + 56, w - 24, 3, theme.accentSoft());
        UiRender.rect(context, x + 12, y + 56, (w - 24) / 2, 3, theme.accent());
    }

    private void renderPresetPlaceholder(DrawContext context, Theme theme, int x, int y, int w, int h) {
        int cardW = (w - 54) / 3;
        drawMiniCard(context, theme, x + 18, y + 24, cardW, "Cozy", "Sunset + soft rain", IconType.WEATHER);
        drawMiniCard(context, theme, x + 27 + cardW, y + 24, cardW, "Fantasy", "Stars + glow", IconType.SKY);
        drawMiniCard(context, theme, x + 38 + cardW * 2, y + 24, cardW, "Horror", "Fog + thunder", IconType.FOG);
        UiRender.centeredText(context, textRenderer, "Preset behavior will be added after weather/time systems.", x + w / 2, y + 138, theme.mutedText());
    }

    private void renderComingSoon(DrawContext context, Theme theme, int x, int y, int w, int h) {
        UiRender.borderedRect(context, x + w / 2 - 18, y + 58, 36, 36, theme.accentSoft(), theme.accent());
        IconRenderer.drawCentered(context, selected.icon, x + w / 2, y + 76, 22);
        UiRender.centeredText(context, textRenderer, "Coming soon", x + w / 2, y + 112, theme.text());
        UiRender.centeredText(context, textRenderer, "This category is ready for the next milestone.", x + w / 2, y + 132, theme.mutedText());
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (AtmosphereKeybinds.matchesOpenMenuMouse(click)) {
            MinecraftClient.getInstance().setScreen(null);
            return true;
        }

        if (UiRender.hovered(click.x(), click.y(), closeX, closeY, closeSize, closeSize)) {
            MinecraftClient.getInstance().setScreen(null);
            return true;
        }

        if (UiRender.hovered(click.x(), click.y(), searchX, searchY, searchW, searchH)) {
            searchFocused = true;

            if (!searchQuery.isEmpty() && click.x() >= searchX + searchW - 22) {
                searchQuery = "";
                rebuildWidgets();
            }

            return true;
        } else {
            searchFocused = false;
        }

        for (AtmosphereWidget widget : widgets) {
            if (widget.mouseClicked(click.x(), click.y(), click.button())) {
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        for (AtmosphereWidget widget : widgets) {
            if (widget.mouseDragged(click.x(), click.y(), click.button(), offsetX, offsetY)) {
                return true;
            }
        }

        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        for (AtmosphereWidget widget : widgets) {
            if (widget.mouseReleased(click.x(), click.y(), click.button())) {
                return true;
            }
        }

        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (AtmosphereKeybinds.matchesOpenMenu(input)) {
            MinecraftClient.getInstance().setScreen(null);
            return true;
        }

        if (searchFocused) {
            if (input.isEscape()) {
                searchFocused = false;
                return true;
            }

            if (input.getKeycode() == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                rebuildWidgets();
                return true;
            }

            if (input.getKeycode() == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (searchFocused && input.isValidChar() && searchQuery.length() < 32) {
            searchQuery += input.asString();
            rebuildWidgets();
            return true;
        }

        return super.charTyped(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
