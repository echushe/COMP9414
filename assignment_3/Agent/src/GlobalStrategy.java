import java.util.Queue;

public class GlobalStrategy extends Strategy
{
    private Queue<Character> m_g_moves;
    public GlobalStrategy(Agent agent, Queue<Character> moves)
    {
        super(agent);
        this.m_g_moves = moves;
    }

    @Override
    public boolean construct()
    {
        while (!this.m_g_moves.isEmpty())
        {
            this.m_moves.add(this.m_g_moves.poll());
        }
        return true;
    }

}
