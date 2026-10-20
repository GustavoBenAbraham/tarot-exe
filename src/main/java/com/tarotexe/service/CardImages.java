package com.tarotexe.service;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.net.URL;
import java.util.function.IntFunction;

/** Recursos incluídos no aplicativo; nenhuma consulta à rede durante o uso. */
public final class CardImages {
    private final Map<Integer, Optional<Image>> cache = new HashMap<>();
    private final IntFunction<URL> resources;
    public CardImages() { this(id -> CardImages.class.getResource("/cards/%02d.jpg".formatted(id))); }
    public CardImages(IntFunction<URL> resources) { this.resources = java.util.Objects.requireNonNull(resources); }

    public ImageView view(int id, boolean reversed) {
        if (id < 0 || id > 77) throw new IllegalArgumentException("ID de carta inválido");
        var image = cache.computeIfAbsent(id, key -> {
            var url = resources.apply(key);
            if (url == null) return Optional.empty();
            try {
                var loaded = new Image(url.toExternalForm(), 280, 460, true, true, false);
                return loaded.isError() ? Optional.empty() : Optional.of(loaded);
            } catch (IllegalArgumentException e) { return Optional.empty(); }
        });
        var view = new ImageView(image.orElse(null));
        view.setFitWidth(180); view.setFitHeight(300); view.setPreserveRatio(true);
        view.setRotate(reversed ? 180 : 0);
        return view;
    }
}
