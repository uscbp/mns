/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */

import java.awt.*;
import java.lang.*;
public class Master extends Frame {
int[] map={0};
  public TextField pattern_filename,weight_filename,epochs;
  public Scrollbar wsc;
  public static final int wscMAX=1000;
  public static final double wscrealMAX=32; // max real value used by drawconn 
    BP kinematics=null;
    BP hos=null; //hand-object-shape

    public Master() {

    setTitle("Back Propagation for cascaded kinematics-hand-shape-obj relation. - Erhan Oztop may'00");
    setLayout(new BorderLayout());
    //canvas = new DrawCanvas(this);
    //canvas.setBackground(Color.white);
    //add("Center",canvas);
    Panel p1=new Panel();
    Panel p2=new Panel();
    p1.setLayout(new GridLayout(5,2));
    p2.setLayout(new GridLayout(2,3));
    p2.add(new Label("Pattern File:",Label.LEFT));
    pattern_filename=new TextField("long.pat",12);
    p2.add(new Label("Weight File:",Label.LEFT));
    weight_filename=new TextField("long.wgt",12);
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
    //cv=new MyCanvas(this);
    //add("Center",cv);
    wsc=new Scrollbar(Scrollbar.VERTICAL, wscMAX, 1, 0,wscMAX+1);
    add(wsc,"West");
    }
    public void netFromPattern(String base) {
    netFromPattern("hand_"+base,"kin_"+base);
    }
    public void netFromPattern(String hosfile,String kinfile) {
    kinematics=new BP();
    hos=new BP();
    kinematics.netFromPattern(kinfile);
    hos.netFromPattern(hosfile);
        kinematics.resize(400,300);
        kinematics.show();
        kinematics.locate(20,40);
        hos.resize(400,300);
    hos.show();
        hos.locate(20,400);
    hos.setTitle("Hand-Object relation and Hand Shape");
    kinematics.setTitle("Kinematics");

    //hos.cascadeForward(kinematics,map);
}
public void netFromWeight(String base) {
    netFromWeight("hand_"+base,"kin_"+base);
    }
public void netFromWeight(String hosfile,String kinfile) {
    kinematics=new BP();
    hos=new BP();
    kinematics.netFromWeight(kinfile);
    hos.netFromWeight(hosfile);
        kinematics.resize(400,300);
        kinematics.show();
        kinematics.locate(20,40);
        hos.resize(400,300);
    hos.show();
        hos.locate(20,400);
    hos.setTitle("Hand-Object relation and Hand Shape");
    kinematics.setTitle("Kinematics");
    int[] map={0};
    //hos.cascadeForward(kinematics,map);
}

    double[] ask(double [] inp,int size) {
    if (size!=(hos.Xdim+kinematics.Xdim-map.length))
        {System.err.println("Master:Pattern input dimensionCount does not match network input dimensionCount!!"+"size:"+size+" hosdim:"+hos.Xdim+"kindim:"+kinematics.Xdim);
        return null;
        }
    double[] kinpart=new double[kinematics.Xdim];
    double[] hospart=new double[hos.Xdim];
    for (int i=0;i<kinematics.Xdim;i++) kinpart[i]=inp[i+90];
    int k=map.length;
    for (int i=0;i<hos.Xdim+kinematics.Xdim-map.length;i++)
        if (i<90 || i>=90+60) hospart[k++]=
                      inp[i];

    return hos.ask(hospart,hos.Xdim);

    }
public static void main(String[] argv)
  {
       Master mas;
       String s=null;

       mas=new Master();
       if (argv.length==2) mas.netFromPattern(argv[0],argv[1]);
     //    else if(argv.length==1) mas.netFromPattern(argv[0],"defkin.pat");
//                      else  mas.netFromPattern("defhos.pat","defkin.pat");
       mas.resize(300,300);
       mas.show();
       mas.locate(800,40);
/*
       bp.train(1000000);
       double finalerr=bp.testPattern();
       System.err.println( "Training done. Final error on the training patterns:"+finalerr);
*/
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
    //cv.repaint();
    }
    return super.handleEvent(evt);
  }

  public boolean action(Event evt, Object arg)
  {int hit;

   if (arg.equals("Load Pattern"))
     {
 //       patc=0;
//        clearHistory();
//        readPattern(pattern_filename.getText());
//        //dumpPattern();
    }
   else if (arg.equals("Train"))
    {
//      int cc=toInt(epochs.getText());
//      System.out.println("* Training Epoch Started ["+cc+" steps] *");
//      train(cc);
//      double err=testPattern();
//      System.err.println( "* Epoch Done *");
//      cv.repaint();

    }
   else if (arg.equals("Make Network from Pattern"))
    {
    System.err.println("Making a new network for "+pattern_filename.getText());
    netFromPattern(pattern_filename.getText());
    //dumpPattern();
    //cv.repaint();
    System.err.println("Made a new network for "+pattern_filename.getText()+" and loaded the patterns.");
    }
   else if (arg.equals("Make Network from Weight"))
    {
     System.err.println("Making a new network from the weight file "+weight_filename.getText());
     netFromWeight(weight_filename.getText());
     //     cv.repaint();
    }
   else if (arg.equals("Load Weight"))
    {
//       totalit=0;
//       System.err.println("Loading weights from "+weight_filename.getText());
//       clearHistory();
//       installWeight(weight_filename.getText()); 
//       cv.repaint();
    }
   else if (arg.equals("Generate Weight File"))
    {
//       System.err.println("Writing weight file "+weight_filename.getText());
//       writeWeight(weight_filename.getText());
    }
    else if (arg.equals("Randomize Weights"))
    {
 //      initNet();
    }

   else if (arg.equals("Test"))
    {
  //    double terr=testPattern();
    }
    else if (arg.equals("QUIT"))
    {
      System.exit(0);
    }
   else return super.action(evt,arg);
   return true;
 }
} // Master
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
