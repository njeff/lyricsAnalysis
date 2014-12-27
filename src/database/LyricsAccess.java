/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database-related code
 * 
 * @author Jeffrey
 */
public class LyricsAccess {
    
    /**
     * Connects to a database
     * Connection code is from http://www.mkyong.com/jdbc/connect-to-oracle-db-via-jdbc-driver-java/
     * 
     * @param database The name of the database you want to connect to
     * @return The connection to the database
     */
    public static Connection startconnection(String database){
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
	} catch (ClassNotFoundException e) {
            System.out.println("Where is your Oracle JDBC Driver?");
            e.printStackTrace();
            return null;
	}
 
	Connection connection = null;
 
	try {
            connection = DriverManager.getConnection( //get connection to specified database
			"jdbc:oracle:thin:@localhost:1521:"+database, "C##PROTOTYPE",
			"oracle10g");
	} catch (SQLException e) {
		System.out.println("Connection Failed! Check output console");
		e.printStackTrace();
                return null;
	}
 
	if (connection != null) {
		System.out.println("Connected.");
	} else {
		System.out.println("Failed to make connection.");
	}
        return connection;
    }
    
    /**
     * Saves a song to the database
     * Tables: ARTIST_TABLE
     *         SONG_TABLE
     *         SONGMOOD_TABLE
     *         SUBSONG_TABLE
     * 
     * @param con Database connection
     * @param title Name of the song
     * @param artist Artist of the song
     * @param length Length of the song in seconds
     * @param lyrics Lyrics of the song without timestamp
     * @param timeLyrics Lyrics with timestamp
     * @param moods Array of all the possible moods the song is
     * @throws SQLException 
     */
    public static void saveto(Connection con, String title, String artist, int length, String lyrics, String timeLyrics, int[] moods) throws SQLException{
        ////////////////////////////////check to see if the artist is already registered
        int artistid = 0;
        boolean artistpre = false;
        Statement stmt = null;
        String query =
            "SELECT ARTISTID FROM ARTIST_TABLE WHERE ARTISTNAME = '" + artist + "'"; 
        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) { //if the artist isn't found the artistid variable will remain 0
                artistid = rs.getInt("ARTISTID");
                break;
            }
        } catch (SQLException e) {
            System.err.println(e);
        } finally {
            if (stmt != null) { stmt.close(); }
        }
        
        ////////////////////////////////if the artist was registered check to see if the song was already registered
        int prevSongID = 0; //place to hold songid of song if it is already registered
        boolean songcheck = false;
        if(artistid != 0){ 
            Statement stmt2 = null;
            String query2 =
                "SELECT SONGNAME,ARTISTID,SONGID FROM SONG_TABLE WHERE SONGNAME = '" + title + "' AND ARTISTID = '" + artistid + "'"; //check to see if the song is already registered
            try {
                stmt2 = con.createStatement();
                ResultSet rs = stmt2.executeQuery(query2);
                if(rs.next()) { //if the song is already registered
                    prevSongID = rs.getInt("SONGID"); //save its id
                    songcheck = true;
                }
            } catch (SQLException e) {
                System.err.println(e);
            } finally {
                if (stmt2 != null) { stmt2.close(); }
            }
        }
             
        int songid = 0;
        ////////////////////////////////if the song was already there
        if(songcheck){ //check to see it any moods need to be added
            ArrayList<Integer> regMoods= new ArrayList<Integer>(); //already registered moods
            Statement stmt3 = null;
            String query3 =
                "SELECT MOOD FROM SONGMOOD_TABLE WHERE SONGID = '" + prevSongID + "'";
            try {
                stmt3 = con.createStatement();
                ResultSet rs = stmt3.executeQuery(query3);
                while(rs.next()) { 
                    regMoods.add(rs.getInt("MOOD"));
                }
            } catch (SQLException e) {
                System.err.println(e);
            } finally {
                if (stmt3 != null) { stmt3.close(); }
            }
            
            for(int i = 0; i < moods.length; i++){
                if(!regMoods.contains(moods[i])){ //if the song doesn't have a mood registered yet
                    PreparedStatement ms = con.prepareStatement("INSERT INTO SONGMOOD_TABLE (SONGID, MOOD) VALUES (?,?)");
                    ms.setInt(1,prevSongID);
                    ms.setInt(2,moods[i]); //register that mood
                    ms.addBatch();
                    ms.executeBatch();
                }
            }
                
            return; //exit
            
        } else { //find the highest song ID
            Statement stmt4 = null;
            String query4 =
                "SELECT MAX(SONGID) \"MAXid\" FROM SONG_TABLE"; //check to see if the song is already registered
            try {
                stmt4 = con.createStatement();
                ResultSet rs = stmt4.executeQuery(query4);
                if(rs.next()) { 
                    songid = rs.getInt("MAXid")+1; //add one to get the next songid
                }
            } catch (SQLException e) {
                System.err.println(e);
            } finally {
                if (stmt4 != null) { stmt4.close(); }
            }
        }
        
        ////////////////////////////////if artist isn't registered, find the next artistid
        if(artistid == 0){ 
            Statement stmt5 = null;
            String query5 =
                "SELECT MAX(ARTISTID) \"MAXid\" FROM ARTIST_TABLE"; //get greatest artist id
            try {
                stmt5 = con.createStatement();
                ResultSet rs = stmt5.executeQuery(query5);
                while (rs.next()) {
                    artistid = rs.getInt("MAXid")+1; //add one to get the next artist id
                    artistpre = true;                  
                }
            } catch (SQLException e) {
                System.err.println(e);
            } finally {
                if (stmt5 != null) { stmt5.close(); }
            }
        }
        
        con.setAutoCommit(false); //disable autocommit
        
        PreparedStatement pstmt; //for inserting into the main song table
        PreparedStatement pstmt2; //for insterting into the artists table
        PreparedStatement pstmt3; //for inserting into the song mood table
        
//        System.out.println(title);
//        System.out.println(artistid);
//        System.out.println(songid);
//        System.out.println(lyrics);
//        System.out.println(length);
        
        if(!artistpre){ //if artist was already registered
            pstmt = con.prepareStatement("INSERT INTO SONG_TABLE (SONGNAME, ARTISTID, SONGID, LYRICS, TIME_LYRICS, LENGTH) VALUES (?,?,?,?,?,?)"); //insert song
            pstmt.setString(1, title);
            pstmt.setInt(2, artistid);
            pstmt.setInt(3, songid); 
            pstmt.setString(4, lyrics);
            pstmt.setString(5, timeLyrics);
            pstmt.setInt(6, length);
        }
        else{ //if artist wasn't registered before
            pstmt = con.prepareStatement("INSERT INTO SONG_TABLE (SONGNAME, ARTISTID, SONGID, LYRICS, TIME_LYRICS, LENGTH) VALUES (?,?,?,?,?,?)"); //insert song
            pstmt.setString(1, title); 
            pstmt.setInt(2, artistid); 
            pstmt.setInt(3, songid); 
            pstmt.setString(4, lyrics); 
            pstmt.setString(5, timeLyrics);
            pstmt.setInt(6,length);
            
            pstmt2 = con.prepareStatement("INSERT INTO ARTIST_TABLE (ARTISTNAME, ARTISTID) VALUES (?,?)"); //put the artist and his/her id into the artist table
            pstmt2.setString(1, artist);
            pstmt2.setInt(2, artistid);
            pstmt2.addBatch();
            pstmt2.executeBatch();
        }
                
        pstmt.addBatch();
        
        //puts in the mood(s) for the song
        for(int i = 0; i<moods.length; i++){ //loop though all moods
            pstmt3 = con.prepareStatement("INSERT INTO SONGMOOD_TABLE (SONGID, MOOD) VALUES (?,?)");
            pstmt3.setInt(1, songid);
            pstmt3.setInt(2, moods[i]);
            pstmt3.addBatch();
            pstmt3.executeBatch();
        }
        
        try {
            pstmt.executeBatch();
            con.commit(); //commit changes
            con.setAutoCommit(true); //reenable auto commit
        } catch (SQLException e){
            System.err.println(e);
        } finally {
            if (pstmt != null) { pstmt.close(); }
        }
    }
    
    public static void saveAlbum(Connection con, String album){
        
    }
    
    
    /**
     * Save subsong moods to the database
     * 
     * @param con Database connection
     */
    public static void saveSubsong(Connection con){
        
    }
    
    /**
     * Dumps lyrics and their moods into a .arff format
     * 
     * @param con Database connection
     * @param destination File to output to
     */
    public static void retrieveSave(Connection con, String destination){
        PrintWriter writer = null;
        Statement stmt = null;
        try {   
            writer = new PrintWriter(destination,"UTF-8");
            writer.println("@RELATION songdump");
            writer.println("@ATTRIBUTE lyrics string");
            writer.println("@ATTRIBUTE category {0,1,2,3,4,5,6,7}");
            writer.println("@DATA");
            for(int i = 0; i<8; i++){ //loop though each possible mood
                String query =
                    "SELECT LYRICS FROM SONG_TABLE INNER JOIN SONGMOOD_TABLE ON SONG_TABLE.SONGID = SONGMOOD_TABLE.SONGID WHERE SONGMOOD_TABLE.MOOD = " + i; 
            
                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) { 
                    writer.println("\"" + rs.getString("LYRICS").trim().replace("\"", "") + "\"," + i); //sanitize output
                    System.out.println("\"" + rs.getString("LYRICS").trim().replace("\"", "") + "\"," + i);
                }
            }
        } catch (Exception e) { //error handling
            System.err.println(e);
        } finally {
            writer.close();
            if (stmt != null) { 
                try {
                    stmt.close(); //close connection
                } catch (SQLException ex) {
                    Logger.getLogger(LyricsAccess.class.getName()).log(Level.SEVERE, null, ex); //more error handling for the close
                }
            }
        }
    }
}
