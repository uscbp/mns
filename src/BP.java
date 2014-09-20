 
 
import java.io.*;
import java.net.*;
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

public class BP extends Frame
{
 
  public Vector patterns;
  public double inpat[][];
  public double outpat[][]; 
  public int patc=0;
  
  public boolean valid=false; // is there a valid net created?

  public double eta,etaDOWN,etaUP   ;
  public double beta  ;

  public double X[];  // input layer
  public double Xnet[];  // input layer before squashed
  public double Y[];  // hidden layer
  public double Ynet[];  // hidden layerbefore squashed
  public double Z[];  // output layer 
  public double Znet[];  // output layer before squashed
  public double eZ[];  // output layer error
  public double eY[];  // hidden layer error
  public double W[][]; // hidden to output weights (Y->Z weights)
  public double w[][]; // input to hidden weights  (X->Y weights)
  public double dW[][]; // hidden to output weights (Y->Z weights)
  public double dw[][]; // input to hidden weights  (X->Y weights)
  public double _dW[][]; // old hidden to output weights (Y->Z weights)
  public double _dw[][]; // old input to hidden weights  (X->Y weights)


  public int Xdim;    // input dimension
  public int Ydim;    // number of hidden units
  public int Zdim;    // output dimension


  public TextField pattern_filename,weight_filename,epochs;
  public Scrollbar wsc;
  public static final int wscMAX=1000;
  public static final double wscrealMAX=32; // max real value used by drawconn
  MyCanvas cv=null;
  public BP()
  {
 
setTitle("Back Propagation. - Erhan Oztop Dec'99");
    setLayout(new BorderLayout());
    //canvas = new DrawCanvas(this);
    //canvas.setBackground(Color.white);
    //add("Center",canvas);
    Panel p1=new Panel();
    Panel p2=new Panel();
    p1.setLayout(new GridLayout(5,2));
    p2.setLayout(new GridLayout(2,3));
    p2.add(new Label("Pattern File:",Label.LEFT));
    pattern_filename=new TextField("xor2.pat",12);
    p2.add(new Label("Weight File:",Label.LEFT));
    weight_filename=new TextField("weights.dat",12);
    p2.add(new Label("Training Epochs:",Label.LEFT));
    epochs=new TextField("10000",12);
    p2.add(pattern_filename);
    p2.add(weight_filename);
    p2.add(epochs);

    Button lastb;
    p1.add(lastb=new Button("Load Pattern"));
    p1.add(lastb=new Button("Load Weight"));
    p1.add(lastb=new Button("Train"));
    p1.add(lastb=new Button("Test"));
    p1.add(lastb=new Button("Randomize Weights"));
//    p1.add(new Label("*",Label.CENTER));
    p1.add(new Label("*",Label.CENTER));
    p1.add(lastb=new Button("Make Network from Pattern"));
    lastb.setForeground(Color.white); lastb.setBackground(Color.blue); 
    p1.add(lastb=new Button("Make Network from Weight"));
    lastb.setForeground(Color.white); lastb.setBackground(Color.blue); 
    p1.add(lastb=new Button("Generate Weight File"));
    lastb.setBackground(Color.red); lastb.setForeground(Color.white);
    p1.add(lastb=new Button("QUIT"));
    add("North",p1);
    add("South",p2);
    cv=new MyCanvas(this);
    add("Center",cv);
    wsc=new Scrollbar(Scrollbar.VERTICAL, wscMAX, 1, 0,wscMAX+1);
    add(wsc,"West");
  }



  public void createNet(int indim,int hiddim,int outdim)
  { 
    if (hiddim<=0) hiddim=indim/2 + 1;
  
     Xdim=indim;
     Ydim=hiddim;
     Zdim=outdim;
    X    = new double[Xdim+1];   // last one will be clamped to 1
    Y    = new double[Ydim+1];   // last one will be clamped to 1
    Ynet = new double[Ydim+1];
    Z    = new double[Zdim];
    Znet = new double[Zdim];

    eY=new double[Ydim+1];   //  error at the hidden layer
    eZ=new double[Zdim];     //  error at the output layer

    w  = new double [Ydim+1][Xdim+1];  // Y=w*X
    dw = new double [Ydim+1][Xdim+1];
    _dw = new double [Ydim+1][Xdim+1]; // old dw
    W  = new double [Zdim  ][Ydim+1];  // Z=W*Y
    dW = new double [Zdim  ][Ydim+1];
    _dW = new double [Zdim  ][Ydim+1]; // old DW

    initNet();
    valid=true;
   
  }

  public BP(int indim,int hiddim,int outdim,double def_eta, double def_beta,double def_etaUP, double def_etaDOWN)
 {
   this();
   beta=def_beta;
   eta =def_eta;
   etaUP=def_etaUP;
   etaDOWN=def_etaDOWN;
   createNet(indim,hiddim,outdim); 
  }


  public BP(String s)
  {
   this(s,0.05,0.9,0.01,0.1);
  }

  public BP(String s,double def_eta, double def_beta,double def_etaUP,
                                                   double def_etaDOWN)
  {
     this();
     beta=def_beta;
     eta =def_eta;
     etaUP=def_etaUP;
     etaDOWN=def_etaDOWN;
     System.out.println("Constructing the network with file:"+s);
     netFromPattern(s);
  }


 public void netFromPattern(String with)
 {
   cv.showAll();
   patc=0;
   if (!readPattern(with))
     { System.err.println("Error occured reading pattern file:"+with);
        return;
      }

      //dumpPattern();
     
     if (pat_beta>-1) beta=pat_beta;
     if (pat_eta> -1)  eta=pat_eta;
     if (pat_etaUP>-1) etaUP=pat_etaUP;
     if (pat_etaDOWN>-1) etaDOWN=pat_etaDOWN;

     //create the network
     createNet(pat_indim,pat_hiddim,pat_outdim);
 }

    public void netFromWeight(String with) {
	netFromWeight(null,with);
    }
 public void netFromWeight(URL base, String with)
 {
   cv.showAll();
   if (!readWeight(base,with)) 
    { System.err.println("Error occured reading weight file:"+with);
      return;
    }
   if (base==null)
       {
   dumpMatrix("intput->hidden weights",filew,fileYdim+1,fileXdim+1);
   dumpMatrix("hidden->output weights",fileW,fileZdim,fileYdim+1);
       }
   //create the network
   createNet(fileXdim,fileYdim,fileZdim);
   for (int i=0;i<fileYdim+1;i++)
    {
    for (int j=0;j<fileXdim+1;j++)
      w[i][j]=filew[i][j];
    }

    for (int k=0;k<fileZdim;k++)
    {
    for (int i=0;i<fileYdim+1;i++)
      W[k][i]=fileW[k][i];
    }
    System.err.println("Xdim:"+Xdim+" Ydim:"+Ydim+" Zdim:"+Zdim);

 }

public void installWeight(String with)
 {
   cv.showAll();
   if (!readWeight(with))
    { System.err.println("Error occured reading weight file:"+with);
      return;
    }
   if (filew==null || fileW==null)  
     { System.err.println("Error occured reading weight file:"+with);
      return;
    }
 
   dumpMatrix("intput->hidden weights",filew,fileYdim+1,fileXdim+1);
   dumpMatrix("hidden->output weights",fileW,fileZdim,fileYdim+1);
   //create the network
   if (fileXdim!=Xdim) 
     {System.err.println("Mismatch in input dimension!"); return; }
   if (fileYdim!=Ydim) 
     {System.err.println("Mismatch in hidden dimension!"); return; }
   if (fileZdim!=Zdim) 
     {System.err.println("Mismatch in output dimension!"); return; }
   for (int i=0;i<fileYdim+1;i++)
    {
    for (int j=0;j<fileXdim+1;j++)
      w[i][j]=filew[i][j];
    }

    for (int k=0;k<fileZdim;k++)
    {
    for (int i=0;i<fileYdim+1;i++)
      W[k][i]=fileW[k][i];
    }
    System.err.println("Xdim:"+Xdim+" Ydim:"+Ydim+" Zdim:"+Zdim);

 }



 // we are using G(x)= 1/(1+exp(-2bX)) with b=0.5 
  public void squash(double[] v,int size,double[] r)
  { for (int i=0;i<size;i++)
        r[i]=1.0/(1+Math.exp(-v[i]));
  }

  // r=Mv, im size of r, jm is size of v (M is imxjm)
  public void multiply(double M[][], double[] v, int im, int jm,double r[])
  { for (int i=0;i<im;i++)
     {  r[i]=0;
        for (int j=0;j<jm;j++) r[i]+=M[i][j]*v[j];
      }
  }

  public void addto(double M[][], int im,int jm, double[][] C )
  { for (int i=0;i<im;i++)
      for (int j=0;j<jm;j++) M[i][j]+=C[i][j];
  }

  public void forward()
  {
    multiply(w,X,Ydim+1,Xdim+1,Ynet);
    Ynet[Ydim]=0;  // to avoid confusion in use of Ynet in G' evaluation
    squash(Ynet,Ydim+1,Y);
    Y[Ydim]=1;    //  clamped to 1
    multiply(W,Y,Zdim,Ydim+1,Znet);
    squash(Znet,Zdim,Z);   
  }
 

// learn pattern p
  public double learn(int p,int step)
  {
    double err=0;
    for (int runs=0;runs<step;runs++)
    {
    forward();
    for (int k=0;k<Zdim;k++) 
       { double ee=(outpat[p][k]-Z[k]);
         eZ[k]=ee*(1-Z[k])*Z[k];
                 // this part equals G'(Znet) for b=0.5
         err+=ee*ee;
        }
     
    for (int i=0;i<Ydim+1;i++) 
     {
       eY[i]=0;
       for (int k=0;k<Zdim;k++) eY[i]+=W[k][i]*eZ[k]; //each Y[i]'s error share
       eY[i]=eY[i]*(1-Y[i])*Y[i];
                   // this part equals G'(Ynet) for b=0.5
     }
       
    for (int i=0;i<Ydim+1;i++)
    for (int j=0;j<Xdim+1;j++) 
      {                        // momentum term 
       int M=Ydim/Zdim;
       int N=30;// Xdim/Zdim;
       int R=N/M;

        dw[i][j]=eta*eY[i]*X[j] + beta*_dw[i][j];
        _dw[i][j]=dw[i][j];

      }

    for (int k=0;k<Zdim  ;k++)
    for (int i=0;i<Ydim+1;i++) 
     {
      dW[k][i]=eta*eZ[k]*Y[i] + beta*_dW[k][i];
      _dW[k][i]=dW[k][i];
     }

   addto(W,Zdim,Ydim+1,dW);
   addto(w,Ydim+1,Xdim+1,dw);
   } 
   return err/step;
 }

 public void presentPattern(int p)
 {
  for (int j=0;j<Xdim;j++)
   X[j]=inpat[p][j];
  X[Xdim]=1.0;           //clamp to zero
 }

 public void presentPattern(double[] inp)
 {
  for (int j=0;j<Xdim;j++)
   X[j]=inp[j];
  X[Xdim]=1.0;           //clamp to zero
 }

 double[] ask(double [] inp,int size)
 {
  if (size!=Xdim)    
   {System.err.println("Pattern input dimensionCount does not match network input dimensionCount!!");
    return null;
   }
  presentPattern(inp);
  forward();
/*
  System.out.print("Z[] = ");
  for (int i=0;i<Zdim;i++)
  System.out.print(Z[i]+" ");
  System.out.println("");
*/

  return Z;
 }
 
 double[] askHidden(double [] inp,int size)
 {
  if (size!=Xdim)
   {System.err.println("Pattern input dimensionCount does not match network input dimensionCount!!");
    return null;
   }
  presentPattern(inp);
  forward();

/*
  System.out.print("Y[] = ");
  for (int i=0;i<Ydim;i++)
  System.out.print(Y[i]+" ");
  System.out.println("");
*/


  double[] hid=new double[Ydim];
  System.arraycopy(Y,0,hid,0,Ydim);
  return hid;
 }


  
 public double testPattern()
 { return testPattern(false); }
 public double testPattern(boolean verbose)
 {
   double err=0;
   int OK=0;

   if (pat_indim!=Xdim)
   {System.err.println("Pattern input dimensionCount does not match network input dimensionCount!!");
    return -1;
   }
   if (pat_outdim!=Zdim)
   {System.err.println("Pattern output dimensionCount does not match network output dimensionCount!!");
    return -1;
   }
 
   double maxerr=-1;
   for (int p=0;p<patc;p++)
   { 
     presentPattern(p);
     forward();

     if (verbose) System.out.print(p+") Output:");
     int oo=0;
     double thiserr=0;
     for (int k=0;k<Zdim;k++)
      {
       double ee=(outpat[p][k]-Z[k])*(outpat[p][k]-Z[k]);
       if (ee<0.1) oo++;
       if (ee>maxerr) maxerr=ee;
       thiserr+=ee;
       if (verbose) System.out.print(Elib.snice(Z[k],1e3,6)+"["+outpat[p][k]+"]   "); 
      }
       if (verbose) System.out.println(""); 
      //thiserr/=Zdim;
      err+=thiserr;
      if (oo==Zdim) OK++;
      
/*
     System.out.print("\n"+p+" Target:");
     for (int k=0;k<Zdim;k++)
      System.out.print(outpat[p][k]+"  "); 
     System.out.println("");
*/
   }
   //err/=patc;
   String rep=totalit+":Total error over patterns:"+err+" # Correct:"+OK+"/"+patc+
              "MAX (unit) err:"+maxerr+" [L.rate:"+eta+"]";
   setTitle(rep);
   //if (verbose) 
   System.out.println(rep);
   return err;
 }

public void initNet()
 {
    totalit=0;
       int M=Ydim/Zdim;
       int N=30;// Xdim/Zdim;
       int R=N/M;
       System.out.println("R:"+R+" M:"+M+", N:"+N);
    for (int i=0;i<Ydim+1;i++)
    for (int j=0;j<Xdim+1;j++)
      { 
       w[i][j]=(Math.random()-0.5)*0.1;
       _dw[i][j]=0;
       }

    for (int k=0;k<Zdim  ;k++)
    for (int i=0;i<Ydim+1;i++)
      {
       W[k][i]=(Math.random()-0.5)*0.1;
       _dW[k][i]=0;
      }
  }

public void clearHistory()
 {
    if (_dw!=null)
    for (int i=0;i<Ydim+1;i++)
    for (int j=0;j<Xdim+1;j++)
      {
       _dw[i][j]=0;
       }

    if (_dW!=null)
    for (int k=0;k<Zdim  ;k++)
    for (int i=0;i<Ydim+1;i++)
      {
       _dW[k][i]=0;
      }
 }

int totalit=0;
public void train(int maxiter)
 { double err=0,immerr,olderr=0,dE=0;
   double[] rr;
   int avc=0,p=0;
   if (pat_indim!=Xdim) 
   {System.err.println("Pattern input dimensionCount does not match network input dimensionCount!!");
    return;
   }
   if (pat_outdim!=Zdim) 
   {System.err.println("Pattern output dimensionCount does not match network output dimensionCount!!");
    return;
   }
   rr=new double[Xdim];
   for (int it=0;it<maxiter;it++)
   {
    totalit++;
    
    if (Math.random()<0.3) 
    { p=patc;  
      for (int i=0;i<Xdim;i++) inpat[patc][i]=Math.random();
     } else
     
    p=((int)(Math.random()*100000))%patc;

    presentPattern(p);
    immerr=learn(p,1);
    if (p==patc) continue; 
    olderr=err;
    err=immerr; //(0.9*err+immerr);
    dE+=err-olderr;
    avc++;
    if (avc==20*patc) 
    { if (dE<0) 
       { eta+=etaUP; 
         //System.out.println("Learning rate increased to:"+eta);
       } 
      else
       { eta-=etaDOWN*eta; 
         //System.out.println("Learning rate deccreased to:"+eta);
       } 
       avc=0; dE=0;
     }
    
    if (it%16000==0) 
      {double terr=testPattern();
       if (terr<0.00001) { System.err.println("Error is less than 0.0000001, stopping training.");
                     return;}
      }
   }
 }

  public static void main(String[] argv)
  {    
       BP bp;
       String s=null;
       if (argv.length>0) bp=new BP(argv[0]);
                    else  bp=new BP();
       bp.resize(400,300);
       bp.show();
       bp.locate(20,40);
/*
       bp.train(1000000);
       double finalerr=bp.testPattern();
       System.err.println( "Training done. Final error on the training patterns:"+finalerr);
*/
       }

  public void dumpNet()
 {
     System.out.println("** Below data reflects the current Network **");
    System.out.println("(L.rate)eta          :"+eta);
    System.out.println("(L.rate+)etaUP       :"+etaUP);
    System.out.println("(L.rate-)etaDOWN     :"+etaDOWN);
    System.out.println("(momentum)beta       :"+eta);
    System.out.println("-------------------------------------");
    System.out.println("(input)   Xdim:"+Xdim);
    System.out.println("(hidden)  Ydim:"+Ydim);
    System.out.println("(output)  Zdim:"+Zdim);
    System.out.println("** Above data reflects the current Network **");
  }

  public void dumpPattern()
  {
    System.out.println("** Below data reflects the current PATTERN FILE **");
    System.out.println("(L.rate)pat_eta          :"+pat_eta);
    System.out.println("(L.rate+)pat_etaUP       :"+pat_etaUP);
    System.out.println("(L.rate-)pat_etaDOWN     :"+pat_etaDOWN);
    System.out.println("(momentum)pat_beta       :"+pat_eta);
    System.out.println("-------------------------------------");
    System.out.println("(input)   Xdim:"+pat_indim);
    System.out.println("(hidden)  Ydim:"+pat_hiddim);
    System.out.println("(output)  Zdim:"+pat_outdim);
    System.out.println("# Patterns loaded:"+patc);
    for (int i=0;i<patc;i++)
     { for (int j=0;j<pat_indim;j++) 
          System.out.print(inpat[i][j]+" ");
       System.out.print("     ");
       for (int j=0;j<pat_outdim;j++) 
          System.out.print(outpat[i][j]+" ");
       System.out.println("");
      }
    System.out.println("** Above data reflects the current PATTERN FILE **");
  }

  public double toDouble(String s)
  {
   Double d=new Double(s);
   return d.doubleValue();
  }

 public int toInt(String s)
  {
   return (int)(toDouble(s)+0.5);
  }

static public DataInputStream openfileREAD(String fn) throws IOException
 { DataInputStream in = new DataInputStream(new FileInputStream(fn));
     return in;
 }

static public DataInputStream openfileREAD(URL base,String fn) throws IOException
    { System.out.println("openfileREAD:base="+base+"  dimensionName="+fn);
   DataInputStream in = Elib.openURLfile(base,fn);
   return in;
 }

static public DataOutputStream openfileWRITE(String fn) throws IOException
 {
   DataOutputStream out = new DataOutputStream(new FileOutputStream(fn));
     return out;
 }

 int  pat_indim=0,pat_outdim=0,pat_hiddim=0;
 double pat_eta=0,pat_beta=0,pat_etaUP=0,pat_etaDOWN=0;

 public boolean readPattern(String fn)
 {
   Vector vpi=new Vector(40),vpo=new Vector(40);
   ArrayCover bufi=null,bufo=null;
   String t[]=new String[2];
   int tc=0,linec=0;
   String s,u;
   boolean added=false;
   pat_indim=0;pat_outdim=0;pat_hiddim=0;
   pat_eta=-1;pat_beta=-1;pat_etaUP=-1;pat_etaDOWN=-1;
   try {
   DataInputStream in = openfileREAD(fn);
   if (in==null) return false;
   linec=0;
   while (null!=(s=in.readLine()))
   {
    linec++;
    if (s.equals("")) continue;
    if (s.charAt(0) == '#') continue;
    StringTokenizer st= new StringTokenizer(s," ");
    tc=0; 
    added=false;
    while (st.hasMoreTokens())
    {

     u = st.nextToken();
     //System.out.println("token:"+u);
     if (tc==0 && u.equals("inputdim"))
     { u = st.nextToken(); 
       pat_indim=toInt(u);
       continue;
     }       
     if (tc==0 && u.equals("outputdim"))
     { u = st.nextToken();
       pat_outdim=toInt(u);
       continue;
     }
     if (tc==0 && u.equals("hiddendim"))
     { u = st.nextToken();
       pat_hiddim=toInt(u);
       continue;
     }
     if (tc==0 && u.equals("learningrate"))
     {
       u = st.nextToken();
       pat_eta=toDouble(u);
       continue;
     }
     if (tc==0 && u.equals("momentum"))
     {
       u = st.nextToken();
       pat_beta=toDouble(u);
       continue;
     }
     if (tc==0 && u.equals("learningincrease"))
     {
       u = st.nextToken();
       pat_etaUP=toDouble(u);
       continue;
     }
     if (tc==0 && u.equals("learningdecrease"))
     {
       u = st.nextToken();
       pat_etaDOWN=toDouble(u);
       continue;
     }
     if (bufi==null){
     bufi=new ArrayCover(pat_indim);
     bufo=new ArrayCover(pat_outdim);
     }

     if (tc<pat_indim) { bufi.val[tc]=toDouble(u);  
                   // System.out.println("bufi["+tc+"]:"+bufi.val[tc]);         
                        }
                 else  { bufo.val[tc-pat_indim]=toDouble(u);   
//   System.out.println("bufo["+(tc-pat_indim)+"]:"+bufo.val[tc-pat_indim]); 
                       }
     added=true; tc++;
    }
    if (!added) continue;
    vpi.addElement(bufi);
    vpo.addElement(bufo);
    bufi=null;
    bufo=null;
    patc++;
    if (tc!=pat_indim+pat_outdim)
    {
     System.err.println("File format Error in "+fn+" line "+linec);
     System.err.println("indim+outdim:"+(pat_indim+pat_outdim)+" BUT token count:"+tc);
     return false;
    }
   }
   in.close();
  } catch (IOException e)
   { System.err.println("BP.readPattern() : EXCEPTION "+e);
   }
  
//+1 is for random input presentation
 inpat   = new double[patc+1][pat_indim];
 outpat  = new double[patc+1][pat_outdim];

 for (int i=0;i<pat_outdim;i++) outpat[patc][i]=0;  // 0 output for rand.

 ArrayCover r;

 int i=0;
 Enumeration e=vpi.elements();
   while ( e.hasMoreElements())
   { r=(ArrayCover)e.nextElement();
     ///System.out.println("");
     for (int j=0;j<pat_indim;j++) inpat[i][j]=r.val[j];
     i++;
   }

 i=0;
 Enumeration  f=vpo.elements();
   while ( f.hasMoreElements())
  { r=(ArrayCover)f.nextElement();
    for (int j=0;j<pat_outdim;j++) outpat[i][j]=r.val[j];
    i++;
  }
  return true;
 }

 int fileXdim, fileYdim, fileZdim;
 double[][] filew;
 double[][] fileW;

    public boolean readWeight(String fn) {
	return readWeight(null,fn);
    }
 public boolean readWeight(URL base,String fn)
 {
   filew=null;
   fileW=null;
   fileXdim=0;
   fileYdim=0;
   fileZdim=0;

   String t[]=new String[2];
   int tc=0,linec=0,row=0;
   String s,u;
   boolean added=false;
   try {DataInputStream in;
   if (base==null) in = openfileREAD(fn);
   else in=openfileREAD(base,fn);
   if (in==null) return false;
   linec=0;
   while (null!=(s=in.readLine()))
   {
    linec++;
    if (s.equals("")) continue;
    if (s.charAt(0) == '#') continue;
    StringTokenizer st= new StringTokenizer(s," ");
    tc=0; 
    added=false;
    while (st.hasMoreTokens())
    {

     u = st.nextToken();
     if (tc==0 && u.equals("inputdim"))
     { u = st.nextToken(); 
       fileXdim=toInt(u);
       continue;
     }       
     if (tc==0 && u.equals("outputdim"))
     { u = st.nextToken();
       fileZdim=toInt(u);
       continue;
     }
     if (tc==0 && u.equals("hiddendim"))
     { u = st.nextToken();
       fileYdim=toInt(u);
       continue;
     }
     if (fileXdim==0 || fileYdim==0 || fileZdim==0)
     { System.err.println("The weight file doesn't specify the net size properly!");}
/* threshold implementation is fixed 1 as the last term */
     if (filew==null) filew=new double[fileYdim+1][fileXdim+1];
     if (fileW==null) fileW=new double[fileZdim][fileYdim+1];
     //System.err.println("row,tc:"+row+","+tc+" = "+u);
     if (row<fileYdim+1) filew[row][tc]=toDouble(u);  
                  else   fileW[row-fileYdim-1][tc]=toDouble(u);   
     added=true; tc++;
    }
    if (!added) continue;
    if (tc!=((row<fileYdim+1)?fileXdim+1:fileYdim+1))
    {
     System.err.println("File format Error in "+fn+" line "+linec);
     return false;
    }
    row++;
   }
   in.close();
  } catch (IOException e)
   { System.err.println("BP.readPattern() : EXCEPTION "+e);
   }
  
  return true;
 }

void dumpMatrix(String title, double[][] w, int rows,int cols)
{
   System.out.println("#"+title);
   for (int i=0;i<rows;i++)
    {
    for (int j=0;j<cols;j++) 
      System.out.print(w[i][j]+" ");
    System.out.println("");
    }
}

 public void writeWeight(String fn)
 {
  System.out.println("Creating weight file:"+fn);

   try {
   DataOutputStream out = openfileWRITE(fn);
   out.writeBytes("# This weight file is generated by BP (Erhan Oztop -Dec'99)\n");
   out.writeBytes("# This file specfies the network size and the weight values\n");
out.writeBytes("# That the network sizes excludes the clamped 1's for input and hidden layer\n");
out.writeBytes("# So the weight matrices has one more column for the clamped unit.\n");
out.writeBytes("\n# Note: To train the network you need to load a pattern file\n");
out.writeBytes("# Note: You can not specify learning parameters from this file\n");
out.writeBytes("# Note: If you want to continue a learning session that you saved the \n");
out.writeBytes("# weights from, use Make Network from Weight followed by Load Pattern then continue training.\n\n");
out.writeBytes("# First matrix is the input(x)->hidden(y) weights(w) \n");
out.writeBytes("# Second matrix is the hidden(y)->output(z) weights(W) \n");
out.writeBytes("# The network computes  sgn(W.sgn(w.x)) where sgn(t)=1/(1+exp(-t))\n\n");

   out.writeBytes("outputdim  "+Zdim+"\nhiddendim  "+Ydim+"\ninputdim   "+Xdim+"\n\n");
 out.writeBytes("#input  -> hidden weights  w["+(Ydim+1)+"]["+(Xdim+1)+"]\n");
   for (int i=0;i<Ydim+1;i++)
    {
    for (int j=0;j<Xdim+1;j++)
      out.writeBytes(w[i][j]+" ");
    out.writeBytes("\n");
    }

 out.writeBytes("\n#hidden -> output weights  W["+(Zdim  )+"]["+(Ydim  )+"]:\n");
    for (int k=0;k<Zdim;k++)
    {
    for (int i=0;i<Ydim+1;i++)
       out.writeBytes(W[k][i]+" ");
    out.writeBytes("\n");
    }

   out.close();
  } catch (IOException e)
   { System.err.println("writeWeight() : EXCEPTION "+e);
   }
 }


 public void redrawCanvas(Graphics g)
 {
  System.out.println("Redraw canvas dear Erhan!");
 }


// -------------------------------------------------------------------
// AWT events

 public boolean mouseDown(Event evt, int x, int y)
  {
   Point p=cv.getLocation();
   x-=p.x;
   y-=p.y;
   Point pp=cv.which(x,y);
   if (pp!=null)
   {
    if (pp.x==0) System.out.println("input unit "+pp.y+ " selected.");
    if (pp.x==1) System.out.println("hidden unit "+pp.y+ " selected.");
    if (pp.x==2) System.out.println("output unit "+pp.y+ " selected.");
    cv.showThis(pp);
   } else
   {
    System.out.println("All network connections is shown.");
    cv.showAll();
   }
   return true;
  }
 public boolean handleEvent(Event evt)
  { if (evt.id == Event.WINDOW_DESTROY) System.exit(0);
    else if (evt.id == Event.SCROLL_ABSOLUTE ||
             evt.id == Event.SCROLL_LINE_DOWN ||
             evt.id == Event.SCROLL_LINE_UP ||
             evt.id == Event.SCROLL_PAGE_DOWN ||
             evt.id == Event.SCROLL_PAGE_UP
            )
    {
     cv.repaint();
    }
    return super.handleEvent(evt);
  }

  public boolean action(Event evt, Object arg)
  {int hit;

   if (arg.equals("Load Pattern"))
     {
      patc=0;
      clearHistory();
      readPattern(pattern_filename.getText());
      //dumpPattern();
    } 
   else if (arg.equals("Train"))
    {
    int cc=toInt(epochs.getText());
    System.out.println("* Training Epoch Started ["+cc+" steps] *");
    train(cc);
    double err=testPattern();
    System.err.println( "* Epoch Done *");
    cv.repaint();

    }
   else if (arg.equals("Make Network from Pattern"))
    {
    System.err.println("Making a new network for "+pattern_filename.getText());
    netFromPattern(pattern_filename.getText());
    //dumpPattern();
    cv.repaint();
    System.err.println("Made a new network for "+pattern_filename.getText()+" and loaded the patterns.");
    }
   else if (arg.equals("Make Network from Weight"))
    {
     System.err.println("Making a new network from the weight file "+weight_filename.getText());
     netFromWeight(weight_filename.getText());
     cv.repaint();
    }
   else if (arg.equals("Load Weight"))
    {
     totalit=0;
     System.err.println("Loading weights from "+weight_filename.getText());
     clearHistory();
     installWeight(weight_filename.getText()); 
     cv.repaint();
    }
   else if (arg.equals("Generate Weight File"))
    {
     System.err.println("Writing weight file "+weight_filename.getText());
     writeWeight(weight_filename.getText());
    }
    else if (arg.equals("Randomize Weights"))
    {
     initNet();
    }

   else if (arg.equals("Test"))
    {
    double terr=testPattern();
    }
    else if (arg.equals("QUIT"))
    {
      System.exit(0);
    }
   else return super.action(evt,arg);
   return true;
 }



}

class MyCanvas extends Canvas
{
 public MyCanvas(BP owner)
 { this.owner=owner;
   
 }


 private int[][] drawlayer(Graphics g,int x, int y, int R, int gap,double v[],int size)
 {
  int X=x;
  int Y=y;
  int offset=(wi-size*(R+gap)-gap)/2;
  if (offset<0) {offset=0; System.err.println("Overflow occured during drawlayer...");}
  int[][] p=new int[size][3]; 
  for (int i=0;i<size;i++)
  { p[i][0]=x+offset+R/2; p[i][1]=y; p[i][2]=y+R;
    g.drawArc(x+offset,y,R,R,0,360);
    g.fillArc(x+offset,y,R,R,0,(int)(0.5+360*v[i]));
    x+=R+gap;
    if ((i+1)%30==0) x+=R+gap;
    if (p[i][0]>wi-R-R/2) {x=X; y+=5*R;}
  } 
  return p;
 }

 private boolean showOK(int from,int to)
 {
  if (showexc==null) return true;
  if (drawingfrom==0)   // from=X to=Y
  {
   if (showexc.x==0 && from==showexc.y) return true; //showthis
   if (showexc.x==1 &&   to==showexc.y) return true; //showthis
  } else
  if (drawingfrom==1)   // from=Y to=Z
  {
   if (showexc.x==1 && from==showexc.y) return true; //showthis
   if (showexc.x==2 &&   to==showexc.y) return true; //showthis
  } 
  return false; //don't show
 }
 
 private void drawconn(Graphics g,int[][] X,int[][] Y,int xd,int yd,double[][] w, double thresh) 
 {
  for (int j=0;j<yd;j++)
   {
    for (int i=0;i<xd;i++)
     { 
      if (!showOK(i,j)) continue;
      double st=Math.abs(w[j][i]);
      double tt=0;
      if (st==0) continue;
      if (w[j][i]<0) g.setColor(Color.blue);
               else  g.setColor(Color.red);
      if (thresh==0) tt=Math.log(st+1)/Math.log(Math.exp(1)); 
      else tt=Math.log(st/thresh)/Math.log(Math.exp(1)); // log2 of st
      int htness=(int)(tt+.5);
      if (tt<0) htness=-1;
      if (tt>5) htness=5;

      //if (st>=thresh)
      for (int t=-1;t<htness;t++)
       g.drawLine(X[i][0]+t,X[i][1],Y[j][0]+t,Y[j][2]);

     }
     
   }
 }



 public void paint(Graphics g)
 { 
   Dimension d=getSize();
   he=d.height;
   wi=d.width;
   midx=wi/2;
   int mhe=he-40;
   inplev=20+ mhe-mhe/3;
   hidlev=20+ mhe/3;
   outlev=20+ 0;
   
   double thresh=(owner.wscMAX-owner.wsc.getValue());
   thresh/=(owner.wscMAX/owner.wscrealMAX); // 0.wscrealMAX

   if (!owner.valid)
   { g.drawString("No network created",20,he/2); return; }
   else
    g.drawString("Weight display threshold:"+thresh,20,20); 

   xrad=20; xgap=10;
   while ((xrad+xgap)*(owner.Xdim+2) > wi) { xrad--; xgap--;}
   if (xrad<=0) xrad=1; if (xgap<=0) xgap=1; 
   yrad=20; ygap=10;
   while ((yrad+ygap)*(owner.Ydim+2) > wi) { yrad--; ygap--;}
   if (yrad<=0) yrad=1; if (ygap<=0) ygap=1; 
   zrad=20; zgap=10;
   while ((zrad+zgap)*(owner.Zdim+2) > wi)
    { zrad--; zgap--;}
   if (zrad<=0) zrad=1; 
   if (zgap<=0) zgap=1; 
   int xpos[][] = drawlayer(g,20,inplev,xrad,xgap,owner.X,owner.Xdim+1); 
   int ypos[][] = drawlayer(g,20,hidlev,yrad,ygap,owner.Y,owner.Ydim+1); 
   int zpos[][] = drawlayer(g,20,outlev,zrad,zgap,owner.Z,owner.Zdim); 
   lastxpos=xpos;
   lastypos=ypos;
   lastzpos=zpos;
   drawingfrom=0;
   drawconn(g,xpos,ypos,owner.Xdim+1,owner.Ydim+1,owner.w,thresh);
   drawingfrom=1;
   drawconn(g,ypos,zpos,owner.Ydim+1,owner.Zdim,owner.W,thresh);
   drawingfrom=-1;
 }

 public Point which(int x,int y)
 {
  if (lastxpos==null) return null;
  for (int i=0;i<lastxpos.length;i++)
   { if (inunit(x,y,lastxpos[i][0],lastxpos[i][1],lastxpos[i][2]))
       return new Point(0,i);
   }
   for (int i=0;i<lastypos.length;i++)
   { if (inunit(x,y,lastypos[i][0],lastypos[i][1],lastypos[i][2]))
       return new Point(1,i);
   }
   for (int i=0;i<lastzpos.length;i++)
   { if (inunit(x,y,lastzpos[i][0],lastzpos[i][1],lastzpos[i][2]))
       return new Point(2,i);
   }

  return null;
 }

 private boolean inunit(int X,int Y,int x,int yt,int yb)
 {
  double R=(yb-yt)/2;
  double y=(yt+yb)/2.0;
  if ( (X-x)*(X-x)+(Y-y)*(Y-y) <=R*R ) return true; 
  return false;
 }

 public void showThis(Point p)
 {
  showexc=p;
  repaint();
 }
 public void showAll()
 {
  showexc=null;
  repaint();
 }
 public int wi,he,midx,xgap,ygap,zgap,xrad,yrad,zrad;
 public int inplev,hidlev,outlev;
 private BP owner;
 
 private int drawingfrom=-1;
 private Point showexc=null;
 private int[][] lastxpos=null;
 private int[][] lastypos=null;
 private int[][] lastzpos=null;
}

//---------------------------------------------
class ArrayCover 
{
 public double[] val;
 public ArrayCover()
 { val=null;
 }
 public ArrayCover(double[] a)
 {
  val=new double[a.length] ;
  for (int i=0;i<a.length;i++) val[i]=a[i];
 }
 public ArrayCover(int size,double value)
 {
  val=new double[size] ;
  for (int i=0;i<size;i++) val[i]=value;
 }
 public ArrayCover(int size)
 {
  val=new double[size] ;
 }
}

/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright
 */
 
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
