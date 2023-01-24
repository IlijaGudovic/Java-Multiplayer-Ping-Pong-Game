import javax.swing.JFrame;

import java.awt.Color;

import javax.sound.sampled.*;

import java.awt.Graphics;
import java.awt.event.*;
import java.io.File;
import java.awt.*;
import java.net.URL;

public class Gameplay extends JFrame implements KeyListener, Runnable
{

    private int direction = 0;

    private Client cliant;

    public Position player01;
    public Position player02;
    public Position ball;
    private Skin skin;
    private Clip clip;
    private URL path;

    public Gameplay(Client cliant)
    {

        this.cliant = cliant;

        path = getClass().getResource("Resources/Sound.wav");
        
        File file = new File("Sound.wav");
        System.out.println(file.toPath());

        try
        {
            AudioInputStream audio = AudioSystem.getAudioInputStream(path);
            clip = AudioSystem.getClip();
            clip.open(audio);
        } 
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        ball = new Position();
        skin = new Skin();

        player01 = new Position();
        player02 = new Position();

        player01.x = 210; player01.y = 620;
        player02.x = 210; player02.y = 20;

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e)
            {
                cliant.call("/gameover");
                setVisible(false);
            }
        });

        setTitle("Game");
        setSize(420, 640);
        setResizable(false);

        addKeyListener(this);  

        setVisible(true);

        Thread gameLoop = new Thread(this);
        gameLoop.start();

    }

    public void playAudio()
    {
        clip.setFramePosition(0);
        clip.start();
    }

    public String getScore()
    {

        int p1 = Integer.parseInt(player01.score);
        int p2 = Integer.parseInt(player02.score);

        if (p1 > p2)
        {
            return player01.username + " won " + player01.score + ":" + player02.score + " vs "  + player02.username;
        }
        else if(p1 < p2)
        {
            return player02.username + " won " + player02.score + ":" + player01.score + " vs "  + player01.username;
        }
        else
        {
            return player01.username + " vs "  + player02.username + " resulted in a draw !";
        }

    }

    public void paint(Graphics g)
    {
        Image image = createImage(getWidth(), getHeight());
        Graphics graphic = image.getGraphics();
        
        graphic.fillRect(player01.x - player01.size_x / 2, player01.y, player01.size_x, player01.size_y);
        graphic.fillRect(player02.x - player02.size_x / 2, player02.y, player02.size_x, player02.size_y);

        FontMetrics metrics = g.getFontMetrics();

        String score = player01.username + " " + player01.score + " VS " + player02.score +  " " + player02.username;
        graphic.drawString(score, 210 - metrics.stringWidth(score) / 2, 320 - metrics.getHeight() / 2);
        
        graphic.setColor(skin.color()); skin.update();
        graphic.fillOval(ball.x, ball.y, 20, 20);

        g.drawImage(image, 0, 0, this);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if ((e.getKeyCode() == 65 || e.getKeyCode() == 37) && direction != -1) //Left
        {
            direction = -1;
            castDir();
        }
        else if ((e.getKeyCode() == 68 || e.getKeyCode() == 39) && direction != 1) //Right
        {
            direction = 1;
            castDir();
        }

    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        if (e.getKeyCode() == 65 || e.getKeyCode() == 37) //Left
        {
            direction += 1;
            castDir();
        }
        if (e.getKeyCode() == 68 || e.getKeyCode() == 39) //Right
        {
            direction -= 1;
            castDir();
        }
    }

    private void castDir()
    {
        cliant.call("/move " +  Integer.toString(direction));
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
        
    }

    @Override
    public void run()
    {
        while(true)
        {
            repaint();

            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e) {}

        }
    }

    public class Position
    {
        public String username;
        public String score = "0";

        public int x;
        public int y;

        public Position()
        {
            size_x = 60;
            size_y = 20;
        }

        public int size_x;
        public int size_y;
    }

    private class Skin
    {

        private int[] Channel;
        private int value = 1;
        private int i;

        public Skin()
        {
            Channel = new int[3];
            i = 0;
        }

        public void update()
        {

            Channel[i] += value;

            if(Channel[i] == 255 || Channel[i] == 0)
            {
                i++;

                if (i >= 2)
                {
                    value *=  -1;

                    i = 0;

                }
            }

        }

        public Color color()
        {
            return new Color(0, Channel[0], Channel[1]);
        }

    }
    
}
