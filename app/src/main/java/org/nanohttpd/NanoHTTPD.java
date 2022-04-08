package org.nanohttpd;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public abstract class NanoHTTPD {
    private static final String CONTENT_DISPOSITION_REGEX = "([ |\t]*Content-Disposition[ |\t]*:)(.*)";
    private static final Pattern CONTENT_DISPOSITION_PATTERN;
    private static final String CONTENT_TYPE_REGEX = "([ |\t]*content-type[ |\t]*:)(.*)";
    private static final Pattern CONTENT_TYPE_PATTERN;
    private static final String CONTENT_DISPOSITION_ATTRIBUTE_REGEX = "[ |\t]*([a-zA-Z]*)[ |\t]*=[ |\t]*['|\"]([^\"^']*)['|\"]";
    private static final Pattern CONTENT_DISPOSITION_ATTRIBUTE_PATTERN;
    public static final int SOCKET_READ_TIMEOUT = 5000;
    public static final String MIME_PLAINTEXT = "text/plain";
    public static final String MIME_JSON = "application/json;charset=UTF-8";
    public static final String MIME_HTML = "text/html";
    private static final String QUERY_STRING_PARAMETER = "NanoHttpd.QUERY_STRING";
    private static final Logger LOG;
    protected static Map<String, String> MIME_TYPES;
    private final String hostname;
    private final int myPort;
    private volatile ServerSocket myServerSocket;
    private ServerSocketFactory serverSocketFactory;
    private Thread myThread;
    protected AsyncRunner asyncRunner;
    private TempFileManagerFactory tempFileManagerFactory;

    public static Map<String, String> mimeTypes() {
        if (NanoHTTPD.MIME_TYPES == null) {
            loadMimeTypes(NanoHTTPD.MIME_TYPES = new HashMap<>(), "META-INF/nanohttpd/default-mimetypes.properties");
            loadMimeTypes(NanoHTTPD.MIME_TYPES, "META-INF/nanohttpd/mimetypes.properties");
            if (NanoHTTPD.MIME_TYPES.isEmpty()) {
                NanoHTTPD.LOG.log(Level.WARNING, "no mime types found in the classpath! please provide mimetypes.properties");
            }
        }
        return NanoHTTPD.MIME_TYPES;
    }

    private static void loadMimeTypes(final Map<String, String> result, final String resourceName) {
        try {
            final Enumeration<URL> resources = NanoHTTPD.class.getClassLoader().getResources(resourceName);
            while (resources.hasMoreElements()) {
                final URL url = resources.nextElement();
                final Properties properties = new Properties();
                InputStream stream = null;
                try {
                    stream = url.openStream();
                    properties.load(stream);
                } catch (IOException e) {
                    NanoHTTPD.LOG.log(Level.SEVERE, "could not load mimetypes from " + url, e);
                } finally {
                    safeClose(stream);
                }
                result.putAll((Map) properties);
            }
        } catch (IOException e2) {
            NanoHTTPD.LOG.log(Level.INFO, "no mime types available at " + resourceName);
        }
    }

    public static SSLServerSocketFactory makeSSLSocketFactory(final KeyStore loadedKeyStore, final KeyManager[] keyManagers) throws IOException {
        SSLServerSocketFactory res = null;
        try {
            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(loadedKeyStore);
            final SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(keyManagers, trustManagerFactory.getTrustManagers(), null);
            res = ctx.getServerSocketFactory();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return res;
    }

    public static SSLServerSocketFactory makeSSLSocketFactory(final KeyStore loadedKeyStore, final KeyManagerFactory loadedKeyFactory) throws IOException {
        try {
            return makeSSLSocketFactory(loadedKeyStore, loadedKeyFactory.getKeyManagers());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public static SSLServerSocketFactory makeSSLSocketFactory(final String keyAndTrustStoreClasspathPath, final char[] passphrase) throws IOException {
        try {
            final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            final InputStream keystoreStream = NanoHTTPD.class.getResourceAsStream(keyAndTrustStoreClasspathPath);
            if (keystoreStream == null) {
                throw new IOException("Unable to load keystore from classpath: " + keyAndTrustStoreClasspathPath);
            }
            keystore.load(keystoreStream, passphrase);
            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, passphrase);
            return makeSSLSocketFactory(keystore, keyManagerFactory);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public static String getMimeTypeForFile(final String uri) {
        final int dot = uri.lastIndexOf(46);
        String mime = null;
        if (dot >= 0) {
            mime = mimeTypes().get(uri.substring(dot + 1).toLowerCase());
        }
        return (mime == null) ? "application/octet-stream" : mime;
    }

    private static void safeClose(final Object closeable) {
        try {
            if (closeable != null) {
                if (closeable instanceof Closeable) {
                    ((Closeable) closeable).close();
                } else {
                    throw new IllegalArgumentException("Unknown object to close");
                }
            }
        } catch (IOException e) {
            NanoHTTPD.LOG.log(Level.SEVERE, "Could not close", e);
        }
    }

    public NanoHTTPD(final int port) {
        this(null, port);
    }

    public NanoHTTPD(final String hostname, final int port) {
        this.serverSocketFactory = new DefaultServerSocketFactory();
        this.hostname = hostname;
        this.myPort = port;
        this.setTempFileManagerFactory(new DefaultTempFileManagerFactory());
        this.setAsyncRunner(new DefaultAsyncRunner());
    }

    public synchronized void closeAllConnections() {
        this.stop();
    }

    protected ClientHandler createClientHandler(final Socket finalAccept, final InputStream inputStream) {
        return new ClientHandler(inputStream, finalAccept);
    }

    protected ServerRunnable createServerRunnable(final int timeout) {
        return new ServerRunnable(timeout);
    }

    protected static Map<String, List<String>> decodeParameters(final Map<String, String> parms) {
        return decodeParameters(parms.get("NanoHttpd.QUERY_STRING"));
    }

    protected static Map<String, List<String>> decodeParameters(final String queryString) {
        final Map<String, List<String>> parms = new HashMap<>();
        if (queryString != null) {
            final StringTokenizer st = new StringTokenizer(queryString, "&");
            while (st.hasMoreTokens()) {
                final String e = st.nextToken();
                final int sep = e.indexOf(61);
                final String propertyName = (sep >= 0) ? decodePercent(e.substring(0, sep)).trim() : decodePercent(e).trim();
                if (!parms.containsKey(propertyName)) {
                    parms.put(propertyName, new ArrayList<String>());
                }
                final String propertyValue = (sep >= 0) ? decodePercent(e.substring(sep + 1)) : null;
                if (propertyValue != null) {
                    parms.get(propertyName).add(propertyValue);
                }
            }
        }
        return parms;
    }

    protected static String decodePercent(final String str) {
        String decoded = null;
        try {
            decoded = URLDecoder.decode(str, "UTF8");
        } catch (UnsupportedEncodingException ignored) {
            NanoHTTPD.LOG.log(Level.WARNING, "Encoding not supported, ignored", ignored);
        }
        return decoded;
    }

    protected boolean useGzipWhenAccepted(final Response r) {
        return r.getMimeType() != null && (r.getMimeType().toLowerCase().contains("text/") || r.getMimeType().toLowerCase().contains("/json"));
    }

    public final int getListeningPort() {
        return (this.myServerSocket == null) ? -1 : this.myServerSocket.getLocalPort();
    }

    public final boolean isAlive() {
        return this.wasStarted() && !this.myServerSocket.isClosed() && this.myThread.isAlive();
    }

    public ServerSocketFactory getServerSocketFactory() {
        return this.serverSocketFactory;
    }

    public void setServerSocketFactory(final ServerSocketFactory serverSocketFactory) {
        this.serverSocketFactory = serverSocketFactory;
    }

    public String getHostname() {
        return this.hostname;
    }

    public TempFileManagerFactory getTempFileManagerFactory() {
        return this.tempFileManagerFactory;
    }

    public void makeSecure(final SSLServerSocketFactory sslServerSocketFactory, final String[] sslProtocols) {
        this.serverSocketFactory = new SecureServerSocketFactory(sslServerSocketFactory, sslProtocols);
    }

    public static Response newChunkedResponse(final Response.IStatus status, final String mimeType, final InputStream data) {
        return new Response(status, mimeType, data, -1L);
    }

    public static Response newFixedLengthResponse(final Response.IStatus status, final String mimeType, final InputStream data, final long totalBytes) {
        return new Response(status, mimeType, data, totalBytes);
    }

    public static Response newFixedLengthResponse(final Response.IStatus status, final String mimeType, final String txt) {
        ContentType contentType = new ContentType(mimeType);
        if (txt == null) {
            return newFixedLengthResponse(status, mimeType, new ByteArrayInputStream(new byte[0]), 0L);
        }
        byte[] bytes;
        try {
            final CharsetEncoder newEncoder = Charset.forName(contentType.getEncoding()).newEncoder();
            if (!newEncoder.canEncode(txt)) {
                contentType = contentType.tryUTF8();
            }
            bytes = txt.getBytes(contentType.getEncoding());
        } catch (UnsupportedEncodingException e) {
            NanoHTTPD.LOG.log(Level.SEVERE, "encoding problem, responding nothing", e);
            bytes = new byte[0];
        }
        return newFixedLengthResponse(status, contentType.getContentTypeHeader(), new ByteArrayInputStream(bytes), bytes.length);
    }

    public static Response newFixedLengthResponse(final String msg) {
        return newFixedLengthResponse(Response.Status.OK, "text/html", msg);
    }

    public Response serve(final IHTTPSession session) {
        final Map<String, String> files = new HashMap<>();
        final Method method = session.getMethod();
        Label_0097:
        {
            if (!Method.PUT.equals(method)) {
                if (!Method.POST.equals(method)) {
                    break Label_0097;
                }
            }
            try {
                session.parseBody(files);
            } catch (IOException ioe) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            } catch (ResponseException re) {
                return newFixedLengthResponse(re.getStatus(), "text/plain", re.getMessage());
            } catch (Exception exception) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", Response.Status.INTERNAL_ERROR.description);
            }
        }
        final Map<String, String> parms = session.getParms();
        parms.put("NanoHttpd.QUERY_STRING", session.getQueryParameterString());
        return this.serve(session.getUri(), method, session.getHeaders(), parms, files);
    }

    @Deprecated
    public Response serve(final String uri, final Method method, final Map<String, String> headers, final Map<String, String> parms, final Map<String, String> files) {
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
    }

    public void setAsyncRunner(final AsyncRunner asyncRunner) {
        this.asyncRunner = asyncRunner;
    }

    public void setTempFileManagerFactory(final TempFileManagerFactory tempFileManagerFactory) {
        this.tempFileManagerFactory = tempFileManagerFactory;
    }

    public void start() throws IOException {
        this.start(5000);
    }

    public void start(final int timeout) throws IOException {
        this.start(timeout, true);
    }

    public void start(final int timeout, final boolean daemon) throws IOException {
        (this.myServerSocket = this.getServerSocketFactory().create()).setReuseAddress(true);
        final ServerRunnable serverRunnable = this.createServerRunnable(timeout);
        (this.myThread = new Thread(serverRunnable)).setDaemon(daemon);
        this.myThread.setName("NanoHttpd Main Listener");
        this.myThread.start();
        while (!serverRunnable.hasBinded && serverRunnable.bindException == null) {
            try {
                Thread.sleep(10L);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (serverRunnable.bindException != null) {
            throw serverRunnable.bindException;
        }
    }

    public void stop() {
        try {
            safeClose(this.myServerSocket);
            this.asyncRunner.closeAll();
            if (this.myThread != null) {
                this.myThread.join();
            }
        } catch (Exception e) {
            NanoHTTPD.LOG.log(Level.SEVERE, "Could not stop all connections", e);
        }
    }

    public final boolean wasStarted() {
        return this.myServerSocket != null && this.myThread != null;
    }

    static {
        CONTENT_DISPOSITION_PATTERN = Pattern.compile("([ |\t]*Content-Disposition[ |\t]*:)(.*)", 2);
        CONTENT_TYPE_PATTERN = Pattern.compile("([ |\t]*content-type[ |\t]*:)(.*)", 2);
        CONTENT_DISPOSITION_ATTRIBUTE_PATTERN = Pattern.compile("[ |\t]*([a-zA-Z]*)[ |\t]*=[ |\t]*['|\"]([^\"^']*)['|\"]");
        LOG = Logger.getLogger(NanoHTTPD.class.getName());
    }

    public class ClientHandler implements Runnable {
        private final String TAG = "ClientHandler";
        private final InputStream inputStream;
        private final Socket acceptSocket;

        public ClientHandler(final InputStream inputStream, final Socket acceptSocket) {
            this.inputStream = inputStream;
            this.acceptSocket = acceptSocket;
        }

        public void close() {
            safeClose(this.inputStream);
            safeClose(this.acceptSocket);
        }

        @Override
        public void run() {
            OutputStream outputStream = null;
            try {
                outputStream = this.acceptSocket.getOutputStream();
                final TempFileManager tempFileManager = NanoHTTPD.this.tempFileManagerFactory.create();
                final HTTPSession session = new HTTPSession(tempFileManager, this.inputStream, outputStream, this.acceptSocket.getInetAddress());
                //                while (!this.acceptSocket.isClosed() && this.inputStream.available() > 0) {
                session.execute();
                //                }
            } catch (Exception e) {
                if ((!(e instanceof SocketException) || !"NanoHttpd Shutdown".equals(e.getMessage())) && !(e instanceof SocketTimeoutException)) {
                    NanoHTTPD.LOG.log(Level.SEVERE, "Communication with the client broken, or an bug in the handler code", e);
                }
            } finally {
                safeClose(outputStream);
                safeClose(this.inputStream);
                safeClose(this.acceptSocket);
                NanoHTTPD.this.asyncRunner.closed(this);
            }
        }
    }

    public static class Cookie {
        private final String n;
        private final String v;
        private final String e;

        public static String getHTTPTime(final int days) {
            final Calendar calendar = Calendar.getInstance();
            final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            calendar.add(5, days);
            return dateFormat.format(calendar.getTime());
        }

        public Cookie(final String name, final String value) {
            this(name, value, 30);
        }

        public Cookie(final String name, final String value, final int numDays) {
            this.n = name;
            this.v = value;
            this.e = getHTTPTime(numDays);
        }

        public Cookie(final String name, final String value, final String expires) {
            this.n = name;
            this.v = value;
            this.e = expires;
        }

        public String getHTTPHeader() {
            final String fmt = "%s=%s; expires=%s";
            return String.format(fmt, this.n, this.v, this.e);
        }
    }

    public static class CookieHandler implements Iterable<String> {
        private final HashMap<String, String> cookies;
        private final ArrayList<Cookie> queue;

        public CookieHandler(final Map<String, String> httpHeaders) {
            this.cookies = new HashMap<String, String>();
            this.queue = new ArrayList<Cookie>();
            final String raw = httpHeaders.get("cookie");
            if (raw != null) {
                final String[] arr$;
                final String[] tokens = arr$ = raw.split(";");
                for (final String token : arr$) {
                    final String[] data = token.trim().split("=");
                    if (data.length == 2) {
                        this.cookies.put(data[0], data[1]);
                    }
                }
            }
        }

        public void delete(final String name) {
            this.set(name, "-delete-", -30);
        }

        @Override
        public Iterator<String> iterator() {
            return this.cookies.keySet().iterator();
        }

        public String read(final String name) {
            return this.cookies.get(name);
        }

        public void set(final Cookie cookie) {
            this.queue.add(cookie);
        }

        public void set(final String name, final String value, final int expires) {
            this.queue.add(new Cookie(name, value, Cookie.getHTTPTime(expires)));
        }

        public void unloadQueue(final Response response) {
            for (final Cookie cookie : this.queue) {
                response.addHeader("Set-Cookie", cookie.getHTTPHeader());
            }
        }
    }

    public static class DefaultAsyncRunner implements AsyncRunner {
        private long requestCount;
        private final List<ClientHandler> running;

        public DefaultAsyncRunner() {
            this.running = Collections.synchronizedList(new ArrayList<ClientHandler>());
        }

        public List<ClientHandler> getRunning() {
            return this.running;
        }

        @Override
        public void closeAll() {
            for (ClientHandler clientHandler : new ArrayList<>(this.running)) {
                clientHandler.close();
            }
        }

        @Override
        public void closed(final ClientHandler clientHandler) {
            this.running.remove(clientHandler);
        }

        @Override
        public void exec(final ClientHandler clientHandler) {
            ++this.requestCount;
            final Thread t = new Thread(clientHandler);
            t.setDaemon(true);
            t.setName("NanoHttpd Request Processor (#" + this.requestCount + ")");
            this.running.add(clientHandler);
            t.start();
        }
    }

    public static class DefaultTempFile implements TempFile {
        private final File file;
        private final OutputStream fstream;

        public DefaultTempFile(final File tempdir) throws IOException {
            this.file = File.createTempFile("NanoHTTPD-", "", tempdir);
            this.fstream = new FileOutputStream(this.file);
        }

        @Override
        public void delete() throws Exception {
            safeClose(this.fstream);
            if (!this.file.delete()) {
                throw new Exception("could not delete temporary file: " + this.file.getAbsolutePath());
            }
        }

        @Override
        public String getName() {
            return this.file.getAbsolutePath();
        }

        @Override
        public OutputStream open() throws Exception {
            return this.fstream;
        }
    }

    public static class DefaultTempFileManager implements TempFileManager {
        private final File tmpdir;
        private final List<TempFile> tempFiles;

        public DefaultTempFileManager() {
            this.tmpdir = new File(System.getProperty("java.io.tmpdir"));
            if (!this.tmpdir.exists()) {
                this.tmpdir.mkdirs();
            }
            this.tempFiles = new ArrayList<TempFile>();
        }

        @Override
        public void clear() {
            for (final TempFile file : this.tempFiles) {
                try {
                    file.delete();
                } catch (Exception ignored) {
                    NanoHTTPD.LOG.log(Level.WARNING, "could not delete file ", ignored);
                }
            }
            this.tempFiles.clear();
        }

        @Override
        public TempFile createTempFile(final String filename_hint) throws Exception {
            final DefaultTempFile tempFile = new DefaultTempFile(this.tmpdir);
            this.tempFiles.add(tempFile);
            return tempFile;
        }
    }

    private class DefaultTempFileManagerFactory implements TempFileManagerFactory {
        @Override
        public TempFileManager create() {
            return new DefaultTempFileManager();
        }
    }

    public static class DefaultServerSocketFactory implements ServerSocketFactory {
        @Override
        public ServerSocket create() throws IOException {
            return new ServerSocket();
        }
    }

    public static class SecureServerSocketFactory implements ServerSocketFactory {
        private SSLServerSocketFactory sslServerSocketFactory;
        private String[] sslProtocols;

        public SecureServerSocketFactory(final SSLServerSocketFactory sslServerSocketFactory, final String[] sslProtocols) {
            this.sslServerSocketFactory = sslServerSocketFactory;
            this.sslProtocols = sslProtocols;
        }

        @Override
        public ServerSocket create() throws IOException {
            SSLServerSocket ss = null;
            ss = (SSLServerSocket) this.sslServerSocketFactory.createServerSocket();
            if (this.sslProtocols != null) {
                ss.setEnabledProtocols(this.sslProtocols);
            } else {
                ss.setEnabledProtocols(ss.getSupportedProtocols());
            }
            ss.setUseClientMode(false);
            ss.setWantClientAuth(false);
            ss.setNeedClientAuth(false);
            return ss;
        }
    }

    protected static class ContentType {
        private static final String ASCII_ENCODING = "US-ASCII";
        private static final String MULTIPART_FORM_DATA_HEADER = "multipart/form-data";
        private static final String CONTENT_REGEX = "[ |\t]*([^/^ ^;^,]+/[^ ^;^,]+)";
        private static final Pattern MIME_PATTERN;
        private static final String CHARSET_REGEX = "[ |\t]*(charset)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?";
        private static final Pattern CHARSET_PATTERN;
        private static final String BOUNDARY_REGEX = "[ |\t]*(boundary)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?";
        private static final Pattern BOUNDARY_PATTERN;
        private final String contentTypeHeader;
        private final String contentType;
        private final String encoding;
        private final String boundary;

        public ContentType(final String contentTypeHeader) {
            this.contentTypeHeader = contentTypeHeader;
            if (contentTypeHeader != null) {
                this.contentType = this.getDetailFromContentHeader(contentTypeHeader, ContentType.MIME_PATTERN, "", 1);
                this.encoding = this.getDetailFromContentHeader(contentTypeHeader, ContentType.CHARSET_PATTERN, null, 2);
            } else {
                this.contentType = "";
                this.encoding = "UTF-8";
            }
            if ("multipart/form-data".equalsIgnoreCase(this.contentType)) {
                this.boundary = this.getDetailFromContentHeader(contentTypeHeader, ContentType.BOUNDARY_PATTERN, null, 2);
            } else {
                this.boundary = null;
            }
        }

        private String getDetailFromContentHeader(final String contentTypeHeader, final Pattern pattern, final String defaultValue, final int group) {
            final Matcher matcher = pattern.matcher(contentTypeHeader);
            return matcher.find() ? matcher.group(group) : defaultValue;
        }

        public String getContentTypeHeader() {
            return this.contentTypeHeader;
        }

        public String getContentType() {
            return this.contentType;
        }

        public String getEncoding() {
            return (this.encoding == null) ? "UTF-8" : this.encoding;
        }

        public String getBoundary() {
            return this.boundary;
        }

        public boolean isMultipart() {
            return "multipart/form-data".equalsIgnoreCase(this.contentType);
        }

        public ContentType tryUTF8() {
            if (this.encoding == null) {
                return new ContentType(this.contentTypeHeader + "; charset=UTF-8");
            }
            return this;
        }

        static {
            MIME_PATTERN = Pattern.compile("[ |\t]*([^/^ ^;^,]+/[^ ^;^,]+)", 2);
            CHARSET_PATTERN = Pattern.compile("[ |\t]*(charset)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?", 2);
            BOUNDARY_PATTERN = Pattern.compile("[ |\t]*(boundary)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?", 2);
        }
    }

    protected class HTTPSession implements IHTTPSession {
        private static final int REQUEST_BUFFER_LEN = 512;
        private static final int MEMORY_STORE_LIMIT = 1024;
        public static final int BUFSIZE = 8192;
        public static final int MAX_HEADER_SIZE = 1024;
        private static final String TAG = "HTTPSession";
        private final TempFileManager tempFileManager;
        private final OutputStream outputStream;
        private final BufferedInputStream inputStream;
        //        private Socket acceptSocket;
        private int splitbyte;
        private int rlen;
        private String uri;
        private Method method;
        private Map<String, List<String>> parms;
        private Map<String, String> headers;
        private CookieHandler cookies;
        private String queryParameterString;
        private String remoteIp;
        private String remoteHostname;
        private String protocolVersion;

        public HTTPSession(final TempFileManager tempFileManager, final InputStream inputStream, final OutputStream outputStream) {
            this.tempFileManager = tempFileManager;
            this.inputStream = new BufferedInputStream(inputStream, 8192);
            this.outputStream = outputStream;
        }

        public HTTPSession(final TempFileManager tempFileManager, final InputStream inputStream, final OutputStream outputStream, final InetAddress inetAddress) {
            this.tempFileManager = tempFileManager;
            this.inputStream = new BufferedInputStream(inputStream, 8192);
            this.outputStream = outputStream;
            this.remoteIp = ((inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress()) ? "127.0.0.1" : inetAddress.getHostAddress().toString());
            this.remoteHostname = ((inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress()) ? "localhost" : inetAddress.getHostName().toString());
            this.headers = new HashMap<>();
        }

        //        public HTTPSession(final TempFileManager tempFileManager, final InputStream inputStream, final OutputStream outputStream, Socket acceptSocket, final InetAddress inetAddress) {
        //            this.tempFileManager = tempFileManager;
        //            this.inputStream = new BufferedInputStream(inputStream, 8192);
        //            this.outputStream = outputStream;
        //            this.acceptSocket = acceptSocket;
        //            this.remoteIp = ((inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress()) ? "127.0.0.1" : inetAddress.getHostAddress().toString());
        //            this.remoteHostname = ((inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress()) ? "localhost" : inetAddress.getHostName().toString());
        //            this.headers = new HashMap<>();
        //        }

        private void decodeHeader(final BufferedReader in, final Map<String, String> pre, final Map<String, List<String>> parms, final Map<String, String> headers) throws ResponseException {
            try {
                final String inLine = in.readLine();
                if (inLine == null) {
                    return;
                }
                final StringTokenizer st = new StringTokenizer(inLine);
                if (!st.hasMoreTokens()) {
                    throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
                }
                pre.put("method", st.nextToken());
                if (!st.hasMoreTokens()) {
                    throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
                }
                String uri = st.nextToken();
                final int qmi = uri.indexOf(63);
                if (qmi >= 0) {
                    this.decodeParms(uri.substring(qmi + 1), parms);
                    uri = NanoHTTPD.decodePercent(uri.substring(0, qmi));
                } else {
                    uri = NanoHTTPD.decodePercent(uri);
                }
                if (st.hasMoreTokens()) {
                    this.protocolVersion = st.nextToken();
                } else {
                    this.protocolVersion = "HTTP/1.1";
                    NanoHTTPD.LOG.log(Level.FINE, "no protocol version specified, strange. Assuming HTTP/1.1.");
                }
                for (String line = in.readLine(); line != null && !line.trim().isEmpty(); line = in.readLine()) {
                    final int p = line.indexOf(58);
                    if (p >= 0) {
                        headers.put(line.substring(0, p).trim().toLowerCase(Locale.US), line.substring(p + 1).trim());
                    }
                }
                pre.put("uri", uri);
            } catch (IOException ioe) {
                throw new ResponseException(Response.Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage(), ioe);
            }
        }

        private void decodeMultipartFormData(final ContentType contentType, final ByteBuffer fbuf, final Map<String, List<String>> parms, final Map<String, String> files) throws ResponseException {
            int pcount = 0;
            try {
                final int[] boundaryIdxs = this.getBoundaryPositions(fbuf, contentType.getBoundary().getBytes());
                if (boundaryIdxs.length < 2) {
                    throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but contains less than two boundary strings.");
                }
                final byte[] partHeaderBuff = new byte[1024];
                for (int boundaryIdx = 0; boundaryIdx < boundaryIdxs.length - 1; ++boundaryIdx) {
                    fbuf.position(boundaryIdxs[boundaryIdx]);
                    final int len = Math.min(fbuf.remaining(), 1024);
                    fbuf.get(partHeaderBuff, 0, len);
                    final BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(partHeaderBuff, 0, len), Charset.forName(contentType.getEncoding())), len);
                    int headerLines = 0;
                    String mpline = in.readLine();
                    ++headerLines;
                    if (mpline == null || !mpline.contains(contentType.getBoundary())) {
                        throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but chunk does not start with boundary.");
                    }
                    String partName = null;
                    String fileName = null;
                    String partContentType = null;
                    mpline = in.readLine();
                    ++headerLines;
                    while (mpline != null && mpline.trim().length() > 0) {
                        Matcher matcher = NanoHTTPD.CONTENT_DISPOSITION_PATTERN.matcher(mpline);
                        if (matcher.matches()) {
                            final String attributeString = matcher.group(2);
                            matcher = NanoHTTPD.CONTENT_DISPOSITION_ATTRIBUTE_PATTERN.matcher(attributeString);
                            while (matcher.find()) {
                                final String key = matcher.group(1);
                                if ("name".equalsIgnoreCase(key)) {
                                    partName = matcher.group(2);
                                } else {
                                    if (!"filename".equalsIgnoreCase(key)) {
                                        continue;
                                    }
                                    fileName = matcher.group(2);
                                    if (fileName.isEmpty()) {
                                        continue;
                                    }
                                    if (pcount > 0) {
                                        partName += String.valueOf(pcount++);
                                    } else {
                                        ++pcount;
                                    }
                                }
                            }
                        }
                        matcher = NanoHTTPD.CONTENT_TYPE_PATTERN.matcher(mpline);
                        if (matcher.matches()) {
                            partContentType = matcher.group(2).trim();
                        }
                        mpline = in.readLine();
                        ++headerLines;
                    }
                    int partHeaderLength = 0;
                    while (headerLines-- > 0) {
                        partHeaderLength = this.scipOverNewLine(partHeaderBuff, partHeaderLength);
                    }
                    if (partHeaderLength >= len - 4) {
                        throw new ResponseException(Response.Status.INTERNAL_ERROR, "Multipart header size exceeds MAX_HEADER_SIZE.");
                    }
                    final int partDataStart = boundaryIdxs[boundaryIdx] + partHeaderLength;
                    final int partDataEnd = boundaryIdxs[boundaryIdx + 1] - 4;
                    fbuf.position(partDataStart);
                    List<String> values = parms.get(partName);
                    if (values == null) {
                        values = new ArrayList<String>();
                        parms.put(partName, values);
                    }
                    if (partContentType == null) {
                        final byte[] data_bytes = new byte[partDataEnd - partDataStart];
                        fbuf.get(data_bytes);
                        values.add(new String(data_bytes, contentType.getEncoding()));
                    } else {
                        final String path = this.saveTmpFile(fbuf, partDataStart, partDataEnd - partDataStart, fileName);
                        if (!files.containsKey(partName)) {
                            files.put(partName, path);
                        } else {
                            int count;
                            for (count = 2; files.containsKey(partName + count); ++count) {
                            }
                            files.put(partName + count, path);
                        }
                        values.add(fileName);
                    }
                }
            } catch (ResponseException re) {
                throw re;
            } catch (Exception e) {
                throw new ResponseException(Response.Status.INTERNAL_ERROR, e.toString());
            }
        }

        private int scipOverNewLine(final byte[] partHeaderBuff, int index) {
            while (partHeaderBuff[index] != 10) {
                ++index;
            }
            return ++index;
        }

        private void decodeParms(final String parms, final Map<String, List<String>> p) {
            if (parms == null) {
                this.queryParameterString = "";
                return;
            }
            this.queryParameterString = parms;
            final StringTokenizer st = new StringTokenizer(parms, "&");
            while (st.hasMoreTokens()) {
                final String e = st.nextToken();
                final int sep = e.indexOf(61);
                String key = null;
                String value = null;
                if (sep >= 0) {
                    key = NanoHTTPD.decodePercent(e.substring(0, sep)).trim();
                    value = NanoHTTPD.decodePercent(e.substring(sep + 1));
                } else {
                    key = NanoHTTPD.decodePercent(e).trim();
                    value = "";
                }
                List<String> values = p.get(key);
                if (values == null) {
                    values = new ArrayList<String>();
                    p.put(key, values);
                }
                values.add(value);
            }
        }

        @Override
        public void execute() throws IOException {
            Response response = null;
            try {
                int buffer_size = 1024 * 10;
                final byte[] buf = new byte[buffer_size];
                this.splitbyte = 0;
                this.rlen = 0;
                int read = -1;
                this.inputStream.mark(buffer_size);
                try {
                    read = this.inputStream.read(buf, 0, buffer_size);
                } catch (SSLException e) {
                    throw e;
                } catch (IOException e3) {
                    safeClose(this.inputStream);
                    safeClose(this.outputStream);
                    throw new SocketException("NanoHttpd Shutdown");
                }
                if (read == -1) {
                    safeClose(this.inputStream);
                    safeClose(this.outputStream);
                    throw new SocketException("NanoHttpd Shutdown");
                }
                while (read > 0) {
                    this.rlen += read;
                    this.splitbyte = this.findHeaderEnd(buf, this.rlen);
                    if (this.splitbyte > 0) {
                        break;
                    }
                    read = this.inputStream.read(buf, this.rlen, buffer_size - this.rlen);
                }
                if (this.splitbyte < this.rlen) {
                    this.inputStream.reset();
                    long skip = this.inputStream.skip(this.splitbyte);
                }
                this.parms = new HashMap<>();
                if (null == this.headers) {
                    this.headers = new HashMap<>();
                } else {
                    this.headers.clear();
                }
                final BufferedReader hin = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf, 0, this.rlen)));
                final Map<String, String> pre = new HashMap<>();
                this.decodeHeader(hin, pre, this.parms, this.headers);
                if (null != this.remoteIp) {
                    this.headers.put("remote-addr", this.remoteIp);
                    this.headers.put("http-client-ip", this.remoteIp);
                }
                this.method = Method.lookup(pre.get("method"));
                if (this.method == null) {
                    throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Syntax error. HTTP verb " + pre.get("method") + " unhandled.");
                }
                this.uri = pre.get("uri");
                this.cookies = new CookieHandler(this.headers);
                final String connection = this.headers.get("connection");
                final boolean keepAlive = "HTTP/1.1".equals(this.protocolVersion) && (connection == null || !connection.matches("(?i).*close.*"));
                response = NanoHTTPD.this.serve(this);
                if (response == null) {
                    throw new ResponseException(Response.Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: Serve() returned a null response.");
                }
                final String acceptEncoding = this.headers.get("accept-encoding");
                this.cookies.unloadQueue(response);
                response.setRequestMethod(this.method);
                response.setGzipEncoding(NanoHTTPD.this.useGzipWhenAccepted(response) && acceptEncoding != null && acceptEncoding.contains("gzip"));
                response.setKeepAlive(keepAlive);
                boolean send = response.send(this.outputStream);
                if (!keepAlive || response.isCloseConnection()) {
                    throw new SocketException("NanoHttpd Shutdown");
                }
            } catch (SocketException | SocketTimeoutException e2) {
                throw e2;
            } catch (SSLException ssle) {
                final Response resp = NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "SSL PROTOCOL FAILURE: " + ssle.getMessage());
                resp.send(this.outputStream);
                safeClose(this.outputStream);
            } catch (IOException ioe) {
                final Response resp = NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
                resp.send(this.outputStream);
                safeClose(this.outputStream);
            } catch (ResponseException re) {
                final Response resp = NanoHTTPD.newFixedLengthResponse(re.getStatus(), "text/plain", re.getMessage());
                resp.send(this.outputStream);
                safeClose(this.outputStream);
            } catch (Exception exception) {
                final Response resp = NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", Response.Status.INTERNAL_ERROR.description);
                resp.send(this.outputStream);
                safeClose(this.outputStream);
            } finally {
                safeClose(response);
            }
        }

        private int findHeaderEnd(final byte[] buf, final int rlen) {
            for (int splitbyte = 0; splitbyte + 1 < rlen; ++splitbyte) {
                if (buf[splitbyte] == 13 && buf[splitbyte + 1] == 10 && splitbyte + 3 < rlen && buf[splitbyte + 2] == 13 && buf[splitbyte + 3] == 10) {
                    return splitbyte + 4;
                }
                if (buf[splitbyte] == 10 && buf[splitbyte + 1] == 10) {
                    return splitbyte + 2;
                }
            }
            return 0;
        }

        private int[] getBoundaryPositions(final ByteBuffer b, final byte[] boundary) {
            int[] res = new int[0];
            if (b.remaining() < boundary.length) {
                return res;
            }
            int search_window_pos = 0;
            final byte[] search_window = new byte[4096 + boundary.length];
            final int first_fill = (b.remaining() < search_window.length) ? b.remaining() : search_window.length;
            b.get(search_window, 0, first_fill);
            int new_bytes = first_fill - boundary.length;
            do {
                for (int j = 0; j < new_bytes; ++j) {
                    for (int i = 0; i < boundary.length && search_window[j + i] == boundary[i]; ++i) {
                        if (i == boundary.length - 1) {
                            final int[] new_res = new int[res.length + 1];
                            System.arraycopy(res, 0, new_res, 0, res.length);
                            new_res[res.length] = search_window_pos + j;
                            res = new_res;
                        }
                    }
                }
                search_window_pos += new_bytes;
                System.arraycopy(search_window, search_window.length - boundary.length, search_window, 0, boundary.length);
                new_bytes = search_window.length - boundary.length;
                new_bytes = ((b.remaining() < new_bytes) ? b.remaining() : new_bytes);
                b.get(search_window, boundary.length, new_bytes);
            } while (new_bytes > 0);
            return res;
        }

        @Override
        public CookieHandler getCookies() {
            return this.cookies;
        }

        @Override
        public final Map<String, String> getHeaders() {
            return this.headers;
        }

        @Override
        public final InputStream getInputStream() {
            return this.inputStream;
        }

        @Override
        public final Method getMethod() {
            return this.method;
        }

        @Deprecated
        @Override
        public final Map<String, String> getParms() {
            final Map<String, String> result = new HashMap<String, String>();
            for (final String key : this.parms.keySet()) {
                result.put(key, this.parms.get(key).get(0));
            }
            return result;
        }

        @Override
        public final Map<String, List<String>> getParameters() {
            return this.parms;
        }

        @Override
        public String getQueryParameterString() {
            return this.queryParameterString;
        }

        private RandomAccessFile getTmpBucket() {
            try {
                final TempFile tempFile = this.tempFileManager.createTempFile(null);
                return new RandomAccessFile(tempFile.getName(), "rw");
            } catch (Exception e) {
                throw new Error(e);
            }
        }

        @Override
        public final String getUri() {
            return this.uri;
        }

        public long getBodySize() {
            if (this.headers.containsKey("content-length")) {
                return Long.parseLong(this.headers.get("content-length"));
            }
            if (this.splitbyte < this.rlen) {
                return this.rlen - this.splitbyte;
            }
            return 0L;
        }

        @Override
        public void parseBody(final Map<String, String> files) throws IOException, ResponseException {
            RandomAccessFile randomAccessFile = null;
            try {
                long size = this.getBodySize();
                ByteArrayOutputStream baos = null;
                DataOutput requestDataOutput = null;
                if (size < 1024L) {
                    baos = new ByteArrayOutputStream();
                    requestDataOutput = new DataOutputStream(baos);
                } else {
                    randomAccessFile = (RandomAccessFile) (requestDataOutput = this.getTmpBucket());
                }
                final byte[] buf = new byte[512];
                while (this.rlen >= 0 && size > 0L) {
                    this.rlen = this.inputStream.read(buf, 0, (int) Math.min(size, 512L));
                    size -= this.rlen;
                    if (this.rlen > 0) {
                        requestDataOutput.write(buf, 0, this.rlen);
                    }
                }
                ByteBuffer fbuf = null;
                if (baos != null) {
                    fbuf = ByteBuffer.wrap(baos.toByteArray(), 0, baos.size());
                } else {
                    fbuf = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0L, randomAccessFile.length());
                    randomAccessFile.seek(0L);
                }
                if (Method.POST.equals(this.method)) {
                    final ContentType contentType = new ContentType(this.headers.get("content-type"));
                    if (contentType.isMultipart()) {
                        final String boundary = contentType.getBoundary();
                        if (boundary == null) {
                            throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but boundary missing. Usage: GET /example/file.html");
                        }
                        this.decodeMultipartFormData(contentType, fbuf, this.parms, files);
                    } else {
                        final byte[] postBytes = new byte[fbuf.remaining()];
                        fbuf.get(postBytes);
                        final String postLine = new String(postBytes, contentType.getEncoding()).trim();
                        if ("application/x-www-form-urlencoded".equalsIgnoreCase(contentType.getContentType())) {
                            this.decodeParms(postLine, this.parms);
                        } else if (postLine.length() != 0) {
                            files.put("Data", postLine);
                        }
                    }
                } else if (Method.PUT.equals(this.method)) {
                    files.put("content", this.saveTmpFile(fbuf, 0, fbuf.limit(), null));
                }
            } finally {
                safeClose(randomAccessFile);
            }
        }

        private String saveTmpFile(final ByteBuffer b, final int offset, final int len, final String filename_hint) {
            String path = "";
            if (len > 0) {
                FileOutputStream fileOutputStream = null;
                try {
                    final TempFile tempFile = this.tempFileManager.createTempFile(filename_hint);
                    final ByteBuffer src = b.duplicate();
                    fileOutputStream = new FileOutputStream(tempFile.getName());
                    final FileChannel dest = fileOutputStream.getChannel();
                    src.position(offset).limit(offset + len);
                    dest.write(src.slice());
                    path = tempFile.getName();
                } catch (Exception e) {
                    throw new Error(e);
                } finally {
                    safeClose(fileOutputStream);
                }
            }
            return path;
        }

        @Override
        public String getRemoteIpAddress() {
            return this.remoteIp;
        }

        @Override
        public String getRemoteHostName() {
            return this.remoteHostname;
        }
    }

    public enum Method {
        GET,
        PUT,
        POST,
        DELETE,
        HEAD,
        OPTIONS,
        TRACE,
        CONNECT,
        PATCH,
        PROPFIND,
        PROPPATCH,
        MKCOL,
        MOVE,
        COPY,
        LOCK,
        UNLOCK;

        static Method lookup(final String method) {
            if (method == null) {
                return null;
            }
            try {
                return valueOf(method);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public static class Response implements Closeable {
        private static final String TAG = "Response";
        private IStatus status;
        private String mimeType;
        private InputStream data;
        private long contentLength;
        private final Map<String, String> header;
        private final Map<String, String> lowerCaseHeader;
        private Method requestMethod;
        private boolean chunkedTransfer;
        private boolean encodeAsGzip;
        private boolean keepAlive;

        protected Response(final IStatus status, final String mimeType, final InputStream data, final long totalBytes) {
            this.header = new HashMap<String, String>() {
                @Override
                public String put(final String key, final String value) {
                    Response.this.lowerCaseHeader.put((key == null) ? key : key.toLowerCase(), value);
                    return super.put(key, value);
                }
            };
            this.lowerCaseHeader = new HashMap<String, String>();
            this.status = status;
            this.mimeType = mimeType;
            if (data == null) {
                this.data = new ByteArrayInputStream(new byte[0]);
                this.contentLength = 0L;
            } else {
                this.data = data;
                this.contentLength = totalBytes;
            }
            this.chunkedTransfer = (this.contentLength < 0L);
            this.keepAlive = true;
        }

        @Override
        public void close() throws IOException {
            if (this.data != null) {
                this.data.close();
            }
        }

        public void addHeader(final String name, final String value) {
            this.header.put(name, value);
        }

        public void closeConnection(final boolean close) {
            if (close) {
                this.header.put("connection", "close");
            } else {
                this.header.remove("connection");
            }
        }

        public boolean isCloseConnection() {
            return "close".equals(this.getHeader("connection"));
        }

        public InputStream getData() {
            return this.data;
        }

        public String getHeader(final String name) {
            return this.lowerCaseHeader.get(name.toLowerCase());
        }

        public String getMimeType() {
            return this.mimeType;
        }

        public Method getRequestMethod() {
            return this.requestMethod;
        }

        public IStatus getStatus() {
            return this.status;
        }

        public void setGzipEncoding(final boolean encodeAsGzip) {
            this.encodeAsGzip = encodeAsGzip;
        }

        public void setKeepAlive(final boolean useKeepAlive) {
            this.keepAlive = useKeepAlive;
        }

        protected boolean send(final OutputStream outputStream) {
            final SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
            gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                if (this.status == null) {
                    throw new Error("sendResponse(): Status can't be null.");
                }
                final PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, new ContentType(this.mimeType).getEncoding())), false);
                pw.append("HTTP/1.1 ").append(this.status.getDescription()).append(" \r\n");
                if (this.mimeType != null) {
                    this.printHeader(pw, "Content-Type", this.mimeType);
                }
                if (this.getHeader("date") == null) {
                    this.printHeader(pw, "Date", gmtFrmt.format(new Date()));
                }
                for (final Map.Entry<String, String> entry : this.header.entrySet()) {
                    this.printHeader(pw, entry.getKey(), entry.getValue());
                }
                if (this.getHeader("connection") == null) {
                    this.printHeader(pw, "Connection", this.keepAlive ? "keep-alive" : "close");
                }
                if (this.getHeader("content-length") != null) {
                    this.encodeAsGzip = false;
                }
                if (this.encodeAsGzip) {
                    this.printHeader(pw, "Content-Encoding", "gzip");
                    this.setChunkedTransfer(true);
                }
                long pending = (this.data != null) ? this.contentLength : 0L;
                if (this.requestMethod != Method.HEAD && this.chunkedTransfer) {
                    this.printHeader(pw, "Transfer-Encoding", "chunked");
                } else if (!this.encodeAsGzip) {
                    pending = this.sendContentLengthHeaderIfNotAlreadyPresent(pw, pending);
                }
                pw.append("\r\n");
                pw.flush();
                this.sendBodyWithCorrectTransferAndEncoding(outputStream, pending);
                safeClose(this.data);
                return true;
            } catch (Exception ioe) {
                NanoHTTPD.LOG.log(Level.SEVERE, "Could not send response to the client", ioe);
                safeClose(this.data);
                return false;
            }
        }

        protected void printHeader(final PrintWriter pw, final String key, final String value) {
            pw.append(key).append(": ").append(value).append("\r\n");
        }

        protected long sendContentLengthHeaderIfNotAlreadyPresent(final PrintWriter pw, final long defaultSize) {
            final String contentLengthString = this.getHeader("content-length");
            long size = defaultSize;
            if (contentLengthString != null) {
                try {
                    size = Long.parseLong(contentLengthString);
                } catch (NumberFormatException ex) {
                    NanoHTTPD.LOG.severe("content-length was no number " + contentLengthString);
                }
            }
            pw.print("Content-Length: " + size + "\r\n");
            return size;
        }

        private void sendBodyWithCorrectTransferAndEncoding(final OutputStream outputStream, final long pending) throws IOException {
            if (this.requestMethod != Method.HEAD && this.chunkedTransfer) {
                final ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream(outputStream);
                this.sendBodyWithCorrectEncoding(chunkedOutputStream, -1L);
                chunkedOutputStream.finish();
            } else {
                this.sendBodyWithCorrectEncoding(outputStream, pending);
            }
        }

        private void sendBodyWithCorrectEncoding(final OutputStream outputStream, final long pending) throws IOException {
            if (this.encodeAsGzip) {
                final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
                this.sendBody(gzipOutputStream, -1L);
                gzipOutputStream.finish();
            } else {
                this.sendBody(outputStream, pending);
            }
        }

        private void sendBody(final OutputStream outputStream, long pending) throws IOException {
            //final long BUFFER_SIZE = 16384L;
            final long BUFFER_SIZE = 1024L * 4;
            final byte[] buff = new byte[(int) BUFFER_SIZE];
            final boolean sendEverything = pending == -1L;
            while (pending > 0L || sendEverything) {
                final long bytesToRead = sendEverything ? BUFFER_SIZE : Math.min(pending, BUFFER_SIZE);
                int read = this.data.read(buff, 0, (int) bytesToRead);
                if (read <= 0) {
                    break;
                }
                //byte[] send = new byte[read];
                //System.arraycopy(buff, 0, send, 0, read);
                //Timber.e(TAG, "sendBody   send:" + new String(send));
                //Timber.e(TAG, "sendBody   send:" + ByteUtils.bytes2hex(send));
                //Timber.e(TAG, "sendBody   send:" + Arrays.toString(send));
                outputStream.write(buff, 0, read);
                outputStream.flush();
                if (sendEverything) {
                    continue;
                }
                pending -= read;
            }
        }

        public void setChunkedTransfer(final boolean chunkedTransfer) {
            this.chunkedTransfer = chunkedTransfer;
        }

        public void setData(final InputStream data) {
            this.data = data;
        }

        public void setMimeType(final String mimeType) {
            this.mimeType = mimeType;
        }

        public void setRequestMethod(final Method requestMethod) {
            this.requestMethod = requestMethod;
        }

        public void setStatus(final IStatus status) {
            this.status = status;
        }

        public enum Status implements IStatus {
            SWITCH_PROTOCOL(101, "Switching Protocols"),
            OK(200, "OK"),
            CREATED(201, "Created"),
            ACCEPTED(202, "Accepted"),
            NO_CONTENT(204, "No Content"),
            PARTIAL_CONTENT(206, "Partial Content"),
            MULTI_STATUS(207, "Multi-Status"),
            REDIRECT(301, "Moved Permanently"),
            @Deprecated
            FOUND(302, "Found"),
            REDIRECT_SEE_OTHER(303, "See Other"),
            NOT_MODIFIED(304, "Not Modified"),
            TEMPORARY_REDIRECT(307, "Temporary Redirect"),
            BAD_REQUEST(400, "Bad Request"),
            UNAUTHORIZED(401, "Unauthorized"),
            FORBIDDEN(403, "Forbidden"),
            NOT_FOUND(404, "Not Found"),
            METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
            NOT_ACCEPTABLE(406, "Not Acceptable"),
            REQUEST_TIMEOUT(408, "Request Timeout"),
            CONFLICT(409, "Conflict"),
            GONE(410, "Gone"),
            LENGTH_REQUIRED(411, "Length Required"),
            PRECONDITION_FAILED(412, "Precondition Failed"),
            PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
            UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
            RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
            EXPECTATION_FAILED(417, "Expectation Failed"),
            TOO_MANY_REQUESTS(429, "Too Many Requests"),
            INTERNAL_ERROR(500, "Internal Server Error"),
            NOT_IMPLEMENTED(501, "Not Implemented"),
            SERVICE_UNAVAILABLE(503, "Service Unavailable"),
            UNSUPPORTED_HTTP_VERSION(505, "HTTP Version Not Supported");

            private final int requestStatus;
            private final String description;

            private Status(final int requestStatus, final String description) {
                this.requestStatus = requestStatus;
                this.description = description;
            }

            public static Status lookup(final int requestStatus) {
                for (final Status status : values()) {
                    if (status.getRequestStatus() == requestStatus) {
                        return status;
                    }
                }
                return null;
            }

            @Override
            public String getDescription() {
                return "" + this.requestStatus + " " + this.description;
            }

            @Override
            public int getRequestStatus() {
                return this.requestStatus;
            }
        }

        private static class ChunkedOutputStream extends FilterOutputStream {
            public ChunkedOutputStream(final OutputStream out) {
                super(out);
            }

            @Override
            public void write(final int b) throws IOException {
                final byte[] data = {(byte) b};
                this.write(data, 0, 1);
            }

            @Override
            public void write(final byte[] b) throws IOException {
                this.write(b, 0, b.length);
            }

            @Override
            public void write(final byte[] b, final int off, final int len) throws IOException {
                if (len == 0) {
                    return;
                }
                this.out.write(String.format("%x\r\n", len).getBytes());
                this.out.write(b, off, len);
                this.out.write("\r\n".getBytes());
            }

            public void finish() throws IOException {
                this.out.write("0\r\n\r\n".getBytes());
            }
        }

        public interface IStatus {
            String getDescription();

            int getRequestStatus();
        }
    }

    public static final class ResponseException extends Exception {
        private static final long serialVersionUID = 6569838532917408380L;
        private final Response.Status status;

        public ResponseException(final Response.Status status, final String message) {
            super(message);
            this.status = status;
        }

        public ResponseException(final Response.Status status, final String message, final Exception e) {
            super(message, e);
            this.status = status;
        }

        public Response.Status getStatus() {
            return this.status;
        }
    }

    public class ServerRunnable implements Runnable {
        private final int timeout;
        private IOException bindException;
        private boolean hasBinded;

        public ServerRunnable(final int timeout) {
            this.hasBinded = false;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            try {
                NanoHTTPD.this.myServerSocket.bind((NanoHTTPD.this.hostname != null) ? new InetSocketAddress(NanoHTTPD.this.hostname, NanoHTTPD.this.myPort) : new InetSocketAddress(NanoHTTPD.this.myPort));
                this.hasBinded = true;
            } catch (IOException e) {
                this.bindException = e;
                return;
            }
            do {
                try {
                    final Socket finalAccept = NanoHTTPD.this.myServerSocket.accept();
                    if (this.timeout > 0) {
                        finalAccept.setSoTimeout(this.timeout);
                    }
                    final InputStream inputStream = finalAccept.getInputStream();
                    NanoHTTPD.this.asyncRunner.exec(NanoHTTPD.this.createClientHandler(finalAccept, inputStream));
                } catch (IOException e) {
                    NanoHTTPD.LOG.log(Level.FINE, "Communication with the client broken", e);
                }
            } while (!NanoHTTPD.this.myServerSocket.isClosed());
        }
    }

    public interface ServerSocketFactory {
        ServerSocket create() throws IOException;
    }

    public interface TempFileManagerFactory {
        TempFileManager create();
    }

    public interface TempFileManager {
        void clear();

        TempFile createTempFile(final String p0) throws Exception;
    }

    public interface TempFile {
        void delete() throws Exception;

        String getName();

        OutputStream open() throws Exception;
    }

    public interface AsyncRunner {
        void closeAll();

        void closed(final ClientHandler p0);

        void exec(final ClientHandler p0);
    }

    public interface IHTTPSession {
        void execute() throws IOException;

        CookieHandler getCookies();

        Map<String, String> getHeaders();

        InputStream getInputStream();

        Method getMethod();

        @Deprecated
        Map<String, String> getParms();

        Map<String, List<String>> getParameters();

        String getQueryParameterString();

        String getUri();

        void parseBody(final Map<String, String> p0) throws IOException, ResponseException;

        String getRemoteIpAddress();

        String getRemoteHostName();
    }
}
