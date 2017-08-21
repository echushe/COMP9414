
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * The agent will randomly visit the map if the agent has not made any decision
 * to do something yet.
 * 
 * @author Chunnan Sheng
 *
 */
public class Exploration extends Strategy
{
    public class StackItem
    {
        private Cell m_cell;
        /**
         * This set records directions that could be used for search.
         * <p>
         * If the direction is used in search, it will be deleted from this map.
         */
        private Set<Integer> m_uv;

        private int m_dir_coming_from;
        private int m_dir_going_to;

        /**
         * 
         * @param cell
         * @param prev_dir:
         *            The direction of the agent coming into this cell
         */
        public StackItem(Cell cell, int dir)
        {
            this.m_cell = cell;
            this.m_uv = new HashSet<Integer>();
            this.m_uv.add(0);
            this.m_uv.add(1);
            this.m_uv.add(2);
            this.m_uv.add(3);
            this.m_dir_coming_from = dir;
            this.m_dir_going_to = dir;
        }

        public Cell getCell()
        {
            return this.m_cell;
        }
        
        public void setDirComingFrom(int dir)
        {
            this.m_dir_coming_from = dir;
        }

        public int getDirComingFrom()
        {
            return this.m_dir_coming_from;
        }

        public void setDirGoingTo(int dir)
        {
            this.m_dir_going_to = dir;
        }

        public int getDirGoingTo()
        {
            return this.m_dir_going_to;
        }

        public Set<Integer> getUnvisitedDirs()
        {
            return this.m_uv;
        }
    }
    
    public class DepthFirstSearchStack
    {
        private ArrayList<StackItem> m_data;

        public DepthFirstSearchStack()
        {
            this.m_data = new ArrayList<StackItem>();
        }

        public StackItem get(int index)
        {
            return this.m_data.get(index);
        }

        public StackItem pop()
        {
            if (this.m_data.size() > 0)
            {
                StackItem removed = this.m_data.remove(this.m_data.size() - 1);
                return removed;
            }
            else
            {
                return null;
            }
        }

        public StackItem peak()
        {
            if (this.m_data.size() > 0)
            {
                return this.get(this.m_data.size() - 1);
            }

            return null;
        }

        public void push(StackItem item)
        {
            if (this.m_data.size() > 0)
            {
                StackItem top = this.get(this.m_data.size() - 1);
                top.setDirGoingTo(item.getDirComingFrom());
            }

            this.m_data.add(item);
        }

        public int size()
        {
            return this.m_data.size();
        }
    }
    
    
    protected DepthFirstSearchStack m_stack = null;
    protected int m_agent_dir = 0;
    
    //private int m_farthest_dis = 0;
    // private Coordinate m_farthest_pos = null;

    public Exploration(Agent agent)
    {
        super(agent);

        Cell start_cell = this.m_agent.map().getCell(agent.pos());
        start_cell.algorithmVisit();

        this.m_stack = new DepthFirstSearchStack();
        this.m_agent_dir = agent.dir();
        this.m_stack.push(new StackItem(start_cell, this.m_agent_dir));
    }

    @Override
    public boolean construct()
    {
        /*
        int rand = Globals.random.nextInt(50);
        int index = 0;
        while (this.m_stack.size() > 0 && index <= rand)
        {
            // this.m_agent.map().printMap();
            visitNextCell();
            index ++;
        }
        */
        
        while (this.m_stack.size() > 0)
        {
            // this.m_agent.map().printMap();
            visitNextCell();
        }

        return true;
    }

    protected void visitNextCell()
    {
        if (0 == this.m_stack.size())
        {
            return;
        }
        // Pick up top of the stack
        StackItem top_item = this.m_stack.peak();

        //
        int dir_from = top_item.getDirComingFrom();

        // Check if this Cell has unvisited sub dirs
        Set<Integer> unvisited_dirs = top_item.getUnvisitedDirs();

        // Current node has unvisited sub dirs
        Cell next = null;
        int dir_to = 0;

        while (unvisited_dirs.size() > 0)
        {
            dir_to = Globals.random.nextInt(4);
            while (!unvisited_dirs.contains(dir_to))
            {
                dir_to = Globals.random.nextInt(4);
            }

            unvisited_dirs.remove(dir_to);

            // Get the Cell that corresponds to the dir.
            // This Cell may be NULL.
            // One reason is that this Cell is outside of the map, or
            // this Cell belongs to undiscovered area.
            Cell check_next = this.m_agent.map().getNeighbor(top_item.getCell(), dir_to);

            // If the sub Cell is not null, is accessible, and is not visited,
            // then, the
            // loop will stop.
            if (null != check_next && !check_next.isVisited() && check_next.isAccessible())
            {
                next = check_next;
                break;
            }
        }

        // There is a visible sub dir
        if (null != next)
        {
            this.m_stack.push(new StackItem(next, dir_to));
            // Set the next Cell as visited
            next.algorithmVisit();

            this.addTurns(this.m_moves, dir_to, this.m_agent_dir);
            this.m_moves.add('F');
            this.m_agent_dir = dir_to;
        }
        // All possible sub dirs are visited
        else
        {
            this.m_stack.pop();

            // If the popped stack item is the last item, the random move is finished.
            // It is insane to move back if the stack is already empty.
            if (this.m_stack.size() > 0)
            {
                int dir_back = (dir_from + 2) % 4;

                this.addTurns(this.m_moves, dir_back, this.m_agent_dir);
                this.m_moves.add('F');
                this.m_agent_dir = dir_back;
            }
        }
    }
    

    public static void main(String[] args)
    {
        BufferedReader br = null;
        Agent agent = new Agent();
        try
        {
            // Read all the text from the file
            br = new BufferedReader(new FileReader("my.in"));
            String line = null;

            ArrayList<String> absolute_view = new ArrayList<String>();
            while ((line = br.readLine()) != null)
            {
                absolute_view.add(line);
            }
            
            agent.map().update(absolute_view);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
        agent.map().printMap();

        Strategy strategy = new Exploration(agent);
        strategy.construct();
        strategy.executeAllMoves();
        
        agent.map().printRouteMap();
        
        strategy = new Exploration(agent);
        strategy.construct();
        strategy.executeAllMoves();       
        
        agent.map().printRouteMap();
        
        strategy = new Exploration(agent);
        strategy.construct();
        strategy.executeAllMoves();       
        
        agent.map().printRouteMap();
        
        strategy = new Exploration(agent);
        strategy.construct();
        strategy.executeAllMoves();       
        
        agent.map().printRouteMap();
        
        strategy = new Exploration(agent);
        strategy.construct();
        strategy.executeAllMoves();       
        
        agent.map().printRouteMap();
        strategy = new Exploration(agent);
        strategy.construct();
        strategy.executeAllMoves();       
        
        agent.map().printRouteMap();
    }
}

