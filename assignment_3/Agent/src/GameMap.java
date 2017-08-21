
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class GameMap
{
    private Map<Coordinate, Cell> m_map = null;
    private Map<Coordinate, Cell> m_virgin_map = null;
    private Collection<Set<Coordinate>> m_seas;
    
    private int min_x = 0;
    private int min_y = 0;
    private int max_x = 0;
    private int max_y = 0;
    private Agent m_agent;

    public GameMap(Agent agent)
    {
        this.m_map = new HashMap<Coordinate, Cell>();
        this.m_virgin_map = new HashMap<Coordinate, Cell>();
        this.m_agent = agent;
    }

    /**
     * The copy constructor is very important here
     * 
     * @param other
     */
    public GameMap(GameMap other, Agent agent)
    {
        this.m_agent = agent;

        this.max_x = other.max_x;
        this.max_y = other.max_y;
        this.min_x = other.min_x;
        this.min_y = other.min_y;

        this.m_map = new HashMap<Coordinate, Cell>();
        this.m_virgin_map = new HashMap<Coordinate, Cell>();
        
        for (Coordinate pos : other.m_map.keySet())
        {
            Cell cell = other.m_map.get(pos);
            Cell cell_copy = new Cell(cell, this.m_agent);
            this.m_map.put(pos, cell_copy);
        }
        for (Coordinate pos : other.m_virgin_map.keySet())
        {
            Cell cell = other.m_virgin_map.get(pos);
            Cell cell_copy = new Cell(cell, this.m_agent);
            this.m_virgin_map.put(pos, cell_copy);
        }
        
        this.m_seas = other.m_seas;
    }

    public void unvisitAll()
    {
        for (Coordinate pos : this.m_map.keySet())
        {
            Cell cell = this.m_map.get(pos);
            cell.unvisit();
        }
    }

    private Coordinate relativeToAbsolute(int i, int j, Coordinate agent_pos)
    {
        if (this.m_agent.dir() == 0)
        {
            return new Coordinate(agent_pos.getX() - i + 2, agent_pos.getY() + j - 2);
        }
        else if (this.m_agent.dir() == 1)
        {
            return new Coordinate(agent_pos.getX() - j + 2, agent_pos.getY() - i + 2);
        }
        else if (this.m_agent.dir() == 2)
        {
            return new Coordinate(agent_pos.getX() + i - 2, agent_pos.getY() - j + 2);
        }
        else
        {
            return new Coordinate(agent_pos.getX() + j - 2, agent_pos.getY() + i - 2);
        }
    }

    /**
     * Update and expand the map if there is a movement of the agent <b> If the
     * agent moves more than one step (Manhattan distance is more than 1), this
     * move will be treated as invalid. <b> If the agent does not move, nothing
     * will be done here
     * 
     * @param agent_pos
     * @param view
     * @throws Exception
     */
    public void update(Coordinate agent_pos, char[][] view)
    {
        // We assume that size of view is 5 * 5
        for (int i = 0; i < 5; i++)
        {
            for (int j = 0; j < 5; j++)
            {
                CellType type = Globals.char2CellType(view[i][j]);
                Coordinate pos = relativeToAbsolute(i, j, agent_pos);
                this.createCell(pos, type);
            }
        }
        
        this.m_seas = this.generateWaterGroups();
    }

    /**
     * This function is for test. We assume that the left-top corner of the
     * absolute view is (0, 0)
     * 
     * @param absolute_view
     */
    public void update(ArrayList<String> absolute_view)
    {
        for (int i = 0; i < absolute_view.size(); i++)
        {
            for (int j = 0; j < absolute_view.get(i).length(); j++)
            {
                CellType type = CellType.Floor;
                char cr = absolute_view.get(i).charAt(j);
                
                if (cr == '^' || cr == 'V' || cr == 'v' || cr == '<' || cr == '>')
                {
                    this.m_agent.setPos(new Coordinate(j, i));
                    this.m_agent.setHome(new Coordinate(j, i));
                    int dir = 0;
                    switch (cr)
                    {
                    case '^':
                        dir = 3;
                        break;
                    case '<':
                        dir = 2;
                        break;
                    case 'V':
                    case 'v':
                        dir = 1;
                        break;
                    case '>':
                        dir = 0;
                        break;
                    default:
                    }
                    this.m_agent.setDir(dir);
                }
                else
                {
                    type = Globals.char2CellType(absolute_view.get(i).charAt(j));
                }

                this.createCell(new Coordinate(j, i), type);
            }
        }
        
        this.m_seas = this.generateWaterGroups();
    }

    public Cell createCell(Coordinate pos, CellType type)
    {
        Cell the_cell = null;
        if (!this.m_map.containsKey(pos))
        {
            the_cell = new Cell(pos, type, this.m_agent);
            this.putCell(pos, the_cell);

            switch (type)
            {
            case Treasure:
                this.m_agent.res().get(CellType.Treasure).add(the_cell.getPos());
                break;
            case Key:
                this.m_agent.res().get(CellType.Key).add(the_cell.getPos());
                break;
            case Axe:
                this.m_agent.res().get(CellType.Axe).add(the_cell.getPos());
                break;
            case Dynamite:
                this.m_agent.res().get(CellType.Dynamite).add(the_cell.getPos());
                break;
            case Tree:
                this.m_agent.res().get(CellType.Tree).add(the_cell.getPos());
                break;
            case Door:
                this.m_agent.res().get(CellType.Door).add(the_cell.getPos());
                break;
            case Wall:
                this.m_agent.res().get(CellType.Wall).add(the_cell.getPos());
                break;
            case Floor:
                this.m_agent.res().get(CellType.Floor).add(the_cell.getPos());
            default:
            }
        }

        return the_cell;
    }

    /**
     * 0: East <b> 1: South <b> 2: West <b> 3: North <b>
     * 
     * @param dir
     * @return
     */
    public Cell getNeighbor(Cell me, int dir)
    {
        if (dir < 0 || dir > 3)
        {
            return null;
        }

        Direction dr = new Direction(dir);
        int new_x = me.getPos().getX() + dr.getX();
        int new_y = me.getPos().getY() + dr.getY();

        return this.m_map.get(new Coordinate(new_x, new_y));
    }

    /**
     * 
     * @param me
     * @param dir
     * @return
     */
    public Cell getNeighbor(Coordinate me, int dir)
    {
        if (dir < 0 || dir > 3)
        {
            return null;
        }

        Direction dr = new Direction(dir);
        int new_x = me.getX() + dr.getX();
        int new_y = me.getY() + dr.getY();

        return this.m_map.get(new Coordinate(new_x, new_y));
    }

    public Collection<Cell> getAllNeighbors(Coordinate me)
    {
        ArrayList<Cell> list = new ArrayList<Cell>();
        for (int i = 0; i < 4; i++)
        {
            Cell nei = this.getNeighbor(me, i);

            if (null != nei)
            {
                list.add(nei);
            }
        }

        return list;
    }

    /**
     * 0: East <b> 1: South <b> 2: West <b> 3: North <b> All directions will be
     * randomly selected
     * 
     * @param me
     * @return
     */
    public Cell getRandomNeighbor(Cell me)
    {
        int dir = Globals.random.nextInt(4);
        Direction dr = new Direction(dir);
        int new_x = me.getPos().getX() + dr.getX();
        int new_y = me.getPos().getY() + dr.getY();

        return this.m_map.get(new Coordinate(new_x, new_y));
    }

    /**
     * Create a new neighbor of direction: 0: East <b> 1: South <b> 2: West <b>
     * 3: North <b>
     * 
     * @param me
     * @param dir
     * @param type
     * @return
     */
    public Cell createNeighbor(Cell me, int dir, CellType type)
    {
        if (dir < 0 || dir > 3)
        {
            return null;
        }

        Direction dr = new Direction(dir);
        int new_x = me.getPos().getX() + dr.getX();
        int new_y = me.getPos().getY() + dr.getY();

        Coordinate pos = new Coordinate(new_x, new_y);

        if (!this.m_map.containsKey(pos))
        {
            Cell new_cell = new Cell(pos, type, this.m_agent);
            this.putCell(pos, new_cell);
        }

        return this.m_map.get(pos);
    }

    public boolean containsCell(Coordinate pos)
    {
        return this.m_map.containsKey(pos);
    }

    public Cell getCell(Coordinate pos)
    {
        return this.m_map.get(pos);
    }

    public Cell putCell(Coordinate pos, Cell value)
    {
        this.min_x = this.min_x <= pos.getX() ? this.min_x : pos.getX();
        this.min_y = this.min_y <= pos.getY() ? this.min_y : pos.getY();

        this.max_x = this.max_x >= pos.getX() ? this.max_x : pos.getX();
        this.max_y = this.max_y >= pos.getY() ? this.max_y : pos.getY();
        
        this.m_virgin_map.put(pos, value);
        
        return m_map.put(pos, value);
    }
    
    public void explore(Coordinate pos)
    {
        this.m_virgin_map.remove(pos);
    }
    
    public class CellDist
    {
        private int m_dist = 1;
        private Cell m_cell = null;
        public CellDist(int dist, Cell c)
        {
            this.m_dist = dist;
            this.m_cell = c;
        }
        
        public Cell getCell()
        {
            return this.m_cell;
        }
        
        public int getDist()
        {
            return this.m_dist;
        }
    }
    
    public Map<Coordinate, Cell> getAllUnexplored()
    {
        return this.m_virgin_map;
    }

    public int size()
    {
        return this.m_map.size();
    }
    
    public int largestPossibleSize()
    {
        return (this.max_x - this.min_x + 1) * (this.max_y - this.min_y + 1);
    }

    public Set<Coordinate> getAllPos()
    {
        return this.m_map.keySet();
    }
    
    public void printRouteMap()
    {
        int width = this.max_x - this.min_x + 1;
        int height = this.max_y - this.min_y + 1;

        char[][] view = new char[height][width];
        for (int i = 0; i < height; i++)
        {
            for (int j = 0; j < width; j++)
            {
                view[i][j] = '0';
            }
        }

        for (Coordinate pos : this.m_map.keySet())
        {
            Cell c = this.m_map.get(pos);
            char ch = Globals.cellType2Char(c.getType());

            if (c.isVisited())
            {
                ch = '+';
            }

            view[pos.getY() - this.min_y][pos.getX() - this.min_x] = ch;
        }

        for (int i = 0; i < height; i++)
        {
            for (int j = 0; j < width; j++)
            {
                System.out.print(view[i][j]);
            }
            System.out.println();
        }
    }

    /**
     * Print the map for debug and test
     */
    public void printMap()
    {
        int width = this.max_x - this.min_x + 1;
        int height = this.max_y - this.min_y + 1;

        char[][] view = new char[height][width];
        for (int i = 0; i < height; i++)
        {
            for (int j = 0; j < width; j++)
            {
                view[i][j] = '0';
            }
        }

        for (Coordinate pos : this.m_map.keySet())
        {
            Cell c = this.m_map.get(pos);
            char ch = Globals.cellType2Char(c.getType());

            //if (c.isVisited())
            //{
            //    ch = '+';
            //}
            if (this.m_agent.pos().equals(pos))
            {
                switch (this.m_agent.dir())
                {
                case 0:
                    ch = '>';
                    break;
                case 1:
                    ch = 'v';
                    break;
                case 2:
                    ch = '<';
                    break;
                case 3:
                    ch = '^';
                    default:
                }
            }

            view[pos.getY() - this.min_y][pos.getX() - this.min_x] = ch;
        }

        for (int i = 0; i < height; i++)
        {
            for (int j = 0; j < width; j++)
            {
                System.out.print(view[i][j]);
            }
            System.out.println();
        }
    }
    
    public boolean equals(Object other)
    {
        if (null == other || !(other instanceof GameMap))
        {
            return false;
        }
        
        GameMap o_map = (GameMap)other;
        if (o_map.m_map.size() != this.m_map.size())
        {
            return false;
        }
        
        for (Coordinate pos : this.m_map.keySet())
        {
            if (!o_map.m_map.containsKey(pos))
            {
                return false;
            }
            
            Cell me = this.m_map.get(pos);
            Cell you = o_map.m_map.get(pos);
            
            if (!me.getPos().equals(you.getPos()))
            {
                return false;
            }
            
            if (me.getType() != you.getType())
            {
                return false;
            }
        }
        
        return true;
    }
    
    private Collection<Set<Coordinate>> generateWaterGroups()
    {
        Collection<Set<Coordinate>> col = new ArrayList<Set<Coordinate>> ();
        
        for (Coordinate pos : this.m_map.keySet())
        {
            Cell cell = this.m_map.get(pos);
            if (!cell.isWater())
            {
                continue;
            }
            
            boolean belongs = false;
            for (Set<Coordinate> group : col)
            {
                if (group.contains(pos))
                {
                    belongs = true;
                    break;
                }
            }
            
            if (belongs)
            {
                continue;
            }
            
            Queue<Coordinate> queue = new LinkedList<Coordinate>();
            Set<Coordinate> group = new HashSet<Coordinate> ();
            queue.add(cell.getPos());
            while (queue.size() > 0)
            {
                Coordinate current_pos = queue.poll();
                group.add(current_pos);         
                
                for (int dir = 0; dir < 4; dir++)
                {
                    Cell neighbor = this.m_agent.map().getNeighbor(current_pos, dir);

                    if (null != neighbor && neighbor.isWater())
                    {
                        if (!queue.contains(neighbor.getPos()) && !group.contains(neighbor.getPos()))
                        {
                            queue.add(neighbor.getPos());
                        }
                    }
                }
            }
            
            col.add(group);
        }
        
        /*
        for (Set<Coordinate> sea : col)
        {
            System.out.println("This sea has " + sea.size() + " cells!");
        }
        */
        
        return col;
    }
    
    public Collection<Set<Coordinate>> getSeas()
    {
        return this.m_seas;
    }
}
