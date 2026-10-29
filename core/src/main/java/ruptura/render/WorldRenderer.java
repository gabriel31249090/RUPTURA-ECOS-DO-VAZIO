package ruptura.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

/** Original, asset-free isometric art. World coordinates use a 64 by 32 diamond. */
public final class WorldRenderer implements Disposable {
    private final ShapeRenderer shapes = new ShapeRenderer(18000);
    private float time;
    private boolean reducedMotion;

    public WorldRenderer() { shapes.setAutoShapeType(true); }
    public static float isoX(float x, float y) { return (x - y) * 32f; }
    public static float isoY(float x, float y) { return (x + y) * 16f; }
    public void setProjection(Matrix4 projection) { shapes.setProjectionMatrix(projection); }

    public void begin(float time, boolean reducedMotion) {
        this.time = time;
        this.reducedMotion = reducedMotion;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
    }

    /** Types: 0 asphalt, 1 pavement, 2 building, 3 rubble, 4 walkable anomaly. */
    public void tile(int gx, int gy, int type, int elevation, boolean highlight, boolean danger) {
        float x = isoX(gx, gy), y = isoY(gx, gy), h = Math.max(0, elevation) * 7;
        int n = hash(gx, gy);
        c(0x0a111b, 0.85f); diamond(x + 2, y - 5, 32, 16);
        c(0x101d29); quad(x-32,y+h, x,y-16+h, x,y-22, x-32,y-6);
        c(0x1a2c39); quad(x,y-16+h, x+32,y+h, x+32,y-6, x,y-22);
        y += h;
        int base = type == 1 ? 0x344453 : type == 2 ? 0x293641 : type == 3 ? 0x28343d : type == 4 ? 0x193f43 : 0x26333d;
        c(base); diamond(x, y, 31.5f, 15.5f);
        c(0x425461, 0.32f); quad(x-30,y, x,y+15, x+30,y, x+27,y+1);
        c(0x14232e, 0.55f); tri(x-29,y-1,x,y-15,x+29,y-1);
        if (type == 1) {
            c(0x65717a, 0.22f);
            line(x-16,y-8,x+15,y+7,0.7f);
            line(x-16,y+8,x+15,y-7,0.7f);
            c(0x75827f,0.24f); line(x-29,y+0.5f,x,y+15,0.7f);
        } else if (type == 0) {
            c(0x788485, 0.13f);
            for (int k=0;k<3;k++) {
                float sx=x+((n >> (k*4)) & 15)-9, sy=y+((n >> (k*3+2)) & 7)-4;
                shapes.rect(sx,sy,2,1);
            }
            if (gx == 7 || gx == 8) {
                c(0xa69d77, 0.26f); quad(x-3,y-5,x+4,y-1,x+9,y-4,x+2,y-8);
            }
            if ((n & 7) == 1) {
                c(0x101e28,0.8f); line(x-17,y+4,x-6,y+1,1.1f); line(x-6,y+1,x+5,y+7,1.0f); line(x-6,y+1,x+1,y-6,0.8f);
            }
        } else if (type == 2) {
            block(x,y,30,15,18+(n&3)*5,0x3a4650,0x25323f,0x4b5960);
            c(0x111e28); shapes.rect(x-14,y+15,10,5);
            c(0x6e8986,0.4f); shapes.rect(x-12,y+16,3,3);
        } else if (type == 3) {
            for(int k=0;k<4;k++) {
                float rx=x-15+k*9, ry=y-3+((n >> (k+1))&7);
                block(rx,ry,5+(k%2)*3,3,4+(k%3)*3,0x586068,0x303c47,0x727576);
            }
            c(0xa96f49,0.5f); line(x-21,y+3,x+15,y-5,1.4f);
        } else if (type == 4) {
            float pulse=reducedMotion ? .6f : .45f+.16f*(float)Math.sin(time*1.8f+gx);
            c(0x28bfb4,pulse*.25f); diamond(x,y,28,13);
            c(0x62e3c8,pulse); line(x-23,y-1,x-5,y+3,1.2f); line(x-5,y+3,x+2,y-5,1.2f); line(x+2,y-5,x+19,y+1,1.2f);
            c(0x9df8d9,.65f); shapes.rect(x-6,y+1,2,2); shapes.rect(x+8,y-1,1,1);
        }
        if (highlight) {
            c(0x56d9ca,0.17f); diamond(x,y,30,14);
            c(0x70eddb,0.75f); diamondOutline(x,y,30,14,0.9f);
        }
        if (danger) {
            c(0xeb7459,0.17f); diamond(x,y,30,14);
            c(0xe77b60,0.72f); diamondOutline(x,y,28,13,1f);
        }
    }

    /** 1 ruined facade, 2 abandoned vehicle, 3 street light, 4 supplies, 5 radio, 6 crystal, 7 cover. */
    public void prop(int gx,int gy,int type,float time) {
        float x=isoX(gx,gy), y=isoY(gx,gy);
        switch(type) {
            case 1 -> building(x,y,hash(gx,gy));
            case 2 -> car(x,y);
            case 3 -> lamp(x,y,time);
            case 4 -> crate(x,y,time);
            case 5 -> radio(x,y,time);
            case 6 -> crystal(x,y,time);
            case 7 -> barricade(x,y);
            default -> { }
        }
    }

    private void building(float x,float y,int seed) {
        c(0x080e17,.5f); quad(x-15,y-3,x+67,y+12,x+27,y+33,x-40,y+11);
        float height=72+(seed&3)*9;
        // Broken roofline and two visible, differently lit facades.
        c(0x202e3b); quad(x-30,y,x,y-15,x,y+height-14,x-30,y+height);
        c(0x2e3d48); quad(x,y-15,x+30,y,x+30,y+height-8,x,y+height-14);
        c(0x57636a); quad(x-30,y+height,x-8,y+height+11,x+2,y+height+4,x,y+height-14);
        c(0x394c56); quad(x,y+height-14,x+2,y+height+4,x+30,y+height+15,x+30,y+height-8);
        c(0x172530); shapes.rect(x-26,y+height,4,14); shapes.rect(x+22,y+height+8,5,11);
        for(int floor=0;floor<3;floor++) {
            float wy=y+12+floor*22;
            c(0x101c29); quad(x-24,wy+8,x-8,wy,x-8,wy+12,x-24,wy+20);
            c(0x13222e); quad(x+7,wy-2,x+24,wy+6,x+24,wy+18,x+7,wy+10);
            if ((seed+floor)%3==0) {
                c(0xb88754,.7f); quad(x-22,wy+9,x-11,wy+3,x-11,wy+12,x-22,wy+17);
            } else {
                c(0x40616b,.5f); line(x-23,wy+10,x-10,wy+3,1);
            }
            c(0x60727b,.35f); line(x+7,wy+10,x+24,wy+18,1);
        }
        c(0x101b24); line(x+2,y+20,x+8,y+33,2); line(x+8,y+33,x+4,y+46,1.6f);
        c(0x82513e); quad(x-21,y+5,x-7,y-2,x-7,y+12,x-21,y+19);
        c(0xc3875c,.6f); line(x-20,y+12,x-9,y+5,2);
        block(x+25,y-9,9,5,6,0x58626a,0x2e3b46,0x6b7475);
        block(x-18,y-12,6,4,4,0x505e62,0x2b3740,0x727d78);
    }

    private void car(float x,float y) {
        c(0x071019,.7f); shapes.ellipse(x-32,y-11,65,22);
        c(0x0e1721); shapes.rect(x-22,y-4,7,9); shapes.rect(x+16,y+3,7,9);
        c(0x4c5657); quad(x-30,y+6,x+11,y-10,x+31,y+2,x-10,y+21);
        c(0x775e4b); quad(x-30,y+6,x+11,y-10,x+11,y+1,x-30,y+17);
        c(0x465658); quad(x+11,y-10,x+31,y+2,x+31,y+13,x+11,y+1);
        c(0x827d64); quad(x-28,y+17,x+10,y+2,x+28,y+13,x-10,y+30);
        c(0x40555d); quad(x-17,y+21,x-5,y+15,x+7,y+26,x-6,y+33);
        c(0x162e3d); quad(x-4,y+16,x+9,y+10,x+18,y+18,x+7,y+26);
        c(0x718782,.6f); line(x-1,y+19,x+7,y+24,1);
        c(0x233039); line(x-13,y+10,x-12,y+20,1.5f);
        c(0xd9b77a,.8f); shapes.rect(x-28,y+10,4,3); shapes.rect(x+24,y+10,4,3);
        c(0x9a6144); shapes.rect(x-16,y+7,8,2); shapes.rect(x+15,y+10,5,3);
    }

    private void lamp(float x,float y,float time) {
        c(0x050e18,.55f); quad(x-4,y-2,x+40,y+14,x+43,y+18,x,y+2);
        block(x,y,5,3,5,0x43565e,0x22313e,0x66706d);
        c(0x192b39); shapes.rect(x-2,y+3,3,76);
        c(0x58707b); shapes.rect(x+1,y+3,1,76);
        c(0x61787f); shapes.rect(x-2,y+77,21,3);
        c(0x344d59); shapes.rect(x+15,y+74,7,6);
        c(0x6ce7d3,.05f); shapes.circle(x+18,y+73,36,18);
        c(0x71ead7,.07f); shapes.circle(x+18,y+73,18,14);
        c(0xb8f2d8,.95f); shapes.rect(x+16,y+73,6,2);
        c(0x79debf,.04f); tri(x+17,y+72,x-14,y+4,x+48,y+4);
    }

    private void crate(float x,float y,float time) {
        c(0x071019,.55f); shapes.ellipse(x-16,y-9,34,14);
        block(x,y,15,8,16,0x607166,0x364a45,0x82907a);
        c(0x1b3434); quad(x-11,y+13,x-3,y+9,x-3,y+13,x-11,y+17);
        c(0xb6ceb1); shapes.rect(x+3,y+6,6,3); shapes.rect(x+5,y+4,2,7);
        c(0x85e8c3,.6f); shapes.rect(x-2,y+18,4,2);
    }

    private void radio(float x,float y,float time) {
        c(0x071019,.55f); shapes.ellipse(x-34,y-12,70,26);
        block(x,y,24,12,28,0x3f535a,0x223843,0x667779);
        c(0x102e37); quad(x-18,y+23,x-5,y+17,x-5,y+28,x-18,y+34);
        c(0x7af0cf); quad(x-16,y+24,x-7,y+20,x-7,y+26,x-16,y+31);
        c(0x46616b); shapes.rect(x+7,y+25,3,85);
        c(0x849b9d); line(x+8,y+105,x-7,y+27,1.8f); line(x+8,y+105,x+25,y+35,1.8f);
        for(int i=0;i<4;i++) { float yy=y+43+i*15; c(0x5f7980); line(x-3+i*2,yy,x+20-i*2,yy+3,1.5f); }
        c(0x9be7d0); shapes.rect(x+3,y+111,12,2); shapes.rect(x+8,y+104,2,16);
        c(0x66ead1,.06f); shapes.circle(x+9,y+113,20,18);
        c(0x9bffdb); shapes.rect(x+8,y+112,3,3);
        c(0xb19a6f); shapes.rect(x+15,y+12,3,10); shapes.rect(x+19,y+14,2,5);
    }

    private void crystal(float x,float y,float time) {
        float bob=reducedMotion ? 0 : (float)Math.sin(time*1.5+x)*2;
        c(0x32ceb5,.08f); shapes.ellipse(x-33,y-15,66,30);
        c(0x53e8cb,.13f); diamond(x,y,21,10);
        float h=y+18+bob;
        c(0x245c61); tri(x-9,h,x,h+32,x,h-8);
        c(0x69e9cc); tri(x,h+32,x+10,h+2,x,h-8);
        c(0xc0ffe5); tri(x,h+32,x+3,h+5,x,h-8);
        c(0x1f8a87); tri(x-9,h,x-3,h-7,x,h-8);
        c(0x8affe0,.75f); shapes.rect(x-16,y+22+bob,2,2); shapes.rect(x+16,y+13-bob,2,3);
    }

    private void barricade(float x,float y) {
        c(0x07101b,.65f); shapes.ellipse(x-30,y-12,61,20);
        for(int k=0;k<3;k++) block(x-18+k*17,y+(k-1)*7,10,6,12+(k%2)*4,0x62605a,0x404443,0x858176);
        c(0x242f35); line(x-26,y+17,x+27,y+37,3);
        c(0xc58a52); line(x-24,y+18,x-13,y+22,3); line(x-5,y+25,x+5,y+29,3); line(x+13,y+32,x+24,y+36,3);
    }

    public void actor(float gx,float gy,String kind,boolean ally,int skin,int hair,int outfit,float time,boolean selected,float hpRatio) {
        actor(gx,gy,kind,ally,skin,hair,outfit,0,time,selected,hpRatio);
    }

    public void actor(float gx,float gy,String kind,boolean ally,int skin,int hair,int outfit,int accessory,float time,boolean selected,float hpRatio) {
        float x=isoX(gx,gy), y=isoY(gx,gy);
        String type=kind == null ? "hero" : kind.toLowerCase(java.util.Locale.ROOT);
        boolean giant=type.contains("suss") || type.contains("boss");
        boolean runner=type.contains("corr");
        boolean crystal=type.contains("estil");
        boolean medic=type.contains("lia");
        float bob=reducedMotion?0:(float)Math.sin(time*2.4f+gx*1.7f+gy)*.65f;
        c(0x040b15,.6f); shapes.ellipse(x-(giant?16:12),y-6,giant?33:25,10);
        if(selected) { c(0x69e6d1,.17f); diamond(x,y-1,22,10); c(0x98f8df,.85f); diamondOutline(x,y-1,21,9,1.3f); }
        float yy=y+bob;
        if(ally) {
            int coat=medic?0xced0b3:outfit%3==1?0xa8734b:outfit%3==2?0x577678:0x3b666c;
            int shaded=medic?0x888f83:outfit%3==1?0x6f4636:outfit%3==2?0x344852:0x25434e;
            int flesh=switch(Math.floorMod(skin,4)){case 1->0xb47e59;case 2->0x80513e;case 3->0x563d35;default->0xddaf86;};
            // Small pixel silhouette: boots, asymmetric coat, backpack and face.
            c(0x11232e); shapes.rect(x-7,yy,5,10); shapes.rect(x+2,yy+1,5,9);
            c(0x6e7e7e); shapes.rect(x-8,yy,6,2); shapes.rect(x+2,yy+1,7,2);
            c(shaded); shapes.rect(x-9,yy+9,17,16); shapes.rect(x-12,yy+13,5,12);
            c(coat); shapes.rect(x-6,yy+11,13,17); shapes.rect(x-9,yy+21,16,7);
            c(0x1d3642); shapes.rect(x-3,yy+12,3,15); shapes.rect(x-7,yy+11,15,3);
            c(medic?0xbc6950:0xcb9f66); shapes.rect(x+1,yy+23,7,3);
            c(flesh); shapes.rect(x-5,yy+29,11,11); shapes.rect(x-7,yy+31,3,6);
            c(medic?0x3e2927:hair%3==1?0x997053:hair%3==2?0x726d73:0x172833);
            shapes.rect(x-6,yy+37,13,5); shapes.rect(x-7,yy+33,4,7);
            if(medic || hair%3==1) { shapes.rect(x-8,yy+26,4,12); shapes.rect(x+5,yy+30,3,9); }
            c(0x172a34); shapes.rect(x+1,yy+33,2,2);
            c(0xf0d2aa,.6f); shapes.rect(x-1,yy+31,5,1);
            if(!medic && accessory != 0) {
                c(0x183a47); shapes.rect(x-6,yy+33,13,4);
                c(0x57cdbd); shapes.rect(x-3,yy+34,8,2);
                c(0xbbf6db); shapes.rect(x+2,yy+35,3,1);
                c(0xcc9160); shapes.rect(x-7,yy+34,2,2);
            }
            c(flesh); shapes.rect(x+7,yy+15,4,7);
            if(medic) { c(0xe7e3c7); shapes.rect(x-11,yy+15,5,8); c(0xb9634a); shapes.rect(x-10,yy+18,4,2); shapes.rect(x-9,yy+16,2,6); }
            else { c(0x122831); shapes.rect(x+9,yy+12,3,19); c(0xa3c0b9); shapes.rect(x+9,yy+29,3,9); c(0x70efcf); shapes.rect(x+10,yy+32,1,5); }
        } else if (giant) {
            c(0x273b44); quad(x-17,yy+6,x-10,yy+37,x+11,yy+38,x+19,yy+5);
            c(0x536663); quad(x-10,yy+8,x-8,yy+38,x+5,yy+33,x+9,yy+5);
            c(0x172d37); shapes.rect(x-18,yy+2,9,24); shapes.rect(x+9,yy+2,9,25);
            c(0x768d7a); shapes.rect(x-8,yy+37,15,13);
            c(0x2b403f); shapes.rect(x-11,yy+46,22,8);
            c(0xe0cfa8); shapes.rect(x-3,yy+40,10,3);
            c(0x91f3d0); shapes.rect(x-7,yy+45,4,2); shapes.rect(x+4,yy+45,4,2);
            c(0x324b51); shapes.rect(x-22,yy+25,8,16); shapes.rect(x+15,yy+18,8,22);
            c(0xc7d2b8); shapes.rect(x-23,yy+15,6,12); shapes.rect(x+18,yy+10,5,11);
            c(0x70e6c5,.6f); line(x-6,yy+29,x+4,yy+21,1.5f); line(x+4,yy+21,x+1,yy+11,1.5f);
            if(!reducedMotion) { c(0x6ad7b4,.17f); line(x-20,yy+45,x-27,yy+52+(float)Math.sin(time)*3,1.3f); }
        } else {
            c(runner?0x58423c:0x263f45); shapes.rect(x-7,yy+1,5,12); shapes.rect(x+3,yy,5,14);
            c(runner?0x9e5c46:crystal?0x5b6778:0x667b6a); quad(x-10,yy+13,x-7,yy+29,x+6,yy+27,x+10,yy+10);
            c(runner?0x6b3f39:0x354c4c); shapes.rect(x-10,yy+15,6,13); shapes.rect(x+5,yy+12,6,13);
            c(0xa6ab84); shapes.rect(x-6,yy+28,13,10); shapes.rect(x+10,yy+6,3,11);
            c(0x33473f); shapes.rect(x-7,yy+35,11,5);
            c(runner?0xf09465:0xa6efc4); shapes.rect(x+1,yy+33,3,2);
            c(0x263d3e); shapes.rect(x+1,yy+29,6,2);
            c(0x1b3039); line(x-3,yy+25,x+3,yy+17,2);
            if(runner) { c(0xa46d4e); tri(x-10,yy+25,x-15,yy+15,x-6,yy+19); c(0x262f35); shapes.rect(x-9,yy-1,7,3); }
            if(crystal) {
                c(0x507a91); tri(x-9,yy+22,x-15,yy+46,x-2,yy+32);
                c(0x93e7e0); tri(x-15,yy+46,x-6,yy+30,x-2,yy+32);
                c(0x4fa6ad); tri(x+7,yy+16,x+18,yy+33,x+10,yy+29);
                c(0xa0ffdf); shapes.rect(x-8,yy+40,2,3);
            }
        }
        if(hpRatio<.999f) {
            c(0x08141e,.9f); shapes.rect(x-14,yy-12,28,4);
            c(ally?0x72d5b7:0xd17b5f); shapes.rect(x-13,yy-11,26*Math.max(0,Math.min(1,hpRatio)),2);
        }
    }

    public void marker(float gx,float gy,Color color,float time) {
        float x=isoX(gx,gy),y=isoY(gx,gy);
        float pulse=reducedMotion ? .8f : .65f+.2f*(float)Math.sin(time*2);
        shapes.setColor(color.r,color.g,color.b,pulse*.18f); diamond(x,y,24,11);
        shapes.setColor(color.r,color.g,color.b,pulse); diamondOutline(x,y,24,11,1.2f);
        float bob=reducedMotion?0:(float)Math.sin(time*2)*2;
        tri(x-4,y+53+bob,x+4,y+53+bob,x,y+48+bob);
    }

    private void block(float x,float y,float w,float d,float h,int left,int right,int top) {
        c(left); quad(x-w,y,x,y-d,x,y-d+h,x-w,y+h);
        c(right); quad(x,y-d,x+w,y,x+w,y+h,x,y-d+h);
        c(top); diamond(x,y+h,w,d);
    }
    private void diamond(float x,float y,float w,float h) { tri(x-w,y,x,y+h,x+w,y); tri(x-w,y,x+w,y,x,y-h); }
    private void diamondOutline(float x,float y,float w,float h,float thickness) { line(x-w,y,x,y+h,thickness); line(x,y+h,x+w,y,thickness); line(x+w,y,x,y-h,thickness); line(x,y-h,x-w,y,thickness); }
    private void quad(float ax,float ay,float bx,float by,float cx,float cy,float dx,float dy) { tri(ax,ay,bx,by,cx,cy); tri(ax,ay,cx,cy,dx,dy); }
    private void tri(float ax,float ay,float bx,float by,float cx,float cy) { shapes.triangle(ax,ay,bx,by,cx,cy); }
    private void line(float x,float y,float xx,float yy,float width) { shapes.rectLine(x,y,xx,yy,width); }
    private void c(int rgb) { c(rgb,1); }
    private void c(int rgb,float alpha) { shapes.setColor(((rgb>>16)&255)/255f,((rgb>>8)&255)/255f,(rgb&255)/255f,alpha); }
    private static int hash(int x,int y) { int v=x*374761393+y*668265263; v=(v^(v>>>13))*1274126177; return v^(v>>>16); }
    public void end() { shapes.end(); }
    @Override public void dispose() { shapes.dispose(); }
}
