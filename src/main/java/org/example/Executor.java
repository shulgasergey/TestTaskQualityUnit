package org.example;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.IntStream;


public class Executor {
    ArrayList<String> finalAnswer = new ArrayList<>();

    public static void main(String[] args) {
        String[] input = {
                "C 1.1 8.15.1 P 15.10.2012 83",
                "C 1 10.1 P 01.12.2012 65",
                "C 1.1 5.5.1 P 01.11.2012 117",
                "D 1.1 8 P 01.01.2012-01.12.2012",
                "C 3 10.2 N 02.10.2012 100",
                "D 1 * P 8.10.2012-20.11.2012",
                "D 3 10 P 01.12.2012"
        };
        Executor executor = new Executor();
        executor.executor(reformatInputData(input));
    }

    // Splitting a one-dimensional array into a two-dimensional
    private static String[][] reformatInputData(String[] inputData) {
        String[][] data = new String[inputData.length][];

        for (int i = 0; i < inputData.length; i++) {
            data[i] = inputData[i].split(" ");
        }

        return data;
    }

    // Go through the entire input array, find D requests and process them
    private void executor(String[][] inputData) {
        // Проверка входных данных на null
        if (inputData.length == 0) {
            System.out.println("Input data is null!");
            return;
        }

        IntStream.range(0, inputData.length)
                .filter(indexOfD -> Objects.equals(inputData[indexOfD][0], "D"))
                .forEach(indexOfD -> findMatchingData(inputData, indexOfD));


        printResult(finalAnswer);
    }

    private void findMatchingData(String[][] inputData, int indexOfD) {
        ArrayList<String[]> result = new ArrayList<>();

        for (String[] input : inputData) {
            if (input[0].equals("C")) {
                if (matchesService(input[1], inputData[indexOfD][1])) {
                    if (matchesQuestionType(input[2], inputData[indexOfD][2])) {
                        if (matchesResponseType(input[3], inputData[indexOfD][3])) {
                            if (matchesDate(input[4], inputData[indexOfD][4])) {
                                result.add(input);
                            }
                        }
                    }
                }
            }
        }

        getResponse(result);
    }

    // Getting the final result from the currently passed D query
    private void getResponse(ArrayList<String[]> result) {
        // If input is empty -> response "-"
        // If input contains only 1 line -> response 5th index (time)
        // If input contains more than 1 line -> response is the average value of 5 indexes (time)
        String response = result.isEmpty() ? "-" : result.size() == 1 ? result.get(0)[5] : String.valueOf(getAverage(result));

        // Write the result to the list of answers
        finalAnswer.add(response);
    }

    /*
     * The second and third parameters are checked using the following algorithm
     * Each item corresponds to a parameter of the opposite request with a similar number
     * <p>
     * If request D contains "*" -> all variants of request C are passed;
     * If the exact request D is specified (with a dot) -> only the same variants of request C are passed;
     * If an inaccurate query D is specified (without a dot) -> variants of query C with the same beginning are passed;
     * If a relatively precise request D is specified (with one dot) (ex 1.1) -> the same options for request C are suitable,
     * and related (ex 1.1(+), 1.1.5(+)).
     */

    // Checking the second parameter
    private boolean matchesService(String cService, String dService) {
        return dService.equals("*") || cService.equals(dService) || (dService.length() == 1 && cService.startsWith(dService) || (cService.startsWith(dService + ".")));
    }

    // Checking the third parameter
    private boolean matchesQuestionType(String cQuestionType, String dQuestionType) {
        return dQuestionType.equals("*") || cQuestionType.equals(dQuestionType) || (dQuestionType.length() == 1 && cQuestionType.startsWith(dQuestionType) || (cQuestionType.startsWith(dQuestionType + ".")));
    }

    // Checking the fourth parameter
    private boolean matchesResponseType(String cResponseType, String dResponseType) {
        return cResponseType.equals(dResponseType);
    }

    // Checking for coincidence of dates, or for finding a date in the required period
    private boolean matchesDate(String cDate, String dDate) {
        try {
            if (dDate.contains("-")) {
                String[] datesOfD = dDate.split("-");
                LocalDate startDate = LocalDate.parse(datesOfD[0], customDateFormatter());
                LocalDate endDate = LocalDate.parse(datesOfD[1], customDateFormatter());
                LocalDate inputDate = LocalDate.parse(cDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                return inputDate.isAfter(startDate) && inputDate.isBefore(endDate.plusDays(1));
            } else return cDate.equals(dDate);

        } catch (DateTimeParseException e) {
            System.err.println("Date parsing error: " + e.getMessage());
            return false;
        }
    }

    // Calculation of the average time value if, after processing D request, several responses were received
    private int getAverage(ArrayList<String[]> result) {
        try {
            int sum = 0;
            for (String[] line : result) {
                sum += Integer.parseInt(line[5]);
            }
            return sum / result.size();
        } catch (NumberFormatException e) {
            System.err.println("Number format exception " + e.getMessage());
            return -1;
        }
    }

    // Outputting the final result to the console
    private void printResult(ArrayList<String> result) {
        for (String x : result) {
            System.out.println(x);
        }
    }

    // Set custom date format for input
    // Input format d.mm.yyyy or dd.mm.yyyy
    private DateTimeFormatter customDateFormatter() {
        return new DateTimeFormatterBuilder()
                .appendPattern("[d.]MM.uuuu")
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .toFormatter();
    }
}