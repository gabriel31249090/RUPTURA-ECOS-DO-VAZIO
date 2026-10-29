package ruptura.ui;

import com.badlogic.gdx.Gdx;
import ruptura.RupturaGame;

public final class SettingsPanel {
    private static String error="";
    private SettingsPanel() {}
    public static void draw(RupturaGame game,Runnable close) {
        Ui ui=game.ui; ui.modal(); ui.shade(.88f); ui.panel(350,155,580,500);
        ui.text(ui.mono,"SISTEMA / PREFERÊNCIAS",382,620,Ui.CYAN);
        ui.text(ui.heading,"Ajuste a sua experiência",382,588,Ui.TEXT);
        ui.text(ui.small,"Volume ambiente e efeitos",382,524,Ui.MUTED);
        ui.button("−",690,490,48,42,false,()->game.settings.volume=Math.max(0,game.settings.volume-.1f));
        ui.center(ui.mono,Math.round(game.settings.volume*100)+"%",748,517,60,Ui.CYAN);
        ui.button("+",823,490,48,42,false,()->game.settings.volume=Math.min(1,game.settings.volume+.1f));
        ui.text(ui.small,"Movimento e flashes",382,453,Ui.MUTED);
        ui.button(game.settings.reducedMotion?"Reduzidos":"Completos",690,418,181,44,false,()->game.settings.reducedMotion=!game.settings.reducedMotion);
        ui.text(ui.small,"Modo de exibição",382,381,Ui.MUTED);
        ui.button(game.settings.fullscreen?"Tela cheia":"Janela · 1280 × 800",690,346,181,44,false,()->{
            game.settings.fullscreen=!game.settings.fullscreen;
            if(game.settings.fullscreen)Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            else Gdx.graphics.setWindowedMode(1280,800);
        });
        ui.text(ui.small,"Velocidade do diálogo",382,309,Ui.MUTED);
        ui.button(game.settings.textSpeed<45?"Natural":game.settings.textSpeed<80?"Rápida":"Muito rápida",690,274,181,44,false,()->{
            game.settings.textSpeed=game.settings.textSpeed<45?65:game.settings.textSpeed<80?180:35;
        });
        if(!error.isEmpty())ui.wrap(ui.small,error,382,263,489,Ui.RED);
        ui.button("Salvar e voltar",382,185,489,48,true,()->{
            try{game.saves.saveSettings(game.settings);error="";close.run();}
            catch(RuntimeException ex){error="Não foi possível gravar as configurações.";}
        });
    }
}
