
public class Blast extends Strategy
{
    private Coordinate m_target_pos;

    public Blast(Agent agent, Coordinate target)
    {
        super(agent);
        this.m_target_pos = target;
    }

    @Override
    public boolean construct()
    {
        Cell target_cell = this.m_agent.map().getCell(this.m_target_pos);
        CellType target_type = target_cell.getType();
        if (this.m_agent.pos().manhattan(this.m_target_pos) != 1
                || (target_type != CellType.Door && target_type != CellType.Tree && target_type != CellType.Wall)
                || this.m_agent.tools().get(ToolType.Dynamite) == 0)
        {
            return false;
        }

        int new_dir = this.getDir(this.m_agent.pos(), this.m_target_pos);
        this.addTurns(this.m_moves, new_dir, this.m_agent.dir());
        this.m_moves.add('B');
        return true;
    }

}
