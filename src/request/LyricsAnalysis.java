/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package request;

import com.gargoylesoftware.htmlunit.xml.XmlPage;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import database.LyricsAccess;
import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.SQLException;
import lastfm.TagRequest;
import lyricsanalysis.*;

/**
 * Main
 * 
 * @author Jeffrey
 */
public class LyricsAnalysis {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException {  
        Connection con = LyricsAccess.startconnection("orcl");
        
        //int[] moods = {0,1,2};
        //LyricsAccess.saveto(con, "1title", "artist", 10, "lyrics", moods);
        
        //TagRequest.getTitles("joyful");
        LyricsAccess.retrieveSave(con);
//        CSVWriter writeme = new CSVWriter();
//        String dir = "F:\\Jeffrey\\Music\\Songs\\mp3_1415"; //directory for MP3
//        for(int i = 0; i<8;i++){
//            File musicdir = new File(dir + "\\" + i);
//
//            //array of MP3 files (to get artist and title)
//            File[] mp3Files = musicdir.listFiles(new FilenameFilter(){
//                @Override
//                public boolean accept(File dir, String name){
//                    if(name.toLowerCase().endsWith(".mp3")){
//                        return true;
//                    }
//                    return false;
//                }
//            }
//            );
//
//            CSVWriter writer = new CSVWriter();
//
//            Mp3File mp3file = null;
//            for(int j = 0; j < mp3Files.length; j++){ //go through every song
//                try {
//                    mp3file = new Mp3File(mp3Files[j].getPath());
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//
//                String artist = null;
//                String title = null;
//                if (mp3file.hasId3v1Tag()) {
//                    ID3v1 id3v1Tag = mp3file.getId3v1Tag();
//                    artist = id3v1Tag.getArtist();
//                    title = id3v1Tag.getTitle();
//                }
//
//                if (mp3file.hasId3v2Tag()) {
//                  ID3v2 id3v2Tag = mp3file.getId3v2Tag();
//                  artist = id3v2Tag.getArtist();
//                  title = id3v2Tag.getTitle();
//                }
//
//                LyricsProcess lyric = new LyricsProcess();
//                String uLyrics = lyric.webgrab("Love is Gone","David Guetta"); //get lyrics
//                String c_lyrics = "";
//                String nt_lyrics = ""; //no timestamp
//                c_lyrics = lyric.cleanup7(uLyrics,true); //clean up
//                nt_lyrics = lyric.cleanup7(uLyrics, false); //clean up (without timestamp)
//                System.out.println(c_lyrics);
//
//                WordSpeed speed = new WordSpeed(c_lyrics);
//                speed.computeSpeed();
//
//                writer.CSVIndiv(speed.getAvgSpeed(),nt_lyrics,i,"\\"+i+"\\"+title); //write to CSV
//            }
//
//            writer.CSVModel("..\\mp3csv\\" + i,"..\\mp3csv\\"+"model"+i);
//        }
//        
//        writeme.CSVModel("..\\mp3csv","..\\"+"totalmodel");
//        BigramTagger tagger = new BigramTagger("..\\TagHelper Tools\\TagHelperTools2","../../model.csv");
//        tagger.makeArffTrain();
    }
}