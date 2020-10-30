import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class SqlParser_alpha {
    public static final String URL = "https://www.sql.ru/forum/job-offers"; // url constant

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nYou have too much free time? Well, let`s find you a new job! :)\nEnter url(sql.ru), keyword, period(months)");

        while (true) {
            String[] input = scanner.nextLine().replaceAll(" ", "").split(","); // get formatted input

            if (input[0].equals("q") || input[0].equals("Q")) { // if 'q' - quit the program
                System.out.println("\nbye-bye");
                break;

            } else if (checkInput(input)) {
                getVacancies(input);
                break;
            }
        }
    }

    public static boolean checkInput(String[] inputArray) {
        boolean isCorrect = true;

        if (!inputArray[0].equalsIgnoreCase("sql.ru")) { // check link
            System.out.println("\nWrong url\nExample: sql.ru");
            isCorrect = false;

        } else if (inputArray.length == 3) { // if the user entered one keyword
            try { // try to parse the last element(period) to int and check it
                if (Integer.parseInt(inputArray[2]) > 12 || Integer.parseInt(inputArray[2]) < 1) {
                    System.out.println("\nPeriod must be in months");
                    isCorrect = false;
                }
            } catch (NumberFormatException e){ // if period is entered in words
                System.out.println("\nPeriod must be in months");
                isCorrect = false;
            }
        } else if (inputArray.length == 4) {// if the user entered two keyword
            try { // try to parse period to int and check it
                if (Integer.parseInt(inputArray[3]) > 12 || Integer.parseInt(inputArray[3]) < 1) {
                    System.out.println("\nPeriod must be in months");
                    isCorrect = false;
                }
            } catch (NumberFormatException e){ // if period is entered in words
                System.out.println("\nPeriod must be in months");
                isCorrect = false;
            }
        } else { // if inputArray.length != 3 && inputArray.length != 4
            System.out.println("\nNeeded format:\nurl, keyword(s), period(months) - sql.ru, java, (python,) 1");
            isCorrect = false;
        }
        return isCorrect;
    }

    public static void getVacancies(String[] inputArray) {
        System.out.println("\nreceiving data...");

        List<Vacancy> vacancyList = new ArrayList<>(); // here we`ll put our vacancies

        Calendar calendar = Calendar.getInstance(); // to get the current month and then the previous ones
        SimpleDateFormat sdf = new SimpleDateFormat("MMM"); // format the date

        Map<String, String> months = listOfMonths(); // create a list of months

        int inputMonths = Integer.parseInt(inputArray[inputArray.length - 1]); // period the user entered
        String[] keyWords = inputArray.length == 3? new String[]{inputArray[1], null}: new String[]{inputArray[1], inputArray[2]}; // keywords the user entered

        int monthCounter = 0; // when the month changes
        int pageCounter = 1; // when the page changes
        int pageCeil = 2; // the page ceiling for each month

        try {
            while (monthCounter != inputMonths) {

                if (pageCounter > pageCeil) { // if we reach the ceil
                    calendar.add(Calendar.MONTH, -1); // set the previous month from the current
                    monthCounter++; // when the vacancies ran out this month
                    pageCeil += 2; // add two pages because we set the previous month
                    pageCounter = 1; // reset 'pageCounter' because every new month we check from the first page

                } else {
                    Document doc = Jsoup.connect(URL + "/" + pageCounter).get(); // connect to the page
                    Elements tdElements = doc.getElementsByAttributeValue("class", "postslisttopic"); // now 'tdElements' has all vacancies on the page

                    for (Element tdElement: tdElements) { // check each vacancy
                        if (checkByWords(tdElement, keyWords) && checkByDate(months, calendar, sdf, tdElement, inputMonths)) {

                            String link = tdElement.child(0).attr("href");
                            String title = tdElement.child(0).text();

                            vacancyList.add(new Vacancy(link, title));
                        }
                    }
                    pageCounter++; // 'tdElements' variable contains all the vacancies on the page, so after 'for' loop we end the page
                }
            }
            printToFile(vacancyList); // after 'while' loop all the vacancies we found are in the list, so let`s print it to the file

        } catch (IOException e) {
            System.out.println("\nSome input-output Exception in 'getVacancies' method: " + e.getMessage());
        }
    }

    public static void printToFile(List<Vacancy> vacancyList) {
        Path path = Paths.get("C:/Users/Daniyar/Desktop/sqlru.txt");

        try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset())) {
            for (Vacancy vacancy: vacancyList) {
                writer.write(String.valueOf(vacancy));
            }
            writer.close();

            System.out.println(Files.newBufferedReader(path, Charset.defaultCharset()).readLine() != null? "\nAll the vacancies have been written to the file": "\nThere are no vacancies with such keywords during this period");

        } catch (IOException e) {
            System.out.println("\nSome input-output Exception in 'printToFile' method: " + e.getMessage());
        }
    }

    public static boolean checkByDate(Map<String, String> months, Calendar calendar, SimpleDateFormat sdf, Element tdElement, int monthCounter) {
        boolean exists = false;
        try {
            Document dateDoc = Jsoup.connect(tdElement.child(0).attr("href")).get(); // connect to vacancy page
            Elements dateTdElements = dateDoc.getElementsByAttributeValue("class", "msgFooter"); // get vacancy posting date

            String[] vacancyDate = dateTdElements.text().split(" ");

            // "сегодня,", "вчера," can only be in the current month, so if 'monthCounter' != 0, we only check the date with the month name
            if (vacancyDate[0].equals("сегодня,") || vacancyDate[0].equals("вчера,") && monthCounter == 0 || vacancyDate[1].equals(months.get(sdf.format(calendar.getTime())))) {
                exists = true;
            }

        } catch (IOException e) {
            System.out.println("\nSome input-output Exception in 'checkByDate' method: " + e.getMessage());
        }

        return exists;
    }

    public static boolean checkByWords(Element tdElement, String[] keyWords) {
        boolean exists = false;

        // split the vacancy title into separated words and check each one if it matches a keyword
        String[] words = tdElement.text().replaceAll("[^a-zA-Zа-яА-Я ]", " ").split(" ");
        for (String word: words) {
            if (word.equalsIgnoreCase(keyWords[0]) || word.equalsIgnoreCase(keyWords[1])) {
                exists = true;
                break;
            }
        }

        return exists;
    }

    public static Map<String, String> listOfMonths() {
        /*
            keys - month names as in the 'Calendar' class,
            values - month names as on the site

            the user cannot enter a period less than 1, so first we check the vacancies with the current month,
            but then, if the period is greater than 1, we need to check the previous months, but their names on the site are
            different from the names in the 'Calendar' class, so we need to compare them
         */
        Map<String, String> months = new HashMap<>();

        String[] monthNamesInCalendar = {"янв.", "февр.", "март", "апр.", "май", "июнь", "июль", "авг.", "сент.", "окт.", "нояб.", "дек."};
        String[] monthNamesOnSite = {"янв", "фев", "мар", "апр", "май", "июн", "июл", "авг", "сен", "окт", "ноя", "дек"};

        for (int i = 0; i < 12; i++) {
            months.put(monthNamesInCalendar[i], monthNamesOnSite[i]);
        }

        return months;
    }
}

class Vacancy {
    private String url;
    private String title;

    public Vacancy(String url, String title) {
        this.url = url;
        this.title = title;
    }

    @Override
    public String toString() {
        return url + "\n" + title + "\n\n";
    }
}