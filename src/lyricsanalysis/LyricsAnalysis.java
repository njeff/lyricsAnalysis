/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lyricsanalysis;

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
        LyricsProcess lyric = new LyricsProcess();
        String c_lyrics = lyric.cleanup7(lyric.webgrab("$","!"));
        System.out.println(c_lyrics);
        WordSpeed speed = new WordSpeed(c_lyrics);
        speed.computeSpeed();
    }
}
