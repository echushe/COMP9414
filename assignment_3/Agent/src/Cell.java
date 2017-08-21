

public class Cell
{
    private Agent m_agent;
    private boolean m_visited;
    private CellType m_type;
    private int m_priority;
    private int m_visited_time;

    // Position of this cell
    // The agent's position when the program starts is (0, 0).
    private Coordinate m_pos;

    public Cell(Coordinate pos, Agent agent)
    {
        this.m_agent = agent;
        this.m_visited = false;
        this.m_type = CellType.Floor;
        this.m_pos = pos;
        this.m_priority = 100000;
        this.m_visited_time = 0;
    }
    
    public Cell(Cell other, Agent agent)
    {
        this.m_agent = agent;
        this.m_visited = other.m_visited;
        this.m_type = other.m_type;
        this.m_pos = other.m_pos;
        this.m_priority = other.m_priority;
        this.m_visited_time = other.m_visited_time;
    }
    
    public Cell(Coordinate pos, CellType type, Agent agent)
    {
        this(pos, agent);
        this.m_type = type;
    }
    
    public int getVisitedTime()
    {
        return this.m_visited_time;
    }
    
    public void setPriority(int p)
    {
        this.m_priority = p;
    }
    
    public int getPriority()
    {
        return this.m_priority;
    }
    
    public void assessVisit()
    {
        this.m_visited = true;
    }
    
    public int getCost()
    {
        if (this.isAccessible())
        {
            return 1;
        }
        else if (this.m_type == CellType.Water)
        {
            return 100;
        }
        else
        {
            return 10000;
        }
    }

    public Coordinate getPos()
    {
        return new Coordinate(this.m_pos);
    }

    public boolean isVisited()
    {
        return this.m_visited;
    }
    
    public void algorithmVisit()
    {
        this.m_visited = true;
    }
    
    public boolean isWater()
    {
        if (this.m_type == CellType.Water)
        {
            return true;
        }
        
        return false;
    }
    
    public boolean moveVisit()
    {
        switch (this.m_type)
        {   
        case Floor:
            this.m_visited = true;
            break;
            
        case Treasure:
            this.m_agent.setTreasure(this.m_agent.treasure() + 1);
            this.m_type = CellType.Floor;
            this.m_agent.res().get(CellType.Treasure).remove(this.m_pos);
            
            this.m_visited = true;
            break;

        case Key:
            Integer key = this.m_agent.tools().get(ToolType.Key);
            key++;
            this.m_agent.tools().put(ToolType.Key, key);
            this.m_type = CellType.Floor;
            this.m_agent.res().get(CellType.Key).remove(this.m_pos);
            
            this.m_visited = true;
            break;

        case Axe:
            Integer Axe = this.m_agent.tools().get(ToolType.Axe);
            Axe++;
            this.m_agent.tools().put(ToolType.Axe, Axe);
            this.m_type = CellType.Floor;
            this.m_agent.res().get(CellType.Axe).remove(this.m_pos);
            
            this.m_visited = true;
            break;

        case Dynamite:
            Integer dy = this.m_agent.tools().get(ToolType.Dynamite);
            dy++;
            this.m_agent.tools().put(ToolType.Dynamite, dy);
            this.m_type = CellType.Floor;
            this.m_agent.res().get(CellType.Dynamite).remove(this.m_pos);
            
            this.m_visited = true;
            break;

        case Water:
            if (this.m_agent.tools().get(ToolType.Raft) > 0)
            {
                this.m_visited = true;
            }
            else
            {
                return false;
            }
            break;
            
        case Wall:
        case Door:
        case Tree:
            return false;

        default:
            return false;
        }

        //Agent.map_unexplored.remove(this.m_pos);
        this.m_agent.map().explore(this.m_pos);
        this.m_visited_time ++;
        
        return true;
    }

    public boolean isAccessible()
    {
        boolean accessible = false;

        switch (this.m_type)
        {
        case Floor:
        case Axe:
        case Key:
        case Dynamite:
        case Treasure:
            accessible = true;
            break;

        case Water:
            accessible = false;

            break;

        case Wall:
        case Tree:
        case Door:
            accessible = false;
            break;
        case Death:
            accessible = false;

        default:
            accessible = false;
        }

        return accessible;
    }

    public void unvisit()
    {
        this.m_visited = false;
    }

    public CellType getType()
    {
        return this.m_type;
    }

    public void setType(CellType type)
    {
        this.m_type = type;
    }
    
    public boolean equals(Object obj)
    {
        if (null == obj || !(obj instanceof Cell))
        {
            return false;
        }
        
        Cell you = (Cell)obj;
        return you.getPos().equals(this.m_pos);
    }
    
    public int hashCode()
    {
        return this.m_pos.hashCode();
    }
}
