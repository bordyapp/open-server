package io.bordy.kanban.utils;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

/**
 * https://habr.com/ru/articles/510448/
 */
@ApplicationScoped
public class Lexorank {

    public static final String START_POSITION = "aaa";
    public static final String END_POSITION = "zzz";
    private static final int ALPHABET_SIZE = 26;
    private static final int TASK_FOR_PROJECT_LIMIT_TOTAL = 100;

    public List<String> getDefaultRank(int forNumOfTasks) {
        final String startPos = START_POSITION;
        final String endPos = END_POSITION;

        final int startCode = startPos.codePointAt(0);
        final int endCode = endPos.codePointAt(0);

        final int diffInOneSymb = endCode - startCode;

        final int totalDiff = diffInOneSymb + diffInOneSymb * ALPHABET_SIZE + diffInOneSymb * ALPHABET_SIZE * ALPHABET_SIZE;
        final int diffForOneItem = totalDiff / (TASK_FOR_PROJECT_LIMIT_TOTAL + 1);

        final List<Integer> diffForSymbols = new ArrayList<>();
        diffForSymbols.add(diffForOneItem % ALPHABET_SIZE);
        diffForSymbols.add(diffForOneItem / ALPHABET_SIZE % ALPHABET_SIZE);
        diffForSymbols.add(diffForOneItem / (int) Math.pow(ALPHABET_SIZE, 2) % ALPHABET_SIZE);

        final List<String> positions = new ArrayList<>();
        String lastAddedElement = startPos;
        for (int ind = 0; ind < forNumOfTasks; ind++) {
            int offset = 0;
            String newElement = "";
            for (int index = 0; index < 3; index++) {
                final int diffInSymbols = diffForSymbols.get(index);

                int newElementCode = lastAddedElement.codePointAt(2 - index) + diffInSymbols;
                if (offset != 0) {
                    newElementCode += 1;
                    offset = 0;
                }
                if (newElementCode > 'z') {
                    offset += 1;
                    newElementCode -= ALPHABET_SIZE;
                }
                final char symbol = (char) newElementCode;
                newElement += symbol;
            }

            newElement = new StringBuilder(newElement).reverse().toString();
            positions.add(newElement);
            lastAddedElement = newElement;
        }

        positions.sort(null);
        positions.forEach(System.out::println);
        return positions;
    }

    public String getRankBetween(String firstRank, String secondRank) {
        assert firstRank.compareTo(secondRank) < 0 : "First position must be lower than second. Got firstRank " + firstRank + " and second rank " + secondRank;

        // Make positions equal
        while (firstRank.length() != secondRank.length()) {
            if (firstRank.length() > secondRank.length())
                secondRank += "a";
            else
                firstRank += "a";
        }

        int[] firstPositionCodes = new int[firstRank.length()];
        for (int i = 0; i < firstRank.length(); i++) {
            firstPositionCodes[i] = firstRank.charAt(i);
        }

        int[] secondPositionCodes = new int[secondRank.length()];
        for (int i = 0; i < secondRank.length(); i++) {
            secondPositionCodes[i] = secondRank.charAt(i);
        }

        int difference = 0;

        for (int index = firstPositionCodes.length - 1; index >= 0; index--) {
            // Codes of the elements of positions
            int firstCode = firstPositionCodes[index];
            int secondCode = secondPositionCodes[index];

            // i.e. ' a < b '
            if (secondCode < firstCode) {
                // ALPHABET_SIZE = 26 for now
                secondCode += ALPHABET_SIZE;
                secondPositionCodes[index - 1] -= 1;
            }

            // formula: x = a * size^0 + b * size^1 + c * size^2
            final int powRes = (int) Math.pow(ALPHABET_SIZE, firstRank.length() - index - 1);
            difference += (secondCode - firstCode) * powRes;
        }

        String newElement = "";
        if (difference <= 1) {
            // add middle char from alphabet
            newElement = firstRank + (char) ('a' + ALPHABET_SIZE / 2);
        } else {
            difference /= 2;

            int offset = 0;
            for (int index = 0; index < firstRank.length(); index++) {
                // formula: x = difference / (size^place - 1) % size;
                // i.e. difference = 110, size = 10, we want place 2 (middle),
                // then x = 100 / 10^(2 - 1) % 10 = 100 / 10 % 10 = 11 % 10 = 1
                final int diffInSymbols = difference / (int) Math.pow(ALPHABET_SIZE, index) % (ALPHABET_SIZE);

                int newElementCode = firstRank.charAt(secondRank.length() - index - 1) + diffInSymbols + offset;
                offset = 0;

                // if newElement is greater then 'z'
                if (newElementCode > 'z') {
                    offset++;
                    newElementCode -= ALPHABET_SIZE;
                }

                newElement += (char) newElementCode;
            }

            newElement = new StringBuilder(newElement).reverse().toString();
        }

        return newElement;
    }

}
