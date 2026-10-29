package ruptura.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Small immediate-mode UI. Clicks are dispatched after drawing to keep screen changes safe. */
public final class Ui implements Disposable {
    public static final Color INK = Color.valueOf("080f18"), PANEL = Color.valueOf("101d29");
    public static final Color LINE = Color.valueOf("29404a"), TEXT = Color.valueOf("e5ece6");
    public static final Color MUTED = Color.valueOf("8b9eaa"), CYAN = Color.valueOf("72e5c2");
    public static final Color ORANGE = Color.valueOf("fa9563"), RED = Color.valueOf("ed7475");
    public final ShapeRenderer shapes = new ShapeRenderer();
    public final SpriteBatch batch = new SpriteBatch();
    public final BitmapFont title, heading, body, small, mono;
    private final Vector2 mouse = new Vector2();
    private final GlyphLayout layout = new GlyphLayout();
    private boolean clicked;
    private Runnable pending;
    private Runnable clickSound=()->{};

    public Ui() {
        title = font("BarlowCondensed-Bold.ttf", 112);
        heading = font("BarlowCondensed-Bold.ttf", 40);
        body = font("Barlow-Regular.ttf", 23);
        small = font("Barlow-Regular.ttf", 18);
        mono = font("IBMPlexMono-Regular.ttf", 13);
        shapes.setAutoShapeType(true);
    }
    private BitmapFont font(String path, int size) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/" + path));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = size;
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ÁÀÂÃÄÉÈÊËÍÌÎÏÓÒÔÕÖÚÙÛÜÇáàâãäéèêëíìîïóòôõöúùûüç–—…•→←↑↓▸×ºª✓−";
        BitmapFont font = generator.generateFont(p);
        generator.dispose();
        return font;
    }
    public void begin(Viewport viewport) {
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        mouse.set(Gdx.input.getX(), Gdx.input.getY()); viewport.unproject(mouse);
        clicked = Gdx.input.justTouched(); pending = null;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
    public void end() { if (pending != null) { Runnable action = pending; pending = null; action.run(); } }
    /** Drop clicks captured behind an overlay and offer the original click to its own controls. */
    public void modal() { pending=null; clicked=Gdx.input.justTouched(); }
    public void setClickSound(Runnable sound){clickSound=sound;}
    public float mouseX() { return mouse.x; }
    public float mouseY() { return mouse.y; }
    public boolean hit(float x, float y, float w, float h) {
        return mouse.x >= x && mouse.x <= x + w && mouse.y >= y && mouse.y <= y + h;
    }
    public void rect(float x,float y,float w,float h,Color color) {
        shapes.begin(ShapeRenderer.ShapeType.Filled); shapes.setColor(color); shapes.rect(x,y,w,h); shapes.end();
    }
    public void line(float x,float y,float x2,float y2,Color color) {
        shapes.begin(ShapeRenderer.ShapeType.Filled); shapes.setColor(color); shapes.rectLine(x,y,x2,y2,1); shapes.end();
    }
    public void panel(float x,float y,float w,float h) {
        rect(x,y,w,h,PANEL); line(x,y+h,x+w,y+h,LINE);
    }
    public void text(BitmapFont font,String text,float x,float top,Color color) {
        batch.begin(); font.setColor(color); font.draw(batch,text,x,top); batch.end();
    }
    public void wrap(BitmapFont font,String text,float x,float top,float width,Color color) {
        batch.begin(); font.setColor(color); font.draw(batch,text,x,top,width,Align.left,true); batch.end();
    }
    public void center(BitmapFont font,String text,float x,float top,float width,Color color) {
        batch.begin(); font.setColor(color); font.draw(batch,text,x,top,width,Align.center,false); batch.end();
    }
    public void right(BitmapFont font,String text,float right,float top,Color color) {
        layout.setText(font,text); text(font,text,right-layout.width,top,color);
    }
    public void button(String label,float x,float y,float w,float h,boolean primary,Runnable action) {
        button(label,x,y,w,h,primary,true,action);
    }
    public void button(String label,float x,float y,float w,float h,boolean primary,boolean enabled,Runnable action) {
        boolean hover = enabled && hit(x,y,w,h);
        Color background = primary ? (hover?TEXT:CYAN) : (hover?LINE:PANEL);
        if (!enabled) background = INK;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(background); shapes.rect(x,y,w-10,h); shapes.triangle(x+w-10,y,x+w,y+10,x+w-10,y+h);
        shapes.end();
        text(small,label,x+18,y+h/2+8,enabled?(primary?INK:TEXT):MUTED);
        if (enabled && clicked && hover && pending==null) { clicked=false; pending=()->{clickSound.run();action.run();}; }
    }
    public void bar(float x,float y,float w,float ratio,Color color) {
        rect(x,y,w,4,LINE); rect(x,y,w*Math.max(0,Math.min(1,ratio)),4,color);
    }
    public void shade(float alpha) { rect(0,0,1280,800,new Color(0.015f,0.03f,0.05f,alpha)); }
    public void frameTitle(String eyebrow,String title,String right) {
        text(mono,eyebrow,36,764,CYAN); text(heading,title,36,736,TEXT);
        right(mono,right,1244,764,MUTED); line(36,682,1244,682,LINE);
    }
    @Override public void dispose() {
        shapes.dispose(); batch.dispose(); title.dispose(); heading.dispose(); body.dispose(); small.dispose(); mono.dispose();
    }
}
