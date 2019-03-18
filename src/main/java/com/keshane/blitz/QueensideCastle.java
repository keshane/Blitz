package com.keshane.blitz;

class QueensideCastle implements Castle {
    @Override
    public void makeMoveOnBoard(Board board) {
        board.move(kingSource, kingDestination);
        board.move(rookSource, rookDestination);
    }

    private final Square rookSource;
    private final Square rookDestination;
    private final Square kingSource;
    private final Square kingDestination;
    private final Piece king;
    private final boolean isCheck;
    private final boolean isCheckmate;

    QueensideCastle(Piece king, boolean isCheck, boolean isCheckmate) {
        if (king == null) {
            throw new IllegalArgumentException("king cannot be null");
        }

        int rank = king.getColor() == Color.WHITE ? 0 : 7;
        rookSource = Square.of(0, rank);
        rookDestination = Square.of(3, rank);
        kingSource = Square.of(4, rank);
        kingDestination = Square.of(2, rank);
        this.king = king;
        this.isCheck = isCheck;
        this.isCheckmate = isCheckmate;

    }

    @Override
    public Square getSource() {
        return kingSource;
    }

    @Override
    public Square getDestination() {
        return kingDestination;
    }

    @Override
    public Piece getMovingPiece() {
        return king;
    }

    @Override
    public boolean isCheck() {
        return isCheck;
    }

    @Override
    public boolean isCheckmate() {
        return isCheckmate;
    }

    @Override
    public Square getRookSource() {
        return rookSource;
    }

    @Override
    public Square getRookDestination() {
        return rookDestination;
    }

    @Override
    public boolean isAMatch(InterpretedNotation rawMove) {
        return rawMove.moveTypes.contains(MoveType.QUEENSIDE_CASTLE);
    }
}
