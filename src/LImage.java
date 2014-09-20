 
import java.net.*;
import java.applet.*;
import java.awt.*;
import java.awt.image.*;

/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */

public class LImage extends Applet
{
 private Image im1,im2,buf1,buf2;
 private boolean im1_done=false, im2_done=false,waitdraw=false;
 private int im1_wi, im1_he, im2_wi, im2_he;
 private int act=1;
 private String target=null;
 public void init()
 { URL codebase=getCodeBase();
   System.out.println("Codebase:"+codebase);
   String im1s=getParameter("IMAGE1");
   String im2s=getParameter("IMAGE2");
   target=getParameter("TARGET");
   im1=getImage(codebase,im1s);
   im2=getImage(codebase,im2s);
   printSize();
 }
 
 public void paint(Graphics g)
 { //System.out.println(im1_done+","+im2_done+"   "+im1_wi+","+im1_he+"   "+im2_wi+","+im2_he);
   if (!im1_done)
   { buf1=createImage(1,1);
     Graphics bg1= buf1.getGraphics();
	 bg1.drawImage(im1,0,0,this);
	 bg1.dispose();
	} else
	{ if (act==1) g.drawImage(im1,0,0,this);
	}
	
	if (!im2_done)
   { buf2=createImage(1,1);
     Graphics bg2= buf2.getGraphics();
	 bg2.drawImage(im2,0,0,this);
	 bg2.dispose();
	} else
	{ if (act==2) g.drawImage(im2,0,0,this);
	}
 }
 
 private void printSize()
 { System.out.println("Image 1 width, height:"+im1.getWidth(this)+" , "+im1.getHeight(this));
   System.out.println("Image 2 width, height:"+im2.getWidth(this)+" , "+im2.getHeight(this));
 
   int[] pix=new int[im1.getWidth(this)*im1.getHeight(this)];
   PixelGrabber pim=new PixelGrabber(im1,0,0,im1.getWidth(this),im1.getHeight(this),pix,0,im1.getWidth(this));
 }
 
 public void update(Graphics g)
 { paint(g); }
 
 public boolean imageUpdate(Image img,int infoflags,int x,int y,int width,int height)
 { if ((infoflags & ImageObserver.ALLBITS) != 0)
   { // image is complete
     if (im1==img) 
	 { im1_wi=img.getWidth(null);
	   im1_he=img.getHeight(null);
	   im1_done=true;
	   waitdraw=false;
	   repaint();
	   return false;
	 }
	 else if (im2==img)
	 { im2_wi=img.getWidth(null);
	   im2_he=img.getHeight(null);
	   im2_done=true;
	   waitdraw=false;
	   repaint();
	   return false;
	 }
	}
	 return true; // want more info
 }
 
   public boolean mouseExit(Event evt, int x, int y)
  { if (!im1_done || !im2_done) return true;
    act=1; 
   //Graphics g=getGraphics();
   //g.drawImage(im1,0,0,this);
   //g.dispose();
    repaint();
    return true;
  
  }
  
     public boolean mouseEnter(Event evt, int x, int y)
  { if (!im1_done || !im2_done) return true;
    act=2; 
   //Graphics g=getGraphics();
   //g.drawImage(im2,0,0,this);
   //g.dispose();
   repaint();
   return true;
  }
   
   public boolean mouseDown(Event evt, int x, int y) 
   { hyperJump(target);
     return true;
   }
  
   public void hyperJump(String t)
   { URL pg=null;
     try{
	   pg=new URL(t);
	   } catch(MalformedURLException e)
	     { System.err.println("Bad URL address:"+t); }
	 getAppletContext().showDocument(pg);
	}
     
} 
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
