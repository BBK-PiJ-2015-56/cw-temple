package student;

import game.EscapeState;
import game.ExplorationState;
import game.Node;
import game.NodeStatus;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Explorer {

    /**
     * Explore the cavern, trying to find the orb in as few steps as possible.
     * Once you find the orb, you must return from the function in order to pick
     * it up. If you continue to move after finding the orb rather
     * than returning, it will not count.
     * If you return from this function while not standing on top of the orb,
     * it will count as a failure.
     * <p>
     * There is no limit to how many steps you can take, but you will receive
     * a score bonus multiplier for finding the orb in fewer steps.
     * <p>
     * At every step, you only know your current tile's ID and the ID of all
     * open neighbor tiles, as well as the distance to the orb at each of these tiles
     * (ignoring walls and obstacles).
     * <p>
     * To get information about the current state, use functions
     * getCurrentLocation(),
     * getNeighbours(), and
     * getDistanceToTarget()
     * in ExplorationState.
     * You know you are standing on the orb when getDistanceToTarget() is 0.
     * <p>
     * Use function moveTo(long id) in ExplorationState to move to a neighboring
     * tile by its ID. Doing this will change state to reflect your new position.
     * <p>
     * A suggested first implementation that will always find the orb, but likely won't
     * receive a large bonus multiplier, is a depth-first search.
     *
     * @param state the information available at the current state
     */
    public void explore(ExplorationState state) {
        System.out.println("starting to explore.......");
        List<Long> visitedNodes = new ArrayList<>();
        //add the starting node to visited
        visitedNodes.add(state.getCurrentLocation());

        Collection<NodeStatus> neighboursCollection;
        List<NodeStatus> neighbours;
        List<NodeStatus> unvisitedNeighbours;
        List<NodeStatus> visitedNeighbours;

        //A list of nodes that are to be avoided because they get George stuck
        List<Long> blacklist = new ArrayList<>();

        long currentId;

        while (state.getDistanceToTarget() != 0) {
            System.out.println("starting a  new move........");
            currentId = state.getCurrentLocation();
            System.out.println("The current node is: " + currentId);
            System.out.println("This distance away from the orb is: " + state.getDistanceToTarget());
            neighboursCollection = state.getNeighbours();
            //convert the new neighbours collection into a new list
            neighbours = new ArrayList<>(neighboursCollection);
            System.out.print("current node neighbours: ");
            neighbours.forEach(node -> System.out.print("node id:" + node.getId() + "..."));
            System.out.println();

            //take out any neighbours that are on the blacklist
            for (int i = 0; i < neighbours.size(); i++) {
                if (blacklist.contains(neighbours.get(i).getId())) {
                    neighbours.remove(i);
                }
            }
            System.out.print("current node neighbours, excluding blacklisted: ");
            neighbours.forEach(node -> System.out.print("node id:" + node.getId() + "..."));
            System.out.println();
            //sort the list
            Collections.sort(neighbours);
            System.out.print("available neighbours, sorted: ");
            neighbours.forEach(node -> System.out.print("node id:" + node.getId() + "..."));

            //set the visited and unvisited neighbours lists to empty
            System.out.println();
            unvisitedNeighbours = new ArrayList<>();
            visitedNeighbours = new ArrayList<>();

            // split the sorted list into 2 sublists
            splitList(visitedNodes, neighbours, unvisitedNeighbours, visitedNeighbours);
            System.out.println("Here are the 2 neighbour lists for the current node: ");
            System.out.print("unvisited: [ ");
            unvisitedNeighbours.forEach(node -> System.out.print("  node id: " + node.getId()));
            System.out.println(" ]");
            System.out.print("visited: [ ");
            visitedNeighbours.forEach(node -> System.out.print("  node id: " + node.getId()));
            System.out.println(" ]");

            //Decide nextNode
            // now need to check 2 lists and decide next move
            long nextNodeId;
            // move to the first node in unvisitedNeighbours, if such a node exists
            if (!unvisitedNeighbours.isEmpty()) {
                System.out.println("There is an unvisited neighbour. We move to the one nearest the orb");
                nextNodeId = unvisitedNeighbours.get(0).getId();
            } else {
                System.out.println("All neighbours are visited. We move to a random neighbour");
                Collections.shuffle(visitedNeighbours);
                nextNodeId = visitedNeighbours.get(0).getId();
            }
            move(nextNodeId, visitedNodes, state);
        }
    }

    // a method to move to a neighbour, updating visited nodes when move is made
    private void move(long nextNode, List<Long> visitedNodes, ExplorationState state){
        long nextNodeId;
        nextNodeId = nextNode;
        visitedNodes.add(nextNodeId);
        System.out.println("moving to node with id: " + nextNodeId);
        state.moveTo(nextNodeId);
        System.out.println("Check move: node should be: " + nextNodeId + " node is: " + state.getCurrentLocation());
        System.out.println();
    }

    private void splitList (List<Long> visitedNodes, List<NodeStatus> neighbours,
                            List<NodeStatus> unvisitedNeighbours, List<NodeStatus> visitedNeighbours){
        NodeStatus tempNode;
        for(int i = 0; i < neighbours.size(); i++){
            tempNode = neighbours.get(i);
            if (visitedNodes.contains(tempNode.getId())) {
                visitedNeighbours.add(tempNode);
            } else{
                unvisitedNeighbours.add(tempNode);
            }
        }
    }

    /**
     * Escape from the cavern before the ceiling collapses, trying to collect as much
     * gold as possible along the way. Your solution must ALWAYS escape before time runs
     * out, and this should be prioritized above collecting gold.
     * <p>
     * You now have access to the entire underlying graph, which can be accessed through EscapeState.
     * getCurrentNode() and getExit() will return you Node objects of interest, and getVertices()
     * will return a collection of all nodes on the graph.
     * <p>
     * Note that time is measured entirely in the number of steps taken, and for each step
     * the time remaining is decremented by the weight of the edge taken. You can use
     * getTimeRemaining() to get the time still remaining, pickUpGold() to pick up any gold
     * on your current tile (this will fail if no such gold exists), and moveTo() to move
     * to a destination node adjacent to your current node.
     * <p>
     * You must return from this function while standing at the exit. Failing to do so before time
     * runs out or returning from the wrong location will be considered a failed run.
     * <p>
     * You will always have enough time to escape using the shortest path from the starting
     * position to the exit, although this will not collect much gold.
     *
     * @param state the information available at the current state
     */
    public void escape(EscapeState state) {
        Collection<Node> nodesCollection = state.getVertices();
        List<Node> allNodes = new ArrayList<>(nodesCollection);

        //HashMap<Node, Integer> dstGoldToNodes = new HashMap<Node, Integer>();
        HashMap<Node, HashMap<Node, Integer>> dstGoldsToNodes = new HashMap<Node, HashMap<Node, Integer>>();

        System.out.println("STARTING ESCAPE: start:" + state.getCurrentNode().getId() + "  exit:" + state.getExit().getId()
                + "  nodes:" + allNodes.size() + "  time:" + state.getTimeRemaining());

        // get the best gold squares - top 25% because larger board means more steps allowed
        Map<Node, Integer> highGolds = getTop25PCGolds(allNodes);

        //avg gold per square, including blank squares
        Double avgGold = calcAvgGold(allNodes);

        //HashMap<Node, Map<Node, List<Node>>> dstsGoldsToNodes = new HashMap<>();
        //calc all paths from all highGolds to all Nodes
        HashMap<Node, Map<Node, List<Node>>> allPathsFromAllHighGolds = findAllPathsFromGolds(highGolds, allNodes);


        //calculate the shortestPaths and Distances from start to all nodes
        //declare the variables we will be using to help us make our moves

        Node start = state.getCurrentNode();
        System.out.println("CALLING METHOD: initDstToNodes for start........................");
        HashMap<Node, Integer> dstStartToNodes = initDstToNodes(start, nodes);
        System.out.println("Here are the nodes and their dst from the start");
        System.out.print("[ ");
        allNodes.forEach(node ->
                System.out.print("(node: " + node.getId() + ", " + "dst:" + dstStartToNodes.get(node) + ") "));
        System.out.println("]");
        System.out.println("CALLING METHOD: findPathsToNodes for start");
        Map<Node, List<Node>> pathsStartToNodes = findPathsToNodes(start, nodes, dstStartToNodes);
        System.out.println("ALL PATHS FROM START CALCULATED");

        makeEscape(state);
    }

    //returns the top 25 per cent of Gold Nodes, in terms of their gold amount
    private  Map<Node, Integer> getTop25PCGolds(List<Node> nodes) {
        Map<Node, Integer> goldNodes = new HashMap<>();
        nodes.forEach(node -> {
            if (node.getTile().getGold() != 0)
                goldNodes.put(node, node.getTile().getGold());
        });
        long goldNodesRequired;
        goldNodesRequired = Math.round(0.25 * goldNodes.size());

        Map<Node, Integer> top25PCGolds = goldNodes.entrySet().stream()
                . sorted(Map.Entry.<Node, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        return top25PCGolds;
    }
    private Double calcAvgGold(List<Node> nodes) {
        Map<Node,Integer> nodeGolds = new HashMap<>();
        // we will use avg later to convert all nodes to score out of 100
        Double avgGold = nodeGolds.entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList())
                .stream()
                .mapToDouble(a -> a)
                .average()
                .getAsDouble();
        return avgGold;
    }

    //returns a Map of all paths from all golds
    private HashMap<Node, Map<Node, List<Node>>> findAllPathsFromGolds(Map<Node, Integer> golds, List<Node> allNodes,...dst?){
        HashMap<Node, Map<Node, List<Node>>> allPathsFromAllGolds = new HashMap<>();

        //list of gold nodes
        List<Node> goldList = golds.entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        System.out.println("CALCULATING ALL PATHS FROM ALL GOLD PATHS...");
        //for each node
        for (int i = 0; i < golds.size(); i++) {
            Node tempNode = goldList.get(i);
            // for each node, initialise a new map for paths and new map for distances
            //Map<Node, Integer> dstToNodesFromTemp = initDstToNodes(tempNode, goldNodeList);
            System.out.println("CALC PATHS FOR GOLD NODE:" + tempNode.getId() );
            Map<Node, List<Node>> pathsFromTempToNodes = findPathsFromNodeToNodes(tempNode, allNodes);
            allPathsFromAllGolds.put(tempNode, pathsFromTempToNodes);

            //update the map that stores all the distances from all golds to all other nodes
            //dstGoldsToNodes.put(tempNode, dstGoldsToNodes.get(state.getExit()));
        }
        System.out.println("ALL GOLD PATHS CALCULATED...");
        return allPathsFromAllGolds;
    }

    // A method that calcs and returns the shortest paths from any start node to every node in nodes
    //It also updates the map(nodes, dstToNodes) from this start node
    private Map<Node, List<Node>> findPathsFromNodeToNodes(Node start, List<Node> allNodes, Map<Node, Integer> dstToNodes) {
        System.out.println("findPathFromNodeToNodes from " + start.getId());
        Map<Node, List<Node>> pathsFromNode = initPathsToNodes(start, allNodes);

        //A List of all Nodes for which we don't know shortest dst - all of them to begin with
        List<Node> unoptNodes = new ArrayList<>();
        allNodes.forEach(node -> unoptNodes.add(node));
        //A List of all nodes for which we do know shortest dst
        List<Node> optNodes = new ArrayList<>();

        //The node we are optimizing next
        Node currentOptNode;

        while(!unoptNodes.isEmpty()) {
            // get another node to optimize - this is 'start' the first time
            currentOptNode = getNextNode(unoptNodes, dstToNodes);

            //take this node out of unoptimized and put into optimized
            optNodes.add(currentOptNode);
            unoptNodes.remove(currentOptNode);

            // get list of the unoptimized neighbours
            List<Node> unoptNeighbours = findUnoptNeighbours(currentOptNode, unoptNodes);

            // update the shortestDst paths for these neighbours
            updateMaps(currentOptNode, unoptNeighbours, dstToNodes, pathsFromNode);
        }
        System.out.print("All node paths from " + start.getId() + " are optimized...");
        return pathsFromNode;
    }

    //A method to init the Map of shortest distances from the start to every node
    private Map<Node, Integer> initDstToNodes(Node start, List<Node> allNodes){
        System.out.println("initialising map of distances from " + start.getId() + "...");
        Map<Node, Integer> dstToNodes = new HashMap<>();
        allNodes.forEach(node -> dstToNodes.put(node , 100000));
        dstToNodes.replace(start, 0);
        return dstToNodes;
    }
    //a method to init all paths as having their own node in pos 0
    private Map<Node, List<Node>> initPathsToNodes(Node start, List<Node> allNodes){
        Map<Node, List<Node>> pathsFromNodeToNodes = new HashMap<>();

        for (int i = 0; i < allNodes.size(); i++) {
            List<Node> path = new ArrayList<>();
            path.add(allNodes.get(i));
            pathsFromNodeToNodes.put(allNodes.get(i), path);
        }
        return pathsFromNodeToNodes;
    }

    //returns the node in unopt that has the lowest current shortestDst
    //these dst have all been initially set to 100,000, except start node set to 0
    //This should return the start node the first time as distance set to 0
    private Node getNextNode(List<Node> unopt, Map<Node, Integer> distances){
        Node nextNode = unopt.get(0);
        if(unopt.size() > 1) {
            for (int i = 1; i < unopt.size(); i++) {
                if (distances.get(unopt.get(i)) < distances.get(nextNode)){
                    nextNode = unopt.get(i);
                }
            }
        }
        System.out.print("next node for opt: " + nextNode);
        return nextNode;
    }

    private List<Node> findUnoptNeighbours(Node currentOptNode, List<Node> unoptNodes){
        Collection<Node> neighboursSet = currentOptNode.getNeighbours();
        List<Node> neighbours = new ArrayList<>(neighboursSet);
        return neighbours.stream()
                .filter( p -> unoptNodes.contains(p))
                .collect(Collectors.toList());
    }

    // This adds the currentOptNode to the paths for all unoptNeighbours if it makes a shorter route
    // It also adds the nodes in the predecessor nodes recursively all the way back to the start
    private void updateMaps(Node current, List<Node> unoptNeighbours, Map<Node,Integer> shortestDst,
                            Map<Node,List<Node>> paths){
        System.out.println("Updating maps (if shorter) for neighbours....");
        if(unoptNeighbours.isEmpty())
            System.out.println("ALL NEIGHBOURS FULLY OPTIMIZED ALREADY.");
        else {
            unoptNeighbours.forEach(neighbour -> {
                //sum the dst from start to current with dst from current to this neighbour
                int newPathDst = shortestDst.get(current) + current.getEdge(neighbour).length();

                // check if this dst is shorter than current best estimate for neighbour
                if (newPathDst < shortestDst.get(neighbour)) {
                    System.out.println(" Updating neighbour " + neighbour.getId() + "...");
                    //get the path of current node
                    List<Node> pathOfCurrent = paths.get(current);
                    // create a new path
                    List<Node> newPathForNeighbour = new ArrayList<>();
                    newPathForNeighbour.add(neighbour);
                    for (int i = 0; i < pathOfCurrent.size(); i++) {
                        newPathForNeighbour.add(pathOfCurrent.get(i));
                    }
                    //replace the path of neighbour in paths
                    paths.replace(neighbour, newPathForNeighbour);
                    System.out.print("...UPDATE COMPLETE FOR THIS NEIGHBOUR");

                    //update the shortest distance
                    shortestDst.replace(neighbour, newPathDst);
                } else {
                    System.out.println(" Not updating neighbour.");
                    System.out.println();
                }
            });
            System.out.println("UPDATES COMPLETE FOR ALL NEIGHBOURS");
        }
    }

    private void makeEscape(EscapeState state){
        // start our loop to plan and make next move, which we do until we get to exit
        while (state.getCurrentNode() != state.getExit()) {
            // new Method
            //    private Node planNextJourney(EscapeState state, Map dstGoldsToNodes,
            //                               Map topGoldNodes, Map dstStartToNodes)
            Node currentNode = state.getCurrentNode();
            System.out.println("Still not at exit - we are at " + currentNode.getId() + " need to move again..");
            //we are not at exit yet - find the best move
            Node nextMove = calcBestMove(state.getCurrentNode(), state.getTimeRemaining(),
                    dstGoldsToNodes, topGoldNodes, dstStartToNodes, state);
            System.out.println("Best move is to node " + nextMove.getId()
                    + " with " + nextMove.getTile().getGold() + " golds.");

            if(state.getCurrentNode() == start) {
                System.out.println("About to make first journey...");
                makeJourney(state, nextMove, pathsStartToNodes);
            }else
                makeJourney(state, nextMove, pathsGoldsToNodes);
        }
    }

    private Node calcBestMove(Node currentPos, int timeRemaining, HashMap<Node, HashMap<Node, Integer>> dstGoldsToNodes,
                              List<Node> topGoldNodes, HashMap<Node, Integer> dstStartToNodes, EscapeState state){
        System.out.println();
        System.out.println("calculating best move...");
        Node bestMove;
        Map<Node,Double> nodeValues = new HashMap<>();
        //constants to add weighting to three factors in deciding nextMove
        Double kGold = 1.0;
        Double kDstFromCurrent = 1.0;
        Double kDstFromExit = 1.0;
        Node tempNode;
        int gold;
        int dstFromCurrent;
        int dstFromExit;
        Double nodeValue;

        // for each gold node, we get nodeValue and put in map nodeValues
        for(int i = 0; i < topGoldNodes.size(); i++) {
            tempNode = topGoldNodes.get(i);
            System.out.println("getting nodeValue for node " + i + " which is " + tempNode.getId());

            //new method
            //   private setValues
            //set gold for this node
            System.out.print("getting gold........ ");
            gold = tempNode.getTile().getGold();
            System.out.println("node " + tempNode.getId() + " has gold: " + gold);

            //set dstFromCurrent for this node
            System.out.println("getting dstFromCurrent........");
            if(!(topGoldNodes.contains(currentPos))){
                System.out.println("we are at start - need to use dstStartToNodes Map for dst to temp Node");
                //current must be start
                dstFromCurrent = dstStartToNodes.get(tempNode);
            }else{
                System.out.println("we are at a gold - need to use dstGoldsToNodes Map for dst to tempNode");
                //NPE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                //AFTER ALL NODES WERE OPTIMIZED
                dstFromCurrent = dstGoldsToNodes.get(currentPos).get(tempNode);
            }

            //set dstFromExit for this Node
            System.out.println("getting dstFromExit........");
            dstFromExit = dstGoldsToNodes.get(tempNode).get(state.getExit());//NPE
            System.out.println();

            // new method - returns exit if all goldNodeValues == 0
            //   private Node calcTopNodeValue(goldValue, dstFromCurrent, dstFromExit)
            System.out.println("calculating nodeValue for " + tempNode);
            if(topGoldNodes.get(i).getTile().getGold() == 0) {
                //node already been visited
                nodeValue = 0.0;
            } else if(dstFromCurrent  + dstFromExit > timeRemaining) {
                //node too far away from exit
                nodeValue = 0.0;
            } else{
                nodeValue = (kGold * gold)/(kDstFromCurrent * dstFromCurrent + kDstFromExit * dstFromExit);
            }
            nodeValues.put(tempNode, nodeValue);
        }
        System.out.print("Here are the nodes and their move values: ");

        bestMove = state.getExit();
        for(int i = 0; i < nodeValues.size(); i++){
            System.out.print(" (node:" + topGoldNodes.get(i).getId()
                    + ", value:" + nodeValues.get(topGoldNodes.get(i)) + ") ");
            if(nodeValues.get(topGoldNodes.get(i)) > nodeValues.get(bestMove))
                bestMove = topGoldNodes.get(i);
        }
        System.out.println();
        return bestMove;
    }


    private void makeJourney(EscapeState state, Node nextMove, Map<Node, List<Node>> pathsForAllNodes) {
        System.out.print("journey start pos:" + state.getCurrentNode().getId());
        System.out.print("...journey path list: [ ");
        for(int i = 0; i < journeyNodes.size(); i++){
            System.out.print((journeyNodes.get(i).getId()) + ", ");
        }
        System.out.println(" ]");

        // 1st element should be equal to startingNode
        if (state.getCurrentNode() != journeyNodes.get(journeyNodes.size()-1)) {
            System.out.println("Cannot make this journey as you are not at the right starting point");
            System.out.println("The currentNodeId is " + state.getCurrentNode().getId());
            System.out.println("The journey starting nodeId is " + journeyNodes.get(journeyNodes.size()-1).getId());
        } else {
            for (int i = journeyNodes.size()-2; i >= 0; i--) {
                System.out.print("   current pos:" + state.getCurrentNode().getId());
                System.out.print("..next intended move:" + journeyNodes.get(i).getId() + "  ");
                Set<Node> neighboursSet = state.getCurrentNode().getNeighbours();
                List<Node> neighboursList = Arrays.asList(neighboursSet.toArray(new Node[neighboursSet.size()]));
                Boolean containsNextMove = neighboursSet.contains(journeyNodes.get(i));
                if(!containsNextMove){
                    System.out.println("WARNING: next move is not a neighbour of current pos!!!");
                    System.out.print("The neighbours for the current node are: ");
                    System.out.print("[");
                    for(int j = 0; j < neighboursSet.size(); j++){
                        System.out.print(neighboursList.get(j).getId() + ", ");
                    }
                    System.out.println(" ]");

                    System.out.println("The separate paths for every node in the journey path are as follows: ");
                    for(int k = 0; k< journeyNodes.size(); k++){
                        List<Node> pathForThisNode = pathsForAllNodes.get(journeyNodes.get(k));
                        System.out.println("node(" + journeyNodes.get(k).getId() + ")");
                        System.out.print("Path(");
                        for(int n = 0; n < pathForThisNode.size(); n++){
                            System.out.print(pathForThisNode.get(n).getId() + ", ");
                        }
                        System.out.println(")");
                    }
                    System.out.println(" ]");
                    // print the path for each node in the path
                }
                System.out.print("....moving to " + journeyNodes.get(i).getId());
                state.moveTo(journeyNodes.get(i));
                if(state.getCurrentNode().getTile().getGold() > 0){
                    System.out.println("..Picking up Gold!!!");
                    state.pickUpGold();
                }
                System.out.println();
            }
        }
    }
}
