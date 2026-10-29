package ruptura.story;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Leitor de roteiro externo UTF-8: falas, expressões, escolhas e condições simples. */
public final class DialogueScript {
    private static final Properties SCRIPT = load();
    private DialogueScript() { }

    private static Properties load() {
        Properties p = new Properties();
        try (InputStream stream = DialogueScript.class.getResourceAsStream("/data/dialogues/prologue.properties")) {
            if (stream == null) throw new IllegalStateException("Roteiro data/dialogues/prologue.properties não encontrado nos recursos");
            p.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return p;
        } catch (IOException ex) { throw new IllegalStateException("Falha ao ler o roteiro do prólogo", ex); }
    }

    public static List<DialogueLine> scene(String id, String heroName) {
        GameSession session = new GameSession();
        session.heroName = heroName;
        return scene(id, session);
    }

    public static List<DialogueLine> scene(String id, GameSession session) {
        int count = Integer.parseInt(SCRIPT.getProperty(id + ".count", "0"));
        if (count == 0) throw new IllegalArgumentException("Cena desconhecida: " + id);
        List<DialogueLine> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String prefix = id + "." + i + ".";
            if (!condition(SCRIPT.getProperty(prefix + "if", ""), session)) continue;
            result.add(new DialogueLine(resolve(required(prefix + "speaker"),session),
                    resolve(required(prefix + "text"),session), SCRIPT.getProperty(prefix + "expression", "neutral")));
        }
        return List.copyOf(result);
    }

    public static List<DialogueChoice> choices(String id) {
        int count = Integer.parseInt(SCRIPT.getProperty(id + ".choices", "0"));
        List<DialogueChoice> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String prefix = id + ".choice." + i + ".";
            result.add(new DialogueChoice(required(prefix + "text"), required(prefix + "key"),
                    required(prefix + "value"), Integer.parseInt(SCRIPT.getProperty(prefix + "affinity", "0"))));
        }
        return List.copyOf(result);
    }

    public static void applyChoice(GameSession session, DialogueChoice choice) {
        if (session.choices.containsKey(choice.key())) return;
        session.choose(choice.key(), choice.value());
        session.affinity += choice.affinityDelta();
        session.dialogueHistory.add(session.heroName + ": " + choice.text());
    }

    private static boolean condition(String expression, GameSession session) {
        if (expression.isBlank()) return true;
        if ("affinity>=2".equals(expression)) return session.affinity >= 2;
        if ("affinity<2".equals(expression)) return session.affinity < 2;
        String[] pieces = expression.split("=",2);
        return pieces.length == 2 && pieces[1].equals(session.choices.get(pieces[0]));
    }
    private static String resolve(String text,GameSession s) {
        return text.replace("{hero}",s.heroName).replace("{origin}",s.origin).replace("{specialization}",s.specialization);
    }
    private static String required(String key) {
        String value = SCRIPT.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Campo narrativo ausente: " + key);
        return value;
    }
}
