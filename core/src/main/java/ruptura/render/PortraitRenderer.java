package ruptura.render;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** Faceted original character portraits, drawn in a square at the supplied UI position. */
public final class PortraitRenderer {
    public void draw(ShapeRenderer s,float x,float y,float size,String kind,String expression) {
        float k=size/100f;
        String id=kind==null?"narrador":kind.toLowerCase(java.util.Locale.ROOT);
        boolean lia=id.contains("lia"), davi=id.contains("davi"), monster=id.contains("criatura")||id.contains("suss");
        boolean narrator=id.contains("narra")||id.contains("sinal")||id.contains("ambiente");
        c(s,0x0c202b); s.rect(x,y,size,size);
        c(s,lia?0x34504e:davi?0x473e33:0x214c51);
        tri(s,x,y+size*.17f,x+size,y+size*.8f,x+size,y);
        c(s,0x6ad6bb,.16f); s.rect(x+size*.06f,y+size*.06f,size*.88f,1);
        if(narrator) {
            c(s,0x3e8d84,.35f);
            tri(s,x+size*.25f,y+size*.2f,x+size*.5f,y+size*.86f,x+size*.5f,y+size*.16f);
            c(s,0x79dabe,.85f);
            tri(s,x+size*.5f,y+size*.86f,x+size*.72f,y+size*.36f,x+size*.5f,y+size*.16f);
            c(s,0xd0ffdf); tri(s,x+size*.5f,y+size*.86f,x+size*.53f,y+size*.36f,x+size*.5f,y+size*.16f);
            return;
        }
        // Wide coat and high collar form a deliberate, readable silhouette.
        c(s,lia?0x768d83:davi?0x8b7656:monster?0x4e6459:0x427078);
        quad(s,x+9*k,y,x+15*k,y+27*k,x+42*k,y+42*k,x+82*k,y+27*k);
        tri(s,x+9*k,y,x+82*k,y+27*k,x+96*k,y);
        c(s,lia?0xd0d4b8:davi?0xac946b:monster?0x657b63:0x5a9295);
        tri(s,x+17*k,y,x+28*k,y+31*k,x+46*k,y+25*k);
        quad(s,x+65*k,y+32*k,x+83*k,y+23*k,x+91*k,y,x+54*k,y);
        c(s,0x18323d); quad(s,x+34*k,y+33*k,x+49*k,y+15*k,x+62*k,y+33*k,x+56*k,y+6*k);
        c(s,lia?0xa5785e:davi?0x916345:monster?0x79816a:0xa67b5e); s.rect(x+42*k,y+29*k,17*k,17*k);
        c(s,lia?0xd6a586:davi?0xbb8c64:monster?0xabb38b:0xd8ad89);
        quad(s,x+31*k,y+73*k,x+66*k,y+75*k,x+69*k,y+47*k,x+53*k,y+35*k);
        tri(s,x+31*k,y+73*k,x+53*k,y+35*k,x+34*k,y+46*k);
        c(s,lia?0xaf7e65:davi?0x926f55:monster?0x687b64:0xb78968);
        quad(s,x+31*k,y+71*k,x+41*k,y+59*k,x+43*k,y+40*k,x+34*k,y+46*k);
        c(s,lia?0xe5b999:davi?0xd3a475:monster?0xc4c69e:0xe4bd9c);
        tri(s,x+50*k,y+65*k,x+56*k,y+53*k,x+49*k,y+52*k);
        c(s,lia?0x392a2a:davi?0x262e32:monster?0x32473d:0x1c303b);
        quad(s,x+27*k,y+69*k,x+29*k,y+84*k,x+48*k,y+92*k,x+71*k,y+81*k);
        quad(s,x+27*k,y+69*k,x+71*k,y+81*k,x+63*k,y+71*k,x+40*k,y+75*k);
        tri(s,x+27*k,y+69*k,x+40*k,y+75*k,x+32*k,y+53*k);
        if(lia) {
            c(s,0x49312b); quad(s,x+24*k,y+75*k,x+34*k,y+67*k,x+31*k,y+29*k,x+20*k,y+39*k);
            c(s,0x654135); tri(s,x+25*k,y+77*k,x+31*k,y+65*k,x+27*k,y+39*k);
            c(s,0x57372f); quad(s,x+63*k,y+77*k,x+72*k,y+78*k,x+76*k,y+32*k,x+67*k,y+39*k);
            c(s,0xc4cbb3); s.rect(x+73*k,y+8*k,12*k,10*k);
            c(s,0xaa5f49); s.rect(x+76*k,y+9*k,4*k,8*k); s.rect(x+74*k,y+11*k,8*k,4*k);
        }
        if(davi) {
            c(s,0x172e38); s.rect(x+33*k,y+55*k,15*k,9*k); s.rect(x+54*k,y+55*k,15*k,9*k); s.rect(x+47*k,y+59*k,8*k,2*k);
            c(s,0x78bcbb); s.rect(x+35*k,y+60*k,11*k,2*k); s.rect(x+56*k,y+60*k,11*k,2*k);
            c(s,0x2a3334); s.rect(x+69*k,y+47*k,4*k,18*k); s.rect(x+65*k,y+44*k,7*k,3*k);
        } else {
            boolean worried=expression!=null&&(expression.contains("tens")||expression.contains("preocup")||expression.contains("séria"));
            c(s,monster?0x99ffd4:0x283835);
            s.rect(x+37*k,y+59*k,7*k,2*k); s.rect(x+56*k,y+59*k,7*k,2*k);
            c(s,lia?0x624537:0x263a3f);
            if(worried) { s.rectLine(x+35*k,y+66*k,x+45*k,y+64*k,1.8f*k); s.rectLine(x+54*k,y+64*k,x+64*k,y+67*k,1.8f*k); }
            else { s.rect(x+35*k,y+65*k,10*k,2*k); s.rect(x+55*k,y+65*k,10*k,2*k); }
        }
        c(s,monster?0x344c40:0x8b5d4a); s.rect(x+48*k,y+44*k,12*k,1.5f*k);
        c(s,0x85c4b9,.55f); s.rect(x+80*k,y+63*k,2*k,23*k); s.rect(x+80*k,y+84*k,10*k,2*k);
    }
    private static void tri(ShapeRenderer s,float ax,float ay,float bx,float by,float cx,float cy) { s.triangle(ax,ay,bx,by,cx,cy); }
    private static void quad(ShapeRenderer s,float ax,float ay,float bx,float by,float cx,float cy,float dx,float dy) { tri(s,ax,ay,bx,by,cx,cy); tri(s,ax,ay,cx,cy,dx,dy); }
    private static void c(ShapeRenderer s,int rgb) { c(s,rgb,1); }
    private static void c(ShapeRenderer s,int rgb,float a) { s.setColor(((rgb>>16)&255)/255f,((rgb>>8)&255)/255f,(rgb&255)/255f,a); }
}
