package com.skrra.atmosphereplus.themes;

public class CustomThemeData {
    public String id = "";
    public String displayName = "Custom Theme";
    public int background;
    public int panel;
    public int panelAlt;
    public int accent;
    public int accentSoft;
    public int text;
    public int mutedText;
    public int border;

    public CustomThemeData() {
    }

    public CustomThemeData(String id, String displayName, Theme theme) {
        this.id = id;
        this.displayName = displayName;
        this.background = theme.background();
        this.panel = theme.panel();
        this.panelAlt = theme.panelAlt();
        this.accent = theme.accent();
        this.accentSoft = theme.accentSoft();
        this.text = theme.text();
        this.mutedText = theme.mutedText();
        this.border = theme.border();
    }

    public CustomThemeData(CustomThemeData other) {
        this.id = other.id;
        this.displayName = other.displayName;
        this.background = other.background;
        this.panel = other.panel;
        this.panelAlt = other.panelAlt;
        this.accent = other.accent;
        this.accentSoft = other.accentSoft;
        this.text = other.text;
        this.mutedText = other.mutedText;
        this.border = other.border;
    }

    public Theme toTheme() {
        return new Theme(id, displayName, background, panel, panelAlt, accent, accentSoft, text, mutedText, border);
    }
}
