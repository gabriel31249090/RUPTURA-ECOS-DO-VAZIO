package ruptura.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import ruptura.RupturaGame;
import ruptura.ui.Ui;

public abstract class BaseScreen extends ScreenAdapter {
    protected final RupturaGame game;
    protected final Ui ui;
    protected final FitViewport viewport = new FitViewport(1280,800);
    protected float elapsed;
    protected BaseScreen(RupturaGame game) { this.game=game; this.ui=game.ui; }
    @Override public final void render(float delta) {
        elapsed += Math.min(delta,.05f); ScreenUtils.clear(Ui.INK);
        viewport.apply(); ui.begin(viewport); draw(Math.min(delta,.05f)); ui.end();
    }
    protected abstract void draw(float delta);
    @Override public void resize(int width,int height) { viewport.update(width,height,true); }
    protected void footer(String left,String right) {
        ui.line(36,48,1244,48,Ui.LINE); ui.text(ui.mono,left,36,31,Ui.MUTED); ui.right(ui.mono,right,1244,31,Ui.MUTED);
    }
}
