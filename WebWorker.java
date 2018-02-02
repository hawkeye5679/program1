/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection. 
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring 
* the fact that the entirety of the webserver execution might be handling
* other clients, too. 
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format). 
*
**/

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;

public class WebWorker implements Runnable
{
private Socket socket;

/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;
}

/**
* Worker thread starting point. Each worker handles just one HTTP 
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{
   System.err.println("Handling connection...");
   try {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      //Get the file path
      String path = readHTTPRequest(is);
      //Copy the file to a string
      String file = copyFile( path );
      writeHTTPHeader(os,"text/html", path);
      //Write the content to the ouput stream
      writeContent(os, file);
      //Put the output on the screen
      os.flush();
      socket.close();
   } catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   System.err.println("Done handling connection.");
   return;
}

/**
* Read the HTTP request header.
**/
private String readHTTPRequest(InputStream is)
{
   String line;
   String [  ] excess;
   String y = "";
   try {
      //Get the file path in the buffer
      BufferedReader r = new BufferedReader(new InputStreamReader(is));
      while (!r.ready()) Thread.sleep(1);
      line = r.readLine();
      //Split the file path from the rest of the buffer
      excess = line.split( " " );
      //save the file path
      y = excess[ 1 ];
      System.err.println("Request line: ("+line+")");
   } catch (Exception e) {
      System.err.println("Request error: "+e);
      return "404";
      
   }
   //return the file path
   return y;
}

/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType, String path ) throws Exception
{
   //Test if the file is made
   int flag = 0;
   try{
      File a = new File( path );
   }
   catch( Exception e ){
      flag = 1;
   }
   Date d = new Date();
   DateFormat df = DateFormat.getDateTimeInstance();
   df.setTimeZone(TimeZone.getTimeZone("GMT"));
   //Set the header based on if the file was made
   if( flag == 0 )
      os.write("HTTP/1.1 200 OK\n".getBytes());
   else
      os.write("HTTP/1.1 404 Error\n".getBytes( ) );
   os.write("Date: ".getBytes());
   os.write((df.format(d)).getBytes());
   os.write("\n".getBytes());
   os.write("Server: Jon's very own server\n".getBytes());
   //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
   //os.write("Content-Length: 438\n".getBytes()); 
   os.write("Connection: close\n".getBytes());
   os.write("Content-Type: ".getBytes());
   os.write(contentType.getBytes());
   os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
   return;
}

/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContent(OutputStream os, String file) throws Exception
{
   //Write the file or error message
   if( file.equals( "404" ) ){
      os.write( file.getBytes( ) );
      os.write( ": File not found".getBytes( ) );
   }//end if
   else {
      os.write("The contents are: ".getBytes( ) );
      os.write( file.getBytes( ) );
   }//end else
}

private String copyFile( String s ) throws Exception {
   
   String file = " ";
   String y;
   String t;
   
   try{
      //Turn the path to a file and put it in a buffered reader
      File temp = new File( s.substring( 1 ) );
      BufferedReader x = new BufferedReader( new FileReader( temp ) );
      while( x.ready( ) ){
         y = x.readLine( );
         //Read the file and save it to a string
         if( y.contains( "<cs371date>" ) ){
            Date d = new Date();
            DateFormat df = DateFormat.getDateTimeInstance();
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            t = df.format( d );
            file = file + t + "\n";
         }
         else if( y.contains( "cs371server>" ) ){
            t = "Luke's Server";
            file = file + t + "\n";
         }
         else
            file = file + y + "\n";
         
      }//end while
   }//end try
   catch( Exception e ) {
      return "404";
   }//end catch
   return file;
}//end writeFile

} // end class
