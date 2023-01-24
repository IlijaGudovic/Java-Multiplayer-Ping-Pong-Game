import java.util.ArrayList;

public class GameLobby
{
    public String name;

    public ArrayList<ServerThread> activeUsers;

    final static int playerLimit = 2;

    private Server server;

    public boolean inGame = false;
    
    public GameLobby(String name, Server server)
    {
        activeUsers = new ArrayList<ServerThread>();
        this.server = server;
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public Boolean isAvailable()
    {

        for (int i = 0; i < activeUsers.size(); i++)
        {
            ServerThread player = activeUsers.get(i);
            if (player.sock.isClosed())
            {
                System.out.println(player.username + " is not active");
                activeUsers.remove(player);
                i--;
            }
        }

        if (activeUsers.size() == playerLimit)
        {
            return true;
        }
        else
        {
            if (activeUsers.size() == 0)
            {
                server.removeAt(name, null);
            }

            return false;
        }
    }

    public Boolean joinPlayer(ServerThread newPlayer)
    {

        if (activeUsers.size() == playerLimit)
        {
            if (isAvailable())
            {
                System.out.println("Room Size is Full");
                return false;
            }
            
        }

        newPlayer.broadcast("/join " + name.replace(" ", "_"));
        activeUsers.add(newPlayer);
        newPlayer.lobby = this;

        if (activeUsers.size() == playerLimit)
        {
            if (isAvailable())
            {
                System.out.println("Start The Game");

                print("/start " + activeUsers.get(0).username + " " + activeUsers.get(1).username);
                inGame = true;

                activeUsers.get(0).ball = activeUsers.get(0).new Ball();
                activeUsers.get(1).ball = null;
            }
        }

        return true;

    }

    public void print(String massage)
    {
        for (ServerThread player : activeUsers)
        {
            player.broadcast(massage);
        }
    }

    public Boolean removePlayer(ServerThread player)
    {

        for (ServerThread active : activeUsers)
        {
            if (player == active)
            {
                activeUsers.remove(player);
                player.lobby = null;

                if (activeUsers.size() == 0)
                {
                    System.out.println("No Active Players in " + name);
                    server.discnnectRoom(this);
                }

                return true;
            }
        }

        return false;
    }

}