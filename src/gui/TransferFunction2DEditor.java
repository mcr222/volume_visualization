/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JColorChooser;
import util.TFChangeListener;
import volume.GradientVolume;
import volume.Volume;
import volume.VoxelGradient;
import volvis.TFColor;

/**
 *
 * @author michel
 */
public class TransferFunction2DEditor extends javax.swing.JPanel {

    private Volume vol;
    private GradientVolume gradvol;
    private TransferFunction2DView tfView;
    public TriangleWidget triangleWidget;
    public TriangleWidget triangleWidget1;
    public int xbins, ybins;
    public double[] histogram;
    private short maxIntensity;
    public double maxGradientMagnitude;
    private ArrayList<TFChangeListener> listeners = new ArrayList<TFChangeListener>();

    
    public TransferFunction2DEditor(Volume volume, GradientVolume gradientvolume) {
        initComponents();

        this.vol = volume;
        this.gradvol = gradientvolume;
        compute2Dhistogram();

        this.tfView = new TransferFunction2DView(this);
        plotPanel.setLayout(new BorderLayout());
        plotPanel.add(tfView, BorderLayout.CENTER);
        labelGradMin.setText("0.0");
        labelGradMax.setText(Double.toString(Math.floor(10 * maxGradientMagnitude) / 10));
        labelMinVal.setText("0");
        labelMaxVal.setText(Integer.toString(maxIntensity));

        triangleWidget = new TriangleWidget((short) (maxIntensity / 4), 0.2, maxGradientMagnitude/2+1,maxGradientMagnitude/2-1);
        triangleWidget1 = new TriangleWidget((short) (3*maxIntensity / 4), 0.6, maxGradientMagnitude/2+1,maxGradientMagnitude/2-1);
        
        setSelectedInfo();
        
    }

    public void addTFChangeListener(TFChangeListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void changed() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).changed();
        }
    }

    private void compute2Dhistogram() {
        maxIntensity = vol.getMaximum();
        maxGradientMagnitude = gradvol.getMaxGradientMagnitude();

        System.out.println("maxIntensity = " + maxIntensity);
        System.out.println("max gradient = " + maxGradientMagnitude);

        xbins = maxIntensity + 1;
        ybins = 300;

        histogram = new double[xbins * ybins];
        int volumeSize = vol.getDimX() * vol.getDimY() * vol.getDimZ();
        for (int i = 0; i < volumeSize; i++) {
            short voxelVal = vol.getVoxel(i);
            VoxelGradient grad = gradvol.getVoxel(i);
            int yPos = (int) Math.floor(((ybins - 1) * grad.mag) / maxGradientMagnitude);
            histogram[yPos * xbins + voxelVal] += 1;
        }
    }

    public void setSelectedInfo() {
        intensityLabel.setText(Integer.toString(triangleWidget.baseIntensity));
        radiusLabel.setText(String.format("%.3f", triangleWidget.radius));
        opacityLabel.setText(String.format("%.1f", triangleWidget.color.a));
        colorButton.setBackground(new Color((float) triangleWidget.color.r, (float) triangleWidget.color.g, (float) triangleWidget.color.b));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        plotPanel = new javax.swing.JPanel();
        labelGradMax = new javax.swing.JLabel();
        labelGradMin = new javax.swing.JLabel();
        labelMinVal = new javax.swing.JLabel();
        labelMaxVal = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        colorButton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        opacityLabel = new javax.swing.JTextField();
        intensityLabel = new javax.swing.JTextField();
        radiusLabel = new javax.swing.JTextField();

        javax.swing.GroupLayout plotPanelLayout = new javax.swing.GroupLayout(plotPanel);
        plotPanel.setLayout(plotPanelLayout);
        plotPanelLayout.setHorizontalGroup(
            plotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        plotPanelLayout.setVerticalGroup(
            plotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 157, Short.MAX_VALUE)
        );

        labelGradMax.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelGradMax.setText("jLabel1");

        labelGradMin.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelGradMin.setText("jLabel2");

        labelMinVal.setText("jLabel3");

        labelMaxVal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelMaxVal.setText("jLabel4");

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setText("Gradient");

        jLabel2.setText("magnitude");

        jLabel3.setText("Intensity");

        jLabel4.setText("Intensity");

        jLabel5.setText("Opacity");

        jLabel6.setText("Color");

        colorButton.setBounds(new java.awt.Rectangle(0, 0, 75, 29));
        colorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorButtonActionPerformed(evt);
            }
        });

        jLabel7.setText("Radius");

        opacityLabel.setText("jTextField2");
        opacityLabel.setMinimumSize(new java.awt.Dimension(84, 28));
        opacityLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opacityLabelActionPerformed(evt);
            }
        });

        intensityLabel.setEditable(false);
        intensityLabel.setText("jTextField1");
        intensityLabel.setMinimumSize(new java.awt.Dimension(84, 28));

        radiusLabel.setEditable(false);
        radiusLabel.setText("jTextField3");
        radiusLabel.setMinimumSize(new java.awt.Dimension(84, 28));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(labelGradMin, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(labelGradMax, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelMinVal)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(opacityLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(intensityLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(colorButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 73, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(labelMaxVal)
                                .addContainerGap())
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radiusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(130, 130, 130))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(plotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelGradMax)
                        .addGap(45, 45, 45)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelGradMin))
                    .addComponent(plotPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelMaxVal, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(labelMinVal))
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(intensityLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(radiusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(opacityLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(colorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void colorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorButtonActionPerformed
        // TODO add your handling code here:
        Color newColor = JColorChooser.showDialog(this, "Choose color", colorButton.getBackground());
        if (newColor != null) {
            colorButton.setBackground(newColor);
            triangleWidget.color.r = newColor.getRed() / 255.0;
            triangleWidget.color.g = newColor.getGreen() / 255.0;
            triangleWidget.color.b = newColor.getBlue() / 255.0;
            changed();
        }
    }//GEN-LAST:event_colorButtonActionPerformed

    private void opacityLabelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opacityLabelActionPerformed
        try {
            double value = Double.parseDouble(opacityLabel.getText());
            if (value < 0) {
                value = 0;
            } 
            if (value > 1.0) {
                value = 1.0;
            }
            triangleWidget.color.a = value;
        } catch (NumberFormatException e) {
            triangleWidget.color.a = 0.2;
        }
        setSelectedInfo();
        changed();
    }//GEN-LAST:event_opacityLabelActionPerformed

    public class TriangleWidget {

        public short baseIntensity;
        public double radius;
        public double top;
        public double bottom;
        public TFColor color;
        

        public TriangleWidget(short base, double r, double top, double bottom) {
            this.baseIntensity = base;
            this.radius = r;
            this.top = top;
            this.bottom = bottom;
            this.color = new TFColor(0.0, 204.0/255.0, 153.0/255.0, 0.3);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton colorButton;
    private javax.swing.JTextField intensityLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel labelGradMax;
    private javax.swing.JLabel labelGradMin;
    private javax.swing.JLabel labelMaxVal;
    private javax.swing.JLabel labelMinVal;
    private javax.swing.JTextField opacityLabel;
    private javax.swing.JPanel plotPanel;
    private javax.swing.JTextField radiusLabel;
    // End of variables declaration//GEN-END:variables
}
