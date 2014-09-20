 
import java.io.*;
import java.net.*;
import java.applet.*;
import java.util.*;
import java.awt.*;
import java.lang.*;
import java.util.Vector;
import java.util.Enumeration;
/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */

class HVCanvas extends Canvas{
    static int bgColor_R=Resource.getInt("bgColor_R");
    static int bgColor_G=Resource.getInt("bgColor_G");
    static int bgColor_B=Resource.getInt("bgColor_B");
    static int fgColor_R=Resource.getInt("fgColor_R");
    static int fgColor_G=Resource.getInt("fgColor_G");
    static int fgColor_B=Resource.getInt("fgColor_B");
    static Color backColor=new Color(bgColor_R, bgColor_G, bgColor_B);
    static Color foreColor=new Color(fgColor_R, fgColor_G, fgColor_B);
 Image buf=null;
 int   bufwi=0, bufhe=0;
 Graphics bufg;
 public Eye    eye;
 public int wi,he,midx,midy;
 int showmode;

 public Feature showfeat=null; // set by match to draw image features
 public HVCanvas(Eye eye)
 { 
   this.eye  =eye;
 }

 public HVCanvas()
 { 
   this.eye  =Mars.eye;
 }


public void refreshDisplay()
 {
   Mars.project();
   repaint();
 }

public void setShowmode(int i)
 { showmode=i; }

public void clear()
{
 Graphics g=getGraphics();
 g.clearRect(0,0,wi,he);
}

public void repaint()
{ 
 //System.out.println("repaint called.");
 paint(getGraphics());

}

public Graphics getGfx()
 { return getGraphics(); }

public void paint(Graphics g)
 {
   Dimension d=getSize();
   he=d.height;
   wi=d.width;
   midx=wi/2;
   midy=he/2;
   Mars.midx=midx;
   Mars.midy=midy;
   if (HV.doubleBuffering)
   {
   	if (wi!=bufwi || he!=bufhe)
   	{
    	if (bufg!=null) bufg.dispose();
   	 buf=null;
    	bufg=null;
   	}
   	if (buf==null) 
   	{ if (wi<=0 || he <=0) return; 
     	bufwi=wi; bufhe=he;
     	System.out.println("off screen:"+wi+" x "+he);
     	buf=createImage(wi,he);
     	bufg=buf.getGraphics();
   	}
    }
    else  //no double buffering
    { bufwi=wi; bufhe=he; bufg=g;}
   bufg.setColor(backColor);
	bufg.fillRect(0,0,bufwi,bufhe);
   //bufg.setColor(Color.black);
   //bufg.drawLine(0,he-midy,wi,he-midy);
   //bufg.drawLine(midx,0, midx,he);
   //bufg.setColor(Color.blue);
   Mars.drawCube(bufg);
   if (showmode==HV.SHOWWIRE) Mars.drawWire(bufg);
   else if (showmode==HV.SHOWSKEL) Mars.drawSkel(bufg);
   else
   if (showmode==HV.SHOWSOLID || showmode==HV.SHOWLSHADE ||
       showmode==HV.SHOWSHADE || showmode==HV.SHOWHIDDEN) 
           Mars.drawSolid(bufg,showmode);
   else {System.err.println("Unknown show mode:"+showmode);}
   if (showfeat!=null && HV.showfeature) showfeat.drawImex(midx,midy,bufg);
   if (showmode!=HV.SHOWSKEL && HV.showfeature)
     Mars.drawSkel(bufg);


   Mars.drawStars(bufg);
   Hand h=(Hand)Mars.getObject("HAND");
   if (showfeat!=null && HV.showfeature) h.drawTriangle(midx,midy,bufg); 
   if (HV.recbarReq==1) {
       System.out.println("REquest gott!!");
       int b1=2*(1+(int) (Elib.logscale(HV.precBar,1,0.001)*50));
       int b2=2*(1+(int) (Elib.logscale(HV.sideBar,1,0.001)*50));        
       int b3=2*(1+(int)  (Elib.logscale(HV.powBar,1,0.001)*50));
       bufg.setColor(Color.red);
       bufg.fillRect(10,bufhe-b1,15,b1);
       bufg.setColor(Color.green);
       bufg.fillRect(26,bufhe-b2,15,b2);
       bufg.setColor(Color.yellow);
       bufg.fillRect(42,bufhe-b3,15,b3);
       //HV.recbarReq=0; //reset the request
   }
   if (HV.doubleBuffering) {
       g.drawImage(buf,0,0,null); 
       if (HV.recordCanvas) {
	   HV.self.createGif(); 
       }
   }
   if (HV.recordLW) {
       HV.self.rHand.dumpLWTargets(HV.LWrecordSession);
   }
   
//   ((Hand)Mars.obj[0]).infoPalm();
 }

public Image lastFrame() {
    if (HV.doubleBuffering) return buf;
    System.out.println("Can grab the image only in double buffer mode!!");
    return null;
}
}


/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
