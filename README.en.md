# Tarot.exe

[Português](README.md) | **English**

A Java/JavaFX desktop application with 78 illustrated tarot cards, card draws and readings composed entirely from local text and rules. AI integration has been removed.

The current priority is to explore the mathematics of card draws, followed by visual design and reading text.

## Card draws

- One or three cards, without replacement within a spread.
- Shuffling uses SecureRandom; each new spread starts with all 78 cards.
- Reversals are always enabled: each card independently has a 50% chance of appearing upright or reversed.
- The question, theme and history do not influence the draw.
- New spreads use a general reading, without a theme selector or a control to disable reversals.

Probability studies remain in the code and documentation. Interpretations use local catalogs and rules.

## Desktop features

Local card images with zoom, saved history, UTF-8 export, copying readings and probabilities, font sizing and window preferences. Older histories remain readable: legacy AI fields are ignored, and opening a file preserves the original.

A late-1990s-style shuffle/deal/reveal animation presents the result already drawn. Skipping the animation does not change the cards. A new spread cancels the previous animation.

An optional personal introduction accepts a name or nickname, birth date and location. The name is used in the reading, and the date identifies an approximate zodiac sign. Data remains local and is included in history only when saved. An ascendant is not calculated.

## Development

Requires JDK 21. On Windows:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd javafx:run
```

On Linux/macOS, use `./mvnw` for the equivalent commands. UI tests require a graphical session. Older binaries in `dist` and previous releases need rebuilding to include current changes.

## Learning context and documentation

The project provides practice with JavaFX, local persistence, automated tests and random sampling/probability. Supporting documents are currently in Portuguese:

- [Probability formulas](docs/PROBABILIDADES.md)
- [Learning notes](docs/APRENDIZADOS.md)
- [Current priorities](PENDENCIAS.md)
- [Installation](INSTALACAO.md)
- [Consultation structure](docs/CONSULTA.md)
- [Reading reference map](docs/REFERENCIA-LEITURAS.md)

Interpretations were revised using a user-provided copy of Rachel Pollack's *Seventy-Eight Degrees of Wisdom*. The reference map records pages, adaptation criteria and limitations; responses still use local text and rules.

Illustrations: Pamela Colman Smith, Rider–Waite–Smith (1909). See [attribution](src/main/resources/cards/ATTRIBUTION.md).
