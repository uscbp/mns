 
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
class Learn
{

    static public final int shuffledInputCount=1;
static public final int MAX_sample=500;
    static public final int MAX_timeslot=100;
    static public final int MAX_unit=21;
    static public String[] 
	params={"aper1", "ang1","ang2", "speed","dist","axisdisp1","axisdisp2"};//,"dirdisp1"};
//static public String[] mirror={"PRECISION","POWER","SIDE","POWER2","WIDEPREC","WIDESIDE"};
static public int MAXobj=0;
static public int MAXmirror=Graspable.graspc;

    static final double decay=0.5; //decay in AFFRES coding
    static final int AFFRES=10; // only object aperture for now
    static final int OBJRES=MAXobj;  // no object identity is input for now

static public int splineRepresentationRes=30;
static public int indim=splineRepresentationRes*params.length+OBJRES+AFFRES; //no object for now
static public int outdim=MAXmirror;
static public int hiddim=MAXmirror+1;
static public DataOutputStream out=null;
static public String fname="action.pat";

static private int status=0;
static private double initialdist=0;
static private int patc=0;
static private Point3d lastpos=new Point3d(0,0,0);
static private String info="";

static public ParamsNode parnode=new ParamsNode(MAX_sample,params);
    static public ParamsNode hidtimeSeries=null,outtimeSeries=null;
 public Learn()
 {
 }

 static public void reset()
 {
  if (out!=null) close();
  out=null;
  patc=0;
 }

    static final boolean COPYCON=true;
    static public void writeout(DataOutputStream out,String s) throws IOException {
	if (COPYCON) System.err.println("=====> "+ s);
	out.writeBytes(s);
    }
 static public void prepareForPattern()
 {
  status=0;
  hidtimeSeries=null;
  outtimeSeries=null;
  parnode.reset();
 }
 static   private double chopNeg(double v){
	if (v<0) return 0;
	return v;
    }
 static public void collect(Hand hand, Graspable obj)
 {
  if (status==0) 
  { status=1;
    lastpos.x=hand.wristx.limb_pos.x; 
    lastpos.y=hand.wristx.limb_pos.y; 
    lastpos.z=hand.wristx.limb_pos.z; 
    //initialdist=VA.dist(obj.objectCenter,hand.wristx.limb_pos);
    initialdist=VA.dist(obj.objectCenter,hand.indexApertureCenter());
    return;  // wait for next call
  }

  parnode.put("aper1",hand.indexAperture()/350.0);
  parnode.put("ang1",
    Math.pow( (hand.thumb.beta + Math.PI)/(2*Math.PI),2) 
             );
  parnode.put("ang2",
    Math.pow( (hand.thumb.child[0].beta + Math.PI)/(2*Math.PI),2)
             );
  parnode.put("speed",5*VA.dist(lastpos,hand.wristx.limb_pos)/initialdist);
  
  parnode.put("dist",VA.dist(obj.objectCenter,hand.indexApertureCenter())/initialdist);
   //parnode.put("dist",VA.dist(obj.objectCenter,hand.wristx.limb_pos)/initialdist);
  //parnode.put("dist",VA.dist(obj.objectCenter,hand.thumb.child[0].child[0].child[0].limb_pos)/initialdist);
  parnode.put("axisdisp1",
    Elib.sqr(VA.inner(hand.indexAperDir(), obj.lastopposition)));
  parnode.put("axisdisp2",
      Elib.sqr(VA.inner(hand.sideAperDir(), obj.lastopposition)));

  /* 
  Point3d objdir=VA.subtract(obj.objectCenter,hand.wristx.limb_pos);
  VA._normalize(objdir);
  Point3d thumbdir=hand.thumbDir();
 
  parnode.put("dirdisp1",
     Elib.sqr(chopNeg(VA.inner(objdir, thumbdir))));
  
  */
  parnode.advance();
  
  lastpos.x=hand.wristx.limb_pos.x; 
  lastpos.y=hand.wristx.limb_pos.y; 
  lastpos.z=hand.wristx.limb_pos.z; 
 }

 static public void writePattern(Hand hand, Graspable obj)
 {
  writePattern(hand,obj,"");
 }
 static public void writeNegPattern(Hand hand, Graspable obj)
 {
  writeNegPattern(hand,obj,"");
 }
static public void writeNegPattern(Hand hand, Graspable obj,String ss)
    {boolean saveHV=HV.negativeExample;
    HV.negativeExample=true;
    writePattern(hand,obj,ss);
    HV.negativeExample=saveHV;
    }
 static public void writePattern(Hand hand, Graspable obj,String ss)
 {
  info=ss;

  if (HV.self.IamApplet())
   { System.err.println("You can't use this feature in applet mode!"); 
     return;
   }
  System.out.println("------> Writing pattern "+patc+". Object:"+obj.myname+" affords "+obj.grasps[obj.affordance]);
 patc++;
try {
 if (out==null)
  {
   out=Elib.openfileWRITE(fname);
   if (out==null)  
    { System.err.println("Error while creating file:"+fname);
      return;
    }
   writeHeader(hand,obj);
   //System.out.println("Pattern file opened and header is written. Now writing pattern.");
  }
 else; //System.out.println("Good, pattern file is already open. Now writing.");
 writeRandomized(shuffledInputCount,hand, obj);
 } catch (Exception e) {};



 //System.out.println(formInputPattern(hand, obj));

}

 static public double learningrate    =   0.1;
 static public double momentum        =   0.9;
 static public double learningincrease=   0.01;
 static public double learningdecrease=   0.1;

 static private void writeHeader(Hand hand, Graspable obj) throws IOException
 {
   System.out.println("Writing header");
   writeout(out,"# This pattern file is generated by reach& grasp simulator HV (by Erhan Oztop -April'00)\n");
   writeout(out,"outputdim  "+outdim+"\nhiddendim  "+hiddim+"\ninputdim   "+indim+"\n\n");
   writeout(out,"#these are optional network settings. If not supplied defaults will be used\n");
   writeout(out,"\nlearningrate       "+learningrate );
   writeout(out,"\nmomentum           "+momentum);
   writeout(out,"\nlearningincrease   "+learningincrease);
   writeout(out,"\nlearningdecrease   "+learningdecrease+"\n");
   writeout(out,"# The paramaters coding follows this order:\n#");
   for (int i=0;i<params.length;i++) out.writeBytes(params[i]+" ");
   writeout(out,"\n# The paramaters are equally sampled spline curves ("+ splineRepresentationRes+ " samples each)\n");
   writeout(out,"# For each correct pattern there are ("+shuffledInputCount+") shuffled -wrong- pattern.\n\n");
 }


 static public void close()
 {
  System.out.println("Closing pattern file...");
  try{
  if (out!=null) out.close();
  } catch(IOException e) {};
  out=null;
 }


 static public void showParSplines(String gname)
 {
     if (HV.isApplet) return;
  Spline sp[]=parnode.getSplines();
  Spline.showSplines(sp,gname);
/*
  for (int i=0;i<sp.length;i++)
   sp[i].showSpline();
*/
 }

    static int graspCount=0;
 static public void writeRandomized(int K,Hand hand, Graspable obj) throws IOException
    {graspCount++;
  String comment ="# Grasp ["+graspCount+"] for "+obj.myname+" with "+
                  obj.grasps[obj.affordance]+
                  ", obj-aper-size:"+obj.objsize+
                  "["+info+"]\n";
  writeout(out,"#"+comment);
  
  String spl = formSplinePattern(hand,obj); 
  String objs= formObjectIDPattern(obj.ID)  ;  
  String mir = formMirrorPattern(obj.affordance);
  String zero= formMirrorPattern(-11);
  String objaper=formObjectSizePattern(AFFRES,obj.objsize,decay);

  if (!HV.negativeExample) {
  String correct=spl+"   "+objs+"   "+objaper+"   "+mir+"\n";
  writeout(out,correct);
  } else {
      System.out.println("Writing negative example...");
  String negex=spl+"   "+objs+"   "+objaper+"   "+zero+"\n";
  writeout(out,negex);
  }
//    for (int i=0;i<MAXobj;i++)
//    {
//     if (i==obj.ID) continue;
//     String wrong=spl+formObjectPattern(i)+zero+"\n";
//     out.writeBytes(wrong);
//    }

  //Let's shuffle the hand state trajectory
  if (!HV.negativeExample)
      for (int i=0;i<K;i++) {
	  String shuffled=formShuffledPattern(hand,obj);
	  String wrong2=shuffled+"   "+objs+"   "+objaper+"   "+zero+"\n";
	  writeout(out,"# splines shuffled \n"+wrong2);
  }

  
  //Let's shuffle the objectaperture coding
  
  /*
    if (!HV.negativeExample)
    for (int i=0;i<K;i++) {
    String shuffled_objaper=formShuffledObjectSizePattern(AFFRES,obj.objsize,decay);
    String wrong3=spl+"   "+objs+"   "+shuffled_objaper+"   "+zero+"\n";
    writeout(out,"# object size coding randomized.\n"+wrong3);
    }
*/
    }


 static public String formShuffledPattern(Hand hand, Graspable obj)
 {
  String s="";
  Spline sp[]=parnode.getSplines();
  for (int i=0;i<sp.length;i++)
  {
   double step=1.0/(splineRepresentationRes-1);
   for (int j=0;j<splineRepresentationRes;j++)
    {
      double v=sp[i].eval(Math.random());
      s+=Elib.nice(v,1e4)+" ";
    }
  }
  return s;
 }
   
 static public double[] formInputArray(Hand hand, Graspable obj)
 {
  Spline sp[]=parnode.getSplines();
  int size=splineRepresentationRes*sp.length+OBJRES+AFFRES;
  double[] A=new double[size];
  //System.out.println("Input size:"+size);
  int k=0;


  int id=0;
  if (obj.myname.equals("box.seg")) id=0;
  if (obj.myname.equals("coin.seg")) id=1;
  if (obj.myname.equals("pent.seg")) id=2;
  double[] idarr=null;// formZeroArray(3); //formObjectIDArray(id);
  double[] splarr=formSplineArray(hand,obj);
  double[] objsizearr= formObjectSizeArray(AFFRES,obj.objsize,decay);  
 
  if (splarr!=null) for (int i=0;i<splarr.length;i++) A[k++]=splarr[i];
  System.out.println("Wrote "+k+" entries for spline");
  if (idarr !=null) for (int i=0;i<idarr.length;i++) A[k++]=idarr[i];
  System.out.println("Write index is  "+k+" after object id entries");
  if (objsizearr!=null) for (int i=0;i<objsizearr.length;i++) A[k++]=objsizearr[i]; 
  System.out.println("Write index is  "+k+" after objectsize  entries");
    System.out.println("~~~~~~~~~~Object size:"+obj.objsize);

  if (k!=size) 
   System.out.println("WARNING: input sie mismatch. Put in "+k+" values...");
   // for (int i=0;i<A.length;i++) System.out.println(i+":"+A[i]);
  return A;
 }

    static double objectSizeCode(int len,double aper,double decay,int i) {

	double apercode=aper/350.0;
	double peak=(apercode*(len-2))+2;
	return F(decay,peak,i);

	    }

    static String formObjectSizePattern(int len,double objaper, double decay) {
	String s="";
	for (int i=0;i<len;i++)
       {   s=s+Elib.nice(objectSizeCode(len,objaper,decay,i),1e4)+" ";
       //System.out.print(A[k-1]+" ");
       }
	return s;
    }

    static double[] formObjectSizeArray(int len,double objaper, double decay) {
	double[] a=new double[len];
        objaper*=HV.self.object_scale_fix;
	System.out.println(" - - - - - -  > Object aperture used:"+objaper);
	for (int i=0;i<len;i++) {
	    a[i]=objectSizeCode(len,objaper,decay,i);
	}
	return a;
    }
   static String formShuffledObjectSizePattern(int len,double objaper, double decay) {
	String s="";
	for (int i=0;i<len;i++)
	    {   
		int j=(int)(Math.random()*len+0.5);
		s=s+Elib.nice(objectSizeCode(len,objaper,decay,j),1e4)+" ";
       //System.out.print(A[k-1]+" ");
       }
	return s;
    }

static double F(double decay,double max,double oth) {
   return (1.0/(1+decay*(max-oth)*(max-oth))); 
}
    
    static public double[] formSplineArray(Hand hand, Graspable obj) { 
	Spline sp[]=parnode.getSplines();
	int size=splineRepresentationRes*sp.length;
	double[] A=new double[size];
	//System.out.println("Input size:"+size);
        int k=0; 
	for (int i=0;i<sp.length;i++)
	    {
		double step=1.0/(splineRepresentationRes-1);
		for (int j=0;j<splineRepresentationRes;j++)
		    {
			double v=sp[i].eval(j*step);
			A[k++]=v;
		    }
	    }
	return A;
    }

 static public String formSplinePattern(Hand hand, Graspable obj)
 {
  String s="";
  Spline sp[]=parnode.getSplines();
  for (int i=0;i<sp.length;i++)
  {
   double step=1.0/(splineRepresentationRes-1);
   for (int j=0;j<splineRepresentationRes;j++)
    {
      double v=sp[i].eval(j*step);
      s+=Elib.nice(v,1e4)+" ";
    }
  }
  return s;
 }

    // No object identity coding now
 static public String formObjectIDPattern(int ID)
 {
  String s="";
  return "";
  /*
   for (int i=0;i<MAXobj;i++)
   if (ID==i) s+=1+" ";
             else s+=0+" ";
  s+="    ";
 return s;
  */
 }
 static public double[] formObjectIDArray(int ID)
 {
     return null;
/*
     doubel v=new double[MAXobj];
     String s="";
  
    
       for (int i=0;i<MAXobj;i++)
       if (ID==i) v[i]=1;
       else v[i]=;;
       return v;
*/
 }

  //The mirror response coding
  static public String formMirrorPattern(int affordance)
  {
    String s="";
   for (int i=0;i<MAXmirror;i++)
    if (affordance==i) s+=1+" ";
              else s+=0+" ";
  return s;
  }

    static public double[] formZeroArray(int c) {
	double[] a=new double[c];
	for (int i=0;i<c;i++) a[i]=0;
	return a;
    }
    static public void showtimeSeries(String gname)
    {
	if (HV.isApplet) return;
	//Spline sp[]=hidtimeSeries.getSplines();
	Spline sp1[]=outtimeSeries.getSplines();
	//Spline.showSplines(sp);
	Spline.showSplines(sp1,gname);
	
    }
    static public String[] makeString(String  base, int c)
    {
	String s[]=new String[c];

	for (int i=0;i<c;i++) s[i]=base+i;
	return s;
    }
    static public void getTimeSeries(double[] inp,double[] hid, double[] out)
    {
	/*
if (hidtimeSeries==null) {
	    hidtimeSeries=new ParamsNode(MAX_timeslot, makeString("hid.",hid.length));
	} 
	*/  
if (outtimeSeries==null) {
	    outtimeSeries=new ParamsNode(MAX_timeslot, makeString("out.",out.length));
}
/*
for (int i=0;i<hid.length;i++)
  hidtimeSeries.put("hid."+i,hid[i]);
*/


 for (int i=0;i<out.length;i++)
  outtimeSeries.put("out."+i,out[i]);

 //hidtimeSeries.advance();
outtimeSeries.advance();
    }

static public String interpret(double[] ans)
 {double min=1e10;
  double max=-1e10;
  int minix=-1,maxix=-1;
  for (int i=0;i<ans.length;i++)
   {
    //System.out.print(" "+Elib.snice(ans[i],1e3,6));
    if (ans[i]<min)
      { minix=i;
        min=ans[i];
      }
    if (ans[i]>max)
      { maxix=i;
        max=ans[i];
      }
   }
   //System.out.println("");
   String s;
   if ((max-min)>0.1)
     s=Graspable.getGrasp(maxix);
   else
     s="[not confident] "+Graspable.getGrasp(maxix);
   return s;
 }

    static double[] lastout=null;
    static NDisp ndisp=null;
    static double[][] inpHistory=null;
    static double[][] hiddenHistory=null;
    static double[][] outHistory=null;
    static int index=0;
static public String recognize(BP bp,Hand hand, Graspable obj,String gname, boolean plot)
  {
    if(inpHistory==null)
    {
        inpHistory = new double[MAX_timeslot][bp.Xdim];
        hiddenHistory = new double[MAX_timeslot][bp.Ydim];
        outHistory = new double[MAX_timeslot][bp.Zdim];
        index=0;
    }
   double[] inp=formInputArray(hand,obj);
   double[] out=bp.ask(inp,inp.length);
   double[] hidden=bp.askHidden(inp,inp.length);
   System.arraycopy(inp, 0, inpHistory[index], 0, bp.Xdim);
   System.arraycopy(hidden, 0, hiddenHistory[index], 0, bp.Ydim);
   System.arraycopy(out, 0, outHistory[index], 0, bp.Zdim);
   index++;
   if(plot)
   {
       Gplot g1 = new Gplot(outHistory, index, 3, null, "set title \"output\";set xlabel \"time\";set zlabel \"activation\";set ylabel \"unit\"; set label \"precision\" at 0,0,0; set label \"side\" at 0,1,0; set label \"power\" at 0,2,0");
       Gplot g2 = new Gplot(hiddenHistory, index, bp.Ydim, null, "set title \"hidden\";set xlabel \"time\";set zlabel \"activation\";set ylabel \"unit\"");
       Gplot g3 = new Gplot(inpHistory, index, bp.Xdim, null, "set title \"input\";set xlabel \"time\";set zlabel \"activation\";set ylabel \"unit\"");
   }
   if (ndisp==null) {
       ndisp=new NDisp(bp,"mirror output");
       ndisp.setBounds(100,100,400,400);
       ndisp.show();
       
   }
   ndisp.drawArray(out,0,1,null,out.length,1);
   lastout=out;
   getTimeSeries(inp,hidden,out);
   for (int i=0;i<out.length;i++)
    System.out.print(Elib.nice(out[i],1e4)+"   ");
   System.out.println("");
   System.out.print("   HHHHHH [ ");
   for (int i=0;i<hidden.length;i++)
    System.out.print(Elib.nice(hidden[i],1e4)+"   ");
   System.out.println("]");

   showParSplines(gname);
   return interpret(out);
  }

//  static public String interpret(double[] ans)
//   {double min=1e10;
//    double max=-1e10;
//    int minix=-1,maxix=-1;
//    for (int i=0;i<ans.length;i++)
//     {
//      //System.out.print(" "+Elib.snice(ans[i],1e3,6));
//      if (ans[i]<min)
//        { minix=i;
//          min=ans[i];
//        }
//      if (ans[i]>max)
//        { maxix=i;
//          max=ans[i];
//        }
//     }
//     //System.out.println("");
//     String s;
//     if ((max-min)>0.1)
//       s=Graspable.getGrasp(maxix);
//     else
//       s="[not confident] "+Graspable.getGrasp(maxix);
//     return s;
//   }

}



  
  



 
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
