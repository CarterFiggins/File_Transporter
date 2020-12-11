import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.file.Files;
import javax.sound.sampled.*;

class FileSender {

  public static void main(String[] args) throws Exception {
    // No args gives basic info
    if (args.length == 0) {
      System.out.println("ERROR: Incorect args");
      System.out.println("Example: java FileSender <filePath> <host> <port>");
      System.out.println("Example: java FileSender -f <folderPath> <host> <port>");
      System.out.println("Example: java FileSender -mkdir <dirName> <host> <port>");
      return;
    }

    // Sending Folder
    if(args[0].equals("-f")){
      if (args.length != 4) {
        System.out.println("ERROR: Incorect args");
        System.out.println("Example: java FileSender -f <path> <host> <port>");
        return;
      }

      // get folder host and port to send
      File folder = new File(args[1]);
      String host = args[2];
      int port = Integer.parseInt(args[3]);
      sendFolder(folder, host, port, "");
      return;
    }

    // Make new folder
    if(args[0].equals("-mkdir")){
      if (args.length != 4) {
        System.out.println("ERROR: Incorect args");
        System.out.println("Example: java FileSender -mkdir <dirName> <host> <port>");
        return;
      }

      File file = new File(args[1]);
      String host = args[2];
      int port = Integer.parseInt(args[3]);

      System.out.println("Sending dir: " + file.getName());
      setupFile(file, file.getName(), true, host, port);
      return;
    }
    
    // Send Single File

    // Get file from user
    File file = new File(args[0]);
    String host = args[1];
    System.out.println("Sending file: " + file.getName());
    int port = Integer.parseInt(args[2]);

    setupFile(file, file.getName(), false, host, port);
  }

  // Sends everything in Folder
  static void sendFolder(final File folder, String host, int port, String dirPath) throws Exception {
    // Send folder name
    dirPath += folder.getName() + "/";
    setupFile(folder, dirPath, true, host, port);
    // Give server time to make folder
    Thread.sleep(10);
    System.out.println("Sending " + dirPath);
    for (final File file : folder.listFiles()) {
        if (file.isDirectory()) {
          // ender folder
          sendFolder(file, host, port, dirPath);
        } else {
          String filePath = dirPath + file.getName();
          System.out.println("Sending: " + filePath);
          // Send file in folder
          setupFile(file, filePath, false, host, port);
      } 
    }
  }

  // Set up the file or folder. makes haeder and body
  static void setupFile(File file, String name, boolean isDir, String host, int port) throws Exception {
    byte[] body = null;
    // each file/folder gets a socket
    Socket socket = new Socket(host, port);
    // Folders do not get a body 
    if(!isDir){
      body = Files.readAllBytes(file.toPath());
    }
    // header holds info about folder/file (name and is it a folder)
    byte[] header = makeHeader(name, isDir);

    //Run SendFile
    new Thread(new SendFile(socket, body, header, isDir)).start();
  }

  // Creates info in header
  static byte[] makeHeader(String name, boolean isDir) throws IOException {
 
    ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
		DataOutputStream dataOutput = new DataOutputStream(byteArrayOutput);

    // boolean for folder
    dataOutput.writeBoolean(isDir);
    // String for name
		dataOutput.writeBytes(name);

		dataOutput.close();

    // Convert to byte array
		byte[] infoBytes = byteArrayOutput.toByteArray();
		byteArrayOutput.close();

    return wrap(infoBytes);
  }

  // Get length of header and add it to byte array
  static byte[] wrap(byte[] byteArray) {
		long length = byteArray.length;
    // make new byte array to add length to header
    byte[] lengthByteArray = new byte[4];
		byte[] newByteArray = new byte[(int) length + 4];
		for (int i = 0; i < 4; i++)
			lengthByteArray[i] = (byte) (length >> (3 - i) * 8 & 255);

    // combind lengthByteArray and byteArray
    int pos =0;
    for(byte b : lengthByteArray){
      newByteArray[pos] = b;
      pos++;
    }
    for(byte b : byteArray){
      newByteArray[pos] = b;
      pos++;
    }

		return newByteArray;
	}

  // Sends file/folder in bytes to client
  static class SendFile extends Thread {
		Socket socket;
    byte[] body;
    byte[] header;
    boolean isDir;
    // get socket, body
		SendFile(Socket socket, byte[] body, byte[] header, boolean isDir) {
			this.socket = socket;
      this.body = body;
      this.header = header;
      this.isDir = isDir;
		}

    @Override
		public void run() {

			try {
        // create output to sent to server
        OutputStream output = socket.getOutputStream();
        output.write(this.header);
        output.flush();
        if(!isDir) {
          output.write(this.body);
          output.flush();
        }
        //close socket
        socket.close();
			} catch (IOException e) {
				System.out.println(e);
			} 

		}
  }

}
