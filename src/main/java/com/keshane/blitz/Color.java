package com.keshane.blitz;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines all the possible colors of chess pieces.
 */
enum Color {
    WHITE("w", 0),
    BLACK("b", 7);

    private static Map<String, Color> colorFromNotation = new HashMap<>();

    static {
        WHITE.opposite = BLACK;
        BLACK.opposite = WHITE;

        for (Color color : Color.values()) {
            colorFromNotation.put(color.notation, color);
        }
    }

    private final String notation;
    private final int backRank;
    private Color opposite;

    Color(String notation, int backRank) {
        this.notation = notation;
        this.backRank = backRank;
    }

    @Deprecated
    static Color oppositeOf(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("There is no opposite of null Color.");
        }
        return color == WHITE ? BLACK : WHITE;
    }

    static Color from(String notation) {
        if (!colorFromNotation.containsKey(notation)) {
            throw new IllegalArgumentException("There is no color with that notation.");
        }
        return colorFromNotation.get(notation);

    }

    Color opposite() {
        return opposite;
    }

    int backRank() {
        return backRank;
    }

    String getNotation() {
        return notation;
    }

}
