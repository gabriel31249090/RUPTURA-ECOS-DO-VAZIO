package ruptura.story;

/** Expressões referenciam poses do retrato, sem depender do renderizador. */
public record DialogueLine(String speaker, String text, String expression) { }
