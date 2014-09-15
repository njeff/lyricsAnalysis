/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lyricsanalysis;

import java.io.BufferedReader;
import java.io.StringReader;

/**
 * Gets the relative position of the words and weights them based on position
 * 
 * @author Jeffrey
 */
public class WordPosition {
    String lyrics = null;
    public WordPosition(String lyrics){
        this.lyrics = lyrics;
    }
    
    private void getWeights(){
        String words[];
        BufferedReader in;
        try{
            in = new BufferedReader(new StringReader(lyrics)); //initialize a string reader
            String line = "";
            while((line = in.readLine())!=null){ //iterate thorugh each line
                line = line.substring(line.indexOf(" ")); //get rid of timestamps
                words = line.split("\\s+");
                
            }  
            in.close();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
