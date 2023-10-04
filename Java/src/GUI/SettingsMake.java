/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/AWTForms/Dialog.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import Utils.ClassBuilder;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

/**
 *Диалоговое окно создания объекта
 * @author Kerravitarr
 */
public class SettingsMake extends java.awt.Dialog {

	/** Creates new form SettingsMake
	 * @param modal окно модальное?
	 * @param childList список всех возможных подтипов текущего объекта
	 */
	public SettingsMake(boolean modal, List<ClassBuilder> childList) {
		super((Frame)null, Configurations.getProperty(SettingsMake.class,"title"), ModalityType.APPLICATION_MODAL);
		initComponents();
		
		final var values = new javax.swing.DefaultComboBoxModel();
		for(final var c : childList){
			values.addElement(c);
		}
		selectType.setModel(values);
		selectType.setSelectedIndex(0);
		selectType.setEnabled(childList.size() > 1);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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

        add(jPanel1, java.awt.BorderLayout.SOUTH);

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

        add(jPanel2, java.awt.BorderLayout.CENTER);

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
        jPanel1.updateUI();
		jPanel2.updateUI();
    }//GEN-LAST:event_formWindowOpened

    private void selectConstructorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectConstructorActionPerformed
		new Thread(()->{
			final var type = (ClassBuilder)selectType.getSelectedItem();
			final var constructor = (ClassBuilder.Constructor) selectConstructor.getSelectedItem();
			paramPanel.removeAll();
			for(final var param : constructor.getParams()){
				paramPanel.add(addPanel(type.printName(),constructor.name(),(ClassBuilder.ConstructorParametr) param));
			}
			paramPanel.updateUI();
		}).start();
    }//GEN-LAST:event_selectConstructorActionPerformed

    private void canselActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_canselActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_canselActionPerformed

    private void generateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateActionPerformed
        final var constructor = (ClassBuilder.Constructor) selectConstructor.getSelectedItem();
		_return = constructor.build();
		closeDialog(null);
    }//GEN-LAST:event_generateActionPerformed
	/**Создаёт и добавляет панель с нужными параметрами
	 * @param param 
	 */
	private javax.swing.JPanel addPanel(final Class<?> clr,final String constructorName, ClassBuilder.ConstructorParametr param) {
		final String parametrName;
		if(constructorName.isEmpty())
			parametrName = String.format("constructor.parameter.%s", param.name());
		else
			parametrName = String.format("constructor.%s.parameter.%s", constructorName,param.name());
		if(param instanceof Utils.ClassBuilder.BooleanConstructorParam<?> np){
			return new SettingsBoolean(clr,parametrName, np.getDefault(), e -> {np.setValue(e);});
		} else if(param instanceof Utils.ClassBuilder.StringConstructorParam<?> np){
			return new SettingsString(clr,parametrName, np.getDefault(),np.getDefault(), e -> {np.setValue( e);});
		} else if(param instanceof Utils.ClassBuilder.NumberConstructorParam<?,?> np_){
			final var npn = (Utils.ClassBuilder.NumberConstructorParam<? extends Number,?>) np_;
			final var def = npn.getDefault().getClass();
			if(def.equals(Integer.class)){
				final var np = (Utils.ClassBuilder.NumberConstructorParam<Integer,?>) npn;
				return new SettingsSlider<>(clr,parametrName, np.getSliderMinimum(),np.getDefault(),np.getSliderMaximum(),np.getRealMinimum(),np.getDefault(),np.getRealMaximum(), e -> {
					np.setValue( e);
				});
			} else if(def.equals(Double.class)){
				final var np = (Utils.ClassBuilder.NumberConstructorParam<Double,?>) npn;
				return new SettingsSlider<>(clr,parametrName, np.getSliderMinimum(),np.getDefault(),np.getSliderMaximum(),np.getRealMinimum(),np.getDefault(),np.getRealMaximum(), e -> {
					np.setValue( e);
				});
			} 
		} else if(param instanceof Utils.ClassBuilder.MapPointConstructorParam<?> np){
			return new SettingsPoint(clr,parametrName, np.getDefault(),np.getDefault(), e -> {np.setValue( e);});
		} else if(param instanceof Utils.ClassBuilder.MapPointVectorConstructorParam<?> np){
			final var panel = new javax.swing.JPanel();
			final var def = np.getDefault()[0];
			panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
			/*final var points = new ArrayList<Calculations.Point>();
			final var newP = new javax.swing.JButton(Configurations.getHProperty(SettingsMake.class, "newPoint.L"));
			newP.setToolTipText(Configurations.getHProperty(Settings.class, "newPoint.T"));
			newP.addActionListener( e -> {
				final var set = new SettingsPoint(clr, parametrName, def, def, p -> {
					points.add(p);
				});
				panel.add(set);
			});*/
		}
		
		throw new AssertionError("Нет у нас реализации для " + String.valueOf(param));
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel paramPanel;
    private javax.swing.JComboBox<ClassBuilder.Constructor> selectConstructor;
    private javax.swing.JComboBox<ClassBuilder> selectType;
    // End of variables declaration//GEN-END:variables

	/**Непосредтсвенно объект, который мы сгенерировали*/
	private Object _return = null;

}
