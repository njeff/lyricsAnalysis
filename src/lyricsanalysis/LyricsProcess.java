/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lyricsanalysis;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.text.WordUtils;

/**
 * Grabs lyrics from online and cleans them up
 * 
 * @author Jeffrey
 */
public class LyricsProcess {
    String title;
    String artist;
    
    /**
     * Grabs the timestamped lyrics of a song
     * 
     * @param title Title of the song
     * @param artist Name of the artist
     * @return XML formatted lyrics
     */
    public String webgrab(String title, String artist){ 
        System.out.println(title);
        System.out.println(artist);
        String artistTemp = null;
        artist = artist.replaceAll("[é]","e");
        String feat = null; //string for holding featured artists
        int andindex = 0; //position of the "and"
        andindex = artist.indexOf(" & "); //looks for the & symbol
        if(andindex != -1){ //if found
            artistTemp = artist.substring(0,andindex); //get the main artist
            feat = artist.substring(andindex+3); //get the featured artist
            title = title + " (ft. " + feat + ")"; //combine the featured artist with the title (formatted like the ones found on www.cicyzone.com)
            artist = artistTemp;
        }
        else{ //if you dont find the symbol
            andindex = artist.indexOf(" and "); //look for the word "and"
            if(andindex != -1){
                artistTemp = artist.substring(0,andindex); //get the main artist
                feat = artist.substring(andindex+5); //get the featured artist
                title = title + " (ft. " + feat + ")"; //combine the featured artist with the title (formatted like the ones found on www.cicyzone.com)
                artist = artistTemp;
            }
        }
        andindex = 0;
        this.title = title;
        this.artist = artist;
        //System.out.println(title);
        //System.out.println(feat);    
        //Settings from: http://stackoverflow.com/questions/17449826/cannot-get-htmlunit-to-follow-links-on-page-that-uses-a-dopostback-function
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
        
        artist = artist.replaceAll(" ", "%20");
        String url = "http://www.cicyzone.com/Lyrictest/Default.aspx?Artist="+artist;
        System.out.println(url);
        
        // Get the first page
        try{
            HtmlPage page1 = webClient.getPage(url); //load cicyzone.com
            HtmlAnchor a = null;
            HtmlPage page2 = null;
            try {
                a = page1.getAnchorByText(title.replaceAll("[ \t]+$", ""));  
                page2 = a.click();
            } catch (Exception e){
                //e.printStackTrace();
                a = page1.getAnchorByText(WordUtils.capitalize(title.replaceAll("\\(.*\\)", "").replaceAll("[ \t]+$", "")));
                page2 = a.click();
            }
            
            //System.out.println(page2.asText()); //debug
        
            HtmlInput i = page2.getHtmlElementById("Button7");
            
            if(i != null){ //if there was a result returned
                HtmlPage page3 = i.click();
                lyrics = page3.asText();
                //System.out.println(page3.asText()); //debug
            }
            else{
                lyrics = "NONE FOUND"; //for debugger to find; after the lyrics go throught the lyric cleaner it will return nothing: "" (not null though)
            }
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
     * Cleans up raw lyrics from Button6 (LRC)
     * 
     * @param raw_lyrics XML formatted lyrics
     * @return Formatted lyrics
     */
    public String cleanup6(String raw_lyrics){
        String lyrics = "";
        try {
            boolean start = false;
            boolean firstline = false;
            String line = null;

            BufferedReader in = new BufferedReader(new StringReader(raw_lyrics)); //initialize a string reader

            Pattern braket_find = Pattern.compile("\\[.*\\]"); //use regex to find things in brackets
            Matcher matchline;

            while((line = in.readLine())!=null){ //iterate thorugh each line                 
               matchline = braket_find.matcher(line);
               String found = null;

               if(matchline.find()){ //if found
                  found = matchline.group(); //save the braketed tag  

                  if(found.startsWith("[00")){ //if a timestamp is found
                      start = true; //make the lyrics start flag true
                  }

                  if(start){ //if we are reading lyrics
                      //System.out.println(found);
                      //System.out.println(cleantime(found));
                      if(!firstline){
                          if(line.toLowerCase().contains(title.toLowerCase())||line.toLowerCase().contains(artist.toLowerCase())){
                              line = "";
                          }
                          else {
                              lyrics += cleantime(found) + " " + line.substring(line.indexOf("]")+1,line.length()) + "\n";  
                          }
                      }
                      else{
                          lyrics += cleantime(found) + " " + line.substring(line.indexOf("]")+1,line.length()) + "\n";   
                      }
                      firstline = true;
                  }
               }
            }
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return lyrics;
     }
    
    /**
     * Cleans up raw lyrics from Button7 (XML)
     * 
     * @param raw_lyrics The un-cleaned lyrics
     * @param timeflag Output with timestamp
     * @return Formatted lyrics
     */
    public String cleanup7(String raw_lyrics, boolean timeflag){
        String lyrics = "";
        if(raw_lyrics != null){
            try {
                boolean start = false;
                boolean firstline = true;
                String line = null;
                float time = 0;
                String l_lyrics = null;

                int q1,q2,q3,q4 = 0;

                BufferedReader in = new BufferedReader(new StringReader(raw_lyrics)); //initialize a string reader

                while((line = in.readLine())!=null){ //iterate thorugh each line
                    if(line.startsWith("</swf")){ //if a </swf if found (previous line was last)
                        start = false;
                    }

                    if(start){ //if we are reading lyrics
                        if(line.toLowerCase().contains("\"0\"")&&(line.toLowerCase().contains(artist.toLowerCase())||line.toLowerCase().contains(title.toLowerCase()))){
                            line = "";
                        }
                        //get positions of all the quotation marks
                        q1 = line.indexOf("\"");
                        q2 = line.indexOf("\"", q1+1);
                        q3 = line.indexOf("\"", q2+1);
                        q4 = line.indexOf("\"", q3+1);

                        if(q1 != -1){
                            time = Float.parseFloat(line.substring(q1+1, q2))/1000;
                            l_lyrics = line.substring(q3+1, q4);  
                            if(timeflag){
                                if(firstline){
                                    lyrics += time + " " + l_lyrics;
                                    firstline = false;
                                } else {
                                    lyrics += "\n" + time + " " + l_lyrics;
                                }
                            } else {
                                if(firstline){
                                    lyrics += l_lyrics;
                                    firstline = false;
                                } else {
                                    lyrics += "\n" + l_lyrics;
                                }
                            }
                        }
                    }

                    if(line.startsWith("<swf")){ //if a <swf if found (next line is start of lyrics)
                        start = true; //make the lyrics start flag true
                    }
                }
                in.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return lyrics;
     }
     
    /**
     * Takes a string of time in this format: [MM:SS.SSS] and converts it into seconds
     * 
     * @param raw_time Time in MM:SS.SSS format
     * @return Time in seconds
     */
     private static float cleantime(String raw_time){
         int clean_time = 0;
         int colon = raw_time.indexOf(":");
         int minutes = Integer.parseInt(raw_time.substring(1, colon));
         float seconds = Float.parseFloat(raw_time.substring(colon+1, raw_time.length()-1));
         
         seconds += minutes*60;
         return seconds;
     }
    
    /**
     * Converts a multiline string into one line
     * 
     * @param input String
     * @return New string
     */
    public static String oneLine(String input){
        String output = "";
        try{
            BufferedReader in = new BufferedReader(new StringReader(input));
            String line = "";
        
            while((line=in.readLine())!=null){
                output += line + " ";
            }
        } catch (Exception e) {
            
        }
        return output;
    }
}
