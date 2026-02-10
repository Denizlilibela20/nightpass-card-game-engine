/**
 * AVL based index for the discard pile used during the healing phase.
 *
 * Cards are ordered by (missing health, discard timestamp) to support:
 * - Selecting the largest fully revivable card
 * - Selecting the smallest partially revivable card
 * - Enforcing first-discarded-first tiebreaking rules
 *
 * This structure is active only in Type-2 gameplay scenarios.
 */


public class DiscardPile {

    private static final class Node implements Comparable<Node> {
        int healthMissing;
        int discardTimeStamp;
        Card card;
        Node left;
        Node right;
        int height = 1;
        int size = 1;

        Node(Card c){
            this.card = c;
            this.healthMissing = c.healthMissing;
            this.discardTimeStamp = c.discardTimestamp;
        }
        //for search
        Node(int healthMissing, int discardTimeStamp){
            this.healthMissing = healthMissing;
            this.discardTimeStamp = discardTimeStamp;
            this.card = null;
        }
        public int compareTo(Node n){
            if (healthMissing != n.healthMissing) return (healthMissing < n.healthMissing) ? -1 : 1;
            if (discardTimeStamp != n.discardTimeStamp) return (discardTimeStamp < n.discardTimeStamp) ? -1 : 1;
            return 0;
        }
    }

    private Node root;

    private static int height(Node n){ return n==null ? 0 : n.height; }
    private static int size (Node n){ return n==null ? 0 : n.size; }
    private static void pull (Node n){
        n.height = Math.max(height(n.left), height(n.right)) + 1;
        n.size = size(n.left) + size(n.right) + 1;
    }
    private static int balanceFactor(Node n){ return height(n.left) - height(n.right);}
    private static int compare(Node a, Node b){ return a.compareTo(b);}
    private static Node minNode(Node n){ while(n!=null && n.left !=null) n=n.left; return n; }

    private static Node rightRotation(Node n){
        Node l1 = n.left;
        Node r_l1 = l1.right;
        l1.right = n;
        n.left = r_l1;
        pull(n);
        pull(l1);
        return l1;
    }
    private static Node leftRotation(Node n){
        Node r1 = n.right;
        Node l_r1 = r1.left;
        r1.left = n;
        n.right = l_r1;
        pull(n);
        pull(r1);
        return r1;
    }


    private Node balance(Node n){
        pull(n);
        int b = balanceFactor(n);
        if (b == 2){
            //check if its LR
            if (balanceFactor(n.left) < 0)
                n.left = leftRotation(n.left); //LR
            return rightRotation(n); //LL
        }
        if (b == -2){
            //check if its RL
            if (balanceFactor(n.right) > 0)
                n.right = rightRotation(n.right); //RL
            return leftRotation(n); //RR
        }
        return n;
    }

    private Node insertNode(Node n, Node x){
        if (n == null) return x;
        int c = compare(x, n);
        if (c < 0)
            n.left = insertNode(n.left, x);
        else
            n.right = insertNode(n.right, x);
        return balance(n);
    }


    private static void swapKey(Node a, Node b){
        int h = a.healthMissing;
        int t = a.discardTimeStamp;
        Card c = a.card;

        a.healthMissing = b.healthMissing;
        a.discardTimeStamp = b.discardTimeStamp;
        a.card= b.card;

        b.healthMissing = h;
        b.discardTimeStamp = t;
        b.card = c;
    }

    private Node deleteNode(Node n, Node x){
        if (n == null) return null;

        int c = compare(x, n);
        if (c < 0){
            n.left = deleteNode(n.left, x);
        } else if (c > 0){
            n.right = deleteNode(n.right, x);
        } else { //found the node
            if (n.left == null) return n.right;
            if (n.right == null) return n.left;

            Node s = minNode(n.right);

            swapKey(n, s);
            n.right = deleteNode(n.right, s);
        }
        return balance(n);
    }


    private Node lowerBoundNode(Node key){
        Node cur = root;
        Node candidate = null;
        while (cur != null){
            if (compare(cur, key) >= 0){ candidate = cur; cur = cur.left; }
            else cur = cur.right;
        }
        return candidate;
    }

    private Node predecessorNodeLowerBound(Node key){
        Node current = root;
        Node candidate = null;
        while (current != null){
            int c = compare(current, key);
            if (c <= 0){
                candidate = current;
                current = current.right;
            }
            else {current = current.left;}
        }
        return candidate;
    }


    public Card findP1(int heal){
        Node n = predecessorNodeLowerBound(new Node(heal,Integer.MAX_VALUE));
        while (n!=null){
            int i = n.healthMissing;
            Node k = lowerBoundNode(new Node(i, Integer.MIN_VALUE));
            if(k!=null && k.healthMissing ==i){
                return k.card;
            }
            n = predecessorNodeLowerBound(new Node(i,Integer.MIN_VALUE));
        }
        return null;
    }

    public Card findP3(int heal){
        Node n = lowerBoundNode(new Node(Integer.MIN_VALUE,Integer.MIN_VALUE));
        if(n==null)
            return null;
        else {
            return n.card;
        }
    }

    public void insertDeathPile(Card c, int discardTs){
        c.discardTimestamp = discardTs;
        root = insertNode(root, new Node(c));
    }

    public void remove(Card c){
        root = deleteNode(root, new Node(c.healthMissing, c.discardTimestamp));
    }

    public int  size(){ return size(root); }
}
