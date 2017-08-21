import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * 
 * @author Chunnan Sheng
 * @author Yufei Zhang
 *
 */
public class Globals
{
    public static Agent global_agent = null;
    public static Random random = new Random(1);

    /**
     * Current Strategy
     */
    public static Strategy current_strategy = null;
    
    public static DecisionMaker  decision_maker = null;

    /**
     * 
     */
    // public static DecisionMaker decision_maker = null;

    public static void initialize(Agent agent)
    {
        global_agent = agent;
        decision_maker = new DecisionMaker(global_agent);
    }

    public static CellType char2CellType(char ch)
    {
        CellType type = null;
        switch (ch)
        {
        case ' ':
            type = CellType.Floor;
            break;
        case '*':
            type = CellType.Wall;
            break;
        case '-':
            type = CellType.Door;
            break;
        case 'T':
        case 't':
            type = CellType.Tree;
            break;
        case '~':
            type = CellType.Water;
            break;
        case 'K':
        case 'k':
            type = CellType.Key;
            break;
        case '$':
            type = CellType.Treasure;
            break;
        case 'A':
        case 'a':
            type = CellType.Axe;
            break;
        case 'D':
        case 'd':
            type = CellType.Dynamite;
            break;
        case '.':
            type = CellType.Death;
            break;
        default:
            type = CellType.Floor;
        }

        return type;
    }

    public static char cellType2Char(CellType type)
    {
        char ch = 0;
        switch (type)
        {
        case Floor:
            ch = ' ';
            break;
        case Wall:
            ch = '*';
            break;
        case Door:
            ch = '-';
            break;
        case Tree:
            ch = 'T';
            break;
        case Water:
            ch = '~';
            break;
        case Key:
            ch = 'k';
            break;
        case Dynamite:
            ch = 'd';
            break;
        case Treasure:
            ch = '$';
            break;
        case Axe:
            ch = 'a';
            break;
        case Death:
            ch = '.';
            break;
        default:
            ch = ' ';
        }

        return ch;
    }

}
