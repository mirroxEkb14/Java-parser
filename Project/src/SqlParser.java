import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Scanner;

import java.util.Map;
import java.util.HashMap;

import java.util.Calendar;
import java.text.SimpleDateFormat;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;


public class SqlParser {
    public static final String URL = "https://www.sql.ru/forum/job-offers"; // url constant
    public static Map<String, String> months; // month names
    public static int inputMonths; // the number of months the user entered

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nYou have too much free time? Well, let`s find you a new job! :)\nEnter url(sql.ru), keyword, period(months)");

        String input;
        while (true) {
            if (checkInput((input = scanner.nextLine()))) {
                getVacancy(input);
                break;
            } else if (input.equals("q") || input.equals("Q")) {
                break;
            }
        }
    }

    public static boolean checkInput(String input) {
        boolean isCorrect = true;

        input = input.replaceAll(" ", "");
        String[] inputArray = input.split(",");
        if (input.equals("q") || input.equals("Q")) { // if 'q' - quit the program
            System.out.println("\nbye-bye");
            isCorrect = false;

        } else if (inputArray.length != 4 && inputArray.length != 3) { // check the input format
            System.out.println("\nNeeded format: url(sql.ru), keyword(java, javascript), period(months) - with ','");
            isCorrect = false;

        } else if (!inputArray[0].equalsIgnoreCase("sql.ru")) { // check url
            System.out.println("\nWrong url\nExample: sql.ru");
            isCorrect = false;

        } else {
            // check if the number of months is correct
            try { // if there is one keyword
                if (Integer.parseInt(inputArray[2]) > 12 || Integer.parseInt(inputArray[2]) < 1) {
                    System.out.println("\nPeriod must be in months");
                    isCorrect = false;
                }
                inputMonths = Integer.parseInt(inputArray[2]);

            } catch (NumberFormatException e) { // if there are two keyword
                if (Integer.parseInt(inputArray[3]) > 12 || Integer.parseInt(inputArray[3]) < 1) {
                    System.out.println("\nPeriod must be in months");
                    isCorrect = false;
                }
                inputMonths = Integer.parseInt(inputArray[3]);
            }
        }
        return isCorrect;
    }

    public static void getVacancy(String input) {
        try {
            System.out.println("\nreceiving data...");

            input = input.replaceAll(" ", ""); // from 'input' we need keyword(s) and period
            String[] inputArray = input.split(",");

            Calendar calendar = Calendar.getInstance(); // to get the current month and then the previous ones
            SimpleDateFormat sdf = new SimpleDateFormat("MMM");

            File file = new File("C:/Users/Daniyar/Desktop/sqlru.txt"); // our text file
            PrintWriter pw = new PrintWriter(file);

            listOfMonths(); // create a list of month names

            Scanner scanner = new Scanner(file); // in case there`ll be no vacancies in the file

            int monthCounter = 0; // when the month changes
            int pageCounter = 1; // when the page changes

            /*
                the vacancies on the site are not in order of dates, so I decided that for each month we`ll look through two pages:
                for the current month - the first 2 pages
                for the previous month - the first 4 pages etc.

                Bad logic but I haven`t come up with a better one yet
             */
            int pageCeil = 3; // the page ceiling for each month
            while (monthCounter != inputMonths) {
                Document doc = Jsoup.connect(URL + "/" + pageCounter).get(); // connect to the page

                Elements tbodyElements = doc.getElementsByTag("tbody"); // find all 'tbody' tags that contain the date

                Elements tdElements = doc.getElementsByAttributeValue("class", "postslisttopic"); // now 'tdElements' has all vacancies on the page

                int vacancyCounter = 4; // the first 4 rows in the table on the site are not vacancies, so we start with 5th row
                int skipCounter = 0; // skip the first 3 rows in the table (these are not vacancies)
                for (Element tdElement: tdElements) { // take each row(vacancy) in the table on the site
                    if (vacancyCounter < tbodyElements.get(2).childrenSize()) { // 'tbodyElements.get(2).childrenSize()' - the number of vacancies on the page
                        if (skipCounter < 3) { // skip the first 3 rows
                            skipCounter++;
                        } else {
                            String[] words = tdElement.text().replaceAll("[^a-zA-Zа-яА-Я ]", " ").split(" "); // find a vacancy by keyword(s)
                            for (String word: words) {
                                if (word.equalsIgnoreCase(inputArray[1]) || word.equalsIgnoreCase(inputArray[2])) {
                                    Document dateDoc = Jsoup.connect(tdElement.child(0).attr("href")).get(); // connect to vacancy page

                                    Elements dateTdElements = dateDoc.getElementsByAttributeValue("class", "msgFooter"); // get vacancy posting date
                                    String[] vacancyDate = dateTdElements.text().split(" ");

                                    if (pageCounter >= pageCeil) { // if we reach the ceil
                                        calendar.add(Calendar.MONTH, -1); // set the previous month from the current
                                        monthCounter++; // when the vacancies ran out this month
                                        pageCeil += 2; // add two pages because we set the previous month
                                        pageCounter = 1; // reset 'pageCounter' because every new month we check from the first page
                                        break;
                                    } else if (vacancyDate[0].equals("сегодня,") || vacancyDate[0].equals("вчера,") || vacancyDate[1].equals(months.get(sdf.format(calendar.getTime())))) {
                                        if (!file.exists()) { // if the file does not exist
                                            new FileWriter(file);
                                        }
                                        // write to the file
                                        pw.println(tdElement.child(0).attr("href")); // link
                                        pw.println(tdElement.child(0).text()); // title
                                        pw.println(vacancyDate[0] + " " + vacancyDate[1] + "\n"); // date
                                    }
                                }
                            }
                            vacancyCounter++;
                            if (pageCounter == pageCeil) {
                                break;
                            }
                        }
                    }
                }
                pageCounter++;
            }
            // checking if the file is empty
            if (scanner.hasNext()) {
                System.out.println("\nAll the vacancies have been written to the file");
            } else {
                System.out.println("\nThere are no vacancies with such keywords during this period");
            }
            pw.close();

        } catch (IOException e) {
            System.out.println("\nSome input-output Exception: " + e.getMessage());
        }
    }

    public static void listOfMonths() {
        /*
            keys - month names as in the 'Calendar' class,
            values - month names as on the site

            the user cannot enter a period less than 1, so first we check the vacancies with the current month,
            but then, if the period is greater than 1, we need to check the previous months, but their names on the site are
            different from the names in the 'Calendar' class, so we need to compare them
         */
        months = new HashMap<>();

        String[] monthNamesInCalendar = {"янв.", "февр.", "март", "апр.", "май", "июнь", "июль", "авг.", "сент.", "окт.", "нояб.", "дек."};
        String[] monthNamesOnSite = {"янв", "фев", "мар", "апр", "май", "июн", "июл", "авг", "сен", "окт", "ноя", "дек"};

        for (int i = 0; i < 12; i++) {
            months.put(monthNamesInCalendar[i], monthNamesOnSite[i]);
        }
    }
}