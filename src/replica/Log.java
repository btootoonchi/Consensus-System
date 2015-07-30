package replica;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Log {
	private ArrayList<LogMessage> entries = new ArrayList<LogMessage>();
	public String logPath;
	
	/** Constructor
	 * @param logPath is the name of log file.  
	 */
	public Log(String logPath) {
        this.logPath = logPath;
        loadFromFile();
    }
	
	/** Get pointer to list of logMessage
	 * @return pointer to the list of logMessage  
	 */
	public ArrayList<LogMessage> getEntries() {
        return entries;
    }
    
	/** Load logs from the log file
	 */
	@SuppressWarnings("unchecked")
	public void loadFromFile() {
        ObjectInputStream inputStream = null;

        try {
            inputStream = new ObjectInputStream(new FileInputStream(logPath));
            entries = (ArrayList<LogMessage>) inputStream.readObject();
            for(int i=0;i < entries.size(); i++)
            	System.out.println(entries.get(i).toString());
        } catch (FileNotFoundException e) { 
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	
	/** Add a log message into a file
	 * @param entry is a log message
	 */
	public void addLog(LogMessage entry) {
		entries.add(entry);
		if (writeToFile()) {
			System.out.println("LOG:" + entry.toString());
		}
    }
	
	/** Write a log message into a file
	 * @return true if everything is done successfully otherwise returns false.
	 */
	public boolean writeToFile() {
        ObjectOutputStream outputStream = null;
        boolean success = true;

        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(logPath));
            outputStream.writeObject(entries);
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }
	
	/** Get the latest log message
	 * @return the last message.
	 */
	public LogMessage latest(){
		if (entries.size() > 0)
			return entries.get(entries.size()-1);
		return null;
	}
}
