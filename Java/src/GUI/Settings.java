/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import Calculations.Gravitation;
import Calculations.Point;
import Calculations.SunAbstract;
import MapObjects.CellObject;
import Utils.RingBuffer;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JPopupMenu;

/**
 *
 * @author Kerravitarr
 */
public class Settings extends javax.swing.JPanel {

	/** Creates new form Settings */
	public Settings() {
		initComponents();
		rebuild();		
	}
	/**Пересоздаёт все ползунки*/
	public void rebuild(){
		rebuildConfig();
		rebuildGravitation();
		rebuildSuns();
		rebuildMinerals();
		rebuildStreams();
	}
	/**Пересоздаёт конфигурацию мира*/
	private void rebuildConfig(){
		configuationsNorm.removeAll();
		configuationsRebuild.removeAll();
		
		configuationsNorm.add(new javax.swing.JLabel(Configurations.getProperty(Settings.class, "worldSize",Configurations.getWidth(),Configurations.getHeight())));
		configuationsNorm.add(new javax.swing.JLabel(Configurations.getProperty(Settings.class, "worldType",Configurations.confoguration.world_type)));

				
		final var dc = Configurations.getDefaultConfiguration(Configurations.confoguration.world_type);
		configuationsRebuild.add(new SettingsNumber("configuations.width", 100, dc.MAP_CELLS.width, 1_000_000, Configurations.getWidth(), e -> {
			Configurations.world.awaitStop();
			Configurations.rebuildMap(new Configurations(Configurations.confoguration,Configurations.confoguration.world_type, e, Configurations.getHeight()));
			final var w = Configurations.getViewer().get(WorldView.class);
			w.dispatchEvent(new ComponentEvent(w, ComponentEvent.COMPONENT_RESIZED));
		}));
		configuationsRebuild.add(new SettingsNumber("configuations.height", 100, dc.MAP_CELLS.height, 1_000_000, Configurations.getHeight(), e -> {
			Configurations.world.awaitStop();
			Configurations.rebuildMap(new Configurations(Configurations.confoguration,Configurations.confoguration.world_type,Configurations.getWidth() , e));
			final var w = Configurations.getViewer().get(WorldView.class);
			w.dispatchEvent(new ComponentEvent(w, ComponentEvent.COMPONENT_RESIZED));
		}));
		configuationsRebuild.add(new SettingsSelect<>("configuations.WORLD_TYPE", Configurations.WORLD_TYPE.values, Configurations.WORLD_TYPE.LINE_H, Configurations.confoguration.world_type, e -> {
			Configurations.world.awaitStop();
			Configurations.rebuildMap(new Configurations(Configurations.confoguration,e,Configurations.getWidth() , Configurations.getHeight()));
			rebuild();
			updateUI();
			final var w = Configurations.getViewer().get(WorldView.class);
			w.dispatchEvent(new ComponentEvent(w, ComponentEvent.COMPONENT_RESIZED));
		}));
		//WORLD_TYPE.setEnabled(false);
		configuationsNorm.add(new JPopupMenu.Separator());
		configuationsNorm.add(new SettingsNumber("configuations.savePeriod", 1_000, (int)dc.SAVE_PERIOD, 10_000_000, (int)Configurations.confoguration.SAVE_PERIOD, e -> Configurations.confoguration.SAVE_PERIOD = e));
		configuationsNorm.add(new SettingsNumber("configuations.countSave", 1, dc.COUNT_SAVE, 10, Configurations.confoguration.COUNT_SAVE, e -> Configurations.confoguration.COUNT_SAVE = e));
		configuationsNorm.add(new JPopupMenu.Separator());
		configuationsNorm.add(new SettingsSlider("configuations.mutagenicity",
				0, dc.AGGRESSIVE_ENVIRONMENT, 100,
				0, Configurations.confoguration.AGGRESSIVE_ENVIRONMENT, 100, e -> Configurations.confoguration.AGGRESSIVE_ENVIRONMENT = e));
		configuationsNorm.add( new SettingsSlider("configuations.timeLifeOrg", 0, dc.TIK_TO_EXIT, 100, 0, Configurations.confoguration.TIK_TO_EXIT,null, e -> Configurations.confoguration.TIK_TO_EXIT = e));
		configuationsNorm.add(new SettingsSlider("configuations.dirtiness",
				0, (int)(dc.DIRTY_WATER * 100), 1000,
				0, (int)(Configurations.confoguration.DIRTY_WATER * 100), null, e -> Configurations.confoguration.DIRTY_WATER = e / 100d));
	}
	/**Пересоздаёт гравитацию*/
	private void rebuildGravitation(){
		gravitations.removeAll();
		for (int i = 0; i < CellObject.LV_STATUS.length; i++) {
			final var status = CellObject.LV_STATUS.values[i];
			if(status == CellObject.LV_STATUS.GHOST) continue;
			final var buildGrav = Configurations.gravitation[i];
			final var defDir = switch (Configurations.confoguration.world_type) {
				case LINE_H,LINE_V -> Gravitation.Direction.DOWN;
				default -> throw new AssertionError();
			};
			final var defPower = switch (Configurations.confoguration.world_type) {
				case LINE_H -> 2;
				case LINE_V -> 100;
				default -> throw new AssertionError();
			};
			//Немного говнокода вам в копилочку.
			//Нам-же нельзя работать с объектом до его объявления
			//Но объявить можно только финальный объект
			//Ну значит будет у нас... Вот такая вот шляпа :)
			final var sliders = new javax.swing.JPanel[3];
			sliders[0] = new SettingsSlider("gravitation." + status.name(), 0, defPower, 1000, 0, buildGrav.getValue(),null,e -> {
				final var g = Configurations.gravitation[status.ordinal()];
				if (sliders[1] instanceof SettingsSelect<?> direction) {
					if (e == 0){
						Configurations.gravitation[status.ordinal()] = new Gravitation();
						direction.setVisible(false);
					} else if(g.getDirection() != Gravitation.Direction.NONE) {
						Configurations.gravitation[status.ordinal()] = new Gravitation(e,g.getDirection());
						direction.setVisible(true);
						((SettingsSelect<Gravitation.Direction>)direction).setValue(g.getDirection());
					} else {
						Configurations.gravitation[status.ordinal()] = new Gravitation(e,defDir);
						direction.setVisible(true);
						((SettingsSelect<Gravitation.Direction>)direction).setValue(defDir);
					}
				}
			});
			sliders[1] = new SettingsSelect<>("gravitation.dir", Gravitation.Direction.values, defDir, buildGrav.getDirection(),e -> {
				final var g = Configurations.gravitation[status.ordinal()];
				if (sliders[0] instanceof SettingsSlider power) {
					switch (e) {
						case NONE -> {
							Configurations.gravitation[status.ordinal()] = new Gravitation();
							if (sliders[1] instanceof SettingsSelect<?> direction) {
								direction.setVisible(false);
							}
						}
						case TO_POINT -> {
							power.setVisible(true);
							final var p = g.getDirection() != Gravitation.Direction.NONE ? g.getValue() : defPower;
							Configurations.gravitation[status.ordinal()] = new Gravitation(p, new Point(Configurations.getWidth()/2, Configurations.getHeight() / 2));
							power.setValue(defPower);
						}
						default -> {
							power.setVisible(true);
							final var p = g.getDirection() != Gravitation.Direction.NONE ? g.getValue() : defPower;
							Configurations.gravitation[status.ordinal()] = new Gravitation(p, e);
							power.setValue(p);
						}
					}
				}
			});
			sliders[1].setVisible(buildGrav.getDirection() != Gravitation.Direction.NONE);
			gravitations.add(sliders[0]);
			gravitations.add(sliders[1]);
		}
	}
	/**Пересоздаёт звёзды*/
	private void rebuildSuns(){
		suns.removeAll();
		for (int i = 0; i < Configurations.suns.size(); i++) {
			if(i > 0)
				suns.add(new JPopupMenu.Separator());
			
			final var sun = Configurations.suns.get(i);
			suns.add(new javax.swing.JLabel(Configurations.getProperty(Settings.class, "object.name",sun.toString())));
			suns.add(new SettingsSlider("sun.power", 1, 30, 200, 1,(int)sun.getPower(),  null, e -> {
				sun.setPower(e);
			}));
			suns.add(new SettingsBoolean("emitter.isLine", !sun.getIsLine(), e -> {
				sun.setIsLine(!e);
			}));
			for(final Calculations.ParamObject p : sun.getParams()){
				switch (p.type) {
					case INT -> {
						suns.add(new SettingsSlider(String.format("%s.%s", sun.getClass().getName(),p.name), p.minD,  p.maxD, p.minA,p.getI(),  p.maxA, e -> {
							p.setValue(e);
						}));
					}
					default -> throw new AssertionError();
				}
			}
		}
	}
	/**Пересоздаёт минералы*/
	private void rebuildMinerals(){
		minerals.removeAll();
		for (int i = 0; i < Configurations.minerals.size(); i++) {
			if(i > 0)
				minerals.add(new JPopupMenu.Separator());
			final var mineral = Configurations.minerals.get(i);
			minerals.add(new javax.swing.JLabel(Configurations.getProperty(Settings.class, "object.name",mineral.toString())));
			minerals.add(new SettingsSlider("minerals.power", 1, 20, 200, 1,(int)mineral.getPower(),  null, e -> {
				mineral.setPower(e);
			}));
			minerals.add(new SettingsBoolean("emitter.isLine", !mineral.getIsLine(), e -> {
				mineral.setIsLine(!e);
			}));
			for(final Calculations.ParamObject p : mineral.getParams()){
				switch (p.type) {
					case INT -> {
						minerals.add(new SettingsSlider(String.format("%s.%s", mineral.getClass().getName(),p.name), p.minD,  p.maxD, p.minA,p.getI(),  p.maxA, e -> {
							p.setValue(e);
						}));
					}
					default -> throw new AssertionError();
				}
			}
		}
	}
	/**Пересоздаёт потоки*/
	private void rebuildStreams(){
		streams.removeAll();
		for (int i = 0; i < Configurations.streams.size(); i++) {
			if(i > 0)
				streams.add(new JPopupMenu.Separator());
			final var stream = Configurations.streams.get(i);
			streams.add(new javax.swing.JLabel(Configurations.getProperty(Settings.class, "object.name",stream.toString())));
			
			for(final Calculations.ParamObject p : stream.getParams()){
				switch (p.type) {
					case INT -> {
						streams.add(new SettingsSlider(String.format("%s.%s", stream.getClass().getName(),p.name), p.minD,  p.maxD, p.minA,p.getI(),  p.maxA, e -> {
							p.setValue(e);
						}));
					}
					default -> throw new AssertionError();
				}
			}
		}
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scroll = new javax.swing.JScrollPane();
        jPanel = new javax.swing.JPanel();
        tableLists = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        configuationsNorm = new javax.swing.JPanel();
        gravitations = new javax.swing.JPanel();
        suns = new javax.swing.JPanel();
        streams = new javax.swing.JPanel();
        minerals = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        configuationsRebuild = new javax.swing.JPanel();

        setToolTipText(Configurations.getProperty(Settings.class,"toolTop"));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        jPanel.setLayout(new javax.swing.BoxLayout(jPanel, javax.swing.BoxLayout.Y_AXIS));

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        configuationsNorm.setBackground(new java.awt.Color(204, 204, 204));
        configuationsNorm.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Configurations.getProperty(Settings.class,"configurations"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        configuationsNorm.setLayout(new javax.swing.BoxLayout(configuationsNorm, javax.swing.BoxLayout.Y_AXIS));
        jPanel1.add(configuationsNorm);

        gravitations.setBackground(new java.awt.Color(204, 204, 204));
        gravitations.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Configurations.getProperty(Settings.class,"gravitations"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        gravitations.setLayout(new javax.swing.BoxLayout(gravitations, javax.swing.BoxLayout.Y_AXIS));
        jPanel1.add(gravitations);

        suns.setBackground(new java.awt.Color(204, 204, 204));
        suns.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Configurations.getProperty(Settings.class,"suns"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        suns.setLayout(new javax.swing.BoxLayout(suns, javax.swing.BoxLayout.Y_AXIS));
        jPanel1.add(suns);

        streams.setBackground(new java.awt.Color(204, 204, 204));
        streams.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Configurations.getProperty(Settings.class,"streams"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        streams.setLayout(new javax.swing.BoxLayout(streams, javax.swing.BoxLayout.Y_AXIS));
        jPanel1.add(streams);

        minerals.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Configurations.getProperty(Settings.class,"minerals"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        minerals.setLayout(new javax.swing.BoxLayout(minerals, javax.swing.BoxLayout.Y_AXIS));
        jPanel1.add(minerals);

        tableLists.addTab(Configurations.getProperty(Settings.class,"edit.name"), null, jPanel1, Configurations.getProperty(Settings.class,"edit.toolTip"));

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));

        configuationsRebuild.setBackground(new java.awt.Color(204, 204, 204));
        configuationsRebuild.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Configurations.getProperty(Settings.class,"configurations"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        configuationsRebuild.setLayout(new javax.swing.BoxLayout(configuationsRebuild, javax.swing.BoxLayout.Y_AXIS));
        jPanel2.add(configuationsRebuild);

        tableLists.addTab(Configurations.getProperty(Settings.class,"rebuild.name"), null, jPanel2, Configurations.getProperty(Settings.class,"rebuild.tooltip"));

        jPanel.add(tableLists);
        tableLists.getAccessibleContext().setAccessibleDescription("");

        scroll.setViewportView(jPanel);

        add(scroll);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel configuationsNorm;
    private javax.swing.JPanel configuationsRebuild;
    private javax.swing.JPanel gravitations;
    private javax.swing.JPanel jPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel minerals;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JPanel streams;
    private javax.swing.JPanel suns;
    private javax.swing.JTabbedPane tableLists;
    // End of variables declaration//GEN-END:variables

}
