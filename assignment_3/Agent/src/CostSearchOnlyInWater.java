import java.util.Stack;

public class CostSearchOnlyInWater extends CostSearch
{
    public CostSearchOnlyInWater(Agent agent, Coordinate goal_pos)
    {
        super(agent, goal_pos);
        
        Cell start_cell = agent.map().getCell(agent.pos());
        
        SearchItem start_i = this.m_all_items.get(agent.pos());
        
        if (this.m_queue.isEmpty() && start_cell.isWater())
        {
            this.m_queue.add(start_i);
        }
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

                if (null != neighbor && neighbor.isWater())
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
            // System.out.println("No solution of cost search via water is found!");
            // This means no solution is found
            return null;
        }

        SearchItem goal_item = this.m_queue.poll();
        
        return goal_item;
    }

}