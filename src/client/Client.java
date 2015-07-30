package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

import common.Message;


public class Client {
	public static String SERVER_IP = "localhost";
	public static int SERVER_PORT = 5001;
	public static int TIMEOUT = 5000;		// 5 seconds
	
	private Socket socket;
	
	/** Constructor  
	 */
	public Client() {
	}
	
	/** Send a message to a server
	 * @param message is string of a request.
	 */
	public void send(Message message){
		try {
			socket = new Socket(SERVER_IP, SERVER_PORT);
			socket.setSoTimeout(TIMEOUT);
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(message);
			System.out.println("Sent " + message.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Receive a message from a server
	 * @return a response message from server 
	 */
	public String receive() throws SocketTimeoutException{
		ObjectInputStream input;
		String message = null;
		try {
			input = new ObjectInputStream(socket.getInputStream());
			message = (String) input.readObject();
		}catch(SocketTimeoutException e){
			throw e;
		}catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("Received " + message);
		return message;
	}
	
	/** Get user entries and check it out 
	 * Users can select a test case or type their commands  
	 */
	public void run() {
		String input = null;
		TestSystem test = new TestSystem(this);

		Scanner sc = new Scanner(System.in);
		
		while (true) {
			input = sc.nextLine();
			Message message = new Message(input);
			if (input.equals("exit")) 
				break;
			else if (!message.validCommands.contains(input.split(" ")[0]))
				test.testList(input);
			else if (message.isValid()) {
				System.out.println("sending "+message.toString());
				try {
					send(message);
					receive();
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("invalid message: "+message.toString());
			}
		}
		sc.close();
	}
	
	/** Main function of Client
	 * @param args
	 */
	public static void main(String[] args) {
		Client c = new Client();
		c.run();
	}
}
