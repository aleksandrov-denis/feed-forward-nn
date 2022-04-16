package nn.DenisAleksandrov.dataHarvester;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static java.awt.event.KeyEvent.*;

class MyCanvas extends JComponent {
    int x, y;

    MyCanvas(int a, int b) {
        x=a;
        y=b;
    }

    public void paint(Graphics g) {
        g.drawRect (10, 10, 200, 200);
        g.drawLine(20,20,20,20);
    }
}

public class DrawRect implements KeyListener {
    JFrame window = new JFrame();
    MyCanvas aCanvas = new MyCanvas(10,10);

    void makeWindow() {
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setBounds(30, 30, 300, 300);
        window.getContentPane().add(aCanvas);
        window.setVisible(true);
        window.addKeyListener(this);
    }
    public static void main(String[] a) {
        DrawRect dr = new DrawRect();
        dr.makeWindow();
    }

    public void keyTyped (KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case VK_DOWN: aCanvas.y--; break;
            case VK_UP: aCanvas.y++; break;
            case VK_RIGHT: aCanvas.x++; break;
            case VK_LEFT: aCanvas.x--; break;
        }
        aCanvas.repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}