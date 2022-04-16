package nn.DenisAleksandrov.dataHarvester;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**********************************************************************
 * Using key/button to move a rectangle.
 ***********************************************************************/

@SuppressWarnings("serial")
public class CGMoveALine extends JFrame {
    // Define constants
    public static final int CANVAS_WIDTH = 500;
    public static final int CANVAS_HEIGHT = 500;
    public static final int DIAGONAL = (int) Math.hypot(CANVAS_HEIGHT, CANVAS_WIDTH);
    public static final Color LINE_COLOR = Color.BLACK;
    public static final Color WALL_COLOR = Color.BLUE;
    public static final Color CANVAS_BACKGROUND = Color.CYAN;
    public static final int STEP = 5;

    // parallel sensor world
    private char world[][];
    private boolean firstTime = true;

    private int x1 = 250; // Initial robot location
    private int y1 = 495; // Initial robot location
    private int x2 = 5; // robot width
    private int y2 = 5; // robot height
    private char direction = 'n'; // n-north(up), s-south, e-east(right), w-west
    private int sensor_forward_distance = y1;
    private int sensor_back_distance = CANVAS_HEIGHT - y1;
    private int sensor_right_distance = CANVAS_WIDTH - x1;
    private int sensor_left_distance = x1;
    private int sensor_RD_distance;
    private int sensor_LD_distance;

    // Array where the first four elements are LD, F, RD, and B sensor data
    // and the last twelve are FAR, NEAR, or BLOCKING boolean values
    // for each of the first four data units
    // FAR: data >= 20
    // NEAR: 5 < data < 20
    // BLOCKING: data <= 5
    int[] trainArray = new int[16];
    //List<>[] trainingSet = new ArrayList<>();

    // The custom drawing canvas (extends JPanel)
    private DrawCanvas canvas;

    // CSV File
    String filename = "data.csv";
    String filename1 = "along_left_side.txt";
    File f;
    File f1;
    FileWriter w;
    //ObjectOutputStream fw;
    FileWriter fw;

    // Constructor to set up the GUI components and event handlers
    public CGMoveALine() {
        int x,y;

        try {
            f = new File(filename);
            w = new FileWriter(f);
            f1 = new File(filename1);
            //fw = new ObjectOutputStream(new FileOutputStream(f1));
            fw = new FileWriter(f1);
            w.write("x, y, direction, F, Back, LD, RD, Battery, S, Ltouch, Rtouch\n");
            w.flush();
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }

        //
        // Define world
        //

        world = new char[CANVAS_WIDTH][CANVAS_HEIGHT];
        for(x=0;x<CANVAS_WIDTH;x++)
            for(y=0;y<CANVAS_HEIGHT;y++)
                world[x][y]=' '; // empty spaces

        //
        // Set up a panel for the buttons
        //

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnLeft = new JButton("Move Left ");
        btnPanel.add(btnLeft);
        btnLeft.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                x1 -= 10;
                canvas.repaint();
                requestFocus(); // change the focus to JFrame to receive KeyEvent
            }
        });
        JButton btnRight = new JButton("Move Right");
        btnPanel.add(btnRight);
        btnRight.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                x1 += 10;
                canvas.repaint();
                requestFocus(); // change the focus to JFrame to receive KeyEvent
            }
        });

        //
        // Set up a drawing JPanel
        //

        canvas = new DrawCanvas();
        canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

        //
        // Add both panels to this JFrame's content-pane
        //

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(canvas, BorderLayout.CENTER);
        cp.add(btnPanel, BorderLayout.SOUTH);

        //
        // Keyboard listener for JFrame KeyEvent
        //

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                switch(evt.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        if (x1 - 10 > 0) {
                            if (sensor_forward_distance > 5) x1 -= STEP;
                            direction = 'w';
                            repaint();
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (x1 + 10 < CANVAS_WIDTH) {
                            if (sensor_forward_distance > 5) x1 += STEP;
                            direction = 'e';
                            repaint();
                        }
                        break;
                    case KeyEvent.VK_UP:
                        if (y1 - 10 > 0) {
                            if (sensor_forward_distance > 5) y1 -= STEP;
                            direction = 'n';
                            repaint();
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (y1 + 10 < CANVAS_HEIGHT) {
                            if (sensor_forward_distance > 5) y1 += STEP;
                            direction = 's';
                            repaint();
                        }
                        break;
                }
            }
        });

        //
        // JFrame housekeeping before displaying
        //

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Handle the CLOSE button
        setTitle("Move a Robot");
        pack();           // pack all the components in the JFrame
        setVisible(true); // show it
        requestFocus();   // set the focus to JFrame to receive KeyEvent
    }

    /****************************************************************
     * Draw the world
     ****************************************************************/

    class DrawCanvas extends JPanel {

        private void addWallFirstTime(Graphics g, boolean notBeenDone, int x1, int y1, int x2, int y2){
            g.drawLine(x1, y1, x2, y2);
            if(notBeenDone){
                int deltaX = Math.abs(x1-x2);
                int deltaY = Math.abs(y1-y2);
                for(int x=0;x<deltaX;x++) world[x1+x][y1] = 'B';
                for(int y=0;y<deltaY;y++) world[x1][y1+y] = 'B';
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            int x,y,diagonal;
            //int sensor_right_distance;
            //int sensor_left_distance;
            super.paintComponent(g);
            // Background
            setBackground(CANVAS_BACKGROUND);
            // Robot
            g.setColor(LINE_COLOR);
            g.drawRect(x1,y1,x2,y2);
            // Obstacles
            g.setColor(WALL_COLOR);
            // Set walls
            addWallFirstTime(g, firstTime, 40, 40, 70, 40);
            addWallFirstTime(g, firstTime, 20, 220, 400, 220);
            addWallFirstTime(g, firstTime, 70, 40, 70, 150);
            addWallFirstTime(g, firstTime, 70, 160, 70, 300);
            firstTime = false;

            // sensor measurement
            switch(direction) {
                case 'n':
                    y = y1;
                    while(y>0 && world[x1][y]!='B') y--;
                    sensor_forward_distance = Math.abs((y==0)? y1: y-y1);
                    y = y1+y2;
                    while(y<CANVAS_HEIGHT && world[x1][y]!='B') y++;
                    sensor_back_distance = Math.abs(y-y1-y2);
                    x = x1;
                    y = y1;
                    diagonal = 0;
                    while((x>0 && y>0) && world[x][y]!='B'){
                        x--;
                        y--;
                        diagonal++;
                    }
                    sensor_LD_distance = diagonal;
                    x = x1+x2;
                    y = y1;
                    diagonal = 0;
                    while((x<CANVAS_WIDTH && y>0) && world[x][y]!='B'){
                        x++;
                        y--;
                        diagonal++;
                    }
                    sensor_RD_distance = diagonal;
                    break;
                case 's':
                    y = y1+y2;
                    while(y<CANVAS_HEIGHT && world[x1][y]!='B') y++;
                    sensor_forward_distance = Math.abs(y-y1-y2);
                    y = y1;
                    while(y>0 && world[x1][y]!='B') y--;
                    sensor_back_distance = Math.abs((y==0)? y1: y-y1);
                    x = x1+x2;
                    y = y1+y2;
                    diagonal = 0;
                    while((x<CANVAS_WIDTH && y<CANVAS_HEIGHT) && world[x][y]!='B'){
                        x++;
                        y++;
                        diagonal++;
                    }
                    sensor_LD_distance = diagonal;
                    x = x1;
                    y = y1+y2;
                    diagonal = 0;
                    while((x>0 && y<CANVAS_HEIGHT) && world[x][y]!='B'){
                        x--;
                        y++;
                        diagonal++;
                    }
                    sensor_RD_distance = diagonal;
                    break;
                case 'e':
                    x = x1+x2;
                    while(x<CANVAS_WIDTH && world[x][y1]!='B') x++;
                    sensor_forward_distance = Math.abs(x-x1-x2);
                    x = x1;
                    while(x>0 && world[x][y1]!='B') x--;
                    sensor_back_distance = Math.abs((x==0)? x1: x-x1);
                    x = x1+x2;
                    y = y1;
                    diagonal = 0;
                    while((x<CANVAS_WIDTH && y>0) && world[x][y]!='B'){
                        x++;
                        y--;
                        diagonal++;
                    }
                    sensor_LD_distance = diagonal;
                    x = x1+x2;
                    y = y1+y2;
                    diagonal = 0;
                    while((x<CANVAS_WIDTH && y<CANVAS_HEIGHT) && world[x][y]!='B'){
                        x++;
                        y++;
                        diagonal++;
                    }
                    sensor_RD_distance = diagonal;
                    break;
                case 'w':
                    x = x1;
                    while(x>0 && world[x][y1]!='B') x--;
                    sensor_forward_distance = Math.abs((x==0)? x1:x-x1);
                    x = x1+x2;
                    while(x<CANVAS_WIDTH && world[x][y1]!='B') x++;
                    sensor_back_distance = Math.abs(x-x1-x2);
                    x = x1;
                    y = y1+y2;
                    diagonal = 0;
                    while((x>0 && y<CANVAS_HEIGHT) && world[x][y]!='B'){
                        x--;
                        y++;
                        diagonal++;
                    }
                    sensor_LD_distance = diagonal;
                    x = x1;
                    y = y1;
                    while((x>0 && y>0) && world[x][y]!='B'){
                        x--;
                        y--;
                        diagonal++;
                    }
                    sensor_RD_distance = diagonal;
                    break;
            }
            trainArray = new int[16];
            trainArray[0] = sensor_LD_distance;
            trainArray[1] = sensor_forward_distance;
            trainArray[2] = sensor_RD_distance;
            trainArray[3] = sensor_back_distance;

            // LD train values are located in the index range 4 to 6 inclusive
            addTrainValues(sensor_LD_distance, 4);

            // LD train values are located in the index range 7 to 9 inclusive
            addTrainValues(sensor_forward_distance, 7);

            // LD train values are located in the index range 10 to 12 inclusive
            addTrainValues(sensor_RD_distance, 10);

            // LD train values are located in the index range 13 to 15 inclusive
            addTrainValues(sensor_back_distance, 13);

            // CSV recording
            try {
                w.write(""+x1+","+y1+","+direction+","+sensor_forward_distance + "," +
                        sensor_back_distance +  "," +
                        sensor_LD_distance + "," + sensor_RD_distance + ",0,0,0,0\n");
                for(int i = 0; i < trainArray.length; i++){
                    if(i == trainArray.length - 1){
                        fw.write(trainArray[i] + "\n");
                        break;
                    }
                    fw.write(trainArray[i] + ", ");
                }
                //fw.write(Arrays.toString(trainArray) + "\n");
                //fw.writeObject(trainArray);
                fw.flush();
                w.flush();
            } catch(IOException e) {
                System.out.println("recording: "+e.getMessage());
            }
        }
    }

    private void addTrainValues(int dataValue, int relevantIndex){
        if(dataValue >= 20){
            trainArray[relevantIndex] = 1;
            return;
        }
        else if(dataValue > 5){
            trainArray[relevantIndex + 1] = 1;
            return;
        }
        trainArray[relevantIndex + 2] = 1;
    }

    /*****************************************************************************
     / The entry main() method
     ***************************************************************************/
    public static void main(String[] args) {
        // Run GUI codes on the Event-Dispatcher Thread for thread safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CGMoveALine(); // Let the constructor do the job
            }
        });
    }
}
