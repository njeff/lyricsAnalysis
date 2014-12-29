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
import database.LyricsAccess;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.util.Random;
import lyricsanalysis.LyricsProcess;
import lyricsanalysis.LyricsWiki;

/**
 * Get songs from last.fm by tag
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
     * @return The results page
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
    
    /**
     * Gets songs from last.fm (and their lyrics) that have a specific tag and saves this to the database
     * 
     * @param tag The tag we are searching with
     */
    public static void getTitles(String tag){
        Connection con = LyricsAccess.startconnection("orcl");
        int count = 0; //song lyrics count
        int page = 1; //page of lyrics
        
        while(count<50){
            XmlPage xml = getXmlResults(tag,page);
            if(xml.asXml().contains("error code")){ //if there was an error, exit
                return;
            }
            //System.out.println(xml.asXml());
            try{
                BufferedReader in = new BufferedReader(new StringReader(xml.asXml()));

                String line = "";
                String lastline = "";
                boolean nameflag = false;
                boolean artistflag = false;
                String artist = "";
                String name = "";      
                int length = 0;

                while((line = in.readLine())!=null){ //iterate thorugh each line 
                    if(lastline.trim().equals("<duration>")){
                        length = Integer.parseInt(line.trim());
                        //System.out.println(length);
                    }                
                    if(nameflag){ //song name
                        name = line.trim().replace("&amp;", "&"); //convert HTML escaped character into normal character
                        //System.out.println(line.trim());
                    }
                    if(artistflag){ //song artist
                        artist = line.trim();
                        //System.out.println(" " + line.trim());

                        // Get lyrics from online
                        LyricsProcess lyric = new LyricsProcess();
                        String uLyrics = lyric.webgrab(name,artist); //get lyrics
                        String c_lyrics = ""; //with timestamp
                        String nt_lyrics = ""; //no timestamp
                        //c_lyrics = lyric.cleanup7(uLyrics,true); //clean up
                        //nt_lyrics = lyric.cleanup7(uLyrics, false); //clean up (without timestamp)
                        
                        nt_lyrics = LyricsWiki.cleanup(LyricsWiki.grabLyrics(name, artist)); //get lyrics and clean up
                        //System.out.println(c_lyrics);
                        
                        //random wait time
                        Random rand = new Random();
                        int value = 1000*(rand.nextInt(4)+2);
                        try {
                            Thread.sleep(value);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                        
                        int moods[] = {5}; ///////////mood to be input, 0-7\\\\\\\\\\\
                        
                        //c_lyrics = LyricsProcess.oneLine(c_lyrics);
                        nt_lyrics = LyricsProcess.oneLine(nt_lyrics); //put lyrics in one line
                        
                        if(!nt_lyrics.isEmpty()){ //if there are lyrics
                            LyricsAccess.saveto(con, name, artist, length, nt_lyrics, c_lyrics, moods);
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
    
    /**
     * Return album information for the given song
     * Result array contains album name, release date, and a URL to the album cover, in that order
     * 
     * @param name Name of the song
     * @param artist Name of the artist
     * @return 
     */
    public static String[] albumResults(String name, String artist){
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
            xml = webClient.getPage("http://ws.audioscrobbler.com/2.0/?method=track.getInfo&artist="+artist+"&track="+name+"&api_key=dd37652a477e74f92d58b48835b9f314");
            //System.out.println(xml.asXml());
        } catch (Exception e){
            e.printStackTrace();
        }
        
        String album = "";  
        try{
            BufferedReader in = new BufferedReader(new StringReader(xml.asXml()));

            String line = "";
            String lastline = ""; 

            while((line = in.readLine())!=null){ //iterate through each line 
                if(lastline.trim().contains("<title>")){ //get title of album
                    System.out.println(line.trim()); 
                    album = line.trim();
                }
                lastline = line;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        
        //////////////////////Get album image and release date\\\\\\\\\\\\\\\\\\\\
        try{
            xml = webClient.getPage("http://ws.audioscrobbler.com/2.0/?method=album.getInfo&artist="+artist+"&album="+album+"&limit=100&api_key=dd37652a477e74f92d58b48835b9f314");
            //System.out.println(xml.asXml());
        } catch (Exception e){
            e.printStackTrace();
        }
        
        String releasedate = "";
        String imageURL = "";
        
        try{
            BufferedReader in = new BufferedReader(new StringReader(xml.asXml()));

            String line = "";
            String lastline = ""; 
            
            while((line = in.readLine())!=null){ //iterate through each line 
                if(lastline.trim().contains("<image size=\"extralarge\">")){ //get album image
                    System.out.println(line.trim()); 
                    imageURL = line.trim();
                    saveImage(imageURL,"..\\Album Art\\"+album.replace("\\", "")+".png");
                }
                if(lastline.trim().contains("<releasedate>")){ //get album release date
                    System.out.println(line.trim().replace(", 00:00", ""));
                    releasedate = line.trim().replace(", 00:00", "");
                }               
                lastline = line;
            }
        } catch (Exception e){
            e.printStackTrace();
        }  
        String[] results = {album,releasedate,"..\\Album Art\\"+album.replace("\\", "")+".png"};
        return results;
    }
    
    /**
     * Method from: http://www.avajava.com/tutorials/lessons/how-do-i-save-an-image-from-a-url-to-a-file.html
     * 
     * @param imageUrl URL of image
     * @param destinationFile Image save location
     * @throws IOException 
     */
    public static void saveImage(String imageUrl, String destinationFile) throws IOException {
        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
        }

        is.close();
        os.close();
    }
}