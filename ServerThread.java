import java.util.concurrent.ThreadLocalRandom;
import java.io.*;import java.net.*;

public class ServerThread extends Thread
{
 
    public Socket sock;

    private BufferedReader in;
    private PrintWriter out;

    private Server server;

    public String username;

    //Gameplay settings
    public GameLobby lobby;
    private int direction = 0;
    public int position = 210;
    public int speed = 4;
    public int score;
    public Ball ball;

    public ServerThread(Socket sock, Server server)
    {
        this.sock = sock;
        this.server = server;

        ball = new Ball();
        score = 0;

        try
        {
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintWriter( new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);

            start();

            new Update().start();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    } 

    public void broadcast(String request)
    {
        try
        {
            out.println(request);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage() + "ServerThread.java Error Line " + new Throwable().getStackTrace()[0].getLineNumber());
        }
    }

    public void closeConection()
    {

        server.removeConection(this);

        try
        {
            in.close();
            out.close();
            sock.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage() + "ServerThread.java Error Line " + new Throwable().getStackTrace()[0].getLineNumber());
        }
            
    }

    public void run()
    {
        try
        {
            String massage;
            while((massage = in.readLine()) != null)
            {

                if (massage.startsWith("/host"))
                {
                    String massageSplit[] = massage.split(" ");
                    System.out.println("Callled Action from client: " + massageSplit[0]);

                    String lobbyName = massageSplit[1].replace("_", " ");

                    if (server.addLobby(lobbyName, this))
                    {
                        server.update();
                    }
                    else
                    {
                        out.println(lobbyName + " Already Active");
                    }
                    
                }
                else if(massage.startsWith("/connect"))
                {
                    String massageSplit[] = massage.split(" ");
                    username = massageSplit[1];

                    System.out.println(username + " is trying to connect");

                    if (!server.tryConnection(username, this))
                    {
                        out.println(username + " Is Already Active");
                        closeConection();
                        break;
                    }
                    else
                    {
                        out.println(username + " Connected on Server");
                        server.singleUpdate(this);
                    }
                }
                else if(massage.startsWith("/leave"))
                {

                    String massageSplit[] = massage.split(" ");
                    String lobby = massageSplit[1].replace("_", " ");
                    server.removeAt(lobby, this);

                }
                else if(massage.startsWith("/join"))
                {

                    String massageSplit[] = massage.split(" ");
                    String lobby = massageSplit[1].replace("_", " ");
                    
                    if (!server.joinAt(lobby, this))
                    {
                        out.println((lobby + " is full"));
                    }

                }
                else if(massage.startsWith("/move"))
                {
                    String massageSplit[] = massage.split(" ");
                    direction = Integer.parseInt( massageSplit[1]);
                }
                else if(massage.startsWith("/gameover"))
                {
                    server.discnnectRoom(lobby);
                    lobby.print(massage);
                }
                else if (massage.startsWith("/exit"))
                {
                    closeConection();
                    break;
                }
                else
                {
                    server.msg(massage);
                }

            }

        }
        catch (IOException e)
        {
            System.out.println(e.getMessage() + " ServerThread.java Error Line " + new Throwable().getStackTrace()[0].getLineNumber());
        }
    }

    private int clamp(int num)
    {
        if(num < -1)
        {
            return -1;
        }
        if(num > 1)
        {
            return 1;
        }

        return num;
    }

    public class Ball
    {
        int dir_x, dir_y, radius;

        float x, y, speed;

        public Ball()
        {
            dir_x = ThreadLocalRandom.current().nextInt(0, 2); if(dir_x == 0) dir_x = -1;
            dir_y = ThreadLocalRandom.current().nextInt(0, 2); if(dir_y == 0) dir_y = -1;

            x = 210;
            y = 320;

            radius = 20;
            speed = 2;
        }

        public void transform()
        {
            x += dir_x * speed;
            y += dir_y * speed;

            if (x > 420 - radius)
            {
                dir_x = -1;
            }
            else if (x < 0)
            {
                dir_x = 1;
            }

            if (y >= 640 - radius )
            {
                lobby.activeUsers.get(0).score ++;
                reset();
            }
            else if(y <= radius)
            {
                lobby.activeUsers.get(1).score ++;
                reset();
            }


            if (ball.x > lobby.activeUsers.get(0).position - 30 && ball.x < lobby.activeUsers.get(0).position + 30)
            {
                if (ball.y + radius > 620)
                {
                    speed += 0.2f;
                    ball.dir_y = -1;
                    lobby.print("/audio");
                }
            }

            if (ball.x > lobby.activeUsers.get(1).position - 30 && ball.x < lobby.activeUsers.get(1).position + 30)
            {
                if (ball.y < 40)
                {
                    speed += 0.2f;
                    ball.dir_y = 1;
                    lobby.print("/audio");
                }
            }
            
        }

        private void reset()
        {

            speed = 2f;
            lobby.print("/score " +  lobby.activeUsers.get(0).score + " " + lobby.activeUsers.get(1).score);

            dir_x *= -1;
            dir_y *= -1;

            x = 210;
            y = 320;
        }
    }

    public class Update extends Thread
    {

        @Override
        public void run()
        {
            while(true)
            {

                try 
                {
                    Thread.sleep(10);

                    if (lobby != null)
                    {
                        if (lobby.inGame)
                        {
                            position += clamp(direction) * speed;
                            
                            if (position < 30) {
                                position = 30;
                            }
                            if (position > 390) {
                                position = 390;
                            }

                            if (ball != null)
                            {
                                ball.transform();

                                for (ServerThread player : lobby.activeUsers)
                                {
                                    player.broadcast("/move " + "ball" + " " + Math.round(ball.x) + " " + Math.round(ball.y));
                                }
                            }

                            for (ServerThread player : lobby.activeUsers)
                            {
                                player.broadcast("/move " + username + " " + position);
                            }
                        }
                    }
                    
                }
                catch (Exception e)
                {

                }
                
               
            }

        }

    }
    
}
