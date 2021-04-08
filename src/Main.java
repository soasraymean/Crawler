import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.print("Copy/Paste your page: ");
        String start_url = in.nextLine();
        System.out.print("Enter terms you need to look for(with delimiter ';'): ");
        String str = in.nextLine();
        System.out.println("Wait for a while...");
        String[] terms = str.split(";");
                //{"Tesla", "Musk", "Gigafactory", "Elon Mask"};
        Crawler crawler = new Crawler(start_url, terms);
        crawler.start();
    }
}
