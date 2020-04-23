//package ClientServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class ip
{
        public static String ip_addr = "localhost";
}

class Client_Send implements Runnable
{
    String send = "";
    Socket s;
    DataInputStream kb = null;
    DataOutputStream socoutput = null;

    Client_Send(Socket s) throws IOException
    {
        this.s = s;
        socoutput = new DataOutputStream(s.getOutputStream());
        kb = new DataInputStream(System.in);
    }

    void send() throws IOException
    {
        send = kb.readLine();
        socoutput.writeUTF(send);

        if(send.equals("bye"))
            return;
    }

    public void run()
    {
        try
        {
            while(true)
            {
                if(send.equals("bye"))
                    break;

                send();
            }

            s.close();
        }

        catch(IOException e)
        {
            System.out.println(e);
        }

    }
}

class Client_Receive implements Runnable
{
    String receive = "";
    DataInputStream socinput = null;

    Client_Receive(Socket s) throws IOException
    {
        socinput = new DataInputStream(s.getInputStream());
    }

    void receive() throws IOException
    {
        receive = socinput.readUTF();
        System.out.println(receive);
    }

    public void run()
    {
        try
        {
            while(true)
            {
                receive();
            }

        }

        catch(IOException e)
        {
            System.out.println(e);
        }
    }
}

public class Client_GroupChat
{
    Socket s = null;

    Client_GroupChat(String ip,int port) throws IOException
    {
        s = new Socket(ip,port);
        System.out.println("Connection Established");
    }

    public static void main(String[] args) throws IOException
    {
        Client_GroupChat client = new Client_GroupChat(ip.ip_addr,5000);
        Client_Send send_msg = new Client_Send(client.s);
        Client_Receive receive_msg = new Client_Receive(client.s);

        Thread receive = new Thread(send_msg);
        Thread send = new Thread(receive_msg);

        send.start();
        receive.start();
    }
}
