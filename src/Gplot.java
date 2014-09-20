
import java.io.*;
import java.lang.*;

/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */

class Gplot //extends Thread
{

    static int XGAP=5;
    static int YGAP=20;
    static int in_wi=255-XGAP;
    static int in_he=255-YGAP;
    static int in_xpos=0;
    static int in_ypos=0;

    static int xend=1280;
    static int yend=1024-60;

    static int wi=in_wi;
    static int he=in_he;
    static int xpos=in_xpos;
    static int ypos=in_xpos;
    static boolean spread_window=false;

    static final String persist="-persist";
    Runtime r=Runtime.getRuntime();
    Process p=null;
    DataOutputStream pout;
    OutputStream rawpout;
    DataInputStream pin;
    InputStream rawpin;

    /** (x,y) first Gplot top-left coord. (w,h) is the size of the plot and
     (xen,yen) defines the end of the gnuplot usuable area in the screen */
    static public void resetGeom(int x,int y, int xen, int yen, int w, int h) {
        in_wi=w;
        in_he=h;
        in_xpos=x;
        in_ypos=y;
        xend=xen;
        yend=yen;
        Gplot.resetGeom();
    }
    static public void resetGeom(int x,int y, int w, int h) {
        in_wi=w;
        in_he=h;
        in_xpos=x;
        in_ypos=y;
        Gplot.resetGeom();
    }

    static public void resetGeom(int x,int y) {
        in_xpos=x;
        in_ypos=y;
    }

    static public void resetGeom() {
        if (in_wi>640) in_wi=640;
        if (in_he>480) in_he=640;
        wi=in_wi;
        he=in_he;
        xpos=in_xpos;
        ypos=in_ypos;
    }

    static public void spreadWindow(boolean k) {
        spread_window=k;
    }

    static public void spreadWindow(int k) {
        spread_window = k==0?false:true;
    }

    static String usercommand="";
    static public void setUserCommand(String s) {
        usercommand=s;
    }

    public Gplot()
    {
        construct();
    }

    private void construct()
    {
        String os = System.getProperty("os.name").toString();
        String gplotcmd = "";
        if(os.equals("Linux"))
            gplotcmd = "gnuplot "+persist+" -geometry "+wi+"x"+he+"+"+xpos+"+"+ypos;
        else
            gplotcmd = "c:/gnuplot/bin/pgnuplot.exe "+persist;
        try
        {
            p=r.exec(gplotcmd);
        }
        catch (Exception e)
        {
            System.err.println("Error running gnuplot");
        }
        pin =new DataInputStream(rawpin=p.getInputStream());
        pout=new DataOutputStream(rawpout=p.getOutputStream());
        if (spread_window)
        {
            xpos += wi + XGAP ;
            if (xpos>xend-wi)
            {
                xpos=in_xpos;
                ypos+= he+ YGAP;
                if (ypos>yend-he)
                {
                    in_xpos += 5;
                    in_ypos += 5;
                    Gplot.resetGeom();
                }
            }
        }

    }

    private void construct(String s)
    {
        construct();
        send(s);
    }
/*
int TIME=5;
public void run()
{
try {
sleep(TIME);
} catch (Exception e) {};
System.out.println("Killing after "+TIME+" waits.");
p.destroy();
destroy();
}

public void startTimer(int k)
{
TIME=k;
start();
}

*/
    Gplot(String s)
    {
        construct(s);
    }

    Gplot(String s,String gifname)
    {
        if (gifname==null)
            construct(s);
        else
        {
            try
            {
                String cmd = "fakeout gnuplot "+gifname+/*persist+*/" -geometry "+wi+"x"+he+"+"+xpos+"+"+ypos;
                p=r.exec(cmd);
            }
            catch (Exception e)
            {
                System.err.println("Error running gnuplot");
            }
            pin =new DataInputStream(rawpin=p.getInputStream());
            pout=new DataOutputStream(rawpout=p.getOutputStream());
            send("set terminal gif\n"+s);
            try
            {
                p.waitFor();
            }
            catch (Exception e)
            {
                System.err.println("Exception occured waiting for the external process!");
            }
        }
    }

    Gplot (double[] v)
    {
        this(v,v.length);
    }

    Gplot (double[] v, int N)
    {
        //this();
        String name="/tmp/GP."+System.currentTimeMillis();
        Elib.array2file(v,N,name);
        construct("plot \""+name+
                //"\" with boxes");
                "\" with linespoints\n");
        //send();
    }

    Gplot (double [][] v,String com)
    {
        this(v,v.length,v[0].length,"/tmp/WEIGHT.",com);
    }

    Gplot  (double[][] v, int N,int M)
    {
        this(v,N,M,null);
    }

    Gplot (Point3d[] v, String suffix, String extracommand)
    {
        this();
        if (suffix==null) suffix="/tmp/GP.";
        String name=suffix; //+System.currentTimeMillis();
        Elib.point3d2file(v,v.length, name);
        //send("set zrange [0:0.025];set hidden3d; splot \""+dimensionName+
        //"\" matrix with lines");
        send(usercommand+extracommand+"; set hidden3d; splot \""+name+
                "\" with lines\n");
        if (HV.DLEV>0)
            System.out.println(usercommand+extracommand+"; set hidden3d; splot \""+name+
                    "\" with lines");

    }

    Gplot (Point3d[] v, String suffix)
    {
        this(v,suffix,"");
    }

    Gplot (double[][] v, int N,int M, String suffix) {
        this(v,N,M,suffix,"");
    }

    Gplot (double[][] v, int N,int M, String suffix,String extracommand)
    {
        if (suffix==null) suffix="/tmp/GP.";
        String name=suffix+System.currentTimeMillis();
        double[][] Q=transpose(v);
        Elib.array2file(Q,M,N,name);
        //send("set zrange [0:0.025];set hidden3d; splot \""+dimensionName+
        //"\" matrix with lines");
        String s="";
        if(extracommand.length()>0)
            s += usercommand+extracommand+"; ";
        construct(s+"set hidden3d; splot \""+name+
                "\" matrix with lines\n");
    }

    // gif writing is not working. Never worked actually
    Gplot (double[][] v, int N,int M,String gifname, int ign)
    {
        System.out.println("HEYYYYYYYYY+++++++++++");
        try
        {
            String s = "gnuplot "+/*persist+*/" -geometry "+wi+"x"+he+"+"+xpos+"+"+ypos+" >"+gifname;
            p=r.exec(s);
        }
        catch (Exception e)
        {
            System.err.println("Error running gnuplot");
        }
        pin =new DataInputStream(rawpin=p.getInputStream());
        pout=new DataOutputStream(rawpout=p.getOutputStream());

        String name="/tmp/GP."+System.currentTimeMillis();
        Elib.array2file(v,N,M,name);
        send("set terminal gif\n");
        send("plot \""+name+"\" with linespoints\n");
    }


    public void waitFinish()
    {
        try
        {
            p.waitFor();
        }
        catch (Exception e)
        {
            System.err.println("Error during waitFinsh ");
        }
    }

    public void send(String s)
    {
        try
        {
            if(!s.endsWith("\n"))
                s += "\n";
            pout.writeBytes(s);
            pout.flush();
            rawpout.flush();
            //rawpout.close();
            //pout=new DataOutputStream(rawpout=p.getOutputStream());
        }
        catch (Exception e)
        {
            System.err.println("Error during issuing command");
        }
    }

    public void get()
    {
        byte[] b=new byte[20];
        String s="fake";

        try
        {
            while (s!=null)
            {
                s=pin.readLine();
                if (s!=null) System.out.println(s);
            }
        }
        catch (Exception e)
        {
            System.err.println("Error during issuing command");
        }
    }


    public void close()
    {
        try
        {
            pin.close();
            pout.close();
        }
        catch (Exception e)
        {
            System.err.println("Error during closse command");
        }
    }

    public static void main(String[] args)
    {
        double[] x={1,2,3,4,5,6,7,8,9,8,7,6,5,4,3,2,1};
        //Gplot gp=new Gplot("plot x");
        Gplot gp=new Gplot(x);
        //gp.startTimer(10);
    }

    static double[][] transpose(double[][] M)
    {
        int m=M.length;
        int n=M[0].length;
        double [][] Q=new double[n][m];
        for (int i=0;i<m;i++)
            for (int j=0;j<n;j++)
                Q[j][i]=M[i][j];
        return Q;
    }
}

























/*
*
* Erhan Oztop, 2000-2002  <br>
* Source code by Erhan Oztop (erhan@atr.co.jp) <br>
* Copyright August 2002 under <br>
* University of Southern California Ph.D. publication copyright <br>
*/

