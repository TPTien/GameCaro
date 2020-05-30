package server;

import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

public class Server implements Runnable{
    
    
    // Server Socket
    ServerSocket serversocket;
    //Socket socket;
    OutputStream os;// ....
    InputStream is;// ......
    ObjectOutputStream oos = null;// .........
    ObjectInputStream ois =null;//
    private boolean running =false;
    //
//    private Connection[] connection = new Connection[10];
    public ArrayList<Connection> listWaiting= new ArrayList<>();
    public ArrayList<ArrayList<Connection> > listPlaying =  
                  new ArrayList< >();
    
    public ArrayList<Connection> room=new ArrayList<>();
    static int count =0;
    
    //MenuBar
    MenuBar menubar;
    
    
    public Server() {
        start();
        

        
    }
    
    private  void start(){
        try {
            serversocket = new ServerSocket(1234);
            System.out.println("Dang doi client...");
            new Thread(this).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
    }
    public void shutdown(){
        running=false;
        try {
//            ois.close();
//            oos.close();
//            socket.close();
            serversocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public synchronized void addClient(Connection client){
        listWaiting.add(client);
    }
    
    public int getNumClients(){
        return listWaiting.size();
    }
    public int getNumClientsWaiting(){
        return listPlaying.size();
    }
    public String getClientName(int i){
        return listWaiting.get(i).getCName();
    }
    
    public synchronized void play(Connection client){
        if(!room.contains(client)){
            room.add(client);
        }
        else{
            System.out.println("Đã tồn tại trong phòng");
            
        }
         if(room.size()==2){

        ArrayList<Connection> roomtemp=new ArrayList<>();
        roomtemp.addAll(room);
        listPlaying.add(roomtemp);
        room.clear();

    }
        
            
    }
    public void removeWaiting(Connection client){
        listWaiting.remove(client);
    }
    public void removeRoom(Connection client){
       for(ArrayList<Connection> list :listPlaying){
           if(list.contains(client)){
               listWaiting.addAll(list);
               listPlaying.remove(list);
               break;
           }
       }
    }
  public int randomWhoFirst(){
        Random r = new Random();
        int low = 0;
        int high = 2;
        int result= r.nextInt(2) + low;
        System.out.println("random "+ result);
        return result;
    }
    public static void main(String[] args) {
        new Server();
    }
    @Override
    public void run() {
        running =true;
        while(running ) {
            try {
                 Socket socket = serversocket.accept();
                
                    System.out.println("Connection from:" + socket.getInetAddress());
                   Connection connection =new Connection(this,socket);
                   Thread t= new Thread(connection);
                   //list.add(connection);
                   t.start();
                   //randomWhoFirst();
                   count++;
                    
                     
                 
                
                if(socket.isConnected()){
                    System.out.println("Client " +count+  " đã kết nối!");
                }
                else{
                    System.out.println("Client"+count +"đã thoát");
                }
                //ois=new ObjectInputStream(socket.getInputStream());
                
            } catch (Exception e) {
                e.printStackTrace();
            }
                       
        }
        shutdown();
        System.out.println("ngat ket noi");
        
    }
    private class Connection implements Runnable{
        private Socket socket=null;
        private Server s;
        private ObjectOutputStream out =null;
        private ObjectInputStream in=null;
       
        private boolean   isFounded=false;
        String clientname;
        public Connection(Server s,Socket socket) throws IOException {
            this.socket = socket;
            this.s =s;
            out =new ObjectOutputStream(socket.getOutputStream());
            in= new ObjectInputStream(socket.getInputStream());
        }
        
        @Override
        public void run() {
            try {
                while(socket.isConnected()){
                    
                    try {
                        String stream = in.readObject().toString();
                        System.out.println(stream);
                        String[] data=stream.split(",");
                       
                        if(data[0].equals("login")){
                            clientname=data[3];
                            s.addClient(this);
                        }
                        if(data[0].equals("waiting")){
                            s.play(this);
                            s.removeWaiting(this);
                            System.out.println(s.listPlaying.size());
                            //System.out.println("â" +s.listPlaying.get(0).size());
                            System.out.println("list waiting" +s.listWaiting.size());
                            
                                    
                            if(s.listPlaying.size()>=1 && !isFounded){
                            for(int i=0 ;i<s.listPlaying.size();i++){
                                   for(int j=0;j<s.listPlaying.get(i).size();j++){
                                     if(s.listPlaying.get(i).get(j).clientname.equals(data[3])){
//                                         
                                            int temp =randomWhoFirst();
                                            if(temp==0){
                                                s.listPlaying.get(i).get(j).out.writeObject("opponentName,"+s.listPlaying.get(i).get(j-1).clientname+","+(temp+1));
                                                s.listPlaying.get(i).get(j-1).out.writeObject("opponentName,"+s.listPlaying.get(i).get(j).clientname+","+temp);

                                                s.listPlaying.get(i).get(j).out.flush();
                                                isFounded=true;
                                                break;
                                            }
                                            else{
                                                s.listPlaying.get(i).get(j).out.writeObject("opponentName,"+s.listPlaying.get(i).get(j-1).clientname+","+(temp-1));
                                                s.listPlaying.get(i).get(j-1).out.writeObject("opponentName,"+s.listPlaying.get(i).get(j).clientname+","+temp);

                                                s.listPlaying.get(i).get(j).out.flush();
                                                isFounded=true;
                                                break;
                                            }
                                            
                                            
                                     }
                                   }
                            }
                        }
                            
                            
                    }

                        
                         if(s.listPlaying.size()>=1 || data[0].equals("exitroom")){                         
                                for (int i = 0; i < s.listPlaying.size(); i++) { 

                                            System.out.println("room: "+i+1 +" "+s.listPlaying.get(i).size());
                                            for (int j = 0; j < s.listPlaying.get(i).size(); j++) 
                                                if(s.listPlaying.get(i).get(j).clientname.equals(data[3])){
                                                    System.out.println("name: " +s.listPlaying.get(i).get(j).clientname);
                                                    s.listPlaying.get(i).get(j).out.writeObject(stream);
                                                    s.listPlaying.get(i).get(j).out.flush();
                                                    break;
                                                    
                                                }
                                                 
                                                     
                                                 
                                                
                                            }
                                   
                                }
                         if(data[0].equals("exitroom")){
                            s.removeRoom(this);
                             System.out.println("number room: " +s.listPlaying.size());
                            //s.addClient(this);
                            System.out.println("list waiting: " +s.listWaiting.size());
                            isFounded=false;
                            
                         }
                        if(data[0].equals("exitgame")){
                            
                            s.removeRoom(this);
                            if(s.listWaiting.contains(this)){
                                s.removeWaiting(this);
                            }
                            System.out.println("number room: " +s.listPlaying.size());
                            //s.addClient(this);
                            System.out.println("list waiting: " +s.listWaiting.size());
                            isFounded=false;
                            //this.close();
                        }

                    } catch (Exception e) {
                        //this.close();
                        e.printStackTrace();
//                        this.out=null;
//                        this.in=null;
                        this.out.close();
                        this.in.close();
                        
                        
                        count--;
                        break;
                        
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            }
        public void sendObject(Object pacObject){
            try {
                out.writeObject(pacObject);
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void close(){
            try {
                in.close();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public String getCName(){
            return clientname;
        }
        
    }
    
}
