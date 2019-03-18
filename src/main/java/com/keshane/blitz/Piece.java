package com.keshane.blitz;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumerates all the possible pieces on a chessboard
 */
public enum Piece {
    WHITE_PAWN(Color.WHITE, Role.PAWN, "P"),
    WHITE_BISHOP(Color.WHITE, Role.BISHOP, "B"),
    WHITE_KNIGHT(Color.WHITE, Role.KNIGHT, "N"),
    WHITE_ROOK(Color.WHITE, Role.ROOK, "R"),
    WHITE_QUEEN(Color.WHITE, Role.QUEEN, "Q"),
    WHITE_KING(Color.WHITE, Role.KING, "K"),

    BLACK_PAWN(Color.BLACK, Role.PAWN, "p"),
    BLACK_BISHOP(Color.BLACK, Role.BISHOP, "b"),
    BLACK_KNIGHT(Color.BLACK, Role.KNIGHT, "n"),
    BLACK_ROOK(Color.BLACK, Role.ROOK, "r"),
    BLACK_QUEEN(Color.BLACK, Role.QUEEN, "q"),
    BLACK_KING(Color.BLACK, Role.KING, "k");

    private static final Map<String, Piece> PIECE_FROM_FEN;
    private static final Map<Color, Map<Role, Piece>> PIECE_FROM_COLOR_AND_ROLE;

    static {
        PIECE_FROM_FEN = new HashMap<>();
        PIECE_FROM_COLOR_AND_ROLE = new HashMap<>();
        for (Piece piece : Piece.values()) {
            PIECE_FROM_FEN.put(piece.getFenNotation(), piece);

            if (!PIECE_FROM_COLOR_AND_ROLE.containsKey(piece.getColor())) {
                PIECE_FROM_COLOR_AND_ROLE.put(piece.getColor(), new HashMap<>());
            }
            PIECE_FROM_COLOR_AND_ROLE.get(piece.getColor()).put(piece.getRole(), piece);
        }


    }

    private final Color color;
    private final String fenNotation;
    private final Role type;

    Piece(Color color, Role type, String fenNotation) {
        this.color = color;
        this.fenNotation = fenNotation;
        this.type = type;

    }

    static Piece fromFen(String fenNotation) {
        if (!PIECE_FROM_FEN.containsKey(fenNotation)) {
            throw new IllegalArgumentException(fenNotation + " is not a valid FEN for a piece.");
        }

        return PIECE_FROM_FEN.get(fenNotation);
    }

    static Piece from(Color color, Role pieceType) {
        if (color == null || pieceType == null) {
            throw new IllegalArgumentException("Cannot have a null Color or Role");
        }
        return PIECE_FROM_COLOR_AND_ROLE.get(color).get(pieceType);
    }

    /**
     * Get the color of this piece.
     *
     * @return the color associated with this piece.
     */
    Color getColor() {
        return color;
    }

    String getFenNotation() {
        return fenNotation;
    }

    Role getRole() {
        return type;
    }}
