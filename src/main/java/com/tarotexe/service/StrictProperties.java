package com.tarotexe.service;

import java.io.*;
import java.util.Properties;

/** Properties não pode sobrescrever silenciosamente uma carta já cadastrada. */
final class StrictProperties extends Properties {
    @Override public synchronized Object put(Object key, Object value) {
        if (containsKey(key)) throw new IllegalArgumentException("Chave duplicada no catálogo: " + key);
        return super.put(key, value);
    }
    static Properties read(Reader reader) throws IOException {
        var properties = new StrictProperties(); properties.load(reader); return properties;
    }
}
