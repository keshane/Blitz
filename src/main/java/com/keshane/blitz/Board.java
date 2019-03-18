package com.keshane.blitz;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class Board {
    /**
     * Keeps track of the pieces by square.
     */
    private Map<Square, Piece> occupiedSquares;

    /**
     * Keeps track of the locations of pieces by piece type.
     * Each piece type will always have an entry in this Map.
     */
    private Map<Piece, Set<Square>> pieceLocations;


    Board(String fenBoardPosition) {
        pieceLocations = new EnumMap<>(Piece.class);
        for (Piece piece : Piece.values()) {
            pieceLocations.put(piece, EnumSet.noneOf(Square.class));
        }
        occupiedSquares = new EnumMap<>(Square.class);
        parseFen(fenBoardPosition);
    }

    /**
     * Copies the state of the specified {@link Board} into the current instance.
     *
     * @param otherBoard the {@link Board} whose state should be copied
     */
    void replaceBoardWith(Board otherBoard) {
        this.occupiedSquares.clear();
        this.occupiedSquares.putAll(otherBoard.occupiedSquares);
        // for each piece, clear out the old locations and add the new locations
        // The pieceLocations field in a Board contains an entry for every piece, so all pieces will be updated in
        // the loop.
        for (Map.Entry<Piece, Set<Square>> newPieceLocation : otherBoard.pieceLocations.entrySet()) {
            pieceLocations.get(newPieceLocation.getKey()).clear();
            pieceLocations.get(newPieceLocation.getKey()).addAll(newPieceLocation.getValue());

        }
    }

    private void parseFen(String fenBoardPosition) {
        int file = 0;
        int rank = 7;
        for (char pieceFen : fenBoardPosition.toCharArray()) {
            if (Character.isDigit(pieceFen)) {
                int emptySquareCount = Character.digit(pieceFen, 10);
                file += emptySquareCount;
                if (file < 1 || file > 8) {
                    throw new IllegalArgumentException("There can only be 1 to 8 empty squares in a single rank.");
                }
            } else if (Character.isAlphabetic(pieceFen)) {
                Piece piece = Piece.fromFen(Character.toString(pieceFen));
                if (piece == null) {
                    throw new IllegalArgumentException(String.format("%c is not a valid FEN for a piece.", pieceFen));
                }
                occupiedSquares.put(Square.of(file, rank), piece);
                pieceLocations.get(piece).add(Square.of(file, rank));
                file += 1;
            } else if (pieceFen == '/') {
                file = 0;
                rank -= 1;
                if (rank < 0) {
                    throw new IllegalArgumentException("There can only be 8 ranks.");
                }
            } else {
                throw new IllegalArgumentException(String.format("%c is not a valid FEN for piece placement",
                    pieceFen));
            }
        }
    }

    public Set<Square> getPieceLocations(Piece piece) {
        return EnumSet.copyOf(pieceLocations.get(piece));
    }

    public Piece getPieceOn(Square square) {
        return occupiedSquares.get(square);
    }

    void move(Square source, Square destination) {
        if (source == null || destination == null) {
            throw new IllegalArgumentException("Square to move from and to cannot be null.");
        }
        if (!occupiedSquares.containsKey(source)) {
            throw new IllegalArgumentException("Can't move from an unoccupied square: " + source.toString());
        }
        Piece movingPiece = occupiedSquares.get(source);
        occupiedSquares.remove(source);
        Piece pieceAtDestination = occupiedSquares.get(destination);
        occupiedSquares.put(destination, movingPiece);

        pieceLocations.get(movingPiece).remove(source);
        pieceLocations.get(movingPiece).add(destination);

        if (pieceAtDestination != null) {
            pieceLocations.get(pieceAtDestination).remove(destination);
        }
    }

    void remove(Square target) {
        if (target == null) {
            throw new IllegalArgumentException("Square to remove piece from cannot be null.");
        }

        if (!occupiedSquares.containsKey(target)) {
            throw new IllegalArgumentException("Can't remove from an unoccupied square: " + target.toString());
        }

        Piece pieceToRemove = occupiedSquares.get(target);
        occupiedSquares.remove(target);

        pieceLocations.get(pieceToRemove).remove(target);
    }

    void place(Piece piece, Square target) {
         if (target == null) {
            throw new IllegalArgumentException("Square to remove piece from cannot be null.");
        }

        if (occupiedSquares.containsKey(target)) {
            throw new IllegalArgumentException("Can't place on an occupied square: " + target.toString());
        }

        occupiedSquares.put(target, piece);

        pieceLocations.get(piece).add(target);

    }

}
