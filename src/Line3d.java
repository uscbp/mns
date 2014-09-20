/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
class Line3d 
{
 int first,second;
 int color=0;
 public Line3d()
 { first=second=0;
 }
 public Line3d(int i)
 {
   first=second=i;
 }

 public Line3d(int i,int j)
 { 
   first=i;
   second=j;
 }

 public String str()
 { 
  return ("["+first+" to "+second+"]");
 }

 
}

   
 
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
