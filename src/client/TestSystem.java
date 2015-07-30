package client;

import java.net.SocketTimeoutException;

import common.Message;

public class TestSystem
{
	private Client client = null;
	
	/** Constructor
	 * @param client is a pointer to current client object  
	 */
	public TestSystem(Client client) {
		this.client = client;
	}
	
	/** 1- test get command to get a key from DBA.
	 */
    public void testGet() {
		System.out.println("INFO: test.testGet: Begin.");

		String input = "get test";
		Message message = new Message(input);
		
		if (message.isValid()) {
			System.out.println("sending "+message.toString());
		} else {
			System.out.println("invalid message: "+message.toString());
		}
			
		try {
			client.send(message);
			client.receive();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		}
		
		System.out.println("INFO: test.testGet: Finished.");
	}
	
    /** 2- test put command to put a new key in the DBA.
	 */
	public void testPut() {
		System.out.println("INFO: test.testPut: Begin.");

		String input = "put test consensus";
		Message message = new Message(input);
		
		if (message.isValid()) {
			System.out.println("sending "+message.toString());
		} else {
			System.out.println("invalid message: "+message.toString());
		}
			
		try {
			client.send(message);
			client.receive();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		}
		
		System.out.println("INFO: test.testPut: Finished.");
	}
	
	/** 3- test get command when the key does not exist in the DBA.
	 */
	public void testGetKeyNotInDBA() {
		System.out.println("INFO: test.testGetKeyNotInDBA: Begin.");
		
		String input = "get test10";
		Message message = new Message(input);
		
		if (message.isValid()) {
			System.out.println("sending "+message.toString());
		} else {
			System.out.println("invalid message: "+message.toString());
		}
			
		try {
			client.send(message);
			client.receive();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		}
		
		System.out.println("INFO: test.testGetKeyNotInDBA: Finished.");
	}
	
	/** 4- test put command to update the existing key in the DBA.
	 */
	public void testPutUpdateKey() {
		System.out.println("INFO: test.testPutUpdateKey: Begin.");
		
		String input = "get test newconsensus";
		Message message = new Message(input);
		
		if (message.isValid()) {
			System.out.println("sending "+message.toString());
		} else {
			System.out.println("invalid message: "+message.toString());
		}
			
		try {
			client.send(message);
			client.receive();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		}
		
		System.out.println("INFO: test.testPutUpdateKey: Finished.");
	}
	
	/** 5- test del command.
	 */
	public void testDelete() {
		System.out.println("INFO: test.testDelete: Begin.");
		
		String input = "del test";
		Message message = new Message(input);
		
		if (message.isValid()) {
			System.out.println("sending "+message.toString());
		} else {
			System.out.println("invalid message: "+message.toString());
		}
			
		try {
			client.send(message);
			client.receive();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		}
		
		System.out.println("INFO: test.testDelete: Finished.");
	}
	
	/** 6- test del command when there is no the key that wants to delete.
	 */
	public void testDeleteNotKey() {
		System.out.println("INFO: test.testDeleteNotKey: Begin.");
		
		String input = "del hello";
		Message message = new Message(input);
		
		if (message.isValid()) {
			System.out.println("sending "+message.toString());
		} else {
			System.out.println("invalid message: "+message.toString());
		}
			
		try {
			client.send(message);
			client.receive();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		}
		
		System.out.println("INFO: test.testDeleteNotKey: Finished.");
	}
	
	/** 7- test put, get, and del commands sequentially
	 */
	public void testPutGetDelete() {
		System.out.println("INFO: test.testPutGetDelete: Begin.");
		
		// Put new key and value on DBA
		String input = "put hello helloworld";
		Message message = new Message(input);
		
		if (message.isValid()) {
			System.out.println("sending "+message.toString());
		} else {
			System.out.println("invalid message: "+message.toString());
		}
			
		try {
			client.send(message);
			client.receive();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		}
		
		// Get the key.
		input = "get hello";
		message = new Message(input);
		
		if (message.isValid()) {
			System.out.println("sending "+message.toString());
		} else {
			System.out.println("invalid message: "+message.toString());
		}
			
		try {
			client.send(message);
			client.receive();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		}
		try
		{
			Thread.sleep(100);
		} catch (InterruptedException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Delete the key.
		input = "del hello";
		message = new Message(input);
		
		if (message.isValid()) {
			System.out.println("sending "+message.toString());
		} else {
			System.out.println("invalid message: "+message.toString());
		}
			
		try {
			client.send(message);
			client.receive();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		}
		
		// Get the key to make sure it is deleted.
		input = "get hello";
		message = new Message(input);
		
		if (message.isValid()) {
			System.out.println("sending "+message.toString());
		} else {
			System.out.println("invalid message: "+message.toString());
		}
			
		try {
			client.send(message);
			client.receive();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		}
		
		System.out.println("INFO: test.testPutGetDelete: Finished.");
	}
	
	/** Run test cases
	 * @param input is a user entry for selecting a test case
	 */
	public void testList(String input) {		
		String regex = "[0-9]+";
		if (input.equals("test") || !input.matches(regex))
			input = "100";
		
		switch (Integer.parseInt(input)) {
		case 0:
			testGet();
			testPut();
			testGetKeyNotInDBA();
			testPutUpdateKey();
			testDelete();
			testDeleteNotKey();
			testPutGetDelete();
			break;
		case 1:
			testGet();
		break;
		case 2:
			testPut();
		break;
		case 3:
			testGetKeyNotInDBA();
		break;
		case 4:
			testPutUpdateKey();
		break;
		case 5:
			testDelete();
		break;
		case 6:
			testDeleteNotKey();
		break;
		case 7:
			testPutGetDelete();
		break;
		default:
			System.out.println("****************************************");
			System.out.println("* Welcome to test the consensus system *");
			System.out.println("* Please enter a number between 1 - 7  *");
			System.out.println("************* List of test *************");
			System.out.println(" 0 - test All");
			System.out.println(" 1 - test get a key");
			System.out.println(" 2 - test put a key");
			System.out.println(" 3 - test Get a key not available");
			System.out.println(" 4 - test update a key");
			System.out.println(" 5 - test delete a key");
			System.out.println(" 6 - test delete a key not available");
			System.out.println(" 7 - test put, get, and delete sequentially");
			System.out.println(" 8 - ");
			System.out.println("****************************************");
			
			System.out.println("Please enter a number between 1 - 7 or  ");
			System.out.println( "write a command(put <key> <value>, get <key>, del <key>)");
		break;
		}
	}
}
