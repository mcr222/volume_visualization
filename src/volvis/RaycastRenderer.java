/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volvis;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import gui.RaycastRendererPanel;
import gui.TransferFunction2DEditor;
import gui.TransferFunctionEditor;
import java.awt.image.BufferedImage;
import util.TFChangeListener;
import util.VectorMath;
import volume.GradientVolume;
import volume.Volume;
import java.lang.Math;

/**
 *
 * @author michel
 */
public class RaycastRenderer extends Renderer implements TFChangeListener {

    private Volume volume = null;
    private GradientVolume gradients = null;
    RaycastRendererPanel panel;
    TransferFunction tFunc;
    TransferFunctionEditor tfEditor;
    TransferFunction2DEditor tfEditor2D;
    
    public enum raycastModes {
        slicer,mip,compositing, transformationfunct
    }
    private raycastModes raycastMode;

    
    public RaycastRenderer() {
        raycastMode = raycastModes.slicer;
        panel = new RaycastRendererPanel(this);
        panel.setSpeedLabel("0");
    }

    public void setRaycastMode(raycastModes raycastMode) {
        this.raycastMode = raycastMode;
    }
    
    public void setVolume(Volume vol) {
        System.out.println("Assigning volume");
        volume = vol;

        System.out.println("Computing gradients");
        gradients = new GradientVolume(vol);

        // set up image for storing the resulting rendering
        // the image width and height are equal to the length of the volume diagonal
        int imageSize = (int) Math.floor(Math.sqrt(vol.getDimX() * vol.getDimX() + vol.getDimY() * vol.getDimY()
                + vol.getDimZ() * vol.getDimZ()));
        if (imageSize % 2 != 0) {
            imageSize = imageSize + 1;
        }
        image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
        // create a standard TF where lowest intensity maps to black, the highest to white, and opacity increases
        // linearly from 0.0 to 1.0 over the intensity range
        tFunc = new TransferFunction(volume.getMinimum(), volume.getMaximum());
        
        // uncomment this to initialize the TF with good starting values for the orange dataset 
        tFunc.setTestFunc();
        
        
        tFunc.addTFChangeListener(this);
        tfEditor = new TransferFunctionEditor(tFunc, volume.getHistogram());
        
        tfEditor2D = new TransferFunction2DEditor(volume, gradients);
        tfEditor2D.addTFChangeListener(this);

        System.out.println("Finished initialization of RaycastRenderer");
    }

    public RaycastRendererPanel getPanel() {
        return panel;
    }

    public TransferFunction2DEditor getTF2DPanel() {
        return tfEditor2D;
    }
    
    public TransferFunctionEditor getTFPanel() {
        return tfEditor;
    }
    
    private class PointsInLine {
        
        private double[] q0=null;
        private double[] q1=null;
        
        public PointsInLine(double[] viewVec, double[] uVec, double[] vVec, int i, int j){
            int imageCenter = image.getWidth() / 2;
            double[] volumeCenter = new double[3];
            VectorMath.setVector(volumeCenter, volume.getDimX() / 2, volume.getDimY() / 2, volume.getDimZ() / 2);
            double[] pointLine = {uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter) + volumeCenter[0],
                uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)+ volumeCenter[1],
                uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)+ volumeCenter[2]};
            /*System.out.println("pointLine");
            System.out.println(pointLine[0]);
            System.out.println(pointLine[1]);
            System.out.println(pointLine[2]);
            System.out.println("viewVec");
            System.out.println(viewVec[0]);
            System.out.println(viewVec[1]);
            System.out.println(viewVec[2]);*/
            double maxX = volume.getDimX();
            double maxY = volume.getDimY();
            double maxZ = volume.getDimZ();
            double[][] normalVectors = {{1,0,0},{0,1,0},{0,0,1}};
            double[][] pointsPlanes = {{0,0,0},{maxX,maxY,maxZ}};
            double[] intersectionPoint;
            for(int ni=0; ni<3; ++ni) {
                for(int nj=0; nj<2; ++nj) {
                    intersectionPoint = findIntersection(pointLine,viewVec,pointsPlanes[nj],normalVectors[ni]);
                    /*System.out.println("intersectionPoint");
                    System.out.println(intersectionPoint);*/
                    
                    if(intersectionPoint!=null) {
                        if(q0==null) {
                            q0 = intersectionPoint;
                        } else if(Math.abs(q0[0]-intersectionPoint[0])>0.01 || Math.abs(q0[1]-intersectionPoint[1])>0.01 || Math.abs(q0[2]-intersectionPoint[2])>0.01) {
                            double[] distVectq0 = {q0[0]-pointLine[0],q0[1]-pointLine[1],q0[2]-pointLine[2]};
                            double[] distVectint= {intersectionPoint[0]-pointLine[0],intersectionPoint[1]-pointLine[1],intersectionPoint[2]-pointLine[2]};
                            if(scalarMultiplication(distVectq0,distVectq0)>scalarMultiplication(distVectint,distVectint)) {
                                q1 = q0;
                                q0 = intersectionPoint;
                            } else {
                                q1 = intersectionPoint;
                            }
                        }
                    }
                }
            }
            /*System.out.println("q0");
            System.out.println(q0);
            System.out.println("q1");
            System.out.println(q1);*/
        }
        
        public boolean isThereIntersection() {
            if(q0==null && q1==null) {
                //System.out.println("no intersection");
            }
            return q0!=null && q1!=null;
        }
        
        private double scalarMultiplication(double[] x1, double[] x2) {
            double mult = 0;
            for(int i=0;i<3;++i){
                mult += x1[i]*x2[i];
            }
            return mult;
        }
        
        private double[] findIntersection(double[] pointLine, double[] vectorLine, double[] pointPlane, double[] normalPlane){
            double den = scalarMultiplication(vectorLine, normalPlane);
            if(den==0) {
                return null;
            }
            double[] diff = {pointPlane[0]-pointLine[0],pointPlane[1]-pointLine[1],pointPlane[2]-pointLine[2]};
            double d = scalarMultiplication(diff,normalPlane)/den;
            double[] intersectionPoint = {d*vectorLine[0]+pointLine[0],d*vectorLine[1]+pointLine[1],d*vectorLine[2]+pointLine[2]};
            if(coordinatesInRange(intersectionPoint)) {
                return intersectionPoint;
            }
            return null;
        }
        
        public short getPointInLine(double k){
            double[] pointsInLine = new double[3];
            pointsInLine[0] = q0[0]+k*(q1[0]-q0[0]);
            pointsInLine[1] = q0[1]+k*(q1[1]-q0[1]);
            pointsInLine[2] = q0[2]+k*(q1[2]-q0[2]);
            /*System.out.println(k);
            System.out.println(coordinatesInRange(q0));
            System.out.println(coordinatesInRange(q1));
            System.out.println(coordinatesInRange(pointsInLine));*/
            return getVoxel(pointsInLine);
        }
    }
    
    
    public boolean coordinatesInRange(double[] coord) {
        return !(coord[0] < 0 || coord[0] > volume.getDimX() || coord[1] < 0 || coord[1] > volume.getDimY()
                || coord[2] < 0 || coord[2] > volume.getDimZ());
    }
    
    short getVoxel(double[] coord) {

        if (!coordinatesInRange(coord)) {
            return 0;
        }

        int x = (int) Math.floor(coord[0]);
        int y = (int) Math.floor(coord[1]);
        int z = (int) Math.floor(coord[2]);
        try {
            return volume.getVoxel(x, y, z);
        }catch(Exception e){
            /*System.out.println("wrong value");
            System.out.println(x);
            System.out.println(y);
            System.out.println(z);*/
            
            return 0;
        }
    }


    void slicer(double[] viewMatrix) {
        /*System.out.println("ViewMatrix");
        for(int i=0;i<viewMatrix.length;i++){
            System.out.print(viewMatrix[i]+" ");
            
        }*/
        // clear image
        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                image.setRGB(i, j, 0);
            }
        }

        // vector uVec and vVec define a plane through the origin, 
        // perpendicular to the view vector viewVec
        double[] viewVec = new double[3];
        double[] uVec = new double[3];
        double[] vVec = new double[3];
        VectorMath.setVector(viewVec, viewMatrix[2], viewMatrix[6], viewMatrix[10]);
        VectorMath.setVector(uVec, viewMatrix[0], viewMatrix[4], viewMatrix[8]);
        VectorMath.setVector(vVec, viewMatrix[1], viewMatrix[5], viewMatrix[9]);
        /*System.out.println("viewVec");
        for(int i=0;i<viewVec.length;i++){
            System.out.print(viewVec[i]+" ");
        }
        System.out.println("uVec");
        for(int i=0;i<uVec.length;i++){
            System.out.print(uVec[i]+" ");
        }
        System.out.println("vVec");
        for(int i=0;i<vVec.length;i++){
            System.out.print(vVec[i]+" ");
        }*/
        // image is square
        int imageCenter = image.getWidth() / 2;

        double[] pixelCoord = new double[3];
        double[] volumeCenter = new double[3];
        VectorMath.setVector(volumeCenter, volume.getDimX() / 2, volume.getDimY() / 2, volume.getDimZ() / 2);

        // sample on a plane through the origin of the volume data
        double max = volume.getMaximum();
        TFColor voxelColor = new TFColor();

        
        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                pixelCoord[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter)
                        + volumeCenter[0];
                pixelCoord[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)
                        + volumeCenter[1];
                pixelCoord[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)
                        + volumeCenter[2];

                int val = getVoxel(pixelCoord);
                
                // Map the intensity to a grey value by linear scaling
                voxelColor.r = val/max;
                voxelColor.g = voxelColor.r;
                voxelColor.b = voxelColor.r;
                voxelColor.a = val > 0 ? 1.0 : 0.0;  // this makes intensity 0 completely transparent and the rest opaque
                // Alternatively, apply the transfer function to obtain a color
                // voxelColor = tFunc.getColor(val);
                
                
                // BufferedImage expects a pixel color packed as ARGB in an int
                int c_alpha = voxelColor.a <= 1.0 ? (int) Math.floor(voxelColor.a * 255) : 255;
                int c_red = voxelColor.r <= 1.0 ? (int) Math.floor(voxelColor.r * 255) : 255;
                int c_green = voxelColor.g <= 1.0 ? (int) Math.floor(voxelColor.g * 255) : 255;
                int c_blue = voxelColor.b <= 1.0 ? (int) Math.floor(voxelColor.b * 255) : 255;
                int pixelColor = (c_alpha << 24) | (c_red << 16) | (c_green << 8) | c_blue;
                image.setRGB(i, j, pixelColor);
            }
        }

    }

    void MIP(double[] viewMatrix) {
        // clear image
        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                image.setRGB(i, j, 0);
            }
        }

        // vector uVec and vVec define a plane through the origin, 
        // perpendicular to the view vector viewVec
        double[] viewVec = new double[3];
        double[] uVec = new double[3];
        double[] vVec = new double[3];
        VectorMath.setVector(viewVec, viewMatrix[2], viewMatrix[6], viewMatrix[10]);
        VectorMath.setVector(uVec, viewMatrix[0], viewMatrix[4], viewMatrix[8]);
        VectorMath.setVector(vVec, viewMatrix[1], viewMatrix[5], viewMatrix[9]);
        /*System.out.println("dimensions");
        System.out.println(volume.getDimX());
        System.out.println(volume.getDimY());
        System.out.println(volume.getDimZ());*/
        // sample on a plane through the origin of the volume data
        double max = volume.getMaximum();
        TFColor voxelColor = new TFColor();
        int max_val,val;
        double k;
        double kspacing=0.01;
        PointsInLine pointsInLine;

        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                pointsInLine = new PointsInLine(viewVec,uVec,vVec,i,j);
                k=0;
                max_val = 0;
                while(pointsInLine.isThereIntersection() &&  k<=1) {
                    val = pointsInLine.getPointInLine(k);
                    if(val > max_val) {
                        max_val = val;   
                    }
                    k=k+kspacing;
                }
                
                // Map the intensity to a grey value by linear scaling
                voxelColor.r = max_val/max;
                voxelColor.g = voxelColor.r;
                voxelColor.b = voxelColor.r;
                voxelColor.a = max_val > 0 ? 1.0 : 0.0;  // this makes intensity 0 completely transparent and the rest opaque
                // Alternatively, apply the transfer function to obtain a color
                //voxelColor = tFunc.getColor(max_val);
                
                
                // BufferedImage expects a pixel color packed as ARGB in an int
                int c_alpha = voxelColor.a <= 1.0 ? (int) Math.floor(voxelColor.a * 255) : 255;
                int c_red = voxelColor.r <= 1.0 ? (int) Math.floor(voxelColor.r * 255) : 255;
                int c_green = voxelColor.g <= 1.0 ? (int) Math.floor(voxelColor.g * 255) : 255;
                int c_blue = voxelColor.b <= 1.0 ? (int) Math.floor(voxelColor.b * 255) : 255;
                int pixelColor = (c_alpha << 24) | (c_red << 16) | (c_green << 8) | c_blue;
                image.setRGB(i, j, pixelColor);
            }
        }

    }
    void compositing(double[] viewMatrix) {
        // clear image
        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                image.setRGB(i, j, 0);
            }
        }
        // vector uVec and vVec define a plane through the origin, 
        // perpendicular to the view vector viewVec
        double[] viewVec = new double[3];
        double[] uVec = new double[3];
        double[] vVec = new double[3];
        VectorMath.setVector(viewVec, viewMatrix[2], viewMatrix[6], viewMatrix[10]);
        VectorMath.setVector(uVec, viewMatrix[0], viewMatrix[4], viewMatrix[8]);
        VectorMath.setVector(vVec, viewMatrix[1], viewMatrix[5], viewMatrix[9]);
        /*System.out.println("dimensions");
        System.out.println(volume.getDimX());
        System.out.println(volume.getDimY());
        System.out.println(volume.getDimZ());*/
        // sample on a plane through the origin of the volume data
        double max = volume.getMaximum();
        TFColor voxelColor = new TFColor();
        int val= 0;
        double k;
        double kspacing=0.01;
        PointsInLine pointsInLine;

        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                pointsInLine = new PointsInLine(viewVec,uVec,vVec,i,j);
                k=1;
                TFColor acumVoxelColor = new TFColor(0,0,0,0);
                while(pointsInLine.isThereIntersection() &&  k>=0) {
                    val = pointsInLine.getPointInLine(k);               
                    voxelColor = tFunc.getColor(val);
                    
                    acumVoxelColor.r = voxelColor.r*(1-voxelColor.a)+voxelColor.a*acumVoxelColor.r;
                    acumVoxelColor.g = voxelColor.g*(1-voxelColor.a)+voxelColor.a*acumVoxelColor.g;
                    acumVoxelColor.b = voxelColor.b*(1-voxelColor.a)+voxelColor.a*acumVoxelColor.b;
                    
                    k=k-kspacing;
                }
                // Map the intensity to a grey value by linear scaling
                voxelColor.r = acumVoxelColor.r;
                voxelColor.g = acumVoxelColor.g;
                voxelColor.b = acumVoxelColor.b;
                voxelColor.a = 1;  // this makes intensity 0 completely transparent and the rest opaque
                // Alternatively, apply the transfer function to obtain a color
                
                
                
                // BufferedImage expects a pixel color packed as ARGB in an int
                int c_alpha = voxelColor.a <= 1.0 ? (int) Math.floor(voxelColor.a * 255) : 255;
                int c_red = voxelColor.r <= 1.0 ? (int) Math.floor(voxelColor.r * 255) : 255;
                int c_green = voxelColor.g <= 1.0 ? (int) Math.floor(voxelColor.g * 255) : 255;
                int c_blue = voxelColor.b <= 1.0 ? (int) Math.floor(voxelColor.b * 255) : 255;
                int pixelColor = (c_alpha << 24) | (c_red << 16) | (c_green << 8) | c_blue;
                image.setRGB(i, j, pixelColor);
            }
        }

    }

    private void drawBoundingBox(GL2 gl) {
        gl.glPushAttrib(GL2.GL_CURRENT_BIT);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glColor4d(1.0, 1.0, 1.0, 1.0);
        gl.glLineWidth(1.5f);
        gl.glEnable(GL.GL_LINE_SMOOTH);
        gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glDisable(GL.GL_LINE_SMOOTH);
        gl.glDisable(GL.GL_BLEND);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glPopAttrib();

    }

    @Override
    public void visualize(GL2 gl) {

        if (volume == null) {
            return;
        }

        drawBoundingBox(gl);

        gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, viewMatrix, 0);

        long startTime = System.currentTimeMillis();
        
        if(raycastMode == raycastModes.mip) {
            MIP(viewMatrix);
        }
        else if(raycastMode == raycastModes.slicer){
            slicer(viewMatrix);        
        }
        else{
            compositing(viewMatrix); 
                }
        
        long endTime = System.currentTimeMillis();
        double runningTime = (endTime - startTime);
        panel.setSpeedLabel(Double.toString(runningTime));

        Texture texture = AWTTextureIO.newTexture(gl.getGLProfile(), image, false);

        gl.glPushAttrib(GL2.GL_LIGHTING_BIT);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        // draw rendered image as a billboard texture
        texture.enable(gl);
        texture.bind(gl);
        double halfWidth = image.getWidth() / 2.0;
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glTexCoord2d(0.0, 0.0);
        gl.glVertex3d(-halfWidth, -halfWidth, 0.0);
        gl.glTexCoord2d(0.0, 1.0);
        gl.glVertex3d(-halfWidth, halfWidth, 0.0);
        gl.glTexCoord2d(1.0, 1.0);
        gl.glVertex3d(halfWidth, halfWidth, 0.0);
        gl.glTexCoord2d(1.0, 0.0);
        gl.glVertex3d(halfWidth, -halfWidth, 0.0);
        gl.glEnd();
        texture.disable(gl);
        texture.destroy(gl);
        gl.glPopMatrix();

        gl.glPopAttrib();


        if (gl.glGetError() > 0) {
            System.out.println("some OpenGL error: " + gl.glGetError());
        }

    }
    private BufferedImage image;
    private double[] viewMatrix = new double[4 * 4];

    @Override
    public void changed() {
        for (int i=0; i < listeners.size(); i++) {
            listeners.get(i).changed();
        }
    }
}
