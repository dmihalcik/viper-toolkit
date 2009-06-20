package edu.umd.cfar.lamp.viper.examples.persontracking;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

import edu.umd.cfar.lamp.viper.geometry.*;


public class Tracker {
	
    static int NColors=64;
    static int Distance=2;
    static int THRESHOLD=70;

    int FrameStep;
    int OverlapWeight; 
    int CorrelogramWeight;
    String path,bgs_path,blobs_path,output_path;
    ImageLabel BackwardModel,ForwardModel;
    GalleryObject ObjectToTrack;
    int FirstFrameNo,LastFrameNo;

	
    public Tracker(String Path,String BGS_path, String Blobs_path, String Output_path) {
        path=Path;
        bgs_path=BGS_path;
        blobs_path = Blobs_path;
        output_path = Output_path;
        ForwardModel=null;
        BackwardModel=null;
        ObjectToTrack=null;
        FirstFrameNo=0;LastFrameNo=0;
    }
    
	public BoundingBox GetBlob(int FrameNo,double X,double Y){
	    
	      String frameNo=String.valueOf(FrameNo);
          String frameno_str="";
          for(int i=0;i<5-frameNo.length();i++) frameno_str=frameno_str+"0";
          frameno_str=frameno_str+frameNo;
     
          String BlobsPath=blobs_path+frameno_str+".txt";
		
		
        BufferedReader infile;
	try    {
		 infile = new BufferedReader(new FileReader(BlobsPath));
	
         while(infile.ready()) {
               String InputString=infile.readLine();
               StringTokenizer st = new StringTokenizer(InputString); 
               while(st.hasMoreTokens()){
                    int id = Integer.parseInt(st.nextToken()); 
                    int xmin= Integer.parseInt(st.nextToken()); 
                    int ymin= Integer.parseInt(st.nextToken()); 
                    int xmax= Integer.parseInt(st.nextToken()); 
                    int ymax= Integer.parseInt(st.nextToken());
                    
                    BoundingBox R2=new BoundingBox(xmin,ymin,xmax-xmin+1, ymax-ymin+1);

                    //if(R2.contains((int)X,(int)Y)) return R2;
                    if(X>=xmin & X<=xmax & Y>=ymin & Y<=ymax) return R2;
               }
         }
        }
        catch (Exception e){
        	e.printStackTrace();
        }
        
        return null;
    }

	public Vector GetTrack(int frameno, int step, BoundingBox startposition){
	        int FrameStep=step;
	        String OutputFileName=output_path+frameno+"_"+(int)startposition.getX()+"_"+(int)startposition.getY()+"_"+FrameStep+".txt";
	        int StartFrame=frameno;
	        //int EndFrame=StartFrame+200;
	        FileWriter out;       
	        //open outputfile
	        Rectangle r_startposition= new Rectangle(startposition.getX(),startposition.getY(),startposition.getWidth(),startposition.getHeight());
	         Vector Path=new Vector(5);
	         PathPoint p=new PathPoint(frameno,r_startposition);
	         Path.add(p);
	         
	         int currentstate=1; //single object
	         Vector OtherModels=new Vector(3);
	         GalleryObject ObjectToTrackBeforeMerging=null;        
	         ObjectToTrack=CreateObjectFromBlob(frameno,r_startposition);
	        try{
	           out=new FileWriter(OutputFileName);    
	           int FrameNo=StartFrame+FrameStep;
	        
	           Rectangle R=new Rectangle(startposition.getX(),startposition.getY(),startposition.getWidth(),startposition.getHeight());
	           boolean EndFlag=false;
//	           double OriginalBlobSize=R.getWidth()*R.getHeight();
	           
	           while(EndFlag==false){//(FrameNo<EndFrame) {
	            
	             String frameNo=String.valueOf(FrameNo);
	             String frameno_str="";
	             for(int i=0;i<5-frameNo.length();i++) frameno_str=frameno_str+"0";
	             frameno_str=frameno_str+frameNo;
	        
	             String framepath=path+frameno_str+".jpg";
	             String BGSPath=bgs_path+frameno_str+".jpg";
	             String BlobsPath=blobs_path+frameno_str+".txt";
	        
	             BufferedReader infile;
	             try    {
	                	 infile = new BufferedReader(new FileReader(BlobsPath));
	             }catch(Exception E) {EndFlag=true;}
	         //while(infile.ready()) {
	        
	           if(EndFlag==false) { 
	                Vector Blobs=GetOverlappingBlobs(BlobsPath,R);
	            
	                if(Blobs.size()==0) {
	                    out.write(FrameNo+" Disappeared\n");
	                    EndFlag=true;
	                }
	                else if(Blobs.size()==1) {
	                    
	                    Rectangle newR=(Rectangle)Blobs.elementAt(0);
	                    frameNo=String.valueOf(FrameNo-FrameStep);
	                    frameno_str="";
	                    for(int i=0;i<5-frameNo.length();i++) frameno_str=frameno_str+"0";
	                    frameno_str=frameno_str+frameNo;
	        
	                    String R_blobspath=blobs_path+frameno_str+".txt";

	                    
	                    Vector OtherOverlappingBlobs=GetOverlappingBlobs(R_blobspath,newR);
	                    int mergeflag=0;
	                    if(OtherOverlappingBlobs.size()==1){    //Stop tracking if blob size < 30% of original blob size
	                    	Rectangle r=(Rectangle)Blobs.elementAt(0);
	                        int overlap=GetOverlapRatio(r,R);
	 //                       if(overlap <30) {
//	                            EndFlag=true;
//	                            out.write(FrameNo+" Disappeared\n");
	 //                           }
	                        
	                        //no change in state
	                    }
	                    else if(OtherOverlappingBlobs.size()>1){    //merge
	                        mergeflag=1;
	                        currentstate=2;
	                        //save other models
	                        for(int k=0;k<OtherOverlappingBlobs.size();k++) {
	                        	Rectangle r=(Rectangle)OtherOverlappingBlobs.elementAt(k);
	                            if (R.equals(r) || R.contains(r)) {
	                                ObjectToTrackBeforeMerging=CreateObjectFromBlob(FrameNo,R);
	                                    //update model for ObjectToTrack
	                                
	                            }
	                            else{
	                                //save other blobs information
	                                GalleryObject obj=CreateObjectFromBlob(FrameNo,r);
	                               OtherModels.add(obj);
	                            }
	                        }
	                    }
	                    int OverlapRatio=GetOverlapRatio(newR,R);
	                    int Xmax=(int)newR.getX()+(int)newR.getWidth()-1;
	                    float Ymax=(int)newR.getY()+(int)newR.getHeight()-1;
	                   // if(OverlapRatio <30) {
	                   //        EndFlag=true;
	                  //         out.write(FrameNo+" Disappeared\n");
	                 //   }
	                 //   else
	                    if(!EndFlag){ 
	                 //**   	out.write(FrameNo+" "+(int)newR.getX()+" "+(int)newR.getY()+" "+(int)Xmax+" "+(int)Ymax+" "+OverlapRatio+" "+mergeflag+"\n");
	              //      	model.addEvidence(person,FrameNo,new BoundingBox(newR.getX(),newR.getY(),newR.getWidth(),newR.getHeight()));
	                    	if(OverlapRatio<20) {
		                    	   EndFlag=true;
		                    	   if(FrameStep>0) LastFrameNo=FrameNo; else FirstFrameNo=FrameNo;
		                       }
	                    	
	                    }
	                    PathPoint point=new PathPoint(FrameNo,newR);
	                    Path.add(point);
	                    R=newR;
	                 //   if(OverlapRatio<70) EndFlag=true;
	                }

	                else{       //Blobs.size>1   split  //get models
	                
	                   
	                   if(currentstate==1) {        //one object split into two
	                       double overlap=0;
	                       boolean objectsplit=true;
	                       int i=0;
	                       while(objectsplit&& i<Blobs.size()){
	                            Rectangle r=(Rectangle)Blobs.elementAt(i);
	                            Rectangle in=r.intersection(R);
	                            if(in!=null) overlap=100*(in.getWidth()*in.getHeight())/(r.getWidth()*r.getHeight()); 
	                            if(overlap<70)  {objectsplit=false;}
	                            i++;
	                       }
	                       Rectangle newR=null;
	                       if(objectsplit) {    //merge blobs into one corresponding the one object
	                           newR=MergeBlobs(Blobs);
	                       }
	                       else {
	                           newR=GetMaxOverlap(Blobs,R);
	                       }
	                       int OverlapRatio=GetOverlapRatio(newR,R);
	                       R=newR;
	                       PathPoint point=new PathPoint(FrameNo,newR);
	                       Path.add(point);
	                       R=newR;
	                       int Xmax=(int)newR.getX()+(int)newR.getWidth()-1;
	                       float Ymax=(int)newR.getY()+(int)newR.getHeight()-1;
//	                     **    out.write(FrameNo+" "+(int)newR.getX()+" "+(int)newR.getY()+" "+(int)Xmax+" "+(int)Ymax+" "+(int)OverlapRatio+" "+" split\n");
	                //    	model.addEvidence(person,FrameNo,new BoundingBox(newR.getX(),newR.getY(),newR.getWidth(),newR.getHeight()));
	                    	if(OverlapRatio<20) {
		                    	   EndFlag=true;
		                    	   if(FrameStep>0) LastFrameNo=FrameNo; else FirstFrameNo=FrameNo;
		                       }
	                   }
	                   
	                   else if(currentstate==2) {       //split
	                       int objloc=GetNearestMatch(FrameNo,Blobs,ObjectToTrackBeforeMerging);
	                       if(objloc>-1) {
	                       Rectangle newR= (Rectangle)Blobs.elementAt(objloc);
	                       PathPoint point=new PathPoint(FrameNo,newR);
	                       Path.add(point);
	                       int OverlapRatio=GetOverlapRatio(newR,R);
	                       R=newR;
	                       int Xmax=(int)newR.getX()+(int)newR.getWidth()-1;
	                       float Ymax=(int)newR.getY()+(int)newR.getHeight()-1;
//	                     **  out.write(FrameNo+" "+(int)newR.getX()+" "+(int)newR.getY()+" "+(int)Xmax+" "+(int)Ymax+" "+(int)OverlapRatio+" "+" split\n");
	                  //     model.addEvidence(person,FrameNo,new BoundingBox(newR.getX(),newR.getY(),newR.getWidth(),newR.getHeight()));
	                       if(OverlapRatio<20) {
	                    	   EndFlag=true;
	                    	   if(FrameStep>0) LastFrameNo=FrameNo; else FirstFrameNo=FrameNo;
	                       }
	                       }
	                       currentstate=1;
	                   }
	                   }
	                    
//	                    out.write(FrameNo+" split\n");
//	                    OtherModels.removeAllElements();
	                //}
	           }
	            FrameNo=FrameNo+FrameStep;
	        }
	        
	        out.close();
	        if(!EndFlag) {
         	   EndFlag=true;
         	   if(FrameStep>0) LastFrameNo=FrameNo; else FirstFrameNo=FrameNo;
            }
	    }
	     catch (Exception e){}
	        
	        return Path;//OutputFileName;
	    }


	 public ImageLabel GetBackwardModel() {
	        return BackwardModel;
	    }
	    
	    public ImageLabel GetForwardModel() {
	        return ForwardModel;
	    }
	    
	    private int GetSimilarityMeasure(GalleryObject NewObj,GalleryObject obj){
	        int[][][] Correl1=NewObj.GetCorrelogram();
	        int[][][] Correl2=obj.GetCorrelogram();
	        
	        int D=0;
	        int N=0;
	       for(int i=0;i<NColors;i++) 
	            for(int j=0;j<NColors;j++) 
	                for(int k=0;k<Distance;k++){ 
	                    D=D+Math.abs(Correl1[i][j][k]-Correl2[i][j][k]);
	                    N=N+Correl1[i][j][k]+Correl2[i][j][k];
	                }
	        int similarity=(int)(100*(1.0-((float)D/N)));
	        
	        return similarity;
	    }

	    private int GetNearestMatch(int FrameNo,Vector blobs,GalleryObject NewObj) {
	            
	        int maxsimilarity=0;
	        int bestmatchloc=-1;
	        GalleryObject bestmatch=null;
	        for(int i=0;i<blobs.size();i++) {
	            Rectangle r=(Rectangle)blobs.elementAt(i);
	            GalleryObject obj=CreateObjectFromBlob(FrameNo,r);
	            int similaritymeasure=GetSimilarityMeasure(NewObj,obj);
	            if(similaritymeasure>maxsimilarity) {
	                maxsimilarity=similaritymeasure;
	                bestmatch=obj;
	                bestmatchloc=i;
	            }
	        }
	        return bestmatchloc;
	    }
	    
	    public GalleryObject CreateObjectFromBlob(int frameNum,Rectangle R) {
	              
	        
	              int xmin=(int)R.getX(); int ymin=(int)R.getY();
	              int xmax=(int)R.getX()+(int)R.getWidth()-1;
	              int ymax=(int)R.getY()+(int)R.getHeight()-1;
	              ImageLabel l = CreateModel_BB(path,bgs_path,frameNum,xmin,ymin,xmax,ymax);
	              GalleryObject obj=new GalleryObject(l,null,1,"tracker");
	              obj.BuildCorrelogram();
	              return obj;

	    
	    }
	    
	    private Rectangle MergeBlobs(Vector blobs){
	        
	        Rectangle Union=(Rectangle)blobs.elementAt(0);
	                
	        for(int i=1;i<blobs.size();i++) {
	            Rectangle r=(Rectangle)blobs.elementAt(i);
	            Union=Union.union(r); 
	        }
	        return Union;
	    }
	    
	    private Rectangle GetMaxOverlap(Vector blobs,Rectangle R){
	            
	        double max=0;
	        Rectangle maxRec=null;
	        for(int i=0;i<blobs.size();i++) {
	            Rectangle r=(Rectangle)blobs.elementAt(i);
	            Rectangle in=R.intersection(r);
	            if(in!=null) {
	                double overlap=in.getWidth()*in.getHeight();
	                if(overlap>max) {
	                    max=overlap;
	                    maxRec=r;
	                }
	            }
	        }
	        return maxRec;
	    }
	    public BufferedImage ShowTrack(JLabel originalframe,BufferedImage segmented,String TrackFile,int offset,Color color){
	        BufferedReader TrackingFile;
	        int frameNumber=0,xmin=0,ymin=0,xmax=0,ymax=0;
	        int last_frameNumber=0,last_xmin=0,last_ymin=0,last_xmax=0,last_ymax=0;

//	        BackwardModel=null;
//	        ForwardModel=null;
	        try{
	            TrackingFile = new BufferedReader(new FileReader(TrackFile));
	            boolean Found=false;
	            String S=TrackingFile.readLine();
	            do {//while(TrackingFile.ready()&& !Found) {
//	                String S=TrackingFile.readLine();
	                StringTokenizer st = new StringTokenizer(S); 
	                if(st.countTokens()==2) {
//	                        segmented=updatePicture(segmented,path,bgs_path,0,0,0,0,0,0);
//	                    segmented=updatePicture(segmented,path,bgs_path,last_frameNumber,last_xmin,last_ymin,last_xmax,last_ymax,offset); //display the next picture
	                    }
	                else {//if(st.countTokens()==6){
	                   last_frameNumber=frameNumber;
	                   last_xmin=xmin;
	                   last_ymin=ymin;
	                   last_xmax=xmax;
	                   last_ymax=ymax; 
	                    frameNumber = Integer.parseInt(st.nextToken()); 
	                    xmin= Integer.parseInt(st.nextToken()); 
	                    ymin= Integer.parseInt(st.nextToken()); 
	                    xmax= Integer.parseInt(st.nextToken()); 
	                    ymax= Integer.parseInt(st.nextToken());
	                    int confidence=Integer.parseInt(st.nextToken());
	                    if(confidence<20) Found=true;
	                    //add dot to the path
	                    if(last_frameNumber==0) {
	                        last_frameNumber=frameNumber;
	                        last_xmin=xmin;
	                        last_ymin=ymin;
	                        last_xmax=xmax;
	                        last_ymax=ymax; 
	                    }
	                    if(Found&&last_frameNumber>0)  {
	  //                      segmented=updatePicture(segmented,path,bgs_path,last_frameNumber,last_xmin,last_ymin,last_xmax,last_ymax,offset); //display the next picture
	                        ImageLabel Model = CreateModel(path,bgs_path,last_frameNumber,last_xmin,last_ymin,last_xmax,last_ymax);
	                        if(Model!=null) Model.SetColor(color);
	                        if(color==Color.GREEN){
	                            ForwardModel=Model;
	                            LastFrameNo=last_frameNumber;
	                        }
	                        else if(color==Color.RED) {
	                            BackwardModel=Model;
	                            FirstFrameNo=last_frameNumber;
	                        }
	                        
	   //                     Graphics g=originalframe.getGraphics();
	   //                     g.setColor(color);
	                        //g.fillRect((last_xmin+last_xmax)/2,(last_ymin+last_ymax)/2, 10, 10);
	  //                      g.drawRect(last_xmin,last_ymin,(last_xmax-last_xmin+1),(last_ymax-last_ymin+1));
	                    }
	           }
	                if(TrackingFile.ready())S=TrackingFile.readLine();
	        }  while(TrackingFile.ready()&&!Found);
	            
	            if(!Found &&last_frameNumber>0)  {
	  //                      segmented=updatePicture(segmented,path,bgs_path,last_frameNumber,last_xmin,last_ymin,last_xmax,last_ymax,offset); //display the next picture
	                        ImageLabel Model = CreateModel(path,bgs_path,last_frameNumber,last_xmin,last_ymin,last_xmax,last_ymax);
	                        if(Model!=null) Model.SetColor(color);
	                        if(color==Color.GREEN){
	                            ForwardModel=Model;
	                            LastFrameNo=last_frameNumber;
	                        }
	                        else if(color==Color.RED) {
	                            BackwardModel=Model;
	                            FirstFrameNo=last_frameNumber;
	                        }
	                        
	  //                      Graphics g=originalframe.getGraphics();
	  //                      g.setColor(color);
	                        //g.fillRect((last_xmin+last_xmax)/2,(last_ymin+last_ymax)/2, 10, 10);
	 //                       g.drawRect(last_xmin,last_ymin,(last_xmax-last_xmin+1),(last_ymax-last_ymin+1));
	                    }
	                
	                
	            
	            
	        }
	        catch (Exception e){}

	    
	    return segmented;
	    }
	    
	    private Vector GetOverlappingBlobs(String BlobsPath,Rectangle R){
	        
	        Vector objs=new Vector(5);
	        
	        
	        BufferedReader infile;
		try    {
			 infile = new BufferedReader(new FileReader(BlobsPath));
		
	         while(infile.ready()) {
	               String InputString=infile.readLine();
	               StringTokenizer st = new StringTokenizer(InputString); 
	               while(st.hasMoreTokens()){
	                    int id = Integer.parseInt(st.nextToken()); 
	                    int xmin= Integer.parseInt(st.nextToken()); 
	                    int ymin= Integer.parseInt(st.nextToken()); 
	                    int xmax= Integer.parseInt(st.nextToken()); 
	                    int ymax= Integer.parseInt(st.nextToken());
	                    
	                    Rectangle R2=new Rectangle(xmin,ymin,xmax-xmin+1, ymax-ymin+1);

	                    if(R.intersects(R2)) objs.add(R2);
	               }
	         }
	        }
	        catch (Exception e){}
	        
	        return objs;
	    }
	    private int GetOverlapRatio(Rectangle newR,Rectangle R) {
	        int overlap=0;
	        
	        Rectangle inter=(Rectangle) R.intersection(newR);
	        
	        if (inter!=null) overlap=(int)(100*inter.getWidth()*inter.getHeight()/(R.getWidth()*R.getHeight()));
	        
	        return overlap;
	    
	    }
	    public ImageLabel CreateModel(String path,String bgs_path,int frameNum,int xmin,int ymin,int xmax,int ymax){
            String frameno=String.valueOf(frameNum);
            String frameno_str="";
            for(int i=0;i<5-frameno.length();i++) frameno_str=frameno_str+"0";
            frameno_str=frameno_str+frameno;
        
            String Framepath=path+frameno_str+".jpg";
            String BGSPath=bgs_path+frameno_str+".jpg";
            ImageLabel mylabel=null;
            int mid=(int)(-16777216*0.5);
            try{
                
                   File f= new File(Framepath);
                   BufferedImage bi=ImageIO.read(f);
                   
                   BufferedImage sub=bi.getSubimage(xmin,ymin,(xmax-xmin+1),(ymax-ymin+1));

                   bi=ImageIO.read(new File(BGSPath));
                   BufferedImage sub_BGS=bi.getSubimage(xmin,ymin,(xmax-xmin+1),(ymax-ymin+1));

                   
                   BufferedImage segmented=new BufferedImage((xmax-xmin+1),(ymax-ymin+1),BufferedImage.TYPE_INT_BGR);
                   for(int i=0;i<(xmax-xmin+1);i++)
                       for(int j=0;j<(ymax-ymin+1);j++)
                           segmented.setRGB(i,j,-1);

                   int index=0;
//                   int offset=150-xmin;
                   for(int i=0;i<(xmax-xmin+1);i++)
                       for(int j=0;j<(ymax-ymin+1);j++){
                            int rgb=sub_BGS.getRGB(i,j);
//                            System.out.println(rgb);
                           if(rgb>mid)  segmented.setRGB(i,j,sub.getRGB(i,j));
                       }
                   
                   ImageIcon icon=new ImageIcon(segmented);
                   
                   
                   mylabel=new ImageLabel(icon,Framepath,140,240);
                   mylabel.SetImage(segmented);
                   bi=ImageIO.read(new File(BGSPath));
                   mylabel.SetBGSImage(sub_BGS);
            }
           catch(Exception E){}
            
            return mylabel;
    }
    
    public ImageLabel CreateModel_BB(String path,String bgs_path,int frameNum,int xmin,int ymin,int xmax,int ymax){
            String frameno=String.valueOf(frameNum);
            String frameno_str="";
            for(int i=0;i<5-frameno.length();i++) frameno_str=frameno_str+"0";
            frameno_str=frameno_str+frameno;
        
            String Framepath=path+frameno_str+".jpg";
            String BGSpath=bgs_path+frameno_str+".jpg";
            ImageLabel mylabel=null;
            
            try{
                
                   File f= new File(Framepath);
                   BufferedImage bi=ImageIO.read(f);
                   
                   BufferedImage segmented=bi.getSubimage(xmin,ymin,(xmax-xmin+1),(ymax-ymin+1));
                   ImageIcon icon=new ImageIcon(segmented);
                   
                   
                   mylabel=new ImageLabel(icon,Framepath,140,240);
                   mylabel.SetImage(segmented);

                    f= new File(BGSpath);
                    bi=ImageIO.read(f);
                    segmented=bi.getSubimage(xmin,ymin,(xmax-xmin+1),(ymax-ymin+1));
                    mylabel.SetBGSImage(segmented);
            }
           catch(Exception E){}
            
            return mylabel;
    }

    public int GetFirstFrameNo(Vector Path){ 
    	PathPoint p=(PathPoint)Path.elementAt(Path.size()-1);
		int frame=p.GetFrameno()+1;

    	return frame;
    	}
    public int GetLastFrameNo(Vector Path){ 
    	PathPoint p=(PathPoint)Path.elementAt(Path.size()-1);
		int frame=p.GetFrameno()-1;

    	return frame;
}
    public void SetObjectToTrack(GalleryObject obj){
        ObjectToTrack=obj;
    
    }
    
    public void AddEvidences(Vector Path,PersonGalleryModel model,GalleryEntity person){
    
//        FileWriter out;       
//        try{
//        out=new FileWriter(OutputFileName);    

    	
    	for(int i=0;i<Path.size()-1;i++){
    		PathPoint p1=(PathPoint)Path.elementAt(i);
    		PathPoint p2=(PathPoint)Path.elementAt(i+1);
    		int frame1=p1.GetFrameno();
    		int frame2=p2.GetFrameno();
    		int Step=frame2-frame1;
    		Rectangle rect1=p1.GetRectangle();
    		Rectangle rect2=p2.GetRectangle();
    		double xinc=(rect2.getX()-rect1.getX())/Step;
    		double yinc=(rect2.getY()-rect1.getY())/Step;
    		double winc=(rect2.getWidth()-rect1.getWidth())/Step;
    		double hinc=(rect2.getHeight()-rect1.getHeight())/Step;

    		for(int j=frame1;j<frame2;j++) 
    			model.addEvidence(person,j,new BoundingBox(rect1.getX()+xinc,rect1.getY()+yinc,rect1.getWidth()+winc,rect1.getHeight()+hinc));
    	}

    	
    }
    public void MergeTracks(Vector BackwardPath,int currentFrame,BoundingBox box,Vector ForwardPath,GalleryEntity person){
    	FileWriter out;   
    	String name=String.valueOf(person.getId());//.getDescriptor().getDescName();
    	
    	String OutputFileName=name+"_track.txt";
    	try{
    		out=new FileWriter(OutputFileName);

    		for(int i=BackwardPath.size()-1;i>0;i--){
        		PathPoint p1=(PathPoint)BackwardPath.elementAt(i);
        		PathPoint p2=(PathPoint)BackwardPath.elementAt(i-1);
        		int frame1=p1.GetFrameno();
        		int frame2=p2.GetFrameno();
        		int Step=frame2-frame1;
        		Rectangle rect1=p1.GetRectangle();
        		Rectangle rect2=p2.GetRectangle();
        		double xinc=(rect2.getX()-rect1.getX())/Step;
        		double yinc=(rect2.getY()-rect1.getY())/Step;
        		double winc=(rect2.getWidth()-rect1.getWidth())/Step;
        		double hinc=(rect2.getHeight()-rect1.getHeight())/Step;

        		double x=rect1.getX();
        		double y=rect1.getY();
        		double w=rect1.getWidth();
        		double h=rect1.getHeight();
        		for(int j=frame1;j<frame2;j++) {
        			out.write(j+" "+(int)x+" "+(int)y+" "+(int)w+" "+(int)h+"\n");
        			x=x+xinc;
        			y=y+yinc;
        			w=w+winc;
        			h=h+hinc;
        		}
        	}

    		//out.write(currentFrame+" "+box.getX()+" "+box.getY()+" "+box.getWidth()+" "+box.getHeight()+"\n");

    		for(int i=0;i<ForwardPath.size()-1;i++){
        		PathPoint p1=(PathPoint)ForwardPath.elementAt(i);
        		PathPoint p2=(PathPoint)ForwardPath.elementAt(i+1);
        		int frame1=p1.GetFrameno();
        		int frame2=p2.GetFrameno();
        		int Step=frame2-frame1;
        		Rectangle rect1=p1.GetRectangle();
        		Rectangle rect2=p2.GetRectangle();
        		double xinc=(rect2.getX()-rect1.getX())/Step;
        		double yinc=(rect2.getY()-rect1.getY())/Step;
        		double winc=(rect2.getWidth()-rect1.getWidth())/Step;
        		double hinc=(rect2.getHeight()-rect1.getHeight())/Step;

        		double x=rect1.getX();
        		double y=rect1.getY();
        		double w=rect1.getWidth();
        		double h=rect1.getHeight();
        		for(int j=frame1;j<frame2;j++) {
        			out.write(j+" "+(int)x+" "+(int)y+" "+(int)w+" "+(int)h+"\n");
        			x=x+xinc;
        			y=y+yinc;
        			w=w+winc;
        			h=h+hinc;
        		}
        	}

    		
    		out.close();		
    }catch (Exception e){}
    
    }
}
