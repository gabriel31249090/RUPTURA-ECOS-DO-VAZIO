package ruptura.save;

import ruptura.story.GameSession;
import ruptura.world.WorldMap;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Optional;
import java.util.Properties;

/** Slot local versionado com validação, escrita temporária e cópia anterior. */
public final class SaveSystem {
    public static final int FORMAT_VERSION = 1;
    private static final long MAX_BYTES = 1_048_576;
    private final Path directory;
    private String lastError = "";

    public SaveSystem(Path directory) { this.directory = directory.toAbsolutePath().normalize(); }
    public Path directory() { return directory; }
    public boolean exists() { return Files.isRegularFile(slot()) || Files.isRegularFile(backup()); }
    public String lastError() { return lastError; }
    private Path slot() { return directory.resolve("slot-1.properties"); }
    private Path backup() { return directory.resolve("slot-1.backup.properties"); }

    public void save(GameSession session) {
        try {
            validate(session);
            Properties p = encode(session);
            Files.createDirectories(directory);
            // Não substituir um backup válido por um slot externo corrompido.
            if (Files.isRegularFile(slot())) {
                try {
                    decode(read(slot()));
                    Files.copy(slot(), backup(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException | IllegalArgumentException ignored) { /* mantém backup anterior */ }
            }
            writeAtomically(slot(), p, "RUPTURA — progresso local");
            lastError = "";
        } catch (IOException | IllegalArgumentException ex) {
            lastError = "Não foi possível salvar: " + ex.getMessage();
            throw new IllegalStateException(lastError, ex);
        }
    }

    public Optional<GameSession> load() {
        lastError = "";
        if (!exists()) return Optional.empty();
        try {
            return Optional.of(decode(read(slot())));
        } catch (IOException | IllegalArgumentException primary) {
            if (Files.isRegularFile(backup())) {
                try {
                    GameSession session = decode(read(backup()));
                    lastError = "O salvamento principal está inválido; a cópia anterior foi recuperada.";
                    return Optional.of(session);
                } catch (IOException | IllegalArgumentException ignored) { /* informa falha original */ }
            }
            lastError = "Não foi possível carregar: " + primary.getMessage();
            return Optional.empty();
        }
    }

    public GameSettings loadSettings() {
        Path path = directory.resolve("settings.properties");
        if (!Files.isRegularFile(path)) return new GameSettings();
        try {
            Properties p = read(path);
            if (integer(p, "version") != FORMAT_VERSION) throw new IllegalArgumentException("Versão de configuração incompatível");
            GameSettings s = new GameSettings();
            s.reducedMotion = bool(p, "reducedMotion");
            s.fullscreen = bool(p, "fullscreen");
            s.volume = decimal(p, "volume");
            s.textSpeed = decimal(p, "textSpeed");
            s.difficultyIndex = integer(p, "difficulty");
            s.width = integer(p, "width");
            s.height = integer(p, "height");
            s.validate();
            return s;
        } catch (IOException | IllegalArgumentException ex) {
            lastError = "Configurações inválidas; padrões restaurados.";
            return new GameSettings();
        }
    }

    public void saveSettings(GameSettings settings) {
        try {
            settings.validate();
            Properties p = new Properties();
            p.setProperty("version", "" + FORMAT_VERSION);
            p.setProperty("reducedMotion", "" + settings.reducedMotion);
            p.setProperty("fullscreen", "" + settings.fullscreen);
            p.setProperty("volume", "" + settings.volume);
            p.setProperty("textSpeed", "" + settings.textSpeed);
            p.setProperty("difficulty", "" + settings.difficultyIndex);
            p.setProperty("width", "" + settings.width);
            p.setProperty("height", "" + settings.height);
            Files.createDirectories(directory);
            writeAtomically(directory.resolve("settings.properties"), p, "RUPTURA — configurações");
            lastError = "";
        } catch (IOException | IllegalArgumentException ex) {
            lastError = "Não foi possível salvar configurações: " + ex.getMessage();
            throw new IllegalStateException(lastError, ex);
        }
    }

    private static Properties encode(GameSession s) {
        Properties p = new Properties();
        p.setProperty("version", "" + FORMAT_VERSION);
        p.setProperty("savedAt", Instant.now().toString());
        p.setProperty("heroName", s.heroName);
        p.setProperty("specialization", s.specialization);
        p.setProperty("pronouns", s.pronouns);
        p.setProperty("origin", s.origin);
        p.setProperty("skin", "" + s.skin);
        p.setProperty("hair", "" + s.hair);
        p.setProperty("outfit", "" + s.outfit);
        p.setProperty("accessory", "" + s.accessory);
        p.setProperty("difficulty", "" + s.difficultyIndex);
        p.setProperty("questStage", "" + s.questStage);
        p.setProperty("affinity", "" + s.affinity);
        p.setProperty("medkits", "" + s.medkits);
        p.setProperty("scrap", "" + s.scrap);
        p.setProperty("level", "" + s.level);
        p.setProperty("xp", "" + s.xp);
        p.setProperty("seed", "" + s.seed);
        p.setProperty("playerX", "" + s.playerX);
        p.setProperty("playerY", "" + s.playerY);
        p.setProperty("crateOpened", "" + s.crateOpened);
        p.setProperty("firstBattleWon", "" + s.firstBattleWon);
        p.setProperty("bossWon", "" + s.bossWon);
        p.setProperty("survivorHelped", "" + s.survivorHelped);
        p.setProperty("completed", "" + s.completed);
        p.setProperty("choice.count", "" + s.choices.size());
        int i = 0;
        for (var choice : s.choices.entrySet()) {
            p.setProperty("choice." + i + ".key", choice.getKey());
            p.setProperty("choice." + i + ".value", choice.getValue());
            i++;
        }
        p.setProperty("history.count", "" + s.dialogueHistory.size());
        for (i = 0; i < s.dialogueHistory.size(); i++) p.setProperty("history." + i, s.dialogueHistory.get(i));
        return p;
    }

    private static GameSession decode(Properties p) {
        if (integer(p, "version") != FORMAT_VERSION)
            throw new IllegalArgumentException("Versão de salvamento incompatível");
        GameSession s = new GameSession();
        s.heroName = required(p,"heroName");
        s.specialization = required(p,"specialization");
        s.pronouns = required(p,"pronouns");
        s.origin = required(p,"origin");
        s.skin = integer(p,"skin");
        s.hair = integer(p,"hair");
        s.outfit = integer(p,"outfit");
        s.accessory = integer(p,"accessory");
        s.difficultyIndex = integer(p,"difficulty");
        s.questStage = integer(p,"questStage");
        s.affinity = integer(p,"affinity");
        s.medkits = integer(p,"medkits");
        s.scrap = integer(p,"scrap");
        s.level = integer(p,"level");
        s.xp = integer(p,"xp");
        s.seed = Long.parseLong(required(p,"seed"));
        s.playerX = decimal(p,"playerX");
        s.playerY = decimal(p,"playerY");
        s.crateOpened = bool(p,"crateOpened");
        s.firstBattleWon = bool(p,"firstBattleWon");
        s.bossWon = bool(p,"bossWon");
        s.survivorHelped = bool(p,"survivorHelped");
        s.completed = bool(p,"completed");
        int choices = bounded(integer(p,"choice.count"), 0, 1000, "Quantidade de escolhas");
        for (int i = 0; i < choices; i++) s.choices.put(required(p,"choice." + i + ".key"), required(p,"choice." + i + ".value"));
        int history = bounded(integer(p,"history.count"), 0, 10000, "Quantidade de falas");
        for (int i = 0; i < history; i++) s.dialogueHistory.add(required(p,"history." + i));
        validate(s);
        return s;
    }

    private static void validate(GameSession s) {
        if (s == null) throw new IllegalArgumentException("Progresso ausente");
        if (s.heroName == null || s.heroName.isBlank() || s.heroName.length() > 28)
            throw new IllegalArgumentException("Nome inválido");
        if (s.specialization == null || s.pronouns == null || s.origin == null)
            throw new IllegalArgumentException("Perfil incompleto");
        bounded(s.skin,0,20,"Tom de pele"); bounded(s.hair,0,20,"Cabelo");
        bounded(s.outfit,0,20,"Roupa"); bounded(s.accessory,0,20,"Acessório");
        bounded(s.difficultyIndex,0,3,"Dificuldade"); bounded(s.questStage,0,5,"Etapa da missão");
        bounded(s.affinity,-1000,1000,"Afinidade"); bounded(s.medkits,0,999,"Estojos");
        bounded(s.scrap,0,999999,"Sucata"); bounded(s.level,1,10000,"Nível"); bounded(s.xp,0,999999,"Experiência");
        if (!Float.isFinite(s.playerX) || !Float.isFinite(s.playerY)
                || s.playerX < 0 || s.playerX >= WorldMap.WIDTH || s.playerY < 0 || s.playerY >= WorldMap.HEIGHT)
            throw new IllegalArgumentException("Posição inválida");
        if (s.completed && !s.bossWon || s.bossWon && !s.firstBattleWon)
            throw new IllegalArgumentException("Progresso narrativo inconsistente");
        if (s.choices.size() > 1000 || s.dialogueHistory.size() > 10000)
            throw new IllegalArgumentException("Histórico excedeu o limite do formato");
    }

    private static Properties read(Path path) throws IOException {
        if (Files.size(path) > MAX_BYTES) throw new IOException("Arquivo excede o limite permitido");
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { p.load(reader); }
        return p;
    }
    private static void writeAtomically(Path target, Properties properties, String comment) throws IOException {
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) { properties.store(writer, comment); }
            try (FileChannel channel = FileChannel.open(temp, StandardOpenOption.WRITE)) { channel.force(true); }
            try { Files.move(temp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }
    private static String required(Properties p,String key) {
        String value = p.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Campo ausente: " + key);
        return value;
    }
    private static int integer(Properties p,String key) { return Integer.parseInt(required(p,key)); }
    private static float decimal(Properties p,String key) { return Float.parseFloat(required(p,key)); }
    private static boolean bool(Properties p,String key) {
        String value = required(p,key);
        if (!"true".equals(value) && !"false".equals(value)) throw new IllegalArgumentException("Valor lógico inválido: " + key);
        return Boolean.parseBoolean(value);
    }
    private static int bounded(int value,int min,int max,String field) {
        if (value < min || value > max) throw new IllegalArgumentException(field + " fora dos limites");
        return value;
    }
}
