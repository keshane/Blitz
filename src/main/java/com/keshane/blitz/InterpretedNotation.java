package com.keshane.blitz;

import java.util.EnumSet;
import java.util.Set;

/**
 * Holds the interpretation of an algebraic notation move.
 */
class InterpretedNotation {
    Role piece;
    Role promotionPiece;
    int destinationRank = -1;
    int destinationFile = -1;
    int sourceRank = -1;
    int sourceFile = -1;
    Set<MoveType> moveTypes = EnumSet.noneOf(MoveType.class);

    /**
     * Construct an empty InterpretedNotation
     */
    InterpretedNotation() {
        // no-op
    }
}
