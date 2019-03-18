package com.keshane.blitz;

class Promotion implements Move, Capture {
    private final Square source;
    private final Square destination;
    private final Piece pawn;
    private final Piece promotedPiece;
    private final boolean isCheck;
    private final boolean isCheckmate;
    private final Piece capturedPiece;

    Promotion(Piece pawn, Square source, Square destination, Piece promotedPiece, Piece capturedPiece,
        boolean isCheck, boolean isCheckmate) {
        this.source = source;
        this.destination = destination;
        this.pawn = pawn;
        this.promotedPiece = promotedPiece;
        this.isCheck = isCheck;
        this.isCheckmate = isCheckmate;
        this.capturedPiece = capturedPiece;

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
        return pawn;
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
    public void makeMoveOnBoard(Board board) {
        board.remove(source);
        if (isCapture()) {
            board.remove(destination);
        }
        board.place(promotedPiece, destination);
    }

    Piece getPromotedPiece() {
        return promotedPiece;
    }

    @Override
    public boolean isCapture() {
        return capturedPiece != null;
    }

    @Override
    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    @Override
    public Square getCapturedPieceLocation() {
        return destination;
    }

    @Override
    public boolean isAMatch(InterpretedNotation rawMove) {
        if (!rawMove.moveTypes.contains(MoveType.PROMOTION) || rawMove.promotionPiece == null) {
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
        if (rawMove.moveTypes.contains(MoveType.CAPTURE) && !isCapture()) {
            return false;
        }

        Square rawMoveDestination = Square.of(rawMove.destinationFile, rawMove.destinationRank);
        return rawMove.piece == Role.PAWN && rawMoveDestination == destination
            && rawMove.promotionPiece == promotedPiece.getRole();
    }
}
