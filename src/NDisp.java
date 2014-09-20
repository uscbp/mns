 
import java.awt.*;
import java.math.*;
import java.lang.*;
/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 * <hl>
 * Borrowed from my early MNS nsl model tries. It can display arrays 
 * contents in a visual way
*/
public class NDisp extends Frame
{
 String mytitle;
 public double FIRING_MAX = 1.5;
 public double MEMBRANE_MAX = 3.5;
    public long[] def_color;
    public int def_col_count=16;

 public NDisp(Object m,String title)
 { 
     ownerR=null;
   owner=m;
   if (title==null) mytitle="Neuron Activity";
   else mytitle=title;
   setTitle(mytitle);

   def_color =new long[def_col_count];
   for (int i=0;i<def_col_count;i++) {
       def_color[i]= (230L<<16) + (190L<<8) + (190L); // default is gray
   }
   
   canvas= new Canvas();
   canvas.setBackground(Color.white);
   add("Center",canvas);
   Panel p=new Panel();
   p.add(new Button("Close"));
   p.add(new Button("Draw"));
   add("South",p);
 }

 public NDisp(Region R,Object m,String title)
 {
  this(m,title);
  System.out.println("Region got by NDISPL");
  ownerR=R; 
 }
  
 public NDisp() { this(null,null); }

 public void paint(Graphics g)
 {
  System.out.println("Paint!!!");
 }

 public void repaint()
 {  
  System.out.println("REPaint!!!");
}

 public boolean mouseDown(Event evt, int x, int y)
 {
   Point p=canvas.getLocation();
   x=x-p.x;  y=y-p.y;

  //System.out.println("Down: step 0");
  if (!Lastdrawn) return true;
  x-=Lastx0; y-=Lasty0;
  //System.out.println("Down: step 1");
  if (x<0 || y<0) return true;
  //System.out.println("Down: step 2");
  int col= x / (Lastwi+1);
  int row= y / (Lastwi+1);
  //System.out.println("hit row,col:"+row+","+col);
  if (col>=Lastncol) return true;
  //System.out.println("Down: step 3");
  if (row>=Lastnrow) return true;
  //System.out.println("Down: step 4");
  if (row*Lastncol+col>=Lastsize) return true;
  //System.out.println("Down: step 5");
  /*
  System.out.println("NDISPmousedown: rpobesel:"+MNS.probeselect);
  Probe pr=null;
  if (MNS.probeselect!=0)
  { 
   if (MNS.probeselect==1) 
   pr=new Probe(owner,row*Lastncol+col,mytitle,320,170,FIRING_MAX, 
      Probe.FIRING,MNS.timestep);
   else 
   pr=new Probe(owner,row*Lastncol+col,mytitle,320,170,MEMBRANE_MAX, 
      Probe.MEMBRANE,MNS.timestep);
 
   ownerR.insertProbe(pr);
   MNS.probeselect=0;
  }
  else MNS.cellNotify(owner,row*Lastncol+col);
  */
  //System.out.println("Down: step 6");
  return true;
 }

 public boolean handleEvent(Event evt)
 { if (evt.id == Event.WINDOW_DESTROY) System.exit(0);
   return super.handleEvent(evt);
 }

 public boolean action(Event evt, Object arg)
 {  if (arg.equals("Draw"))
    { forceDrawNext();
    //ownerR.drawActivity(ownerR.myDinfo.ncol);
      forceDrawNext(); 
    }
   if (arg.equals("Close"))
       { //ownerR.showing=false; 
     hide();
   }
   else return super.action(evt,arg);
   return true;
 }

 public Canvas getCanvas()
 { return canvas;}

 public Graphics getGraphics()
 { return g; }

public void clearCanvas()
{
 Graphics  g=canvas.getGraphics();
 Dimension d=canvas.size();
 g.clearRect(0,0,d.width,d.height);
 forceDrawNext();
}

/* IMPR: pull out the unnecc. geom. computation in an other method. Compute
   them when necc. i.e resize occurs
*/

private int[] old_he=null;
public void forceDrawNext()
{ old_he=null;}
public void drawArray(double[] a,double min,double max,long[] col,int size,int ncol)
{
 int MARGIN;
 double mag;
 int wi,he,x,y,column,x0,y0,nrow;
 int R,G,B;
 boolean first;
 long v;
 Color c;
 Graphics  g=canvas.getGraphics();
 Dimension d=canvas.size();
 
 //if (col==null) col=def_color;
 first=false; 

 if (old_he==null) 
 { old_he=new int[size]; 
   first=true;
 }

 MARGIN = d.width / 50;
 if ((d.height / 50) <MARGIN) MARGIN=d.height/50;

 double yy=1.0*size/ncol;
 nrow=(int)(yy+0.9999);
 wi=(d.width/2-MARGIN-ncol)/ncol;
 
 if (wi>((d.height/2-MARGIN-nrow)/nrow)) wi=(d.height/2-MARGIN-nrow)/nrow;
 //wi-=1;
 wi=wi*2+1;
 
 column=0;
 x0=(d.width-(wi+1)*ncol)/2;
 y0=(d.height-(wi+1)*nrow)/2;
 //x0=MARGIN; y0=MARGIN;
 x=x0; y=y0; 

 Lastncol=ncol;
 Lastnrow=nrow;
 Lastx0  =x0; 
 Lasty0  =y0; 
 Lastwi  =wi;
 Lastsize=size;
 Lastdrawn=true;

 for (int i=0;i<size;i++)
  {
   mag=((a[i])/(max-min))*wi;   
   he=(int)mag;
   
   //if (i>=def_col_count) v=col[i%def_col_count];
   //else
   if (col==null) c=Color.white;
   else {
       v=col[i];
       R=(int)(v>>16); v=v%(1<<16);
       G=(int)(v>>8) ; v=v%(1<<8);
       B=(int)v;
       //System.out.println(R+" "+G+" "+B);
       c=new Color(R,G,B);
   }
   //drawBar(g,x,y,wi,he,c);
   if (old_he[i]!=he || first) 
     drawSArea(g,x,y,wi,he,c);
   //else old_he[i]=he;
   //if (first) old_he[i]=he;
   old_he[i]=he;
   x+=wi+1; column++;
   if (column>=ncol) {column=0; y+=wi+1; x=x0;}
  } 
}

public void notused_drawSArea0(Graphics g,int x,int y,int size,int val,Color col)
 {

  if (val<0) { val=-val; //val=0;
    col=Color.yellow;
//  System.err.println("NDisp: underflow (-) value truncated to 0");
}
  if (val>size) { val=size;
  System.err.println("NDisp: overflow (>maxvalue) value truncated to maxvalue");
  }
  g.clearRect(x+1,y+1,size-2,size-2);
  g.setColor(col);
 
  int cx=(x+x+size-1)/2;
  int cy=(y+y+size-1)/2;
  int r =val/2;
  g.fillRect(cx-r,cy-r,2*r+1,2*r+1);
  g.drawRect(x,y,size-1,size-1);
  g.setColor(Color.red);
  g.drawLine(cx,cy,cx,cy);
 
 }

public void drawSArea(Graphics g,int x,int y,int size,int val,Color col)
{boolean neg=false;
  if (val<0) { val=-val; //val=0;
  neg=true;
  //System.err.println("NDisp: underflow (-) value truncated to 0");
  }
  if (val>size) { val=size;
  System.err.println("NDisp: overflow ("+size+">maxvalue) value truncated to maxvalue");
  }
  //g.clearRect(x+1,y+1,size-2,size-2);
  //g.setColor(col);
  g.setColor(col);
  g.fillRect(x,y,size,size);
  g.setColor(Color.red);
  g.drawRect(x,y,size,size);
  int cx=(x+x+size-1)/2;
  int cy=(y+y+size-1)/2;
  int r =val/2;
  //g.draw3DRect(cx-r,cy-r,2*r+1-1,2*r+1-1,true);
 
  //g.setColor(col);
  g.setColor(Color.black);
  if (neg) {
     g.setColor(Color.green); 
  } else g.setColor(Color.black);

  g.fillRect(x+2,y+size-val,size-3,val);
  //g.drawRect(x,y,size-1,size-1);
  //g.setColor(Color.red);
  //g.drawLine(cx,cy,cx,cy);

 }

public void drawBar(Graphics g,int x,int y,int wi,int he,Color col)
 {
  if (he<0) { y+=he-1; he=-he;}
  g.setColor(col);
  g.fillRect(x,y,wi,he);
  g.setColor(Color.black);
  g.drawRect(x,y,wi,he);
 }

  public static void main(String[] args)
  {  
    NDisp f=new NDisp(null,"Self Test");
    f.resize(400,400);
    f.show();
    int nrows=30;
    int ncols=30;

    double[] arr=new double[nrows*ncols];
    for (int i=0;i<nrows; i++)
	for (int j=0;j<ncols;j++) 
	    arr[i*ncols+j]=Math.cos(0.1*((i-nrows/2.0)*(i-nrows/2.0)+(j-ncols/2.0)*(j-ncols/2.0)))/Math.exp(0.01*((i-nrows/2.0)*(i-nrows/2.0)+(j-ncols/2.0)*(j-ncols/2.0)));
    f.drawArray(arr,0,1,null,arr.length,ncols);
  }

 private Canvas canvas;
 private Graphics g;
 int Lastncol;
 int Lastnrow;
 int Lastx0  ;
 int Lasty0  ;
 int Lastwi  ;
 int Lastsize;
 boolean Lastdrawn = false;
 Object owner;
 Region ownerR;
 

} /* End of NDisp */

/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
