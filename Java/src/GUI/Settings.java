/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import Calculations.Gravitation;
import Calculations.Point;
import Calculations.Trajectory;
import MapObjects.CellObject;
import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

/**
 *
 * @author Kerravitarr
 */
public class Settings extends javax.swing.JPanel {

	/** Creates new form Settings */
	public Settings() {
		initComponents();
		//Инициализируем отображение бордюров
		borderClick(configuationsNorm, null);
		borderClick(configuationsRebuild, null);
		borderClick(gravitations, null);
		borderClick(suns, null);
		borderClick(suns2, null);
		borderClick(streams, null);
		borderClick(minerals, null);
		
		rebuild();	
	}
	/**Пересоздаёт все ползунки*/
	public void rebuild(){
		blinks.clear();
		rebuildConfig();
		rebuildGravitation();
		rebuildSuns();
		rebuildMinerals();
		rebuildStreams();
	}
	/**Режим работы настреок
	 * @return true - тогда мы в режиме редактора карты. Не надо отображать клетки, инфу по боту и остальное
	 */
	public boolean isEdit(){
		return tableLists.getSelectedIndex() == 1;
	}
	
	/**Пересоздаёт конфигурацию мира*/
	private void rebuildConfig(){
		configuationsNorm.removeAll();
		configuationsRebuild.removeAll();
			
		final var dc = Configurations.getDefaultConfiguration(Configurations.confoguration.world_type);
		final var size = new SettingsPoint("configuations.size", 
				100, dc.MAP_CELLS.width, 1_000_000,Configurations.getWidth(),
				100, dc.MAP_CELLS.height, 1_000_000,Configurations.getHeight(),  e -> {
			Configurations.world.awaitStop();
			Configurations.rebuildMap(new Configurations(Configurations.confoguration,Configurations.confoguration.world_type, e.x, e.y));
			final var w = Configurations.getViewer().get(WorldView.class);
			w.dispatchEvent(new ComponentEvent(w, ComponentEvent.COMPONENT_RESIZED));
		});
	
		final var wt = new SettingsSelect<>("configuations.WORLD_TYPE", Configurations.WORLD_TYPE.values, Configurations.WORLD_TYPE.LINE_H, Configurations.confoguration.world_type, e -> {
			Configurations.world.awaitStop();
			Configurations.rebuildMap(new Configurations(Configurations.confoguration,e,Configurations.getWidth() , Configurations.getHeight()));
			rebuild();
			updateUI();
			final var w = Configurations.getViewer().get(WorldView.class);
			w.dispatchEvent(new ComponentEvent(w, ComponentEvent.COMPONENT_RESIZED));
		});
		
		size.setBackground(Color.red);
		configuationsRebuild.add(size);
		wt.setBackground(Color.red);
		configuationsRebuild.add(wt);
		//========================================================
		
		configuationsNorm.add(new javax.swing.JLabel(Configurations.getProperty(Settings.class, "worldSize",Configurations.getWidth(),Configurations.getHeight())));
		configuationsNorm.add(new javax.swing.JLabel(Configurations.getProperty(Settings.class, "worldType",Configurations.confoguration.world_type)));
		configuationsNorm.add(new SettingsSlider("configuations.speed", 0, 0, 1000, 0,(int)Configurations.world.getSpeed(),null, e -> Configurations.world.setSpeed(e)));
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
		
		//А теперь мы обновляем состояние ячеек
		borderClick(configuationsNorm, null);
		borderClick(configuationsNorm, null);
		borderClick(configuationsRebuild, null);
		borderClick(configuationsRebuild, null);
	}
	/**Пересоздаёт гравитацию*/
	private void rebuildGravitation(){
		gravitations.removeAll();
		for (int i = 0; i < CellObject.LV_STATUS.length; i++) {
			final var status = CellObject.LV_STATUS.values[i];
			if(status == CellObject.LV_STATUS.GHOST) continue;
			final var buildGrav = Configurations.gravitation[i];
			final var defDir = switch (Configurations.confoguration.world_type) {
				case LINE_H,LINE_V,RECTANGLE -> Gravitation.Direction.DOWN;
				default -> throw new AssertionError();
			};
			final var defPower = switch (Configurations.confoguration.world_type) {
				case LINE_H -> 2;
				case LINE_V -> 100;
				case RECTANGLE -> 1000;
				default -> throw new AssertionError();
			};
			//Немного говнокода вам в копилочку.
			//Нам-же нельзя работать с объектом до его объявления
			//Но объявить можно только финальный объект
			//Ну значит будет у нас... Вот такая вот шляпа :)
			final var sliders = new javax.swing.JPanel[3];
			//Мощность гравитации
			sliders[0] = new SettingsSlider("gravitation." + status.name(), 0, defPower, 1000, 0, buildGrav.getValue(),null,e -> {
				final var g = Configurations.gravitation[status.ordinal()];
				final var direction = (SettingsSelect<Gravitation.Direction>)sliders[1];
				final var toPoint = (SettingsPoint)sliders[2];
				if (e == 0){
					Configurations.gravitation[status.ordinal()] = new Gravitation();
					direction.setVisible(false);
					toPoint.setVisible(false);
				} if(g.getDirection() == Gravitation.Direction.TO_POINT) {
					Configurations.gravitation[status.ordinal()] = new Gravitation(e,Point.create(toPoint.getValue().x,toPoint.getValue().y));
				} else if(g.getDirection() != Gravitation.Direction.NONE) {
					Configurations.gravitation[status.ordinal()] = new Gravitation(e,g.getDirection());
				} else {
					Configurations.gravitation[status.ordinal()] = new Gravitation(e,defDir);
					direction.setVisible(true);
					((SettingsSelect<Gravitation.Direction>)direction).setValue(defDir);
				}
			});
			//Направление гравитации
			sliders[1] = new SettingsSelect<>("gravitation.dir", Gravitation.Direction.values, defDir, buildGrav.getDirection(),e -> {
				final var g = Configurations.gravitation[status.ordinal()];
				final var power = (SettingsSlider) sliders[0];
				final var direction = (SettingsSelect<Gravitation.Direction>) sliders[1];
				final var toPoint = (SettingsPoint)sliders[2];
				switch (e) {
					case NONE -> {
						direction.setVisible(false);
						toPoint.setVisible(false);
						Configurations.gravitation[status.ordinal()] = new Gravitation();
					}
					case TO_POINT -> {
						toPoint.setVisible(true);
						final var p = g.getDirection() != Gravitation.Direction.NONE ? g.getValue() : defPower;
						Configurations.gravitation[status.ordinal()] = new Gravitation(p, Point.create(toPoint.getValue().x,toPoint.getValue().y));
						power.setValue(p);
					}
					default -> {
						toPoint.setVisible(false);
						final var p = g.getDirection() != Gravitation.Direction.NONE ? g.getValue() : defPower;
						Configurations.gravitation[status.ordinal()] = new Gravitation(p, e);
						power.setValue(p);
					}
				}
			});
			//Точка, к которой гравитация стремиться
			sliders[2] = new SettingsPoint("gravitation.point", 
					0, Configurations.getWidth()/2, Configurations.getWidth(),buildGrav.getPoint() == null ? Configurations.getWidth()/2 : buildGrav.getPoint().getX(),
					0, Configurations.getHeight()/2, Configurations.getHeight(),buildGrav.getPoint() == null ? Configurations.getHeight()/2 : buildGrav.getPoint().getY(),  e -> {
				final var g = Configurations.gravitation[status.ordinal()];
				Configurations.gravitation[status.ordinal()] = new Gravitation(g.getValue(), Point.create(e.x,e.y));
			});
			gravitations.add(sliders[0]);
			gravitations.add(sliders[1]);
			gravitations.add(sliders[2]);
		}
		borderClick(gravitations, null);
		borderClick(gravitations, null);
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
			final var tr = sun.getTrajectory();
			if(!tr.getClass().equals(Trajectory.class)){
				suns.add(new SettingsSlider("trajectory.speed", 0,(int)tr.getSpeed(),1000,0,(int)tr.getSpeed(),null, e -> {
					tr.setSpeed(e);
				}));
			}
		}
		borderClick(suns, null);
		borderClick(suns, null);
		
		suns2.removeAll();
		
		for (int i = 0; i < Configurations.suns.size(); i++) {
			if(i > 0)
				suns2.add(new JPopupMenu.Separator());
			final var sun = Configurations.suns.get(i);
			suns2.add(new SettingsString("object.editname", "Звезда", sun.toString(), e -> sun.setName(e)));
			//Подсветить звезду
			suns2.add(addBlink(sun.getSelect(), e->sun.setSelect(e)));
			//Задать новую траекторию
			//Изменить траекторию
			//Задать новую звезду
			//Удалить звезду
			//final var tr = sun.getTrajectory();
			
		}
		borderClick(suns2, null);
		borderClick(suns2, null);
	}
	/**Пересоздаёт минералы*/
	private void rebuildMinerals(){
		minerals.removeAll();
		final var dc = Configurations.getDefaultConfiguration(Configurations.confoguration.world_type);
		for (int i = 0; i < Configurations.minerals.size(); i++) {
			if(i > 0)
				minerals.add(new JPopupMenu.Separator());
			final var mineral = Configurations.minerals.get(i);
			minerals.add(new javax.swing.JLabel(Configurations.getProperty(Settings.class, "object.name",mineral.toString())));
			minerals.add(new SettingsSlider("minerals.power", 1, 20, 200, 1,(int)mineral.getPower(),  null, e -> {
				mineral.setPower(e);
			}));
			minerals.add(new SettingsSlider("minerals.attenuation",
				0, (int)(dc.DIRTY_WATER * 100), 1000,
				0, (int)(mineral.getAttenuation() * 100), null, e -> mineral.setAttenuation(e / 100d)));
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
			final var tr = mineral.getTrajectory();
			if(!tr.getClass().equals(Trajectory.class)){
				minerals.add(new SettingsSlider("trajectory.speed", 0,(int)tr.getSpeed(),1000,0,(int)tr.getSpeed(),null, e -> {
					tr.setSpeed(e);
				}));
			}
		}
		borderClick(minerals, null);
		borderClick(minerals, null);
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
			final var tr = stream.getTrajectory();
			if(!tr.getClass().equals(Trajectory.class)){
				streams.add(new SettingsSlider("trajectory.speed", 0,(int)tr.getSpeed(),1000,0,(int)tr.getSpeed(),null, e -> {
					tr.setSpeed(e);
				}));
			}
		}
		borderClick(streams, null);
		borderClick(streams, null);
	}
	/**Добавляет кнопку мигания
	 * @param nowValue текущее состояние объекта
	 * @param doing что нужно сделать при включении (выключении) мигания?
	 * @return панель с кнопкой мигания
	 */
	private javax.swing.JPanel addBlink(boolean nowValue, SettingsBoolean.AdjustmentListener doing){
		final var panels = new SettingsBoolean[1];
		panels[0] = new SettingsBoolean("object.blink", nowValue, e -> {
			if(e == true){
				for(final var i : blinks){
					if(panels[0] != i)
						i.setValue(false);
				}
			}
			panels[0].setValue(e);
			doing.adjustmentValueChanged(e);
		});
		blinks.add(panels[0]);
		return panels[0];
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
        suns2 = new javax.swing.JPanel();

        setToolTipText(Configurations.getProperty(Settings.class,"toolTop"));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        scroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        tableLists.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableListsMouseClicked(evt);
            }
        });

        configuationsNorm.setBackground(new java.awt.Color(102, 255, 102));
        configuationsNorm.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Configurations.getProperty(Settings.class,"configurations"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        configuationsNorm.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                configuationsNormMouseClicked(evt);
            }
        });
        configuationsNorm.setLayout(new javax.swing.BoxLayout(configuationsNorm, javax.swing.BoxLayout.Y_AXIS));

        gravitations.setBackground(new java.awt.Color(204, 204, 204));
        gravitations.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Configurations.getProperty(Settings.class,"gravitations"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        gravitations.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gravitationsMouseClicked(evt);
            }
        });
        gravitations.setLayout(new javax.swing.BoxLayout(gravitations, javax.swing.BoxLayout.Y_AXIS));

        suns.setBackground(new java.awt.Color(255, 204, 51));
        suns.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Configurations.getProperty(Settings.class,"suns"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        suns.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sunsMouseClicked(evt);
            }
        });
        suns.setLayout(new javax.swing.BoxLayout(suns, javax.swing.BoxLayout.Y_AXIS));

        streams.setBackground(new java.awt.Color(204, 204, 204));
        streams.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Configurations.getProperty(Settings.class,"streams"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        streams.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                streamsMouseClicked(evt);
            }
        });
        streams.setLayout(new javax.swing.BoxLayout(streams, javax.swing.BoxLayout.Y_AXIS));

        minerals.setBackground(new java.awt.Color(237, 255, 33));
        minerals.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Configurations.getProperty(Settings.class,"minerals"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        minerals.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mineralsMouseClicked(evt);
            }
        });
        minerals.setLayout(new javax.swing.BoxLayout(minerals, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(configuationsNorm, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
            .addComponent(gravitations, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(suns, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(streams, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(minerals, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(configuationsNorm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gravitations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(suns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(streams, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minerals, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tableLists.addTab(Configurations.getProperty(Settings.class,"edit.name"), null, jPanel1, Configurations.getProperty(Settings.class,"edit.toolTip"));

        configuationsRebuild.setBackground(new java.awt.Color(204, 204, 204));
        configuationsRebuild.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Configurations.getProperty(Settings.class,"configurations"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        configuationsRebuild.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                configuationsRebuildMouseClicked(evt);
            }
        });
        configuationsRebuild.setLayout(new javax.swing.BoxLayout(configuationsRebuild, javax.swing.BoxLayout.Y_AXIS));

        suns2.setBackground(new java.awt.Color(255, 204, 51));
        suns2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Configurations.getProperty(Settings.class,"suns2"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        suns2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                suns2MouseClicked(evt);
            }
        });
        suns2.setLayout(new javax.swing.BoxLayout(suns2, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(configuationsRebuild, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
            .addComponent(suns2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(configuationsRebuild, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(suns2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        tableLists.addTab(Configurations.getProperty(Settings.class,"rebuild.name"), null, jPanel2, Configurations.getProperty(Settings.class,"rebuild.tooltip"));

        javax.swing.GroupLayout jPanelLayout = new javax.swing.GroupLayout(jPanel);
        jPanel.setLayout(jPanelLayout);
        jPanelLayout.setHorizontalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tableLists)
        );
        jPanelLayout.setVerticalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLayout.createSequentialGroup()
                .addComponent(tableLists, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        tableLists.getAccessibleContext().setAccessibleDescription("");

        scroll.setViewportView(jPanel);

        add(scroll);
    }// </editor-fold>//GEN-END:initComponents

    private void tableListsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableListsMouseClicked
        if(tableLists.getSelectedIndex() == 1){
			Configurations.world.awaitStop();
			if(isNeedWarning){
				final var checkbox = new javax.swing.JCheckBox(Configurations.getProperty(Settings.class, "hideWarning"));
				String message = Configurations.getProperty(Settings.class, "warningText");
				Object[] params = {message, checkbox};
				JOptionPane.showConfirmDialog(this, params, "BioLife", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
				isNeedWarning = !checkbox.isSelected();
			}
		} else {
			//Сбрасываем выделение
			for(final var i : blinks){
				i.setValue(false);
			}
		}
    }//GEN-LAST:event_tableListsMouseClicked

    private void configuationsRebuildMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_configuationsRebuildMouseClicked
        borderClick(configuationsRebuild, evt);
    }//GEN-LAST:event_configuationsRebuildMouseClicked

    private void mineralsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mineralsMouseClicked
        borderClick(minerals, evt);
    }//GEN-LAST:event_mineralsMouseClicked

    private void streamsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_streamsMouseClicked
        borderClick(streams, evt);
    }//GEN-LAST:event_streamsMouseClicked

    private void sunsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sunsMouseClicked
        borderClick(suns, evt);
    }//GEN-LAST:event_sunsMouseClicked

    private void gravitationsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gravitationsMouseClicked
        borderClick(gravitations, evt);
    }//GEN-LAST:event_gravitationsMouseClicked

    private void configuationsNormMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_configuationsNormMouseClicked
        borderClick(configuationsNorm, evt);
    }//GEN-LAST:event_configuationsNormMouseClicked

    private void suns2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_suns2MouseClicked
        borderClick(suns2, evt);
    }//GEN-LAST:event_suns2MouseClicked
	/**Обрабатывает событие нажатия на рамку параметра
	 * @param panel панель, рамку которой нажали
	 * @param evt событие нажатия. Если тут будет null, то это тоже будет означать нажатие
	 */
	private void borderClick(javax.swing.JPanel panel, java.awt.event.MouseEvent evt){
		final var border = panel.getBorder();
		if (border instanceof javax.swing.border.TitledBorder tb) {
			final var fm = panel.getFontMetrics(panel.getFont());
			final var title = tb.getTitle();
			final var bounds = new java.awt.Rectangle(0, 0, panel.getWidth(), fm.getHeight());
			if(evt == null || bounds.contains(evt.getPoint())){
				if(title.contains("\\/")) {
					tb.setTitle(title.replace("\\/", "/\\"));
					if (panel == gravitations) { //Сравниваем именно указатели!
						boolean isVisiblePoint = false;
						for (final var c : panel.getComponents()) {
							if (c instanceof SettingsSelect<?>) {
								final var settingsSelect = (SettingsSelect<Gravitation.Direction>) c;
								c.setVisible(settingsSelect.getValue() != Gravitation.Direction.NONE);
								isVisiblePoint = settingsSelect.getValue() == Gravitation.Direction.TO_POINT;
							} else if (c instanceof SettingsPoint sp) {
								sp.setVisible(isVisiblePoint);
							} else {
								c.setVisible(true);
							}
						}
					} else {
						for (final var c : panel.getComponents()) {
							c.setVisible(true);
						}
					}
				} else {
					if(title.contains("/\\")) tb.setTitle(title.replace("/\\", "\\/"));
					else						tb.setTitle(title + "  \\/");
					for (final var c : panel.getComponents()) {
						c.setVisible(false);
					}
				}
				panel.updateUI();
			}
		}
	}

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
    private javax.swing.JPanel suns2;
    private javax.swing.JTabbedPane tableLists;
    // End of variables declaration//GEN-END:variables
	/**Нужна печать предупреждения при редактировании карты?*/
	private boolean isNeedWarning = true;
	/**Список всех подсвеченных объектов. Нужен для того, чтобы подсвечивался только один объект*/
	private ArrayList<SettingsBoolean> blinks = new ArrayList<>();
}
