import java.awt.*;
import javax.swing.*;
import java.awt.event.*;  // Needed for ActionListener

import java.io.File; //needed for FileChooser
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;

class LifeSimulationGUIDemo extends JFrame 
{
    //Data fields
    private static Colony colony = new Colony (0.5);
    private static Timer t;

    //Data fields for mouselistener
    public static Point point = new Point (0,0);
    public int  x = 0, y = 0, width = 0, height = 0, w1 = 0, h1 = 0;
    Rectangle rect = new Rectangle (); //user creates a rectangle as they click and drag
    private static boolean dragging = false;

    //======================================================== constructor
    public LifeSimulationGUIDemo ()
    {   
        // 1... Create/initialize components

        //create mousemotionlistener and mouselistener
        MovingAdapter ma = new MovingAdapter();
        addMouseMotionListener(ma);
        addMouseListener(new ClickingAdapter());

        // listener for all buttons
        BtnListener btnListener = new BtnListener (); 

        //create buttons
        JButton simulateBtn = new JButton ("Simulate");
        simulateBtn.addActionListener (btnListener);
        JButton advanceBtn = new JButton ("Advance");
        advanceBtn.addActionListener (btnListener);
        JButton populateBtn = new JButton ("Populate");
        populateBtn.addActionListener (btnListener);
        JButton eradicateBtn = new JButton ("Eradicate");
        eradicateBtn.addActionListener (btnListener);
        JButton saveBtn = new JButton ("Save");
        saveBtn.addActionListener (btnListener);
        JButton loadBtn = new JButton ("Load");
        loadBtn.addActionListener (btnListener);
        JButton helpBtn = new JButton ("Help");
        helpBtn.addActionListener (btnListener);

        // 2... Create content pane, set layout
        JPanel content = new JPanel ();        // Create a content pane
        content.setLayout (new BorderLayout ()); // Use BorderLayout for panel
        JPanel north = new JPanel ();
        north.setLayout (new FlowLayout ()); // Use FlowLayout for input area

        DrawArea board = new DrawArea (500, 500);

        // 3... Add the components to the input area.
        north.add (simulateBtn);
        north.add (populateBtn);
        north.add (eradicateBtn);
        north.add (saveBtn);
        north.add (loadBtn);
        north.add (helpBtn);
        content.add (north, "North"); // Input area
        content.add (board, "South"); // Output area

        // 4... Set this window's attributes.
        setContentPane (content);
        pack ();
        setTitle ("Life Simulation");
        setSize (510, 570);
        setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo (null);           // Center window.
    }

    class BtnListener implements ActionListener 
    {
        public void actionPerformed (ActionEvent e)
        {
            //coordinates of where user clicked and dragged
            int nlength, mlength;
            point.x = Math.round((y + 60)/5 - 12);
            point.y = Math.round((x + 10)/5 - 2);
            nlength = Math.round(h1/5 - 10);
            mlength = Math.round(w1/5);

            if (e.getActionCommand ().equals ("Simulate")) 
            {
                Movement moveColony = new Movement (); // ActionListener for Timer
                t = new Timer (200, moveColony); // set up Movement to run every 200 milliseconds
                t.start (); // start simulation
            }
            else if (e.getActionCommand ().equals ("Populate")) 
            {
                dragging = false; //closes rectangle
                colony.populate (point, nlength, mlength); //populates an area
            }
            else if (e.getActionCommand ().equals ("Eradicate")) 
            {
                dragging = false; //closes rectangle
                colony.eradicate (point, nlength, mlength); //eradicates life in an area
            }
            else if (e.getActionCommand ().equals ("Save"))
                colony.save(); //saves a file
            else if (e.getActionCommand ().equals ("Load"))
                colony.load (); //opens a file
            else if (e.getActionCommand ().equals ("Help")) //gives a set of instructions to user 
                JOptionPane.showMessageDialog(null, "Instructions \n\nTo start the simulation, click Simulate. \nTo populate/eradicate areas, click and drag the mouse to the \nright and down, and then click Populate/Eradicate.\nSave/Load allows you to save or open a text file in plaintext format.");

            repaint ();            // refresh display of colony
        }
    }

    class MovingAdapter extends MouseAdapter 
    {
        public void mouseDragged (MouseEvent e) 
        {
            //get coordinates of current mouse location
            w1 = e.getX(); 
            h1 = e.getY(); 
            //find width and height of the rectangle user has created
            width = Math.abs(w1 - x);
            height = Math.abs(h1 - y - 50);

            if (dragging)
                repaint (); //update rectangle display
        }
    }

    class ClickingAdapter extends MouseAdapter
    {
        public void mousePressed(MouseEvent e) 
        {
            dragging = true; 
            //get coordinates of point clicked
            x = e.getX() - 10; 
            y = e.getY() - 60;
        }

        public void mouseClicked(MouseEvent e) 
        {   
            dragging = false; // destroys the rectangle, user has clicked elsewhere
            repaint (); //update display
        }
    }

    class DrawArea extends JPanel
    {
        public DrawArea (int width, int height)
        {
            this.setPreferredSize (new Dimension (width, height)); // size
        }

        public void paintComponent (Graphics g)
        {
            colony.show (g); // display current state of colony

            if (dragging)
            {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(Color.blue); //set colour to blue

                //make a semi-transparent rectangle
                float alpha = 2 * 0.1f;
                AlphaComposite alcom = AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, alpha);
                g2d.setComposite(alcom);
                g2d.fillRect(x, y, width, height);

                g2d.dispose();
            }

        }
    }

    class Movement implements ActionListener
    {
        public void actionPerformed (ActionEvent event)
        {
            colony.advance (); // advance to the next time step
            repaint (); // refresh 
        }
    }

    //======================================================== method main
    public static void main (String[] args)
    {
        LifeSimulationGUIDemo window = new LifeSimulationGUIDemo ();
        window.setVisible (true);
    }
}

class Colony 
{
    private boolean grid[] []; //new boolean grid array
    public Colony (double density) //default constructor to initialize values in array
    {     
        grid = new boolean [100] [100];
        for (int row = 0 ; row < grid.length ; row++)
            for (int col = 0 ; col < grid [0].length ; col++)
                grid [row] [col] = Math.random () < density;
    }

    public void show (Graphics g)
    {
        for (int row = 0 ; row < grid.length ; row++)
            for (int col = 0 ; col < grid [0].length ; col++)
            {
                if (grid [row] [col]) // life
                    g.setColor (Color.black);
                else //no life
                    g.setColor (Color.white);
                g.fillRect (col * 5 + 2, row * 5 + 2, 5, 5); // draw life form
            }
    }

    public boolean live (int row, int col) //determines if cells live or die
    {
        //Variable Declaration
        boolean state;
        int n, m, counter = 0;

        //check area around cell
        for (int x = 0; x < 9; x ++)
        {
            n = row - 1 + x%3;
            m = col - 1 + x/3;

            if (n >= 0 && n < grid.length && m >= 0 && m < grid [0].length)
                if (grid[n][m])
                    counter ++;
        }

        //live cell continues to live if it has 2 or 3 live neighbours
        //dead cells become alive if they have 3 neighbours
        if (counter == 3)
            state = true;
        else if (counter == 4 && grid [row][col] == true)
            state = true;
        else
            state = false;

        return state; //return if cell is dead/alive
    }

    public void advance () //updates grid to reflect new dead/alive cells
    {
        //Variable declaration
        boolean [] [] copy = new boolean [100] [100]; //create a copy array
        boolean state; //placeholder

        for (int row = 0 ; row < grid.length ; row++)
            for (int col = 0 ; col < grid [0].length ; col++)
            {
                //change values in copy array
                state = live (row, col);
                if (state)
                    copy [row][col] = true;
                else
                    copy [row][col] = false;
            }

        grid = copy; //set grid equal to copy array
    }

    public void populate (Point p, int width, int height) //populate a certain area
    {
        int num; //variable declaration
        for (int row = p.x; row < width; row++)
            if (row > 0 && row < grid.length)
                for (int col = p.y; col < height; col++)
                    if (col > 0 && col < grid[0].length)
                    {
                        num = (int)(Math.random()*10);
                        if (num < 9) //successful 90% of the time
                            grid[row][col] = true; //cells become live
                    }
    }

    public void eradicate (Point p, int width, int height) //eradicate a certain area
    {
        int num; //variable declaration
        for (int row = p.x; row < width; row++)
            if (row > 0 && row < grid.length)
                for (int col = p.y; col < height; col++)
                    if (col > 0 && col < grid[0].length)
                    {
                        num = (int)(Math.random()*10);
                        if (num < 9) //successful 90% of time
                            grid[row][col] = false; //ceels die
                    }
    }

    public void save () //saves information to text file
    {
        //Variable declaration
        int num;
        String str = "";

        JFileChooser chooser = new JFileChooser(); //new filechooser 
        int retrival = chooser.showSaveDialog(null);
        if (retrival == JFileChooser.APPROVE_OPTION) {
            try {
                FileWriter fw = new FileWriter(chooser.getSelectedFile()+".txt"); //new filewriter
                for (int row = 0 ; row < grid.length ; row++)
                    for (int col = 0 ; col < grid [0].length ; col++)
                    {
                        //plaintext format: O = alive and . = dead
                        if (grid [row] [col])
                            str += "O";
                        else
                            str += ".";
                    }
                fw.write(str);
                fw.close (); //closes filewriter
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void load () //loads information from text file
    { 
        //Variable Declaration
        int x = 0;
        String line, contents = "";

        JFileChooser file = new JFileChooser ();
        //filter the files
        file.setFileFilter(new FileNameExtensionFilter("Text files","txt"));
        int result = file.showOpenDialog(null);
        //if the user click on save in Jfilechooser
        if(result == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                File f = file.getSelectedFile();
                FileReader fr = null;
                try
                {
                    fr = new FileReader (f); //new file reader to read file
                    BufferedReader filein = new BufferedReader (fr);
                    while ((line = filein.readLine ()) != null) // reads line if file has not ended
                    {
                        while (line.length() < grid.length)
                            line += "."; //adds dead cells to accomodate for the shorter line
                        contents += line; //adds line to content string
                    }

                    filein.close (); // close file

                    for (int row = 0 ; row < grid.length ; row++)
                        for (int col = 0 ; col < grid [0].length ; col++)
                        {
                            if (x < contents.length ())
                            {
                                //array values depend on values in content string
                                if (contents.charAt(x) == 'O') //alive
                                    grid [row] [col] = true;
                                if (contents.charAt(x) == '.') //dead
                                    grid [row] [col] = false;
                                x++;
                            }
                            else
                                grid [row] [col] = false; //if string ends, everything else is dead
                        }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    if (fr != null)
                    {
                        fr.close(); //closes filereader
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
