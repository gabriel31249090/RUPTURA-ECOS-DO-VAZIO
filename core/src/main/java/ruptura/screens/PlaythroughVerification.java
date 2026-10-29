package ruptura.screens;

import com.badlogic.gdx.Gdx;
import java.nio.charset.StandardCharsets;
import ruptura.RupturaGame;
import ruptura.combat.CombatSystem;
import ruptura.debug.BattlePilot;
import ruptura.story.GameSession;

/** Opt-in integration driver. Walks actual routes and uses the same dialogue/battle commands. */
public final class PlaythroughVerification {
    private final RupturaGame game;
    private final AdventureScreen screen;
    private final boolean rescue;
    private int frames;
    private boolean completed;
    public PlaythroughVerification(RupturaGame game,AdventureScreen screen,boolean rescue){this.game=game;this.screen=screen;this.rescue=rescue;}
    public void update(){
        if(completed)return;
        frames++;
        if(frames>18000)throw new IllegalStateException("Verificação excedeu 18000 quadros em "+screen.currentState());
        if(frames%2!=0)return;
        GameSession s=screen.campaign();
        switch(screen.currentState()){
            case DIALOGUE->{
                var dialogue=screen.conversation();
                if(dialogue.awaitingChoice()){
                    if(!s.choices.containsKey("first_response"))dialogue.chooseResponse(rescue?"empathy":"pragmatic");
                    else dialogue.chooseResponse(rescue?"rescue":"supplies");
                }else dialogue.advance();
            }
            case EXPLORATION->{
                if(!s.crateOpened)walkAndInteract(s,4,6);
                else if(!s.firstBattleWon)screen.walkTo(7,7);
                else if(!s.choices.containsKey("survivor"))walkAndInteract(s,11,8);
                else walkAndInteract(s,15,11);
            }
            case COMBAT->{
                var battle=screen.combat();
                if(battle.phase()==CombatSystem.Phase.DEFEAT)throw new IllegalStateException("Piloto perdeu o encontro de verificação");
                if(battle.phase()==CombatSystem.Phase.VICTORY)screen.continueBattle();
                else BattlePilot.act(battle);
            }
            case ENDING->{
                GameSession restored=game.saves.load().orElseThrow();
                if(!restored.completed||!restored.firstBattleWon||!restored.bossWon||restored.survivorHelped!=rescue
                    ||!restored.choices.equals(s.choices)||restored.affinity!=s.affinity||restored.xp!=150)
                    throw new IllegalStateException("Progresso completo diverge do salvamento");
                String result="PASS: janela GL, recursos, introdução, escolhas, exploração com colisão, suprimentos, dois combates, encerramento e save/load.\n"+
                    "rota="+(rescue?"resgate":"priorizar rádio")+"; xp="+s.xp+"; afinidade="+s.affinity+"; quadros="+frames+"\n";
                Gdx.files.local("build/verification/"+(rescue?"rescue":"supplies")+"-result.txt").writeString(result,false,StandardCharsets.UTF_8.name());
                Gdx.app.log("Verificação",result);completed=true;Gdx.app.exit();
            }
            default->throw new IllegalStateException("Estado inesperado na verificação: "+screen.currentState());
        }
    }
    private void walkAndInteract(GameSession s,int x,int y){
        float dx=s.playerX-x,dy=s.playerY-y;
        if(dx*dx+dy*dy<.55f)screen.interactForVerification();else screen.walkTo(x,y);
    }
}
