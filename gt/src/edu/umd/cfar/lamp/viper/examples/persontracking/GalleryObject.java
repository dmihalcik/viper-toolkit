package edu.umd.cfar.lamp.viper.examples.persontracking;

import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

/**
 *
 * @author  Guest
 */
public class GalleryObject{
    
    static int NColors=64;
    static int Distance=2;
        
    ImageLabel RepresentativeImage;
    Vector Instances;
    int Type;   
    String GalleryName;
    JList DisplayingPanel;      //panel to display instances
    private String ID;
    int InstanceNo;
    int [][][] Correlogram;
    String FramePath,BGSPath,BlobsPath;
    int MouseClick_X,MouseClick_Y;
    JButton mybutton;
    /** Creates a new instance of GalleryObject */
    public GalleryObject(ImageLabel image,JList panel,int type,String s) {
            //super();
            RepresentativeImage=image;
            DisplayingPanel=panel;
            Type=type;
            ID=s;
            RepresentativeImage.ChangeBorder(true);
            Instances=new Vector(10);
            Instances.add(RepresentativeImage);
//            for(int i=1;i<10;i++) 
//                Instances.add(new ImageLabel(null,null));
            InstanceNo=1;
            mybutton=null;
    
    }
    
    public String toString(){
        String S;
        S=ID+" ("+InstanceNo+")";
        return S;
    }
    
public JList GetDisplayingPanel(){
    return DisplayingPanel;
}
public void SetDisplayingPanel(JList p){
        DisplayingPanel=p;
        DisplayingPanel.removeAll();
        
/*        if(RepresentativeImage!=null) {
            RepresentativeImage.ChangeBorder(true);
            //DisplayingPanel.add(RepresentativeImage);   
            Instances.add(RepresentativeImage,InstanceNo);  
        }
  */    //  DisplayObject();
        
            
        
    } 
    
    public void AddInstance(ImageLabel mylabel){
        ImageLabel label=(ImageLabel)Instances.elementAt(InstanceNo-1);
        if(label!=mylabel) {
            Instances.add(mylabel);  InstanceNo++;
            mylabel.ChangeBorder(false);
//            DisplayObject();
        }
        /*DisplayingPanel.add(label);   
        DisplayingPanel.repaint();
        DisplayingPanel.validate();*/
    }
    
    public void SetButton(JButton b) {
    mybutton=b;
//    b.addMouseListener(new ButtonListener());
    }
    
    public JButton GetButton() {
    return mybutton;
    }
    public void DisplayObject(JPanel m){
        //ImageLabel label=mylabel;
        //Instances.addElement(label);  
//        DefaultListModel m=(DefaultListModel)DisplayingPanel.getModel();
        m.removeAll();
        m.add(mybutton);
        mybutton.setBounds(20,50,100,40);
        
        for(int i=0;i<InstanceNo;i++) {
            ImageLabel label=(ImageLabel)Instances.elementAt(i);

            
            m.add(label);   
            label.setSize(150,150);
            label.setBounds(i*150+150,0, 150,150);
          //  DisplayingPanel.repaint();
 //           DisplayingPanel.validate();
//            DisplayingPanel.repaint();
      
        }
            //DisplayingPanel.validate();
            //DisplayingPanel.repaint();
    }
    
    public void BuildCorrelogram() {

        
      //  String path = RepresentativeImage.GetPath();
      //  File f= new File(path);
       
        double levels=Math.pow((double)NColors,0.33333);
        float step = (float)256/(float)levels;
        
        Correlogram = new int[NColors][NColors][Distance];
        int[][] Sum=new int[NColors][Distance];
        int mid=(int)(-16777216*0.5);
        
        for(int i=0;i<NColors;i++) 
            for(int j=0;j<NColors;j++) 
                for(int k=0;k<Distance;k++){ 
                    Correlogram[i][j][k]=0;
                    Sum[i][k]=0;
            }
        String blackvalue="11111111000000000000000000000000";
        try{
            BufferedImage bi=RepresentativeImage.GetImage();//ImageIO.read(f);
         //BufferedImage bi=ImageIO.read(f);
  //       int dotpos=path.indexOf('.');
         //String f_BGS=RepresentativeImage.Get_BGS_Path();
         BufferedImage bi_BGS=RepresentativeImage.GetBGSImage();//ImageIO.read(new File(f_BGS));
         int count=0;
         
         for(int x=0;x<bi.getWidth();x++)
             for(int y=0;y<bi.getHeight();y++) {
             
                 int RGB=bi.getRGB(x,y);
//                 int RGB_BGS=bi_BGS.getRGB(x,y);

 //                String S=Integer.toBinaryString(RGB_BGS);
//                 int r=Integer.parseInt(S.substring(0,7),2);
//                 int g=Integer.parseInt(S.substring(8,15),2);
//                 int b=Integer.parseInt(S.substring(16,23),2);
                 
//                 String s=Integer.toBinaryString(rgb);
//                           if(RGB_BGS>mid) {
//if(r==127 && g==127 && b==127) {
                     if(bi_BGS==null || bi_BGS.getRGB(x,y)>mid){   
                     String S=Integer.toBinaryString(RGB);
                     int r=Integer.parseInt(S.substring(8,15),2);
                     int g=Integer.parseInt(S.substring(16,23),2);
                     int b=Integer.parseInt(S.substring(24,31),2);

                     int colorindex=(int)((r*levels*levels+g*levels+b)/step);
                     int colorindex2=0;
                 
                    for(int l=0;l<Distance;l++)
                        for(int d1=-Distance;d1<=Distance;d1++)
                            for(int d2=-Distance;d2<=Distance;d2++)
                                if((l==Math.abs(d1)) || (l==Math.abs(d2)))
                                    if((x+d1>=0)&&(x+d1<bi.getWidth())&&(y+d2>=0)&&(y+d2<bi.getHeight())) {
                                        RGB=bi.getRGB(x+d1,y+d2);
                                        S=Integer.toBinaryString(RGB);
                                        r=Integer.parseInt(S.substring(8,15),2);
                                        g=Integer.parseInt(S.substring(16,23),2);
                                        b=Integer.parseInt(S.substring(24,31),2);
                                        colorindex2=(int)((r*16+g*4+b)/step);
                                        Correlogram[colorindex][colorindex2][l]++;
                                        Sum[colorindex][l]++;
                                        count++;
                                }
             }
          }
            int v=0;
         }   catch(Exception e) {System.out.print(e.toString());}
        
    
        
        for(int i=0;i<NColors;i++) 
            for(int j=0;j<NColors;j++) 
                for(int k=0;k<Distance;k++){ 
                    if(Sum[i][k]==0) Correlogram[i][j][k]=0;
                    else
                        Correlogram[i][j][k]=(int)(100*Correlogram[i][j][k]/Sum[i][k]);
                }
        

        
     //   return Histo;

    }
    
    public BufferedImage GetImage() {
        
        return RepresentativeImage.GetImage();
        /*String path = RepresentativeImage.GetPath();
        File f= new File(path);
        BufferedImage bi=null;
        try{
             bi=ImageIO.read(f);
        }
        catch(Exception e) {}
        
        return bi;
        */
    }
    
    public void WriteCorrelogram() {
    
        FileWriter f;

        try {
            f=new FileWriter(RepresentativeImage.GetPath()+".txt");
        for(int d=0;d<Distance;d++){
           for(int i=0;i<NColors;i++) 
                for(int j=0;j<NColors;j++) 
                    if(Correlogram[i][j][d] !=0)  f.write(i+" "+j+" "+d+" "+Correlogram[i][j][d]+"\n ");
           //f.write("\n");
                    
                    //Sum[i][k]=0;
            }
        f.close();
        }catch (Exception e) {}
        

    }
    
    public int[][][] GetCorrelogram() {
        return Correlogram;
    
    }
    public int GetCorrelogram(int index1,int index2,int dist) {
        return Correlogram[index1][index2][dist];
    
    }
    
    public ImageLabel GetLabel() {
        return RepresentativeImage;
    }
    
//   public ImageIcon GetIcon() {
//        return RepresentativeImage;
//    }
       
    public void SetFramePath(String framepath){
        FramePath=framepath;
    }
    public void SetBGSPath(String BGSpath){
        BGSPath = BGSpath;
    }
    public void SetBlobsPaths(String Blobspath){
        BlobsPath = Blobspath;
    }
    public void SetMouseClick(int x,int y){
        MouseClick_X=x;
        MouseClick_Y=y;
    }
    
    /*public void CreateLabel(){
        BufferedReader infile;
        infile = null;
        boolean found=false;
        int xmin=0,ymin=0,xmax=0,ymax=0;
	try    {
		 infile = new BufferedReader(new FileReader(BlobsPath));
        
            
            while(infile.ready() && !found) {
               String InputString=infile.readLine();
               StringTokenizer st = new StringTokenizer(InputString); 
               while(st.hasMoreTokens()&& !found){
                    int blobno= Integer.parseInt(st.nextToken()); 
                    xmin= Integer.parseInt(st.nextToken()); 
                    ymin= Integer.parseInt(st.nextToken()); 
                    xmax= Integer.parseInt(st.nextToken()); 
                    ymax= Integer.parseInt(st.nextToken()); 
                    if((MouseClick_X>=xmin)&&(MouseClick_X<=xmax)&&(MouseClick_Y>=ymin)&&(MouseClick_Y<=ymax)) {
                        found=true;
                    }
               }
            }
                 if(found) {
                   File f= new File(FramePath);
                   BufferedImage bi=ImageIO.read(f);
                   BufferedImage sub=bi.getSubimage(xmin,ymin,(xmax-xmin+1),(ymax-ymin+1));
                   ImageIcon icon=new ImageIcon(sub);
                   String iconPath = FramePath+"__"+".jpg";
                   File fout=new File(iconPath);
                   ImageIO.write(sub,"jpg",fout);

                   RepresentativeImage = new ImageLabel(icon,iconPath);
                   Instances.remove(0);
                   Instances.add(0,RepresentativeImage);
                //   DisplayObject();
                   
                   f= new File(BGSPath);
                   bi=ImageIO.read(f);
                   sub=bi.getSubimage(xmin,ymin,(xmax-xmin+1),(ymax-ymin+1));
                   iconPath = FramePath+"___BGS"+".jpg";
                   fout=new File(iconPath);
                   ImageIO.write(sub,"jpg",fout);
              }
        }
        catch (Exception e){}
        
    }
    */
    

    
}
