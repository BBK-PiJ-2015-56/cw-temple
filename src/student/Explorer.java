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
        Node start = state.getCurrentNode();
        Collection<Node> nodesCollection = state.getVertices();
        List<Node> allNodes = new ArrayList<>(nodesCollection);

        Map<Node, Integer> distancesFromStart = new HashMap<Node, Integer>();
        Map<Node, Map<Node, Integer>> distancesFromGolds = new HashMap<>();

        System.out.println("STARTING ESCAPE: start:" + state.getCurrentNode().getId() + "  exit:" + state.getExit().getId()
                + "  nodes:" + allNodes.size() + "  time:" + state.getTimeRemaining());

        // get the best gold squares - top 25% because larger board means more steps allowed
        Map<Node, Integer> highGolds = getTop25PCGolds(allNodes);

        //avg gold per square, including blank squares. used later to calc best next move
        Double avgGold = calcAvgGold(allNodes);

        //HashMap<Node, Map<Node, List<Node>>> dstsGoldsToNodes = new HashMap<>();
        //calc all paths from all highGolds to all Nodes
        Map<Node, Map<Node, List<Node>>> allPathsFromAllHighGolds = findAllPathsFromGolds(highGolds, allNodes, distancesFromGolds);


        //calculate the shortestPaths and Distances from start to all nodes
        Map<Node, List<Node>> pathsFromStartToNodes
                = findPathsFromNodeToNodes(state.getCurrentNode(), allNodes, distancesFromStart);
        System.out.println("ALL PATHS FROM START CALCULATED");

        makeEscape(start, state, distancesFromGolds, distancesFromStart, highGolds, pathsFromStartToNodes, allPathsFromAllHighGolds);
    }

    //returns the top 25 per cent of Gold Nodes, in terms of their gold amount
    private  Map<Node, Integer> getTop25PCGolds(List<Node> nodes) {
        System.out.println("------------------------------------------");
        System.out.println("METHOD CALL: getTop25PCGolds.");
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
        System.out.println("------------------------------------------");
        System.out.println("METHOD CALL: calcAvgGold");
        Double totalGold = 0.0;
        Double avgGold;
        for(int i = 0; i < nodes.size(); i++){
            totalGold += nodes.get(i).getTile().getGold();
        }
        avgGold = totalGold / nodes.size();
        return avgGold;
    }

    //returns a Map of all paths from all golds
    private Map<Node, Map<Node, List<Node>>> findAllPathsFromGolds(Map<Node, Integer> golds, List<Node> allNodes,
                                                                       Map<Node, Map<Node, Integer>> distancesFromGolds){
        System.out.println("------------------------------------------");
        System.out.println("METHOD CALL: findAllPathsFromGolds.");
        Map<Node, Map<Node, List<Node>>> allPathsFromAllGolds = new HashMap<>();

        //list of gold nodes
        List<Node> goldList = golds.entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        System.out.println("CALCULATING ALL PATHS FROM ALL GOLD PATHS...");
        //for each node
        for (int i = 0; i < golds.size(); i++) {
            Node tempNode = goldList.get(i);
            Map<Node,Integer> distancesFromThisGold = new HashMap<>();
            // for each node, initialise a new map for paths and new map for distances
            //Map<Node, Integer> dstToNodesFromTemp = initDstToNodes(tempNode, goldNodeList);
            System.out.println("CALC PATHS FOR GOLD NODE:" + tempNode.getId() );
            Map<Node, List<Node>> pathsFromTempToNodes = findPathsFromNodeToNodes(tempNode, allNodes, distancesFromThisGold);
            allPathsFromAllGolds.put(tempNode, pathsFromTempToNodes);
            //update this map of distances from this gold into the map that stores the distances from all golds
            distancesFromGolds.put(tempNode, distancesFromThisGold);
        }
        System.out.println("ALL GOLD PATHS CALCULATED...");
        return allPathsFromAllGolds;
    }

    // A method that calcs and returns the shortest paths from any start node to every node in nodes
    //It also updates the map(nodes, dstToNodes) from this start node
    private Map<Node, List<Node>> findPathsFromNodeToNodes(Node start, List<Node> allNodes, Map<Node, Integer> distancesFromThisNode) {
        System.out.println("------------------------------------------");
        System.out.println("METHOD CALL: findPathFromNodeToNodes from " + start.getId());
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
            currentOptNode = getNextNode(unoptNodes, distancesFromThisNode);

            //take this node out of unoptimized and put into optimized
            optNodes.add(currentOptNode);
            unoptNodes.remove(currentOptNode);

            // get list of the unoptimized neighbours
            List<Node> unoptNeighbours = findUnoptNeighbours(currentOptNode, unoptNodes);

            // update the shortestDst paths for these neighbours
            updateMaps(currentOptNode, unoptNeighbours, distancesFromThisNode, pathsFromNode);
        }
        System.out.print("All node paths from " + start.getId() + " are optimized...");
        return pathsFromNode;
    }

    //A method to init the Map of shortest distances from the start to every node
    private Map<Node, Integer> initDstToNodes(Node start, List<Node> allNodes){
        System.out.println("------------------------------------------");
        System.out.println("METHOD CALL initDstToNodes from " + start.getId() + "...");
        Map<Node, Integer> dstToNodes = new HashMap<>();
        allNodes.forEach(node -> dstToNodes.put(node , 100000));
        dstToNodes.replace(start, 0);
        return dstToNodes;
    }
    //a method to init all paths as having their own node in pos 0
    private Map<Node, List<Node>> initPathsToNodes(Node start, List<Node> allNodes){
        System.out.println("------------------------------------------");
        System.out.println("METHOD CALL: initPathsToNodes.");
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
        System.out.println("------------------------------------------");
        System.out.println("METHOD CALL: getNextNode.");
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
        System.out.println("------------------------------------------");
        System.out.println("METHOD CALL: findUnoptNeighbours.");
        Collection<Node> neighboursSet = currentOptNode.getNeighbours();
        List<Node> neighbours = new ArrayList<>(neighboursSet);
        return neighbours.stream()
                .filter( p -> unoptNodes.contains(p))
                .collect(Collectors.toList());
    }

    // This adds the currentOptNode to the paths for all unoptNeighbours if it makes a shorter route
    // It also adds the nodes in the predecessor nodes recursively all the way back to the start
    private void updateMaps(Node current, List<Node> unoptNeighbours, Map<Node,Integer> distancesFromThisNode,
                            Map<Node,List<Node>> paths){
        System.out.println("------------------------------------------");
        System.out.println("Updating maps (if shorter) for neighbours....");
        if(unoptNeighbours.isEmpty())
            System.out.println("ALL NEIGHBOURS FULLY OPTIMIZED ALREADY.");
        else {
            unoptNeighbours.forEach(neighbour -> {
                //sum the dst from start to current with dst from current to this neighbour
                int newPathDst = distancesFromThisNode.get(current) + current.getEdge(neighbour).length();

                // check if this dst is shorter than current best estimate for neighbour
                if (newPathDst < distancesFromThisNode.get(neighbour)) {
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
                    distancesFromThisNode.replace(neighbour, newPathDst);
                } else {
                    System.out.println(" Not updating neighbour.");
                    System.out.println();
                }
            });
            System.out.println("UPDATES COMPLETE FOR ALL NEIGHBOURS");
        }
    }
    // a method for producing our combination of journeys from start to exit
    private void makeEscape(Node start, EscapeState state, Map<Node, Map<Node, Integer>> distancesFromGolds,
                            Map<Node, Integer> distancesFromStart, Map<Node, Integer> highGolds,
                            Map<Node, List<Node>> pathsFromStartToNodes, Map<Node, Map<Node, List<Node>>> allPathsFromAllHighGolds){
        System.out.println("------------------------------------------");
        System.out.println("METHOD CALL: makeEscape");
        // start our loop to plan and make next move, which we do until we get to exit
        while (state.getCurrentNode() != state.getExit()) {
            System.out.println("Still not at exit - we are at " + state.getCurrentNode().getId() + " need to move again..");
            //we are not at exit yet - find the best move
            Node nextMove = calcBestMove(state.getCurrentNode(), state.getTimeRemaining(),
                    distancesFromGolds, highGolds, distancesFromStart, state);
            System.out.println("Best move is to node " + nextMove.getId()
                    + " with " + nextMove.getTile().getGold() + " golds.");

            if (state.getCurrentNode() == start) {
                System.out.println("About to make first journey...");
                List<Node> journeyNodes = pathsFromStartToNodes.get(nextMove);
                makeJourney(state, nextMove, journeyNodes);
            } else {
                Map<Node, List<Node>> requiredPathMaps = allPathsFromAllHighGolds.get(state.getCurrentNode());
                List<Node> journeyNodes = requiredPathMaps.get(nextMove);
                makeJourney(state, nextMove, journeyNodes);
            }
        }
    }
    private Node calcBestMove(Node currentPos, int timeRemaining, Map<Node, Map<Node, Integer>> distancesFromGolds,
                              Map<Node, Integer> highGolds, Map<Node, Integer> distancesFromStart, EscapeState state){
        System.out.println("------------------------------------------");
        System.out.println("METHOD CALL: calcBestMove...");
        Node bestMove;
        Map<Node,Double> nodeValues = new HashMap<>();
        //constants to add weighting to three factors in deciding nextMove
        Node tempNode;
        int gold;
        int dstFromCurrent;
        int dstFromExit;
        Double nodeValue;

        //list of gold nodes
        List<Node> goldList = highGolds.entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        // for each gold node
        for(int i = 0; i < highGolds.size(); i++) {
            tempNode = goldList.get(i);

            //get gold on this node
            gold = tempNode.getTile().getGold();

            //set dstFromCurrent and dstFromExit for this node
            if (!(goldList.contains(currentPos))) {
                //we are at start
                dstFromCurrent = distancesFromStart.get(tempNode);
                dstFromExit = distancesFromStart.get(state.getExit());
            } else {
                //we are at a gold
                dstFromCurrent = distancesFromGolds.get(currentPos).get(currentPos);
                dstFromExit = distancesFromGolds.get(tempNode).get(state.getExit());
            }
            //get nodeValue for this potential node and put into nodeValues map
            nodeValue = calcNodeValue(gold, dstFromCurrent, dstFromExit, timeRemaining);
            nodeValues.put(tempNode, nodeValue);
        }
        // find the node with the highest nodeValue
        bestMove = Collections.max(nodeValues.entrySet(),
                (entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).getKey();
        System.out.println("bestMove node: " + bestMove.getId() + ", gold: " + bestMove.getTile().getGold());

        if (bestMove.getTile().getGold() == 0)
                // we have collected all the golds we wanted
                return state.getExit();
        else
            return bestMove;
        }
    // a method for calculating a numeric value to represent the value of moving to each gold node
    // the value is dependent on amount of gold, its distance form current location, and its distance from exit
    // there are 3 constants which act as multipliers for these 3 factors, and can be adjusted easily
    private Double calcNodeValue(int gold, int dstFromCurrent, int dstFromExit, int timeRemaining){
        System.out.println("------------------------------------------");
        System.out.println("METHOD CALL: calcNodeValue");
        final Double kGold = 1.0;
        final Double kDstFromCurrent = 1.0;
        final Double kDstFromExit = 1.0;

        if(gold == 0) {
            //node already been visited
            return  0.0;
        } else if(dstFromCurrent  + dstFromExit > timeRemaining) {
            //node too far away from exit
            return  0.0;
        } else{
            return (kGold * gold)/(kDstFromCurrent * dstFromCurrent + kDstFromExit * dstFromExit);
        }
    }

    // a method to make a journey from one key node to another, picking up any gold it passes
    private void makeJourney(EscapeState state, Node nextMove, List<Node> journeyNodes) {
        System.out.println("------------------------------------------");
        System.out.println("METHOD CALL: makeJourney");
        System.out.println("making a journey from " + state.getCurrentNode().getId() + " to " + nextMove.getId());

        // 1st element should be equal to startingNode
        if (state.getCurrentNode() != journeyNodes.get(journeyNodes.size()-1))
            System.out.println("Cannot make this journey as you are not at the right starting point");
        else {
            for (int i = journeyNodes.size()-2; i >= 0; i--) {
                System.out.print("   current pos:" + state.getCurrentNode().getId());
                System.out.print("..next intended move:" + journeyNodes.get(i).getId() + "  ");
                Set<Node> neighboursSet = state.getCurrentNode().getNeighbours();
                List<Node> neighboursList = Arrays.asList(neighboursSet.toArray(new Node[neighboursSet.size()]));
                Boolean containsNextMove = neighboursSet.contains(journeyNodes.get(i));
                if(!containsNextMove) System.out.println
                        ("WARNING: next move is not a neighbour of current pos!!!");
                System.out.print("....moving to " + journeyNodes.get(i).getId());
                state.moveTo(journeyNodes.get(i));
                if(state.getCurrentNode().getTile().getGold() > 0){
                    System.out.print("..Picking up Gold!!!...");
                    state.pickUpGold();
                }
                System.out.println();
            }
        }
    }
}
