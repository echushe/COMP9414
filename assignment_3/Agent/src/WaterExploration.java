import java.util.Set;

public class WaterExploration extends Exploration
{

    // We assume that the agent is already in water
    // When water exploration starts
    public WaterExploration(Agent agent)
    {
        super(agent);
        // TODO Auto-generated constructor stub
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
            if (null != check_next && !check_next.isVisited() && check_next.isWater())
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

}
