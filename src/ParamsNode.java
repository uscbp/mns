 

import java.awt.*;
import java.util.*;
import java.awt.*;
import java.lang.*;
import java.util.Vector;
import java.util.Enumeration;
import java.io.*;
/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */

public class ParamsNode
{
 private int MAX_N;
 private int parc;
 private Hashtable parhash;
 private double par[][];
 private int slot=0;
 private String names[];
 
 public ParamsNode(int maxsample, String[] params)
 {
  System.out.println("Creating paramsnode....");
  parhash=new Hashtable();
  MAX_N=maxsample;
  parc=params.length;
  for (int i=0;i<parc;i++)
   parhash.put(params[i],new Integer(i));
  par=new double[parc][MAX_N];
  names=new String[parc];
  for (int i=0;i<parc;i++) names[i]=params[i];
  System.out.println("Leaving Creation of paramsnode....");
 }

 private int getIndex(String s)
 {
  return ((Integer)parhash.get(s)).intValue();
 }

 public void advance()
 {
  slot++;
 }

 public void put(String s,double value)
 {
  int k=getIndex(s);
  par[k][slot]=value;
 }

 public double[][] getAll()
 {
  return par;
 }

 public double getRel(String s,int relpos)
 {
  int k=getIndex(s);
  return par[k][slot+relpos];
 }
 public double getAbs(String s,int pos)
 {
  int k=getIndex(s);
  return par[k][pos];
 }

 public Spline getSpline(String s)
 {
  int k=getIndex(s);
  Spline sp=new Spline(slot,par[k]);
  return sp;
 }
  
 public Spline[] getSplines()
 {
  Spline[] sp=new Spline[parc];
  for (int k=0;k<parc;k++)
   {
    sp[k]=new Spline(slot,par[k]);
    sp[k].setLabel(names[k]);
   }
  return sp;
 }
   
 public void reset()
 {
  slot=0;
 }
}
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
