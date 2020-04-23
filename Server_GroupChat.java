//package ClientServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class global
{
    public static ReadWriteLock locks =  new ReentrantReadWriteLock(true);
    public static int last_client = -1;
    public static int n = 3;
    public static M_Client_Handler clients [] = new M_Client_Handler [n];
}


class M_Client_Handler implements Runnable
{
    Socket s = null;
    DataInputStream socinput = null;
    DataInputStream kb = null;
    DataOutputStream socoutput = null;
    String name = "";
    int index = -1;

    M_Client_Handler(Socket s,String name, int index) throws IOException
    {
        this.index = index;
        this.name = name;
        this.s = s;

        if(this.s != null)
        {
            socinput = new DataInputStream(s.getInputStream());
            kb = new DataInputStream(System.in);
            socoutput = new DataOutputStream(s.getOutputStream());
        }

    }

    @Override
    public void run()
    {
      while(true)
      {
          try
          {
              String received = socinput.readUTF();

              if(received.equals("bye"))
              {


                  M_Client_Handler indicate = new M_Client_Handler(null,"-1",-1);

                  try
                  {
                      global.locks.writeLock().lock();

                      global.clients[this.index] = indicate;
                      this.index = -1;
                      s.close();

                  }

                  finally
                  {
                      global.locks.writeLock().unlock();
                  }

                  return;
              }

              received = "Received from "+this.name+" "+ received;

              try
              {
                  global.locks.readLock().lock();
                  
                  for (int i = 0 ; i < global.n ; ++i)
                  {
                      if(global.clients[i] != this && global.clients[i].s != null)
                      {
                          global.clients[i].socoutput.writeUTF(received);
                      }
                  }
              }

              finally
              {
                  global.locks.readLock().unlock();
              }
          }

          catch (IOException e)
          {
              e.printStackTrace();
          }
      }
  }

}


public class Server_GroupChat
{
    ServerSocket ss = null;

    Server_GroupChat(int port) throws IOException
    {
        ss = new ServerSocket(port);
        System.out.println("Socket Established");

        while(true)
        {
            Socket s = ss.accept();
            System.out.println("Connection Established");

            DataInputStream socinput = new DataInputStream(s.getInputStream());
            DataOutputStream socoutput = new DataOutputStream(s.getOutputStream());

            socoutput.writeUTF("Enter Your Name");
            String name = socinput.readUTF();

            while(global.clients[(global.last_client+1) % global.n].s != null)
            {
                if(global.last_client == global.n-1)
                    global.last_client = 0;

                else
                    global.last_client += 1;
            }

            if(global.last_client == global.n-1)
                global.last_client = 0;

            else
                global.last_client += 1;

            M_Client_Handler c = new M_Client_Handler(s,name,global.last_client);
            global.clients[global.last_client] = c;


            Thread t = new Thread(c);

            t.start();
        }
    }

    public static void main(String[] args) throws IOException
    {
        M_Client_Handler indicate = new M_Client_Handler(null,"-1",-1);

        for (int i = 0 ; i < global.n ; i++)
            global.clients[i] = indicate;

        Server_GroupChat server = new Server_GroupChat(5000);
    }
}

