
public class Direction
{
    private int m_dir;
    private int m_x;
    private int m_y;

    public Direction(int dir)
    {
        this.m_dir = dir;
        switch (dir)
        {
        case 0:
            this.m_x = 1;
            this.m_y = 0;
            break;
        case 1:
            this.m_x = 0;
            this.m_y = 1;
            break;
        case 2:
            this.m_x = -1;
            this.m_y = 0;
            break;
        case 3:
            this.m_x = 0;
            this.m_y = -1;
            break;
        default:
            this.m_x = 1;
            this.m_y = 0;
            this.m_dir = 0;
        }
    }

    public Direction(Direction dir)
    {
        this.m_dir = dir.m_dir;
        this.m_x = dir.m_x;
        this.m_y = dir.m_y;
    }

    public int getX()
    {
        return this.m_x;
    }

    public int getY()
    {
        return this.m_y;
    }
    
    public int getDir()
    {
        return this.m_dir;
    }

    public static Direction turnRight(Direction direction)
    {
        int dir = 0;
        if (3 == direction.m_dir)
        {
            dir = 0;
        }
        else
        {
            dir = direction.m_dir + 1;
        }

        return new Direction(dir);
    }

    public static Direction turnLeft(Direction direction)
    {
        int dir = 0;
        if (0 == direction.m_dir)
        {
            dir = 3;
        }
        else
        {
            dir = direction.m_dir - 1;
        }

        return new Direction(dir);
    }

    public boolean equals(Direction other)
    {
        if (this.m_x == other.m_x && this.m_y == other.m_y)
        {
            return true;
        }

        return false;
    }
}
