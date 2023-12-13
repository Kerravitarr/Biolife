/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/AWTForms/Dialog.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import Calculations.Point;
import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.Poison;
import Utils.ClassBuilder;
import java.awt.EventQueue;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 *Диалоговое окно создания объекта
 * @author Kerravitarr
 */
public class MenuSearch extends java.awt.Dialog {
	/**Объектоискатель*/
	public static class Seeker{
		/**Все параметры, которые мы знаем*/
		private final List<SeatchParam> params;
		
		private Seeker(){
			params = new ArrayList<>();
			params.add(new SeatchParam("age",0,Integer.MAX_VALUE,new SeatchParam.CellObjectParam(){@Override public double test(CellObject co){return co.getAge();} }));
			params.add(new SeatchParam("health",0,Integer.MAX_VALUE,new SeatchParam.CellObjectParam(){@Override public double test(CellObject co){return co.getHealth();} }));
			
			params.add(new SeatchParam("HPTank",0,Integer.MAX_VALUE,new SeatchParam.AliveCellParam(){@Override public double test(AliveCell co){return co.getFoodTank();} }));
			params.add(new SeatchParam("mineral",0,Integer.MAX_VALUE,new SeatchParam.AliveCellParam(){@Override public double test(AliveCell co){return co.getMineral();} }));
			params.add(new SeatchParam("MPTank",0,Integer.MAX_VALUE,new SeatchParam.AliveCellParam(){@Override public double test(AliveCell co){return co.getMineralTank();} }));
			params.add(new SeatchParam("DNAWall",0,Integer.MAX_VALUE,new SeatchParam.AliveCellParam(){@Override public double test(AliveCell co){return co.getDNA_wall();} }));
			params.add(new SeatchParam("buoyancy",-100,100,new SeatchParam.AliveCellParam(){@Override public double test(AliveCell co){return co.getBuoyancy();} }));
			params.add(new SeatchParam("mucosa",0,Integer.MAX_VALUE,new SeatchParam.AliveCellParam(){@Override public double test(AliveCell co){return co.getMucosa();} }));
			params.add(new SeatchParam("generation",0,Integer.MAX_VALUE,new SeatchParam.AliveCellParam(){@Override public double test(AliveCell co){return co.getGeneration();} }));
			
			params.add(new SeatchParam("posion",Poison.TYPE.vals,new SeatchParam.AliveCellParam(){@Override public double test(AliveCell co){return co.getPosionType().ordinal();} }));
			params.add(new SeatchParam("direction",Point.DIRECTION.values,new SeatchParam.AliveCellParam(){@Override public double test(AliveCell co){return co.direction.ordinal();} }));
			final var CMDS_DNA_SET = (new HashSet<>(Arrays.asList(MapObjects.dna.CommandList.list))).toArray(MapObjects.dna.CommandDNA[]::new);
			params.add(new SeatchParam("DNA_CMD",CMDS_DNA_SET,new SeatchParam.AliveCellVParam(){@Override public int[] test(AliveCell co){return co.getDna().mind;} }));
			
		}
		
		/**Проверяет, что переданная клетка подходит под условия поиска
		 * @param cell кого надо оценить (может быть null)
		 * @return true, если это как раз тот, кто нам нужен!
		 */
		public boolean isCorrect(CellObject cell){
			return cell != null && params.stream().allMatch(p -> p.isCorrect(cell));
		}
		
	}
	/**Параметр поиска*/
	private static class SeatchParam{		
		public interface CellObjectParam{ public double test(CellObject co); };
		public interface AliveCellParam { public double test(AliveCell ac); };
		public interface AliveCellVParam { public int[] test(AliveCell ac); };
		
		private interface Tester{public boolean isCorrect(CellObject cell, Point.Vector val);};
		
		/**К какому типу объектов относится параметр. Если null, то к любому типу*/
		public final CellObject.LV_STATUS type;
		/**Панель с параметром*/
		public javax.swing.JPanel param;
		/**Текущее значение точечного параметра*/
		private Point.Vector nowP;
		/**Тестер клетки*/
		public final Tester tester;
		
		/** Создание поискового параметра ограниченного сверху и снизу двумя числами
		 * @param name название параметра (берётся для локализованного имени)
		 * @param min минимальное значение параметра
		 * @param init инициализационное значение
		 * @param max максимальное значение параметра
		 * @param tst фукнция тестирования
		 * @param t тип объекта
		 */
		private SeatchParam(Tester tst, CellObject.LV_STATUS t){
			tester = tst;
			type = t;
		}
		public SeatchParam(String name,int min,int max, CellObjectParam tst){
			this((co, val) -> (val.x <= tst.test(co) && tst.test(co) <= val.y), null);
			nowP = Point.Vector.create(min, max);
			param = new SettingsPoint(MenuSearch.class,name,MenuSearch.class,"vectorParam", min, min, max, nowP.x, min, max, max, nowP.y, e -> {
				nowP = e;
			});
		}
		public SeatchParam(String name,int min,int max, AliveCellParam tst){
			this((co, val) -> (val.x <= tst.test((AliveCell)co) && tst.test((AliveCell)co) <= val.y), CellObject.LV_STATUS.LV_ALIVE);
			nowP = Point.Vector.create(min, max);
			param = new SettingsPoint(MenuSearch.class,name,MenuSearch.class,"vectorParam", min, min, max, nowP.x, min, max, max, nowP.y, e -> {
				nowP = e;
			});
		}
		public <T extends Enum> SeatchParam(String name,T[] values,T init, AliveCellParam tst){
			this((co, val) -> (val.x == values.length || val.x == (int) tst.test((AliveCell)co)), CellObject.LV_STATUS.LV_ALIVE);
			if(init == null)
				nowP = Point.Vector.create(values.length, 0);
			else
				nowP = Point.Vector.create(init.ordinal(), init.ordinal());
			final var collWidthNull = Utils.Utils.addNull(values);
			
			param = new SettingsSelect(MenuSearch.class,name, collWidthNull, init, collWidthNull[nowP.x], (e)->{
				nowP = Point.Vector.create(((T) e).ordinal(), 0);
			});
		}
		public <T extends Enum> SeatchParam(String name,T[] values, AliveCellParam tst){
			this(name, values, null, tst);
		}
		public <T> SeatchParam(String name,T[] values, AliveCellVParam tst){
			this((co, val) -> (val.x == values.length || Arrays.stream(tst.test((AliveCell)co)).anyMatch(num -> num == val.x)), CellObject.LV_STATUS.LV_ALIVE);
			nowP = Point.Vector.create(values.length, 0);
			final var collWidthNull = Utils.Utils.addNull(values);
			param = new SettingsSelect(MenuSearch.class,name, collWidthNull, null, collWidthNull[nowP.x], (e)->{
				final var o = java.util.Arrays.asList(values).indexOf(e);
				nowP = Point.Vector.create( o,  o);
			});
		}
		
		/**Проверяет, что переданная клетка подходит под условия поиска
		 * @param cell кого надо оценить (может быть null)
		 * @return true, если это как раз тот, кто нам нужен!
		 */
		public boolean isCorrect(CellObject cell){
			return !cell.aliveStatus(type) || tester.isCorrect(cell,nowP);
		}
	}
	
	
	/**Слушатель события изменения свойств*/
	public static interface PropertyChangeListener {
		/**
		 * Вызывается каждый раз, когда изменяется создаваемый объект
		 * @param evt текущий конструктор со всеми параметрами, которые были изменены
		 */
		public void propertyChange(ClassBuilder.Constructor evt);
	}

	/** Creates new form SettingsMake
	 * @param modal окно модальное?
	 */
	public MenuSearch(boolean modal) {
		super((Frame)null, Configurations.getProperty(MenuSearch.class,"title"));
		setAlwaysOnTop(true);
		initComponents();
		if(mainSeeker == null)
			mainSeeker = new Seeker();
		
		final var values = new javax.swing.DefaultComboBoxModel<CellObject.LV_STATUS>();
		for(final var c : CellObject.LV_STATUS.values){
			if(c != CellObject.LV_STATUS.GHOST)
			values.addElement(c);
		}
		selectType.setModel(values);
		selectType.setSelectedIndex(0);
	}
	/**
	 * Добавляет слушателя изменения объекта
	 * @param pc слушатель изменения объекта
	 */
	public void addConstructorPropertyChangeListener(PropertyChangeListener pc){listeners.add(pc);pc.propertyChange((ClassBuilder.Constructor) null);}
	/**
	 * Удаляет слушателя изменения объекта
	 * @param pc слушатель изменения объекта
	 */
	public void removePropertyChangeListener(PropertyChangeListener pc){listeners.remove(pc);}
	/**Создаёт событие изменения свойств для оповещения всех слушателей*/
	public void propertyChange(){
		if(!listeners.isEmpty()){
			final var constructor = (ClassBuilder.Constructor) null;
			for(final var l : listeners){
				l.propertyChange(constructor);
			}
		}
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        generate = new javax.swing.JButton();
        cansel = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        selectType = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        paramPanel = new javax.swing.JPanel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        generate.setText(Configurations.getProperty(SettingsMake.class,"make"));
        generate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateActionPerformed(evt);
            }
        });
        jPanel1.add(generate);

        cansel.setText(Configurations.getProperty(SettingsMake.class,"cansel"));
        cansel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canselActionPerformed(evt);
            }
        });
        jPanel1.add(cansel);

        jPanel3.add(jPanel1, java.awt.BorderLayout.SOUTH);

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));

        selectType.setMaximumSize(new java.awt.Dimension(32767, 23));
        selectType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectTypeActionPerformed(evt);
            }
        });
        jPanel2.add(selectType);

        paramPanel.setLayout(new javax.swing.BoxLayout(paramPanel, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane1.setViewportView(paramPanel);

        jPanel2.add(jScrollPane1);

        jPanel3.add(jPanel2, java.awt.BorderLayout.CENTER);

        add(jPanel3, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	/** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		setVisible(false);
		dispose();
    }//GEN-LAST:event_closeDialog

    private void selectTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectTypeActionPerformed
        final var type = (CellObject.LV_STATUS)selectType.getSelectedItem();
		paramPanel.removeAll();
		
		paramPanel.add(new SettingsPoint(MenuSearch.class,"posLU", Point.create(0, 0),LU, e -> {LU = e;propertyChange();}));
		paramPanel.add(new SettingsPoint(MenuSearch.class,"WH", 
				1, Configurations.getWidth(), Configurations.getWidth(),WH.x,
				1, Configurations.getHeight(), Configurations.getHeight(),WH.y,  e -> {WH = e;propertyChange();}
		));
		
		for(final var p : mainSeeker.params)
			paramPanel.add(p.param);
		
    }//GEN-LAST:event_selectTypeActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
       	
    }//GEN-LAST:event_formWindowOpened
	@Override
	public void setVisible(boolean b){
		super.setVisible(b);
		if(b){
			 //Я пока не нашёл другого решения, а это через одно место...
			 //В чём проблема - при загрузке окно может быть серым. И может быть таким слишком уж часто... Я не знаю что с этим делать, так что 
			 //Через одно местное решение вот...
			 final var task = new Runnable[1];
			 task[0] = () -> {
				 if(isVisible()){
					 jPanel3.revalidate();
					 jPanel3.repaint();
					 EventQueue.invokeLater(task[0]);
				 }
			 }; 
			 task[0].run();
		}
	}
    private void canselActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_canselActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_canselActionPerformed

    private void generateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateActionPerformed
        final var constructor = (ClassBuilder.Constructor) null;
		_return = constructor.build();
		closeDialog(null);
    }//GEN-LAST:event_generateActionPerformed
	
	/**Возвращает построенный объект
	 * @param <T>
	 * @param cls
	 * @return построенный объект или null, если пользователь отменил действие
	 */
	public <T> T get(Class<T> cls){
		return (T)_return;
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cansel;
    private javax.swing.JButton generate;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel paramPanel;
    private javax.swing.JComboBox<CellObject.LV_STATUS> selectType;
    // End of variables declaration//GEN-END:variables

	/**Непосредтсвенно объект, который мы сгенерировали*/
	private Object _return = null;
	/**Слушатели событий изменения объекта*/
	private List<PropertyChangeListener> listeners = new ArrayList<>();
	
	/**Главный поисковик*/
	private static Seeker mainSeeker;
	
	/**Верхний левый угол поиска*/
	private static Point LU = Point.create(0, 0);
	/**Ширина и высота окна поиска*/
	private static Point.Vector WH = Point.Vector.create(100, 100);

	
}
