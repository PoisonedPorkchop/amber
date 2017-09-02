package haven.mod.discord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * A simple Swing-based client for the capitalization server.
 * It has a main frame window with a text field for entering
 * strings and a textarea to see the results of capitalizing
 * them.
 */
public class Client implements MessageListener{

    private BufferedReader in;
    private PrintWriter out;

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Enter in the
     * listener sends the textfield contents to the server.
     */
    public Client() {

    	try {
			connectToServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Failed to connect to the server...");
			//e.printStackTrace();
		}
    }
    
    public void listen()
    {
    	System.out.println("Now listening!");
    	Thread listen = new Thread(new Runnable() {
    	    
    	    public void run()
    	    {
    	    	boolean stop = true;
    	    	while(stop)
    	    	{
    	    		String response;
    	    		try {
                      response = in.readLine();
                      if (response == null || response.equals("")) {
                    	  
                      }
                      else if(response.equals("exit101"))
                      {
                    	  stop = false;
                      }
                      else
                      {
                    	  sayMessage(response);
                    	  //mod w/e print shit goes here.
                      }
                  } catch (IOException | NullPointerException ex) {
                         response = "Error: " + ex;
                  }
    	    	}
    	         // code goes here.
    	    }});  
    	    listen.start();
    }

    /**
     * Implements the connection logic by prompting the end user for
     * the server's IP address, connecting, setting up streams, and
     * consuming the welcome messages from the server.  The Capitalizer
     * protocol says that the server sends three lines of text to the
     * client immediately after establishing a connection.
     */
    String host = "play.flossynetworks.net";
    //String host = "0.0.0.0";
    public void connectToServer() throws IOException {

        // Get the server address from a dialog box.

        // Make connection and initialize streams
        Socket socket = new Socket(host, 9898);
        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

    }

	@Override
	public void messageReceived(String msg) {
		System.out.println("Server Response was: " + msg);
		
	}
	public static Initiater initiater = new Initiater();
	
	public void sayMessage(String msg)
	{		
        initiater.message(msg);
	}
	
	/**
	 * This method sends a message to the server.
	 * @param msg The stirng message to be sent to the server.
	 */
	public void sendMessage(String msg)
	{

	}
	
	public void debugMessage(String debug)
	{
		if(out != null)
		out.println("discord debug " + debug);
	}
	
	public void debugMessage(String debug, String user)
	{
		if(out != null)
		out.println("discord user debug " + user + " " + debug);
	}
	
//	public static void main(String[] args)
//	{
//		Client client = new Client();
//		client.sendMessage("Test for response.");
//		client.listen();
//		client.debugMessage("My anaconda don't!", "Red");
//		client.debugMessage("I am too fly!");
//	}


}
