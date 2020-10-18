import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.Scanner;

public class SqlParser {
    public static final String URL = "https://www.sql.ru/forum/job-offers"; // url constant

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nEnter url(sql.ru), key word(java), period(months)");

        while (true) {
            if (checkInput(scanner.nextLine())) {
                System.out.println("worked");
                break;
            }
        }


//        try {
//            List<Vacancy> vacancyList = new ArrayList<>(); // create a list for our vacancies
//
//            Document doc = Jsoup.connect(URL).get(); // connect to the website
//
//            // get vacancy title, vacancy url and vacancy date
//            Elements tdElements = doc.getElementsByAttributeValue("class", "postslisttopic"); // now 'tdElements' has all vacancies on the page
//            Elements tbodyElements = doc.getElementsByTag("tbody"); // find all 'tbody' tags (to get date)
//            int count = 1; // counter for date
//            for (Element tdElement: tdElements) { // for each vacancy we get its url, title and date
//                String url = String.valueOf(tdElement.child(0).attr("href"));
//                String title = tdElement.child(0).text();
//                String date = tbodyElements.get(2).child(count).child(5).text();
//
//                vacancyList.add(new Vacancy(url, title, date)); // put it in the list
//
//                count++;
//            }
//
//        } catch (IOException e) {
//            System.out.println("\nSome input-output Exception: " + e.getMessage());
//        }
    }

    public static boolean checkInput(String input) {
        boolean isCorrect = false;

        String[] inputArray = input.split(",");
        if (inputArray.length == 3) {
            isCorrect = true;

        } else if (inputArray.length != 3) {
            System.out.println("\nNeeded format: url, key word, period(months) - with ','");
            isCorrect = false;

        } else if (!inputArray[0].toLowerCase().equals(String.valueOf(URL.charAt(12) + URL.charAt(13) + URL.charAt(14) + URL.charAt(15) + URL.charAt(16) + URL.charAt(17)))) {
            System.out.println("\nWrong url\nExapmle: sql.ru");
            isCorrect = false;

        } else if (Integer.parseInt(inputArray[3]) > 12) {
            System.out.println("\nPeriod must be in months");
            isCorrect = false;
        }

        System.out.println(inputArray[0].toLowerCase());
        return isCorrect;
    }
}

class Vacancy {
    private String url;
    private String title;
    private String date;

    public Vacancy(String url, String title, String date) {
        this.url = url;
        this.title = title;
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Vacancy{" +
                "url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}

//            for (Vacancy vacancy: vacancyList) { // or: vacancyList.forEach(System.out::println);
//                System.out.println(vacancy);
//            }
//
//            for (Vacancy vacancy: vacancyList) {
//                String url = vacancy.getUrl();
//                String title = vacancy.getTitle();
//                String date = vacancy.getDate();
//
//                System.out.printf("Vacancy title: %s\nVacancy url: %s\nVacancy date: %s\n\n", title, url, date);
//            }