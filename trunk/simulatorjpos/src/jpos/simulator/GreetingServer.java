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
                readHeader();
                readContent();
                String s = "request: " + url;
                for (String key: params.keySet()) {
                    s = s + "\n" + key + ": " + params.get(key);
                }
                tracer.println(s);
                sendResponse();
                tracer.println("code: " + code + "\n" + "result: " + result);
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

        private void readHeader() throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String ln;
            int i;
            String request = reader.readLine();
            processRequest(request);
            while (true) {
                ln = reader.readLine();
                if (ln == null || ln.isEmpty()) {
                    break;
                }
                i = ln.indexOf(":");
                if (i >= 0) {
                    headers.put(ln.substring(0, i), ln.substring(i + 1).trim());
                }
            }
        }

        private void processRequest(String header) {
            if (header == null) {
                url = "";
                return;
            }
            int from = header.indexOf(" ") + 1;
            if (header.charAt(from) == '/') {
                from++;
            }
            int to = header.indexOf(" ", from);
            url = header.substring(from, to);
            int i = url.indexOf("?");
            if (i != -1) {
                processParams(url.substring(i + 1));
                url = url.substring(0, i);
            }
        }

        private void processParams(String content) {
            String[] sa = content.split("&");
            for (String s : sa) {
                int i = s.indexOf("=");
                if (i != -1) {
                    params.put(s.substring(0, i), s.substring(i + 1));
                } else {
                    params.put(s, "");
                }
            }
        }

        private void readContent() throws IOException {
            String clen = headers.get("Content-Length");
            if (clen != null) {
                int len = Integer.parseInt(clen);
                char[] buffer = new char[len];
                String ctype = headers.get("Content-Type");
                int i = ctype.indexOf(";");
                InputStreamReader reader;
                if (i >= 0) {
                    String charset = ctype.substring(i + 1).trim();
                    reader = new InputStreamReader(in, charset);
                } else {
                    reader = new InputStreamReader(in);
                }
                reader.read(buffer, 0, len);
                String content = new String(buffer);
                processParams(content);
            }
        }

        private void sendResponse() throws IOException {
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
            String response = createResponse(contentType);
            PrintStream answer = new PrintStream(out, true, "utf-8");
            answer.print(response);
        }

        /**
         * Возвращает http заголовок ответа.
         *
         * @param code код результата отправки.
         * @return http заголовок ответа.
         */
        private String createResponse(String contentType) {
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
            if (result != null) {
                buffer.append("Content-Length: ");
                buffer.append(result.length());
                buffer.append("\r\n");
            }
            buffer.append("Connection: close\r\n");
            buffer.append("\r\n");
            if (result != null) {
                buffer.append(result);
            }
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
        private String url = null;
        private HashMap<String, String> params = new HashMap<>();
        private HashMap<String, String> headers = new HashMap<>();
        private int code;
        private String result;
    }
    private ServerSocket serverSocket;
    private HashMap<String, ServerResource> resources = new HashMap<>();
    private HashMap<String, String> resourceContents = new HashMap<>();
    private static Tracer tracer = TracerFactory.getInstance().createTracer("GreetingServer");
}