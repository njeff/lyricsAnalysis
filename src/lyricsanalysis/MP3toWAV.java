/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lyricsanalysis;

import java.io.File;
import javazoom.jl.converter.Converter;

/**
 * Converts MP3 files to WAV
 * 
 * @author Jeffrey
 */
public class MP3toWAV {
    public void convert(String filename, String dest){
        Converter mp3wav = new Converter();
        try {
            mp3wav.convert(filename,dest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void delete(String filename){
        File file = new File(filename);
        file.delete();
    }
}
