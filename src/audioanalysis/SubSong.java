/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package audioanalysis;

import java.io.File;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Jeffrey
 */
public class SubSong {
    
    /**
     * Automatically splits a song file into segments based on transitions in the audio
     * Returns the split files in a directory in the directory of the original song file
     * Also returns the .arff output from jAudio in the directory of the original song file
     * 
     * @param songPath Full path to the song file in WAV format
     */
    public static void createSubSong(String songPath){
        File songFile = new File(songPath);
        String song = "";
        String arffOutput = "";
        if(songFile.getName().toLowerCase().endsWith(".wav")){
            System.out.println(songFile.getAbsolutePath());
            song = songFile.getAbsolutePath();
            arffOutput = songFile.getParent()+"\\"+FilenameUtils.removeExtension(songFile.getName())+".arff";
        } else {
            System.out.println("Invalid File.");
            return;
        }
        //instantiate jAudio
        jAudioRunner jRunner = new jAudioRunner("F:\\Jeffrey\\Desktop\\Science Project 2014-2015\\similarity tests\\output", "F:\\Jeffrey\\Documents\\GitHub\\msj2013\\jaudioout\\definitions.xml");
        jRunner.run(song, arffOutput);
        jAudioRunner.jAudioCleaner(arffOutput);
        SelfSimilarity similarity = new SelfSimilarity(arffOutput);
        similarity.split(new File(song));
    }
}
