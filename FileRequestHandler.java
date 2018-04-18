import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;


/**
 * Request handler for HTTP/1.1 GET requests.
 */
public class FileRequestHandler {

    private final Path documentRoot;
    private static final String NEW_LINE = System.lineSeparator();

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
        response.write("Method handle not implemented.".getBytes());
        response.write(NEW_LINE.getBytes());

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
}
