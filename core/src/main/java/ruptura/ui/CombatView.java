package ruptura.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import ruptura.RupturaGame;
import ruptura.combat.ActionType;
import ruptura.combat.CombatSystem;
import ruptura.combat.CombatUnit;
import java.util.List;
import ruptura.render.WorldRenderer;
import ruptura.story.GameSession;

/** Battle input/presentation; all legality and turn resolution live in CombatSystem. */
public final class CombatView {
    private final RupturaGame game;
    private final GameSession session;
    private final CombatSystem battle;
    private ActionType selected=ActionType.MOVE;
    private String lastActor="";
    private int pickedX=-1,pickedY=-1;
    public CombatView(RupturaGame game,GameSession session,CombatSystem battle){this.game=game;this.session=session;this.battle=battle;}
    public void draw(Matrix4 projection,float dt,boolean enabled){
        Ui ui=game.ui;
        if(enabled)battle.update(dt);
        if(!lastActor.equals(battle.current().id)){lastActor=battle.current().id;selected=ActionType.MOVE;}
        float gx=(ui.mouseX()-608)/1.20f/32, gy=(ui.mouseY()-290)/1.20f/16;
        int cx=Math.round((gx+gy)/2),cy=Math.round((gy-gx)/2);
        WorldRenderer r=game.worldRenderer;
        r.setProjection(new Matrix4(projection).translate(608,290,0).scale(1.20f,1.20f,1));r.begin(game.time,game.settings.reducedMotion);
        List<CombatUnit> units=battle.units();
        for(int d=16;d>=0;d--)for(int x=0;x<10;x++){
            int y=d-x;if(y<0||y>=8)continue;
            boolean valid=selected==ActionType.MOVE?battle.reachable(x,y):battle.canTarget(selected,x,y);
            r.tile(x,y,battle.blocked(x,y)?2:battle.hazardous(x,y)?4:0,0,valid,battle.hazardous(x,y));
            if(cx==x&&cy==y&&!battle.blocked(x,y))r.marker(x,y,valid?Ui.CYAN:Ui.ORANGE,game.time);
        }
        for(int d=16;d>=0;d--)for(int x=0;x<10;x++){
            int y=d-x;if(y<0||y>=8)continue;
            if(battle.blocked(x,y))r.prop(x,y,7,game.time);
            for(CombatUnit unit:units)if(unit.alive()&&unit.x==x&&unit.y==y){
                float anim=game.time;
                r.actor(x,y,unit.kind,unit.ally,session.skin,session.hair,session.outfit,unit.kind.equals("hero")?session.accessory:0,anim,battle.current()==unit,(float)unit.hp/unit.maxHp);
                if(unit.guarded)r.marker(x,y,Ui.CYAN,game.time);
            }
        }
        if(battle.effectTime()>0)r.marker(battle.effectX(),battle.effectY(),battle.effectKind().contains("heal")?Ui.CYAN:Ui.ORANGE,game.time*3);
        r.end();
        ui.text(ui.mono,"RUPTURA / CONTATO HOSTIL",36,767,Ui.ORANGE);
        ui.text(ui.heading,"Não siga as vozes.",36,735,Ui.TEXT);
        String phase=switch(battle.phase()){case PLAYER->"SUA VEZ";case ENEMY->"HOSTIL";case ANIMATING->"AÇÃO";case VICTORY->"VITÓRIA";case DEFEAT->"DERROTA";};
        ui.right(ui.mono,"RODADA "+String.format("%02d",battle.round())+" / "+phase,1244,767,Ui.MUTED);
        drawInitiative();
        ui.panel(36,279,236,353);ui.text(ui.mono,"SITUAÇÃO TÁTICA",54,608,Ui.MUTED);
        int cardY=568;
        for(CombatUnit unit:battle.units()){
            ui.text(ui.small,unit.name,54,cardY,unit.alive()?(unit.ally?Ui.CYAN:Ui.TEXT):Ui.MUTED);
            ui.text(ui.mono,unit.hp+" / "+unit.maxHp+" PV"+(unit.guarded?" · DEF":""),54,cardY-25,Ui.MUTED);
            ui.bar(54,cardY-42,198,(float)unit.hp/unit.maxHp,unit.ally?Ui.CYAN:Ui.ORANGE);cardY-=61;
        }
        ui.panel(1000,279,244,353);ui.text(ui.mono,"LEITURA DO ECO",1017,608,Ui.CYAN);
        CombatUnit hover=battle.unitAt(cx,cy);
        if(hover!=null){pickedX=cx;pickedY=cy;}
        CombatUnit target=battle.unitAt(pickedX,pickedY);
        if(target!=null){
            ui.text(ui.heading,target.name,1017,570,target.ally?Ui.CYAN:Ui.ORANGE);
            ui.wrap(ui.small,"Fraqueza: "+weakness(target.weakness)+"\nPostura: "+target.posture+"\nDefesa: "+target.defense+"\nMovimento: "+target.move+"\n"+(target.broken?"VULNERÁVEL":target.status),1017,522,208,Ui.MUTED);
            if(target.phaseStage>1)ui.text(ui.mono,"FASE II / RUPTURA",1017,360,Ui.ORANGE);
        }else ui.wrap(ui.small,"Aponte para uma unidade.\n\nAcertar uma fraqueza quebra a postura e abre espaço para um combo.",1017,565,208,Ui.MUTED);
        ui.text(ui.mono,"TERRENO CIANO",1017,328,Ui.ORANGE);ui.text(ui.small,"Resíduo: 5 PV ao pisar",1017,305,Ui.MUTED);
        if(enabled&&battle.phase()==CombatSystem.Phase.PLAYER){
            keyboard();
            if(Gdx.input.justTouched()&&ui.mouseX()>278&&ui.mouseX()<994&&ui.mouseY()>260&&ui.mouseY()<640)battle.perform(selected,cx,cy);
        }
        drawCommands(enabled);
        String line=battle.message();
        if(!line.equals(lastMessage)){if(battle.effectTime()>0)game.audio.play(battle.effectKind().contains("skill")?"weakness":"impact",game.settings.volume);lastMessage=line;}
    }
    private String lastMessage="";
    private void drawInitiative(){
        Ui ui=game.ui;int i=0;
        for(CombatUnit unit:battle.initiative()){
            float x=36+i*244;ui.panel(x,648,232,35);
            ui.text(ui.mono,(i==0?"→ ":"  ")+unit.name.toUpperCase(),x+12,672,i==0?Ui.CYAN:Ui.MUTED);i++;
        }
    }
    private void drawCommands(boolean enabled){
        Ui ui=game.ui;CombatUnit actor=battle.current();boolean can=enabled&&battle.phase()==CombatSystem.Phase.PLAYER;
        ui.panel(36,64,1208,195);ui.rect(36,64,4,195,actor.ally?Ui.CYAN:Ui.ORANGE);
        ui.text(ui.heading,actor.name,55,238,Ui.TEXT);
        ui.text(ui.mono,actor.energy+" / "+actor.maxEnergy+" EN   ·   "+(battle.hasMoved()?"MOVIMENTO USADO":"MOVER + UMA AÇÃO"),55,191,Ui.CYAN);
        ui.wrap(ui.small,battle.message(),55,158,302,Ui.MUTED);
        String[] labels={"1  MOVER","2  ATACAR","3  "+battle.skillName().toUpperCase(),"4  DEFENDER","5  ESTOJO ("+battle.potions()+")","6  ENCERRAR"};
        ActionType[] actions={ActionType.MOVE,ActionType.ATTACK,ActionType.SKILL,ActionType.DEFEND,ActionType.ITEM,ActionType.END_TURN};
        for(int i=0;i<6;i++){
            ActionType action=actions[i];float x=388+(i%3)*275,y=i<3?177:117;
            ui.button(labels[i],x,y,263,48,selected==action,can,()->select(action));
        }
        ui.text(ui.mono,selected==ActionType.SKILL?battle.skillDescription()+"  /  "+battle.skillCost()+" EN":selected==ActionType.MOVE?"CLIQUE NUMA CASA ILUMINADA. DEPOIS ESCOLHA UMA AÇÃO.":"CLIQUE NUM ALVO NO ALCANCE. AS CASAS VÁLIDAS FICAM ILUMINADAS.",388,94,Ui.MUTED);
    }
    private void select(ActionType action){
        selected=action;if(action==ActionType.DEFEND||action==ActionType.END_TURN)battle.perform(action,battle.current().x,battle.current().y);
    }
    private void keyboard(){
        ActionType[] a={ActionType.MOVE,ActionType.ATTACK,ActionType.SKILL,ActionType.DEFEND,ActionType.ITEM,ActionType.END_TURN};
        for(int i=0;i<6;i++)if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1+i))select(a[i]);
    }
    private String weakness(String value){return switch(value){case "electric"->"elétrica";case "dimensional"->"dimensional";case "physical"->"física";default->"nenhuma";};}
}
