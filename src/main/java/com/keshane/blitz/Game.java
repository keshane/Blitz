package com.keshane.blitz;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the chess game state and behavior.
 * <p>
 * This is the only class that users of this library should use.
 */
// TODO generateXMoves should return a map of sets instead of list
public class Game {

    private static final List<Direction> INTERCARDINAL_DIRECTIONS;

    private static final List<Direction> CARDINAL_DIRECTIONS;

    private static final List<Direction> CARDINAL_AND_INTERCARDINAL_DIRECTIONS;

    private static final Map<Color, List<Direction>> PAWN_CAPTURE_DIRECTIONS;

    private static final Map<Color, Direction> PAWN_MOVE_DIRECTIONS;
    private static final List<Direction> KNIGHT_DIRECTIONS;
    private static String DEFAULT_STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    static {
        // TODO make these part of Piece class
        INTERCARDINAL_DIRECTIONS = new ArrayList<>(4);
        INTERCARDINAL_DIRECTIONS.add(Direction.NORTHEAST);
        INTERCARDINAL_DIRECTIONS.add(Direction.SOUTHEAST);
        INTERCARDINAL_DIRECTIONS.add(Direction.SOUTHWEST);
        INTERCARDINAL_DIRECTIONS.add(Direction.NORTHWEST);

        CARDINAL_DIRECTIONS = new ArrayList<>(4);
        CARDINAL_DIRECTIONS.add(Direction.NORTH);
        CARDINAL_DIRECTIONS.add(Direction.EAST);
        CARDINAL_DIRECTIONS.add(Direction.SOUTH);
        CARDINAL_DIRECTIONS.add(Direction.WEST);

        CARDINAL_AND_INTERCARDINAL_DIRECTIONS = new ArrayList<>(8);
        CARDINAL_AND_INTERCARDINAL_DIRECTIONS.addAll(CARDINAL_DIRECTIONS);
        CARDINAL_AND_INTERCARDINAL_DIRECTIONS.addAll(INTERCARDINAL_DIRECTIONS);


        KNIGHT_DIRECTIONS = new ArrayList<>();
        KNIGHT_DIRECTIONS.add(Direction.NORTH_NORTHEAST);
        KNIGHT_DIRECTIONS.add(Direction.EAST_NORTHEAST);
        KNIGHT_DIRECTIONS.add(Direction.EAST_SOUTHEAST);
        KNIGHT_DIRECTIONS.add(Direction.SOUTH_SOUTHEAST);
        KNIGHT_DIRECTIONS.add(Direction.SOUTH_SOUTHWEST);
        KNIGHT_DIRECTIONS.add(Direction.WEST_SOUTHWEST);
        KNIGHT_DIRECTIONS.add(Direction.WEST_NORTHWEST);
        KNIGHT_DIRECTIONS.add(Direction.NORTH_NORTHWEST);


        PAWN_CAPTURE_DIRECTIONS = new EnumMap<>(Color.class);

        List<Direction> whitePawnDirections = new ArrayList<>();
        whitePawnDirections.add(Direction.NORTHEAST);
        whitePawnDirections.add(Direction.NORTHWEST);
        PAWN_CAPTURE_DIRECTIONS.put(Color.WHITE, whitePawnDirections);

        List<Direction> blackPawnDirections = new ArrayList<>();
        blackPawnDirections.add(Direction.SOUTHEAST);
        blackPawnDirections.add(Direction.SOUTHWEST);
        PAWN_CAPTURE_DIRECTIONS.put(Color.BLACK, blackPawnDirections);


        PAWN_MOVE_DIRECTIONS = new EnumMap<>(Color.class);
        PAWN_MOVE_DIRECTIONS.put(Color.WHITE, Direction.NORTH);
        PAWN_MOVE_DIRECTIONS.put(Color.BLACK, Direction.SOUTH);
    }


    /**
     * Contains all the possible moves of the next player to move.
     */
    private final Set<Move> nextPossibleMoves = new HashSet<>();
    /**
     * The name of the player using the black pieces.
     */
    private String blackName;
    /**
     * The name of the player using the white pieces.
     */
    private String whiteName;
    /**
     * The initial state of the game in Forsyth-Edwards Notation (FEN).
     */
    private String startingFen;
    /**
     * Keeps track of the locations of the pieces.
     */
    private Board board;
    /**
     * Used to verify the correctness of a move.
     */
    private Board verificationBoard;
    /**
     * The next player to move.
     */
    private Color playerToMove;
    /**
     * Holds the history of the moves made in this game.
     */
    private List<Move> history = new ArrayList<>();
    /**
     * Keeps track of which castling moves are available.
     */
    private Set<Piece> availableCastles = EnumSet.noneOf(Piece.class);
    /**
     * If set, identifies the square that can be moved to in an en passant during the next move.
     */
    private Square enPassantTarget;
    private Thread moveGeneratorWorker;
    private boolean areMovesGenerated;
    private boolean isGameStopped = true;

    /**
     * Construct the Game.
     *
     * @param whiteName the name of the player using the white pieces
     * @param blackName the name of the player using the black pieces
     */
    public Game(String whiteName, String blackName) {
        this(whiteName, blackName, DEFAULT_STARTING_FEN);

    }

    public Game(String whiteName, String blackName, String startingFen) {
        this.whiteName = whiteName;
        this.blackName = blackName;
        this.startingFen = startingFen;
        parseFen(startingFen);
        generateMoves();
    }

    /**
     * Parse a FEN into data structures to be used by this class.
     * <p>
     * The FEN includes board position, castling availability, en passant target, the number of halfmoves
     * since the last capture or pawn advance, and the number of full moves that have been played so far.
     *
     * @param fenInitialPosition the Forsyth-Edwards notation that represents the chess game's current state.
     */
    private void parseFen(String fenInitialPosition) {

        String[] fenComponents = fenInitialPosition.trim().split("\\s+");
        if (fenComponents.length != 6) {
            throw new IllegalArgumentException("FEN must consist of at least 6 components.");
        }

        Board tempBoard = new Board(fenComponents[0]);

        Color playerToMove = Color.from(fenComponents[1]);

        Set<Piece> availableCastles = parseCastlingAvailability(fenComponents[2]);

        Square enPassantTarget = parseEnPassantTarget(fenComponents[3]);

        // TODO halfmove and fullmove

        board = tempBoard;
        verificationBoard = new Board(fenComponents[0]);
        this.playerToMove = playerToMove;
        this.availableCastles = availableCastles;
        this.enPassantTarget = enPassantTarget;

    }

    /**
     * Interprets the castling availability component of a FEN string
     * <p>
     * The returned Set contains {@link Piece}s that correspond to each of the four possible castles
     * ({@link Piece#WHITE_KING} indicates white still has a kingside castle, {@link Piece#BLACK_QUEEN} indicates
     * black still has a queenside castle).
     *
     * @param availableCastlesNotation the notation to parse
     * @return a Set of {@link Piece}s that indicates which castles are still available
     */
    private static Set<Piece> parseCastlingAvailability(String availableCastlesNotation) {
        Set<Piece> availableCastles = EnumSet.noneOf(Piece.class);
        if (availableCastlesNotation.length() > 4) {
            throw new IllegalArgumentException(String.format(
                "%s is too long to fit the format of castling " + "availability", availableCastlesNotation));
        }

        if (availableCastlesNotation.equals("-")) {
            return availableCastles;
        }

        for (int i = 0; i < availableCastlesNotation.length(); i++) {
            String notation = availableCastlesNotation.substring(i, i + 1);
            Piece availableCastle = Piece.fromFen(notation);
            availableCastles.add(availableCastle);
        }
        return availableCastles;
    }

    /**
     * Interprets the en passant target component of a FEN string.
     *
     * @param enPassantTargetNotation the notation to parse
     * @return the {@link Square} that the pawn performing the en passant will end up on or null if the notation does
     * not indicate a potential en passant
     */
    private static Square parseEnPassantTarget(String enPassantTargetNotation) {
        if (enPassantTargetNotation.equals("-")) {
            return null;
        }

        Square enPassantTarget;
        try {
            enPassantTarget = Square.valueOf(enPassantTargetNotation.trim().toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(enPassantTargetNotation + " is not a valid square.");
        }

        if (enPassantTarget.rank != 2 && enPassantTarget.rank != 5) {
            throw new IllegalArgumentException(
                enPassantTargetNotation + " is not a valid square for an en passant capture.");
        }
        return enPassantTarget;
    }


    private void generateMoves() {
        if (!nextPossibleMoves.isEmpty()) {
            return;
        }
        nextPossibleMoves.addAll(generateBishopMoves(playerToMove, board));
        nextPossibleMoves.addAll(generateRookMoves(playerToMove, board));
        nextPossibleMoves.addAll(generateKnightMoves(playerToMove, board));
        nextPossibleMoves.addAll(generateQueenMoves(playerToMove, board));
        nextPossibleMoves.addAll(generateKingMoves(playerToMove, board));
        nextPossibleMoves.addAll(generatePawnMoves(playerToMove, board, enPassantTarget));
        nextPossibleMoves.addAll(generateCastles(playerToMove, board, availableCastles));

        filterOutIllegalMoves(nextPossibleMoves, board, verificationBoard);
    }

    /**
     * Make the specified move.
     *
     * @param moveNotation algebraic notation describing the desired move to make
     */
    public MoveResult move(String moveNotation) {
        MoveResult result = new MoveResult();
        InterpretedNotation parsedMove;
        try {
            parsedMove = NotationParser.parseMove(moveNotation);
        }
        catch (ParseException ex) {
            String errorMessage = String.format(Locale.US, "%s at position %d", ex.getMessage(), ex.getErrorOffset());
            result.addError(errorMessage);
            return result;
        }


        Optional<Move> foundMove = getMove(parsedMove);
        if (!foundMove.isPresent()) {
            result.addError("No such move: " + moveNotation);
            nextPossibleMoves.clear();
            return result;
        }

        Move moveToMake = foundMove.get();
        moveToMake.makeMoveOnBoard(board);
        if (moveToMake instanceof KingsideCastle || moveToMake instanceof QueensideCastle) {
            availableCastles.remove(Piece.from(playerToMove, Role.KING));
            availableCastles.remove(Piece.from(playerToMove, Role.QUEEN));
        }
        if (moveToMake.getMovingPiece().getRole() == Role.KING) {
            availableCastles.remove(Piece.from(playerToMove, Role.KING));
            availableCastles.remove(Piece.from(playerToMove, Role.QUEEN));
        }
        Square moveSource = moveToMake.getSource();
        if (moveSource == Square.of(0, playerToMove.backRank())) {
            availableCastles.remove(Piece.from(playerToMove, Role.QUEEN));
        }
        if (moveSource == Square.of(7, playerToMove.backRank())) {
            availableCastles.remove(Piece.from(playerToMove, Role.KING));
        }

        int opponentBackRank = playerToMove.opposite().backRank();
        Square moveDestination = moveToMake.getDestination();
        if (moveDestination == Square.of(0, opponentBackRank)) {
            availableCastles.remove(Piece.from(playerToMove.opposite(), Role.QUEEN));
        }
        if (moveDestination == Square.of(7, opponentBackRank)) {
            availableCastles.remove(Piece.from(playerToMove.opposite(), Role.KING));
        }
        enPassantTarget = null;
        if (moveToMake.getMovingPiece().getRole() == Role.PAWN
            && Math.abs(moveToMake.getDestination().rank - moveToMake.getSource().rank) == 2) {
            int rankOfEnPassantTarget = (moveToMake.getDestination().rank + moveToMake.getSource().rank) / 2;
            enPassantTarget = Square.of(moveToMake.getDestination().file, rankOfEnPassantTarget);
        }
        playerToMove = playerToMove.opposite();

        nextPossibleMoves.clear();
        generateMoves();
        if (nextPossibleMoves.isEmpty()) {
            result.addWarning("Checkmate!");
        }
        return result;


    }

    private Optional<Move> getMove(InterpretedNotation parsedMove) {
        Set<Move> matchedMoves =
            nextPossibleMoves.stream().filter(move -> move.isAMatch(parsedMove)).collect(Collectors.toSet());

        if (matchedMoves.size() == 1) {
            return Optional.of(matchedMoves.iterator().next());
        }
        else {
            return Optional.empty();
        }
    }


    private static boolean isKingInCheck(Color color, Board activeBoard) {
        Square kingLocation = activeBoard.getPieceLocations(Piece.from(color, Role.KING)).iterator().next();
        return isAttackedByAnyEnemy(kingLocation, Color.oppositeOf(color), activeBoard);
    }


    private static boolean willOwnKingBeInCheckAfterMove(Move move, Board activeBoard) {
        move.makeMoveOnBoard(activeBoard);
        return isKingInCheck(move.getMovingPiece().getColor(), activeBoard);
    }

    private static void filterOutIllegalMoves(Set<Move> moves, final Board actualBoard, Board verificationBoard) {
        for (Iterator<Move> moveIterator = moves.iterator(); moveIterator.hasNext(); ) {
            Move move = moveIterator.next();
            verificationBoard.replaceBoardWith(actualBoard);
            if (willOwnKingBeInCheckAfterMove(move, verificationBoard)) {
                moveIterator.remove();
            }
        }
    }

    private static boolean isAttackedByAnyEnemy(Square target, Color enemyPlayer, Board activeBoard) {
        for (Move move : generateRookMoves(enemyPlayer, activeBoard)) {
            if (move.getDestination() == target) {
                return true;
            }
        }
        for (Move move : generateKnightMoves(enemyPlayer, activeBoard)) {
            if (move.getDestination() == target) {
                return true;
            }
        }
        for (Move move : generateBishopMoves(enemyPlayer, activeBoard)) {
            if (move.getDestination() == target) {
                return true;
            }
        }
        for (Move move : generateQueenMoves(enemyPlayer, activeBoard)) {
            if (move.getDestination() == target) {
                return true;
            }
        }
        for (Move move : generatePawnMoves(enemyPlayer, activeBoard, null)) {
            if (move.getDestination() == target) {
                return true;
            }
        }
        for (Move move : generateKingMoves(enemyPlayer, activeBoard)) {
            if (move.getDestination() == target) {
                return true;
            }
        }
        // none of enemy's moves attack the target
        return false;
    }


    private static Set<Move> generateCastles(Color kingColor, Board targetBoard, Set<Piece> availableCastles) {
        Piece king = Piece.from(kingColor, Role.KING);
        Set<Move> possibleMoves = new HashSet<>();

        // create a kingside castle if it exists
        Square kingsidePathFirst = Square.of(5, kingColor.backRank());
        Square kingsidePathSecond = Square.of(6, kingColor.backRank());
        if (availableCastles.contains(king) && targetBoard.getPieceOn(kingsidePathFirst) == null
            && targetBoard.getPieceOn(kingsidePathSecond) == null
            && !isAttackedByAnyEnemy(kingsidePathFirst, Color.oppositeOf(kingColor), targetBoard)
            && !isAttackedByAnyEnemy(kingsidePathSecond, Color.oppositeOf(kingColor), targetBoard)) {
            possibleMoves.add(new KingsideCastle(king, false, false));
        }

        // create a queenside castle if it exists
        Square queensidePathFirst = Square.of(1, kingColor.backRank());
        Square queensidePathSecond = Square.of(2, kingColor.backRank());
        Square queensidePathThird = Square.of(3, kingColor.backRank());
        if (availableCastles.contains(Piece.from(kingColor, Role.QUEEN))
            && targetBoard.getPieceOn(queensidePathFirst) == null && targetBoard.getPieceOn(queensidePathSecond) == null
            && targetBoard.getPieceOn(queensidePathThird) == null
            && !isAttackedByAnyEnemy(queensidePathSecond, Color.oppositeOf(kingColor), targetBoard)
            && !isAttackedByAnyEnemy(queensidePathThird, Color.oppositeOf(kingColor), targetBoard)) {
            possibleMoves.add(new QueensideCastle(king, false, false));
        }
        return possibleMoves;
    }

    /**
     * Gets all the possible places that all the bishops of one color can move to.
     * <p>
     * The color depends on which player's turn it is.
     */
    private static Set<Move> generateBishopMoves(Color bishopColor, Board targetBoard) {
        Piece bishop = Piece.from(bishopColor, Role.BISHOP);
        Set<Move> possibleMoves = new HashSet<>();
        // iterate through each bishop of a color...
        for (Square square : targetBoard.getPieceLocations(bishop)) {
            // ...and for each of the directions that a bishop can move, continue adding squares as possible
            // destinations until the path is blocked
            for (Direction direction : INTERCARDINAL_DIRECTIONS) {
                int file = square.file + direction.file;
                int rank = square.rank + direction.rank;
                while (file >= 0 && file < 8 && rank >= 0 && rank < 8) {
                    Square possibleDestination = Square.of(file, rank);
                    Piece pieceAtDestination = targetBoard.getPieceOn(possibleDestination);
                    if (pieceAtDestination == null || pieceAtDestination.getColor() != bishopColor) {
                        possibleMoves.add(new NormalMove(bishop, square, possibleDestination, pieceAtDestination,
                            false, false));
                    }
                    if (pieceAtDestination != null) {
                        // path is blocked, so stop searching
                        break;
                    }

                    file += direction.file;
                    rank += direction.rank;

                }
            }
        }
        return possibleMoves;
    }


    /**
     * Gets all the possible places that all the rooks of one color can move to.
     * <p>
     * The color depends on which player's turn it is.
     */
    private static Set<Move> generateRookMoves(Color rookColor, Board targetBoard) {
        Piece rook = Piece.from(rookColor, Role.ROOK);
        Set<Move> possibleMoves = new HashSet<>();
        // for each rook of a color...
        for (Square square : targetBoard.getPieceLocations(rook)) {
            // ...and for each of the directions that a rook can move, continue adding squares as possible
            // destinations until the path is blocked
            for (Direction direction : CARDINAL_DIRECTIONS) {
                int file = square.file + direction.file;
                int rank = square.rank + direction.rank;
                while (file >= 0 && file < 8 && rank >= 0 && rank < 8) {
                    Square possibleDestination = Square.of(file, rank);
                    Piece pieceAtDestination = targetBoard.getPieceOn(possibleDestination);
                    if (pieceAtDestination == null || pieceAtDestination.getColor() != rookColor) {
                        possibleMoves.add(new NormalMove(rook, square, possibleDestination, pieceAtDestination, false
                            , false));
                    }
                    if (pieceAtDestination != null) {
                        // path is blocked, so stop searching
                        break;
                    }

                    file += direction.file;
                    rank += direction.rank;

                }
            }
        }
        return possibleMoves;
    }

    private static Set<Move> generateKnightMoves(Color knightColor, Board targetBoard) {
        Piece knight = Piece.from(knightColor, Role.KNIGHT);
        Set<Move> possibleMoves = new HashSet<>();
        // for each knight of a color...
        for (Square square : targetBoard.getPieceLocations(knight)) {
            // ...and for each of the directions that a knight can move, continue adding squares as possible
            // destinations until the path is blocked
            for (Direction direction : KNIGHT_DIRECTIONS) {
                int file = square.file + direction.file;
                int rank = square.rank + direction.rank;
                if (file >= 0 && file < 8 && rank >= 0 && rank < 8) {
                    Square possibleDestination = Square.of(file, rank);
                    Piece pieceAtDestination = targetBoard.getPieceOn(possibleDestination);
                    if (pieceAtDestination == null || pieceAtDestination.getColor() != knightColor) {
                        possibleMoves.add(new NormalMove(knight, square, possibleDestination, pieceAtDestination,
                            false, false));
                    }
                }
            }

        }
        return possibleMoves;
    }


    private static Set<Move> generateKingMoves(Color kingColor, Board targetBoard) {
        Piece king = Piece.from(kingColor, Role.KING);
        Set<Move> possibleMoves = new HashSet<>();
        Square square = targetBoard.getPieceLocations(king).iterator().next();
        for (Direction direction : CARDINAL_AND_INTERCARDINAL_DIRECTIONS) {
            int file = square.file + direction.file;
            int rank = square.rank + direction.rank;
            if (file >= 0 && file < 8 && rank >= 0 && rank < 8) {
                Square possibleDestination = Square.of(file, rank);
                Piece pieceAtDestination = targetBoard.getPieceOn(possibleDestination);
                if (pieceAtDestination == null || pieceAtDestination.getColor() != kingColor) {
                    possibleMoves.add(new NormalMove(king, square, possibleDestination, pieceAtDestination, false,
                        false));
                }
            }
        }
        return possibleMoves;
    }


    private static Set<Move> generateQueenMoves(Color queenColor, Board targetBoard) {
        Piece queen = Piece.from(queenColor, Role.QUEEN);
        Set<Move> possibleMoves = new HashSet<>();
        for (Square square : targetBoard.getPieceLocations(queen)) {
            for (Direction direction : CARDINAL_AND_INTERCARDINAL_DIRECTIONS) {
                int file = square.file + direction.file;
                int rank = square.rank + direction.rank;
                while (file >= 0 && file < 8 && rank >= 0 && rank < 8) {
                    Square possibleDestination = Square.of(file, rank);
                    Piece pieceAtDestination = targetBoard.getPieceOn(possibleDestination);
                    if (pieceAtDestination == null || pieceAtDestination.getColor() != queenColor) {
                        possibleMoves.add(new NormalMove(queen, square, possibleDestination, pieceAtDestination,
                            false, false));
                    }
                    if (pieceAtDestination != null) {
                        // path is blocked, so stop searching
                        break;
                    }
                    file += direction.file;
                    rank += direction.rank;
                }
            }
        }
        return possibleMoves;
    }


    private static Set<Move> generatePawnMoves(Color pawnColor, Board targetBoard, Square enPassantTarget) {
        Piece pawn = Piece.from(pawnColor, Role.PAWN);
        Set<Move> possibleMoves = new HashSet<>();
        for (Square square : targetBoard.getPieceLocations(pawn)) {
            // Check for captures
            for (Direction direction : PAWN_CAPTURE_DIRECTIONS.get(pawnColor)) {
                int file = square.file + direction.file;
                int rank = square.rank + direction.rank;
                if (file < 0 || file > 7 || rank < 0 || rank > 7) {
                    continue;
                }
                Square possibleDestination = Square.of(file, rank);

                // check for en passant
                if (enPassantTarget != null && possibleDestination == enPassantTarget) {

                    Square capturedPieceLocation = Square.of(file, square.rank);
                    EnPassant enPassant = new EnPassant(pawn, square, possibleDestination,
                        targetBoard.getPieceOn(capturedPieceLocation), false, false);
                    possibleMoves.add(enPassant);
                    continue;
                }

                // check for normal capture
                // TODO capture can be promotion

                Piece pieceAtDestination = targetBoard.getPieceOn(possibleDestination);

                if (pieceAtDestination != null && pieceAtDestination.getColor() != pawnColor) {
                    if (rank == 0 || rank == 7) {
                        possibleMoves.add(new Promotion(pawn, square, possibleDestination,
                            Piece.from(pawnColor, Role.QUEEN), pieceAtDestination, false, false));
                        possibleMoves.add(new Promotion(pawn, square, possibleDestination,
                            Piece.from(pawnColor, Role.ROOK), pieceAtDestination, false, false));
                        possibleMoves.add(new Promotion(pawn, square, possibleDestination,
                            Piece.from(pawnColor, Role.BISHOP), pieceAtDestination, false, false));
                        possibleMoves.add(new Promotion(pawn, square, possibleDestination,
                            Piece.from(pawnColor, Role.KNIGHT), pieceAtDestination, false, false));
                    }
                    else {
                        NormalMove capture = new NormalMove(pawn, square, possibleDestination,
                            targetBoard.getPieceOn(possibleDestination), false, false);
                        possibleMoves.add(capture);
                    }
                }
            }

            // Check the one (or two) spaces in front
            Direction moveDirection = PAWN_MOVE_DIRECTIONS.get(pawnColor);
            int file = square.file + moveDirection.file;
            int rank = square.rank + moveDirection.rank;
            if (rank >= 0 && rank <= 7) {
                Square possibleDestination = Square.of(file, rank);
                Piece pieceAtDestination = targetBoard.getPieceOn(possibleDestination);
                if (pieceAtDestination == null) {
                    // Check for promotion
                    if (rank == 0 || rank == 7) {
                        possibleMoves.add(new Promotion(pawn, square, possibleDestination,
                            Piece.from(pawnColor, Role.QUEEN), null, false, false));
                        possibleMoves.add(new Promotion(pawn, square, possibleDestination,
                            Piece.from(pawnColor, Role.ROOK), null, false, false));
                        possibleMoves.add(new Promotion(pawn, square, possibleDestination,
                            Piece.from(pawnColor, Role.KNIGHT), null, false, false));
                        possibleMoves.add(new Promotion(pawn, square, possibleDestination,
                            Piece.from(pawnColor, Role.BISHOP), null, false, false));
                        continue;
                    }
                    possibleMoves.add(new NormalMove(pawn, square, possibleDestination, null, false, false));
                    // If pawn is on its starting square, it can move an additional space forward
                    if (square.rank - moveDirection.rank == 0 || square.rank - moveDirection.rank == 7) {
                        file += moveDirection.file;
                        rank += moveDirection.rank;
                        possibleDestination = Square.of(file, rank);
                        pieceAtDestination = targetBoard.getPieceOn(possibleDestination);
                        if (pieceAtDestination == null) {
                            possibleMoves.add(new NormalMove(pawn, square, possibleDestination, null, false, false));
                        }
                    }

                }
            }


        }

        return possibleMoves;
    }

    /**
     * Output the state of the game in Forsyth-Edwards Notation (FEN).
     *
     * @return a FEN record in a String
     */
    public String toFenNotation() {
        StringBuilder piecePlacement = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--) {
            if (rank < 7) {
                piecePlacement.append("/");
            }
            int emptySquareCount = 0;
            for (int file = 0; file <= 7; file++) {
                Piece pieceAtLocation = board.getPieceOn(Square.of(file, rank));
                if (pieceAtLocation != null) {
                    if (emptySquareCount > 0) {
                        piecePlacement.append(emptySquareCount);
                        emptySquareCount = 0;
                    }
                    piecePlacement.append(pieceAtLocation.getFenNotation());

                }
                else {
                    emptySquareCount++;
                }
            }
            if (emptySquareCount > 0) {
                piecePlacement.append(emptySquareCount);
            }

        }

        piecePlacement.append(" ");
        piecePlacement.append(playerToMove.getNotation());

        piecePlacement.append(" ");
        if (availableCastles.isEmpty()) {
            piecePlacement.append("-");
        }
        else {
            for (Piece availableCastles : availableCastles.stream().sorted((o1, o2) -> {
                char o1Value = o1.getFenNotation().charAt(0);
                char o2Value = o2.getFenNotation().charAt(0);
                return Character.compare(o1Value, o2Value);
            }).collect(Collectors.toList())) {
                piecePlacement.append(availableCastles.getFenNotation());
            }
        }

        piecePlacement.append(" ");
        piecePlacement.append(enPassantTarget != null ? enPassantTarget.toString().toLowerCase() : "-");
        piecePlacement.append(" - -");
        // TODO other information
        return piecePlacement.toString();

    }


    // TODO utility function to map white to rank 0 and black to rank 7

}
