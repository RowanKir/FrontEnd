import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;

public class Server{

    private static Connection con;
    private boolean running = true;

    private ServerSocket listener;
    private Socket client;

    protected static ObjectOutputStream outS;
    protected  static ObjectInputStream inS;

    private static ArrayList<Customer> custs = new ArrayList<>();
    private static ArrayList<DVD> dvds = new ArrayList<>();
    private static ArrayList<Rental> rentals = new ArrayList<>();

    public static Connection connect(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:1234/mydatabase?autoReconnect=true&useSSL=false", "root", "root");
        }
        catch (ClassNotFoundException e){
            e.getMessage();
            System.out.println("Failed to load driver.");
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Connection Failed");
        }
        return con;
    }

    public Server(){
        try{
            listener = new ServerSocket(3434,10);

        }
        catch(IOException e){
            e.getMessage();
        }
    }

    public void listen(){
        try {
            client = listener.accept();
            System.out.println("SERVER>>> Listening.");

            while(running){
                processClient();
            }
        }
        catch(IOException e){
            e.getMessage();
        }
    }

    public void processClient(){

            try {
                outS = new ObjectOutputStream(client.getOutputStream());
                outS.flush();
                inS = new ObjectInputStream(client.getInputStream());

                String clientMsg = "";

                    clientMsg = (String) inS.readObject();
                    System.out.println(clientMsg);

                    if(clientMsg.equalsIgnoreCase("CLOSE")){
                        outS.writeObject("Shutting down database and server.");
                        outS.flush();
                        running = false;
                        Database.closeDB();
                        outS.close();
                        inS.close();
                        client.close();
                        listener.close();
                    }

                    else if (clientMsg.equalsIgnoreCase("GIVECUSTS")) {
                        ArrayList<Customer> gc = Database.getAllCustomers();
                        outS.writeObject(gc);
                    }
                    else if (clientMsg.equalsIgnoreCase("GIVEDVDS")) {
                        ArrayList<DVD> gd = Database.getAllDVDs();
                        outS.writeObject(gd);
                    }
                    else if(clientMsg.equalsIgnoreCase("GIVERENTALS")){
                        ArrayList<Rental> gr = Database.getAllRentals();
                        outS.writeObject(gr);
                    }
                    else if (clientMsg.charAt(0) == 'A') {
                        String proClient = clientMsg.substring(1);
                        Database.executeUpdate(proClient);
                        outS.writeObject("Customer successfully added to database.");
                    }
                    else if (clientMsg.charAt(0) == 'B') {
                        String proClient = clientMsg.substring(1);
                        Database.executeUpdate(proClient);
                        outS.writeObject("Customer Successfully Removed.");
                    }
                    else if (clientMsg.charAt(0) == 'C') {
                        String proClient = clientMsg.substring(1);
                        Database.executeUpdate(proClient);
                        outS.writeObject("Dvd Successfully Added.");
                    }
                    else if (clientMsg.charAt(0) == 'D') {
                        String proClient = clientMsg.substring(1);
                        Database.executeUpdate(proClient);
                        outS.writeObject("Dvd Successfully Removed.");
                    }

                    //IF THE MESSAGE CHAR AT 1 IS 'E' EXECUTE SQL FOR ADDING RENTAL BY:
                    //SENDING CUST ARRAY TO CLIENT (MAKE A METHOD TO TAKE IN AN ARRAY FROM SERVER AND CHECK IF THE SELECTED CLIENTS ID MATCHES AN ID IN THE ARRAY SENT BY THE SERVER)
                    //SENDING DVD ARRAY TO CLIENT (MAKE A METHOD TO TAKE IN AN ARRAY FROM SERVER AND CHECK IF THE SELECTED DVDS ID MATCHES A DVD ID IN THE ARRAY SENT BY THE SERVER)
                    //RECEIVE THE CUSTOMER AND DVD SELECTED BY THE CLIENT (AT THIS POINT THE CLIENT HAS CHECKED THAT THESE TWO OBJECTS ACTUALLY EXIST AND ARE BOTH AVAILABLE)
                    //UPDATE CUSTOMER TABLE: SET THAT CUSTOMERS CANRENT TO FALSE
                    //UPDATE DVD TABLE: SET THAT DVDS AVAIL TO FALSE
                    //INSERT A NEW RENTAL TO THE RENTAL TABLE
                    //SEND A MESSAGE TO CLIENT TO SHOW RENTAL IS COMPLETED
                    else if (clientMsg.charAt(0) == 'E') {
                        String proClient = clientMsg.substring(1);
                        //do validation in client and execute sql???
                    }

                    //IF THE MESSAGE CHAR AT 1 IS 'F' EXECUTE SQL FOR DELETING RENTAL (RETURNING DVD) BY:
                    //RECEIVING THE RENTAL ID FROM CLIENT
                    //UPDATE CUSTOMER TABLE: GET CUSTID FROM RENTAL ARRAY & SET THAT CUSTOMERS CANRENT TO TRUE
                    //UPDATE DVD TABLE: GET DVDID FROM RENTAL ARRAY SET THAT DVDS AVAIL TO TRUE
                    //DETERMINE IF DVD IS OVERDUE (GET DVDID FROM RENTAL ARRAY & FIND THIS DVD IN THE DVD ARRAYLIST & EXECUTE METHOD TO DETERMINE IF OVERDUE
                    //IF THE DVD IS OVERDUE:
                    //GET DAYS OVERDUE AND CALCULATE PRICE
                    //SEND MESSAGE TO CLIENT THAT DVD IS OVERDUE AND THE PRICE
                    //CLIENT NEEDS TO DEDUCT THAT PRICE FROM THE CUSTOMERS CREDIT
                    //REMOVE THAT RENTAL FROM RENTAL TABLE
                    //UPDATE THAT CUSTOMERS CREDIT IN THE CUSTOMER TABLE ONCE THEYVE PAID
                    //IF THE DVD IS NOT OVERDUE:
                    //REMOVE THAT RENTAL FROM RENTAL TABLE
                    //SEND MESSAGE TO CLIENT THAT RENTAL HAS BEEN COMPLETED

                    //IF THE MESSAGE CHAR AT 1 IS (cant do g because of giveArray methods)'H' EXECUTE SQL FOR RETURNING SORTED LIST OF DVDS(BY CATEGORY/AVAILABLE)
                    //USE DATABASES returnDvdArray() METHOD
                    //SORT THE ARRAY BY CATEGORY/AVAILABILITY
                    //SEND SORTED ARRAY TO CLIENT
                    //CLIENT ADDS SORTED ARRAY TO DESIRED TABLES (2)

                    //IF THE MESSAGE CHAR AT 1 IS 'I' EXECUTE SQL FOR RETURNING SORTED LIST OF CUSTS(BY NAME/CANRENT)
                    //USE DATABASES returnCustArray() METHOD
                    //SORT THE ARRAY BY NAME/CANRENT
                    //SEND SORTED ARRAY TO CLIENT
                    //CLIENT ADDS SORTED ARRAY TO DESIRED TABLES (2)

                    //if the message char at 0 is 'J' EXECUTE SQL FOR RETURNING RENTALS

                if(clientMsg.charAt(0) == 'K'){
                    String proClient = clientMsg.substring(1);
                    Database.executeUpdate(proClient);
                    outS.writeObject("Credit Successfully added.");
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    public static void main(String[] args){
        Server s = new Server();
        connect();
        System.out.println("SERVER>>> Connecting to database...\nSERVER>>> Connected\nSERVER>>> Listening...");
        s.listen();
    }
}