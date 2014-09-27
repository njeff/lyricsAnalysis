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
 * Writes CSV files for TagHelper Tools
 * 
 * @author Jeffrey
 */
public class CSVWriter {
    /**
     * Creates a CSV file for a set of multiple songs (for training model)
     * 
     * @param dir Directory of csv files to put together
     * @param name Name and directory of csv file
     */
    public void CSVModel(String dir, String name){
        String complete = "";
        
        File[] csv = new File(dir).listFiles(new FilenameFilter(){
                @Override
                public boolean accept(File dir, String name){
                    if(name.toLowerCase().endsWith(".csv")){
                        return true;
                    }
                    return false;
                }
            }
            );
        
        for(int i = 0; i < csv.length; i++){
            complete += fileString(csv[i]); //put all text into one large string
        }
               
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(name + ".csv","UTF-8");
            writer.println("category,text,speed");
            writer.print(complete);
            System.out.println(complete);
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
     * @param category If -1, category isn't written
     * @param name Name of csv file
     */
    public void CSVIndiv(float speed, String lyrics, int category, String name){
        if(!lyrics.isEmpty()){
            PrintWriter writer = null;
            try {
                writer = new PrintWriter("..\\mp3csv\\" + name + ".csv","UTF-8");
                if(category == -1){ //no category
                    writer.println("text,speed");
                    writer.println("\"" + lyrics + "\",\"" + speed + "\"");
                } else { //yes category
                    writer.println("category,text,speed");
                    writer.println("\"c." + category + "\",\"" + lyrics + "\",\"" + speed + "\"");
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
     * @return Contents of file without the first line
     */
    private String fileString(File file){
        String everything = "";
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine(); //skip first line
            
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
