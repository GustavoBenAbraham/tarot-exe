package com.tarotexe;

import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import com.tarotexe.domain.*;
import com.tarotexe.service.*;
import org.junit.jupiter.api.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

class OllamaServiceTest {
    private HttpServer server;
    private ExecutorService executor;
    private OllamaService service;
    private final AtomicReference<String> response = new AtomicReference<>("{\"message\":{\"content\":\"Reflexão em português.\"},\"done_reason\":\"stop\"}");
    private final AtomicReference<String> request = new AtomicReference<>();
    private final AtomicInteger status = new AtomicInteger(200);
    private final AtomicLong delay = new AtomicLong();
    private Reading reading() {
        return new Reading("Como agir com atenção?", SpreadType.ONE,
                List.of(new DrawnCard(TarotDeck.standard().get(8), true, "Orientação")), LocalDateTime.now(), ReadingTheme.WORK);
    }
    @BeforeEach void setup() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        executor = Executors.newCachedThreadPool(r -> { var t = new Thread(r); t.setDaemon(true); return t; });
        server.setExecutor(executor);
        server.createContext("/api/chat", exchange -> {
            try {
                request.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                if (delay.get() > 0) Thread.sleep(delay.get());
                byte[] bytes = response.get().getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(status.get(), bytes.length); exchange.getResponseBody().write(bytes);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { exchange.close(); }
        });
        server.start(); service = new OllamaService(URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/api/chat"));
    }
    @AfterEach void cleanup() { server.stop(0); executor.shutdownNow(); }

    @Test void sendsRealCardContextAndChosenParametersWithUtf8() throws Exception {
        var settings = new AiSettings("qwen2.5:3b", 60, 384, 2048, 0.2);
        assertEquals("Reflexão em português.", service.deepen(reading(), settings).get(5, TimeUnit.SECONDS));
        var json = JsonParser.parseString(request.get()).getAsJsonObject();
        assertEquals(settings.model(), json.get("model").getAsString());
        assertFalse(json.get("stream").getAsBoolean());
        var options = json.getAsJsonObject("options");
        assertEquals(384, options.get("num_predict").getAsInt()); assertEquals(2048, options.get("num_ctx").getAsInt());
        assertEquals(0.2, options.get("temperature").getAsDouble());
        String content = json.getAsJsonArray("messages").get(1).getAsJsonObject().get("content").getAsString();
        assertTrue(content.contains("Como agir com atenção?"));
        assertTrue(content.contains("A Força (Invertida)"));
        assertTrue(content.contains(new LocalCardCatalog().detail(8).reversed()));
        assertFalse(content.contains(new LocalCardCatalog().detail(8).upright()));
    }
    @Test void missingModelIsReported() {
        status.set(404);
        var exception = assertThrows(ExecutionException.class, () -> service.deepen(reading(), AiSettings.defaults()).get(5, TimeUnit.SECONDS));
        assertTrue(exception.getCause().getMessage().contains("Modelo não encontrado"));
    }
    @Test void rejectsMalformedEmptyAndTruncatedResponses() {
        for (String body : List.of("not-json", "{}", "{\"message\":{\"content\":\" \"}}",
                "{\"message\":{\"content\":\"Cortada\"},\"done_reason\":\"length\"}",
                "{\"message\":{\"content\":\"<think>internal\"}}")) {
            response.set(body);
            assertThrows(ExecutionException.class, () -> service.deepen(reading(), AiSettings.defaults()).get(5, TimeUnit.SECONDS));
        }
    }
    @Test void requestHonorsTimeout() {
        delay.set(8000);
        var exception = assertThrows(ExecutionException.class, () -> service.deepen(reading(),
                new AiSettings("qwen2.5:3b", 5, 256, 2048, 0.3)).get(7, TimeUnit.SECONDS));
        assertInstanceOf(java.net.http.HttpTimeoutException.class, exception.getCause());
    }
    @Test void canCancelPendingRequest() {
        delay.set(8000);
        var pending = service.deepen(reading(), AiSettings.defaults());
        assertTrue(pending.cancel(true)); assertTrue(pending.isCancelled());
    }
    @Test void rejectsRemoteEndpoint() {
        assertThrows(IllegalArgumentException.class, () -> new OllamaService(URI.create("https://example.com/api/chat")));
    }
}
