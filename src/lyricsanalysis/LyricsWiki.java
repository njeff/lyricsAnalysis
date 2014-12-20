/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lyricsanalysis;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.Jsoup;

/**
 * Grabs lyrics from lyrics.wikia.com and cleans them up (no timestamp)
 * 
 * @author Jeffrey
 */
public class LyricsWiki {
    /**
     * Get raw lyrics form lyrics.wikia.com
     * 
     * @param title Title of the song
     * @param artist Artist of the song
     * @return HTML source of lyrics page
     */
    public static String grabLyrics(String title, String artist){
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
        webClient.getOptions().setTimeout(120000);
        webClient.waitForBackgroundJavaScript(60000);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        
        String lyrics = null;
        
        artist = artist.replaceAll(" ", "_");
        title = title.replaceAll(" ", "_");
        String url = "http://lyrics.wikia.com/"+artist+":"+title;
        System.out.println(url);
        
        // Get the first page
        try{
            HtmlPage page1 = webClient.getPage(url); //load cicyzone.com
            //System.out.println(page1.asXml());
            lyrics = page1.asXml();
        }
        catch (Exception e){
            //e.printStackTrace();
        }
        finally {
            webClient.closeAllWindows();
        }
        return lyrics; //returns an XML document with the timestamped lyrics
    }
    
    /**
     * Cleans up lyrics from lyrics.wikia.com
     * 
     * @param raw_lyrics HTML source of lyrics page
     * @return Lyrics
     */
    public static String cleanup(String raw_lyrics){
        //System.out.println(raw_lyrics);
        String lyrics = "";
        try {
            boolean start = false;
            String line = "";
            String lastline = "";

            BufferedReader in = new BufferedReader(new StringReader(raw_lyrics)); //initialize a string reader

            while((line = in.readLine())!=null){ //iterate thorugh each line      
                if(start == true){ //save lyrics
                    if(!Jsoup.parse(line).text().equals("")){
                        lyrics += Jsoup.parse(line).text() + System.getProperty("line.separator");
                    }
                }
                if(line.trim().equals("<!--")){ //find the end of lyrics
                    //System.out.println(lyrics);
                    break;
                    //start = false;
                }
                
                if(lastline.contains("(function() {var opts = {artist:")){ //find the start of lyrics
                    if(line.contains("//]]>")){
                        start = true;
                    }
                }
                lastline = line;
            }
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println(lyrics);
        return lyrics;
    }
}
