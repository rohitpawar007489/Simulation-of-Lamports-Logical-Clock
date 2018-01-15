
/**
 * 
 */

/**
 * @author Rohit
 * DC Assignment 1
 *
 */

//Import all the required packages

//For I/O communication
import java.io.*;
//For Socket communication
import java.net.*;
//To generate random number
import java.util.Random;



//Client communicate to server and synchronize clock value based on event
public class Client {

	//program execution starts from here
	public static void main(String[] args) {
		
		//check required number of parameters are passed
		if(args.length <=4)
		{
			System.out.println("Usage is: > java Client [server address] [server port number] [send probability][receive probability][internal events probability]");
			return;
		}
		
		//variable declaration and initialization 
		String hostName = args[0];
		int serverPort=Integer.parseInt(args[1]);
		float sendProbability = Float.parseFloat(args[2]);
		float receiveProbability = Float.parseFloat(args[3]);
		float internalEventProbability = Float.parseFloat(args[4]);
		int maxIterations = 10000;
	
		Socket clientSocket=null;
		PrintWriter out = null;
	    BufferedReader in=null;
	    int clientClockValue=0;
	    //KEY to encrypt and decrypt offset
	    final int KEY=20;
		
	    //To write data into text file to generate graph
	    /*BufferedWriter bufferedWriter = null;
		FileWriter fileWriter = null;
		String fileName=null;*/
		try 
		{
			//hostName = InetAddress.getLocalHost().getHostName();
			//Create client socket
			clientSocket = new Socket(hostName, serverPort);
			/*fileName="Client_"+clientSocket.getLocalPort()+".txt";
			fileWriter = new FileWriter(fileName);
			bufferedWriter = new BufferedWriter(fileWriter);
			*/
			//create I/O stream associated with client socket
		    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		    out = new PrintWriter(clientSocket.getOutputStream(), true);
		    System.out.println("Client Info:"+clientSocket.toString());
			//waiting for server to connect to all the client
			while(true)
			{
			    String serverResponse= in.readLine();
			    if(serverResponse!=null)
			    {
			    	if(serverResponse.toUpperCase().contains("START"))
			    	{
			    		System.out.println("Received START event from Server");
			    		break;
			    	}
			    }
			    //polling for server to send START signal after 5 secs
			    Thread.sleep(5*1000);
			}
			System.out.println("Received ACK from server");
			
			//start communication with server
			//while(true)
			for(int loop=0;loop<maxIterations;loop++)
			{
				//clients probabilistic behavior starts from here	
				float probability = new Random().nextFloat();
				//send event
				if(probability <= sendProbability )
				{
					System.out.println("Send event");
					//System.out.println("Clients clock value:"+clientClockValue);
					//encrypt offset
					int encryptedValue= clientClockValue + KEY;
					//System.out.println("Encrypted Clients clock value:"+encryptedValue);
					//send encrypted offset value to server
					out.println(""+encryptedValue);
				}
				//receive event
				else if(probability <= receiveProbability)
				{
					System.out.println("Receive event");
					//System.out.println("========================");
					//send receive signal to the server and then read offset value 
					out.println("RECEIVE");
					//read offset value from server
				  	int offset =Integer.parseInt(in.readLine()) ;
				  	//System.out.println("Offset value received from server:"+offset);
				  	//decrypt offset value
				  	int decryptedValue = offset - KEY;
				  	//System.out.println("Decrypted offset value:"+decryptedValue);
				  	// increment offset value by 1, assuming communication delay
				  	decryptedValue+=1;  
				  	//System.out.println("Clients clock value before updating:"+clientClockValue);
				  	clientClockValue = clientClockValue + decryptedValue;
				  	System.out.println("Updated clients clock value based on offset:"+clientClockValue);
				}
				//internal event
				else if(probability<= internalEventProbability)
				{
					System.out.println("Internal Event");
					//wait for 5 secs	
					Thread.sleep(5*1000);
				}
				//Byzantine behavior
				else
				{
					// receiving servers value but ignoring it
					System.out.println("Byzantine Behavior");
					//send receive signal to the server 
					out.println("RECEIVE");
					// reading offset value  but ignoring servers response
				  	Integer.parseInt(in.readLine()) ; //response from server
				}
				System.out.println("Client Current Clock value:"+clientClockValue);
				/*//write clock value to txt 
				String content =""+ loop +","+clientClockValue;
				bufferedWriter.write(content);
				bufferedWriter.newLine();*/
				//increment logical clock on any event to have unique number for each event	
				clientClockValue = clientClockValue +1; 
				//just to slow process execution for tracing output 
				//Thread.sleep(5*1000); 
			}
		} 
		
		catch (UnknownHostException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			//Failure model :- Catching Server Crash 
			System.out.println("Server has Crashed Unexpectedly...");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				//closing I/O stream 
				if(in!=null)
					in.close();
				if(out!=null)
					out.close();
				//close client socket
				if(clientSocket!=null)
				{
					clientSocket.close();
				}
				/*if(bufferedWriter!=null)
					bufferedWriter.close();
				if(fileWriter!=null)
					fileWriter.close();*/
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
