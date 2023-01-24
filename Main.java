import javax.swing.*;
import java.awt.*;

public class Main extends JFrame
{

    private JTextField ipServerAdress;
    private JTextField cliantName;

    public Boolean serverActive = false;
    private Server server;

    public static void main(String[] args)
    {
        new Main();
    }

    public Main()
    {
        setTitle("Client");
        setSize(460, 110);
        setLayout(new GridBagLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        cliantName = new JTextField("Username", 10);
        panel.add(cliantName);

        //Set local ip adress as default
        ipServerAdress = new JTextField("127.0.0.1", 12);
        panel.add(ipServerAdress);

        JButton joinButton = new JButton("Join");
        JButton hostButton = new JButton("Host");

        joinButton.addActionListener(e ->
        {
            new Client(ipServerAdress.getText(), this, cliantName.getText());

            if (!serverActive)
            {
                setVisible(false);
            }
        });

        hostButton.addActionListener(e ->
        {
            if (serverActive)
            {
                //Close server
                serverActive = false;
                server.closeServer();
                hostButton.setBackground(new JButton().getBackground());
            }
            else
            {
                //Activate server
                serverActive = true;
                server = new Server();
                hostButton.setBackground(new Color(139, 195, 74));
            }
        });

        panel.add(joinButton);
        panel.add(hostButton);

        add(panel);

        setVisible(true);
    }
    
}
