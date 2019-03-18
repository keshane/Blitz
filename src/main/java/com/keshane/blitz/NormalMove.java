package com.keshane.blitz;

class NormalMove implements Move, Capture {
    private final Square source;
    private final Square destination;
    private final Piece movingPiece;
    private final Piece capturedPiece;
    private final boolean isCheck;
    private final boolean isCheckmate;
    private final boolean isCapture;

    NormalMove(Piece movingPiece, Square source, Square destination, Piece capturedPiece, boolean isCheck,
        boolean isCheckmate) {
        this.movingPiece = movingPiece;
        this.source = source;
        this.destination = destination;
        this.capturedPiece = capturedPiece;
        isCapture = capturedPiece != null;
        this.isCheck = isCheck;
        this.isCheckmate = isCheckmate;


    }

    @Override
    public void makeMoveOnBoard(Board board)  {
        board.move(source, destination);
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
    public Square getSource() {
        return source;
    }

    @Override
    public Square getDestination() {
        return destination;
    }

    @Override
    public Piece getMovingPiece() {
        return movingPiece;
    }

    @Override
    public boolean isCapture() {
        return isCapture;
    }

    @Override
    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    @Override
    public Square getCapturedPieceLocation() {
        return capturedPiece == null ? null : destination;
    }

    @Override
    public boolean isAMatch(InterpretedNotation rawMove) {
        if (!rawMove.moveTypes.contains(MoveType.NORMAL)) {
            return false;
        }
        // If source file or rank is specified and doesn't match the expected source file or rank,
        // rawMove doesn't match
        if (rawMove.sourceFile != -1 && rawMove.sourceFile != source.file) {
            return false;
        }
        if (rawMove.sourceRank != -1 && rawMove.sourceRank != source.rank) {
            return false;
        }

        // If a capture is specified by rawMove but the expected move is not a capture,
        // rawMove doesn't match. However, if the rawMove does NOT specify a capture, but
        // the expected move IS a capture, allow the rawMove to match.
        if (rawMove.moveTypes.contains(MoveType.CAPTURE) && !isCapture) {
            return false;
        }

        Square rawMoveDestination = Square.of(rawMove.destinationFile, rawMove.destinationRank);
        return rawMove.piece == movingPiece.getRole() && rawMoveDestination == destination;
    }
}
