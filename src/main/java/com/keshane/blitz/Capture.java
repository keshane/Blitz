package com.keshane.blitz;

interface Capture {
    boolean isCapture();
    Piece getCapturedPiece();
    Square getCapturedPieceLocation();
}
