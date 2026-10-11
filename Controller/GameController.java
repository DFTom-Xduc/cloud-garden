package Controller;

import Service.*;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Local HTTP controller: native frontend files and a Java service API, on one origin. */
public final class GameController implements AutoCloseable {
    private final HttpServer server;
    private final GameService service;
    private final Path frontend;
    private final String token=UUID.randomUUID().toString();
    private final ExecutorService requests=Executors.newFixedThreadPool(4);
    private final ScheduledExecutorService clock=Executors.newSingleThreadScheduledExecutor();
    private long lastTick=System.nanoTime();
    public GameController(GameService service,Path frontend,int port) throws IOException {
        this.service=service;this.frontend=frontend.toAbsolutePath().normalize();
        server=HttpServer.create(new InetSocketAddress("127.0.0.1",port),0);
        server.createContext("/",this::handle);server.setExecutor(requests);
    }
    public int port(){return server.getAddress().getPort();}
    public void start(boolean animate) {
        server.start();
        if(animate)clock.scheduleAtFixedRate(()->{long now=System.nanoTime();double elapsed=(now-lastTick)/1e9;lastTick=now;try{service.tick(elapsed);}catch(RuntimeException e){System.err.println("Game tick failed: "+e.getMessage());}},1,1,TimeUnit.SECONDS);
    }
    private void handle(HttpExchange exchange) throws IOException {
        try {
            String host=exchange.getRequestHeaders().getFirst("Host");
            if(host==null||!Set.of("localhost:"+port(),"127.0.0.1:"+port()).contains(host)){send(exchange,403,Json.object("error","Invalid host"));return;}
            String path=exchange.getRequestURI().getPath(),method=exchange.getRequestMethod();
            if(path.equals("/api/state")&&method.equals("GET")){send(exchange,200,Json.object("state",service.state(),"token",token));return;}
            if(path.equals("/api/action")&&method.equals("POST")) {
                String origin=exchange.getRequestHeaders().getFirst("Origin");
                if(!token.equals(exchange.getRequestHeaders().getFirst("X-Game-Token"))||origin!=null&&!Set.of("http://localhost:"+port(),"http://127.0.0.1:"+port()).contains(origin)){send(exchange,403,Json.object("error","Reload the game before making changes"));return;}
                if(!Optional.ofNullable(exchange.getRequestHeaders().getFirst("Content-Type")).orElse("").startsWith("application/x-www-form-urlencoded")){send(exchange,415,Json.object("error","Expected a form body"));return;}
                byte[] body=exchange.getRequestBody().readNBytes(8193);if(body.length>8192){send(exchange,413,Json.object("error","Request is too large"));return;}
                Map<String,String> input=new HashMap<>();
                for(String pair:new String(body,StandardCharsets.UTF_8).split("&")){String[] parts=pair.split("=",2);if(parts.length==2)input.put(URLDecoder.decode(parts[0],StandardCharsets.UTF_8),URLDecoder.decode(parts[1],StandardCharsets.UTF_8));}
                send(exchange,200,service.act(input));return;
            }
            if(path.startsWith("/api/")){send(exchange,404,Json.object("error","Unknown endpoint"));return;}
            if(!method.equals("GET")&&!method.equals("HEAD")){send(exchange,405,Json.object("error","Method not allowed"));return;}
            Path file=frontend.resolve(path.equals("/")?"index.html":path.substring(1)).normalize();
            if(!file.startsWith(frontend)||!Files.isRegularFile(file)){send(exchange,404,Json.object("error","File not found"));return;}
            String name=file.getFileName().toString();String type=name.endsWith(".html")?"text/html; charset=utf-8":name.endsWith(".css")?"text/css; charset=utf-8":name.endsWith(".js")?"text/javascript; charset=utf-8":name.endsWith(".svg")?"image/svg+xml":name.endsWith(".png")?"image/png":name.endsWith(".webp")?"image/webp":"application/octet-stream";
            exchange.getResponseHeaders().set("Content-Type",type);exchange.getResponseHeaders().set("X-Content-Type-Options","nosniff");
            exchange.getResponseHeaders().set("Content-Security-Policy","default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self'; object-src 'none'");
            exchange.getResponseHeaders().set("Cache-Control","no-cache");byte[] bytes=Files.readAllBytes(file);
            exchange.sendResponseHeaders(200,method.equals("HEAD")?-1:bytes.length);if(!method.equals("HEAD"))exchange.getResponseBody().write(bytes);
        } catch(GameService.Conflict e){send(exchange,409,Json.object("error",e.getMessage(),"state",service.state()));}
        catch(IllegalArgumentException e){send(exchange,400,Json.object("error",e.getMessage(),"state",service.state()));}
        catch(Exception e){e.printStackTrace();send(exchange,500,Json.object("error","Unable to save this action. Please try again."));}
        finally{exchange.close();}
    }
    private void send(HttpExchange exchange,int status,Map<String,Object> value) throws IOException {
        byte[] bytes=Json.write(value).getBytes(StandardCharsets.UTF_8);exchange.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");exchange.getResponseHeaders().set("Cache-Control","no-store");exchange.getResponseHeaders().set("X-Content-Type-Options","nosniff");exchange.sendResponseHeaders(status,bytes.length);exchange.getResponseBody().write(bytes);
    }
    @Override public void close(){clock.shutdownNow();server.stop(0);requests.shutdownNow();service.save();}
}
