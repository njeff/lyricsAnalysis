/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package audioanalysis;

import com.jhlabs.image.GaussianFilter;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.commons.io.FilenameUtils;

/**
 * Does self-similarity analysis on data
 * 
 * @author Jeffrey
 */

//F:\\Jeffrey\\Desktop\\Science Project 2014-2015\\similarity tests\\titanium\\titanium - Copy.arff
//F:\\Jeffrey\\Desktop\\Science Project 2014-2015\\similarity tests\\art of war\\art of war.arff
public class SelfSimilarity{   
    private Color color = Color.RED;
    final int PAD = 0;
    float max = Float.MIN_VALUE;
    float min = Float.MAX_VALUE;
    String xDir = ""; //directory of the x axis data
    String yDir = ""; //directory of the y axis data
    float linesPerSecond = (float)2.746582; //how many lines in the data correspond to one second at a sampling rate of 11.25kHz and window size of 4096 (unsure)  
    BufferedImage base = new BufferedImage(1000,1000,BufferedImage.TYPE_INT_ARGB);
    BufferedImage gImage = new BufferedImage(1000,1000, BufferedImage.TYPE_INT_ARGB);
    
    /**
     * Creates an instance of SelfSimilarity
     * 
     * @param data Data to do self-similarity
     */
    public SelfSimilarity(String data){
        xDir = data;
        yDir = data;
    }
    
    private void paintG() {
        ValueLoader set1 = new ValueLoader(xDir);
        ValueLoader set2 = new ValueLoader(yDir);
        float max = 1;
        float min = (float)0.98;
        Graphics2D g2 = base.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        Font font = new Font("Serif", Font.PLAIN, 10);
        g2.setFont(font);
        
        int w = base.getWidth();
        int h = base.getHeight();
             
        // Draw ordinate.
        //g2.draw(new Line2D.Double(PAD, PAD, PAD, h-PAD));
        // Draw abcissa.
        //g2.draw(new Line2D.Double(PAD, h-PAD, w-PAD, h-PAD));
         
        double xScale = (double)(w - 2*PAD)/set1.y;
        double yScale = (double)(h - 2*PAD)/set2.y;
                
        // Mark data points. 
        /*
         * The code below starts from the lowest, left most corner (0,0)
         * And goes straight up (0,N)
         * And then jumps down to the bottom and right one (1,0)
         * And then back up, and so on
         */
        int k = 0; //current x value
        try(BufferedReader b = new BufferedReader(new FileReader(yDir))){
            int cx[] = new int[12];
            double fx[] = new double[13];
            String sx[] = new String[13];
            
            String xline = "";
             while ((xline = b.readLine()) != null) {
                //get values in that row//
                cx[0] = xline.indexOf(",");
                for(int i =1; i <12; i++){
                   cx[i] = xline.indexOf(",", cx[i-1]+1); //find the next comma
                   if(i==1){ //if we are finding the 2nd comma
                       sx[0] = xline.substring(0,cx[0]); //get the value of the first MFCC
                       fx[0] = Double.valueOf(sx[0]); //convert
                   }    
                   sx[i] = xline.substring(cx[i-1]+1, cx[i]); //get the value 
                   fx[i] = Double.valueOf(sx[i]);
                }
                sx[12] = xline.substring(cx[11]+1); //get the last MFCC
                fx[12] = Double.valueOf(sx[12]);
                //end getting values//
                
                int c[] = new int[12];
                double f[] = new double[13];
                String s[] = new String[13];
                int l = 0; //counter for y-axis position
                //Below is file for y-axis
                
                try(BufferedReader br = new BufferedReader(new FileReader(yDir))) {
                    String line = ""; 
                    double x = 0;
                    double y = 0;
                    while ((line = br.readLine()) != null) { //counts up the y-axis
                        x = PAD + k*xScale;
                        y = h - PAD - l*yScale;
                        l++; //increment for next line up
                        //get values in that row//
                        c[0] = line.indexOf(",");
                        for(int i =1; i <12; i++){
                            c[i] = line.indexOf(",", c[i-1]+1); //find the next comma
                            if(i==1){ //if we are finding the 2nd comma
                                s[0] = line.substring(0,c[0]); //get the value of the first MFCC
                                f[0] = Double.valueOf(s[0]); //convert
                            }    
                            s[i] = line.substring(c[i-1]+1, c[i]); //get the value 
                            f[i] = Double.valueOf(s[i]);
                        }
                        s[12] = line.substring(c[11]+1); //get the last MFCC
                        f[12] = Double.valueOf(s[12]);
                        //end getting values//
                        
                        float difference = cosSim(fx,f); //cosine similarity
                        //System.out.println(difference);
                        difference = ((difference + 1)/2); //scale cosine to 0-1

                        if(difference>max)max=difference;
                        if(difference<min)min=difference;

                        difference = map(difference, min, 1, 0, 1);
                        difference = (float)Math.pow(difference, 5); //create more contrast between similar and non similar vectors
                        if(difference>1) difference=1; //just in case
                        if(difference<0) difference=0;

                        float intensity = difference;
                        setColor(intensity,intensity,intensity);
                        //System.out.println(difference);
                        g2.setPaint(getColor());
                        g2.fill(new Rectangle2D.Double(x, y-yScale, xScale, yScale));
                    }
                } catch (Exception ex){
                   ex.printStackTrace();
                }
                k++; //increment x position
             }
        } catch (Exception ex) {
            ex.printStackTrace();
        }        
        //////////////////////////////Draws axis guidelines\\\\\\\\\\\\\\\\\\\\\\\\\\\
//        float xtime = set1.y/linesPerSecond; //length of audio
//        float interval = 10; //interval on axis       
//        //Draw x-axis labels and lines
//        int numTimeX = (int)(Math.ceil(xtime/interval)); //number of labels to put on x-axis       
//        int pixelsPerIntervalX = (int)(interval*(w-2*PAD)/xtime); //number of pixels between each value       
//        for(int z = 0; z<numTimeX; z++){ //draw labels
//            g2.setColor(Color.BLACK);
//            g2.drawString(Integer.toString((int)(interval*z)), PAD-4+pixelsPerIntervalX*z, h-PAD+10); //draw number
//            g2.setColor(Color.RED);
//            g2.draw(new Line2D.Double(PAD+pixelsPerIntervalX*z, PAD, PAD+pixelsPerIntervalX*z, h-PAD)); //draw line
//        }
//        
//        float ytime = set2.y/linesPerSecond;
//        int numTimeY = (int)(Math.ceil(ytime/interval)); //number of labels to put on y-axis
//        int pixelsPerIntervalY = (int)(interval*(h-2*PAD)/ytime); //number of pixels between each value  
//        //Draw y-axis labels and lines
//        for(int z = 0; z<numTimeY; z++){ //draw labels
//            g2.setColor(Color.BLACK);
//            g2.drawString(Integer.toString((int)(interval*z)), PAD-15, h-PAD-pixelsPerIntervalY*z+3); //draw number
//            g2.setColor(Color.RED);
//            g2.draw(new Line2D.Double(PAD, h-PAD-pixelsPerIntervalY*z, w-PAD, h-PAD-pixelsPerIntervalY*z)); //draw line
//        }
          
        /////////////////////////////Logic to get transistion points\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\      
//        float mean = 0;
//        double[] column = new double[base.getWidth()];
//        for(int x = 0; x<base.getWidth();x++){
//            for(int y = (int)(base.getWidth()*0.45); y<(base.getHeight()*0.55);y++){
//                int RGB = base.getRGB(x, y);
//                int red = (RGB>>16)&255; //get the intensity of the red (or any other color)
//                //System.out.println(red);
//                column[x] += red; //add up the entire column
//            }
//            if(x>0){
//                //System.out.println(Math.abs((Math.pow(((column[x]-column[x-1])/100),2))));
//                mean += Math.abs((Math.pow(((column[x]-column[x-1])/100),2))); //get sum of (weighted differences between columns)
//                column[x-1] = Math.abs((Math.pow(((column[x]-column[x-1])/100),2))); //write the weighted value to the column
//            }
//        }
//     
//        ArrayList<Integer> list = new ArrayList<Integer>(); //list for points with large transitions
//        int length = column.length;
//        mean = mean/(length-1); //get mean
//        double sd = stdDev(column); //get standard deviation
//        //System.out.println(mean);
//        //System.out.println(sd);
//        double runningAvg = 0;
//        for(int j = 0; j<length; j++){
//            if(j>5 && (length-j>10)){
//                runningAvg = (column[j] + column[j-1] + column[j-2] + column[j-3] + column[j-4] + column[j-5])/6; //running avg
//                //System.out.println(intermediate);
//            }
//            //todo: create better transition detection code
//            if((runningAvg>(mean+0.75*sd))){ //add to list of good transition points
//                list.add(j);
//            }
//        }
//        
//        //prevents lines very close together from being drawn multiple times
//        //todo: draw the mean of a group instead of the first (may not be helpful actually)
//        int previous = 0;
//        //int avg = 0;
//        //int groupSize = 0;
//        for(Iterator<Integer> it = list.iterator(); it.hasNext();){
//            int current = it.next();
//            if((current-previous)>5){ //draw the first in a group
//                //if(groupSize == 0) groupSize = 1;
//                //if(avg == 0) avg = current;
//                //avg = avg/groupSize;
//                //groupSize = 0;
//                setColor(1,0,0);
//                g2.setPaint(color);
//                g2.draw(new Line2D.Double(PAD+current*base.getWidth()/length, PAD, PAD+current*base.getWidth()/length, h-PAD)); //draw line
//                //avg = 0;
//            } //else { //if still in a group
//            //    avg += current;
//            //    groupSize++;
//            //}
//            previous = current;
//        }
    }
 
    public Color getColor() {
            return color;
        }

    public void setColor(float r, float g, float b) {
            this.color = new Color(r,g,b);
    }
        
    /*
     * From Arduino implementation:
     * 1. Get input as proportion of input range
     * 2. Multiply input by output range
     * 3. Add minimum of output
     */
    public static float map(float x, float in_min, float in_max, float out_min, float out_max){
          return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min; 
    }
    
    /**
     * Calculates the cosine similarity of two vectors
     * Doesn't handle inputs of zero
     * 
     * @param a Vector a
     * @param b Vector b
     * @return Cosine similarity
     */
    private static float cosSim(double[] a, double[] b){
        if(a.length!=b.length) return 0;
        float dot = 0;
        float aMag = 0;
        float bMag = 0;
        for(int i = 0; i<a.length; i++){
            dot += a[i]*b[i];
            aMag += (float)Math.pow(a[i],2);
            bMag += (float)Math.pow(b[i],2);
        }
        aMag = (float)Math.sqrt(aMag);
        bMag = (float)Math.sqrt(bMag);
        //System.out.println((dot/(aMag*bMag)));
        return (dot/(aMag*bMag));     
    }

    /**
     * Calculates the standard deviation of a sample
     * 
     * @param data Array of data to calculate s.d. for
     * @return The standard deviation
     */
    private static double stdDev(double[] data){
        double n = 0;
        double mean =  0;
        double M2 = 0;
        
        for(int i = 0; i<data.length; i++){
            n = n+1;
            double delta = data[i]-mean;
            mean = mean + delta/n;
            M2 = M2 + delta*(data[i]-mean);
        }
        if (n<2){
            return 0;
        }
        double variance = M2/(n-1);
        //System.out.println(variance);
        return Math.sqrt(variance);
    }
    
    /**
     * Splits an audio file based on transition points
     * 
     * @param file Audio file
     * @param dir Directory of output
     * @throws Exception 
     */
    public void split(File file, String dir) throws Exception{
        paintG();
        
        ////////////////////////// Find transitions \\\\\\\\\\\\\\\\\\\\\\\\\\\
        float sobelX[][] = {{-1,0,1},
                            {-2,0,2},
                            {-1,0,1}};
        float sobelY[][] = {{1,2,1},
                            {0,0,0},
                            {-1,-2,-1}};

        //System.out.println(image.getHeight());
        float columnadd = 0;
        float mean = 0;

        GaussianFilter filter = new GaussianFilter(10);
        gImage = filter.filter(base, gImage);
        
        double[] column = new double[gImage.getWidth()-2];
        for(int x = 1; x<gImage.getWidth()-2;x++){
               for(int y = 1; y<gImage.getHeight()-2;y++){
                   //System.out.println("Hello");
                   int RGB1 = gImage.getRGB(x-1,y-1);
                   RGB1 = (RGB1>>16)&255; //get the intensity of the red (or any other color)
                   int RGB2 = gImage.getRGB(x, y-1);
                   RGB2 = (RGB2>>16)&255; //get the intensity of the red (or any other color)
                   int RGB3 = gImage.getRGB(x+1, y-1);
                   RGB3 = (RGB3>>16)&255; //get the intensity of the red (or any other color)
                   int RGB4 = gImage.getRGB(x-1, y);
                   RGB4 = (RGB4>>16)&255; //get the intensity of the red (or any other color)
                   int RGB5 = gImage.getRGB(x, y);
                   RGB5 = (RGB5>>16)&255; //get the intensity of the red (or any other color)
                   int RGB6 = gImage.getRGB(x+1, y);
                   RGB6 = (RGB6>>16)&255; //get the intensity of the red (or any other color)
                   int RGB7 = gImage.getRGB(x-1, y+1);
                   RGB7 = (RGB7>>16)&255; //get the intensity of the red (or any other color)
                   int RGB8 = gImage.getRGB(x, y+1);
                   RGB8 = (RGB8>>16)&255; //get the intensity of the red (or any other color)
                   int RGB9 = gImage.getRGB(x+1, y+1);
                   RGB9 = (RGB9>>16)&255; //get the intensity of the red (or any other color)
                   //System.out.println(RGB5);
                   //column[x] += red; //add up the entire column
                   //sobel operator: convolute with image
                   float gX = RGB1*sobelX[0][0] + RGB2*sobelX[0][1] + RGB3*sobelX[0][2] +
                           RGB4*sobelX[1][0] + RGB5*sobelX[1][1] + RGB6*sobelX[1][2] +
                           RGB7*sobelX[2][0] + RGB8*sobelX[2][1] + RGB9*sobelX[2][2];
                   float gY =  RGB1*sobelY[0][0] + RGB2*sobelY[0][1] + RGB3*sobelY[0][2] +
                           RGB4*sobelY[1][0] + RGB5*sobelY[1][1] + RGB6*sobelY[1][2] +
                           RGB7*sobelY[2][0] + RGB8*sobelY[2][1] + RGB9*sobelY[2][2];
                   gX = gX/256;
                   gY = gY/256;
                   double gS = Math.sqrt(Math.pow(gX,2)+Math.pow(gY,2)); //get magnitude
                   //System.out.println(gS);
                   columnadd += gS; //add up the column
               }
               //System.out.println(columnadd);
               mean += columnadd;
               column[x] = columnadd;
               columnadd = 0;
           }
     
        ArrayList<Integer> list = new ArrayList<Integer>(); //list for points with large transitions
        ArrayList<Integer> glist = new ArrayList<Integer>(); //list of cleaned (good) transition points
        
        int length = column.length;
        mean = mean/(length-1); //get mean
        double sd = stdDev(column); //get standard deviation
        System.out.println(mean);
        System.out.println(sd);
        for(int j = 0; j<length; j++){
            if((column[j]>(mean+1.75*sd))){ //add to list of good transition points
                list.add(j);
            }
        }
        
        ///////////////////////////// Split Audio \\\\\\\\\\\\\\\\\\\\\\\\\\\\\
        AudioInputStream stream = null;
        stream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = stream.getFormat();
        long filelength = file.length(); //length in bytes
        int bytes_p_sec = (int)format.getSampleRate()*format.getSampleSizeInBits()*format.getChannels()/8; //bytes per second of audio
        long fileduration = filelength/bytes_p_sec; //duration of the song in seconds
        AudioInputStream shortstream = null; //cut audio streams
        
        //prevents lines very close together from being drawn multiple times
        //todo: draw the mean of a group instead of the first (may not be helpful actually)
        int previous = 0;
        for(Iterator<Integer> it = list.iterator(); it.hasNext();){
            int current = it.next();
            if((current-previous)>30){ //if the point is far enough from the last one (each pixel is 0.24 seconds, so 30 pixels is 7.2 sec)
                glist.add(current); //add it to the list
            } 
            previous = current;
        }

        int previousMajor = 0; //previous
        int index = 0; //current section of the song
        File directory = new File(dir + "\\" + FilenameUtils.removeExtension(file.getName())); //define directory with the song's name (where segements will be saved)
        directory.mkdirs(); //create the directory
        for(Iterator<Integer> git = glist.iterator(); git.hasNext();){
            index++;
            int current = git.next();
            int copylength = (int)(format.getSampleRate()*fileduration*(current-previousMajor)/length); //get length of song to cut (fraction of entire duration)
            //System.out.println("Current:" + current + " Previous: " + previousMajor + " Length:" + copylength);

            shortstream = new AudioInputStream(stream,format,copylength); //create segement of audio from where we last stopped
            File tempFile = new File(dir + "\\" + FilenameUtils.removeExtension(file.getName()) + "\\" + index + ".wav"); //save the split song into the new folder
            tempFile.createNewFile(); //make audio file
            AudioSystem.write(shortstream, AudioFileFormat.Type.WAVE, tempFile); //write audio
            previousMajor = current;
        }      
        //Save from the last transition to the end of the song
        shortstream = new AudioInputStream(stream,format,stream.available()*8/(format.getSampleSizeInBits()*format.getChannels())); //make the stream only until the end
        index++; //increment index value
        File tempFile = new File(dir + "\\" + FilenameUtils.removeExtension(file.getName()) + "\\" + index + ".wav"); //file to save to
        tempFile.createNewFile();
        AudioSystem.write(shortstream, AudioFileFormat.Type.WAVE, tempFile);
        stream.close(); //close stream when done
        shortstream.close();
    }
}
