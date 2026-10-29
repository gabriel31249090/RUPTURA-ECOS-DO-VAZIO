package ruptura.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Matrix4;
import ruptura.RupturaGame;
import ruptura.combat.Difficulty;
import ruptura.render.Atmosphere;
import ruptura.story.GameSession;
import ruptura.ui.Ui;

public final class CharacterScreen extends BaseScreen {
    private final GameSession session = new GameSession();
    private static final String[] SPECIALIZATIONS={"Ressonante","Executor","Tecnomante","Sobrevivente","Médico de campo"};
    private static final String[] ORIGINS={"Mensageiro","Ex-militar","Técnico de abrigo"};
    private static final String[] PRONOUNS={"elu/delu","ela/dela","ele/dele"};
    private int specialization,origin,pronoun;
    private boolean editing;
    public CharacterScreen(RupturaGame game) { super(game); }
    @Override public void show(){Gdx.input.setInputProcessor(new InputAdapter(){
        @Override public boolean keyTyped(char c){
            if(!editing)return false;
            if(c=='\b'&&!session.heroName.isEmpty())session.heroName=session.heroName.substring(0,session.heroName.length()-1);
            else if(!Character.isISOControl(c)&&session.heroName.length()<18&&Character.isDefined(c))session.heroName+=c;
            return true;
        }
    });}
    @Override public void hide(){Gdx.input.setInputProcessor(null);}
    @Override protected void draw(float delta){
        Atmosphere.draw(ui,game.time,game.settings.reducedMotion);ui.shade(.48f);
        ui.frameTitle("ARQUIVO DE SOBREVIVENTE / 001","Quem ouve do outro lado?","NOVO JOGO / IDENTIDADE");
        ui.panel(36,105,345,551);ui.text(ui.mono,"RESSONÂNCIA DETECTADA",60,626,Ui.CYAN);
        var r=game.worldRenderer;
        r.setProjection(new Matrix4(viewport.getCamera().combined).translate(206,345,0).scale(4,4,1));
        r.begin(game.time,game.settings.reducedMotion);r.tile(0,0,4,0,true,false);
        r.actor(0,0,"hero",true,session.skin,session.hair,session.outfit,session.accessory,game.time,true,1);r.end();
        ui.text(ui.heading,session.heroName.isBlank()?"Sem nome":session.heroName,60,265,Ui.TEXT);
        ui.wrap(ui.small,"Você nunca esteve além da muralha. Mas sonha com lugares que não existem.",60,216,287,Ui.MUTED);
        ui.text(ui.mono,"SINAL VITAL  /  ESTÁVEL",60,135,Ui.CYAN);
        ui.text(ui.mono,"NOME  /  CLIQUE PARA DIGITAR",414,637,Ui.MUTED);
        ui.button(session.heroName+(editing?"_":""),414,569,380,48,editing,()->editing=!editing);
        ui.button("Pronomes: "+PRONOUNS[pronoun],820,569,424,48,false,()->{pronoun=(pronoun+1)%PRONOUNS.length;session.pronouns=PRONOUNS[pronoun];});
        ui.text(ui.mono,"APARÊNCIA",414,540,Ui.MUTED);
        ui.button("Pele · "+(session.skin+1),414,477,185,44,false,()->session.skin=(session.skin+1)%4);
        ui.button("Cabelo · "+(session.hair+1),611,477,183,44,false,()->session.hair=(session.hair+1)%3);
        ui.button("Roupa · "+(session.outfit+1),820,477,202,44,false,()->session.outfit=(session.outfit+1)%3);
        ui.button("Acessório · "+(session.accessory==0?"Nenhum":"Visor"),1034,477,210,44,false,()->session.accessory=1-session.accessory);
        ui.text(ui.mono,"QUEM VOCÊ ERA",414,447,Ui.MUTED);
        ui.button(ORIGINS[origin],414,383,380,46,false,()->{origin=(origin+1)%ORIGINS.length;session.origin=ORIGINS[origin];});
        ui.button(SPECIALIZATIONS[specialization],820,383,424,46,false,()->{specialization=(specialization+1)%SPECIALIZATIONS.length;session.specialization=SPECIALIZATIONS[specialization];});
        String[] descriptions={"Pulso do vazio: revela fraquezas dimensionais a distância.","Golpes físicos fortes e energia para resistir à linha de frente.","Pulso elétrico: sobrecarrega cristais e sistemas corrompidos.","Mais vida e defesa para atravessar o primeiro contato.","Mais energia e Estabilizar: recupere a vida de um aliado."};
        ui.wrap(ui.small,descriptions[specialization],820,363,414,Ui.MUTED);
        ui.text(ui.mono,"DIFICULDADE  /  ALTERA DANO E IA",414,302,Ui.MUTED);
        for(int i=0;i<4;i++){final int v=i;ui.button(Difficulty.values()[i].label(),414+i*210,238,200,44,session.difficultyIndex==i,()->session.difficultyIndex=v);}
        ui.wrap(ui.small,"A sua aparência, origem e pronomes ficam registrados. A especialização muda seus atributos e sua habilidade inicial.",414,216,790,Ui.MUTED);
        ui.button("Voltar",414,105,185,49,false,()->game.open(new MenuScreen(game)));
        ui.button("ATRAVESSAR A NOITE   →",820,105,424,49,true,()->{
            session.heroName=session.heroName.trim();if(session.heroName.isEmpty())session.heroName="Alex";
            session.medkits=new int[]{4,3,2,1}[session.difficultyIndex];
            game.open(new AdventureScreen(game,session,true));
        });
        footer("CLIQUE NAS OPÇÕES PARA ALTERNAR","APARÊNCIA LIVRE  ·  TODAS AS ORIGENS PODEM OUVIR O ECO");
    }
}
