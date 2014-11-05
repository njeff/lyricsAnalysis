/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package audioanalysis;

import java.io.File;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javazoom.jl.converter.Converter;
import org.apache.commons.io.FilenameUtils;

/**
 * Audio File Utilities
 * 
 * @author Jeffrey
 */
public class AudioFileUtils {
    /**
     * Splits a WAV file into 20 second segments
     * 
     * @param files Directory of WAV files to cut
     * @param dir Directory to place cut up WAV files
     * @throws Exception 
     */  
    public static void split(File[] files, String dir) throws Exception{
        for(int i = 0; i < files.length; i++){
            AudioInputStream stream = null;
            stream = AudioSystem.getAudioInputStream(files[i]);
            AudioFormat format = stream.getFormat();
            long filelength = files[i].length(); //length in bytes
            int bytes_p_sec = (int)format.getSampleRate()*format.getSampleSizeInBits()*format.getChannels()/8;
            long fileduration = filelength/bytes_p_sec;
            AudioInputStream shortstream = null;
            int copylength = (int)format.getSampleRate()*20; //20 seconds
            //System.out.println(copylength);
            int timestocut = (int)Math.ceil(fileduration/20);
            //File tempFile = null;
            for(int j = 0; j <= timestocut; j++){
                //System.out.println(stream.available());
                if(copylength*format.getSampleSizeInBits()*format.getChannels()/8 > stream.available()){ //if there isn't 20 seconds of audio left
                    shortstream = new AudioInputStream(stream,format,stream.available()*8/(format.getSampleSizeInBits()*format.getChannels())); //make the stream only until the end
                }
                else {
                    shortstream = new AudioInputStream(stream,format,copylength);
                }
                File directory = new File(dir + FilenameUtils.removeExtension(files[i].getName()));
                File tempFile = new File(dir + FilenameUtils.removeExtension(files[i].getName()) + "\\" + j + ".wav");
                directory.mkdirs();
                tempFile.createNewFile();
                AudioSystem.write(shortstream, AudioFileFormat.Type.WAVE, tempFile);
                //tempFile.delete();
            }
            stream.close();
            shortstream.close();
        }
    }
    
    /**
     * Converts a mp3 file into a WAV files
     * 
     * @param filename MP3
     * @param dest WAV
     */
    public static void convert(String filename, String dest){
        Converter mp3wav = new Converter();
        try {
            mp3wav.convert(filename,dest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Deletes a file
     * 
     * @param filename 
     */
    public static void delete(String filename){
        File file = new File(filename);
        file.delete();
    }
}
