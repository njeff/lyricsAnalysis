/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lyricsanalysis;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author Jeffrey
 */
public class LyricsAnalysis {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here     
        String dir = "F:\\Jeffrey\\Music\\Songs"; //directory for MP3
        File musicdir = new File(dir);
        //array of MP3 files (to get artist and title)
        File[] mp3Files = musicdir.listFiles(new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name){
                if(name.toLowerCase().endsWith(".mp3")){
                    return true;
                }
                return false;
            }
        }
        );
        
        Mp3File mp3file = null;
        for(int i = 0; i < mp3Files.length; i++){ //go through every song
            try {
                mp3file = new Mp3File(mp3Files[i].getPath());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            String artist = null;
            String title = null;
            if (mp3file.hasId3v1Tag()) {
                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                artist = id3v1Tag.getArtist();
                title = id3v1Tag.getTitle();
            }

            if (mp3file.hasId3v2Tag()) {
              ID3v2 id3v2Tag = mp3file.getId3v2Tag();
              artist = id3v2Tag.getArtist();
              title = id3v2Tag.getTitle();
            }
            
            LyricsProcess lyric = new LyricsProcess();
            String uLyrics = lyric.webgrab(title,artist); //get lyrics
            String c_lyrics = lyric.cleanup7(uLyrics,true); //clean up
            String nt_lyrics = lyric.cleanup7(uLyrics, false); //clean up (without timestamp)
            System.out.println(c_lyrics);

            WordSpeed speed = new WordSpeed(c_lyrics);
            speed.computeSpeed();

            CSVWriter writer = new CSVWriter();
            writer.CSVIndiv(speed.getAvgSpeed(),nt_lyrics,-1,title); //write to CSV
        }
    }
}
