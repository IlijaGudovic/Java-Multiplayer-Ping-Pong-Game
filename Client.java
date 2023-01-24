import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;

public class Client extends JFrame
{

    private Socket sock;
    private BufferedReader in;
    private PrintWriter out;

    private String hostName;
    private String username;

    private JComboBox<String> listBox;
    private JTextArea displayText;
    private JTextArea roomName;

    private ArrayList<String> data;

    private Gameplay game;
    private String lobby;

    public Client(String ip, Main main, String username)
    {

        data = new ArrayList<String>();
        hostName = ip;

        this.username = username;

        setTitle("Game Client");
        setSize(340, 520);
        setLayout(new FlowLayout());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {

                try
                {
                    System.out.println("Client Closed");

                    out.println("/exit");

                    in.close();
                    out.close();
                    sock.close();

                } catch (Exception ex)
                {
                    System.out.println(ex.getMessage());
                }

                if (main.serverActive)
                {
                    setVisible(false);
                }
                else
                {
                    main.setVisible(true);
                    setVisible(false);
                    //System.exit(0);
                }
                
            }
        });

        
        listBox = new JComboBox<String>();
        add(listBox);

        roomName = new JTextArea((username + "'s lobby"));
        add(roomName);

        JButton hostButton = new JButton("Create");

        hostButton.addActionListener(e ->
        {
           creatLobby();
        });
        
        add(hostButton);

        JButton joinButton = new JButton("Join");

        joinButton.addActionListener(e ->
        {
            joinLobby();
        });
        
        add(joinButton);

        displayText = new JTextArea(16, 16);
        add(displayText);

        setVisible(true);

        startConection(username);
        
    }

    public void creatLobby()
    {

        if (lobby != null)
        {
            return;    
        }

        try 
        {
            out.println("/host " + roomName.getText().replace(" ", "_"));
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage() + " Failed to create lobby. Client.java Error Line " + new Throwable().getStackTrace()[0].getLineNumber());
        }
    }

    public void startConection(String username)
    {

        try 
        {
            sock = new Socket(InetAddress.getByName(hostName), 9000);

            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out =new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);

            out.println("/connect " + username);

            String response = in.readLine();
            displayText.append(response + "\n");

            String massageSplit[] = response.split(" ");

            if (massageSplit[1].equals("Connected"))
            {
                System.out.println("New Cleint Joined on " +  hostName);
                new Inbox(this);
            }

        }
        catch (IOException e)
        {
            System.out.println(e.getMessage() + " Client.java Error Line " + new Throwable().getStackTrace()[0].getLineNumber());
        }
        
    }

    public void call(String action)
    {
        out.println(action);
    }

    public void joinLobby()
    {
        try
        {

            String desiredLobby = listBox.getSelectedItem().toString();

            //Already active in same lobby
            if (lobby != null)
            {
                if (lobby.equals(desiredLobby.replace(" ", "_")))
                {
                    return;
                }
            }
           
            System.out.println("Trying to join on " + desiredLobby);

            if (listBox.getSelectedItem() != null)
            {
                out.println("/join " + desiredLobby.replace(" ", "_"));
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

    }

    public class Inbox extends Thread
    {

        private Client myClient;

        public Inbox(Client input)
        {
            myClient = input;
            start();
        }

        public void run()
        {

            try
            {
                String inMassage;
                while((inMassage = in.readLine()) != null)
                {

                    if (inMassage.startsWith("/update"))
                    {

                        String massageSplit[] = inMassage.split(" ");

                        int index = Integer.parseInt(massageSplit[1]);

                        data.clear();

                        for (int i = 0; i < index; i++)
                        {
                            data.add(in.readLine());
                        }

                        System.out.println("Active lists on " + username + ": " + data);

                        listBox.removeAllItems();
                        for (String newItemString : data)
                        {
                            listBox.addItem(newItemString.toString());   
                        }
                    }
                    else if (inMassage.startsWith("/join"))
                    {
                        if (lobby != null)
                        {
                            //Remove from active lobby
                            out.println("/leave " + lobby);
                        }

                        //Join on new
                        String massageSplit[] = inMassage.split(" ");
                        lobby = massageSplit[1];

                        displayText.append("Joined on " + lobby.replace("_", " ") + "\n");

                    }
                    else if (inMassage.startsWith("/start"))
                    {
                        String massageSplit[] = inMassage.split(" ");

                        game = new Gameplay(myClient);
                        game.player01.username = massageSplit[1];
                        game.player02.username = massageSplit[2];
                    }
                    else if (inMassage.startsWith("/move"))
                    {

                        String massageSplit[] = inMassage.split(" ");
                        String user = massageSplit[1];
                        int point = Integer.parseInt(massageSplit[2]);

                        if (user.equals(game.player01.username))
                        {
                            game.player01.x = point;
                        }
                        else if(user.equals(game.player02.username))
                        {
                            game.player02.x = point;
                        }
                        else if(user.equals("ball"))
                        {
                            game.ball.x = Integer.parseInt(massageSplit[2]);
                            game.ball.y = Integer.parseInt(massageSplit[3]);
                        }
                    }
                    else if (inMassage.startsWith("/gameover"))
                    {
                        displayText.append(game.getScore() + "\n");
                        game.setVisible(false);
                        lobby = null;
                    }
                    else if (inMassage.startsWith("/score"))
                    {
                        String massageSplit[] = inMassage.split(" ");
                        game.player01.score = massageSplit[1];
                        game.player02.score = massageSplit[2];
                    }
                    else if (inMassage.startsWith("/audio"))
                    {
                        game.playAudio();
                    }
                    else
                    {
                        displayText.append(inMassage + "\n");
                    }

                }
            }
            catch (IOException e)
            {
                displayText.append(e.getMessage() + " Client Error Line " + new Throwable().getStackTrace()[0].getLineNumber() + "\n");
            }
            
        }
        
    }


}
