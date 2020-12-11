import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sound.sampled.*;

class FileReceiver {

  public static void main(String[] args) throws Exception {
    // No args gives basic info
    if (args.length != 1) {
      System.out.println("ERROR: Incorect number of args");
      System.out.println("Example: java FileReceiver <port>");
      return;
    }
    

    // Set up Server
    int port = Integer.parseInt(args[0]);
    ServerSocket server = new ServerSocket(port);
    boolean isServerOn = true;


    // Print out working info
    System.out.println("Running File Receiver");
    System.out.println(InetAddress.getLocalHost() + " is listining on port "+ args[0]);
    

    // Running Server
    while (isServerOn) {
      // Accept Clients
			Socket socket = server.accept();
      //Run GetFileThread
      new Thread(new GetFileThread(socket)).start();
		}
  }


  static void unpackFile(InputStream input) throws IOException {
		int length = 0;
    int size = 0;
    // get length from server
		for (int i = 0; i < 4; i++) {
      size = input.read();
		}
		// Move bytes and use "AND" operator with size from server
		length = length << 8 | size;

    // Read in all bytes
    byte[] allBytes = input.readAllBytes();
    // Get header bytes and body bytes
		byte[] headerBytes = Arrays.copyOfRange(allBytes, 1, length);
    byte[] bodyBytes = Arrays.copyOfRange(allBytes, length, allBytes.length);

    boolean isDir = allBytes[0]!=0;


    // get file name
    String fileName = new String(headerBytes);

    // set up new file
    File newFile = new File(fileName);

    // if it is a folder
    if(isDir) {
      // Making folder
      boolean bool = newFile.mkdir();
      if(bool){
        System.out.println("Made dir Success: " + fileName);
      }
      else{
        System.out.println("Bad faild at making: " + fileName);
        return;
      }
    }
    else {
      // making file
      System.out.println("made file: " + fileName);
      Files.write(newFile.toPath(), bodyBytes);
    }
	}

  static class GetFileThread extends Thread {
		Socket socket;
    // get socket
		GetFileThread(Socket socket) {
			this.socket = socket;
		}

    @Override
		public void run() {
			try {
        // create output to sent to client
        InputStream input = socket.getInputStream();
        // upack file
        unpackFile(input);
        //close client
        socket.close();
			} catch (IOException e) {
				System.out.println(e);
			} 

		}
  }

}
