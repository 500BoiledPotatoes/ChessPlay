package com.example.chessplay;

import android.content.SharedPreferences;
import android.graphics.Color;

public class ColorTheme {
    private static ColorTheme inst = null;

    public static ColorTheme instance() {
        if (inst == null)
            inst = new ColorTheme();
        return inst;
    }

    public final static int DARK_SQUARE = 0;
    public final static int BRIGHT_SQUARE = 1;
    public final static int SELECTED_SQUARE = 2;
    public final static int DARK_PIECE = 3;
    public final static int BRIGHT_PIECE = 4;
    public final static int CURRENT_MOVE = 5;
    public final static int ARROW_0 = 6;
    public final static int MAX_ARROWS = 8;
    public final static int SQUARE_LABEL = 14;
    public final static int DECORATION = 15;
    public final static int PGN_COMMENT = 16;
    public final static int FONT_FOREGROUND = 17;
    public final static int GENERAL_BACKGROUND = 18;
    private final static int numColors = 19;

    private int[] colorTable = new int[numColors];

    private static final String[] prefNames = {
        "darkSquare", "brightSquare", "selectedSquare", "darkPiece", "brightPiece", "currentMove",
        "arrow0", "arrow1", "arrow2", "arrow3", "arrow4", "arrow5", "arrow6", "arrow7",
        "squareLabel", "decoration", "pgnComment", "fontForeground", "generalBackground"
    };
    private static final String prefPrefix = "color_";

    private final static String[] themeColors =
    {
        "#B58863",   "#F0D9B5",   "#FF000000", "#FF000000", "#FFFFFFFF", "#FF666666",
        "#A01F1FFF", "#A0FF1F1F", "#501F1FFF", "#50FF1F1F", "#371F1FFF", "#3CFF1F1F", "#1E1F1FFF", "#28FF1F1F",
        "#FFFF0000", "#FF808080", "#FFC0C000", "#FFF7FAE3", "#FF40260A"
    };

    final void readColors(SharedPreferences settings) {
        for (int i = 0; i < numColors; i++) {
            String prefName = prefPrefix + prefNames[i];
            String defaultColor = themeColors[i];
            String colorString = settings.getString(prefName, defaultColor);
            colorTable[i] = 0;
            try {
                colorTable[i] = Color.parseColor(colorString);
            } catch (IllegalArgumentException|StringIndexOutOfBoundsException ignore) {
            }
        }
    }
    public final int getColor(int colorType) {
        return colorTable[colorType];
    }
}
