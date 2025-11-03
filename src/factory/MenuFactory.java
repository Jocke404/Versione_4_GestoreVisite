package src.factory;

import src.view.Menu;
import src.view.MenuVolontario;
import src.view.MenuConfiguratore;
import src.view.MenuFruitore;
import src.controller.VolontariController;
import src.controller.ConfiguratoriController;
import src.controller.FruitoreController;

public class MenuFactory {

    public Menu creaMenuConfiguratore(ConfiguratoriController configuratoriController) {
        return new MenuConfiguratore(configuratoriController);
    }

    public Menu creaMenuVolontario(VolontariController volontariController) {
        return new MenuVolontario(volontariController);
    }
    
    public Menu creaMenuFruitore(FruitoreController fruitoreController) {
        return new MenuFruitore(fruitoreController);
    }
}