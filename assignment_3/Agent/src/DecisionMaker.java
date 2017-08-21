import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class DecisionMaker
{
    enum OptionType
    {
        start, go_back_home, go_to_treasure, go_to_key, go_to_axe, go_to_dynamite, cut_down_tree, unlock_door, blast_wall, blast_tree, blast_door, explore_water, explore_land
    }

    public class OptionPriComparator implements Comparator<DecisionStackItem>
    {
        @Override
        public int compare(DecisionStackItem arg0, DecisionStackItem arg1)
        {
            if (arg0.getPriority() < arg1.getPriority())
            {
                return -1;
            }
            else if (arg0.getPriority() > arg1.getPriority())
            {
                return 1;
            }

            return 0;
        }
    }

    public class OptionMarkComparator implements Comparator<DecisionStackItem>
    {
        @Override
        public int compare(DecisionStackItem arg0, DecisionStackItem arg1)
        {
            if (arg0.getMark() > arg1.getMark())
            {
                return -1;
            }
            else if (arg0.getMark() < arg1.getMark())
            {
                return 1;
            }

            return 0;
        }
    }

    public class DecisionStackItem
    {
        private Agent m_agent;
        private Queue<Character> m_moves;
        private Queue<DecisionStackItem> m_next_options;
        private Set<Coordinate> m_ob_for_next_gen;
        private OptionType m_type;

        private int m_priority;
        private int m_mark;

        public DecisionStackItem(Agent agent, Queue<Character> moves, OptionType type)
        {
            this.m_agent = agent;
            this.m_moves = moves;
            this.m_next_options = new PriorityQueue<DecisionStackItem>(new OptionPriComparator());
            this.m_ob_for_next_gen = new HashSet<Coordinate>();
            this.m_type = type;

            this.m_priority = 1;

            // Calculate mark of this option

            int keys = agent.tools().get(ToolType.Key);
            int axes = agent.tools().get(ToolType.Axe);
            int dynamites = agent.tools().get(ToolType.Dynamite);
            int rafts = agent.tools().get(ToolType.Raft);


            int trees = 0;
            for (Coordinate tree : agent.res().get(CellType.Tree))
            {
                CostSearch cs = new CostSearchIncludingWater(agent, tree);
                if (cs.construct())
                {
                    trees++;
                    break;
                }
            }

            int key = keys > 0 ? 1 : 0;
            int axe = axes > 0 ? 1 : 0;
            int dynamite = dynamites > 0 ? 1 : 0;
            int raft = rafts > 0 ? 1 : 0;
            int tree = trees > 0 ? 1 : 0;

            int num_of_tools = keys + axes + dynamites + rafts;
            int kinds_of_tools = key + axe + dynamite + raft * tree;
            
            Cell agent_cell = this.m_agent.map().getCell(this.m_agent.pos());
            
            int new_land_mark = 1;
            if (agent_cell.getVisitedTime() == 1 && agent_cell.isAccessible())
            {
                new_land_mark = 2;
            }
            
            this.m_mark = num_of_tools * kinds_of_tools * new_land_mark;

        }

        public int getMark()
        {
            return this.m_mark;
        }

        public void addObstacle(Coordinate ob)
        {
            // System.out.println("Add obstacle: " + ob.getX() + " " +
            // ob.getY());
            this.m_ob_for_next_gen.add(ob);
        }

        public Set<Coordinate> getObstacles()
        {
            return this.m_ob_for_next_gen;
        }

        public int getPriority()
        {
            return this.m_priority;
        }

        public void setPriority(int p)
        {
            this.m_priority = p;
        }

        public OptionType getType()
        {
            return this.m_type;
        }

        public Queue<Character> getMoves()
        {
            return this.m_moves;
        }

        public Queue<DecisionStackItem> getNextOptions()
        {
            return this.m_next_options;
        }

        public void addNextOption(DecisionStackItem item)
        {
            this.m_next_options.add(item);
        }

        public Agent getAgent()
        {
            return this.m_agent;
        }

        public int getBranches()
        {
            return this.m_next_options.size();
        }
    }

    public class DecisionMakerStack
    {
        private ArrayList<DecisionStackItem> m_data;

        public DecisionMakerStack()
        {
            this.m_data = new ArrayList<DecisionStackItem>();
        }

        public DecisionStackItem get(int index)
        {
            return this.m_data.get(index);
        }

        public DecisionStackItem pop()
        {
            if (this.m_data.size() > 0)
            {
                DecisionStackItem removed = this.m_data.remove(this.m_data.size() - 1);
                return removed;
            }
            else
            {
                return null;
            }
        }

        public DecisionStackItem peek()
        {
            if (this.m_data.size() > 0)
            {
                return this.get(this.m_data.size() - 1);
            }

            return null;
        }

        public void push(DecisionStackItem item)
        {
            this.m_data.add(item);
        }

        public int size()
        {
            return this.m_data.size();
        }

        public void print()
        {
            for (int i = 0; i < this.m_data.size(); i++)
            {
                DecisionStackItem item = this.m_data.get(i);
                Coordinate agent_pos = item.getAgent().pos();
                System.out.print(item.getType() + "(" + agent_pos.getX() + "," + agent_pos.getY() + ") ");

            }
            System.out.println();
        }

        public void clear()
        {
            this.m_data.clear();
        }
    }

    int m_map_size_last_time;
    private DecisionMakerStack m_stack;
    private boolean m_game_win;
    private Agent m_agent;

    /**
     * Failed options ordered by marks
     */
    private Queue<DecisionStackItem> m_entire_option_tree;
    // private Map<OptionType>

    public DecisionMaker(Agent agent)
    {
        this.m_agent = agent;

        this.m_stack = new DecisionMakerStack();
        this.m_entire_option_tree = new PriorityQueue<DecisionStackItem>(new OptionMarkComparator());
    }

    /**
     * @return
     */
    private boolean nextDecisionPoint()
    {
        if (this.m_stack.size() == 0)
        {
            return false;
        }

        DecisionStackItem current = this.m_stack.peek();

        /*
         * System.out.println("Treasure: " + current.getAgent().treasure());
         * System.out.println("Dynamite: " +
         * current.getAgent().tools().get(ToolType.Dynamite));
         * System.out.println("Key: " +
         * current.getAgent().tools().get(ToolType.Key));
         * System.out.println("Raft: " +
         * current.getAgent().tools().get(ToolType.Raft));
         * 
         */
        // current.getAgent().map().printMap();
        // System.out.println();
        // current.getAgent().map().printRouteMap();

        this.m_stack.print();

        if (this.m_game_win)
        {
            this.m_stack.push(current.getNextOptions().poll());
            this.m_stack.print();
            return true;
        }

        // If there are no other options for the top of this stack
        if (current.getNextOptions().isEmpty())
        {
            if (current.getType() != OptionType.start)
            {
                this.m_entire_option_tree.add(current);
            }

            // Tree size that is larger than 500 is not allowed
            if (this.m_entire_option_tree.size() > 500)
            {
                this.m_stack.clear();
                return false;
            }

            this.m_stack.pop();
        }
        // If there are other options
        else
        {
            // Remove the option from the option pool
            DecisionStackItem option = current.getNextOptions().poll();

            // Push the option to top of the stack
            this.m_stack.push(option);

            // Generate sub options of this option
            // Game may win in the generation procedure
            this.generateOptions(option);
        }

        return false;
    }

    private void generateOptions(DecisionStackItem item)
    {
        Agent agent = item.getAgent();

        if (agent.treasure() > 0)
        {
            this.optionsWithTreasure(item);
        }
        else
        {
            this.optionsWithoutTreasure(item);
        }
    }

    private void optionsWithTreasure(DecisionStackItem item)
    {
        Agent agent = item.getAgent();
        boolean go_land = false;
        boolean go_water = false;
        CostSearch cs = null;

        CostSearch test = new CostSearch(agent, agent.home());
        if (test.construct())
        {
            go_land = true;
            cs = test;
        }

        if (!go_land)
        {
            test = new CostSearchIncludingWater(agent, agent.home());
            if (test.construct())
            {
                go_water = true;
                cs = test;
            }
        }

        if (!go_land)
        {
            ObEstimationOnLand obe = new ObEstimationOnLand(agent, agent.home());
            obe.construct();
            Queue<Cell> obs = obe.getObstacles();
            if (this.enoughTools(item, obs))
            {
                for (Cell c : obs)
                {
                    item.addObstacle(c.getPos());
                }
            }
        }

        if (!go_land && !go_water)
        {
            ObEstimation obe = new ObEstimation(agent, agent.home());
            obe.construct();
            Queue<Cell> obs = obe.getObstacles();
            if (this.enoughTools(item, obs))
            {
                for (Cell c : obs)
                {
                    item.addObstacle(c.getPos());
                }
            }
        }

        if (null != cs)
        {
            Agent new_agent = cs.executeAllMovesToAgentCopy();

            Queue<Character> appended_moves = new LinkedList<Character>();
            appended_moves.addAll(item.getMoves());
            appended_moves.addAll(cs.getAllMoves());
            DecisionStackItem new_option = new DecisionStackItem(new_agent, appended_moves, OptionType.go_back_home);
            new_option.setPriority(0);
            item.addNextOption(new_option);
            this.m_game_win = true;
        }
        else
        {
            this.optionsWithoutTreasure(item);
        }
    }

    private void optionsWithoutTreasure(DecisionStackItem item)
    {
        if (item.getType() == OptionType.explore_water)
        {
            return;
        }

        Agent agent = item.getAgent();
        Map<ToolType, Integer> tools = agent.tools();

        // Do going to first so that we can find out potential obstacles

        this.optionsToGetTool(item, OptionType.go_to_treasure);

        int keys = tools.get(ToolType.Key);
        int axes = tools.get(ToolType.Axe);
        int dy = tools.get(ToolType.Dynamite);
        int raft = tools.get(ToolType.Raft);

        if (keys == 0)
        {
            this.optionsToGetTool(item, OptionType.go_to_key);
        }

        if (axes == 0)
        {
            this.optionsToGetTool(item, OptionType.go_to_axe);
        }

        this.optionsToGetTool(item, OptionType.go_to_dynamite);

        if (keys > 0)
        {
            this.optionsForDoors(item, OptionType.unlock_door);
        }

        if (axes > 0)
        {
            this.optionsForTrees(item, OptionType.cut_down_tree);
        }

        if (dy > 0)
        {
            if (agent.map().size() > 400)
            {
                this.optionsWithToolsOnlyDealWithObstacles(item, OptionType.blast_wall);
                this.optionsWithToolsOnlyDealWithObstacles(item, OptionType.blast_tree);
                this.optionsWithToolsOnlyDealWithObstacles(item, OptionType.blast_door);
            }
            else
            {
                this.optionsWithTools(item, OptionType.blast_wall);
                this.optionsWithTools(item, OptionType.blast_tree);
                this.optionsWithTools(item, OptionType.blast_door);
            }
        }

        if (raft > 0)
        {
            this.optionsWithRaft(item, OptionType.explore_water);
        }

    }

    private Queue<Cell> getTargetsFromAgent(DecisionStackItem item, OptionType type)
    {
        Agent agent = item.getAgent();
        Queue<Cell> targets = null;
        switch (type)
        {
        case unlock_door:
            targets = agent.getDoors();
            break;
        case cut_down_tree:
            targets = agent.getTrees();
            break;
        case blast_wall:
            targets = agent.getWalls();
            break;
        case blast_tree:
            targets = agent.getTrees();
            break;
        case blast_door:
            targets = agent.getDoors();
            break;
        default:
        }

        return targets;
    }

    private Strategy generateOperation(Agent new_agent, OptionType type, Cell target_cell)
    {
        Strategy operation = null;

        switch (type)
        {
        case unlock_door:
            operation = new OpenADoor(new_agent, target_cell.getPos());
            break;
        case cut_down_tree:
            operation = new CutDownATree(new_agent, target_cell.getPos());
            break;
        case blast_wall:
        case blast_tree:
        case blast_door:
            operation = new Blast(new_agent, target_cell.getPos());
            break;
        default:
        }

        return operation;
    }

    private void optionsWithTools(DecisionStackItem item, OptionType type)
    {
        int b_limit = 1000;
        if (item.getType() == type)
        {
            b_limit = 1;
        }

        Agent agent = item.getAgent();
        Queue<Cell> target_cells = this.getTargetsFromAgent(item, type);

        ArrayList<Cell> list = new ArrayList<Cell>();
        for (Cell target_cell : target_cells)
        {
            list.add(target_cell);
        }

        int index = 0;
        for (Cell target_cell : list)
        {
            Coordinate target = target_cell.getPos();
            Collection<Cell> neighbors = agent.map().getAllNeighbors(target);

            Cell nei_via_land = null;
            Cell nei_via_water = null;
            CostSearch cs = null;
            for (Cell nei : neighbors)
            {
                if (null != nei_via_land)
                {
                    break;
                }
                if (!nei.isAccessible() && !nei.isWater())
                {
                    continue;
                }

                CostSearch str = new CostSearch(agent, nei.getPos());
                if (str.construct())
                {
                    nei_via_land = nei;
                    cs = str;
                }
            }

            if (null == nei_via_land)
            {
                for (Cell nei : neighbors)
                {
                    if (null != nei_via_water)
                    {
                        break;
                    }
                    if (!nei.isAccessible() && !nei.isWater())
                    {
                        continue;
                    }

                    CostSearch str = new CostSearchIncludingWater(agent, nei.getPos());
                    if (str.construct())
                    {
                        nei_via_water = nei;
                        cs = str;
                    }
                }
            }

            if (null == cs)
            {
                continue;
            }

            Queue<Character> appended_moves = new LinkedList<Character>();
            appended_moves.addAll(item.getMoves());

            Agent new_agent = cs.executeAllMovesToAgentCopy();
            appended_moves.addAll(cs.getAllMoves());

            Strategy operation = this.generateOperation(new_agent, type, target_cell);

            if (operation.construct())
            {
                operation.executeAllMoves();
                appended_moves.addAll(operation.getAllMoves());

                DecisionStackItem sub_option = new DecisionStackItem(new_agent, appended_moves, type);

                // Potential obstacles are of higher priority
                if (item.getObstacles().contains(target))
                {
                    sub_option.setPriority(2);
                }
                else if (item.getType() == type)
                {
                    sub_option.setPriority(5);
                }
                else
                {
                    sub_option.setPriority(4);
                }

                item.addNextOption(sub_option);
                index++;
                if (index >= b_limit)
                {
                    return;
                }
            }
        }
    }

    private void optionsWithToolsOnlyDealWithObstacles(DecisionStackItem item, OptionType type)
    {
        int b_limit = 1000;
        if (item.getType() == type)
        {
            b_limit = 1;
        }

        Agent agent = item.getAgent();
        Queue<Cell> target_cells = this.getTargetsFromAgent(item, type);

        ArrayList<Cell> list = new ArrayList<Cell>();
        for (Cell target_cell : target_cells)
        {
            list.add(target_cell);
        }

        int index = 0;
        for (Cell target_cell : list)
        {
            Coordinate target = target_cell.getPos();
            Set<Coordinate> obstacles = item.getObstacles();

            if (!obstacles.contains(target))
            {
                continue;
            }

            Collection<Cell> neighbors = agent.map().getAllNeighbors(target);

            Cell nei_via_land = null;
            Cell nei_via_water = null;
            CostSearch cs = null;
            for (Cell nei : neighbors)
            {
                if (null != nei_via_land)
                {
                    break;
                }
                if (!nei.isAccessible() && !nei.isWater())
                {
                    continue;
                }

                CostSearch str = new CostSearch(agent, nei.getPos());
                if (str.construct())
                {
                    nei_via_land = nei;
                    cs = str;
                }
            }

            if (null == nei_via_land)
            {
                for (Cell nei : neighbors)
                {
                    if (null != nei_via_water)
                    {
                        break;
                    }
                    if (!nei.isAccessible() && !nei.isWater())
                    {
                        continue;
                    }

                    CostSearch str = new CostSearchIncludingWater(agent, nei.getPos());
                    if (str.construct())
                    {
                        nei_via_water = nei;
                        cs = str;
                    }
                }
            }

            if (null == cs)
            {
                continue;
            }

            Queue<Character> appended_moves = new LinkedList<Character>();
            appended_moves.addAll(item.getMoves());

            Agent new_agent = cs.executeAllMovesToAgentCopy();
            appended_moves.addAll(cs.getAllMoves());

            Strategy operation = this.generateOperation(new_agent, type, target_cell);

            if (operation.construct())
            {
                operation.executeAllMoves();
                appended_moves.addAll(operation.getAllMoves());

                DecisionStackItem sub_option = new DecisionStackItem(new_agent, appended_moves, type);

                // Potential obstacles are of higher priority
                if (item.getObstacles().contains(target))
                {
                    sub_option.setPriority(2);
                }
                else if (item.getType() == type)
                {
                    sub_option.setPriority(5);
                }
                else
                {
                    sub_option.setPriority(4);
                }

                item.addNextOption(sub_option);
                index++;
                if (index >= b_limit)
                {
                    return;
                }
            }
        }
    }

    private void optionsForDoors(DecisionStackItem item, OptionType type)
    {
        Queue<Character> appended_moves = new LinkedList<Character>();
        appended_moves.addAll(item.getMoves());

        Agent new_agent = new Agent(item.getAgent());

        int unlock = 0;
        while (true)
        {
            // System.out.println("============================================================");
            if (new_agent.map().getCell(new_agent.pos()).isWater())
            {
                this.optionsWithTools(item, type);
                break;
            }

            Set<Coordinate> targets = new_agent.res().get(CellType.Door);

            ArrayList<Coordinate> list = new ArrayList<Coordinate>();
            for (Coordinate target : targets)
            {
                list.add(target);
            }

            ArrayList<Object[]> doors_to_open = new ArrayList<Object[]>();

            for (Coordinate target : list)
            {
                Cell target_cell = new_agent.map().getCell(target);
                Collection<Cell> neighbors = new_agent.map().getAllNeighbors(target);

                Cell nei_via_land = null;
                CostSearch cs = null;
                for (Cell nei : neighbors)
                {
                    if (null != nei_via_land)
                    {
                        break;
                    }
                    if (!nei.isAccessible() && !nei.isWater())
                    {
                        continue;
                    }

                    CostSearch str = new CostSearch(new_agent, nei.getPos());
                    if (str.construct())
                    {
                        nei_via_land = nei;
                        cs = str;
                    }
                }

                if (null != nei_via_land)
                {
                    Object[] tr_nei_pair = new Object[3];
                    tr_nei_pair[0] = target_cell;
                    tr_nei_pair[1] = nei_via_land;
                    tr_nei_pair[2] = cs;
                    doors_to_open.add(tr_nei_pair);
                }
            }

            if (doors_to_open.size() < 1)
            {
                if (unlock > 0)
                {
                    DecisionStackItem sub_option = new DecisionStackItem(new_agent, appended_moves, type);
                    sub_option.setPriority(3);
                    item.addNextOption(sub_option);
                }
                else
                {
                    this.optionsWithTools(item, type);
                }

                break;
            }

            for (int i = 0; i < doors_to_open.size(); i++)
            {
                Cell target_cell = (Cell) doors_to_open.get(i)[0];
                Cell nei = (Cell) doors_to_open.get(i)[1];
                CostSearch cs = new CostSearch(new_agent, nei.getPos());
                cs.construct();
                cs.executeAllMoves();
                appended_moves.addAll(cs.getAllMoves());

                Strategy operation = new OpenADoor(new_agent, target_cell.getPos());

                if (operation.construct())
                {
                    operation.executeAllMoves();
                    appended_moves.addAll(operation.getAllMoves());
                }
                unlock++;
            }
        }
    }

    private void optionsForTrees(DecisionStackItem item, OptionType type)
    {
        Queue<Character> appended_moves = new LinkedList<Character>();
        appended_moves.addAll(item.getMoves());

        Agent new_agent = new Agent(item.getAgent());

        int cut = 0;

        while (true)
        {
            if (new_agent.map().getCell(new_agent.pos()).isWater())
            {
                this.optionsWithTools(item, type);
                break;
            }

            Set<Coordinate> targets = new_agent.res().get(CellType.Tree);

            ArrayList<Coordinate> list = new ArrayList<Coordinate>();
            for (Coordinate target : targets)
            {
                list.add(target);
            }

            ArrayList<Object[]> trees_to_cut = new ArrayList<Object[]>();

            for (Coordinate target : list)
            {
                Cell target_cell = new_agent.map().getCell(target);
                Collection<Cell> neighbors = new_agent.map().getAllNeighbors(target);

                Cell nei_via_land = null;
                CostSearch cs = null;
                for (Cell nei : neighbors)
                {
                    if (null != nei_via_land)
                    {
                        break;
                    }
                    if (!nei.isAccessible() && !nei.isWater())
                    {
                        continue;
                    }

                    CostSearch str = new CostSearch(new_agent, nei.getPos());
                    if (str.construct())
                    {
                        nei_via_land = nei;
                        cs = str;
                    }
                }

                if (null != nei_via_land)
                {
                    Object[] tr_nei_pair = new Object[3];
                    tr_nei_pair[0] = target_cell;
                    tr_nei_pair[1] = nei_via_land;
                    tr_nei_pair[2] = cs;
                    trees_to_cut.add(tr_nei_pair);
                }
            }

            if (trees_to_cut.size() < 5)
            {
                if (cut > 0)
                {
                    DecisionStackItem sub_option = new DecisionStackItem(new_agent, appended_moves, type);
                    sub_option.setPriority(3);
                    item.addNextOption(sub_option);
                }
                else
                {
                    this.optionsWithTools(item, type);
                }

                break;
            }

            for (int i = 0; i < trees_to_cut.size() - 4; i++)
            {
                Cell target_cell = (Cell) trees_to_cut.get(i)[0];
                Cell nei = (Cell) trees_to_cut.get(i)[1];
                CostSearch cs = new CostSearch(new_agent, nei.getPos());
                cs.construct();
                cs.executeAllMoves();
                appended_moves.addAll(cs.getAllMoves());

                Strategy operation = new CutDownATree(new_agent, target_cell.getPos());

                if (operation.construct())
                {
                    operation.executeAllMoves();
                    appended_moves.addAll(operation.getAllMoves());
                }
                cut++;
            }
        }
    }

    private boolean enoughTools(DecisionStackItem item, Queue<Cell> ob)
    {
        if (item.getAgent().map().size() > 1000)
        {
            Agent agent = item.getAgent();
            int dynamite = agent.tools().get(ToolType.Dynamite);
            int dynamite_needed = 0;
            for (Cell c : ob)
            {
                if (c.getType() == CellType.Wall)
                {
                    dynamite_needed++;
                }
            }

            if (dynamite < dynamite_needed)
            {
                return false;
            }
        }

        return true;
    }

    private void optionsToGetTool(DecisionStackItem item, OptionType type)
    {
        int b_limit = 1000;
        if (item.getType() == type)
        {
            b_limit = 1;
        }

        Agent agent = item.getAgent();
        Set<Coordinate> tools = null;
        switch (type)
        {
        case go_to_treasure:
            tools = agent.res().get(CellType.Treasure);
            break;
        case go_to_key:
            tools = agent.res().get(CellType.Key);
            break;
        case go_to_axe:
            tools = agent.res().get(CellType.Axe);
            break;
        case go_to_dynamite:
            tools = agent.res().get(CellType.Dynamite);
            break;
        default:
        }

        if (null == tools)
        {
            return;
        }

        ArrayList<Coordinate> list = new ArrayList<Coordinate>();
        for (Coordinate tool : tools)
        {
            list.add(tool);
        }

        int index = 0;
        for (Coordinate tool : list)
        {
            Coordinate via_land = null;
            Coordinate via_water = null;
            CostSearch cs = null;

            CostSearch test = new CostSearch(agent, tool);
            if (test.construct())
            {
                via_land = tool;
                cs = test;
            }

            if (null == via_land)
            {
                test = new CostSearchIncludingWater(agent, tool);
                if (test.construct())
                {
                    via_water = tool;
                    cs = test;
                }
            }

            if (null == via_land)
            {
                ObEstimationOnLand obe = new ObEstimationOnLand(agent, tool);
                obe.construct();
                Queue<Cell> obs = obe.getObstacles();
                if (this.enoughTools(item, obs))
                {
                    for (Cell c : obs)
                    {
                        item.addObstacle(c.getPos());
                    }
                }

                // System.out.println("Go to " + tool.getX() + " " + tool.getY()
                // + " on land failure");
            }

            if (null == via_land && null == via_water)
            {
                ObEstimation obe = new ObEstimation(agent, tool);
                obe.construct();
                Queue<Cell> obs = obe.getObstacles();
                if (this.enoughTools(item, obs))
                {
                    for (Cell c : obs)
                    {
                        item.addObstacle(c.getPos());
                    }
                }

                // System.out.println("Go to " + tool.getX() + " " + tool.getY()
                // + " via sea failure");
            }

            if (null == cs)
            {
                continue;
            }

            Queue<Character> appended_moves = new LinkedList<Character>();
            appended_moves.addAll(item.getMoves());

            // if (type == OptionType.go_to_treasure)
            // {
            // System.out.println("?????????????? Go TO Treasure: " +
            // agent.pos().getX() + " " + agent.pos().getY());
            // }
            Agent new_agent = cs.executeAllMovesToAgentCopy();
            appended_moves.addAll(cs.getAllMoves());

            DecisionStackItem sub_option = new DecisionStackItem(new_agent, appended_moves, type);
            if (type == OptionType.go_to_treasure)
            {
                // System.out.println(
                // "?????????????? Go TO Treasure: " + new_agent.pos().getX() +
                // " " + new_agent.pos().getY());
                sub_option.setPriority(0);
            }
            else
            {
                sub_option.setPriority(1);
            }

            item.addNextOption(sub_option);
            index++;
            if (index >= b_limit)
            {
                return;
            }
        }
    }

    private void optionsWithRaft(DecisionStackItem item, OptionType type)
    {
        Agent agent = item.getAgent();

        Cell agent_cell = agent.map().getCell(agent.pos());
        if (agent_cell.isWater())
        {
            for (Coordinate pos : agent.map().getAllUnexplored().keySet())
            {
                if (agent.map().getCell(pos).isAccessible())
                {
                    Queue<Character> appended_moves = new LinkedList<Character>();
                    appended_moves.addAll(item.getMoves());

                    CostSearchIncludingWater strw = new CostSearchIncludingWater(agent, pos);
                    // Try to move to the door and then open the door
                    if (strw.construct())
                    {
                        Agent new_agent = strw.executeAllMovesToAgentCopy();
                        appended_moves.addAll(strw.getAllMoves());
                        item.addNextOption(new DecisionStackItem(new_agent, appended_moves, type));
                    }
                }
            }

            return;
        }

        Collection<Set<Coordinate>> seas = agent.map().getSeas();

        for (Set<Coordinate> sea : seas)
        {
            Coordinate des = null;
            for (Coordinate pos : sea)
            {
                // if (!agent.map().getAllUnexplored().containsKey(pos))
                // {
                des = pos;
                break;
                // }
            }

            if (null != des)
            {
                Queue<Character> appended_moves = new LinkedList<Character>();
                appended_moves.addAll(item.getMoves());

                CostSearchIncludingWater strw = new CostSearchIncludingWater(agent, des);
                // Try to move to the door and then open the door
                if (strw.construct())
                {
                    Agent new_agent = strw.executeAllMovesToAgentCopy();
                    appended_moves.addAll(strw.getAllMoves());
                    item.addNextOption(new DecisionStackItem(new_agent, appended_moves, type));
                }
            }
        }
    }

    private Queue<Character> exploration(Agent agent)
    {
        Cell agent_cell = agent.map().getCell(agent.pos());
        Queue<Character> moves = new LinkedList<Character>();

        Map<Coordinate, Cell> unexplored = agent.map().getAllUnexplored();
        Set<Coordinate> pos_set = new HashSet<Coordinate>();

        for (Coordinate pos : unexplored.keySet())
        {
            pos_set.add(pos);
        }

        Coordinate start_pos = agent.pos();
        while (!pos_set.isEmpty())
        {
            int min_man = -1;
            Coordinate dest_pos = null;
            for (Coordinate pos : pos_set)
            {
                if (min_man == -1)
                {
                    min_man = pos.manhattan(start_pos);
                    dest_pos = pos;
                }
                else if (pos.manhattan(start_pos) < min_man)
                {
                    min_man = pos.manhattan(start_pos);
                    dest_pos = pos;
                }
            }

            CostSearch cs = null;

            if (!agent_cell.isWater())
            {
                cs = new CostSearch(agent, dest_pos);
            }
            else
            {
                cs = new CostSearchOnlyInWater(agent, dest_pos);
            }

            if (cs.construct())
            {
                cs.executeAllMoves();
                moves.addAll(cs.getAllMoves());
                start_pos = dest_pos;
            }

            pos_set.remove(dest_pos);
        }

        return moves;
    }

    private Queue<Character> explorationHighRisk(Agent agent)
    {
        Queue<Character> moves = new LinkedList<Character>();

        Map<Coordinate, Cell> unexplored = agent.map().getAllUnexplored();
        Set<Coordinate> pos_set = new HashSet<Coordinate>();

        for (Coordinate pos : unexplored.keySet())
        {
            pos_set.add(pos);
        }

        Coordinate start_pos = agent.pos();
        while (!pos_set.isEmpty())
        {
            int max_man = -1;
            Coordinate dest_pos = null;
            for (Coordinate pos : pos_set)
            {
                if (max_man == -1)
                {
                    max_man = pos.manhattan(start_pos);
                    dest_pos = pos;
                }
                else if (pos.manhattan(start_pos) > max_man)
                {
                    max_man = pos.manhattan(start_pos);
                    dest_pos = pos;
                }
            }

            CostSearch cs = null;
            cs = new CostSearchIncludingWater(agent, dest_pos);
            if (cs.construct())
            {
                cs.executeAllMoves();
                moves.addAll(cs.getAllMoves());
                start_pos = dest_pos;
            }

            pos_set.remove(dest_pos);
        }

        return moves;
    }

    public Strategy newDecision()
    {
        this.m_agent.map().printMap();
        Map<ToolType, Integer> atools = this.m_agent.tools();

        for (ToolType type : atools.keySet())
        {
            int number = atools.get(type);
            System.out.println(type + ": " + number);
        }
        System.out.println();
        System.out.println();

        this.m_game_win = false;

        // If the map is expanded, try to expand the map again
        if (this.m_agent.map().size() > this.m_map_size_last_time)
        {
            Queue<Character> moves = new LinkedList<Character>();
            Agent new_agent = new Agent(this.m_agent);
            moves.addAll(exploration(new_agent));

            if (moves.size() > 0)
            {
                GlobalStrategy gs = new GlobalStrategy(this.m_agent, moves);
                gs.construct();

                System.out.println(
                        "============================= There are more places to explore ============================");

                return gs;
            }
        }
        else
        {
            int largest_possible_map_size = this.m_agent.map().largestPossibleSize();
            int map_size = this.m_agent.map().size();
            if ((double)map_size / (double)largest_possible_map_size < 0.8)
            {
                this.explorationHighRisk(this.m_agent);
            }
        }

        {
            // Preparing the start point of searching
            Queue<Character> moves = new LinkedList<Character>();
            Agent new_agent = new Agent(this.m_agent);
            DecisionStackItem start_item = new DecisionStackItem(new_agent, moves, OptionType.start);
            this.m_stack.push(start_item);

            // Generate sub options of this option
            // Game may win in the generation procedure
            this.generateOptions(start_item);
        }

        while (this.m_stack.size() > 0)
        {
            if (this.nextDecisionPoint())
            {
                break;
            }
        }

        // Check the strategy searching result
        GlobalStrategy gs = null;
        if (this.m_game_win)
        {
            for (int i = 0; i < this.m_stack.size(); i++)
            {
                Map<ToolType, Integer> tools = this.m_stack.get(i).getAgent().tools();

                for (ToolType type : tools.keySet())
                {
                    int number = tools.get(type);
                    System.out.println(type + ": " + number);
                }
                this.m_stack.get(i).getAgent().map().printMap();
                System.out.println();
            }

            gs = new GlobalStrategy(this.m_agent, this.m_stack.peek().getMoves());
            gs.construct();

            System.out.println("===================== Game win win win win win win win win ========================");
        }
        else if (!this.m_entire_option_tree.isEmpty())
        {
            DecisionStackItem optimal = this.m_entire_option_tree.peek();
            Queue<Character> failed_moves = new LinkedList<Character> ();
            failed_moves.addAll(optimal.getMoves());

            Agent agent = optimal.getAgent();
            agent.map().printMap();
            Map<ToolType, Integer> tools = agent.tools();

            for (ToolType type : tools.keySet())
            {
                int number = tools.get(type);
                System.out.println(type + ": " + number);
            }

            gs = new GlobalStrategy(this.m_agent, failed_moves);
            gs.construct();

            System.out.println("========= Too many options or all options failed, select the optimal one to continue! ===========");
        }
        else
        {
            Queue<Character> l_moves = new LinkedList<Character>();
            Agent l_new_agent = new Agent(this.m_agent);

            // Explore the land!!!!
            l_moves.addAll(exploration(l_new_agent));
            // Explore the land!!!!

            l_new_agent.map().printMap();
            Map<ToolType, Integer> tools = l_new_agent.tools();

            for (ToolType type : tools.keySet())
            {
                int number = tools.get(type);
                System.out.println(type + ": " + number);
            }

            gs = new GlobalStrategy(this.m_agent, l_moves);
            gs.construct();

            System.out.println(
                    "========================== No options, restart! ===========================");
        }

        this.m_stack.clear();
        this.m_entire_option_tree.clear();

        return gs;
    }

    public static void readMap()
    {
        BufferedReader br = null;
        try
        {
            // Read all the text from the file
            br = new BufferedReader(new FileReader("my.in"));
            String line = null;

            ArrayList<String> absolute_view = new ArrayList<String>();
            while ((line = br.readLine()) != null)
            {
                absolute_view.add(line);
            }

            Globals.global_agent.map().update(absolute_view);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        Globals.initialize(new Agent());
        readMap();
        Globals.global_agent.map().printMap();

        DecisionMaker maker = new DecisionMaker(Globals.global_agent);

        while (!maker.m_game_win)
        {
            Strategy str = maker.newDecision();
            str.executeAllMoves();
        }
    }
}
