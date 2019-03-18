package com.keshane.blitz;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

public class GameTest {
    private Game chessGame;

    @Before
    public void setupTest() {
        chessGame = new Game("white player", "black player");
    }

    @Test
    public void fenNotationStartPositionTest() {
        String expectedFenNotation = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - - -";
        String actualFenNotation = chessGame.toFenNotation();
        Assert.assertEquals(expectedFenNotation, actualFenNotation);
    }

    @Test
    public void fenNotationE4Test() {
        chessGame.move("e4");
        String expectedFenNotation = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 - -";
        String actualFenNotation = chessGame.toFenNotation();
        Assert.assertEquals(expectedFenNotation, actualFenNotation);
    }

    @Test
    public void scholarsMateTest() {
        chessGame.move("e4");
        chessGame.move("e5");
        chessGame.move("Bc4");
        chessGame.move("Nc6");
        chessGame.move("Qh5");
        chessGame.move("Nf6");
        chessGame.move("Qxf7");
        String expectedFenNotation = "r1bqkb1r/pppp1Qpp/2n2n2/4p3/2B1P3/8/PPPP1PPP/RNB1K1NR b KQkq - - -";
        String actualFenNotation = chessGame.toFenNotation();
        Assert.assertEquals(expectedFenNotation, actualFenNotation);
    }

    @Test
    public void pawnCaptureTest() {
        chessGame.move("e4");
        chessGame.move("d5");
        chessGame.move("exd5");
        String expectedFenNotation = "rnbqkbnr/ppp1pppp/8/3P4/8/8/PPPP1PPP/RNBQKBNR b KQkq - - -";
        String actualFenNotation = chessGame.toFenNotation();
        Assert.assertEquals(expectedFenNotation, actualFenNotation);
    }

    @Test
    public void initialFenTest() {
        String initialFen = "8/8/8/1B2K3/pk6/8/8/P1P4Q w KQkq - - -";
        chessGame = new Game("white name", "black name", initialFen);
        String actualFen = chessGame.toFenNotation();
        Assert.assertEquals(initialFen, actualFen);
    }

    @Test
    public void castleTest() {
        chessGame.move("e4");
        chessGame.move("d5");
        chessGame.move("Nf3");
        chessGame.move("Nc6");
        chessGame.move("Be2");
        chessGame.move("Be6");
        chessGame.move("O-O");
        chessGame.move("Qd7");
        chessGame.move("Re1");
        chessGame.move("O-O-O");
        String expectedFenNotation = "2kr1bnr/pppqpppp/2n1b3/3p4/4P3/5N2/PPPPBPPP/RNBQR1K1 w - - - -";
        String actualFenNotation = chessGame.toFenNotation();
        Assert.assertEquals(expectedFenNotation, actualFenNotation);
    }

    @Test
    public void validGamesTest() throws Exception {

        StringBuilder gameMoves = new StringBuilder();
        try (BufferedReader testCasesReader = new BufferedReader(new FileReader("src/test/resources/KingBase2019-A80"
            + "-A99-clean.pgn"))) {

            Game validGame = new Game("white", "black)");
            String line = testCasesReader.readLine();
            int gameCount = 0;
            MoveResult lastResult = null;
            while (line != null) {
                if (line.isEmpty()) {
                    Assert.assertTrue(lastResult.isSuccess() || lastResult.getWarnings().get(0).equals("Checkmate!"));
                    validGame = new Game("white", "black");
                    gameMoves.setLength(0);
                    gameCount++;
                    if (gameCount % 10 == 0) {
                        System.out.println(gameCount);
                    }
                    line = testCasesReader.readLine();
                    continue;
                }
                String[] fullMove = line.split("\\s+");
                lastResult = validGame.move(fullMove[0]);
                gameMoves.append(fullMove[0]);
                Assert.assertTrue(!lastResult.hasErrors());
                if (fullMove.length > 1) {
                    lastResult = validGame.move(fullMove[1]);
                    gameMoves.append("\t");
                    gameMoves.append(fullMove[1]);
                    gameMoves.append("\n");
                    Assert.assertTrue(!lastResult.hasErrors());
                }

                line = testCasesReader.readLine();
            }

        }
        catch (AssertionError ex) {
            System.out.println(gameMoves.toString());
            throw ex;
        }
    }
}
