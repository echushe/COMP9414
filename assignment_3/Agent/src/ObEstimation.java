import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;


public class ObEstimation extends CostSearchIncludingWater
{
    protected Queue<Cell> m_obstacles;
    protected Queue<Cell> m_accessibles;
    protected Coordinate m_first_ob_pos = null;
    private int m_rafts;
    
    public ObEstimation(Agent agent, Coordinate goal_pos)
    {
        super(agent, goal_pos);
        this.m_obstacles = new LinkedList<Cell> ();
        this.m_accessibles = new LinkedList<Cell> ();
        this.m_rafts = agent.tools().get(ToolType.Raft);
    }

    protected SearchItem searchMechanism()
    {
        while (this.m_queue.size() > 0 && this.m_queue.peek().getDistToGoal() > 0)
        {
            SearchItem current_item = this.m_queue.poll();
            Cell current_cell = current_item.getCell();
            this.m_closed.put(current_cell.getPos(), current_item);         
            
            for (int dir = 0; dir < 4; dir++)
            {
                Cell neighbor = this.m_agent.map().getNeighbor(current_cell, dir);

                if (null != neighbor)
                {
                    SearchItem neighbor_item = this.m_all_items.get(neighbor.getPos());

                    int new_cost = current_item.getCost() + neighbor.getCost();

                    if (this.m_queue.contains(neighbor_item) && new_cost < neighbor_item.getCost())
                    {
                        this.m_queue.remove(neighbor_item);
                    }

                    if (this.m_closed.containsKey(neighbor.getPos()) && new_cost < neighbor_item.getCost())
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
            //System.out.println("Estimation -including- water No solution of search is found!");
            // This means no solution is found
            return null;
        }

        SearchItem goal_item = this.m_queue.poll();
        
        return goal_item;
    }
    
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
            cell_stack.add(parent.getCell());
            parent = parent.getParent();
        }
        
        boolean in_water = false;
        while (cell_stack.size() > 0)
        {
            Cell cell = cell_stack.peek();
            
            if (cell.getType() == CellType.Water)
            {
                if (in_water == true)
                {
                    // Already in water
                }
                else // From land to water
                {
                    if (this.m_rafts > 0)
                    {
                        // You can go into water now!
                    }
                    else
                    {
                        // You cannot go into water!
                        // This cell is an obstacle
                        this.m_first_ob_pos = cell.getPos();
                        break;
                    }
                    
                    in_water = true;
                }
            }
            else if (cell.isAccessible())
            {
                if (in_water) // You are now going out of water
                {
                    in_water = false;
                    this.m_rafts = 0;
                }
            }
            else if (!cell.isAccessible())
            {
                // The last one is an obstacle
                this.m_first_ob_pos = cell.getPos();
                break;
            }

            cell_stack.pop();
            this.m_accessibles.add(cell);
            cell.assessVisit();
        }
        
        // Record obstacles
        while (cell_stack.size() > 0)
        {
            Cell cell = cell_stack.pop();
            cell.assessVisit();
            if (!cell.isAccessible())
            {
                this.m_obstacles.add(cell);
            }
        }
        
        int agent_dir = this.m_agent.dir();
        while (this.m_accessibles.size() > 1)
        {
            Cell cell = this.m_accessibles.poll();
            Cell next_cell = this.m_accessibles.peek();
            
            Coordinate pos = cell.getPos();
            Coordinate next_pos = next_cell.getPos();
            
            int dir = getDir(pos, next_pos);
            this.addTurns(this.m_moves, dir, agent_dir);
            
            agent_dir = dir;
            next_cell.algorithmVisit();     
            this.m_moves.add('F');
        }
        
        if (this.m_accessibles.size() == 1 && this.m_first_ob_pos != null)
        {
            int dir = getDir(this.m_accessibles.peek().getPos(), this.m_first_ob_pos);
            this.addTurns(this.m_moves, dir, agent_dir);
            agent_dir = dir;   
        }
        
        /*
        System.out.print("Obstacles: ");
        for (Cell cell : this.m_obstacles)
        {
            System.out.print(" " + Globals.cellType2Char(cell.getType()));
        }
        System.out.println();
        */
        
        return true;
    }
    
    public Queue<Cell> getObstacles()
    {   
        return this.m_obstacles;
    }
    
    public int getObstacleCosts()
    {
        int cost = 0;
        for (Cell c : this.m_obstacles)
        {
            cost += c.getCost();
        }
        return cost;
    }

}
