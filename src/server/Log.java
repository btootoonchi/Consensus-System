package server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import server.LogMessage;

public class Log
{
	public static final String START_2PC = "START_2PC",
			GLOBAL_ABORT = "GLOBAL_ABORT",
			GLOBAL_COMMIT = "GLOBAL_COMMIT",
			VOTE_ABORT = "VOTE_ABORT",
			VOTE_COMMIT = "VOTE_COMMIT",
			GLOBAL_UNDO = "GLOBAL_UNDO",
			COMMIT_FAIL = "COMMIT_FAIL",
			COMMIT_DONE = "COMMIT_DONE",
			DECISION = "DECISION";
			
	// Path to log file
    public ArrayList<LogMessage> entries = new ArrayList<LogMessage>();
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
            for(int i = 0;i < entries.size(); i++)
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
	
	/** Find the START_2PC message in the latest operation in the log message
	 * @return 
	 */
	public LogMessage latestOperation(){
		int i = 0,j = 0/*, k = 0*/;
		LogMessage entry = new LogMessage();
		if (entries.size() > 0) {
			entry = entries.get(entries.size() - ++i);
			while (entries != null && !entry.getMessage().equals(START_2PC) && (++i < entries.size())) {
				System.out.println("size of the log file is "+entries.size()+" index(i): "+ i);
				entry = entries.get(entries.size() - i);
			}
			
			entry = entries.get(entries.size() - ++j);
			while (entries != null && !entry.getMessage().equals(GLOBAL_COMMIT) && (++j < entries.size())) {
				System.out.println("size of the log file is "+entries.size()+" index(j): "+ j);
				entry = entries.get(entries.size() - j);
			}

			if (j < i)
				return null;
			
			/*entry = entries.get(entries.size() - ++k);
			while (entries != null && !entry.getMessage().equals(COMMIT_FAIL) && (++k < entries.size())) {
				System.out.println("size of the log file is "+entries.size()+" index(k): "+ k);
				entry = entries.get(entries.size() - k);
			}
			
			if (k < i)
				return null;*/
			
			return entries.get(entries.size() - i);
		}
		return null;
	}
}
