 
import java.io.*;
import java.net.*;
import java.applet.*;
import java.util.*;
import java.awt.*;
import java.lang.*;
/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
/*--------------- Resource --------------------*/
public class Resource 
{
 static private Vector vars;
 static public URL docbase=null;
static public void read()
 { read("default.res");
 
 }

static public DataInputStream openURLfile(String name)
 {
  InputStream is;
  DataInputStream dis=null;
  BufferedInputStream bis;
  URL url=null;
  try{
   url=new URL(docbase,name);
  } catch (MalformedURLException e)
  { System.err.println("Bad URL  address:"+url);}

  try {
  is = url.openStream();
  bis = new BufferedInputStream(is);
  dis = new DataInputStream(bis);
  } catch (IOException e)
  { System.err.println("File open error:"+e.getMessage()); }
  return dis; 
 }

static public DataInputStream openFile(String fn) throws IOException
 { if (docbase==null) 
   { DataInputStream in = new DataInputStream(new FileInputStream(fn));
     return in;
   }
   return openURLfile(fn);
 }
    
static public void read(String fn)
 {
   int tc;
   String[] t=new String[2] ;
   String s;
   vars=new Vector(40);
   try {
   DataInputStream in = openFile(fn);
   while (null!=(s=in.readLine()))
   {
    if (s.equals("")) continue;
    if (s.charAt(0) == '#') continue;
    StringTokenizer st= new StringTokenizer(s," ");
    tc=0;
    while (st.hasMoreTokens())
    {
     if (tc>=2) break;
     t[tc++] = st.nextToken();
    }
    if (tc==2)
    {
    System.out.println("RESOURCE: "+t[0]+" = "+t[1]);
    vars.addElement(new ResourceVar(t[0],t[1]));
    }
   }
   in.close();
  } catch (IOException e)
   { System.err.println("NBench.update_nlist : EXCEPTION "+e);
   }

 }

static public String getString(String s)
 { ResourceVar r;
   Enumeration e=vars.elements();
   while ( e.hasMoreElements())
   { r=(ResourceVar)e.nextElement();
     if (s.equals(r.name)) return r.value;
   }
   return null;
 }


static public int getInt(String s)
 { ResourceVar r;
   Enumeration e=vars.elements();
   while ( e.hasMoreElements())
   { r=(ResourceVar)e.nextElement();
     if (s.equals(r.name)) return toInt(r.value);
   }
   return 0;
 }

    static public double get(String s) {
	return getDouble(s);
    }

static public double getDouble(String s)
 { ResourceVar r;
   for (Enumeration e=vars.elements(); e.hasMoreElements(); )
   { r=(ResourceVar)e.nextElement();
     if (s.equals(r.name)) return toDouble(r.value);
   }
   return 0.0;
 }


static   public double toDouble(String s)
  {
   char c;
   String t="";
   for (int i=0;i<s.length();i++)
   { c=s.charAt(i);
     if (c>' ') t+=c;
   }
   return (Double.valueOf(t)).doubleValue();
  }


 static  public int toInt(String s)
  {
   return (int)(toDouble(s)+0.5);
  }

}

class ResourceVar 
{
 public String name,value;
 public ResourceVar(String nm, String v)
 {
  name=nm;
  value=v;
 }
}

/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
