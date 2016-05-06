package NodePackage;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class NodeSet extends HashMap<Integer, Node> {

    public NodeSet() {

    }

    public NodeSet(NodeSet nodeSetToCopy, int treeId) {
        Node nodeToCopy;

        for (int nodeId: nodeSetToCopy.keySet()) {
            nodeToCopy = nodeSetToCopy.get(nodeId);

            put(nodeId, new Node(nodeToCopy, treeId));

//            if (nodeToCopy.isToBeSplit) {
//                put(nodeId, new Node(nodeToCopy, treeId));
//            } else {
//                put(nodeId, nodeToCopy);
//            }
        }
    }

    public List<Node> getLeafNodes() {
        return values().stream().filter(Node::isLeafNode).collect(Collectors.toList());
    }

    public List<Node> getNonLeafNodes() {
        return values().stream().filter(node -> !node.isLeafNode()).collect(Collectors.toList());
    }


}
