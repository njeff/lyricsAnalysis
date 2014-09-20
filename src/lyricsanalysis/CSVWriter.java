/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lyricsanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jeffrey
 */
public class CSVWriter {
    /**
     * Creates a CSV file for a set of multiple songs (for training model)
     * 
     * @param dir Directory of csv files to put together
     */
    public void CSVModel(String dir){
        String complete = "";
        
        File[] csv = new File(dir).listFiles();
        for(int i = 0; i < csv.length; i++){
            complete += fileString(csv[i]); //put all text into one large string
        }
               
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("F:\\Jeffrey\\Desktop\\Science Project 2014-2015\\model.csv","UTF-8");
            writer.println("category,speed,text");
            writer.println(complete);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            writer.close();
        }
    }
    
    /**
     * Creates a CSV file for one song (for classification)
     * 
     * @param speed 
     * @param lyrics
     * @param category if -1, category isn't written
     */
    public void CSVIndiv(float speed, String lyrics, int category, String name){
        if(!lyrics.isEmpty()){
            PrintWriter writer = null;
            try {
                writer = new PrintWriter("F:\\Jeffrey\\Desktop\\Science Project 2014-2015\\mp3csv\\" + name + ".csv","UTF-8");
                if(category == -1){
                    writer.println("speed,text");
                    writer.println(speed + ",\"" + lyrics + "\"");
                } else {
                    writer.println("category,speed,text");
                    writer.println(category + "," + speed + ",\"" + lyrics + "\"");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                writer.close();
            }
        }
    }
    
    /**
     * Creates a string from a file
     * 
     * @param file File to convert
     * @return Contents of file
     */
    private String fileString(File file){
        String everything = "";
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            
            if((line = br.readLine()) != null){}; //skip first line
            
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
            everything = sb.toString();
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return everything;
    }
}
