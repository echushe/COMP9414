
import java.io.*;
import java.net.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;



/**
 * COMP9414 Assignment 3
 * <p>
 * Designed and programmed by:
 * <p> Chunnan Sheng
 * <p> Yufei Zhang 
 * 
 * There are three main components of this program:
 * The map, the search algorithm and the decision maker.
 * <p>
 * Instruction of the map:
 * <p>
 * The map is a warehouse of all the known world of the game. This map expands when the
 * agent tries to travel to new areas. We have to transform relative coordinates on behalf
 * of the agent into absolute coordinates to build the map. Hash tables are used to
 * store all the coordinates and their corresponding properties like walls, keys, waters
 * , axes, trees, etc.
 * <p>
 * Instruction of the search algorithm:
 * <p> 
 * The search strategy is designed based on the Uniform Cost Search Algorithm.
 * Each pixel on the map may have a different cost. For example, the ground has a cost of 1, while
 * the wall has a cost of 10000. The search strategy tries to find out a route of minimum cost.
 * If the minimum cost route cannot be found due to obstacles, the strategy can also
 * output the obstacles for assessment of decisions. The cost of each pixel may change if
 * the wall is demolished, or the agent gets a raft after cutting down a tree. For instance,
 * the water costs 10000 without a raft but only costs 100 if the agent gets a raft when cutting
 * down a tree.
 * <p>
 * Instruction of the decision maker:
 * <p>
 * The decision maker is the most complex part of this program. It uses a tree and Depth First Search
 * algorithm to search for viable options. Since the expansion of the tree would be exponential, we
 * have to do something to avoid the endless exponential search. There are several solutions to
 * deal with this problem.
 * <p>
 * 1. Some equivalent operations are valued to be ONE operation. For example, the tree-cut-down
 * options of different trees which are all accessible for the agent are considered to be ONE option.
 * <p>
 * 2. Reduce the chances in use of tools like dynamites. Dynamites can only be used in
 * some special circumstances when the agent is trapped or something useful is blocked by walls.
 * <p>
 * 3. Cancellation and re-start of search if the tree grows too fast. The canceled search tree is still
 * useful for the next round of search.
 * <p>
 * The rigorous logic of this decision maker would lead to failures of large maps.
 * However, it is quite fast and accurate for smaller maps of strict need of logics.
 * On the other hand, large maps of slower speed of option-expansion may also work under this solution.
 * <p>
 * The expansion of map also has some problems because the agent does not seem to explore distant land
 * areas blocked by waters if it does not see something useful on the other side of the water (actually
 * there is). There is shortage of time though improvements are needed.
 * 
 * 
 * 
 * 
 * @author Chunnan Sheng Yufei Zhang
 * @since 05/05/2017
 * @version 1.2
 *
 */
public class Agent
{
    public class CellPriComparator implements Comparator<Cell>
    {
        @Override
        public int compare(Cell arg0, Cell arg1)
        {
            if (arg0.getPriority() < arg1.getPriority())
            {
                return -1;
            }
            else if (arg0.getPriority() > arg1.getPriority())
            {
                return 1;
            }

            return 0;
        }
    }
    
    /**
     * Current position of the agent.
     */
    private Coordinate agent_pos;
    private Coordinate home;
    
    // public static Coordinate farthest_pos = null;
    /**
     * Current direction of the agent
     */
    private int agent_dir;
    private int treasures;
    
    private Map<ToolType, Integer> tools;
    
    private Map<CellType, Set<Coordinate>> res_discovered;
    
    private GameMap my_map;
    
    // These priority queues are to order the obstacles
    private Queue<Cell> m_walls;
    private Queue<Cell> m_trees;
    private Queue<Cell> m_doors;
    
    public Agent()
    {
        agent_pos = new Coordinate(0, 0);
        home = new Coordinate(0, 0);
        // Agent.farthest_pos = new Coordinate(0, 0);

        // We can assume that the default direction is southward (1, 0)
        agent_dir = 1;
        treasures = 0;
        
        res_discovered = new HashMap<CellType, Set<Coordinate>> ();
        res_discovered.put(CellType.Key, new HashSet<Coordinate>());
        res_discovered.put(CellType.Axe, new HashSet<Coordinate>());
        res_discovered.put(CellType.Dynamite, new HashSet<Coordinate>());
        res_discovered.put(CellType.Tree, new HashSet<Coordinate>());
        res_discovered.put(CellType.Treasure, new HashSet<Coordinate>());
        res_discovered.put(CellType.Door, new HashSet<Coordinate>());
        res_discovered.put(CellType.Wall, new HashSet<Coordinate>());
        res_discovered.put(CellType.Floor, new HashSet<Coordinate>());
        
        tools = new HashMap<ToolType, Integer> ();
        tools.put(ToolType.Key, 0);
        tools.put(ToolType.Dynamite, 0);
        tools.put(ToolType.Raft, 0);
        tools.put(ToolType.Axe, 0);
        
        my_map = new GameMap(this);
        
        this.prioritizeObstacles();
    }
    
    /**
     * Copy constructor is very important here
     * @param other
     */
    public Agent(Agent other)
    {
        // Copy the position
        this.agent_pos = new Coordinate(other.agent_pos);
        // Copy of the home
        this.home = new Coordinate(other.home);
        // Copy the direction
        this.agent_dir = other.agent_dir;       
        this.treasures = other.treasures;
        
        // Copy discovered res
        this.res_discovered = new HashMap<CellType, Set<Coordinate>> ();
        for (CellType ct : other.res_discovered.keySet())
        {
            Set<Coordinate> res = other.res_discovered.get(ct);
            Set<Coordinate> res_copy = new HashSet<Coordinate> ();
            for (Coordinate pos : res)
            {
                res_copy.add(pos);
            }
            this.res_discovered.put(ct, res_copy);
        }
        // Copy tools
        tools = new HashMap<ToolType, Integer> ();
        for (ToolType tt : other.tools.keySet())
        {
            tools.put(tt, other.tools.get(tt));
        }
        
        this.my_map = new GameMap(other.map(), this);
        
        // System.out.println("Number of trees: " + this.res_discovered.get(CellType.Tree).size());
        
        this.prioritizeObstacles();
    }
    
    public Coordinate home()
    {
        return this.home;
    }
    
    public void setHome(Coordinate home)
    {
        this.home = home;
    }
    
    public int treasure()
    {
        return this.treasures;
    }
    
    public void setTreasure(int t)
    {
        this.treasures = t;
    }
    
    public Coordinate pos()
    {
        return this.agent_pos;
    }
    
    public void setPos(Coordinate pos)
    {
        this.agent_pos = pos;
    }
    
    public int dir()
    {
        return this.agent_dir;
    }
    
    public void setDir(int dir)
    {
        this.agent_dir = dir;
    }
    
    public Map<ToolType, Integer> tools()
    {
        return this.tools;
    }
    
    public Map<CellType, Set<Coordinate>> res()
    {
        return this.res_discovered;
    }
    
    public GameMap map()
    {
        return this.my_map;
    }
    
    public Queue<Cell> getWalls()
    {
        return this.m_walls;
    }
    
    public Queue<Cell> getTrees()
    {
        return this.m_trees;
    }
    
    public Queue<Cell> getDoors()
    {
        return this.m_doors;
    }
    
    public void prioritizeObstacles()
    {
        this.m_walls = new PriorityQueue<Cell>(new CellPriComparator());
        this.m_trees = new PriorityQueue<Cell>(new CellPriComparator());
        this.m_doors = new PriorityQueue<Cell>(new CellPriComparator());
        
        // axe
        // key
        // dynamite
        // treasure
        Set<Coordinate> axes = res_discovered.get(CellType.Axe);
        Set<Coordinate> keys = res_discovered.get(CellType.Key);
        Set<Coordinate> dynamites = res_discovered.get(CellType.Dynamite);
        Set<Coordinate> treasures = res_discovered.get(CellType.Treasure);

        Set<Coordinate> all_pos = my_map.getAllPos();
        for (Coordinate pos : all_pos)
        {
            Cell cell = my_map.getCell(pos);
            if (cell.getType() == CellType.Wall || cell.getType() == CellType.Tree
                    || cell.getType() == CellType.Door)
            {
                int min_dist = -1;

                for (Coordinate axe : axes)
                {
                    int dist = axe.manhattan(pos);
                    if (-1 == min_dist)
                    {
                        min_dist = dist;
                    }
                    else if (dist < min_dist)
                    {
                        min_dist = dist;
                    }
                }

                for (Coordinate key : keys)
                {
                    int dist = key.manhattan(pos);
                    if (-1 == min_dist)
                    {
                        min_dist = dist;
                    }
                    else if (dist < min_dist)
                    {
                        min_dist = dist;
                    }
                }

                for (Coordinate dynamite : dynamites)
                {
                    int dist = dynamite.manhattan(pos);
                    if (-1 == min_dist)
                    {
                        min_dist = dist;
                    }
                    else if (dist < min_dist)
                    {
                        min_dist = dist;
                    }
                }

                for (Coordinate treasure : treasures)
                {
                    int dist = treasure.manhattan(pos);
                    if (-1 == min_dist)
                    {
                        min_dist = dist;
                    }
                    else if (dist < min_dist)
                    {
                        min_dist = dist;
                    }
                }

                if (-1 != min_dist)
                {
                    cell.setPriority(min_dist);
                }

                switch (cell.getType())
                {
                case Wall:
                    this.m_walls.add(cell);
                    break;
                case Tree:
                    this.m_trees.add(cell);
                    break;
                case Door:
                    this.m_doors.add(cell);
                    break;
                default:
                }
            }
        }
        
        //System.out.println("Number of prioritized trees: " + this.m_trees.size());
    }
       
    
    public static char get_action(char view[][])
    {
        Globals.global_agent.map().update(Globals.global_agent.agent_pos, view);
        // Globals.global_agent.my_map.printMap();

        if (null == Globals.current_strategy)
        {
            Strategy new_strategy = Globals.decision_maker.newDecision();
            Globals.current_strategy = new_strategy;
        }

        char move = Globals.current_strategy.executeNextMove(); 
        // System.out.println("Action: " + move);

        while (0 == move) // 0 indicates that the current_strategy has been
                          // finished
        {
            Strategy new_strategy = Globals.decision_maker.newDecision();
            Globals.current_strategy = new_strategy;
            
            move = Globals.current_strategy.executeNextMove();
            //System.out.println("2. next action is: " + move);
        }

        return move;
    }

    public static void print_view(char view[][])
    {
        int i, j;

        System.out.println("\n+-----+");
        for (i = 0; i < 5; i++)
        {
            System.out.print("|");
            for (j = 0; j < 5; j++)
            {
                if ((i == 2) && (j == 2))
                {
                    System.out.print('^');
                }
                else
                {
                    System.out.print(view[i][j]);
                }
            }
            System.out.println("|");
        }
        System.out.println("+-----+");
    }

    public static void main(String[] args)
    {
        InputStream in = null;
        OutputStream out = null;
        Socket socket = null;
        char view[][] = new char[5][5];
        char action = 'F';
        int port;
        int ch;
        int i, j;

        Globals.initialize(new Agent());

        if (args.length < 2)
        {
            System.out.println("Usage: java Agent -p <port>\n");
            System.exit(-1);
        }

        port = Integer.parseInt(args[1]);

        try
        { // open socket to Game Engine
            socket = new Socket("localhost", port);
            in = socket.getInputStream();
            out = socket.getOutputStream();
        }
        catch (IOException e)
        {
            System.out.println("Could not bind to port: " + port);
            System.exit(-1);
        }

        try
        { // scan 5-by-5 wintow around current location
            while (true)
            {
                for (i = 0; i < 5; i++)
                {
                    for (j = 0; j < 5; j++)
                    {
                        if (!((i == 2) && (j == 2)))
                        {
                            ch = in.read();
                            if (ch == -1)
                            {
                                System.exit(-1);
                            }
                            view[i][j] = (char) ch;
                        }
                    }
                }
                // Agent.print_view(view); // COMMENT THIS OUT BEFORE SUBMISSION
                action = Agent.get_action(view);
                out.write(action);
            }
        }
        catch (IOException e)
        {
            System.out.println("Lost connection to port: " + port);
            System.exit(-1);
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
            }
        }
    }
}