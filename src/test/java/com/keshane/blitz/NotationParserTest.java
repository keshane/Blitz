package com.keshane.blitz;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class NotationParserTest {
    private List<String> validNotations;

    @Before
    public void readTestCases() throws Exception {
        validNotations = new ArrayList<>();
        File testCaseFile = new File("src/test/resources/validnotation.txt");
        BufferedReader testCasesReader = new BufferedReader(new FileReader(testCaseFile));
        String line;
        while ((line = testCasesReader.readLine()) != null) {
            validNotations.add(line);
        }

    }

    @Test
    public void parseMoveTest() throws Exception {
        for (String notation : validNotations) {
            NotationParser.parseMove(notation);
        }
    }

}
