/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */

public class HandBP extends BP {
    
    public HandBP() {}

      public HandBP(String s) {super(s);}
	public void initNet()
 {
     System.out.println("Overwritten initNet talkinf....."); 
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


       //ERH if (((j%N)/R)!=(i%M) && j<210) w[i][j]=0;

       }

    for (int k=0;k<Zdim  ;k++)
    for (int i=0;i<Ydim+1;i++)
      {
       W[k][i]=(Math.random()-0.5)*0.1;
       _dW[k][i]=0;
       //ERH if (i!=Ydim && i/5!=k) W[k][i]=0;
      }
 }

  public void forward()
  {
      for (int i=210;i<Xdim;i++) X[i]=0; //ignore these inputs 
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
	

	//ERH if (((j%N)/R)!=(i%M) && j<210) dw[i][j]=0;
      }

    for (int k=0;k<Zdim  ;k++)
    for (int i=0;i<Ydim+1;i++) 
     {
      dW[k][i]=eta*eZ[k]*Y[i] + beta*_dW[k][i];
      _dW[k][i]=dW[k][i];
      
      if (i!=Ydim && i/5==k) W[k][i]=1;
       //ERH if (i!=Ydim && i/5!=k) { dW[k][i]=0; _dW[k][i]=0;}
     }

   addto(W,Zdim,Ydim+1,dW);
   addto(w,Ydim+1,Xdim+1,dw);
   double sum=0;

   for (int i=0;i<Ydim+1;i++)
       for (int j=210;j<Xdim+1;j++) {
	   //double eps=0.001*eta/Elib.sqr(1+w[i][j]*w[i][j]) ;
	   double eps=0.01; //*eta;
	   sum+=Math.abs(w[i][j]);
	   w[i][j]*=(1-eps);
       }
   //System.out.println("ABS(w[][]) avg:"+sum/Ydim/Xdim);
  /* 
   for (int k=0;k<Zdim  ;k++)
       for (int i=0;i<Ydim+1;i++) {
	   double eps=0.0001*eta/Elib.sqr(1+W[k][i]*W[k][i]) ;
	   //double eps=0.1*eta;
	   W[k][i]*=(1-eps);
       }
  */
   
    } 
   return err/step;
  }
    
      public static void main(String[] argv)
  {    
       HandBP bp;
       System.out.println("HEYYYYYYYYYYYYYYYYYYYYY");
       String s=null;
       if (argv.length>0) bp=new HandBP(argv[0]);
                    else  bp=new HandBP();
       bp.resize(400,300);
       bp.show();
       bp.locate(200,400);
/*
       bp.train(1000000);
       double finalerr=bp.testPattern();
       System.err.println( "Training done. Final error on the training patterns:"+finalerr);
*/
       }

} // HandBP
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
