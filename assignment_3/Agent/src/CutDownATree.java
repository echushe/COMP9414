
/**
 * 
 * @author Yufei
 *
 */
public class CutDownATree extends Strategy
{
    private Coordinate m_tree_pos;

    public CutDownATree(Agent agent, Coordinate tree)
    {
        super(agent);
        this.m_tree_pos = tree;
    }

    @Override
    public boolean construct()
    {
        // TODO Auto-generated method stub
        // Cell curr_cell = this.m_agent.map().getCell(this.m_agent.pos());
        Cell tree_cell = this.m_agent.map().getCell(this.m_tree_pos);

        if (this.m_agent.pos().manhattan(this.m_tree_pos) != 1 || tree_cell.getType() != CellType.Tree
                || this.m_agent.tools().get(ToolType.Axe) == 0)
        {
            return false;
        }

        int new_dir = this.getDir(this.m_agent.pos(), this.m_tree_pos);
        this.addTurns(this.m_moves, new_dir, this.m_agent.dir());
        this.m_moves.add('C');

        return true;
    }

}
