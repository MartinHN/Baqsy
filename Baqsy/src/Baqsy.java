import baqsi.controllers.Controllers;
import baqsi.model.Model;
import baqsi.ui.Window;


// pentatonicité diatonicité zazalité
// double?
public class Baqsy {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Model m = new Model();

		Controllers c = new Controllers(m);

		Window w = new Window(c);

		m.registerControllers(c);
		c.registerWindow(w);
		c.initvalues();

		w.setVisible(true);

	}
}
