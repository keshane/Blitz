package com.keshane.blitz;

import java.util.HashMap;
import java.util.Map;

public enum Role {
    PAWN(""),
    BISHOP("B"),
    KNIGHT("N"),
    ROOK("R"),
    QUEEN("Q"),
    KING("K");

    private final static Map<String, Role> ROLE_FROM_NOTATION;

    static {
        ROLE_FROM_NOTATION = new HashMap<>();
        for (Role piece : Role.values()) {
            ROLE_FROM_NOTATION.put(piece.getNotation(), piece);
        }

    }

    private String notation;

    Role(String notation) {
        this.notation = notation;
    }

    String getNotation() {
        return notation;
    }

    static Role fromNotation(String notation) {
        return ROLE_FROM_NOTATION.get(notation);
    }
}
