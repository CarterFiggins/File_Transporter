import java.io.*;
import java.util.*;
import java.net.*;
import javax.sound.sampled.*;

class AudioServer {

  static Hashtable<AudioFormat.Encoding, Integer> encoding = new Hashtable<>(); 

  public static void main(String[] args) throws Exception {
    // No args gives basic info
    if (args.length <= 1) {
      System.out.println("ERROR: need more args");
      System.out.println("Example: java AudioServer <file> <port>");
      return;
    }
    
    // Get sound file from user
    File soundFile = new File(args[0]);
    AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile);
    AudioFormat audioFormat = audioInput.getFormat();

    // Make hashtable for Encoding
    encoding.put(AudioFormat.Encoding.ALAW, 1);
    encoding.put(AudioFormat.Encoding.PCM_FLOAT, 2);
    encoding.put(AudioFormat.Encoding.PCM_SIGNED, 3);
    encoding.put(AudioFormat.Encoding.PCM_UNSIGNED, 4);
    encoding.put(AudioFormat.Encoding.ULAW, 5);


    // Set up Server
    int port = Integer.parseInt(args[1]);
    ServerSocket server = new ServerSocket(port);
    boolean isServerOn = true;

    // Create Header and Body to send to client
    byte[] header = getBodyBytes(audioFormat);
    byte[] body = audioInput.readAllBytes();

    // Get length and rate to print out time of Audio File
    long length = audioInput.getFrameLength();
		float rate = audioFormat.getFrameRate();

    // Print out working info
    System.out.println("Running Audio Server");
    System.out.println(InetAddress.getLocalHost() + " is listining on port "+ args[1]);
    System.out.println("File Name: " + soundFile.getName() + " Time in Seconds: " + length / rate);
    

    // Running Server
    while (isServerOn) {
      // Accept Clients
			Socket socket = server.accept();
      InetAddress address = socket.getInetAddress();
      // Who is connected
      System.out.println("Connected to: " + address.getHostAddress());

      //Run AudioThread
      new Thread(new AudioThread(socket, header, body)).start();
		}
  }

  static byte[] getBodyBytes(AudioFormat audioFormat) throws IOException {
    // Create data output stream to get info from audio format
    ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
		DataOutputStream dataOutput = new DataOutputStream(byteArrayOutput);

    // Get info from audioFormat and create byte array
		dataOutput.writeInt(audioFormat.getChannels());
		dataOutput.writeInt(audioFormat.getFrameSize());
		dataOutput.writeFloat(audioFormat.getFrameRate());
		dataOutput.writeFloat(audioFormat.getSampleRate());
		dataOutput.writeBoolean(audioFormat.isBigEndian());
		dataOutput.writeInt(audioFormat.getSampleSizeInBits());
		dataOutput.writeInt(encoding.get(audioFormat.getEncoding()));
		dataOutput.close();

    // Convert to byte array
		byte[] infoBytes = byteArrayOutput.toByteArray();
		byteArrayOutput.close();

    return wrap(infoBytes);
  }


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

  // Sends audio in bytes to client
  static class AudioThread extends Thread {
		Socket socket;
    byte[] header;
    byte[] body;
    // get socket, header, body
		AudioThread(Socket socket, byte[] header, byte[] body) {
			this.socket = socket;
      this.header = header;
      this.body = body;
		}

    @Override
		public void run() {
			try {
        // create output to sent to client
        OutputStream output = socket.getOutputStream();
        output.write(this.header);
        output.flush();
        output.write(this.body);
        output.flush();
        //close client
        socket.close();
			} catch (IOException e) {
				System.out.println(e);
			} 

		}
  }

}
