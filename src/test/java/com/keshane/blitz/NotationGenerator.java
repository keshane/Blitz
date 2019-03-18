package com.keshane.blitz;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Contains methods to generate algebraic notation of chess moves.
 */
public class NotationGenerator {
    private static String[] FILE_NOTATIONS = new String[]{"a", "b", "c", "d", "e", "f", "g", "h"};
    private static String[] RANK_NOTATIONS = new String[]{"1", "2", "3", "4", "5", "6", "7", "8"};
    private static String[] SOURCE_FILE_NOTATIONS = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", ""};
    private static String[] SOURCE_RANK_NOTATIONS = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", ""};
    private static String[] PIECE_NOTATIONS = new String[]{"R", "N", "B", "Q", "K", ""};
    private static String[] PROMOTION_PIECE_NOTATIONS = new String[]{"R", "N", "B", "Q"};
    private static String[] CAPTURE_NOTATIONS = new String[]{"x", ""};
    private static String[] CHECK_NOTATIONS = new String[]{"+", "#", ""};


    public static void main(String[] args) throws Exception {
        switch (args[0]) {
            case "--valid":
                generateValidNotation(args[1]);
                break;
            case "--invalid":
                throw new NotImplementedException(); // TODO
            default:
                throw new IllegalArgumentException(String.format("Did not recognize flag %s", args[0]));

        }

    }

    /**
     * Generates all (?) the valid notations that should be expected by the NotationParser.
     * <p>
     * Note that this can still include illegal moves. Only the syntax is checked. Semantics are checked in the game
     * logic.
     *
     * @param fileLocation the file to write all the valid notations to
     * @throws Exception if the file is not valid
     */
    private static void generateValidNotation(String fileLocation) throws Exception {
        try (BufferedWriter validNotationFileWriter = new BufferedWriter(new FileWriter(fileLocation))) {
            StringBuilder notation = new StringBuilder(12);
            for (String piece : PIECE_NOTATIONS) {
                for (String sourceFile : SOURCE_FILE_NOTATIONS) {
                    for (String sourceRank : SOURCE_RANK_NOTATIONS) {
                        for (String capture : CAPTURE_NOTATIONS) {
                            // captures must be specified by a piece or - in the case of pawns - the file the pawn
                            // originated on
                            if (capture.equals("x") && sourceFile.isEmpty() && sourceRank.isEmpty() && piece.isEmpty()) {
                                continue;
                            }
                            for (String file : FILE_NOTATIONS) {
                                for (String rank : RANK_NOTATIONS) {
                                    for (String check : CHECK_NOTATIONS) {
                                        notation.append(piece);
                                        notation.append(sourceFile);
                                        notation.append(sourceRank);
                                        notation.append(capture);
                                        notation.append(file);
                                        notation.append(rank);
                                        notation.append(check);
                                        validNotationFileWriter.write(notation.toString());
                                        validNotationFileWriter.newLine();
                                        notation.setLength(0);

                                        // consider some special cases for pawns
                                        if (piece.isEmpty()) {
                                            // en passants
                                            notation.append(piece);
                                            notation.append(sourceFile);
                                            notation.append(sourceRank);
                                            notation.append(capture);
                                            notation.append(file);
                                            notation.append(rank);
                                            notation.append("e.p.");
                                            notation.append(check);
                                            validNotationFileWriter.write(notation.toString());
                                            validNotationFileWriter.newLine();
                                            notation.setLength(0);

                                            for (String promotionPiece : PROMOTION_PIECE_NOTATIONS) {
                                                notation.append(piece);
                                                notation.append(sourceFile);
                                                notation.append(sourceRank);
                                                notation.append(capture);
                                                notation.append(file);
                                                notation.append(rank);
                                                notation.append("=");
                                                notation.append(promotionPiece);
                                                notation.append(check);
                                                validNotationFileWriter.write(notation.toString());
                                                validNotationFileWriter.newLine();
                                                notation.setLength(0);
                                            }


                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }

    }
}