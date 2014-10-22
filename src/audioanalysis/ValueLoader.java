/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package audioanalysis;

import java.awt.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author Jeffrey
 */
public class ValueLoader {
    String dir = "";
    float max = Float.MIN_VALUE;
    float min = Float.MAX_VALUE;
    float x = 13;
    float y = 0;
    
    public ValueLoader(String dir){
        this.dir = dir;
        
        try(BufferedReader br = new BufferedReader(new FileReader(dir))) {
           String line = ""; 
           while ((line = br.readLine()) != null) {
               y++;
           }
        } catch (Exception ex){
           ex.printStackTrace();
        }       
    }
    
    public double[] getRow(int y){
        int j =0 ;
        if(x < 0 || y < 0){
            return null;
        }
        float intermediate = 0;
        int c[] = new int[12];
        double f[] = new double[13];
        String s[] = new String[13];
        String number = "";

        try(BufferedReader br = new BufferedReader(new FileReader(dir))) {
            String line = ""; 
            while ((line = br.readLine()) != null) {
                if(j<y){
                    j++;
                } else {
                    c[0] = line.indexOf(",");
                    //System.out.println(c[0]);
                    for(int i =1; i <12; i++){
                        c[i] = line.indexOf(",", c[i-1]+1);

                        if(i==1){
                            //System.out.println(line.substring(0, c[0]));
                            s[0] = line.substring(0,c[0]);
                            f[0] = Double.valueOf(s[0]);
                        }    
                        //System.out.println(line.substring(c[i-1]+1, c[i]));
                        s[i] = line.substring(c[i-1]+1, c[i]);
                        f[i] = Double.valueOf(s[i]);
                        //System.out.println(c[i]);
                    }
                    //System.out.println(line.substring(c[11]+1));
                    s[12] = line.substring(c[11]+1);
                    f[12] = Double.valueOf(s[12]);
                    //(intermediate);
                    break;
                }
            }
        } catch (Exception ex){
           ex.printStackTrace();
        }
        return f;
    }
}
