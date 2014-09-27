/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lyricsanalysis;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Gets the singing speed of the lyrics
 * 
 * @author Jeffrey
 */
public class WordSpeed {
    String lyrics = null;
    private float speed = 0;
    private List<Float> speedline = new ArrayList<Float>();
    
    public WordSpeed(String lyrics){
        this.lyrics = lyrics;
        speed = 0;
        speedline = new ArrayList<Float>();
    }
    
    /**
     * Computes the speed of the lyrics
     */
    public void computeSpeed(){
        float t1 = 0;
        float t2 = 0;
        float syllable = 0;
        float totalsyllables = 0;
        float totaltime = 0;
        BufferedReader in;
        try{
            in = new BufferedReader(new StringReader(lyrics)); //initialize a string reader
            String line = "";
            while((line = in.readLine())!=null){ //iterate thorugh each line
                t1 = Float.parseFloat(line.substring(0, line.indexOf(" ")));
                if(t2 != 0){
                    //System.out.println(syllable/(t1-t2));
                    speedline.add(syllable/(t1-t2));
                }
                line = line.substring(line.indexOf(" "));
                syllable = syllableCounter(line);
                totalsyllables += syllable;
                t2 = t1;
            }  
            in.close();
        } catch (Exception ex){
            ex.printStackTrace();
        }
        totaltime = t1;
        speed = totalsyllables/totaltime;
    }
    
    /**
     * Gets the average speed of the whole song
     * @return Average speed
     */
    public float getAvgSpeed(){
        return speed;
    }
    
    /**
     * Gets the average speed of each line
     * @return Array of average speed for each line
     */
    public float[] getLineSpeed(){ //http://stackoverflow.com/questions/4837568/java-convert-arraylistfloat-to-float
        float[] floatArray = new float[speedline.size()];
        int i = 0;

        for (Float f : speedline) {
            floatArray[i++] = (f != null ? f : Float.NaN); //if f isn't null put in f, otherwise NaN
        }
        return floatArray;
    }
    
    /**
     * Syllable counting algorithm:
     * 1. Consecutive vowels count as one vowel
     * 2. An "e" at the end of the word doesn't count
     * 
     * @param line The input line
     */
    private int syllableCounter(String line){
        int numberOfSyllables = 0;
        String[] words = line.split("\\s+");
        char[] vowels = new char[] {'a','e','i','o','u'};
        int wordcount = 0;
        for(String word: words){ //for each word
            //count the number of syllables
            word = word.replaceAll("[^A-Za-z]", ""); //get rid of non-alphabetic characters
            word = word.toLowerCase(); //make all characters lowercase
            boolean lastvowel = false;
            for(int j = 0; j<word.length(); j++){ //for the length of the word
                if(arrayContains(vowels,word.charAt(j))){ //if the characters a vowel
                    if(!lastvowel){ //and there wasn't one before
                        wordcount++; //increment vowel count for the word
                        lastvowel = true; //and set that the last character (relative to the next) was vowel
                    }
                } else{ //if there isn't a vowel
                    lastvowel = false; //there isn't one
                }
            }
            if(word.endsWith("e")){ //if the word ends with an 'e'
                wordcount--; //remove onne from the vowel count
            }
            
            /* Removed because it didn't help in most cases
            if(word.contains("y")){ //if it ends with a y
                wordcount++; //add one to the count
            }
            */
            
            if(wordcount == 0){ //if no vowel was found
                wordcount = 1; //there has to be a minimum of one syllable
            }
            numberOfSyllables += wordcount; //add the number of syllables to the total for the string
            wordcount = 0; //reset count for the word
        }
        return numberOfSyllables;
    }
    
    /**
     * Checks if an array has a character
     * 
     * @param arr Array to check
     * @param targetValue Value to look for
     * @return True or false
     */
    private boolean arrayContains(char[] arr, char targetValue){
        for(char c: arr){
		if(c == targetValue)
			return true;
	}
	return false;
    }
}
