import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Request handler for HTTP/1.1 GET requests.
 */
public class FileRequestHandler {

    private final Path documentRoot;
    private static final String NEW_LINE = System.lineSeparator();
    private String[] args;
    private String buffer;

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
    	//task a
    	args = request.split(" ");
    	Path path = Paths.get(args[1]);
    	Path http_root = Paths.get("/www-root"+args[1]);
    	response.write(Arrays.toString(args).getBytes());
    	response.write(Paths.get("http_root").normalize().toString().getBytes());
    	response.write(Boolean.toString(Files.exists(http_root)).getBytes());
    	if(!(args.length == 3)) {response.write(status_message(400).getBytes()); return;}
    	if(!Files.exists(path) || !Files.exists(http_root)) {response.write(status_message(404).getBytes()); return;}
    	if(!(args[0].equals("GET"))) {response.write(status_message(501).getBytes()); return;}
    	if(!(args[2].equals("HTTP/1.1"))) {response.write(status_message(505).getBytes()); return;}
    	
    	//task b
    	if(Files.exists(http_root)) {path = http_root;};
    	response.write(status_message(200).getBytes());
    	
    	buffer = "DATE: "+getHttpDate()+"\n";
    	response.write(buffer.getBytes());
    	
    	buffer = "Content-Type: "+Files.probeContentType(path);
    	response.write(buffer.getBytes());
    	
    	buffer = "Content-Length: "+Long.toString(Files.size(path));
    	response.write(buffer.getBytes());
    	
    	buffer = "Last-Modified: "+Files.getLastModifiedTime(path).toString();
    	response.write(buffer.getBytes());
    	
    	buffer = Arrays.toString(Files.readAllBytes(path));
    	response.write(buffer.getBytes());
        /*
         * (a) Determine status code of the request and write proper status
         * line to the response output stream.
         *
         * Only continue if the request can be processed (status code 200).
         * In case the path points to a file (b) or a directory (c) write the
         * appropriate header fields and …
         *
         * (b) … the content of the file …
         * (c) … a listing of the directory contents …
         *
         * … to the response output stream.
         */
    }
    //Map status message to status code
    public String status_message(int code) {
    	switch(code) {
    	case 200: return "\nHTTP/1.1 200 OK\n";
    	case 400: return "\nHTTP/1.1 400 Bad Request\n";
    	case 404: return "\nHTTP/1.1 404 Not found\n";
    	case 501: return "\nHTTP/1.1 501 Not implemented\n";
    	case 505: return "\nHTTP/1.1 505 HTTP Version not supported\n";
    	default: return "\nUnkown Error\n";
    	}
    }
    public String getHttpDate() {
    // Proper date format as per RFC 2616 (HTTP/1.1):
    // https://tools.ietf.org/html/rfc2616#section-3.3.1
    	String httpDateFormat = "EEE, dd MMM yyyy HH:mm:ss zzz";
    	DateFormat dateFormat = new SimpleDateFormat(httpDateFormat);
    	return dateFormat.format(new Date());
    }
}
