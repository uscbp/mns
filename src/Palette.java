 
import java.awt.*;

/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 *
 * Simple palette implementation. Let's one to access colors with integers or
 * names. First 10 colors are reserved for standart java colors as black,
 * white,red,gree,blue,cyan,magenta,yellow,gray,lightGray.
*/
class Palette
{
 public int maxN=0,N=0;
 public Color[] C;
 public String[] names=null;
 
/** Creates a palette of 10+size colors.*/
 public Palette (int size) 
 {
  maxN=size+13;
  C=new Color[maxN];
  names=null; 

  addColor(Color.black,"black"); 
  addColor(Color.white,"white");
  addColor(Color.red,"red");
  addColor(Color.green,"green");
  addColor(Color.blue,"blue");
  addColor(Color.cyan,"cyan");
  addColor(Color.magenta,"magenta");
  addColor(Color.yellow,"yellow");
  addColor(Color.gray,"gray");
  addColor(Color.lightGray,"lightGray");
  addColor(new Color(255,128,0),"orange");
  addColor(new Color(130,100,200),"violet");
  addColor(new Color(130,200,130),"gr-green");
 }

/** adds the color c and returns its index.*/
 public int addColor(Color c)
 {
  C[N++]=c;
  return N-1;
 }

/** adds the color r,g,b and returns its index.*/
 public int addColor(int r,int g,int b)
 {
  C[N++]=new Color(r,g,b);
  return N-1;
 }

/** Adds the color (r,g,b) with dimensionName dimensionName and returns its index.*/
 public int addColor(int r,  int g, int b ,String name)
 {
  C[N++]=new Color(r,g,b);
  if (names==null) names=new String[maxN];
  names[N-1]=name;
  return N-1;
 }



/** Adds the color c with dimensionName dimensionName and returns its index.*/
 public int addColor(Color c,String name)
 {
  C[N++]=c;
  if (names==null) names=new String[maxN];
  names[N-1]=name;
  return N-1;
 }

/** Get color by dimensionName.*/
 public Color getColor(String name)
 {
  for (int i=0;i<N;i++)
   if (names[i]!=null)
    if (names[i].equals(name)) return C[i];
  return null;
 }

/** Get color by index */
 public Color getColor(int k)
 {
  if (k>=N) return null;
  return C[k];
 }

 public void spread(int start,int end,int r0,int g0,int b0,int r1,int g1,int b1)
 {
  int len=end-start;
  double r,g,b,dr,dg,db;
  r=r0; g=g0; b=b0;
  dr=(r1-r)/len; dg=(g1-g)/len; db=(b1-b)/len;
  if (N<=end) for (int i=N;i<=end;i++) addColor(Color.black);
  for (int i=0;i<=len;i++)
   {
    C[i+start]=new Color((int)(r+0.5),(int)(g+0.5),(int)(b+0.5));
    r+=dr; g+=dg; b+=db;
   }
 }























}
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
