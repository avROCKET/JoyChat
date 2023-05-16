@echo off
echo Compiling Java files...
javac Server.java
javac ClientGUI.java

echo Creating Server JAR...
echo Main-Class: Server > server_manifest.txt
jar cvfm Server.jar server_manifest.txt *.class

echo Creating Client JAR...
echo Main-Class: ClientGUI > client_manifest.txt
jar cvfm ClientGUI.jar client_manifest.txt *.class *.wav

echo Cleaning up manifest files...
del server_manifest.txt
del client_manifest.txt

echo Cleaning up class files...
del *.class

echo Process Complete.
