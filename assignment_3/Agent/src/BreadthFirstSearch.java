import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;


public abstract class BreadthFirstSearch extends Strategy
{
    public class SearchItem
    {
        private SearchItem m_parent;
        private Coordinate m_goal_pos = null;
        private Cell m_cell = null;
        private int m_raft = 0;
        /**
         * g(x) of heuristic
         */
        private int m_cost = 0;
        /**
         * h(x) of heuristic
         */
        private int m_dist = 0;
        
        public SearchItem(Cell cell, Coordinate goal_pos)
        {
            this.m_cell = cell;
            this.m_goal_pos = goal_pos;
            this.m_parent = null;
            
            this.m_dist = goal_pos.manhattan(this.m_cell.getPos());
            this.m_cost = 0;
            this.m_raft = 0;
        }
        
        public Cell getCell()
        {
            return this.m_cell;
        }
        
        public int getRaft()
        {
            return this.m_raft;
        }
        
        public void setRaft(int r)
        {
            this.m_raft = r;
        }
        
        public Coordinate getGoalPos()
        {
            return this.m_goal_pos;
        }
     
        public int getDistToGoal()
        {
            return this.m_dist;
        }
        
        public void setParent(SearchItem parent)
        {
            this.m_parent = parent;
        }
        
        public SearchItem getParent()
        {
            return this.m_parent;
        }
        
        public int getCost()
        {
            return this.m_cost;
        }
        
        public void setCost(int cost)
        {
            this.m_cost = cost;
        }
        
        public int hashCode()
        {
            return Objects.hash(this.m_cell.getPos());
        }
        
        public boolean equals(Object obj)
        {
            if (null == obj || !(obj instanceof SearchItem))
            {
                return false;
            }

            SearchItem you = (SearchItem) obj;

            boolean result = you.getCell().getPos().equals(this.m_cell.getPos());
            
            //if(result)
            //{
            //    System.out.println("The same item");
            //}
            
            return result;
        }
        
    }

    /**
     * 
     */
    protected Queue<SearchItem> m_queue = null;
    /**
     * 
     */
    protected Map<Coordinate, SearchItem> m_closed = null;
    /**
     * 
     */
    protected Map<Coordinate, SearchItem> m_all_items = null;

    protected Coordinate m_goal_pos = null;

    public BreadthFirstSearch(Agent agent, Coordinate goal_pos)
    {
        super(agent);

        this.m_goal_pos = goal_pos;
        this.m_closed = new HashMap<Coordinate, SearchItem>();
        this.m_all_items = new HashMap<Coordinate, SearchItem>();

        // Create search items of all map cells
        for (Coordinate pos : this.m_agent.map().getAllPos())
        {
            Cell cell = this.m_agent.map().getCell(pos);
            SearchItem new_item = new SearchItem(cell, this.m_goal_pos);
            this.m_all_items.put(pos, new_item);
        }
    }

    protected abstract SearchItem searchMechanism();

    @Override
    public boolean construct()
    {
        SearchItem goal_item = this.searchMechanism();
        if (null == goal_item)
        {
            return false;
        }
        
        SearchItem parent = goal_item;       
        Stack<Cell> cell_stack = new Stack<Cell> ();
        while (parent != null)
        {
            parent.getCell().algorithmVisit(); 
            cell_stack.add(parent.getCell());
            parent = parent.getParent();
        }
        
        // this.m_agent.map().printRouteMap();
        
        int agent_dir = this.m_agent.dir();
        while (cell_stack.size() > 1)
        {
            Cell cell = cell_stack.pop();
            Cell next_cell = cell_stack.peek();
            
            Coordinate pos = cell.getPos();
            Coordinate next_pos = next_cell.getPos();
            
            int dir = getDir(pos, next_pos);      
            this.addTurns(this.m_moves, dir, agent_dir);
            this.m_moves.add('F');
            
            agent_dir = dir;
        }
        
        /*
        Globals.map.printMap();
        
        for (Character move : this.m_moves)
        {
            System.out.println(move);
        }
        System.out.println();
        */
        
        return true;
    }
    
    public Coordinate getGoalPos()
    {
        return this.m_goal_pos;
    }
}
