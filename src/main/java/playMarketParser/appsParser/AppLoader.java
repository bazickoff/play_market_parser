package playMarketParser.appsParser;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import jdk.nashorn.internal.parser.JSONParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import playMarketParser.App;
import playMarketParser.Connection;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AppLoader extends Thread {
    private App app;
    OnAppLoadingCompleteListener onAppLoadingCompleteListener;
    private String language;
    private String country;

    AppLoader(App app, OnAppLoadingCompleteListener onAppLoadingCompleteListener, String language, String country) {
        this.app = app;
        this.onAppLoadingCompleteListener = onAppLoadingCompleteListener;
        this.language = language;
        this.country = country;
    }

    @Override
    public void run() {
        super.run();
        try {
            String url = app.getUrl() +
                    (language != null ? "&hl=" + language : "") +
                    (country != null ? "&gl=" + country : "");
            Document doc = Connection.getDocument(url);
            if (doc == null) throw new IOException("�� ������� ��������� �������� ����������� ������");
            parseHtml(doc);
            parseJson(doc);
            onAppLoadingCompleteListener.onAppParsingComplete(app, true);
        } catch (IOException e) {
            e.printStackTrace();
            onAppLoadingCompleteListener.onAppParsingComplete(app, false);
        }
    }

    private void parseHtml(Document doc) {
        //name
        app.setName(doc.getElementsByTag("h1").first().text());
/*        //devName, devUrl
        try {
            Element devLink = doc.select("a[href^=/store/apps/dev].hrTbp.R8zArc").first();
            app.setDevName(devLink.text());
            app.setDevUrl("https://play.google.com" + devLink.attr("href"));
        } catch (Exception e) {
            System.out.println("�� ������� �������� ��������� ������������");
        }
        //devEmail
        try {
            app.setDevEmail(doc.select("a[href^=mailto:].hrTbp.euBY6b").first().text());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "�� ������� �������� email");
        }*/
        //ratesCount
        try {
            Element element = doc.select("span.AYi5wd.TBRnV").first();
            app.setRatesCount(element != null ? Integer.parseInt(element.text().replaceAll(" ", "")) : 0);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "�� ������� �������� ����� ������");
        }
        //avgRate
        try {
            Element element = doc.select("div[aria-label].BHMmbe").first();
            app.setAvgRate(element != null ? Double.parseDouble(element.text().replaceAll(",", ".")) : 0);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "�� ������� �������� ��. ������");
        }
        //category
        try {
            app.setCategory(doc.select("a[itemprop=genre]").first().text());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "�� ������� �������� ���������");
        }
        //whats new
        try {
            app.setWhatsNew(doc.select("c-wiz[jsrenderer=eG38Ge] div[itemprop=description].DWPxHb").first().text());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "�� ������� �������� ��� ������");
        }
        //description
        try {
            app.setDescription(doc.select("c-wiz[jsrenderer=UsuzQd] div[itemprop=description].DWPxHb").first().text());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "�� ������� �������� ��������");
        }
        //similar apps
        try {
            for (Element element : doc.select("c-wiz[jsrenderer=PRm2u] a.poRVub"))
                app.addSimilarApp("https://play.google.com" + element.attr("href"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "�� ������� �������� ������� ����������");
        }

    }

    private void parseJson(Document doc) {
        //��������� JSON
        Pattern pattern = Pattern.compile("\\{key: 'ds:5'.*?return(.*?)}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(doc.data());
        if (!matcher.find()) {
            System.out.printf("%-40s%s%n", app.getId(), "�� ������� �������� JSON");
            return;
        }
        String jsonData = matcher.group(1);
        //TODO devWebSite, installsCount, minAge, size, lastUpdate, seller, version, minSdk,
        JsonArray data;
        try {
            JsonArray fullData = (JsonArray) Jsoner.deserialize(jsonData);
            data = ((JsonArray) fullData.getCollection(0)).getCollection(12);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "�� ������� �������� JSON");
            return;
        }
        //���. �������
        try {
            String minAge = ((JsonArray) data.getCollection(4)).getString(0);
            app.setMinAge(Integer.parseInt(minAge.replaceAll("\\+", "")));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "�� ������� �������� ����������� �������");
        }
        //�����������
        try {
            JsonArray devData = data.getCollection(5);
            app.setDevName(devData.getString(1));
            app.setDevEmail(((JsonArray) devData.getCollection(2)).getString(0));
            app.setDevUrl("https://play.google.com" +
                    ((JsonArray) ((JsonArray) devData.getCollection(5)).getCollection(4)).getString(2));
            if (devData.getCollection(3) != null)
                app.setDevWebSite(((JsonArray) ((JsonArray) devData.getCollection(3)).getCollection(5)).getString(2));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "�� ������� �������� ���������� � ������������");
        }
        //����� ���������
        try {
            app.setInstallsCount(Integer.parseInt(((JsonArray) data.getCollection(9)).getString(2)));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("%-40s%s%n", app.getId(), "�� ������� �������� ���������� � ������������");
        }
    }

    interface OnAppLoadingCompleteListener {
        void onAppParsingComplete(App app, boolean isSuccess);
    }
}
