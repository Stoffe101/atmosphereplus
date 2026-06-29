package com.skrra.atmosphereplus.ui;

import com.skrra.atmosphereplus.config.ConfigManager;
import com.skrra.atmosphereplus.themes.CustomThemeData;
import com.skrra.atmosphereplus.themes.CustomThemeManager;
import com.skrra.atmosphereplus.themes.Theme;
import com.skrra.atmosphereplus.themes.ThemeManager;

import java.util.HashMap;
import java.util.Map;

public class ThemeStudioState {
    public enum Token {
        BACKGROUND("background", "Background"),
        PANEL("panel", "Panel"),
        PANEL_ALT("panelAlt", "Panel Alt"),
        BORDER("border", "Border"),
        ACCENT("accent", "Accent"),
        ACCENT_SOFT("accentSoft", "Accent Soft"),
        TEXT("text", "Text"),
        MUTED_TEXT("mutedText", "Muted Text");

        public final String id;
        public final String label;

        Token(String id, String label) {
            this.id = id;
            this.label = label;
        }
    }

    private String selectedThemeId = "";
    private CustomThemeData draft;
    private boolean advancedMode = false;
    private String focusedHexToken = "";
    private final Map<String, String> hexInputs = new HashMap<>();

    public String selectedThemeId() {
        resolveSelectedThemeId();
        ensureDraft();
        return selectedThemeId;
    }

    public void selectTheme(String id) {
        if (id != null && ThemeManager.byId(id) != null && !id.equals(selectedThemeId)) {
            selectedThemeId = id;
            draft = null;
            focusedHexToken = "";
            hexInputs.clear();
        }
    }

    public void selectCurrentTheme() {
        selectTheme(ConfigManager.get().theme);
    }

    public boolean selectedIsCustom() {
        resolveSelectedThemeId();
        return CustomThemeManager.isCustomTheme(selectedThemeId);
    }

    public boolean advancedMode() {
        return advancedMode;
    }

    public void setAdvancedMode(boolean advancedMode) {
        this.advancedMode = advancedMode;
    }

    public CustomThemeData draft() {
        ensureDraft();
        return draft;
    }

    public Theme previewTheme() {
        if (selectedIsCustom()) {
            CustomThemeData data = draft();
            return data == null ? ThemeManager.current() : data.toTheme();
        }

        Theme selected = ThemeManager.byId(selectedThemeId());
        return selected == null ? ThemeManager.current() : selected;
    }

    public boolean dirty() {
        if (!selectedIsCustom()) {
            return false;
        }

        CustomThemeData saved = CustomThemeManager.get(selectedThemeId());
        CustomThemeData current = draft();
        return saved != null && current != null && !same(saved, current);
    }

    public void revert() {
        draft = null;
        focusedHexToken = "";
        hexInputs.clear();
        ensureDraft();
    }

    public void resetTo(Theme theme) {
        if (!selectedIsCustom() || theme == null) {
            return;
        }

        String id = selectedThemeId();
        String name = draft().displayName;
        draft = new CustomThemeData(id, name, theme);
        hexInputs.clear();
    }

    public int color(Token token) {
        return getColor(draft(), token);
    }

    public void setColor(Token token, int color) {
        if (!selectedIsCustom()) {
            return;
        }

        setColor(draft(), token, color);
        hexInputs.put(token.id, formatColor(color));
        if (token == Token.ACCENT) {
            int soft = withAlpha(color, 0x66);
            setColor(draft(), Token.ACCENT_SOFT, soft);
            hexInputs.put(Token.ACCENT_SOFT.id, formatColor(soft));
        }
    }

    public String themeName() {
        CustomThemeData data = draft();
        return data == null ? "" : data.displayName;
    }

    public void setThemeName(String name) {
        if (!selectedIsCustom()) {
            return;
        }

        String cleaned = name == null ? "" : name.trim();
        if (cleaned.isBlank()) {
            cleaned = "Custom Theme";
        }
        if (cleaned.length() > 28) {
            cleaned = cleaned.substring(0, 28);
        }

        draft().displayName = cleaned;
    }

    public boolean isHexFocused(Token token) {
        return token != null && token.id.equals(focusedHexToken);
    }

    public void focusHex(Token token) {
        focusedHexToken = token == null ? "" : token.id;
        if (token != null) {
            hexInputs.put(token.id, "#");
        }
    }

    public void clearHexFocus() {
        focusedHexToken = "";
    }

    public Token focusedToken() {
        for (Token token : Token.values()) {
            if (token.id.equals(focusedHexToken)) {
                return token;
            }
        }
        return null;
    }

    public String hexInput(Token token) {
        return hexInputs.getOrDefault(token.id, formatColor(color(token)));
    }

    public HexResult appendHexChar(char character) {
        Token token = focusedToken();
        if (token == null) {
            return HexResult.IGNORED;
        }

        char upper = Character.toUpperCase(character);
        if (!isHexChar(upper)) {
            return HexResult.INVALID;
        }

        String current = hexInput(token).replace("#", "");
        if (current.length() >= 8) {
            return HexResult.INVALID;
        }

        current += upper;
        hexInputs.put(token.id, "#" + current);
        return applyHexInput(token, false);
    }

    public HexResult backspaceHex() {
        Token token = focusedToken();
        if (token == null) {
            return HexResult.IGNORED;
        }

        String current = hexInput(token).replace("#", "");
        if (!current.isEmpty()) {
            current = current.substring(0, current.length() - 1);
        }
        hexInputs.put(token.id, "#" + current);
        return applyHexInput(token, false);
    }

    public HexResult commitHex() {
        Token token = focusedToken();
        if (token == null) {
            return HexResult.IGNORED;
        }

        HexResult result = applyHexInput(token, true);
        if (result == HexResult.APPLIED) {
            clearHexFocus();
        }
        return result;
    }

    public String formatColor(Token token) {
        return formatColor(color(token));
    }

    public static String formatColor(int color) {
        return String.format("#%08X", color);
    }

    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    public enum HexResult {
        APPLIED,
        PARTIAL,
        INVALID,
        IGNORED
    }

    private void ensureDraft() {
        resolveSelectedThemeId();
        if (!CustomThemeManager.isCustomTheme(selectedThemeId)) {
            draft = null;
            return;
        }

        CustomThemeData saved = CustomThemeManager.get(selectedThemeId);
        if (saved != null && (draft == null || !saved.id.equals(draft.id))) {
            draft = new CustomThemeData(saved);
            for (Token token : Token.values()) {
                hexInputs.put(token.id, formatColor(getColor(draft, token)));
            }
        }
    }

    private HexResult applyHexInput(Token token, boolean requireComplete) {
        String raw = hexInput(token).trim();
        String hex = raw.startsWith("#") ? raw.substring(1) : raw;

        if (hex.length() == 6) {
            setColor(token, (0xFF << 24) | Integer.parseUnsignedInt(hex, 16));
            return HexResult.APPLIED;
        }

        if (hex.length() == 8) {
            setColor(token, (int) Long.parseLong(hex, 16));
            return HexResult.APPLIED;
        }

        return requireComplete ? HexResult.INVALID : HexResult.PARTIAL;
    }

    private void resolveSelectedThemeId() {
        if (selectedThemeId == null || selectedThemeId.isBlank() || ThemeManager.byId(selectedThemeId) == null) {
            selectedThemeId = ConfigManager.get().theme;
        }

        Theme fallback = ThemeManager.defaultTheme();
        if (ThemeManager.byId(selectedThemeId) == null && fallback != null) {
            selectedThemeId = fallback.id();
        }
    }

    private boolean same(CustomThemeData left, CustomThemeData right) {
        String leftName = left.displayName == null ? "" : left.displayName;
        String rightName = right.displayName == null ? "" : right.displayName;
        return leftName.equals(rightName)
                && left.background == right.background
                && left.panel == right.panel
                && left.panelAlt == right.panelAlt
                && left.accent == right.accent
                && left.accentSoft == right.accentSoft
                && left.text == right.text
                && left.mutedText == right.mutedText
                && left.border == right.border;
    }

    private boolean isHexChar(char character) {
        return (character >= '0' && character <= '9') || (character >= 'A' && character <= 'F');
    }

    private int getColor(CustomThemeData data, Token token) {
        if (data == null) {
            return 0xFFFFFFFF;
        }

        return switch (token) {
            case BACKGROUND -> data.background;
            case PANEL -> data.panel;
            case PANEL_ALT -> data.panelAlt;
            case BORDER -> data.border;
            case ACCENT -> data.accent;
            case ACCENT_SOFT -> data.accentSoft;
            case TEXT -> data.text;
            case MUTED_TEXT -> data.mutedText;
        };
    }

    private void setColor(CustomThemeData data, Token token, int color) {
        if (data == null) {
            return;
        }

        switch (token) {
            case BACKGROUND -> data.background = color;
            case PANEL -> data.panel = color;
            case PANEL_ALT -> data.panelAlt = color;
            case BORDER -> data.border = color;
            case ACCENT -> data.accent = color;
            case ACCENT_SOFT -> data.accentSoft = color;
            case TEXT -> data.text = color;
            case MUTED_TEXT -> data.mutedText = color;
        }
    }
}
