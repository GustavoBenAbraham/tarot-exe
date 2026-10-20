package com.tarotexe.service;

import com.tarotexe.domain.Reading;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface AiInterpreter {
    CompletableFuture<String> deepen(Reading reading, AiSettings settings);
}
