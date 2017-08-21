import java.util.Comparator;
import java.util.PriorityQueue;

public class CostSearch extends BreadthFirstSearch
{
    public class CostComparator implements Comparator<SearchItem>
    {
        @Override
        public int compare(SearchItem arg0, SearchItem arg1)
        {
            if (arg0.getCost() < arg1.getCost())
            {
                return -1;
            }
            else if (arg0.getCost() > arg1.getCost())
            {
                return 1;
            }

            return 0;
        }
    }

    public CostSearch(Agent agent, Coordinate goal_pos)
    {
        super(agent, goal_pos);
        this.m_queue = new PriorityQueue<SearchItem>(new CostComparator());

        Cell start_cell = agent.map().getCell(agent.pos());
        if (start_cell.isAccessible())
        {
            this.m_queue.add(this.m_all_items.get(agent.pos()));
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

                if (null != neighbor && neighbor.isAccessible())
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
            // System.out.println("No solution of cost search is found!");
            // This means no solution is found
            return null;
        }

        SearchItem goal_item = this.m_queue.poll();

        return goal_item;
    }

}
