/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
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
			"jdbc:oracle:thin:@localhost:1521:"+database, "sys as sysdba",
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
     * 
     * @param con Database connection
     * @param title Name of the song
     * @param artist Artist of the song
     * @param length Length of the song in seconds
     * @param lyrics Lyrics of the song
     * @param moods Array of all the possible moods the song is
     * @throws SQLException 
     */
    public static void saveto(Connection con, String title, String artist, int length, String lyrics, int[] moods) throws SQLException{
        //check to see if the artist is already registered
        int artistid = 0;
        boolean artistpre = false;
        Statement stmt = null;
        String query =
            "SELECT ARTISTID FROM ARTISTS WHERE ARTISTNAME = '" + artist + "'"; 
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
        
        //if the artist was registered check to see if the song was already registered
        if(artistid != 0){ 
            Statement stmt2 = null;
            String query2 =
                "SELECT TITLE,ARTISTID,SONGID FROM SONGTABLE WHERE TITLE = '" + title + "' AND ARTISTID = '" + artistid + "'"; //check to see if the song is already registered
            try {
                stmt2 = con.createStatement();
                ResultSet rs = stmt2.executeQuery(query2);
                if(rs.next()) { //if the song is already registered
                    return; //exit function
                }
            } catch (SQLException e) {
                System.err.println(e);
            } finally {
                if (stmt2 != null) { stmt2.close(); }
            }
        }
               
        //if artist isn't registered
        if(artistid == 0){ 
            Statement stmt3 = null;
            String query3 =
                "SELECT MAX(ARTISTID) \"MAXid\" FROM ARTISTS"; //get greatest artist id
            try {
                stmt3 = con.createStatement();
                ResultSet rs = stmt3.executeQuery(query3);
                while (rs.next()) {
                    artistid = rs.getInt("MAXid"); //get the greatest (last inputted) artist id
                    artistpre = true;                  
                }
            } catch (SQLException e) {
                System.err.println(e);
            } finally {
                if (stmt3 != null) { stmt3.close(); }
            }
        }
        
                
        
        con.setAutoCommit(false); //disable autocommit
        
        PreparedStatement pstmt; //for inserting into the main song table
        PreparedStatement pstmt2; //for insterting into the artists table
        PreparedStatement pstmt3; //for inserting into the song mood table
        
        if(!artistpre){ //if artist was already registered
            pstmt = con.prepareStatement("INSERT INTO SONGTABLE (TITLE, ARTISTID, LYRICS) VALUES (?,?,?,?)"); //all analyzedflags are initialized as zero
            pstmt.setString(1, title); //only insert the song and artist id into the maintable
            pstmt.setInt(2, artistid);
            pstmt.setInt(3, length); //write lengthid to database (length is in divisons of 15)
            pstmt.setString(4, lyrics);
            
            
        }
        else{ //if artist wasn't registered before
            pstmt = con.prepareStatement("INSERT INTO SONGTABLE (TITLE, ARTISTID, LYRICS) VALUES (?,?,?,?)"); //FIXED
            pstmt.setString(1, title); //not only insert into the maintable but also insert into the artists table to register new artist
            pstmt.setInt(2, artistid + 1); //add one to the greatest (last inputted) artist id to get new artist id (IDs will be in increments of one)
            pstmt.setInt(3, length); //isert length of songs in seconds
            pstmt.setString(4, lyrics); //insert cleaned lyrics
            
            pstmt2 = con.prepareStatement("INSERT INTO ARTISTS (ARTISTNAME, ARTISTID) VALUES (?,?)"); //put the artist and his/her id into the artist table
            pstmt2.setString(1, artist);
            pstmt2.setInt(2, artistid + 1); //duplicate of the earlier "artistid + 1" for the artist table
            pstmt2.addBatch();
            pstmt2.executeBatch();
        }
        pstmt.addBatch();
        try {
            int [] updateCounts = pstmt.executeBatch();
            con.commit(); //commit changes
            con.setAutoCommit(true); //reenable auto commit
        } catch (SQLException e){
            System.err.println(e);
        } finally {
            if (pstmt != null) { pstmt.close(); } //close connection
        }
    }
}
