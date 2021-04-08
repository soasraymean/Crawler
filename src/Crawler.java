import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Crawler {
    private String start_url;
    private String[] terms;
    private int[] num_terms;
    private Set<String> crawledURLs = new HashSet<>();//Collections.newSetFromMap(new ConcurrentHashMap<>());
    private Set<String> newURLs = Collections.newSetFromMap(new ConcurrentHashMap<>());


    private void writeToCSV() {

        try {

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("E:\\/links.csv"), "UTF-8"));


            crawledURLs.remove(start_url);
            List<String> crawled = new ArrayList<>(crawledURLs);
            Collections.sort(crawled);
            crawled.add(0, start_url);

            StringBuffer oneLine = new StringBuffer();
            String CSV_SEPARATOR = " ";
            oneLine.append(crawled.get(0)).append("\t\t");
            int sum = 0;
            for (int i = 0; i < num_terms.length; i++) {
                oneLine.append(terms[i]).append(" -- ").append(num_terms[i]).append(" hits; ").append(CSV_SEPARATOR);
                sum += num_terms[i];
            }
            oneLine.append(" Total hits -- ").append(sum).append(" hits.");
            bw.write(oneLine.toString());


            for (int i = 1; i < crawled.size(); i++) {

                bw.newLine();
                oneLine = new StringBuffer();
                oneLine.append(crawled.get(i)).append(CSV_SEPARATOR);

                bw.write(oneLine.toString());
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Crawler(String start_url, String... terms) {
        this.start_url = start_url;
        this.terms = terms;
        num_terms = new int[terms.length];

        newURLs.add(start_url);
    }

    public void start() {

        crawl(start_url);
        crawledURLs.add(start_url);

        while (crawledURLs.size()<10000) {
            for (String s :
                    newURLs) {

                if (crawledURLs.contains(s)) {
                    newURLs.remove(s);
                } else {
                    crawledURLs.add(s);
                    newURLs.remove(s);
                    crawl(s);
                }
                if (crawledURLs.size()>=10000) break;
            }

        }
        writeToCSV();
        System.out.println("Done! Check the CSV file.");
    }

    private void crawl(String url) {
        if(crawledURLs.size()>=10000)return;
        String html = getHTML(url);
        if (html.equals("")) return;
        Document doc = Jsoup.parse(html);
        String[] HTML = html.split(" ");

        for (String s :
                HTML) {
            for (int i = 0; i < terms.length; i++) {
                if (s.equalsIgnoreCase(terms[i])) {
                    num_terms[i]++;
                }
            }
        }

        Elements elements = doc.select("a");
        for (Element e :
                elements) {
            String href = e.attr("href");
            href = processLink(href, url);
            if (href != null) {
                newURLs.add(href);
            }

        }


    }

    private String processLink(String link, String base) {
        try {
            URL u = new URL(base);
            if (link.startsWith("//")) {
                link = u.getProtocol() + ":" + link;
            } else if (link.startsWith("./")) {
                link = link.substring(2);
                link = u.getProtocol() + "://" + u.getAuthority() + getFileName(u.getPath()) + link;
            } else if (link.startsWith("/")) {
                link = u.getProtocol() + "://" + u.getAuthority() + link;
            } else if (link.startsWith("javascript")) {
                return null;
            } else if (link.startsWith("http://") || link.startsWith("https://")) {
                link = link;
            } else if (link.startsWith("../")) {
                return null;
            } else {
                link = u.getProtocol() + "://" + u.getAuthority() + getFileName(u.getPath()) + link;
            }
            link = link.replaceAll(String.valueOf('.' + '/'), "");
            if (link.contains(" ")) link = link.replace(" ", "%20");
            return link;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


    }

    private String getFileName(String path) {
        int pos = path.lastIndexOf("/");
        return pos <= -1 ? path : path.substring(0, pos + 1);
    }


    private String getHTML(String url) {
        URL u;
        try {
            u = new URL(url);
            URLConnection urlConnection = u.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Crawler");
            urlConnection.addRequestProperty("Accept-Charset", "UTF-8");
            InputStream is = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            String html = "";

            while ((line = reader.readLine()) != null) {
                html += line + "\n";
            }
            html = html.trim();
            is.close();
            if (html.length() == 0) html = "";
            return html;


        } catch (Exception e) {
            return "";
        }
    }
}
