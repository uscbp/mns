
import java.awt.*;
import java.net.*;
import java.util.*;
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
 *
 * <b>This class file is a collection of purpose utility methods. </b>
 */

class Elib
{
    /** returns a Color corresponding to i. -limited use check code-*/
    static public Color Ecolor(int i)
    {
        if (i==0) return Color.black;
        if (i==1) return Color.white;
        if (i==2) return Color.red;
        if (i==3) return Color.green;
        if (i==4) return Color.blue;
        if (i==5) return Color.magenta;
        if (i==6) return Color.yellow;
        if (i==7) return Color.cyan;
        if (i==8) return Color.gray;
        if (i==9) return Color.lightGray;
        return Color.blue;
    }

    /** Returns the double value in the given string*/
    static  public double toDouble(String s)
    {
        Double d=new Double(s);
        return d.doubleValue();
    }

    /** Returns the int value in the given string*/
    static public int toInt(String s)
    {
        return (int)(toDouble(s)+0.5);
    }

    /** Returns a truncated vecsion of x so that it will have less decimal
     * points. If scale=1e3, it should return a number with less than or equal
     * to 3 decimal digits. -limited use- -may be buggy-
     */
    static public double nice(double x,double scale)
    {
        long l=(long)(scale*x+0.5);
        return l/scale;
    }

    /** Returns a truncated vecsion of the double given in the string so that
     *  it will have less decimal  points. If scale=1e3, it should return a
     *  number with less than or equal
     * to 3 decimal digits. -limited use- -may be buggy-
     */
    static public double nice(String s,double scale)
    {
        return nice(toDouble(s),scale);
    }

    /** Returns a truncated version of the double given  so that
     *  it will have less decimal  points as a string. If scale=1e3,
     *  it should return a
     *  number with less than or equal  to 3 decimal digits.
     *  The last argument specifies the box length that the number should span
     * -limited use- -may be buggy-
     */
    static public String snice(double x,double scale,int pos)
    {
        double y=nice(x,scale);
        String s=y+"";
        int l=s.length();
        for (int i=0;i<(pos-l);i++)
            s=s+" ";
        return s;
    }

    /** Returns a truncated version of the double given in the string so that
     *  it will have less decimal  points as a string. If scale=1e3,
     *  it should return a
     *  number with less than or equal  to 3 decimal digits.
     *  The last argument specifies the box length that the number should span
     * -limited use- -may be buggy-
     */
    static public String snice(String s,double scale,int pos)
    {
        return snice(toDouble(s),scale,pos);
    }

    /** Returns x*x */
    static public double sqr(double x)
    {
        return x*x;
    }

    /** Returns x*x*x */
    static public double cube(double x)
    {
        return x*x*x;
    }

    /** Returns x*x*SGN(x). Where SGN(x)=0 at 0, -1 for x<0 and +1 else.*/
    static public double negsqrt(double x)
    {
        return (x<0)?-Math.sqrt(-x):Math.sqrt(x);
    }

    /** Return 0 for x=0, -1 for x<0 and +1 else */
    static public double sgn(double x)
    {
        if (x<0)
            return -1;
        if (x>0)
            return  1;
        return 0;
    }

    /** Returns (int) absolute value of x*/
    static public int abs(int x)
    {
        if (x<0)
            return -x;
        return x;
    }

    /** Returns the distance between (x0,y0), (x1,y1) */
    static public double dist(double x0,double y0,double x1,double y1)
    {
        return Math.sqrt((x0-x1)*(x0-x1)+(y0-y1)*(y0-y1));
    }

    /** makes a dirty and fast number check based on 1st char. If it returns
     false it is a non-number for sure but contrary is not true.
     */
    public boolean easy_num(String u)
    {
        if ( (u.charAt(0)>='0' && u.charAt(0)<='9') || u.charAt(0)=='-' || u.charAt(0)=='+')
            return true;
        return false;
    }

    static public DataInputStream openURLfile(URL base,String name)
    {
        InputStream is;
        DataInputStream dis=null;
        BufferedInputStream bis;
        if (base==null)
        {
            try
            {
                return openfileREAD(name);
            }
            catch(IOException e)
            {
                System.out.println("Cannot open"+name);
            }
        }
        URL url=null;
        try
        {
            System.out.println("==== base:"+base+"  dimensionName:"+name);
            url=new URL(base,name);
        }
        catch (MalformedURLException e)
        {
            System.err.println("Bad URL  address:"+url);
        }

        try
        {
            is = url.openStream();
            bis = new BufferedInputStream(is);
            dis = new DataInputStream(bis);
        }
        catch (IOException e)
        {
            System.err.println("File open error:"+e.getMessage());
        }
        return dis;
    }

    /**  open file for read. Should be moved to Elib. */
    static public DataInputStream openfileREAD(String fn) throws IOException
    {
        DataInputStream in = new DataInputStream(new FileInputStream(fn));
        return in;
    }

    /**  open file for write. Should be moved to Elib. */
    static public DataOutputStream openfileWRITE(String fn) throws IOException
    {
        DataOutputStream out = new DataOutputStream(new FileOutputStream(fn));
        return out;
    }

    /** Writes a point3d array to a file */
    static boolean point3d2file(Point3d A[],int N, String fn)
    {
        try
        {
            DataOutputStream patout = Elib.openfileWRITE(fn);
            for (int i=0;i<N;i++)
            {
                if (A[i]==null)
                    patout.writeBytes("\n");
                else
                    patout.writeBytes(A[i].x+" "+A[i].y+" "+A[i].z+"\n");
            }
            patout.close();
        }
        catch (IOException e)
        {
            System.err.println("array2File : EXCEPTION :"+e+"\n while creating:"+fn);
            return false;
        }
        return true;
    }

    /** Writes an array to a file */
    static boolean array2file(double A[],int N, String fn)
    {
        try
        {
            DataOutputStream patout = Elib.openfileWRITE(fn);
            for (int i=0;i<N;i++)
                patout.writeBytes(A[i]+"\n");
            patout.close();
        }
        catch (IOException e)
        {
            System.err.println("array2File : EXCEPTION :"+e+"\n while creating:"+fn);
            return false;
        }

        return true;
    }

    /** Writes an 2darray to a file */
    static boolean array2file(double A[][],int N,int M, String fn)
    {
        try
        {
            DataOutputStream patout = Elib.openfileWRITE(fn);
            for (int i=0;i<N;i++)
            {
                for (int j=0;j<M;j++)
                    patout.writeBytes(A[i][j]+" ");
                patout.writeBytes("\n");
            }
            patout.close();
        }
        catch (IOException e)
        { System.err.println("array2File : EXCEPTION :"+e);
            return false;
        }
        return true;
    }
    /** Writes an 2darray to a file */
    static boolean array2file(double A[][],String fn,String comment)
    {
        int N=A.length;
        int M=A[0].length;
        try
        {
            DataOutputStream patout = Elib.openfileWRITE(fn);
            if (comment!=null) patout.writeBytes("#"+comment);
            for (int i=0;i<N;i++)
            {
                for (int j=0;j<M;j++)
                    patout.writeBytes(A[i][j]+" ");
                patout.writeBytes("\n");
            }
            patout.close();
        }
        catch (IOException e)
        {
            System.err.println("array2File : EXCEPTION :"+e);
            return false;
        }
        return true;
    }

    /** Reads a 2darray from a file */
    static double[][] file2array(String fn)
    {
        String s;
        Vector rows=new Vector(40);
        Vector elems;
        double[][] M;
        try
        {
            DataInputStream mat = Elib.openfileREAD(fn);
            int rowc=0; int colc=0;
            while (null!=(s=mat.readLine()))
            {
                if (s.equals(""))
                    continue;
                if (s.charAt(0) == '#')
                {
                    System.out.println(s);
                    continue;
                }
                rowc++;
                StringTokenizer st= new StringTokenizer(s," ");
                elems=new Vector(20); colc=0;
                while (st.hasMoreTokens())
                {
                    colc++;
                    elems.addElement(new Double(toDouble(st.nextToken())));
                }
                rows.addElement(elems);
            }
            M=new double[rowc][colc];
            Enumeration ro=rows.elements();
            for (int i=0;i<rowc;i++)
            {
                Vector v=(Vector)ro.nextElement();
                Enumeration co=v.elements();
                for (int j=0;j<colc;j++)
                    M[i][j]=((Double)co.nextElement()).doubleValue();
            }
            mat.close();
        }
        catch (IOException e)
        {
            System.err.println("File2Array : EXCEPTION :"+e);
            return null;
        }
        return M;
    }

// x in the range 0..xmax will be log scaled to 0..1 with base steepness of the
// log curve. Large value means almost linear (1 is large!). Choose something
// like 0.1 or 0.01 for the base
    static double logscale(double x, double xmax,double base)
    {
        return (Math.log(base+x)-Math.log(base))/
                (Math.log(base+xmax)-Math.log(base));
    }

    /** input us a ch delimited string. The output is the double[] representation */
    static double[] str2array(String s, String ch)
    {
        StringTokenizer st= new StringTokenizer(s,ch);
        //let's count the number of tokens
        int c=0;
        while (st.hasMoreTokens())
        {
            String u = st.nextToken();
            c++;
        }
        double[] ret=new double[c];
        st= new StringTokenizer(s,ch);
        c=0;
        while (st.hasMoreTokens())
        {
            ret[c++]=Elib.toDouble(st.nextToken());
        }
        return ret;
    }

    static String arrayInfo(double[][] A)
    {
        return (A.length+"x"+A[0].length);
    }
}
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
