import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Request handler for HTTP/1.1 GET requests.
 * This version is intended for a Netbeans 8.0.2 project structure.
 * Critical lines for this are the definition of path and www_root.
 * remove the /src/ to use if the build is inside the directory as www_root
 */
public class FileRequestHandler {

    private final Path documentRoot;
    private static final String NEW_LINE = System.lineSeparator();
    private String[] args;
    private String buffer;
    private List<String> bufferList;

    public FileRequestHandler(Path documentRoot) {
        this.documentRoot = documentRoot;
    }

    /**
     * Called to handle an HTTP/1.1 GET request: first, the status code of the
     * request is determined and a corresponding response header is sent.
     * If the status code is <200>, the requested document root path is sent
     * back to the client. In case the path points to a file, the file is sent,
     * and in case the path points to a directory, a listing of the contained
     * files is sent.
     *
     * @param request Client request
     * @param response Server response
     */
    public void handle(String request, OutputStream response)
    throws IOException {
    	//Setting an absolute path because windows does not like to run with relative paths
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        
        //Splitting the requests helps to parse it
        //The paths are set here already to use them as 404 evaluation
        //They are relative to the directory were the server was build
    	args = request.split(" ");
    	Path path = Paths.get(s+"/src/"+args[1]);  
    	Path www_root = Paths.get(s+"/src/www-root/"+args[1]);
        
        //this is not very fancy, but it works
        //maybe this all could be wrapped into a loop at a later point to reduce the typing affort for the use
    	if(!(args.length == 3)) {response.write(status_message(400).getBytes()); return;}
    	if(!Files.exists(path) && !Files.exists(www_root)) {response.write(status_message(404).getBytes()); return;}
    	if(!(args[0].equals("GET"))) {response.write(status_message(501).getBytes()); return;}
    	if(!(args[2].equals("HTTP/1.1"))) {response.write(status_message(505).getBytes()); return;}
    	response.write(status_message(200).getBytes());
        
        
        //I assume that you would prefer the files in the root directory
        if(Files.exists(www_root)) {path = www_root;}
        Path target = new File(path.toUri()).toPath();//File does not accept path, so path->URI->path :shrug:        
        
        
        if(Files.isRegularFile(target)){

            response.write(NEW_LINE.getBytes());
            //setting the headers with their own response it not best-practise, maybe work on that later
            buffer = "DATE: " + getHttpDate();
            response.write(buffer.getBytes());
            response.write(NEW_LINE.getBytes());

            buffer = "Content-Type: " + Files.probeContentType(target);
            response.write(buffer.getBytes());
            response.write(NEW_LINE.getBytes());

            buffer = "Content-Length: " + Long.toString(Files.size(target));
            response.write(buffer.getBytes());
            response.write(NEW_LINE.getBytes());

            buffer = "Last-Modified: " + toHttpDate(Files.getLastModifiedTime(target).toMillis());
            response.write(buffer.getBytes());
            response.write(NEW_LINE.getBytes());
            response.write(NEW_LINE.getBytes());
            //This will prob. break if none text files are read
            bufferList = Files.readAllLines(target);
            for (String bufferList_local : bufferList) {
                buffer = bufferList_local;
                response.write(buffer.getBytes());
                response.write(NEW_LINE.getBytes());
            }
    	
        }
        if(Files.isDirectory(target)){
            buffer = "DATE: " + getHttpDate();
            response.write(buffer.getBytes());
            response.write(NEW_LINE.getBytes());
            
            buffer = "Last-Modified: " + toHttpDate(Files.getLastModifiedTime(target).toMillis());
            response.write(buffer.getBytes());
            response.write(NEW_LINE.getBytes());
            response.write(NEW_LINE.getBytes());
            
            //I guess it would be worth a try to use an Arraylist instead
            buffer = "Content: Name + [Last modified (if it is a file)]";
            response.write(buffer.getBytes());
            response.write(NEW_LINE.getBytes());
            File directory = new File(target.toUri());
            File[] fList = directory.listFiles();
            for (File file : fList) {
                if (file.isFile()) { 
                    buffer = file.getName() + "   " + toHttpDate(Files.getLastModifiedTime(file.toPath()).toMillis());
                } else if (file.isDirectory()) {
                    buffer = file.getName();
                        }
                response.write(buffer.getBytes());
                response.write(NEW_LINE.getBytes());
            }
        }
    }
    
    //Map status message to status code
    public String status_message(int code) {
    	switch(code) {
    	case 200: return "HTTP/1.1 200 OK";
    	case 400: return "HTTP/1.1 400 Bad Request";
    	case 404: return "HTTP/1.1 404 Not found";
    	case 501: return "HTTP/1.1 501 Not implemented";
    	case 505: return "HTTP/1.1 505 HTTP Version not supported";
    	default: return "Unkown Error";
    	}
    }
    
    public String getHttpDate() {
    // Proper date format as per RFC 2616 (HTTP/1.1):
    // https://tools.ietf.org/html/rfc2616#section-3.3.1
    	String httpDateFormat = "EEE, dd MMM yyyy HH:mm:ss zzz";
    	DateFormat dateFormat = new SimpleDateFormat(httpDateFormat);
    	return dateFormat.format(new Date());
    }
    
    public String toHttpDate(long Millis) {
        /**
         * I need something to convert my FileTime from Files.getLastModifiedTime(path).
         * This will convert take the long created by the FileTime.toMillis to give me my time in the HttpDateFormat.
         */
        String httpDateFormat = "EEE, dd MMM yyyy HH:mm:ss zzz";
    	DateFormat dateFormat = new SimpleDateFormat(httpDateFormat);
    	return dateFormat.format(Millis);
    }
}
