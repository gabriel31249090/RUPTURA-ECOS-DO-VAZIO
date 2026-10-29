package ruptura.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.List;
import java.util.function.Consumer;
import ruptura.RupturaGame;
import ruptura.render.PortraitRenderer;
import ruptura.story.DialogueChoice;
import ruptura.story.DialogueLine;
import ruptura.story.DialogueScript;
import ruptura.story.GameSession;

public final class DialogueOverlay {
    private final RupturaGame game;
    private final GameSession session;
    private final List<DialogueLine> lines;
    private final List<DialogueChoice> choices;
    private final Consumer<DialogueChoice> done;
    private int index;
    private float revealed;
    private boolean history,finished;
    private final PortraitRenderer portraits = new PortraitRenderer();
    public DialogueOverlay(RupturaGame game,GameSession session,String scene,Consumer<DialogueChoice> done) {
        this.game=game;this.session=session;this.lines=DialogueScript.scene(scene,session);this.choices=DialogueScript.choices(scene);this.done=done;
    }
    public void draw(float dt) {
        Ui ui=game.ui;
        ui.rect(0,736,1280,64,Ui.INK);ui.text(ui.mono,"O PRIMEIRO ECO / ARQUIVO DE MEMÓRIA",36,774,Ui.CYAN);
        ui.right(ui.mono,"ESPAÇO  AVANÇAR  /  TAB  REVELAR  /  H  HISTÓRICO",1244,774,Ui.MUTED);
        if(Gdx.input.isKeyJustPressed(Input.Keys.H))history=!history;
        if(history){drawHistory();return;}
        if(index<lines.size()){
            DialogueLine line=lines.get(index); revealed+=dt*game.settings.textSpeed;
            int n=Math.min(line.text().length(),(int)revealed);
            ui.panel(36,66,1208,238);ui.rect(36,66,5,238,Ui.CYAN);
            ui.shapes.begin(ShapeRenderer.ShapeType.Filled);
            portraits.draw(ui.shapes,64,82,205,portraitKind(line.speaker()),line.expression());
            ui.shapes.end();
            ui.text(ui.mono,line.speaker().toUpperCase(),302,270,Ui.CYAN);
            ui.wrap(ui.body,line.text().substring(0,n),302,228,866,Ui.TEXT);
            ui.text(ui.mono,String.format("%02d / %02d",index+1,lines.size()),302,103,Ui.MUTED);
            ui.button(n<line.text().length()?"Revelar fala":"Continuar   →",1020,84,199,43,false,this::advance);
            if(Gdx.input.isKeyJustPressed(Input.Keys.TAB))revealed=line.text().length();
            if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)||Gdx.input.isKeyJustPressed(Input.Keys.ENTER))advance();
        }else if(!choices.isEmpty()){
            ui.panel(300,80,944,90+choices.size()*65);
            ui.text(ui.mono,"O QUE VOCÊ DIZ PERMANECE.",326,148+choices.size()*65,Ui.ORANGE);
            for(int i=0;i<choices.size();i++){
                final DialogueChoice choice=choices.get(i);
                ui.button((i+1)+"   "+choice.text(),326,100+(choices.size()-1-i)*65,892,51,false,()->finish(choice));
                if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1+i))finish(choice);
            }
        }else finish(null);
    }
    private void drawHistory(){
        Ui ui=game.ui;ui.modal();ui.shade(.88f);ui.panel(150,100,980,580);ui.text(ui.heading,"As vozes que ficaram",180,647,Ui.TEXT);
        int start=Math.max(0,session.dialogueHistory.size()-7), y=585;
        for(int i=start;i<session.dialogueHistory.size();i++){ui.wrap(ui.small,session.dialogueHistory.get(i),180,y,900,Ui.MUTED);y-=61;}
        ui.button("Fechar histórico  [H]",180,120,900,42,true,()->history=false);
    }
    public void advance(){
        if(finished||history||index>=lines.size())return;
        DialogueLine line=lines.get(index);
        if(revealed<line.text().length()){revealed=line.text().length();return;}
        session.dialogueHistory.add(line.speaker()+": "+line.text());index++;revealed=0;
        if(index==lines.size()&&choices.isEmpty())finish(null);
    }
    public boolean awaitingChoice(){return !finished&&index>=lines.size()&&!choices.isEmpty();}
    /** Uses the same validated completion path as the visible response buttons. */
    public void chooseResponse(String value){
        if(!awaitingChoice()||history)return;
        for(DialogueChoice choice:choices)if(choice.value().equals(value)){finish(choice);return;}
    }
    private void finish(DialogueChoice choice){if(finished)return;finished=true;done.accept(choice);}
    private String portraitKind(String speaker){
        String s=speaker.toLowerCase();if(s.contains("lia"))return "lia";if(s.contains("davi"))return "davi";
        return speaker.equals(session.heroName)?"hero":"narrador";
    }
}
