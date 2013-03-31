package jpos.simulator;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import jpos.util.tracing.Tracer;
import jpos.util.tracing.TracerFactory;

public class GreetingServer extends Thread {

    public GreetingServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        //serverSocket.setSoTimeout(10000);
    }

    public void registerResource(String url, String contentType, ServerResource resource) {
        registerResource(url, resource);
        resourceContents.put(url, contentType);
    }

    public void registerResource(String url, ServerResource resource) {
        resources.put(url, resource);
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientSession session = new ClientSession(clientSocket);
                (new Thread(session)).start();
            }
        } catch (IOException e) {
            tracer.println(e);
        }
    }

    public void close() throws IOException {
        serverSocket.close();
    }

    public interface ServerResource {

        public String getResource(HashMap<String, String> params);
    }

    private class ClientSession implements Runnable {

        @Override
        public void run() {
            try {
                tracer.println("Requert received\n");
                String header = readHeader();
                tracer.println(header + "\n");
                String url = getURIFromHeader(header);
                HashMap<String, String> params = getParamsFromHeader(header);
                tracer.println("Resource: " + url + "\n");
                int code = send(url, params);
                tracer.println("Result code: " + code + "\n");
            } catch (IOException e) {
                tracer.print(e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    tracer.print(e);
                }
            }
        }

        public ClientSession(Socket socket) throws IOException {
            this.socket = socket;
            in = socket.getInputStream();
            out = socket.getOutputStream();
        }

        private String readHeader() throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder builder = new StringBuilder();
            String ln;
            while (true) {
                ln = reader.readLine();
                if (ln == null || ln.isEmpty()) {
                    break;
                }
                builder.append(ln);
                builder.append(System.getProperty("line.separator"));
            }
            return builder.toString();
        }

        private String getURIFromHeader(String header) {
            int from = header.indexOf(" ") + 1;
            if (header.charAt(from) == '/') {
                from++;
            }
            int to = header.indexOf(" ", from);
            String uri = header.substring(from, to);
            int paramIndex = uri.indexOf("?");
            if (paramIndex != -1) {
                uri = uri.substring(0, paramIndex);
            }
            return uri;
        }

        private HashMap<String, String> getParamsFromHeader(String header) {
            int from = header.indexOf(" ") + 1;
            int to = header.indexOf(" ", from);
            String uri = header.substring(from, to);
            int paramIndex = uri.indexOf("?");
            String[] params = null;
            if (paramIndex != -1) {
                params = uri.substring(paramIndex + 1).split("&");
            }
            HashMap<String, String> result = new HashMap<>();
            if (params != null) {
                for (String param : params) {
                    int i = param.indexOf("=");
                    if (i != -1) {
                        result.put(param.substring(0, i), param.substring(i + 1));
                    } else {
                        result.put(param, "");
                    }
                }
            }
            return result;
        }

        private int send(String url, HashMap<String, String> params) throws IOException {
            int code;
            String result;
            ServerResource resource = resources.get(url);
            String contentType = resourceContents.get(url);

            if (contentType == null) {
                contentType = "text/plain";
            }
            if (resource != null) {
                code = 200;
                try {
                    result = resource.getResource(params);
                } catch (Exception e) {
                    code = 500;
                    contentType = "text/plain";
                    result = e.getMessage();
                }
            } else {
                code = 404;
                result = null;
            }
            String header = getHeader(code, contentType, result);
            PrintStream answer = new PrintStream(out, true, "utf-8");
            answer.print(header);
            if (result != null) {
                answer.print(result);
            }
            return code;
        }

        /**
         * Возвращает http заголовок ответа.
         *
         * @param code код результата отправки.
         * @return http заголовок ответа.
         */
        private String getHeader(int code, String contentType, String content) {
            StringBuilder buffer = new StringBuilder();
            buffer.append("HTTP/1.1 ");
            buffer.append(code);
            buffer.append(" ");
            buffer.append(getAnswer(code));
            buffer.append("\r\n");
            buffer.append("Date: ");
            buffer.append((new Date()).toGMTString());
            buffer.append("\r\n");
            buffer.append("Content-Type: ");
            buffer.append(contentType);
            buffer.append("; charset=utf-8");
            buffer.append("\r\n");
            if (content != null) {
                buffer.append("Content-Length: ");
                buffer.append(content.length());
                buffer.append("\r\n");
            }
            buffer.append("Connection: close\r\n");
            buffer.append("\r\n");
            return buffer.toString();
        }

        private String getAnswer(int code) {
            switch (code) {
                case 200:
                    return "OK";
                case 404:
                    return "Not Found";
                default:
                    return "Internal Server Error";
            }
        }
        private Socket socket;
        private InputStream in = null;
        private OutputStream out = null;
    }
    private ServerSocket serverSocket;
    private HashMap<String, ServerResource> resources = new HashMap<>();
    private HashMap<String, String> resourceContents = new HashMap<>();
    private static Tracer tracer = TracerFactory.getInstance().createTracer("GreetingServer");
}