/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/AWTForms/Dialog.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import Calculations.Point;
import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import MapObjects.CellObject;
import MapObjects.Organic;
import MapObjects.Poison;
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
			params.add(new SeatchParam("age",0,Integer.MAX_VALUE, (CellObject co) -> co.getAge()));
			params.add(new SeatchParam("health",0,Integer.MAX_VALUE, (CellObject co) -> co.getHealth()));
			
			params.add(new SeatchParam("HPTank",0,Integer.MAX_VALUE, AliveCellProtorype::getFoodTank));
			params.add(new SeatchParam("mineral",0,Integer.MAX_VALUE, AliveCellProtorype::getMineral));
			params.add(new SeatchParam("MPTank",0,Integer.MAX_VALUE,  AliveCellProtorype::getMineralTank));
			params.add(new SeatchParam("DNAWall",0,Integer.MAX_VALUE, AliveCellProtorype::getDNA_wall));
			params.add(new SeatchParam("buoyancy",-100,100, AliveCellProtorype::getBuoyancy));
			params.add(new SeatchParam("mucosa",0,Integer.MAX_VALUE, AliveCellProtorype::getMucosa));
			params.add(new SeatchParam("generation",0,Integer.MAX_VALUE, AliveCellProtorype::getGeneration));
			params.add(new SeatchParam("posion",Poison.TYPE.vals, (AliveCell co) -> co.getPosionType().ordinal()));
			params.add(new SeatchParam("direction",Point.DIRECTION.values, (AliveCell co) -> co.direction.ordinal()));
			params.add(new SeatchParam("DNA_lenght",0,Integer.MAX_VALUE, (AliveCell co) -> co.getDna().size));
			final var CMDS_DNA_SET = (new HashSet<>(Arrays.asList(MapObjects.dna.CommandList.list))).toArray(MapObjects.dna.CommandDNA[]::new);
			params.add(new SeatchParam("DNA_CMD",CMDS_DNA_SET, (AliveCell co) -> co.getDna().mind));
			for(final var s : AliveCell.Specialization.TYPE.values){
				params.add(new SeatchParam("specialization."+s.name(),0,AliveCell.Specialization.MAX_SPECIALIZATION, (AliveCell co) -> co.getSpecialization().get(s)));
			}
			params.add(new SeatchParam("comrades", (AliveCell co) -> co.getCountComrades() == 0 ? 0 : 1));
			
			params.add(new SeatchParam("posion",Poison.TYPE.vals, (Poison co) -> co.getType().ordinal()));
			params.add(new SeatchParam("stream",0,Integer.MAX_VALUE, Poison::getStream));
			
			params.add(new SeatchParam("posion",Poison.TYPE.vals, (Organic co) -> co.getPoison().ordinal()));

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
		public interface PoisonParam { public double test(Poison ac); };
		public interface OrganicParam { public double test(Organic ac); };
		
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
		 * @param tst фукнция тестирования
		 * @param t тип объекта
		 */
		private SeatchParam(Tester tst, CellObject.LV_STATUS t){
			tester = tst;
			type = t;
		}
		/** Создание параметра изменяющегося от min к max. Параметр должен укладываться в рамки.
		 * Выбор с двумя ячейками - от и до
		 * Изначально рамки будут полные - от и до.
		 * @param name название параметра (для локализованного имени)
		 * @param min минимальное значение
		 * @param max максимальное значение
		 * @param tst функция тестирования
		 * @param t тип объекта тестирования
		 */
		public SeatchParam(String name,int min,int max, Tester tst, CellObject.LV_STATUS t){
			this(tst, t);
			final int defMin;
			final int defMax;
			if (min == Integer.MIN_VALUE && max == Integer.MAX_VALUE) {
				defMin = -999_999; defMax = 999_999; //Больше просто не влазит в ползунок и получается не красиво :(
			} else if (min == Integer.MIN_VALUE) {
				defMin = -999_999; defMax = max;
			} else if (max == Integer.MAX_VALUE) {
				defMin = min; defMax = 999_999;
			} else {
				defMin = min; defMax = max;
			}

			nowP = Point.Vector.create(defMin, defMax); //Больше просто не влазит в ползунок и получается не красиво :(
			param = new SettingsPoint(MenuSearch.class,name,MenuSearch.class,"vectorParam", min, defMin, max, nowP.x, min, defMax, max, nowP.y, e -> {
				nowP = e;
			});
		}
		public SeatchParam(String name,int min,int max, CellObjectParam tst){
			this(name, min, max, (co, val) -> (val.x <= tst.test(co) && tst.test(co) <= val.y), null);
		}
		public SeatchParam(String name,int min,int max, AliveCellParam tst){
			this(name, min, max, (co, val) -> (val.x <= tst.test((AliveCell)co) && tst.test((AliveCell)co) <= val.y), CellObject.LV_STATUS.LV_ALIVE);
		}
		public SeatchParam(String name,int min,int max, PoisonParam tst){
			this(name, min, max, (co, val) -> (val.x <= tst.test((Poison)co) && tst.test((Poison)co) <= val.y), CellObject.LV_STATUS.LV_POISON);
		}
		/**
		 * Создание параметра, принадлежащего элементу массива. Дополнительно создаёт в массиве параметр null, который будет означать любое значение этого параметра
		 * @param <T> тип массива
		 * @param name название параметра (для локализованного имени)
		 * @param values значения массива
		 * @param init инициализационное значение. Но может быть и null, тогда изначально будут любые параметры подходить
		 * @param tst функция тестирования
		 * @param t тип объекта
		 */
		public <T extends Enum> SeatchParam(String name,T[] values,T init, Tester tst, CellObject.LV_STATUS t){
			this((co, val) -> (val.x == values.length || tst.isCorrect(co, val)), t);
			if(init == null)
				nowP = Point.Vector.create(values.length, 0);
			else
				nowP = Point.Vector.create(init.ordinal(), init.ordinal());
			final var collWidthNull = Utils.Utils.addNull(values);
			param = new SettingsSelect(MenuSearch.class,name, collWidthNull, init, collWidthNull[nowP.x], (e)->{
				nowP = Point.Vector.create(((T) e).ordinal(), 0);
			});
		}
		public <T extends Enum> SeatchParam(String name,T[] values,T init, AliveCellParam tst){
			this(name,values, init,  (co, val) -> (val.x == (int) tst.test((AliveCell)co)), CellObject.LV_STATUS.LV_ALIVE);
		}
		public <T extends Enum> SeatchParam(String name,T[] values, AliveCellParam tst){
			this(name, values, null, (co, val) -> (val.x == (int) tst.test((AliveCell)co)), CellObject.LV_STATUS.LV_ALIVE);
		}
		public <T extends Enum> SeatchParam(String name,T[] values, PoisonParam tst){
			this(name, values, null, (co, val) -> (val.x == (int) tst.test((Poison)co)), CellObject.LV_STATUS.LV_POISON);
		}
		public <T extends Enum> SeatchParam(String name,T[] values, OrganicParam tst){
			this(name, values, null, (co, val) -> (val.x == (int) tst.test((Organic)co)), CellObject.LV_STATUS.LV_ORGANIC);
		}
		
		/**Создание параметра из занчений массива, с нулём на конце. Проверяет чтобы у объекта было такое значение
		 * @param <T>
		 * @param name
		 * @param values
		 * @param tst 
		 */
		public <T> SeatchParam(String name,T[] values, AliveCellVParam tst){
			this((co, val) -> (val.x == values.length || Arrays.stream(tst.test((AliveCell)co)).anyMatch(num -> num == val.x)), CellObject.LV_STATUS.LV_ALIVE);
			nowP = Point.Vector.create(values.length, 0);
			final var collWidthNull = Utils.Utils.addNull(values);
			param = new SettingsSelect(MenuSearch.class,name, collWidthNull, null, collWidthNull[nowP.x], (e)->{
				final var o = java.util.Arrays.asList(values).indexOf(e);
				nowP = Point.Vector.create( o,  o);
			});
		}
		/** Создаёт параметр логический - ДА, НЕТ, любое значение
		 * @param <T>
		 * @param name
		 * @param tst 
		 */
		public <T> SeatchParam(String name,AliveCellParam tst){
			this((co, val) -> (val.x == 2 || val.x == tst.test((AliveCell)co)), CellObject.LV_STATUS.LV_ALIVE);
			nowP = Point.Vector.create(2, 0);
			final String[] values = {Configurations.getProperty(MenuSearch.class,"param.boolean.no"),Configurations.getProperty(MenuSearch.class,"param.boolean.yes"),Configurations.getProperty(MenuSearch.class,"param.boolean.any")};
			param = new SettingsSelect(MenuSearch.class,name, values, values[nowP.x], values[nowP.x], (e)->{
				final var o = java.util.Arrays.asList(values).indexOf(e);
				nowP = Point.Vector.create( o,  o);
			});
		}
		
		/**Проверяет, что переданная клетка подходит под условия поиска
		 * @param cell кого надо оценить (может быть null)
		 * @return true, если это как раз тот, кто нам нужен!
		 */
		public boolean isCorrect(CellObject cell){
			return (type != null && !cell.aliveStatus(type)) || tester.isCorrect(cell,nowP);
		}
	}
	
	

	/** Creates new form SettingsMake
	 * @param modal окно модальное?
	 */
	public MenuSearch(boolean modal) {
		super((Frame)null, Configurations.getProperty(MenuSearch.class,"title"));
		setAlwaysOnTop(true);
		if(WH == null) WH = Point.Vector.create(Configurations.getWidth(), Configurations.getHeight());
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

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
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

        selectType.setMaximumSize(new java.awt.Dimension(32767, 23));
        selectType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectTypeActionPerformed(evt);
            }
        });

        paramPanel.setLayout(new javax.swing.BoxLayout(paramPanel, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane1.setViewportView(paramPanel);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(selectType, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(selectType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE))
        );

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
		
		paramPanel.add(new SettingsPoint(MenuSearch.class,"posLU", Point.create(0, 0),LU, e -> LU = e));
		paramPanel.add(new SettingsPoint(MenuSearch.class,"WH", 
				1, Configurations.getWidth(), Configurations.getWidth(),WH.x,
				1, Configurations.getHeight(), Configurations.getHeight(),WH.y,  e -> WH = e
		));
		
		for(final var p : mainSeeker.params)
			if(p.type == null || p.type == type)
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
	/**Проверяет объект по условиям поиска
	 * @param co проверяемый объект
	 * @return true, если это тот самый объект, что мы ищем
	 */
	public boolean isCorrect(CellObject co){
		return (LU.x <= co.getPos().x && co.getPos().x <= (LU.x + WH.x)) && 
				(LU.y <= co.getPos().y && co.getPos().y <= (LU.y + WH.y)) && 
				mainSeeker.isCorrect(co);
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel paramPanel;
    private javax.swing.JComboBox<CellObject.LV_STATUS> selectType;
    // End of variables declaration//GEN-END:variables
	
	/**Главный поисковик*/
	private static Seeker mainSeeker;
	
	/**Верхний левый угол поиска*/
	private static Point LU = Point.create(0, 0);
	/**Ширина и высота окна поиска*/
	private static Point.Vector WH = null;

	
}
