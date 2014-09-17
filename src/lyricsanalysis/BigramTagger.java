/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lyricsanalysis;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Uses TagHelper Tools to decompose text into a .arff format for building classifier
 * 
 * @author Jeffrey
 */
public class BigramTagger {
    String path = null; //path to csv file
    
    public BigramTagger(String path){
        this.path = path;
    }
    
    /**
     * Runs TagHelperTools to create arff for training
     */
    private void makeArffTrain(){
        try {
            Process p = Runtime.getRuntime().exec("runtht -ng -lang eng -f uni -f bi -f posbi -noev -cl [weka.classifiers.bayes.NaiveBayes]");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Runs TagHelperTools to create arff for classification
     */
    private void makeArffSong(){
        
    }
}
