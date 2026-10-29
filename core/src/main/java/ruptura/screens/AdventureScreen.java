package ruptura.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayDeque;
import java.util.List;
import ruptura.RupturaGame;
import ruptura.combat.CombatSystem;
import ruptura.combat.Difficulty;
import ruptura.render.Atmosphere;
import ruptura.render.WorldView;
import ruptura.story.DialogueChoice;
import ruptura.story.DialogueScript;
import ruptura.story.GameSession;
import ruptura.ui.CombatView;
import ruptura.ui.DialogueOverlay;
import ruptura.ui.SettingsPanel;
import ruptura.ui.Ui;
import ruptura.world.Cell;
import ruptura.world.WorldMap;

/** Campaign coordinator. Domain rules, rendering, battle and narrative remain separate. */
public final class AdventureScreen extends BaseScreen {
    public enum State { EXPLORATION, DIALOGUE, COMBAT, PAUSE, INVENTORY, JOURNAL, CODEX, ENDING }
    private final GameSession session;
    private final WorldMap map;
    private final WorldView world;
    private final ArrayDeque<Cell> route = new ArrayDeque<>();
    private State state=State.EXPLORATION, pausedFrom=State.EXPLORATION;
    private CombatSystem battle;
    private CombatView combatView;
    private DialogueOverlay dialogue;
    private boolean bossEncounter,settings;
    private String toast="";
    private float toastTime;
    private boolean encounterHandled;

    public AdventureScreen(RupturaGame game,GameSession session,boolean introduction){
        super(game);this.session=session;map=new WorldMap(session.seed);world=new WorldView(game,map);
        if(session.completed)state=State.ENDING;
        if(introduction)startDialogue("intro",choice->{
            applyChoice(choice);session.questStage=Math.max(1,session.questStage);
            startDialogue("intro_"+choice.value(),ignored->{state=State.EXPLORATION;save();notify("WASD ou clique no chão para caminhar. E para interagir.");});
        });
    }
    public boolean inCombat(){return state==State.COMBAT||(state==State.PAUSE&&pausedFrom==State.COMBAT);}
    @Override protected void draw(float dt){
        Atmosphere.draw(ui,game.time,game.settings.reducedMotion);
        boolean battleVisible=battle!=null&&(state==State.COMBAT||(state==State.PAUSE&&pausedFrom==State.COMBAT));
        if(battleVisible){
            battle.setReducedMotion(game.settings.reducedMotion);
            combatView.draw(viewport.getCamera().combined,dt,state==State.COMBAT);
            if(state==State.COMBAT)checkBattle();
        }else{
            world.draw(viewport.getCamera().combined,session,dt,state==State.EXPLORATION);
            if(state==State.EXPLORATION){updateMovement(dt);if(state==State.EXPLORATION)drawExploration();}
        }
        if(state==State.DIALOGUE&&dialogue!=null)dialogue.draw(dt);
        if(state==State.PAUSE)drawPause();
        if(state==State.INVENTORY||state==State.JOURNAL||state==State.CODEX)drawArchive();
        if(state==State.ENDING)drawEnding();
        if(toastTime>0){toastTime-=dt;ui.panel(330,714,620,45);ui.center(ui.small,toast,344,744,592,Ui.CYAN);}
        if(settings)SettingsPanel.draw(game,()->settings=false);
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            if(settings)settings=false;
            else if(state==State.PAUSE)state=pausedFrom;
            else if(state==State.INVENTORY||state==State.JOURNAL||state==State.CODEX)state=State.EXPLORATION;
            else if(state==State.EXPLORATION||state==State.COMBAT){pausedFrom=state;state=State.PAUSE;}
        }
    }
    private void drawExploration(){
        ui.text(ui.mono,"RUPTURA / ZONA DE EVACUAÇÃO",36,768,Ui.CYAN);
        ui.text(ui.heading,"Aurora, depois do silêncio",36,735,Ui.TEXT);
        ui.panel(36,508,280,172);ui.text(ui.mono,"MISSÃO ATIVA / 01",54,655,Ui.ORANGE);
        ui.text(ui.heading,"O Primeiro Eco",54,624,Ui.TEXT);
        ui.wrap(ui.small,session.objective(),54,575,243,Ui.MUTED);
        ui.panel(36,67,332,110);ui.text(ui.small,session.heroName+"  /  "+session.specialization,54,153,Ui.TEXT);
        ui.text(ui.mono,"NÍVEL "+session.level+"  ·  "+session.xp+" XP  ·  "+session.medkits+" ESTOJOS",54,122,Ui.CYAN);
        ui.text(ui.small,"Lia Torres  /  "+(session.affinity>=2?"Confia em você":"Observa suas escolhas"),54,94,Ui.MUTED);
        ui.button("Diário [J]",968,184,276,42,false,()->state=State.JOURNAL);
        ui.button("Inventário [I]",968,132,276,42,false,()->state=State.INVENTORY);
        String interaction=interaction();
        if(!interaction.isEmpty())ui.button("E   "+interaction,808,67,436,50,true,this::interact);
        else ui.text(ui.mono,"SIGA O MARCADOR / RÁDIO A LESTE",810,97,Ui.MUTED);
        footer("WASD / SETAS  MOVER   ·   CLIQUE  CAMINHAR   ·   E  INTERAGIR","J  DIÁRIO   I  INVENTÁRIO   F5  SALVAR   ESC  PAUSA");
        if(Gdx.input.isKeyJustPressed(Input.Keys.E))interact();
        if(Gdx.input.isKeyJustPressed(Input.Keys.J))state=State.JOURNAL;
        if(Gdx.input.isKeyJustPressed(Input.Keys.I))state=State.INVENTORY;
        if(Gdx.input.isKeyJustPressed(Input.Keys.F5))save();
    }
    private void updateMovement(float dt){
        float sx=0,sy=0;
        if(Gdx.input.isKeyPressed(Input.Keys.W)||Gdx.input.isKeyPressed(Input.Keys.UP))sy++;
        if(Gdx.input.isKeyPressed(Input.Keys.S)||Gdx.input.isKeyPressed(Input.Keys.DOWN))sy--;
        if(Gdx.input.isKeyPressed(Input.Keys.D)||Gdx.input.isKeyPressed(Input.Keys.RIGHT))sx++;
        if(Gdx.input.isKeyPressed(Input.Keys.A)||Gdx.input.isKeyPressed(Input.Keys.LEFT))sx--;
        float dx=sx+sy,dy=sy-sx;
        if(dx!=0||dy!=0)route.clear();
        else if(!route.isEmpty()){
            Cell next=route.peek();dx=next.x()-session.playerX;dy=next.y()-session.playerY;
            if(dx*dx+dy*dy<.035f){session.playerX=next.x();session.playerY=next.y();route.remove();dx=dy=0;}
        }
        float length=(float)Math.sqrt(dx*dx+dy*dy);
        if(length>0){float amount=Math.min(dt*2.85f,length);move(dx/length*amount,dy/length*amount);}
        if(Gdx.input.justTouched()&&ui.mouseX()>325&&ui.mouseX()<960&&ui.mouseY()>185&&ui.mouseY()<683){
            Vector2 target=world.cellAt(ui.mouseX(),ui.mouseY());
            List<Cell> path=WorldMap.path(map,Math.round(session.playerX),Math.round(session.playerY),(int)target.x,(int)target.y);
            route.clear();route.addAll(path);
        }
        if(!session.firstBattleWon&&near(7,7,1.35f)){
            route.clear();startDialogue("combatIntro",ignored->startBattle(false));
        }
    }
    private void move(float dx,float dy){
        if(canStand(session.playerX+dx,session.playerY))session.playerX+=dx;
        if(canStand(session.playerX,session.playerY+dy))session.playerY+=dy;
    }
    private boolean canStand(float x,float y){
        return map.walkable(Math.round(x-.2f),Math.round(y-.2f))&&map.walkable(Math.round(x+.2f),Math.round(y+.2f))
            &&map.walkable(Math.round(x-.2f),Math.round(y+.2f))&&map.walkable(Math.round(x+.2f),Math.round(y-.2f));
    }
    private boolean near(float x,float y,float distance){float dx=x-session.playerX,dy=y-session.playerY;return dx*dx+dy*dy<distance*distance;}
    private String interaction(){
        if(!session.crateOpened&&near(4,6,1.65f))return "Abrir suprimentos";
        if(near(11,8,1.9f)&&session.firstBattleWon&&!session.choices.containsKey("survivor"))return "Investigar a voz";
        if(near(15,11,2))return session.bossWon?"Ativar o transmissor":"Entrar na estação";
        if(near(3,4,2))return "Conversar com Lia";
        return "";
    }
    private void interact(){
        if(!session.crateOpened&&near(4,6,1.65f)){
            session.crateOpened=true;session.medkits+=2;session.scrap+=3;startDialogue("crate",ignored->{state=State.EXPLORATION;save();});
        }else if(near(11,8,1.9f)&&session.firstBattleWon&&!session.choices.containsKey("survivor")){
            startDialogue("survivor",choice->{applyChoice(choice);startDialogue(session.survivorHelped?"survivorRescue":"survivorSupplies",ignored->{state=State.EXPLORATION;save();});});
        }else if(near(15,11,2)){
            if(!session.firstBattleWon){notify("Precisamos atravessar o bloqueio. Lia espera na avenida.");return;}
            if(!session.choices.containsKey("survivor")){notify("Há uma voz entre os escombros. Investigue antes de seguir.");return;}
            if(!session.bossWon)startDialogue("radio",ignored->startBattle(true));
            else finishMission();
        }else if(near(3,4,2))startDialogue("lia",ignored->state=State.EXPLORATION);
    }
    private void startDialogue(String id,java.util.function.Consumer<DialogueChoice> done){
        route.clear();state=State.DIALOGUE;dialogue=new DialogueOverlay(game,session,id,done);
    }
    private void applyChoice(DialogueChoice choice){if(choice!=null){DialogueScript.applyChoice(session,choice);save();}}
    public void startBattle(boolean boss){
        bossEncounter=boss;encounterHandled=false;
        battle=new CombatSystem(Difficulty.values()[Math.max(0,Math.min(3,session.difficultyIndex))],boss,session.heroName,session.specialization);
        battle.setPotions(session.medkits);battle.setAffinity(session.affinity);
        combatView=new CombatView(game,session,battle);state=State.COMBAT;game.audio.play("impact",game.settings.volume);
    }
    private void checkBattle(){
        if(battle.phase()!=CombatSystem.Phase.VICTORY&&battle.phase()!=CombatSystem.Phase.DEFEAT)return;
        boolean victory=battle.phase()==CombatSystem.Phase.VICTORY;
        if(victory&&!encounterHandled){
            session.medkits=battle.potions();session.rewardBattle(bossEncounter);encounterHandled=true;save();game.audio.play("victory",game.settings.volume);
        }
        ui.shade(.82f);ui.panel(380,245,520,315);
        ui.text(ui.mono,victory?"CONTATO ENCERRADO / SINAL ESTÁVEL":"SINAL PERDIDO / SEM RESPOSTA",411,522,victory?Ui.CYAN:Ui.ORANGE);
        ui.text(ui.heading,victory?"Ainda estamos aqui.":"O vazio se aproxima.",411,477,Ui.TEXT);
        ui.wrap(ui.body,victory?(bossEncounter?"+100 XP · +6 peças\nO transmissor está ao alcance.":"+50 XP · +3 peças\nA avenida está livre. Mas alguém pede ajuda."):"Lia chama seu nome. Há uma chance de voltar ao início deste encontro.",411,416,450,Ui.MUTED);
        ui.button(victory?"Continuar   →":"Tentar o encontro novamente",411,272,458,50,true,()->{
            continueBattle();
        });
    }
    void continueBattle(){
        if(battle.phase()==CombatSystem.Phase.DEFEAT)startBattle(bossEncounter);
        else if(battle.phase()==CombatSystem.Phase.VICTORY){
            if(bossEncounter)finishMission();
            else startDialogue("firstVictory",ignored->{state=State.EXPLORATION;session.playerX=8;session.playerY=7;save();});
        }
    }
    State currentState(){return state;}
    GameSession campaign(){return session;}
    CombatSystem combat(){return battle;}
    DialogueOverlay conversation(){return dialogue;}
    void walkTo(int x,int y){
        if(!route.isEmpty())return;
        route.addAll(WorldMap.path(map,Math.round(session.playerX),Math.round(session.playerY),x,y));
    }
    void interactForVerification(){interact();}
    private void finishMission(){
        startDialogue("ending",ignored->{session.completeMission();save();state=State.ENDING;});
    }
    private void drawPause(){
        ui.shade(.84f);ui.panel(420,171,440,463);ui.text(ui.mono,"A RUA PODE ESPERAR UM INSTANTE",452,598,Ui.CYAN);
        ui.text(ui.heading,"Respire.",452,566,Ui.TEXT);
        ui.button("Retomar [ESC]",452,450,376,50,true,()->state=pausedFrom);
        ui.button("Configurações",452,384,376,50,false,()->settings=true);
        ui.button("Salvar progresso",452,318,376,50,false,pausedFrom!=State.COMBAT,()->save());
        ui.button("Voltar ao menu",452,252,376,50,false,()->game.open(new MenuScreen(game)));
        ui.text(ui.mono,"O PROGRESSO SALVA APÓS CADA ENCONTRO.",452,214,Ui.MUTED);
    }
    private void drawArchive(){
        ui.shade(.92f);ui.panel(100,92,1080,626);
        ui.text(ui.mono,"ARQUIVO LOCAL / "+session.heroName.toUpperCase(),132,682,Ui.CYAN);
        ui.text(ui.heading,state==State.INVENTORY?"O que carregamos":state==State.CODEX?"O que nos observa":"O Primeiro Eco",132,648,Ui.TEXT);
        ui.button("Diário",132,535,270,44,state==State.JOURNAL,()->state=State.JOURNAL);
        ui.button("Inventário",420,535,270,44,state==State.INVENTORY,()->state=State.INVENTORY);
        ui.button("Bestiário",708,535,270,44,state==State.CODEX,()->state=State.CODEX);
        if(state==State.INVENTORY){
            ui.wrap(ui.body,"Estojos médicos     "+session.medkits+"\n\nPeças recuperadas   "+session.scrap+"\n\nExperiência              "+session.xp+" XP / nível "+session.level,132,488,500,Ui.TEXT);
            ui.wrap(ui.small,"Estojos restauram vida durante o combate.\nSelecione ESTOJO e um aliado ferido.\n\nAs peças e os níveis ficam salvos para a expansão da jornada.\nFabricação e árvore de habilidades: fase futura.",690,488,410,Ui.MUTED);
        }else if(state==State.JOURNAL){
            String[] objectives={"Ouça Lia e responda ao primeiro eco.","Atravesse o bloqueio na avenida.","Decida o destino da voz entre os escombros.","Enfrente o Sussurrante na estação.","Ative o transmissor de Orfeu."};
            boolean[] done={session.choices.containsKey("first_response"),session.firstBattleWon,session.choices.containsKey("survivor"),session.bossWon,session.completed};
            for(int i=0;i<5;i++)ui.text(ui.body,(done[i]?"✓  ":"·  ")+objectives[i],132,488-i*47,done[i]?Ui.CYAN:Ui.MUTED);
            ui.wrap(ui.small,"Lia: "+(session.affinity>=2?"confiança. A cura de campo ficou mais forte.":"cautela. Ela ainda observa suas decisões.")+"\nOrigem: "+session.origin+"  ·  Pronomes: "+session.pronouns,132,225,920,Ui.MUTED);
        }else{
            ui.wrap(ui.body,"ERRANTE\nLento. Golpes físicos quebram sua postura.\n\nCORREDOR\nRápido. Usa a distância para pressionar o grupo.\n\nESTILHAÇADO\nCristais instáveis: vulnerável à eletricidade.",132,488,490,Ui.TEXT);
            ui.wrap(ui.body,"SUSSURRANTE\nImita quem você perdeu. Ao perder metade da vida, muda de fase e expõe uma fraqueza dimensional.\n\nRESÍDUO DIMENSIONAL\nEvite as casas cianas durante o movimento: atravessá-las custa vida.",690,488,410,Ui.MUTED);
        }
        ui.button("Voltar à rua [ESC]",132,119,1008,47,true,()->state=State.EXPLORATION);
    }
    private void drawEnding(){
        ui.shade(.86f);ui.text(ui.mono,"MISSÃO CONCLUÍDA / CAPÍTULO 01",136,680,Ui.CYAN);
        ui.text(ui.title,"O PRIMEIRO ECO",128,624,Ui.TEXT);
        ui.wrap(ui.body,session.survivorHelped?"Ícaro abre a porta. Os sobreviventes encontram abrigo.\nAlém das muralhas, um sinal de doze anos começa a responder.":"O pedido de resgate atravessa a noite. Lia retorna à rua.\nVocê fica diante do rádio, ouvindo o que não deveria lembrar.",136,461,925,Ui.MUTED);
        ui.text(ui.heading,"“Desta vez, lembra de nós.”",136,348,Ui.CYAN);
        ui.text(ui.mono,"NÍVEL "+session.level+"  /  "+session.xp+" XP  /  VÍNCULO COM LIA "+session.affinity,136,272,Ui.MUTED);
        ui.text(ui.small,"Fim do trecho jogável. O progresso e as escolhas foram salvos.",136,232,Ui.TEXT);
        ui.button("Voltar ao menu",136,124,340,52,true,()->game.open(new MenuScreen(game)));
        ui.button("Rever o assentamento",496,124,340,52,false,()->state=State.EXPLORATION);
        footer("ATO I / O CÉU SE PARTIU","A TRANSMISSÃO CONTINUA.");
    }
    private void save(){
        try{game.saves.save(session);notify("Progresso salvo neste computador.");}
        catch(Exception ex){notify("Falha ao salvar: "+ex.getMessage());Gdx.app.error("Salvamento","Falha no arquivo local",ex);}
    }
    private void notify(String message){toast=message;toastTime=3.5f;}
}
