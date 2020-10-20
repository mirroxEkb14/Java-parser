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
        if (input.equals("q") || input.equals("Q")) {
            System.out.println("\nbye-bye");
            isCorrect = false;

        } else if (inputArray.length != 3) {
            System.out.println("\nNeeded format: url(sql.ru), keyword(java), period(months) - with ','");
            isCorrect = false;

        } else if (!inputArray[0].toLowerCase().equals("sql.ru")) {
            System.out.println("\nWrong url\nExample: sql.ru");
            isCorrect = false;

        } else if (Integer.parseInt(inputArray[2]) > 12 || Integer.parseInt(inputArray[2]) < 1) {
            System.out.println("\nPeriod must be in months");
            isCorrect = false;
        }

        return isCorrect;
    }

    public static void getVacancy(String input) {
        try {
            System.out.println("\nreceiving data...");

            input = input.replaceAll(" ", ""); // from 'input' we need key word and period
            String[] inputArray = input.split(",");

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM");

            File file = new File("C:/Users/Daniyar/Desktop/sqlru.txt"); // our text file
            PrintWriter pw = new PrintWriter(file);

            listOfMonths(); // create a list of month names

            int monthCounter = 0; // when the month changes
            int pageCounter = 1; // when the page changes
            while (monthCounter != Integer.parseInt(inputArray[2])) {
                Document doc = Jsoup.connect(URL + "/" + pageCounter).get(); // connect to the page

                Elements tbodyElements = doc.getElementsByTag("tbody"); // find all 'tbody' tags that contain the date

                Elements tdElements = doc.getElementsByAttributeValue("class", "postslisttopic"); // now 'tdElements' has all vacancies on the page

                int vacancyCounter = 4; // the first 4 rows in the table on the site are not vacancies, so we start with 4th row
                int counter = 0; // skip the first 4 rows in the table (they are not vacancies)
                for (Element tdElement: tdElements) { // take each row(vacancy) in the table on the site
                    if (vacancyCounter < tbodyElements.get(2).childrenSize()) { // 'tbodyElements.get(2).childrenSize()' - the number of vacancies except the first 4 rows
                        if (counter < 3) { // skip the first 4 rows
                            counter++;
                        } else {
                            String[] words = tdElement.text().split(" "); // find a vacancy by keyword
                            for (String word: words) {
                                if (word.equalsIgnoreCase(inputArray[1])) {

                                    System.out.println(java.util.Arrays.toString(words));

                                    Document dateDoc = Jsoup.connect(tdElement.child(0).attr("href")).get(); // connect to vacancy page

                                    Elements dateTdElements = dateDoc.getElementsByAttributeValue("class", "msgFooter"); // get date
                                    String[] vacancyDate = dateTdElements.text().split(" ");

                                    System.out.println(java.util.Arrays.toString(vacancyDate));

                                    // verification: if we get the next month
                                    if (!vacancyDate[0].equals("сегодня,") && !vacancyDate[0].equals("вчера,") && !vacancyDate[1].equals(months.get(sdf.format(calendar.getTime())))) {
                                        monthCounter++;
                                        calendar.add(Calendar.MONTH, -1);

                                        // when the last month the user wants ends: break
                                        if (monthCounter == Integer.parseInt(inputArray[2])) {
                                            break;
                                        }
                                    }

                                    if (vacancyDate[0].equals("сегодня,") || vacancyDate[0].equals("вчера,") || vacancyDate[1].equals(months.get(sdf.format(calendar.getTime())))) {
                                        if (!file.exists()) { // if the file does not exist
                                            new FileWriter(file);
                                        }

                                        // write to file
                                        pw.println(tdElement.child(0).attr("href")); // link
                                        pw.println(tdElement.child(0).text()); // title
                                        pw.println(vacancyDate[0] + " " + vacancyDate[1] + "\n"); // date
                                    }
                                }
                            }
                            vacancyCounter++;
                            if (monthCounter == Integer.parseInt(inputArray[2])) {
                                break;
                            }
                        }
                    }
                }
                pageCounter++;
            }
            pw.close();
            System.out.println("\nAll the vacancies have been written to the file");

        } catch (IOException e) {
            System.out.println("\nSome input-output Exception: " + e.getMessage());
        }
    }

    public static void listOfMonths() {
        // this method is necessary because we need to keep track of the months the user wants
        months = new HashMap<>();

        String[] monthNumbersInCalendar = {"янв.", "февр.", "март", "апр.", "май", "июнь", "июль", "авг.", "сент.", "окт.", "нояб.", "дек."};
        String[] monthNamesOnSite = {"янв", "фев", "мар", "апр", "май", "июн", "июл", "авг", "сен", "окт", "ноя", "дек"};

        for (int i = 0; i < 12; i++) {
            months.put(monthNumbersInCalendar[i], monthNamesOnSite[i]);
        }
    }
}


//        try {
//                input = input.replaceAll(" ", ""); // from 'input' we need key word and period
//                String[] inputArray = input.split(",");
//
//                Calendar calendar = Calendar.getInstance();
//                SimpleDateFormat sdf = new SimpleDateFormat("MMM");
//
//                File file = new File("C:/Users/Daniyar/Desktop/sqlru.txt"); // our text file
//                PrintWriter pw = new PrintWriter(file);
//
//                listOfMonths(); // create a list of month names
//
//                int monthCounter = 0; // when the month changes
//                int pageCounter = 1; // when the page changes
//                while (monthCounter != Integer.parseInt(inputArray[2])) {
//                    Document doc = Jsoup.connect(URL + "/" + pageCounter).get(); // connect to the page
//
//                    Elements tbodyElements = doc.getElementsByTag("tbody"); // find all 'tbody' tags that contain the date
//
//                    Elements tdElements = doc.getElementsByAttributeValue("class", "postslisttopic"); // now 'tdElements' has all vacancies on the page
//
//                    int vacancyCounter = 4; // the first 4 rows in the table on the site are not vacancies, so we start with 4th row
//                    int counter = 0; // skip the first 4 rows in the table (they are not vacancies)
//                    for (Element tdElement: tdElements) { // take each row(vacancy) in the table on the site
//                        if (vacancyCounter < tbodyElements.get(2).childrenSize()) { // 'tbodyElements.get(2).childrenSize()' - the number of vacancies except the first 4 rows
//                            String[] vacancyDate = tbodyElements.get(2).child(vacancyCounter).child(5).text().split(" ");
//
//                            if (counter < 3) { // skip the first 4 rows
//                            counter++;
//                            } else {
//                            // verification: if we get the next month
//                                if (!vacancyDate[0].equals("сегодня,") && !vacancyDate[0].equals("вчера,") && !vacancyDate[1].equals(months.get(sdf.format(calendar.getTime())))) {
//                                    monthCounter++;
//                                    calendar.add(Calendar.MONTH, -1);
//
//                            // when the last month the user wants ends: break
//                                if (monthCounter == Integer.parseInt(inputArray[2])) {
//                                break;
//                                }
//                            }
//
//                            String[] words = tdElement.text().split(" "); // find a vacancy by keyword
//                            for (String word: words) {
//                                if (word.equalsIgnoreCase(inputArray[1])) {
//                                    if (!file.exists()) { // if the file does not exist
//                                        new FileWriter(file);
//                                    }
//
//                                    // write to file
//                                    pw.println(tdElement.child(0).attr("href")); // link
//                                    pw.println(tdElement.child(0).text()); // title
//                                    pw.println(tbodyElements.get(2).child(vacancyCounter).child(5).text() + "\n"); // date
//                                }
//                            }
//                            vacancyCounter++;
//                            }
//                        }
//                    }
//                pageCounter++;
//                }
//                pw.close();
//                System.out.println("\nAll the vacancies have been written to the file");
//
//        } catch (IOException e) {
//        System.out.println("\nSome input-output Exception: " + e.getMessage());
//        }