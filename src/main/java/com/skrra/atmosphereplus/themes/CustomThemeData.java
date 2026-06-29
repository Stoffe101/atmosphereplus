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

    public Theme toTheme() {
        return new Theme(id, displayName, background, panel, panelAlt, accent, accentSoft, text, mutedText, border);
    }
}
