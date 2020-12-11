# File/Folder sender/receiver

The two java files are for sending and receiving files. 
I have a raspberry pi and it is a hassle to send over files/folders
The way I have been doing it is over github. This programe gets rid 
of the middle man.

# How to setup

Objective is to send a file/folder from one location to another.

There are two java files FileSender.java and FileReceiver.java

move the FileReceiver.java to the computer/location that you would
like the files/folders to go. 

complie java
run: javac FileReceiver.java

Run server
run: java FileReceiver (port) 

move the FileSender.java to computer/location that you would like to get the files to send. 

compile java
run: javac FileSender.java

Send file
run: java FileSender.java (filePath) (host) (port)

There are other opptions to use FileSender

Examples:

Send normal file
java FileSender (filePath) (host) (port)

Send Folder
java FileSender -f (folderPath) (host) (port)

Make New Folder 
java FileSender -mkdir (dirName) (host) (port)