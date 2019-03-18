package com.keshane.blitz;

import java.text.ParseException;
import java.util.*;

// TODO
class NotationParser {
    private static final String CASTLE_KINGSIDE_OHS = "O-O";
    private static final String CASTLE_KINGSIDE_ZEROS = "0-0";
    private static final String CASTLE_QUEENSIDE_OHS = "O-O-O";
    private static final String CASTLE_QUEENSIDE_ZEROS = "0-0-0";
    private static final String EN_PASSANT = "e.p.";
    private static final char MINIMUM_FILE_CHARACTER = 'a';
    private static final char MINIMUM_RANK_CHARACTER = '1';
    private static final char ROOK_CHARACTER = 'R';
    private static final char KNIGHT_CHARACTER = 'N';
    private static final char BISHOP_CHARACTER = 'B';
    private static final char QUEEN_CHARACTER = 'Q';
    private static final char KING_CHARACTER = 'K';
    private static final Set<Character> PROMOTION_CHARACTERS = new HashSet<>(Arrays.asList(ROOK_CHARACTER,
        KNIGHT_CHARACTER, BISHOP_CHARACTER, QUEEN_CHARACTER));
    private static final char CHECK = '+';
    private static final char CHECKMATE = '#';


    static InterpretedNotation parseMove(String notation) throws ParseException {
        if (notation == null || notation.isEmpty()) {
            throw new ParseException("Notation must be a non-empty input", 0);
        }

        InterpretedNotation move = new InterpretedNotation();
        if (notation.startsWith(CASTLE_QUEENSIDE_OHS) || notation.startsWith(CASTLE_QUEENSIDE_ZEROS)) {
            if (notation.length() == CASTLE_QUEENSIDE_OHS.length() + 1) {
                if (notation.endsWith("+")) {
                    move.moveTypes.add(MoveType.CHECK);
                }
                else if (notation.endsWith("#")) {
                    move.moveTypes.add(MoveType.CHECKMATE);
                }
                else {
                    throw new ParseException("Did not recognize character", notation.length());
                }
            }
            move.moveTypes.add(MoveType.QUEENSIDE_CASTLE);
            return move;
        }
        else if (notation.startsWith(CASTLE_KINGSIDE_OHS) || notation.startsWith(CASTLE_KINGSIDE_ZEROS)) {
            if (notation.length() == CASTLE_KINGSIDE_OHS.length() + 1) {
                if (notation.endsWith("+")) {
                    move.moveTypes.add(MoveType.CHECK);
                }
                else if (notation.endsWith("#")) {
                    move.moveTypes.add(MoveType.CHECKMATE);
                }
                else {
                    throw new ParseException("Did not recognize character", notation.length());
                }
            }
            move.moveTypes.add(MoveType.KINGSIDE_CASTLE);
            return move;
            // do something
        }

        else {
            parseStandardNotation(notation, notation.length() - 1, move);
            if (!move.moveTypes.contains(MoveType.EN_PASSANT) && !move.moveTypes.contains(MoveType.PROMOTION)) {
                move.moveTypes.add(MoveType.NORMAL);
            }
            return move;

        }

    }

    private static void parseStandardNotation(String notation, int index, InterpretedNotation move) throws ParseException {
        if (notation == null || notation.length() < 2) {
            throw new ParseException("Notation must be at least 2 character", 0);
        }

        // Start parsing from the end of the string and move forward.
        parseCheck(notation, index, move);

        // Can this ever happen with how the methods are implemented?
        if (move.piece == null || move.destinationFile == -1 || move.destinationRank == -1) {
            throw new ParseException("Move needs a moving piece and/or a destination square.", 0);
        }
    }

    /**
     * TODO
     *
     * @param notation
     * @param move
     * @return
     */
    private static void parseCheck(String notation, int index, InterpretedNotation move) throws ParseException {
        if (notation.charAt(index) == CHECK) {
            move.moveTypes.add(MoveType.CHECK);
            index--;
        }
        parseCheckmate(notation, index, move);
    }

    /**
     * TODO
     *
     * @return
     */
    private static void parseCheckmate(String notation, int index, InterpretedNotation move) throws ParseException {
        if (notation.charAt(index) == CHECKMATE) {
            move.moveTypes.add(MoveType.CHECKMATE);
            index--;
        }

        parseEnPassant(notation, index, move);

    }

    private static void parseEnPassant(String notation, int index, InterpretedNotation move) throws ParseException {
        if (notation.lastIndexOf(EN_PASSANT) != -1
            && notation.lastIndexOf(EN_PASSANT) + EN_PASSANT.length() - 1 == index) {
            move.moveTypes.add(MoveType.EN_PASSANT);
            index = notation.lastIndexOf(EN_PASSANT) - 1;
        }
        parsePromotion(notation, index, move);

    }

    private static void parsePromotion(String notation, int index, InterpretedNotation move) throws ParseException {
        if (PROMOTION_CHARACTERS.contains(notation.charAt(index)) && index - 1 > 1
            && notation.charAt(index - 1) == '=') {
            move.moveTypes.add(MoveType.PROMOTION);
            move.promotionPiece = Role.fromNotation(notation.substring(index, index + 1));
            index -= 2;
        }
        parseDestinationRank(notation, index, move);

    }

    private static void parseDestinationRank(String notation, int index, InterpretedNotation move) throws ParseException {
        if (index < 0) {
            throw new ParseException(String.format("Could not find a destination rank in %s", notation), index);
        }
        char rankRepresentation = notation.charAt(index);
        if (rankRepresentation - MINIMUM_RANK_CHARACTER < 0 || rankRepresentation - MINIMUM_RANK_CHARACTER > 7) {
            throw new ParseException(String.format("Destination rank in %s was not recognized", notation), index);
        }
        int rank = rankRepresentation - MINIMUM_RANK_CHARACTER;
        move.destinationRank = rank;
        parseDestinationFile(notation, index - 1, move);

    }

    private static void parseDestinationFile(String notation, int index, InterpretedNotation move) throws ParseException {
        if (index < 0) {
            throw new ParseException(String.format("Could not find a destination file in %s", notation), index);
        }
        char fileRepresentation = notation.charAt(index);
        if (fileRepresentation - MINIMUM_FILE_CHARACTER < 0 || fileRepresentation - MINIMUM_FILE_CHARACTER > 7) {
            throw new ParseException(String.format("Destination file in %s was not recognized", notation), index);
        }
        int file = fileRepresentation - MINIMUM_FILE_CHARACTER;
        move.destinationFile = file;

        parseCapture(notation, index - 1, move);
    }

    private static void parseCapture(String notation, int index, InterpretedNotation move) throws ParseException {
        if (index < 0) {
            parsePiece(notation, index, move);
            return;
        }
        char captureRepresentation = notation.charAt(index);
        if (captureRepresentation == 'x') {
            index--;
            move.moveTypes.add(MoveType.CAPTURE);
        }
        parseSourceRank(notation, index, move);
    }

    private static void parseSourceRank(String notation, int index, InterpretedNotation move) throws ParseException {
        if (index < 0) {
            parsePiece(notation, index, move);
            return;
        }
        char rankRepresentation = notation.charAt(index);
        if (rankRepresentation - MINIMUM_RANK_CHARACTER >= 0 && rankRepresentation - MINIMUM_RANK_CHARACTER <= 7) {
            index--;
            int rank = rankRepresentation - MINIMUM_RANK_CHARACTER;
            move.sourceRank = rank;
        }

        parseSourceFile(notation, index, move);

    }

    private static void parseSourceFile(String notation, int index, InterpretedNotation move) throws ParseException {
        if (index < 0) {
            parsePiece(notation, index, move);
            return;
        }
        char fileRepresentation = notation.charAt(index);
        if (fileRepresentation - MINIMUM_FILE_CHARACTER >= 0 && fileRepresentation - MINIMUM_FILE_CHARACTER <= 7) {
            index--;
            int file = fileRepresentation - MINIMUM_FILE_CHARACTER;
            move.sourceFile = file;
        }


        parsePiece(notation, index, move);
    }

    private static void parsePiece(String notation, int index, InterpretedNotation move) throws ParseException {
        if (index < 0) {
            // no character means a pawn
            move.piece = Role.PAWN;
        }
        else if (index == 0) {
            String pieceRepresentation = notation.substring(index, index + 1);
            Role piece = Role.fromNotation(pieceRepresentation);
            if (null == piece) {
                throw new ParseException(String.format("%s is not a valid piece.", pieceRepresentation), index);
            }
            move.piece = piece;
        }
        else {
            throw new ParseException(String.format("Did not recognize notation: %s", notation.substring(0, index)), 0);
        }

    }
}
