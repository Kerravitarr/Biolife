/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/AWTForms/Dialog.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import Calculations.Point;
import Utils.ClassBuilder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 *Диалоговое окно создания объекта
 * @author Kerravitarr
 */
public class SettingsMake extends java.awt.Dialog {
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
	 * @param childList список всех возможных подтипов текущего объекта
	 */
	public SettingsMake(boolean modal, List<ClassBuilder> childList) {
		super((Frame)null, Configurations.getProperty(SettingsMake.class,"title"));
		setAlwaysOnTop(true);
		initComponents();
		
		final var values = new javax.swing.DefaultComboBoxModel();
		for(final var c : childList){
			values.addElement(c);
		}
		selectType.setModel(values);
		selectType.setSelectedIndex(0);
		selectType.setEnabled(childList.size() > 1);
	}
	/**
	 * Добавляет слушателя изменения объекта
	 * @param pc слушатель изменения объекта
	 */
	public void addConstructorPropertyChangeListener(PropertyChangeListener pc){listeners.add(pc);pc.propertyChange((ClassBuilder.Constructor) selectConstructor.getSelectedItem());}
	/**
	 * Удаляет слушателя изменения объекта
	 * @param pc слушатель изменения объекта
	 */
	public void removePropertyChangeListener(PropertyChangeListener pc){listeners.remove(pc);}
	/**Создаёт событие изменения свойств для оповещения всех слушателей*/
	public void propertyChange(){
		if(!listeners.isEmpty()){
			final var constructor = (ClassBuilder.Constructor) selectConstructor.getSelectedItem();
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
        selectConstructor = new javax.swing.JComboBox<>();
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

        selectConstructor.setMaximumSize(new java.awt.Dimension(32767, 10));
        selectConstructor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectConstructorActionPerformed(evt);
            }
        });
        jPanel2.add(selectConstructor);

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
        final var type = (ClassBuilder)selectType.getSelectedItem();
		
		final var values = new javax.swing.DefaultComboBoxModel();
		final var constructors = type.getConstructors();
		for(final var c : constructors){
			values.addElement(c);
		}
		selectConstructor.setModel(values);
		selectConstructor.setSelectedIndex(0);
		selectConstructor.setEnabled(constructors.size() > 1);
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
    private void selectConstructorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectConstructorActionPerformed
		//new Thread(()->{
			final var type = (ClassBuilder)selectType.getSelectedItem();
			final var constructor = (ClassBuilder.Constructor) selectConstructor.getSelectedItem();
			paramPanel.removeAll();
			for(final var param : constructor.getParams()){
				final var panel = addPanel(type.printName(),constructor.name(),(ClassBuilder.ConstructorParametr) param);
				panel.setAlignmentX(0);
				paramPanel.add(panel);
			}
			propertyChange();
		//}).start();
    }//GEN-LAST:event_selectConstructorActionPerformed

    private void canselActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_canselActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_canselActionPerformed

    private void generateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateActionPerformed
        final var constructor = (ClassBuilder.Constructor) selectConstructor.getSelectedItem();
		_return = constructor.build();
		closeDialog(null);
    }//GEN-LAST:event_generateActionPerformed
	/**Создаёт и добавляет панель с нужным параметром для выбранного конструктора
	 * @param clr класс, по которому будет искаться локализованное имя параметра
	 * @param constructorName имя конструктора, для поиска локализованного имени
	 * @param param сам параметр, который и должен превратиться в нужную панель
	 */
	private javax.swing.JPanel addPanel(Class<?> clr,final String constructorName, ClassBuilder.ConstructorParametr param) {
		final String parametrFullName;
		//Найдём правильное имя объекта
		if (param.name().startsWith("super.")) {
			parametrFullName = String.format("constructor.parameter.%s", param.name().substring(6));
			//А теперь ищем по суперклассу
			var now = clr;
			Class<?> old;
			do{
				old = now;
				if(Configurations.isHasPropery(now,parametrFullName + ".L")){
					clr = now;
					break;
				}
			} while((now = now.getSuperclass()) != old && now != null);
		} else if (param.name().startsWith("constructor.")) {
			parametrFullName = String.format("constructor.parameter.%s", param.name().substring(12));
		} else if (param.name().startsWith("parameter.")) {
			parametrFullName = String.format("parameter.%s", param.name().substring(10));
		} else if (constructorName.isEmpty()) {
			parametrFullName = String.format("constructor.parameter.%s", param.name());
		} else {
			parametrFullName = String.format("constructor.%s.parameter.%s", constructorName, param.name());
		}
		//Сохраняем значение по умолчанию
		param.setValue(param.getDefault());
		//Ну и понеслась создавать панели!
		if(param instanceof Utils.ClassBuilder.BooleanConstructorParam<?> np){
			return new SettingsBoolean(clr,parametrFullName, np.getDefault(), e -> {np.setValue(e);propertyChange();});
		} else if(param instanceof Utils.ClassBuilder.StringConstructorParam<?> np){
			return new SettingsString(clr,parametrFullName, np.getDefault(),np.getDefault(), e -> {np.setValue( e);propertyChange();});
		} else if(param instanceof Utils.ClassBuilder.NumberConstructorParam<?,?> np_){
			final var npn = (Utils.ClassBuilder.NumberConstructorParam<? extends Number,?>) np_;
			final var def = npn.getDefault().getClass();
			if(def.equals(Integer.class)){
				final var np = (Utils.ClassBuilder.NumberConstructorParam<Integer,?>) npn;
				return new SettingsSlider<>(clr,parametrFullName, np.getSliderMinimum(),np.getDefault(),np.getSliderMaximum(),np.getRealMinimum(),np.getDefault(),np.getRealMaximum(), e -> {
					np.setValue( e);
					propertyChange();
				});
			} else if(def.equals(Long.class)){
				final var np = (Utils.ClassBuilder.NumberConstructorParam<Long,?>) npn;
				return new SettingsSlider<>(clr,parametrFullName, np.getSliderMinimum(),np.getDefault(),np.getSliderMaximum(),np.getRealMinimum(),np.getDefault(),np.getRealMaximum(), e -> {
					np.setValue( e);
					propertyChange();
				});
			} else if(def.equals(Double.class)){
				final var np = (Utils.ClassBuilder.NumberConstructorParam<Double,?>) npn;
				return new SettingsSlider<>(clr,parametrFullName, np.getSliderMinimum(),np.getDefault(),np.getSliderMaximum(),np.getRealMinimum(),np.getDefault(),np.getRealMaximum(), e -> {
					np.setValue( e);
					propertyChange();
				});
			} 
		} else if(param instanceof Utils.ClassBuilder.MapPointConstructorParam<?> np){
			return new SettingsPoint(clr,parametrFullName, np.getDefault(),np.getDefault(), e -> {np.setValue( e);propertyChange();});
		} else if(param instanceof Utils.ClassBuilder.MapPointVectorConstructorParam<?> np){
			final var panel = new javax.swing.JPanel();
			final var def = np.getDefault()[0];
			panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
			final var points = new ArrayList<Calculations.Point>();
			points.add(def);
			final var selectPoint = new int[1];
			selectPoint[0] = 0;
			
			build(clr, parametrFullName,panel,np,points,selectPoint);
			return panel;
		} else if(param instanceof Utils.ClassBuilder.Abstract2ConstructorParam<?> np){
			return new SettingsPoint(clr,parametrFullName, 
					np.get1Minimum(), np.get1Default(), np.get1Maximum(),np.get1Default(),
					np.get2Minimum(), np.get2Default(), np.get2Maximum(),np.get2Default(),  e -> {
				np.setValue( e);
				propertyChange();
			});
		}
		throw new AssertionError("Нет у нас реализации для " + String.valueOf(param));
	}
	/**
	 * Создаёт панель для ввода ряда точек
	 * @param clr класс, по которому будет искаться локализованное имя параметра
	 * @param parametrName имя параметра для локализованного имени
	 * @param panel панель, на которую надо нанести все нужные кнопки
	 * @param np непосредственно параметр, который создаётся
	 * @param points набор точек, которые уже задали
	 * @param selectPoint указатель на финальный объект индекса выбранной точки у этой панели
	 */
	private void build(final Class<?> clr, final String parametrName, javax.swing.JPanel panel, Utils.ClassBuilder.MapPointVectorConstructorParam<?> np, List<Calculations.Point> points, int[] selectPoint) {
		final var def = np.getDefault()[0];
		np.setValue(points.toArray(Point[]::new));
		panel.removeAll();
		for (int i = 0; i < points.size(); i++) {
			final var nowIndex = i;
			final var get = points.get(i);
			final var panelPoint = new javax.swing.JPanel();
			panelPoint.setLayout(new javax.swing.BoxLayout(panelPoint, javax.swing.BoxLayout.X_AXIS));
			if(i == selectPoint[0]){
				final var settings = new SettingsPoint(clr,parametrName, def,get, e -> {
					points.set(nowIndex, e);
					build(clr, parametrName,panel,np,points,selectPoint);
					propertyChange();
				});
				settings.setAlignmentY(java.awt.Component.CENTER_ALIGNMENT);
				panelPoint.add(settings);
			} else {
				final var label = new javax.swing.JLabel(get.toString());
				label.setToolTipText(Configurations.getHProperty(SettingsMake.class, "MapPointVectorConstructorParam.label"));
				label.addMouseListener(new java.awt.event.MouseAdapter() {
						@Override public void mouseClicked(MouseEvent e) {
							selectPoint[0] = nowIndex;
							build(clr, parametrName,panel,np,points,selectPoint);
						}
				});
				panelPoint.add(javax.swing.Box.createRigidArea(new Dimension(5,0))); //Отступ
				panelPoint.add(label);
			}
			panelPoint.add(javax.swing.Box.createHorizontalGlue()); //Связующее звено, чтобы следующая панелька была сбоку
			//Кнопка удалить не нужна, если точек меньше 2х
			final java.awt.event.ActionListener removeEvent = points.size() < 2 ? null : e -> {
				points.remove(get);
				if(selectPoint[0] >= nowIndex && selectPoint[0] > 0){
					selectPoint[0]--;
				}
				build(clr, parametrName,panel,np,points,selectPoint);
				propertyChange();
			};
			final java.awt.event.ActionListener addEvent = e -> {
				points.add(nowIndex+1, def);
				selectPoint[0] = nowIndex + 1;
				build(clr, parametrName,panel,np,points,selectPoint);
				propertyChange();
			};
			panelPoint.add(buildAddRemoveButton(removeEvent,addEvent));
			panel.add(panelPoint);
		}
		panel.updateUI();
	}
	/**
	 * Создаёт панельку с двумя кнопками - добавить и удалить
	 * @param removeEvent событе, при нажатии кнопки удалить. Может быть null, тогда кнопки удалить не будет
	 * @param addEvent событие для добавления точки, или что там надо добавить?
	 * @return панелька с заявленными кнопками
	 */
	private javax.swing.JPanel buildAddRemoveButton(java.awt.event.ActionListener removeEvent, java.awt.event.ActionListener addEvent){
		final var panelBottom = new javax.swing.JPanel();
		panelBottom.setLayout(new javax.swing.BoxLayout(panelBottom, javax.swing.BoxLayout.X_AXIS));
		if(removeEvent != null){
			final var remBut = new javax.swing.JButton("-");
			remBut.setToolTipText(Configurations.getHProperty(SettingsMake.class, "MapPointVectorConstructorParam.remove.L"));
			remBut.setContentAreaFilled(false);
			remBut.setMargin(new java.awt.Insets(0,3,0,3));
			remBut.addActionListener(removeEvent);
			panelBottom.add(remBut);
		}
		final var addBut = new javax.swing.JButton("+");
		addBut.setToolTipText(Configurations.getHProperty(SettingsMake.class, "MapPointVectorConstructorParam.add.L"));
		addBut.setContentAreaFilled(false);
		addBut.setMargin(new java.awt.Insets(0,2,0,2));
		addBut.addActionListener(addEvent);
		panelBottom.add(addBut);
		return panelBottom;
	}
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
    private javax.swing.JComboBox<ClassBuilder.Constructor> selectConstructor;
    private javax.swing.JComboBox<ClassBuilder> selectType;
    // End of variables declaration//GEN-END:variables

	/**Непосредтсвенно объект, который мы сгенерировали*/
	private Object _return = null;
	/**Слушатели событий изменения объекта*/
	private List<PropertyChangeListener> listeners = new ArrayList<>();

}
