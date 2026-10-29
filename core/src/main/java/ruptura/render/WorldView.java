package ruptura.render;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import ruptura.RupturaGame;
import ruptura.story.GameSession;
import ruptura.ui.Ui;
import ruptura.world.WorldMap;

public final class WorldView {
    private final RupturaGame game;
    private final WorldMap map;
    private float focusX=2,focusY=3;
    private final float zoom=1.35f;
    private float offsetX,offsetY;
    public WorldView(RupturaGame game,WorldMap map){this.game=game;this.map=map;}
    public Vector2 cellAt(float screenX,float screenY){
        float ix=(screenX-offsetX)/zoom/32, iy=(screenY-offsetY)/zoom/16;
        return new Vector2(Math.round((ix+iy)/2),Math.round((iy-ix)/2));
    }
    public void draw(Matrix4 projection,GameSession session,float dt,boolean hud){
        focusX+=(session.playerX-focusX)*Math.min(1,dt*5);focusY+=(session.playerY-focusY)*Math.min(1,dt*5);
        offsetX=690-WorldRenderer.isoX(focusX,focusY)*zoom;
        offsetY=355-WorldRenderer.isoY(focusX,focusY)*zoom;
        WorldRenderer r=game.worldRenderer;
        r.setProjection(new Matrix4(projection).translate(offsetX,offsetY,0).scale(zoom,zoom,1));
        r.begin(game.time,game.settings.reducedMotion);
        for(int d=WorldMap.WIDTH+WorldMap.HEIGHT-2;d>=0;d--)for(int x=0;x<WorldMap.WIDTH;x++){
            int y=d-x;if(y<0||y>=WorldMap.HEIGHT)continue;
            r.tile(x,y,map.tile(x,y),map.elevation(x,y),false,map.tile(x,y)==4);
        }
        Vector2 hover=cellAt(game.ui.mouseX(),game.ui.mouseY());
        if(hud&&map.walkable((int)hover.x,(int)hover.y))r.marker(hover.x,hover.y,Ui.MUTED,game.time);
        for(int d=WorldMap.WIDTH+WorldMap.HEIGHT-2;d>=-1;d--)for(int x=0;x<WorldMap.WIDTH;x++){
            int y=d-x;if(y<0||y>=WorldMap.HEIGHT)continue;
            if(map.tile(x,y)==WorldMap.BUILDING&&((x*3+y)%5==0))r.prop(x,y,1,game.time);
            if(x==5&&y==5)r.prop(x,y,2,game.time);
            if((x==3&&y==6)||(x==9&&y==9)||(x==14&&y==12))r.prop(x,y,3,game.time);
            if(x==4&&y==6&&!session.crateOpened)r.prop(x,y,4,game.time);
            if(x==15&&y==11)r.prop(x,y,5,game.time);
            if(x==13&&y==10)r.prop(x,y,6,game.time);
            if(!session.firstBattleWon&&x==7&&y==7)r.actor(x,y,"estilhacado",false,0,0,0,game.time,false,1);
            if(!session.choices.containsKey("survivor")&&x==11&&y==8)r.actor(x,y,"hero",true,2,1,2,game.time,false,.3f);
            if(!session.bossWon&&x==15&&y==10)r.actor(x,y,"sussurrante",false,0,0,0,game.time,false,1);
            if(x==Math.round(session.playerX)&&y==Math.round(session.playerY)){
                r.actor(session.playerX+.65f,session.playerY+.75f,"lia",true,1,0,0,game.time,false,1);
                r.actor(session.playerX,session.playerY,"hero",true,session.skin,session.hair,session.outfit,session.accessory,game.time,true,1);
            }
        }
        if(!session.firstBattleWon)r.marker(7,7,Ui.ORANGE,game.time);
        else if(!session.choices.containsKey("survivor"))r.marker(11,8,Ui.ORANGE,game.time);
        else r.marker(15,11,Ui.CYAN,game.time);
        r.end();
        if(hud)miniMap(session);
    }
    private void miniMap(GameSession s){
        Ui ui=game.ui;ui.panel(1052,566,192,144);ui.text(ui.mono,"SETOR 07 / AURORA",1065,693,Ui.MUTED);
        for(int x=0;x<WorldMap.WIDTH;x++)for(int y=0;y<WorldMap.HEIGHT;y++){
            if(map.walkable(x,y))ui.rect(1068+x*8,571+y*6,6,4,Ui.LINE);
        }
        ui.rect(1068+s.playerX*8,571+s.playerY*6,6,5,Ui.CYAN);
        ui.rect(1068+15*8,571+11*6,6,5,Ui.ORANGE);
    }
}
