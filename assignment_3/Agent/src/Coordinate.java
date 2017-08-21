import java.util.Objects;

public class Coordinate
{
    private int m_x;
    private int m_y;

    public Coordinate(int x, int y)
    {
        this.m_x = x;
        this.m_y = y;
    }

    public Coordinate(Coordinate pos)
    {
        this.m_x = pos.m_x;
        this.m_y = pos.m_y;
    }

    public int getX()
    {
        return this.m_x;
    }
    
    public int getY()
    {
        return this.m_y;
    }

    public static Coordinate move(Coordinate pos, Direction direction)
    {
        int new_x = pos.m_x + direction.getX();
        int new_y = pos.m_y + direction.getY();
        
        return new Coordinate(new_x, new_y);
    }

    public static Coordinate move(Coordinate pos, int dir)
    {
        Direction direction = new Direction(dir);
        return move(pos, direction);
    }

    public Coordinate compare(Coordinate co)
    {
        return new Coordinate(this.m_x - co.m_x, this.m_y - co.m_y);
    }

    public int manhattan(Coordinate co)
    {
        return Math.abs(this.m_x - co.m_x) + Math.abs(this.m_y - co.m_y);
    }
    
    public boolean equals(Object obj)
    {
        if (null == obj || !(obj instanceof Coordinate))
        {
            return false;
        }
        
        Coordinate you = (Coordinate)obj;
        if (you.m_x != this.m_x || you.m_y != this.m_y)
        {
            return false;
        }
        

        //System.out.println("The same coordinate");

        
        return true;
    }
    
    public int hashCode()
    {
        return Objects.hash(this.m_x, this.m_y);
    }
    
    public String toString()
    {
        return "(" + m_x +", "+ m_y + ")";
    }
}
