/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lastfm;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import java.io.BufferedReader;
import java.io.StringReader;
import lyricsanalysis.LyricsProcess;

/**
 *
 * @author Jeffrey
 */
public class TagRequest {
    public TagRequest(){
        
    }
    
    /**
     * Get the list of songs that have that tag
     * 
     * @param tag The tag we want to search with
     * @param page Page number
     */
    public static XmlPage getXmlResults(String tag, int page){
        //initalize browser
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
        
        XmlPage xml = null;
        try{
            xml = webClient.getPage("http://ws.audioscrobbler.com/2.0/?method=tag.gettoptracks&tag="+tag+"&page="+page+"&limit=100&api_key=dd37652a477e74f92d58b48835b9f314");
            //System.out.println(xml.asXml());
        } catch (Exception e){
            e.printStackTrace();
        }
        return xml;
    } 
    
    public static void getTitles(String tag){
        int count = 0; //song lyrics count
        int page = 1; //page of lyrics
        
        while(count<50){
            XmlPage xml = getXmlResults(tag,page);
            if(xml.asXml().contains("error code")){
                return;
            }
            try{
                BufferedReader in = new BufferedReader(new StringReader(xml.asXml()));

                String line = "";
                String lastline = "";
                boolean nameflag = false;
                boolean artistflag = false;
                String artist = "";
                String name = "";            

                while((line = in.readLine())!=null){ //iterate thorugh each line 
                    if(nameflag){ //song name
                        name = line.trim();
                        //System.out.println(line.trim());
                    }
                    if(artistflag){ //song artist
                        artist = line.trim();
                        //System.out.println(" " + line.trim());

                        // Get lyrics from online
                        LyricsProcess lyric = new LyricsProcess();
                        String uLyrics = lyric.webgrab(name,artist); //get lyrics
                        String c_lyrics = "";
                        String nt_lyrics = ""; //no timestamp
                        c_lyrics = lyric.cleanup7(uLyrics,true); //clean up
                        nt_lyrics = lyric.cleanup7(uLyrics, false); //clean up (without timestamp)
                        System.out.println(c_lyrics);
                        if(!c_lyrics.isEmpty()){
                            count++;
                            System.out.println(count);
                        }
                        if(count==50){
                            return;
                        }
                    }           

                    if(line.trim().equals("<name>")&&!lastline.trim().equals("<artist>")){ //finds the name of the song
                        nameflag = true;
                    } else {
                        nameflag = false;
                    }

                    if(line.trim().equals("<name>")&&lastline.trim().equals("<artist>")){ //finds the artist of the song
                        artistflag = true;
                    } else {
                        artistflag = false;
                    }

                    lastline = line;
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            
            page++;
        }
    }
}