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
import java.util.*;

/*
 * Server is responsible for listening to the client 
 * and perform synchronization of clock between them 
 */

public class Server {

	//program execution starts from here
	public static void main(String[] args) {
		
		//variables declaration
		ArrayList<Integer> clientClockValueList = new ArrayList<Integer>();
		int serverClockValue=0;
		ArrayList<Socket> clientSocketList = new ArrayList<Socket>();
		ArrayList<BufferedReader> buffereReaderList = new ArrayList<BufferedReader>();
		ArrayList<PrintWriter> printWriterList= new ArrayList<PrintWriter>();
		int maxIteration=10000;
		ServerSocket serverSocket=null;
		//KEY to encrypt and decrypt an offset value
	    final int KEY=20;
	    
	    //check required number of parameters 
		if(args.length <=1)
		{
			System.out.println("Usage is: > java Server [Number of users] [port number]");
			return;
		}
		try 
		{
			//parse client count
			int maxNumberOfClients= Integer.parseInt(args[0]); 
			//parse port number
			int portNumber = Integer.parseInt(args[1]);  
			//Create server socket
			serverSocket = new ServerSocket(portNumber,maxNumberOfClients);
			
			//Initialization section 
			for(int i=0;i<maxNumberOfClients;i++)
			{
				
				System.out.println("Waiting for Client["+i+"] to connect on port:"+portNumber);
				clientClockValueList.add(0);
				//waiting for client to connect 
				Socket clientSocket = serverSocket.accept();
				if(clientSocket == null)
				{
					System.out.println("Failed to create client socket at server side on port"+portNumber);
					return;
				}
				clientSocketList.add(clientSocket);
				System.out.println("Client connected successfully on port:"+portNumber);
				System.out.println("Client Info:"+clientSocket.toString());
				//Create I/O stream for a client 
			    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			    if (in ==null || out == null)
			    	System.out.println("Failed to create I/O stream");
			    System.out.println("I/O stream connected successfully");
			    buffereReaderList.add(in);
			    printWriterList.add(out);
			}
			
			//Notify clients to get started as all the required clients are connected to the system
			for(int i =0;i<maxNumberOfClients;i++)
			{
				//send start signal to the clients
				printWriterList.get(i).println("START");
			}
			
			System.out.println("All the clients are connected.");
		
			//server will start listening from all the clients 
			for(int loopCount=0;loopCount<maxIteration;loopCount++)
			{
				boolean receiveEvent = false;
				ArrayList<String>  userInputList = new ArrayList<String>();
				ArrayList<Boolean> receiveEventList =new ArrayList<Boolean>();

				for(int i=0;i<maxNumberOfClients;i++)
				{
					//initializing values to default
					String userInput=null;
					userInputList.add( null);
					receiveEventList.add(false);
					try
					{
						//read clients message
						userInput = buffereReaderList.get(i).readLine();
					}
					catch(IOException e)
					{
						System.out.println("Client["+i+"] has crashed" );
						continue;
					}
					//userInputList.add(userInput);
					if(userInput!=null)
					{
						userInputList.set(i,userInput);
						//check if user is requesting for clock value
						if(userInput.toUpperCase().contains("RECEIVE"))
						{
							//set the required flags for later use
							receiveEvent =true;
							receiveEventList.set(i,true);
						}
						//update clients clock value
						else 
						{
							//decrypt clients received clock value
							int decryptedValue = Integer.parseInt(userInput)-KEY;
							clientClockValueList.set(i, decryptedValue);
							//System.out.println("Clock value received from client["+i+"]:" +userInput);
							//System.out.println("Decrypted Clock value for client["+i+"]:" +decryptedValue);
							//receiveEventList.add(false);
						}
					}

				}
				//calculate average and send it to the requested clients
                if(receiveEvent)
                {
                	//System.out.println("=========================");
                	//System.out.println("Calculating average");
                	//Calculating average
    				//System.out.println("Servers Clock Value Before updating it:"+serverClockValue);
    				int sum=0;
    				//sum up all received clock values
    				for(int i=0;i<maxNumberOfClients;i++)
    				{
        				//System.out.println("Client["+i+"] clock value:"+clientClockValueList.get(i));				
        				sum +=clientClockValueList.get(i);
    				}
    				//calculate average
    				serverClockValue =(int) ((sum + serverClockValue)/(maxNumberOfClients+1)); //ignoring fraction
    				System.out.println("Updated Servers clock value:"+serverClockValue);

                }
             
                //Calculate offset and send updated offset value to all the requested clients 
                for(int i =0;i < maxNumberOfClients;i++)
                {
                	if(receiveEventList.get(i))
                	{
                		int offset =serverClockValue -	clientClockValueList.get(i);
                		//System.out.println("Offset value to the client["+i+"]:"+offset);
                		//encrypt offset value and send it to client
                		int encryptedValue = offset + KEY;
                   		//System.out.println("Encrypted Offset value to the client["+i+"]:"+encryptedValue);
                   		//send encrypted offset to client
                   		printWriterList.get(i).println(""+encryptedValue);
                		
                	}
                }
				System.out.println("Servers clock value:"+serverClockValue);
				//just to slow process execution for tracing output 
				//Thread.sleep(3*1000);
				//increment logical clock on any event to have unique number for each event
				serverClockValue = serverClockValue+1;
			}
			
			//closing all the client sockets and I/O streams associated with it
			for(int i =0;i<maxNumberOfClients;i++)
			{
				//closing output stream
				if (printWriterList.get(i)!=null)
				{
					printWriterList.get(i).close();
				}
				//closing input stream
				if (buffereReaderList.get(i)!=null)
				{
					buffereReaderList.get(i).close();
				}
				//closing client socket 
				if (clientSocketList.get(i)!=null)
				{
					clientSocketList.get(i).close();
				}
			}

		}
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		/*catch (InterruptedException e) 
		{
			
			e.printStackTrace();
		}*/
		finally
		{
			//Close server socket connection
			try
			{
				if(serverSocket!=null)
				{
					serverSocket.close();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
		}
	}

}
