package ruptura.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;
import ruptura.RupturaGame;
import ruptura.render.Atmosphere;
import ruptura.render.WorldRenderer;
import ruptura.ui.SettingsPanel;
import ruptura.ui.Ui;

public final class MenuScreen extends BaseScreen {
    private boolean settings,credits;
    private String error="";
    public MenuScreen(RupturaGame game) { super(game); }
    @Override protected void draw(float delta) {
        Atmosphere.draw(ui,game.time,game.settings.reducedMotion);
        WorldRenderer r=game.worldRenderer;
        r.setProjection(new Matrix4(viewport.getCamera().combined).translate(915,205,0).scale(1.10f,1.10f,1));
        r.begin(game.time,game.settings.reducedMotion);
        for(int depth=18;depth>=0;depth--)for(int x=0;x<11;x++){int y=depth-x;if(y<0||y>8)continue;
            boolean edge=x==0||y==0||x==10||y==8;
            r.tile(x,y,edge?3:0,edge?1:0,false,false);
        }
        for(int depth=18;depth>=0;depth--)for(int x=0;x<11;x++){int y=depth-x;if(y<0||y>8)continue;
            if((y==7&&x%3==1)||(x==1&&y%3==0))r.prop(x,y,1,game.time);
            if(x==7&&y==5)r.prop(x,y,5,game.time);
            if(x==6&&y==1)r.prop(x,y,2,game.time);
            if(x==3&&y==5)r.prop(x,y,3,game.time);
            if(x==7&&y==7)r.prop(x,y,6,game.time);
            if(x==4&&y==3)r.actor(x,y,"hero",true,0,0,0,game.time,false,1);
            if(x==5&&y==4)r.actor(x,y,"lia",true,0,0,0,game.time,false,1);
        }
        r.end();
        ui.rect(0,0,560,800,new com.badlogic.gdx.graphics.Color(.025f,.045f,.07f,.83f));
        ui.text(ui.mono,"UM SINAL AINDA ATRAVESSA O SILÊNCIO",66,711,Ui.CYAN);
        ui.text(ui.title,"RUPTURA",58,660,Ui.TEXT);
        ui.text(ui.heading,"E C O S   D O   V A Z I O",65,546,Ui.CYAN);
        ui.rect(66,489,37,3,Ui.ORANGE);
        ui.wrap(ui.body,"O mundo acabou.\nNem todas as vozes se calaram.",66,464,390,Ui.MUTED);
        ui.button("01    INICIAR NOVA HISTÓRIA",66,323,376,55,true,()->game.open(new CharacterScreen(game)));
        ui.button("02    CONTINUAR",66,254,376,55,false,game.saves.exists(),()->{
            var save=game.saves.load();
            if(save.isPresent())game.open(new AdventureScreen(game,save.get(),false));
            else error="Não foi possível ler o salvamento. "+game.saves.lastError();
        });
        ui.button("03    CONFIGURAÇÕES",66,195,376,45,false,()->settings=true);
        ui.button("CRÉDITOS",66,132,180,42,false,()->credits=true);
        ui.button("SAIR",260,132,182,42,false,()->Gdx.app.exit());
        ui.text(ui.mono,"ASSENTAMENTO AURORA",812,183,Ui.TEXT);
        ui.text(ui.mono,"20:41  /  12 ANOS APÓS A RUPTURA",812,159,Ui.MUTED);
        ui.text(ui.mono,"+",1170,240,Ui.CYAN);
        if(!error.isEmpty())ui.wrap(ui.small,error,66,110,450,Ui.RED);
        footer("VERSÃO 0.1.0  /  PRIMEIRO ECO","RPG TÁTICO  ·  EXPERIÊNCIA OFFLINE");
        if(settings)SettingsPanel.draw(game,()->settings=false);
        if(credits) {
            ui.modal();
            ui.shade(.92f);ui.panel(340,230,600,370);
            ui.text(ui.heading,"Feito para escutar o impossível",375,555,Ui.TEXT);
            ui.wrap(ui.body,"RUPTURA: ECOS DO VAZIO\nUma criação original. Protótipo em Java e LibGDX.\n\nArte geométrica e áudio sintetizado originais.\nFontes Barlow e IBM Plex Mono sob licença OFL.\nMotor LibGDX sob Apache 2.0.",375,490,525,Ui.MUTED);
            ui.button("Voltar",375,255,530,45,true,()->credits=false);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){settings=false;credits=false;}
    }
}
