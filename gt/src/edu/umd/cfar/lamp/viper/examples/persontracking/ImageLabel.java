package edu.umd.cfar.lamp.viper.examples.persontracking;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

import javax.swing.*;

/*
 * Created on May 30, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

/**
 * @author ghanem
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ImageLabel extends JPanel{
Frame frame;
int StartFrame;
int EndFrame;

Hashtable videoClips;
String path;
String BGS_Path;
Color mycolor;

BufferedImage myImage,myBGSImage;

public ImageLabel(ImageIcon c,String p,int w,int h) {
      super();
      this.add(new JLabel(c));
      frame = JOptionPane.getFrameForComponent((Component) this);
      setSize(w,h);//(200, 150); 
//      addMouseListener(new MyMouseListener()); 
      path=p;
      mycolor=null;
}	

public void SetColor(Color c) {
    mycolor=c;
}

public int GetHeight(){
    return this.getHeight();

}

public int GetWidth(){
    return this.getWidth();

}


public void  ChangeBorder(boolean chosen){
    if(chosen) {
        setBorder(new javax.swing.border.LineBorder(mycolor, 4));
    }
    else {
        setBorder(new javax.swing.border.LineBorder(mycolor, 2));
    }
}

public String GetPath(){
return path;
}

public void Set_BGS_Path(String path) {
    BGS_Path=path;
}

public String Get_BGS_Path(){
    return BGS_Path;
}

public BufferedImage GetImage(){
    return myImage;
}

public void SetImage(BufferedImage im){
    myImage=im;
}

public BufferedImage GetBGSImage(){
    return myBGSImage;
}

public void SetBGSImage(BufferedImage im){
    myBGSImage=im;
}


public void paintComponent(Graphics g) {
    super.paintComponent(g);
}
		
/* private class MyMouseListener implements MouseListener{ 
    
        public void mouseClicked(MouseEvent e) {
            //chosen=true;
            if(mycolor!=null) ChangeBorder(true);
        }
        
        public void mouseEntered(MouseEvent e) {
        }
        
        public void mouseExited(MouseEvent e) {
        }
        
        public void mousePressed(MouseEvent e) {
        }
        
        public void mouseReleased(MouseEvent e) {
        }
        
 }
*/
}
