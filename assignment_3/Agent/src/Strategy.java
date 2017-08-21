import java.util.LinkedList;
import java.util.Queue;

public abstract class Strategy
{
    protected Queue<Character> m_moves;
    protected Agent m_agent;
    
    public Strategy(Agent agent)
    {
        this.m_moves = new LinkedList<Character>();
        this.m_agent = agent;
        this.m_agent.map().unvisitAll();
    }
    
    protected int getDir(Coordinate from, Coordinate to)
    {
        int dir_x = to.getX() - from.getX();
        int dir_y = to.getY() - from.getY();
       
        int dir;          
        if (1 == dir_x)
        {
            dir = 0;
        }
        else if (-1 == dir_x)
        {
            dir = 2;
        }
        else if (1 == dir_y)
        {
            dir = 1;
        }
        else
        {
            dir = 3;
        }
        
        return dir;
    }
    
    protected void addTurns(Queue<Character> moves, int new_dir, int old_dir)
    {
        if (1 == new_dir - old_dir || -3 == new_dir - old_dir)
        {
            moves.add('R');
        }
        else if (-1 == new_dir - old_dir || 3 == new_dir - old_dir)
        {
            moves.add('L');
        }
        else if (-2 == new_dir - old_dir || 2 == new_dir - old_dir)
        {
            moves.add('R');
            moves.add('R');
        }
    }
    
    /**
     * This function aims to figure out a route the agent has to move.
     * <p>
     * This route consists of multiple forward moves, as well as right and left turns.
     */
    public abstract boolean construct();
    
    /**
     * This function tells the agent what to do next:
     * <p>
     * (Turning right, turning left, going forward, cutting a tree, unlocking a door, etc)
     * @return
     */
    public char executeNextMove()
    {
        // Try to get one movement from the queue
        Character move = this.m_moves.poll();
        if (null == move)
        {
            return 0;
        }

        if (move.equals('R'))
        {
            this.m_agent.setDir(Direction.turnRight(new Direction(this.m_agent.dir())).getDir());
        }
        else if (move.equals('L'))
        {
            this.m_agent.setDir(Direction.turnLeft(new Direction(this.m_agent.dir())).getDir());
        }
        else if (move.equals('F'))
        {
            Cell prev_cell = this.m_agent.map().getCell(this.m_agent.pos());
            this.m_agent.setPos(Coordinate.move(this.m_agent.pos(), this.m_agent.dir()));
            Cell next_cell = this.m_agent.map().getCell(this.m_agent.pos());
            
            // System.out.println(this.m_agent.pos().getX() + " " + this.m_agent.pos().getY());
            next_cell.moveVisit();
            
            if (!next_cell.isWater() && prev_cell.isWater())
            {
                this.m_agent.tools().put(ToolType.Raft, 0);
            }
        }
        else if (move.equals('U'))
        {
            Cell c = this.m_agent.map().getNeighbor(this.m_agent.pos(), this.m_agent.dir());
            this.m_agent.res().get(CellType.Door).remove(c.getPos());
            this.m_agent.getDoors().remove(c);
            
            c.setType(CellType.Floor);
        }
        else if (move.equals('C'))
        {
            Cell c = this.m_agent.map().getNeighbor(this.m_agent.pos(), this.m_agent.dir());
            
            this.m_agent.res().get(CellType.Tree).remove(c.getPos());
            this.m_agent.getTrees().remove(c);
            
            if (this.m_agent.tools().get(ToolType.Raft) == 0)
            {
                Integer raft = this.m_agent.tools().get(ToolType.Raft);
                raft++;
                this.m_agent.tools().put(ToolType.Raft, raft);
            }
            
            c.setType(CellType.Floor);
        }
        else if (move.equals('B'))
        {
            Cell c = this.m_agent.map().getNeighbor(this.m_agent.pos(), this.m_agent.dir());
            CellType target_type = c.getType();
            
            this.m_agent.res().get(target_type).remove(c.getPos());
            
            if (target_type == CellType.Wall)
            {
                this.m_agent.getWalls().remove(c);
            }
            else if (target_type == CellType.Tree)
            {
                this.m_agent.getTrees().remove(c);
            }
            else
            {
                this.m_agent.getDoors().remove(c);
            }

            Integer dy = this.m_agent.tools().get(ToolType.Dynamite);
            dy--;
            this.m_agent.tools().put(ToolType.Dynamite, dy);
            
            c.setType(CellType.Floor);
        }
        
        return move;
    }
    
    public void executeAllMoves()
    {
        Queue<Character> moves_backup = new LinkedList<Character> ();
        char move = 0;
        while((move = executeNextMove()) != 0)
        {
            moves_backup.add(move);
        }
        this.m_moves = moves_backup;
    }
    
    /**
     * This method copies a new agent and record moves to the new agent
     * @return
     */
    public Agent executeAllMovesToAgentCopy()
    {
        this.m_agent = new Agent(this.m_agent);
        Queue<Character> moves_backup = new LinkedList<Character> ();
        char move = 0;
        while((move = executeNextMove()) != 0)
        { 
            moves_backup.add(move);
        }
        this.m_moves = moves_backup;
        return this.m_agent;
    }
    
    public Queue<Character> getAllMoves()
    {
        return this.m_moves;
    }
}
