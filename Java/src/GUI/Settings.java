/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import Calculations.Emitters.DefaultEmitter;
import Calculations.Emitters.EmitterSet;
import Calculations.Gravitation;
import Calculations.Emitters.MineralAbstract;
import Calculations.Point;
import Calculations.Streams.StreamAbstract;
import Calculations.Emitters.SunAbstract;
import Calculations.Trajectories.Trajectory;
import MapObjects.CellObject;
import Utils.ClassBuilder;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.Toolkit;
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
	private class AddListener implements ActionListener {
		private static interface AddNewO <T>{
			public void add(T o);
		}
	
		/**Все конструкторы. Как ни как, а мы-же всё-же слушатель кнопки Add!*/
		private final List<ClassBuilder> _constructorsList;
		/**Само событие, что что-то произошло!*/
		private final AddNewO _addEvent;
		
		
		public AddListener(List<ClassBuilder> cl, AddNewO add){_constructorsList = cl;_addEvent=add;}
		@Override
		public void actionPerformed(ActionEvent e) {
			final var wv = Configurations.getViewer().get(WorldView.class);
			final var make = new SettingsMake(false, _constructorsList);
			make.setBounds(Settings.this.getLocationOnScreen().x, Settings.this.getLocationOnScreen().y, Settings.this.getWidth(), Settings.this.getHeight());
			make.addConstructorPropertyChangeListener(c -> {
				blinks.forEach(b -> b.setValue(false));
				final var build = c.build();
				for(final var f : wv.getClass().getMethods()){
					if(f.getName().equals("setSelect") && f.getParameterCount() == 1 && f.getParameterTypes()[0].isAssignableFrom(build.getClass())){
						try {f.invoke(wv,build);} catch (Exception ex) {}
						break;
					}
				}
			});
			make.addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowClosed(java.awt.event.WindowEvent e){
					//Если у нас нет выделения - то убираем выделение. Оно могло остаться от создаваемого объекта
					if(blinks.stream().filter( b -> b.getValue()).findFirst().orElse(null) == null)
						wv.setSelect((Trajectory) null);
					final var ret = make.get(Object.class);
					if(ret != null)
						_addEvent.add(ret);
					rebuildBuild();
				}
			});
			make.setVisible(true);
		}
		};
	private interface CopyListener  {
		/**Должне преобразовать объек в JSON*/
		public Utils.JSON transform();
	}
	private interface InsertListener  {
		/**Получит JSON когда пользоватль захочет вставить объект из буфера обмена*/
		public void transform(Utils.JSON data);
	}
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
		borderClick(streams2, null);
		borderClick(minerals, null);
		borderClick(minerals2, null);
		
		isNeedWarning = false;
		Logger.getLogger(Settings.class.getName()).log(Level.WARNING, "Поправить настройки");
		
		rebuildEdit();	
	}
	/**перестраивает настрйоки*/
	public void rebuild(){
		 if(tableLists.getSelectedIndex() == 1){
			 rebuildBuild();
		 } else {
			 rebuildEdit();
		 }
	}
	/**Пересоздаёт все ползунки*/
	private void rebuildEdit(){
		blinks.clear();
		rebuildEditConfig();
		rebuildGravitation();
		rebuildEditSuns();
		rebuildEditMinerals();
		rebuildEditStreams();
	}
	/**Пересоздаёт все ползунки*/
	private void rebuildBuild(){
		blinks.clear();
		rebuildBuildConfig();
		gravitations.removeAll();
		rebuildBuildSuns();
		rebuildBuildMinerals();
		rebuildBuildStreams();
	}
	/**Режим работы настреок
	 * @return true - тогда мы в режиме редактора карты. Не надо отображать клетки, инфу по боту и остальное
	 */
	public boolean isEdit(){
		return tableLists.getSelectedIndex() == 1;
	}
	
	/**Пересоздаёт конфигурацию мира*/
	private void rebuildEditConfig(){
		configuationsNorm.removeAll();
		configuationsRebuild.removeAll();
			
		final var dc = Configurations.getDefaultConfiguration(Configurations.confoguration.world_type);
		//========================================================
		
		configuationsNorm.add(new javax.swing.JLabel(Configurations.getProperty(Settings.class, "worldSize",Configurations.getWidth(),Configurations.getHeight())));
		configuationsNorm.add(new javax.swing.JLabel(Configurations.getProperty(Settings.class, "worldType",Configurations.confoguration.world_type)));
		configuationsNorm.add(new SettingsSlider<>(Settings.class,"configuations.speed", 0l, 0l, 1000l, 0l,Configurations.world.getSpeed(),null, e -> Configurations.world.setSpeed(e)));
		configuationsNorm.add(new JPopupMenu.Separator());
		configuationsNorm.add(new SettingsSlider<>(Settings.class,"configuations.savePeriod", 1_000, (int)dc.SAVE_PERIOD, 10_000_000,1_000, (int)Configurations.confoguration.SAVE_PERIOD,null, e -> Configurations.confoguration.SAVE_PERIOD = e));
		configuationsNorm.add(new SettingsSlider<>(Settings.class,"configuations.countSave", 1, dc.COUNT_SAVE, 10,1, Configurations.confoguration.COUNT_SAVE,null, e -> Configurations.confoguration.COUNT_SAVE = e));
		configuationsNorm.add(new JPopupMenu.Separator());
		configuationsNorm.add(new SettingsSlider<>(Settings.class,"configuations.mutagenicity",
				0d, dc.AGGRESSIVE_ENVIRONMENT, 100d,
				0d, Configurations.confoguration.AGGRESSIVE_ENVIRONMENT, 100d, e -> Configurations.confoguration.AGGRESSIVE_ENVIRONMENT = e));
		configuationsNorm.add( new SettingsSlider<>(Settings.class,"configuations.timeLifeOrg", 0, dc.TIK_TO_EXIT, 100, 0, Configurations.confoguration.TIK_TO_EXIT,null, e -> Configurations.confoguration.TIK_TO_EXIT = e));
		configuationsNorm.add(new SettingsSlider<>(Settings.class,"configuations.dirtiness",
				0, (int)(dc.DIRTY_WATER * 100), 1000,
				0, (int)(Configurations.confoguration.DIRTY_WATER * 100), null, e -> {Configurations.confoguration.DIRTY_WATER = e / 100d; Configurations.suns.updateMatrix();}));
		
		//А теперь мы обновляем состояние ячеек
		borderClick(configuationsNorm, null);
		borderClick(configuationsNorm, null);
	}
	/**Пересоздаёт конфигурацию мира*/
	private void rebuildBuildConfig(){
		configuationsNorm.removeAll();
		configuationsRebuild.removeAll();
			
		final var dc = Configurations.getDefaultConfiguration(Configurations.confoguration.world_type);
		final var size = new SettingsPoint(Settings.class,"configuations.size", 
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
			rebuildBuild();
			updateUI();
			final var w = Configurations.getViewer().get(WorldView.class);
			w.dispatchEvent(new ComponentEvent(w, ComponentEvent.COMPONENT_RESIZED));
		});
		final var rebuild = new javax.swing.JButton(Configurations.getHProperty(Settings.class, "configuations.rebuild.L"));
		rebuild.setToolTipText(Configurations.getHProperty(Settings.class, "configuations.rebuild.T"));
		rebuild.addActionListener( e -> {
			Configurations.world.awaitStop();
			Configurations.makeDefaultWord(Configurations.confoguration.world_type,Configurations.getWidth() , Configurations.getHeight());
			rebuildBuild();
			updateUI();
			final var w = Configurations.getViewer().get(WorldView.class);
			w.dispatchEvent(new ComponentEvent(w, ComponentEvent.COMPONENT_RESIZED));
		});
		
		size.setBackground(Color.red);
		configuationsRebuild.add(size);
		wt.setBackground(Color.red);
		configuationsRebuild.add(wt);
		rebuild.setBackground(Color.red);
		configuationsRebuild.add(rebuild);
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
				case FIELD_R,CIRCLE -> Gravitation.Direction.CENTER;
				default -> throw new AssertionError();
			};
			final var defPower = switch (Configurations.confoguration.world_type) {
				case LINE_H -> 2;
				case CIRCLE -> 20;
				case LINE_V -> 100;
				case RECTANGLE,FIELD_R -> 1000;
				default -> throw new AssertionError();
			};
			//Немного говнокода вам в копилочку.
			//Нам-же нельзя работать с объектом до его объявления
			//Но объявить можно только финальный объект
			//Ну значит будет у нас... Вот такая вот шляпа :)
			final var sliders = new javax.swing.JPanel[3];
			//Мощность гравитации
			sliders[0] = new SettingsSlider<>(Settings.class,"gravitation." + status.name(), 0, defPower, 1000, 0, buildGrav.getValue(),null,e -> {
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
			final var center = Point.create(Configurations.getWidth()/2,Configurations.getHeight()/2);
			sliders[2] = new SettingsPoint(Settings.class,"gravitation.point", center,buildGrav.getPoint() == null ? center :buildGrav.getPoint(),  e -> {
				final var g = Configurations.gravitation[status.ordinal()];
				Configurations.gravitation[status.ordinal()] = new Gravitation(g.getValue(), e);
			});
			gravitations.add(sliders[0]);
			gravitations.add(sliders[1]);
			gravitations.add(sliders[2]);
		}
		borderClick(gravitations, null);
		borderClick(gravitations, null);
	}
	/**Пересоздаёт звёзды*/
	private void rebuildEditSuns(){
		suns.removeAll();
		suns2.removeAll();
		for (int i = 0; i < Configurations.suns.size(); i++) {
			if(i > 0)
				suns.add(new JPopupMenu.Separator());
			
			final var sun = Configurations.suns.get(i);
			suns.add(new javax.swing.JLabel(Configurations.getProperty(Settings.class, "object.name",sun.toString())));
			suns.add(new SettingsSlider<>(Settings.class,"sun.power", 1, 30, 200, 1,(int)sun.getPower(),  null, e -> {
				sun.setPower(e);
			}));
			suns.add(new SettingsBoolean(Settings.class,"emitter.isLine", !sun.getIsLine(), e -> {
				sun.setIsLine(!e);
			}));
			for(final var p : sun.getParams())
				suns.add(makePanel(sun,p));
			
			final var tr = sun.getTrajectory();
			if(!tr.getClass().equals(Trajectory.class)){
				suns.add(new SettingsSlider<>(Settings.class,"trajectory.speed", 0,(int)tr.getSpeed(),1000,0,(int)tr.getSpeed(),null, e -> {
					tr.setSpeed(e);
				}));
			}
		}
		borderClick(suns, null);
		borderClick(suns, null);
	}
	/**Пересоздаёт звёзды*/
	private void rebuildBuildSuns(){
		suns.removeAll();
		suns2.removeAll();
		final var wv = Configurations.getViewer().get(WorldView.class);
		
		for (int i = 0; i < Configurations.suns.size(); i++) {
			if(i > 0)
				suns2.add(new JPopupMenu.Separator());
			final var sun = Configurations.suns.get(i);
			suns2.add(new SettingsString(Settings.class,"object.editname", "Звезда", sun.toString(), e -> sun.setName(e)));
			suns2.add(addBlink(sun == wv.getSelect(), e->wv.setSelect(e ? sun : null)));
			suns2.add(addBlinkTrajectory(sun == wv.getSelect(), e->wv.setSelect(e ? sun.getTrajectory() : null)));
			suns2.add(addNew(sun));
			suns2.add(makeAddRemPanel(
					new AddListener(SunAbstract.getChildrens(),ret -> Configurations.suns.add((SunAbstract)ret)),
					()->SunAbstract.serialization(sun),j->{Configurations.suns.add(SunAbstract.generation(j, Configurations.VERSION));},e->Configurations.suns.remove(sun)));
			//suns2.add(addRemove(Configurations.suns,sun));
		}
		if(Configurations.suns.isEmpty()){
			suns2.add(addNew(Configurations.suns,SunAbstract.getChildrens(), c->wv.setSelect((SunAbstract) c.build())));
		}
		borderClick(suns2, null);
		borderClick(suns2, null);
	}
	/**Пересоздаёт минералы*/
	private void rebuildEditMinerals(){
		minerals.removeAll();
		minerals2.removeAll();
		final var dc = Configurations.getDefaultConfiguration(Configurations.confoguration.world_type);
		for (int i = 0; i < Configurations.minerals.size(); i++) {
			if(i > 0)
				minerals.add(new JPopupMenu.Separator());
			final var mineral = Configurations.minerals.get(i);
			minerals.add(new javax.swing.JLabel(Configurations.getProperty(Settings.class, "object.name",mineral.toString())));
			minerals.add(new SettingsSlider<>(Settings.class,"minerals.power", 1, 20, 200, 1,(int)mineral.getPower(),  null, e -> {
				mineral.setPower(e);
			}));
			minerals.add(new SettingsSlider<>(Settings.class,"minerals.attenuation",
				0, (int)(dc.DIRTY_WATER * 100), 1000,
				0, (int)(mineral.getAttenuation() * 100), null, e -> mineral.setAttenuation(e / 100d)));
			minerals.add(new SettingsBoolean(Settings.class,"emitter.isLine", !mineral.getIsLine(), e -> {
				mineral.setIsLine(!e);
			}));
			for(final var p : mineral.getParams())
				minerals.add(makePanel(mineral,p));
			
			final var tr = mineral.getTrajectory();
			if(!tr.getClass().equals(Trajectory.class)){
				minerals.add(new SettingsSlider<>(Settings.class,"trajectory.speed", 0l,tr.getSpeed(),1000l,0l,tr.getSpeed(),null, e -> {
					tr.setSpeed(e);
				}));
			}
		}
		borderClick(minerals, null);
		borderClick(minerals, null);
	}
	/**Пересоздаёт минералы*/
	private void rebuildBuildMinerals(){
		minerals.removeAll();
		minerals2.removeAll();
		final var wv = Configurations.getViewer().get(WorldView.class);
		
		for (int i = 0; i < Configurations.minerals.size(); i++) {
			if(i > 0)
				minerals2.add(new JPopupMenu.Separator());
			final var mineral = Configurations.minerals.get(i);
			minerals2.add(new SettingsString(Settings.class,"object.editname", "Залеж", mineral.toString(), e -> mineral.setName(e)));
			minerals2.add(addBlink(mineral == wv.getSelect(), e->wv.setSelect(e ? mineral : null)));
			minerals2.add(addBlinkTrajectory(mineral == wv.getSelect(), e->wv.setSelect(e ? mineral.getTrajectory() : null)));
			minerals2.add(addNew(mineral));
			minerals2.add(addRemove(Configurations.minerals,mineral));
		}
		minerals2.add(new JPopupMenu.Separator());
		minerals2.add(addNew(Configurations.minerals,MineralAbstract.getChildrens(), c->wv.setSelect((MineralAbstract) c.build())));
		borderClick(minerals2, null);
		borderClick(minerals2, null);
	}
	/**Пересоздаёт потоки*/
	private void rebuildEditStreams(){
		streams.removeAll();
		streams2.removeAll();
		for (int i = 0; i < Configurations.streams.size(); i++) {
			if(i > 0)
				streams.add(new JPopupMenu.Separator());
			final var stream = Configurations.streams.get(i);
			streams.add(new javax.swing.JLabel(Configurations.getProperty(Settings.class, "object.name",stream.toString())));
			
			for(final var p : stream.getParams())
				streams.add(makePanel(stream,p));
			
			final var tr = stream.getTrajectory();
			if(!tr.getClass().equals(Trajectory.class)){
				streams.add(new SettingsSlider<>(Settings.class,"trajectory.speed", 0l,tr.getSpeed(),1000l,0l,tr.getSpeed(),null, e -> {
					tr.setSpeed(e);
					stream.updateMatrix();
				}));
			}
		}
		borderClick(streams, null);
		borderClick(streams, null);
	}
	/**Пересоздаёт потоки*/
	private void rebuildBuildStreams(){
		streams.removeAll();
		streams2.removeAll();
		final var wv = Configurations.getViewer().get(WorldView.class);
		
		for (int i = 0; i < Configurations.streams.size(); i++) {
			if(i > 0)
				streams2.add(new JPopupMenu.Separator());
			final var stream = Configurations.streams.get(i);
			streams2.add(new SettingsString(Settings.class,"object.editname", "Залеж", stream.toString(), e -> stream.setName(e)));
			streams2.add(addBlink(stream == wv.getSelect(),e -> wv.setSelect(e ? stream : null)));
			streams2.add(addBlinkTrajectory(stream == wv.getSelect(), e->wv.setSelect(e ? stream.getTrajectory() : null)));
			streams2.add(addNew(stream));
			streams2.add(addRemove(Configurations.streams,stream));
		}
		streams2.add(new JPopupMenu.Separator());
		streams2.add(addNew(Configurations.streams,StreamAbstract.getChildrens(), c->wv.setSelect((StreamAbstract) c.build())));
		
		borderClick(streams2, null);
		borderClick(streams2, null);
	}
	/**
	 * Создаёт панель с 4мя кнопками - добавить, копировать, вставить, удалить
	 * @return 
	 */
	private javax.swing.JPanel makeAddRemPanel(AddListener addListener,CopyListener copyListener,InsertListener insertListener, java.awt.event.ActionListener removeListener){
		final var panel = new javax.swing.JPanel();
		panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.X_AXIS));
		panel.setAlignmentX(0);
		{
			final var add = new javax.swing.JButton();
			Configurations.setIcon(add,"add");
			add.addActionListener(e -> {addListener.actionPerformed(e);});
			add.setToolTipText(Configurations.getHProperty(Settings.class, "object.add"));
			add.setFocusable(false);
			panel.add(add);
		}
		{
			final var copy = new javax.swing.JButton();
			Configurations.setIcon(copy,"clipboardCopy");
			copy.addActionListener(e -> {
				final var j = copyListener.transform();
				final var stringSelection = new java.awt.datatransfer.StringSelection(j.toJSONString());
				final var clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, null);
			});
			copy.setToolTipText(Configurations.getHProperty(Settings.class, "object.copy"));
			copy.setFocusable(false);
			panel.add(copy);
		}
		{
			final var insert = new javax.swing.JButton();
			Configurations.setIcon(insert,"clipboardInsert");
			insert.addActionListener(e->{
				try{
					final var  data = (String) java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().getData(java.awt.datatransfer.DataFlavor.stringFlavor);
					final var json = new Utils.JSON(data);
					insertListener.transform(json);
				} catch (Exception ex){
					Logger.getLogger(this.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
					JOptionPane.showMessageDialog(null,	"Ошибка вставки объекта!\n" + ex.getMessage(), "BioLife", JOptionPane.ERROR_MESSAGE);
				}
				rebuild();
			});
			insert.setToolTipText(Configurations.getHProperty(Settings.class, "object.insert"));
			insert.setFocusable(false);
			panel.add(insert);
		}
		{
			final var remove = new javax.swing.JButton();
			Configurations.setIcon(remove,"remove");
			remove.addActionListener(e->{removeListener.actionPerformed(e);rebuildBuild();});
			remove.setToolTipText(Configurations.getHProperty(Settings.class, "object.remove"));
			remove.setFocusable(false);
			panel.add(remove);
		}
		return panel;
	}
	
	/**
	 * Создаёт кнопку удаления объекта
	 * @param <T>
	 * @param list список, из которого объект удаляется
	 * @param object сам удаляемый объект
	 * @return объект кнопки, который нужно добавить на панель
	 */
	private <T> javax.swing.JButton addRemove(final List<T> list, final T object){
		final var remove = new javax.swing.JButton(Configurations.getHProperty(Settings.class, "object.parameter.remove.L"));
		remove.setToolTipText(Configurations.getHProperty(Settings.class, "object.parameter.remove.T"));
		remove.addActionListener( e -> {
			list.remove(object);
			rebuildBuild();
		});
		return remove;
	}
	/**
	 * Создаёт кнопку удаления объекта
	 * @param <T>
	 * @param list список, из которого объект удаляется
	 * @param object сам удаляемый объект
	 * @return объект кнопки, который нужно добавить на панель
	 */
	private <T extends DefaultEmitter> javax.swing.JButton addRemove(final EmitterSet<T> list, final T object){
		final var remove = new javax.swing.JButton(Configurations.getHProperty(Settings.class, "object.parameter.remove.L"));
		remove.setToolTipText(Configurations.getHProperty(Settings.class, "object.parameter.remove.T"));
		remove.addActionListener( e -> {
			list.remove(object);
			rebuildBuild();
		});
		return remove;
	}
	/**
	 * Создаёт кнопку генерации новой траектории для объекта
	 * @param <T>
	 * @param object сам объект, траектория которого нас ну оооочень интересует
	 * @return объект кнопки, который нужно добавить на панель
	 */
	private javax.swing.JButton addNew(Trajectory.HasTrajectory object){
		return addNew("object.parameter.newTrajectory",Trajectory.getChildrens(), c -> Configurations.getViewer().get(WorldView.class).setSelect((Trajectory) c.build()), ret -> object.set((Trajectory)ret));
	}
	
	/**
	 * Создаёт кнопку генерации нового объекта
	 * @param <T>
	 * @param list уже существующие объекты
	 * @param constructorList список конструкторов
	 * @param l событие, произошедшее при изменении параметров конструктора
	 * @return объект кнопки, который нужно добавить на панель
	 */
	private <T> javax.swing.JButton addNew(final List<T> list, final List<ClassBuilder> constructorList, SettingsMake.PropertyChangeListener l){
		return addNew("object.parameter.newObject",constructorList, l, ret -> list.add((T)ret));
	}
	/**
	 * Создаёт кнопку добавки нового объекта
	 * @param <T> класс, который добавляем
	 * @param text текстовый ключ для поиска локализованного перевода
	 * @param constructorList набор конструкторов этого объекта
	 * @param l слушатель события изменения конструктора. Для отображения на экране
	 * @param eventAdd событие, которое возникает если объект всё-же создали
	 * @return кнопка, на неё надо нажать и всё будет
	 */
	private <T> javax.swing.JButton addNew(String text, final List<ClassBuilder> constructorList, SettingsMake.PropertyChangeListener l, AddListener.AddNewO<T> eventAdd){
		final var newT = new javax.swing.JButton(Configurations.getHProperty(Settings.class, text +".L"));
		newT.setToolTipText(Configurations.getHProperty(Settings.class, text +".T"));
		newT.addActionListener( e -> {
			final var wv = Configurations.getViewer().get(WorldView.class);		
			final var make = new SettingsMake(false, constructorList);
			make.setBounds(newT.getLocationOnScreen().x, newT.getLocationOnScreen().y, Settings.this.getWidth(), Settings.this.getHeight());
			make.addConstructorPropertyChangeListener(c -> {
				blinks.forEach(b -> b.setValue(false));
				l.propertyChange(c);
			});
			make.addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowClosed(java.awt.event.WindowEvent e){
					//Если у нас нет выделения - то убираем выделение. Оно могло остаться от создаваемого объекта
					if(blinks.stream().filter( b -> b.getValue()).findFirst().orElse(null) == null)
						wv.setSelect((Trajectory) null);
					final var ret = make.get(Object.class);
					if(ret != null)
						eventAdd.add((T)ret);
					rebuildBuild();
				}
			 });
			make.setVisible(true);
		});
		return newT;
	}
	/**
	 * Создаёт кнопку генерации нового объекта
	 * @param <T>
	 * @param list уже существующие объекты
	 * @return объект кнопки, который нужно добавить на панель
	 */
	private <T extends DefaultEmitter> javax.swing.JButton addNew(final EmitterSet<T> list, final List<ClassBuilder> constructorList, SettingsMake.PropertyChangeListener l){
		return addNew("object.parameter.newObject",constructorList, l, ret -> list.add((T)ret));
	}
	
	/**Добавляет кнопку мигания
	 * @param nowValue текущее состояние объекта
	 * @param doing что нужно сделать при включении (выключении) мигания?
	 * @return панель с кнопкой мигания
	 */
	private javax.swing.JPanel addBlink(boolean nowValue, SettingsBoolean.AdjustmentListener doing){
		final var panels = new SettingsBoolean[1];
		panels[0] = new SettingsBoolean(Settings.class,"object.blink", nowValue, e -> {
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
	/**Добавляет кнопку мигания для траектории
	 * @param nowValue текущее состояние объекта
	 * @param doing что нужно сделать при включении (выключении) мигания?
	 * @return панель с кнопкой мигания
	 */
	private javax.swing.JPanel addBlinkTrajectory(boolean nowValue, SettingsBoolean.AdjustmentListener doing){
		final var panels = new SettingsBoolean[1];
		panels[0] = new SettingsBoolean(Settings.class,"object.blinkTrajectory", nowValue, e -> {
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
	/**Создаёт панель параметров
	 * @param <P> возвращаемый тип параметра - Boolean, Boolean[]
	 * @param <T> тип объекта, который строим
	 * @param object сам объект, параметр которого выписываем
	 * @param param параметр, который мы будем крутить
	 * @return готовая панель с нужными крутилками
	 */
	private <P, T> javax.swing.JPanel makePanel(T object, Utils.ClassBuilder.EditParametr<P,T> param){
		final var clr = object.getClass();
		final var parametrName = "parameter."+param.name();
		if(param instanceof Utils.ClassBuilder.BooleanParam<?> np_){
			final var np = (Utils.ClassBuilder.BooleanParam<T>) np_;
			return new SettingsBoolean(clr,parametrName, np.get(object), e -> {np.setValue(object, e);});
		} else if(param instanceof Utils.ClassBuilder.BooleanVectorParam<?> np_){
			final var np = (Utils.ClassBuilder.BooleanVectorParam<T>) np_;
			final var nowVals = np.get(object);
			final var panel = new javax.swing.JPanel();
			panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
			for (int i = 0; i < nowVals.length; i++) {
				final var index = i;
				panel.add(new SettingsBoolean(clr, parametrName, nowVals[index], e -> {
					nowVals[index] = e;
					np.setValue(object, nowVals);
				}));
			}
			throw new AssertionError(String.valueOf(param));
			//return panel; Я просто не уверен, что сделал всё верно :)
		} else if(param instanceof Utils.ClassBuilder.StringParam<?> np_){
			final var np = (Utils.ClassBuilder.StringParam<T>) np_;
			return new SettingsString(clr,parametrName, np.getDefault(),np.get(object), e -> {np.setValue(object, e);});
		} else if(param instanceof Utils.ClassBuilder.StringVectorParam<?> np_){
			throw new AssertionError(String.valueOf(param));
		} else if(param instanceof Utils.ClassBuilder.NumberParam<? extends Number,?> np_){
			final var npn = (Utils.ClassBuilder.NumberParam<? extends Number,T>) np_;
			final var def = npn.getDefault().getClass();
			if(def.equals(Integer.class)){
				final var np = (Utils.ClassBuilder.NumberParam<Integer,T>) npn;
				return new SettingsSlider<>(clr,parametrName, np.getSliderMinimum(),np.getDefault(),np.getSliderMaximum(),np.getRealMinimum(),np.get(object),np.getRealMaximum(), e -> {
					np.setValue(object, e);
				});
			} else if(def.equals(Double.class)){
				final var np = (Utils.ClassBuilder.NumberParam<Double,T>) npn;
				return new SettingsSlider<>(clr,parametrName, np.getSliderMinimum(),np.getDefault(),np.getSliderMaximum(),np.getRealMinimum(),np.get(object),np.getRealMaximum(), e -> {
					np.setValue(object, e);
				});
			} else {
				throw new IllegalArgumentException("Класс " + def + " пока не поддерживается");
			}
		}  else if(param instanceof Utils.ClassBuilder.NumberVectorParam<? extends Number,?> np_){
			throw new AssertionError(String.valueOf(param));
		} else if(param instanceof Utils.ClassBuilder.MapPointParam<?> np_){
			throw new AssertionError(String.valueOf(param));
		} else if(param instanceof Utils.ClassBuilder.MapPointVectorParam<?> np_){
			throw new AssertionError(String.valueOf(param));
		}  else if(param instanceof Utils.ClassBuilder.Abstract2Param<?> np_){
			throw new AssertionError(String.valueOf(param));
		} else if(param instanceof Utils.ClassBuilder.Abstract2VectorParam<?> np_){
			throw new AssertionError(String.valueOf(param));
		} else {
			throw new AssertionError(String.valueOf(param));
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
        suns2 = new javax.swing.JPanel();
        streams2 = new javax.swing.JPanel();
        minerals2 = new javax.swing.JPanel();

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
            .addComponent(configuationsNorm, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
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

        streams2.setBackground(new java.awt.Color(204, 204, 204));
        streams2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Configurations.getProperty(Settings.class,"streams"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        streams2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                streams2MouseClicked(evt);
            }
        });
        streams2.setLayout(new javax.swing.BoxLayout(streams2, javax.swing.BoxLayout.Y_AXIS));

        minerals2.setBackground(new java.awt.Color(237, 255, 33));
        minerals2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Configurations.getProperty(Settings.class,"minerals"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        minerals2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                minerals2MouseClicked(evt);
            }
        });
        minerals2.setLayout(new javax.swing.BoxLayout(minerals2, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(configuationsRebuild, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
            .addComponent(suns2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(streams2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(minerals2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(configuationsRebuild, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(suns2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(streams2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minerals2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tableLists.addTab(Configurations.getProperty(Settings.class,"rebuild.name"), null, jPanel2, Configurations.getProperty(Settings.class,"rebuild.tooltip"));

        javax.swing.GroupLayout jPanelLayout = new javax.swing.GroupLayout(jPanel);
        jPanel.setLayout(jPanelLayout);
        jPanelLayout.setHorizontalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLayout.createSequentialGroup()
                .addComponent(tableLists, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                .addContainerGap())
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
		rebuild();
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

    private void streams2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_streams2MouseClicked
         borderClick(streams2, evt);
    }//GEN-LAST:event_streams2MouseClicked

    private void minerals2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minerals2MouseClicked
         borderClick(minerals2, evt);
    }//GEN-LAST:event_minerals2MouseClicked
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
    private javax.swing.JPanel minerals2;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JPanel streams;
    private javax.swing.JPanel streams2;
    private javax.swing.JPanel suns;
    private javax.swing.JPanel suns2;
    private javax.swing.JTabbedPane tableLists;
    // End of variables declaration//GEN-END:variables
	/**Нужна печать предупреждения при редактировании карты?*/
	private boolean isNeedWarning = true;
	/**Список всех подсвеченных объектов. Нужен для того, чтобы подсвечивался только один объект*/
	private final ArrayList<SettingsBoolean> blinks = new ArrayList<>();
}
