package com.keshane.blitz;

// TODO rename this to Move after deleting previous Move
interface Move {
    Square getSource();
    Square getDestination();
    Piece getMovingPiece();
    boolean isCheck();
    boolean isCheckmate();
    void makeMoveOnBoard(Board board);
    boolean isAMatch(InterpretedNotation rawMove);

}
