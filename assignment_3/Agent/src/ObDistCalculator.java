import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class ObDistCalculator
{
    public class ObComparator implements Comparator <ObItem>
    {
        @Override
        public int compare(ObItem arg0, ObItem arg1)
        { 
            if (arg0.cost() < arg1.cost())
            {
                return -1;
            }
            else if (arg0.cost() > arg1.cost())
            {
                return 1;
            }
            
            return 0;
        }
    }
    
    public class ObItem
    {
        private int m_e_cost;
        private int m_cost;
        private Cell m_cell;
        private ObItem m_parent;
        
        public ObItem(Cell cell)
        {
            this.m_cell = cell;
            this.m_e_cost = 0;
            this.m_cost = 0;
            this.m_parent = null;
            
            switch (cell.getType())
            {
            case Wall:
            case Tree:
            case Door:
                this.m_e_cost = 1;
                break;
            default:
            }
        }
        
        Cell getCell()
        {
            return this.m_cell;
        }
        
        int cost()
        {
            return this.m_cost;
        }
        
        void setCost(int c)
        {
            this.m_cost = c;
        }
        
        int eCost()
        {
            return this.m_e_cost;
        }
        
        public boolean equals(Object other)
        {
            if (null == other || !(other instanceof ObItem))
            {
                return false;
            }
            
            ObItem o_item = (ObItem)other;
            
            return this.m_cell.getPos().equals(o_item.m_cell.getPos());
        }
        
        public void setParent(ObItem parent)
        {
            this.m_parent = parent;
        }
        
        public ObItem getParent()
        {
            return this.m_parent;
        }
    }
    
    private Agent m_agent;
    private Coordinate m_pos1;
    private Coordinate m_pos2;
    private Queue<ObItem> m_queue;
    private Map<Coordinate, ObItem> m_closed;
    private Map<Coordinate, ObItem> m_all_items;
    
    public ObDistCalculator(Agent agent, Coordinate pos1, Coordinate pos2)
    {
        this.m_agent = agent;
        this.m_pos1 = pos1;
        this.m_pos2 = pos2;
        this.m_queue = new PriorityQueue<ObItem> (new ObComparator());
        this.m_closed = new HashMap<Coordinate, ObItem> ();
        this.m_all_items = new HashMap<Coordinate, ObItem> ();
        
        // Create search items of all map cells
        for (Coordinate pos : this.m_agent.map().getAllPos())
        {
            Cell cell = this.m_agent.map().getCell(pos);
            ObItem new_item = new ObItem(cell);
            this.m_all_items.put(pos, new_item);
        }
    }
    
    public int calculate()
    {
        this.m_queue.add(new ObItem(this.m_agent.map().getCell(this.m_pos1)));
        
        while (this.m_queue.size() > 0 && this.m_queue.peek().getCell().getPos().manhattan(m_pos2) > 0)
        {
            ObItem current_item = this.m_queue.poll();
            Cell current_cell = current_item.getCell();
            this.m_closed.put(current_cell.getPos(), current_item);         
            
            for (int dir = 0; dir < 4; dir++)
            {
                Cell neighbor = this.m_agent.map().getNeighbor(current_cell, dir);

                if (null != neighbor)
                {
                    ObItem neighbor_item = this.m_all_items.get(neighbor.getPos());

                    int new_cost = current_item.cost() + neighbor_item.eCost();

                    if (this.m_queue.contains(neighbor_item) && new_cost < neighbor_item.cost())
                    {
                        this.m_queue.remove(neighbor_item);
                    }

                    if (this.m_closed.containsKey(neighbor.getPos()) && new_cost < neighbor_item.cost())
                    {
                        this.m_closed.remove(neighbor.getPos());
                    }

                    if (!this.m_queue.contains(neighbor_item) && !this.m_closed.containsKey(neighbor.getPos()))
                    {
                        neighbor_item.setCost(new_cost);
                        this.m_queue.add(neighbor_item);
                        neighbor_item.setParent(current_item);
                    }
                }
            }
        }

        if (this.m_queue.isEmpty())
        {
            // System.out.println("No solution of cost search is found!");
            // This means no solution is found
            return 1000000;
        }

        ObItem goal_item = this.m_queue.poll();
        return goal_item.cost();
    }
}
