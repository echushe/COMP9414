/**
 * 
 * @author Yufei
 *
 */
public class OpenADoor extends Strategy
{
    private Coordinate m_door_pos;

    public OpenADoor(Agent agent, Coordinate door)
    {
        super(agent);
        m_door_pos = door;
    }

    @Override
    public boolean construct()
    {
        Cell door_cell = this.m_agent.map().getCell(this.m_door_pos);
        if (this.m_agent.pos().manhattan(this.m_door_pos) != 1 || door_cell.getType() != CellType.Door
                || this.m_agent.tools().get(ToolType.Key) == 0)
        {
            return false;
        }
        
        int new_dir = this.getDir(this.m_agent.pos(), this.m_door_pos);
        this.addTurns(this.m_moves, new_dir, this.m_agent.dir());
        this.m_moves.add('U');
        
        return true;
    }

}
