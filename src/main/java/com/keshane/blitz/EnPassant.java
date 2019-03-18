package com.keshane.blitz;

public class EnPassant implements Move, Capture {
    private final Square source;
    private final Square destination;
    private final Piece capturedPiece;
    private final Square capturedPieceLocation;
    private final Piece pawn;
    private final boolean isCheck;
    private final boolean isCheckmate;

    EnPassant(Piece capturingPawn, Square source, Square destination, Piece capturedPiece, boolean isCheck,
        boolean isCheckmate) {
        this.source = source;
        this.destination = destination;
        this.capturedPiece = capturedPiece;
        this.pawn = capturingPawn;
        this.isCheck = isCheck;
        this.isCheckmate = isCheckmate;
        int direction = destination.rank - source.rank;
        this.capturedPieceLocation = Square.of(destination.file, destination.rank - direction);


    }

    @Override
    public boolean isCapture() {
        return true;
    }

    @Override
    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    @Override
    public Square getCapturedPieceLocation() {
        return capturedPieceLocation;
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
        board.move(source, destination);
        board.remove(capturedPieceLocation);
    }

    @Override
    public boolean isAMatch(InterpretedNotation rawMove) {
        // not checking for MoveType.EN_PASSANT because some notations don't include it
        if (!rawMove.moveTypes.contains(MoveType.CAPTURE)) {
            return false;
        }

        Square rawMoveDestination = Square.of(rawMove.destinationFile, rawMove.destinationRank);
        return rawMove.piece == Role.PAWN && rawMove.sourceFile == source.file && rawMoveDestination == destination;
    }
}
