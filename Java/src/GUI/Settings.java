package GUI;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class Settings extends JPanel{
	JComponent listener = null;
	
	/**Лист всех настроек*/
	List<SettingsSlider> listFields;
	
	/**
	 * Create the panel.
	 */
	public Settings() {
		setName("Settings");
		setLayout(new BorderLayout(0, 0));
		construct();
	}
	
	/**Создаёт панель настроек со всем необходимым*/
	private void construct() {
		removeAll();
		//add(makeParamsPanel());
	}
	
	private JScrollPane makeParamsPanel() {
		/*final var panelConstant = new JPanel();
		panelConstant.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), Configurations.getHProperty(Settings.class,"mainPanel"), TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		listFields = new ArrayList<>();

		listFields.add(new Slider("constSun", 1, Configurations.DBASE_SUN_POWER, 200, 1,Configurations.BASE_SUN_POWER,  null, e -> Configurations.setBASE_SUN_POWER(e)));
		listFields.add(new Slider("scrollSun", 0, Configurations.DADD_SUN_POWER, 200, 0,Configurations.ADD_SUN_POWER, null, e -> Configurations.setADD_SUN_POWER(e)));
		listFields.add(new Slider("sunSize", 
				1, Configurations.DSUN_LENGHT, Configurations.MAP_CELLS.width / 2,
				1, Configurations.SUN_LENGHT, Configurations.MAP_CELLS.width / 2, e -> Configurations.setSUN_LENGHT(e)));
		listFields.add(new Slider("sunSpeed", -200, Configurations.DSUN_SPEED, 200, Integer.MIN_VALUE, Configurations.SUN_SPEED, Integer.MAX_VALUE, e -> Configurations.setSUN_SPEED(e)));
		listFields.add(new Slider("dirtiness",
				0, Configurations.DDIRTY_WATER, 100,
				0, Configurations.DIRTY_WATER, null, e -> Configurations.setDIRTY_WATER(e)));
		
		listFields.add(new Slider("mineralHeight",
				0, (int) ((1 - Configurations.DLEVEL_MINERAL) * 100), 100,
				0, (int) ((1 - Configurations.LEVEL_MINERAL) * 100), null, e -> Configurations.setLEVEL_MINERAL(1 - e/100d)));
		listFields.add(new Slider("mineralСoncentration", 0, Configurations.DCONCENTRATION_MINERAL, 400, 0,Configurations.CONCENTRATION_MINERAL, null, e -> Configurations.setCONCENTRATION_MINERAL(e)));
		
		listFields.add( new Slider("timeLifeOrg", 1, Configurations.DTIK_TO_EXIT, 100, 1, Configurations.TIK_TO_EXIT,null, e -> {
			Configurations.TIK_TO_EXIT = e;
		}));
		
		listFields.add(new Slider("mutagenicity",
				0, Configurations.DAGGRESSIVE_ENVIRONMENT, 100,
				0, Configurations.AGGRESSIVE_ENVIRONMENT, 100, e -> Configurations.AGGRESSIVE_ENVIRONMENT = e));
		
		listFields.add( new Slider("sleepFrame", 0, 0, 5, 0, World.msTimeout,null, e -> {
				World.msTimeout = e;
		}));
		
		listFields.add(scale = new Slider("scale", 1,1, 100, 1, 1,null, e -> {
			if(listener != null) {
				listener.dispatchEvent(new ComponentEvent(listener, ComponentEvent.COMPONENT_RESIZED));
				Configurations.world.dispatchEvent(new ComponentEvent(Configurations.world, ComponentEvent.COMPONENT_RESIZED));
			}
		}));
		
		listFields.add( new Slider("sunForm", -50, Configurations.DSUN_FORM, 50, null, Configurations.SUN_FORM,null, e -> Configurations.setSUN_FORM(e)));
		

		GroupLayout gl_panel_const = new GroupLayout(panelConstant);
		var hGroupe = gl_panel_const.createParallelGroup(Alignment.TRAILING);
		for(var i : listFields) {
			hGroupe.addComponent(i, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE);
		}
		gl_panel_const.setHorizontalGroup(
			gl_panel_const.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_const.createSequentialGroup()
					.addContainerGap()
					.addGroup(hGroupe)
					.addContainerGap())
		);
		var wGroupe = gl_panel_const.createSequentialGroup();
		for(var i : listFields) {
			wGroupe.addComponent(i, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addPreferredGap(ComponentPlacement.RELATED);
		}
		
		gl_panel_const.setVerticalGroup(
			gl_panel_const.createParallelGroup(Alignment.LEADING)
				.addGroup(wGroupe)
		);
		panelConstant.setLayout(gl_panel_const);
	
		final var scroll = new JScrollPane(panelConstant);
		scroll.setBorder(null);
		return scroll;*/ throw new AssertionError();
	}

	public void setListener(JComponent scrollPane) {
		listener = scrollPane;
	}
}
