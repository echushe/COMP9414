import java.util.Stack;

public class CostSearchIncludingWater extends CostSearch
{
    private int m_raft;
    public CostSearchIncludingWater(Agent agent, Coordinate goal_pos)
    {
        super(agent, goal_pos);
        
        Cell start_cell = agent.map().getCell(agent.pos());
        
        SearchItem start_i = this.m_all_items.get(agent.pos());
        
        if (this.m_queue.isEmpty() && start_cell.isWater())
        {
            this.m_queue.add(start_i);
        }
        
        this.m_raft = agent.tools().get(ToolType.Raft);
        start_i.setRaft(this.m_raft);
    }

    @Override
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

                if (null != neighbor && (neighbor.isAccessible() || neighbor.isWater()))
                {
                    SearchItem neighbor_item = this.m_all_items.get(neighbor.getPos());
                    
                    if (current_item.getRaft() > 0 && current_cell.isWater() && !neighbor.isWater())
                    {
                        neighbor_item.setRaft(0);
                    }
                    else
                    {
                        neighbor_item.setRaft(current_item.getRaft());
                    }
                    
                    int new_cost = current_item.getCost();
                    if (current_item.getRaft() == 0 && neighbor.isWater())
                    {
                        new_cost += 10000;
                    }
                    else
                    {
                        new_cost += neighbor.getCost();
                    }

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
            // System.out.println("No solution of cost search via water is found!");
            // This means no solution is found
            return null;
        }

        SearchItem goal_item = this.m_queue.poll();
        
        return goal_item;
    }
    
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
            
            if (next_cell.isWater() && this.m_raft == 0)
            {
                return false;
            }
            
            if (cell.isWater() && !next_cell.isWater())
            {
                // Going from water to land
                this.m_raft = 0;
            }
            
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

}
