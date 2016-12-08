/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import gui.TransferFunction2DEditor.TriangleWidget;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author michel
 */
public class TransferFunction2DView extends javax.swing.JPanel {

    TransferFunction2DEditor ed;
    private final int DOTSIZE = 8;
    public Ellipse2D.Double baseControlPoint, radiusControlPoint, topControlPoint, bottomControlPoint;
    boolean selectedBaseControlPoint, selectedRadiusControlPoint, selectedTopControlPoint, selectedBottomControlPoint;
    public Ellipse2D.Double baseControlPoint1, radiusControlPoint1, topControlPoint1, bottomControlPoint1;
    boolean selectedBaseControlPoint1, selectedRadiusControlPoint1, selectedTopControlPoint1, selectedBottomControlPoint1;
    private double maxHistoMagnitude;
    
    /**
     * Creates new form TransferFunction2DView
     * @param ed
     */
    public TransferFunction2DView(TransferFunction2DEditor ed) {
        initComponents();
        
        this.ed = ed;
        selectedBaseControlPoint = false;
        selectedRadiusControlPoint = false;
        selectedTopControlPoint = false;
        selectedBaseControlPoint1 = false;
        selectedRadiusControlPoint1 = false;
        selectedTopControlPoint1 = false;
        addMouseMotionListener(new TriangleWidgetHandler());
        addMouseListener(new SelectionHandler());
    }
    
    @Override
    public void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;

        int w = this.getWidth();
        int h = this.getHeight();
        g2.setColor(Color.white);
        g2.fillRect(0, 0, w, h);
        
        maxHistoMagnitude = ed.histogram[0];
        for (int i = 0; i < ed.histogram.length; i++) {
            maxHistoMagnitude = ed.histogram[i] > maxHistoMagnitude ? ed.histogram[i] : maxHistoMagnitude;
        }
        
        double binWidth = (double) w / (double) ed.xbins;
        double binHeight = (double) h / (double) ed.ybins;
        maxHistoMagnitude = Math.log(maxHistoMagnitude);
        
        for (int y = 0; y < ed.ybins; y++) {
            for (int x = 0; x < ed.xbins; x++) {
                if (ed.histogram[y * ed.xbins + x] > 0) {
                    int intensity = (int) Math.floor(255 * (1.0 - Math.log(ed.histogram[y * ed.xbins + x]) / maxHistoMagnitude));
                    g2.setColor(new Color(intensity, intensity, intensity));
                    g2.fill(new Rectangle2D.Double(x * binWidth, h - (y * binHeight), binWidth, binHeight));
                }
            }
        }
        this.paintTriangle(g2, ed.triangleWidget, h, binWidth, 0);
        this.paintTriangle(g2, ed.triangleWidget1, h, binWidth, 1);

    }
    
    private void paintTriangle(Graphics2D g2, TriangleWidget triangle, int h, double binWidth,int triangleNumber) {
        int ypos = h;
        int xpos = (int) (triangle.baseIntensity * binWidth);

        int vertPosTop = (int) ((1-triangle.top/ed.maxGradientMagnitude)*h);
        int vertPosBottom = (int) ((1-triangle.bottom/ed.maxGradientMagnitude)*h);

        g2.setColor(Color.black);
        if(triangleNumber==0) {
            baseControlPoint = new Ellipse2D.Double(xpos - DOTSIZE / 2, ypos - DOTSIZE, DOTSIZE, DOTSIZE);
            g2.fill(baseControlPoint);
        } else {
            baseControlPoint1 = new Ellipse2D.Double(xpos - DOTSIZE / 2, ypos - DOTSIZE, DOTSIZE, DOTSIZE);
            g2.fill(baseControlPoint1);
        }
        g2.drawLine(xpos, ypos, xpos - (int) (triangle.radius * binWidth * ed.maxGradientMagnitude), 0);
        g2.drawLine(xpos, ypos, xpos + (int) (triangle.radius * binWidth * ed.maxGradientMagnitude), 0);
        if(triangleNumber==0) {
            radiusControlPoint = new Ellipse2D.Double(xpos + (triangle.radius * binWidth * ed.maxGradientMagnitude) - DOTSIZE / 2,  0, DOTSIZE, DOTSIZE);
            g2.fill(radiusControlPoint);
        } else {
            radiusControlPoint1 = new Ellipse2D.Double(xpos + (triangle.radius * binWidth * ed.maxGradientMagnitude) - DOTSIZE / 2,  0, DOTSIZE, DOTSIZE);
            g2.fill(radiusControlPoint1);
        }
        
        int horPosTop = (int) (triangle.radius * binWidth * triangle.top);
        g2.drawLine(xpos - horPosTop, vertPosTop, 
                xpos + horPosTop, vertPosTop);
        g2.drawLine(xpos- horPosTop-DOTSIZE/2, vertPosTop,xpos- horPosTop+DOTSIZE/2,vertPosTop+DOTSIZE);
        g2.drawLine(xpos- horPosTop+DOTSIZE/2, vertPosTop,xpos- horPosTop+DOTSIZE/2,vertPosTop+DOTSIZE);
        if(triangleNumber==0) {
            topControlPoint = new Ellipse2D.Double(xpos- horPosTop,  vertPosTop-DOTSIZE / 2, DOTSIZE, DOTSIZE);
            g2.fill(topControlPoint);
        } else {
            topControlPoint1 = new Ellipse2D.Double(xpos- horPosTop,  vertPosTop-DOTSIZE / 2, DOTSIZE, DOTSIZE);
            g2.fill(topControlPoint1);
        }
        
        int horPosBottom = (int) (triangle.radius * binWidth * triangle.bottom);
        g2.drawLine(xpos-horPosBottom, vertPosBottom, xpos+horPosBottom, vertPosBottom);
        g2.drawLine(xpos-horPosBottom-DOTSIZE/2, vertPosBottom,xpos-horPosBottom+DOTSIZE/2,vertPosBottom-DOTSIZE);
        g2.drawLine(xpos-horPosBottom+DOTSIZE/2, vertPosBottom,xpos-horPosBottom+DOTSIZE/2,vertPosBottom-DOTSIZE);
        if(triangleNumber==0){
            bottomControlPoint = new Ellipse2D.Double(xpos-horPosBottom,  vertPosBottom-DOTSIZE / 2, DOTSIZE, DOTSIZE);
            g2.fill(bottomControlPoint);
        } else {
            bottomControlPoint1 = new Ellipse2D.Double(xpos-horPosBottom,  vertPosBottom-DOTSIZE / 2, DOTSIZE, DOTSIZE);
            g2.fill(bottomControlPoint1);
        }
    }
    
    
    private class TriangleWidgetHandler extends MouseMotionAdapter {

        @Override
        public void mouseMoved(MouseEvent e) {
            if (baseControlPoint.contains(e.getPoint()) || radiusControlPoint.contains(e.getPoint())|| topControlPoint.contains(e.getPoint())|| bottomControlPoint.contains(e.getPoint())
                || baseControlPoint1.contains(e.getPoint()) || radiusControlPoint1.contains(e.getPoint())|| topControlPoint1.contains(e.getPoint())|| bottomControlPoint1.contains(e.getPoint())){
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                setCursor(Cursor.getDefaultCursor());
            }
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            if (selectedBaseControlPoint || selectedRadiusControlPoint || selectedTopControlPoint || selectedBottomControlPoint
                    || selectedBaseControlPoint1 || selectedRadiusControlPoint1 || selectedTopControlPoint1 || selectedBottomControlPoint1) {
                Point dragEnd = e.getPoint();
                
                if (selectedBaseControlPoint) {
                    // restrain to horizontal movement
                    dragEnd.setLocation(dragEnd.x, baseControlPoint.getCenterY());
                } else if (selectedRadiusControlPoint) {
                    // restrain to horizontal movement and avoid radius getting 0
                    dragEnd.setLocation(dragEnd.x, radiusControlPoint.getCenterY());
                    if (dragEnd.x - baseControlPoint.getCenterX() <= 0) {
                        dragEnd.x = (int) (baseControlPoint.getCenterX() + 1);
                    }
                } else if(selectedTopControlPoint){
                    dragEnd.setLocation(topControlPoint.getCenterX(),dragEnd.y);
                    if(dragEnd.y >= bottomControlPoint.getCenterY()){
                        dragEnd.setLocation(topControlPoint.getCenterX(),bottomControlPoint.getCenterY()-2);
                    }
                } else if(selectedBottomControlPoint) {
                    dragEnd.setLocation(bottomControlPoint.getCenterX(),dragEnd.y);
                    if(dragEnd.y <= topControlPoint.getCenterY()){
                        dragEnd.setLocation(bottomControlPoint.getCenterX(),topControlPoint.getCenterY()+2);
                    }
                } else if (selectedBaseControlPoint1) {
                    // restrain to horizontal movement
                    dragEnd.setLocation(dragEnd.x, baseControlPoint1.getCenterY());
                } else if (selectedRadiusControlPoint1) {
                    // restrain to horizontal movement and avoid radius getting 0
                    dragEnd.setLocation(dragEnd.x, radiusControlPoint1.getCenterY());
                    if (dragEnd.x - baseControlPoint1.getCenterX() <= 0) {
                        dragEnd.x = (int) (baseControlPoint1.getCenterX() + 1);
                    }
                } else if(selectedTopControlPoint1){
                    dragEnd.setLocation(topControlPoint1.getCenterX(),dragEnd.y);
                    if(dragEnd.y >= bottomControlPoint1.getCenterY()){
                        dragEnd.setLocation(topControlPoint1.getCenterX(),bottomControlPoint1.getCenterY()-2);
                    }
                } else if(selectedBottomControlPoint1) {
                    dragEnd.setLocation(bottomControlPoint1.getCenterX(),dragEnd.y);
                    if(dragEnd.y <= topControlPoint1.getCenterY()){
                        dragEnd.setLocation(bottomControlPoint1.getCenterX(),topControlPoint1.getCenterY()+2);
                    }
                }
                
                if (dragEnd.x < 0) {
                    dragEnd.x = 0;
                }
                if (dragEnd.x >= getWidth()) {
                    dragEnd.x = getWidth() - 1;
                }
                
                if (dragEnd.y < 0) {
                    dragEnd.y = 0;
                }
                if (dragEnd.y >= getHeight()) {
                    dragEnd.y = getHeight() - 1;
                }
                double w = getWidth();
                double h = getHeight();
                double binWidth = (double) w / (double) ed.xbins;
                if (selectedBaseControlPoint) {
                    ed.triangleWidget.baseIntensity = (short) (dragEnd.x / binWidth);
                } else if (selectedRadiusControlPoint) {
                    ed.triangleWidget.radius = (dragEnd.x - (ed.triangleWidget.baseIntensity * binWidth))/(binWidth*ed.maxGradientMagnitude);
                } else if(selectedTopControlPoint) {
                    ed.triangleWidget.top = (1 - dragEnd.y/h)*ed.maxGradientMagnitude;
                } else if(selectedBottomControlPoint) {
                    ed.triangleWidget.bottom = (1 - dragEnd.y/h)*ed.maxGradientMagnitude;
                } else if (selectedBaseControlPoint1) {
                    ed.triangleWidget1.baseIntensity = (short) (dragEnd.x / binWidth);
                } else if (selectedRadiusControlPoint1) {
                    ed.triangleWidget1.radius = (dragEnd.x - (ed.triangleWidget1.baseIntensity * binWidth))/(binWidth*ed.maxGradientMagnitude);
                } else if(selectedTopControlPoint1) {
                    ed.triangleWidget1.top = (1 - dragEnd.y/h)*ed.maxGradientMagnitude;
                } else if(selectedBottomControlPoint1) {
                    ed.triangleWidget1.bottom = (1 - dragEnd.y/h)*ed.maxGradientMagnitude;
                }
                
                ed.setSelectedInfo();
                
                repaint();
            } 
        }

    }
    
    
    private class SelectionHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (baseControlPoint.contains(e.getPoint())) {
                selectedBaseControlPoint = true;
            } else if (radiusControlPoint.contains(e.getPoint())) {
                selectedRadiusControlPoint = true;
            } else if(topControlPoint.contains(e.getPoint())){
                selectedTopControlPoint = true;
            } else if(bottomControlPoint.contains(e.getPoint())){
                selectedBottomControlPoint = true;
            } else if (baseControlPoint1.contains(e.getPoint())) {
                selectedBaseControlPoint1 = true;
            } else if (radiusControlPoint1.contains(e.getPoint())) {
                selectedRadiusControlPoint1 = true;
            } else if(topControlPoint1.contains(e.getPoint())){
                selectedTopControlPoint1 = true;
            } else if(bottomControlPoint1.contains(e.getPoint())){
                selectedBottomControlPoint1 = true;
            }
            else {
                selectedRadiusControlPoint = false;
                selectedBaseControlPoint = false;
                selectedTopControlPoint = false;
                selectedBottomControlPoint = false;
                selectedRadiusControlPoint1 = false;
                selectedBaseControlPoint1 = false;
                selectedTopControlPoint1 = false;
                selectedBottomControlPoint1 = false;
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            selectedRadiusControlPoint = false;
            selectedBaseControlPoint = false;
            selectedTopControlPoint = false;
            selectedBottomControlPoint = false;
            selectedRadiusControlPoint1 = false;
            selectedBaseControlPoint1 = false;
            selectedTopControlPoint1 = false;
            selectedBottomControlPoint1 = false;
            ed.changed();
            repaint();
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
