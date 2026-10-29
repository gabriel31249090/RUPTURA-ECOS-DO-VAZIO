package ruptura.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import ruptura.ui.Ui;

/** Deterministic illustrated sky, a parallax skyline and a quiet dimensional tear. */
public final class Atmosphere {
    private Atmosphere() {}
    public static void draw(Ui ui,float time,boolean reduced) {
        ShapeRenderer s=ui.shapes;
        s.begin(ShapeRenderer.ShapeType.Filled);
        s.rect(0,0,1280,800,Color.valueOf("08101a"),Color.valueOf("08101a"),Color.valueOf("182d36"),Color.valueOf("101b29"));
        for(int i=0;i<65;i++) {
            float x=(i*197+67)%1280, y=300+(i*137)%480;
            s.setColor(.40f,.68f,.64f,.12f+(i%4)*.08f); s.rect(x,y,i%3==0?2:1,1);
        }
        for(int i=0;i<26;i++) {
            float x=i*55-15,h=75+(i*71)%135;
            s.setColor(.035f,.07f,.1f,1); s.rect(x,210,47,h);
            if(i%3==0) {s.rect(x+8,210+h,9,15);s.rect(x+11,225+h,2,22);}
            s.setColor(.23f,.42f,.40f,.16f);
            for(int j=0;j<5;j++)if((j+i)%3==0)s.rect(x+8,230+j*24,4,7);
        }
        float pulse=reduced?0:(float)Math.sin(time*.7f)*6;
        for(int i=10;i>0;i--){s.setColor(.25f,.8f,.65f,.009f);s.ellipse(750-i*6,530-i*3,290+i*12,65+i*6,70);}
        s.setColor(.11f,.25f,.26f,1); s.triangle(748,558,924,600+pulse,1070,556);
        s.setColor(.42f,.88f,.73f,.9f); s.rectLine(748,558,919,584+pulse,1.5f); s.rectLine(919,584+pulse,1070,556,1.5f);
        s.setColor(.63f,1,.86f,.6f); s.rectLine(876,576,900,565,1); s.rectLine(900,565,944,575,1);
        for(int i=0;i<34;i++) {
            float x=(i*223+time*(reduced?0:4+i%4))%1280,y=(i*59+time*(reduced?0:8))%800;
            s.setColor(.5f,.88f,.75f,.12f+(i%3)*.05f);s.rect(x,y,1,2);
        }
        s.end();
    }
}
