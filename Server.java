import java.net.ServerSocket;
import java.util.ArrayList;
import java.net.*;

public class Server extends Thread
{

    public static final int port = 9000;

    private ArrayList<ServerThread> conections;
    private ArrayList<GameLobby> activeLobbys;

    private ServerSocket ss;

    public Server()
    {
        start();
    }

    @Override
    public void run()
    {

        conections = new ArrayList<ServerThread>();
        activeLobbys = new ArrayList<GameLobby>();

        try 
        {
            ss = new ServerSocket(port);
            System.out.println("Server is running...");

            while (true)
            {
                Socket sock = ss.accept();

                System.out.println(("Client accepted: " + (((InetSocketAddress)sock.getRemoteSocketAddress()).getAddress()).toString().replace("/","")));
                ServerThread newConection = new ServerThread(sock, this);
                conections.add(newConection);
            }

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage() + " Server.java Error Line " + new Throwable().getStackTrace()[0].getLineNumber());
        }
    }

    public void closeServer()
    {

        if (ss != null)
        {
            try 
            {
                ss.close();
                System.out.println("Server is closed...");
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage() + " Server.java Error Line " + new Throwable().getStackTrace()[0].getLineNumber());
            }
        }
        
    }

    public Boolean tryConnection(String username, ServerThread actioner)
    {

        for (ServerThread connecton : conections)
        {
            if (actioner != connecton)
            {
                if (username.equals(connecton.username))
                {
                    System.out.println(username + " User Already Exists");
                    return false;  
                }
            }
        }

        return true;
    }

    public void update()
    {
        for (ServerThread connection : conections)
        {

            connection.broadcast("/update " +  activeLobbys.size());

            for (GameLobby lobby : activeLobbys)
            {
                connection.broadcast(lobby.toString());
            }

        }
    }

    public void singleUpdate(ServerThread newConnection)
    {
        newConnection.broadcast("/update " +  activeLobbys.size());

        for (GameLobby lobby : activeLobbys)
        {
            newConnection.broadcast(lobby.toString());
        }
    }

    public Boolean addLobby(String name, ServerThread actioner)
    {

        for (GameLobby lobby : activeLobbys)
        {
            if (lobby.name.equals(name))
            {
                System.out.println("Lobby Already Exists");
                return false;
            }
        }

        GameLobby newLobby =new GameLobby(name, this);
        newLobby.joinPlayer(actioner); 
        activeLobbys.add(newLobby);
        return true;
    }

    public void removeAt(String lobbyName, ServerThread actioner)
    {
        for (GameLobby lobby : activeLobbys)
        {
            if (lobby.name.equals(lobbyName))
            {
                lobby.removePlayer(actioner);
                break;
            }
        }
    }

    public boolean joinAt(String lobbyName, ServerThread actioner)
    {
        for (GameLobby lobby : activeLobbys)
        {
            if (lobby.name.equals(lobbyName))
            {
                if (lobby.joinPlayer(actioner))
                {
                    return true;
                }
                
                break;
            }
        }

        return false;
    }

    public void discnnectRoom(GameLobby lobby)
    {
        activeLobbys.remove(lobby);
        update();
    }

    public void removeConection(ServerThread conection)
    {
        for (GameLobby lobby : activeLobbys)
        {
            if (lobby.removePlayer(conection))
            {
                break;
            }
        }

        conections.remove(conection);
    }

    public void msg(String msg)
    {
        System.out.println(msg);
    }
    
}
