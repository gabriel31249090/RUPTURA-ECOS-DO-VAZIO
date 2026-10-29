package ruptura;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import java.nio.file.Path;
import java.util.Arrays;
import ruptura.render.WorldRenderer;
import ruptura.save.GameSettings;
import ruptura.save.SaveSystem;
import ruptura.screens.AdventureScreen;
import ruptura.screens.CharacterScreen;
import ruptura.screens.MenuScreen;
import ruptura.story.GameSession;
import ruptura.ui.Ui;
import ruptura.audio.AudioSystem;
import ruptura.screens.PlaythroughVerification;

public final class RupturaGame extends Game {
    public Ui ui;
    public WorldRenderer worldRenderer;
    public SaveSystem saves;
    public GameSettings settings;
    public AudioSystem audio;
    public float time;
    private final String[] args;
    private int frames;
    private PlaythroughVerification verification;
    public RupturaGame(String[] args) { this.args=args; }
    @Override public void create() {
        boolean diagnostic=has("--smoke")||has("--capture")||has("--combat")||has("--explore")||has("--creator")||has("--intro");
        saves = new SaveSystem(has("--verify")?Path.of("build","verification",has("--supplies")?"supplies":"rescue"):
            diagnostic?Path.of("build","diagnostics"):Path.of(System.getProperty("user.home"),".ruptura"));
        settings = saves.loadSettings();
        ui = new Ui(); worldRenderer = new WorldRenderer(); audio = new AudioSystem();
        ui.setClickSound(()->audio.play("click",settings.volume));
        if (settings.fullscreen) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        if(has("--verify")) {
            settings.reducedMotion=true;settings.volume=0;
            AdventureScreen screen=new AdventureScreen(this,new GameSession(),true);open(screen);
            verification=new PlaythroughVerification(this,screen,!has("--supplies"));
        } else if (has("--combat")) {
            GameSession session = new GameSession();
            if(has("--boss"))session.rewardBattle(false);
            AdventureScreen screen = new AdventureScreen(this,session,false);
            open(screen); screen.startBattle(has("--boss"));
        } else if (has("--explore")) open(new AdventureScreen(this,new GameSession(),false));
        else if (has("--creator")) open(new CharacterScreen(this));
        else if (has("--intro")) open(new AdventureScreen(this,new GameSession(),true));
        else open(new MenuScreen(this));
    }
    private boolean has(String arg) { return Arrays.asList(args).contains(arg); }
    public void open(Screen next) {
        Screen previous = getScreen(); setScreen(next); if(previous!=null) previous.dispose();
    }
    @Override public void render() {
        time += Math.min(Gdx.graphics.getDeltaTime(),.05f);
        super.render();
        audio.update(Math.min(Gdx.graphics.getDeltaTime(),.05f),settings.volume,getScreen() instanceof AdventureScreen adventure && adventure.inCombat());
        if(verification!=null)verification.update();
        frames++;
        // Repeatable GL smoke/capture modes; no desktop automation or player save changes.
        if (has("--capture") && frames==75) {
            Pixmap pixmap = Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
            com.badlogic.gdx.graphics.PixmapIO.writePNG(Gdx.files.local("captures/"+(has("--combat")?"combat":has("--explore")?"explore":has("--creator")?"creator":has("--intro")?"intro":"menu")+".png"),pixmap, -1, true);
            pixmap.dispose();
        }
        if (has("--smoke") && frames>=90) Gdx.app.exit();
    }
    @Override public void dispose() {
        if(getScreen()!=null)getScreen().dispose();
        if(ui!=null)ui.dispose(); if(worldRenderer!=null)worldRenderer.dispose();
        if(audio!=null)audio.dispose();
    }
}
