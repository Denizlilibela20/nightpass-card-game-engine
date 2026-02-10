/**
 * Secondary AVL tree index based on card health.
 *
 * Cards are ordered by (current health, timestamp) and used exclusively
 * as a fast precheck to determine whether any card can survive an
 * opponents attack.
 *
 * This avoids unnecessary traversal of the main deck index when
 * survival-based battle priorities are impossible.
 *
 *  This design choice is algorithmically meaningful for large input sizes
 *  and reflects performance aware indexing rather than brute-force scanning.
 */


public class AVLCaseTwo {

    // Health based index sorted by (current health, deck-entry time)
    // Acts as a fast pre-check to determine if survival-based priorities are applicable

    // Used only to quickly check whether any card can survive the opponents attack
    // before attempting survivalbased battle priorities.

    //i used comparable interface, i updated the compareto method
    private static final class Node implements Comparable<Node> {
        int healthCur;
        int timeStamp;
        Card card;
        Node left;
        Node right;
        int height=1;
        int size=1;

        Node(Card card){
            this.healthCur = card.healthCurr;
            this.timeStamp = card.timeStamp;
            this.card = card;
        }
        //for searching in tree
        Node(int healthCur, int timeStamp){
            this.healthCur = healthCur;
            this.timeStamp = timeStamp;
            this.card = null;
        }
        public int compareTo(Node n){
            if (healthCur != n.healthCur) return (healthCur < n.healthCur) ? -1 : 1;
            if (timeStamp != n.timeStamp) return (timeStamp < n.timeStamp) ? -1 : 1;
            return 0;
        }
    }

    private Node root;

    private static int height(Node n){ return n==null ? 0 : n.height;}
    private static int size (Node n){ return n==null ? 0 : n.size;}
    //pull method used to check size and height
    private static void pull (Node n){
        n.height = Math.max(height(n.left), height(n.right)) + 1;
        n.size= size(n.left) + size(n.right) + 1;
    }
    private static Node minNode(Node n){ while(n!=null && n.left !=null) n=n.left; return n;}

    private static int balanceFactor(Node n){ return height(n.left)-height(n.right); }
    private static int compare(Node a, Node b){ return a.compareTo(b);}

    //left heavy
    private static Node rightRotation(Node n){
        Node l1 = n.left;
        Node r_l1 = l1.right;
        l1.right = n;
        n.left = r_l1;
        pull(n);
        pull(l1);
        return l1;
    }

    //right heavy
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
        int bf = balanceFactor(n);
        if (bf == 2){
            //check if its LR
            if (balanceFactor(n.left) < 0)
                n.left = leftRotation(n.left); //LR
            return rightRotation(n); //LL
        }
        if (bf == -2){
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
        if (c < 0) n.left = insertNode(n.left, x);
        else n.right = insertNode(n.right, x);
        return balance(n);
    }
    private static void swapKey(Node a,Node b){
        int a_healthCur = a.healthCur;
        int a_timeStamp = a.timeStamp;
        Card a_card = a.card;

        a.healthCur = b.healthCur;
        a.timeStamp = b.timeStamp;
        a.card = b.card;

        b.healthCur = a_healthCur;
        b.timeStamp = a_timeStamp;
        b.card = a_card;
    }
    private Node deleteNode(Node n,Node x){
        if (n == null) return null;
        int c = compare(x, n);
        if (c < 0){
            n.left = deleteNode(n.left, x);
        } else if (c > 0){
            n.right = deleteNode(n.right, x);
        } else {
            //we found the node
            if (n.left == null) return n.right;// 1 child on right
            if (n.right == null) return n.left;//1 child on left

            Node s = minNode(n.right);
            swapKey(n, s);
            n.right = deleteNode(n.right, s);
        }
        return balance(n);
    }

    private Node successorNode(Node key){
        Node current = root;
        Node candidate = null;
        while (current != null){
            int c = compare(current, key);
            if (c > 0){
                candidate = current;
                current = current.left;}
            else{ current = current.right; }
        }
        return candidate;
    }

    public void insert(Card c){ root = insertNode(root, new Node(c));}
    public void remove(Card c){ root = deleteNode(root, new Node(c.healthCurr, c.timeStamp));}
    public void reindex(Card c, int oldHealth, int oldTimeStamp){
        root = deleteNode(root, new Node(oldHealth, oldTimeStamp));
        root = insertNode(root, new Node(c));
    }

    // Checks whether at least one card can survive the opponent's attack.
    // Used as a guard condition to skip survival based priority searches when no card has sufficient health.
    public boolean existsHGreaterThan(int attackStranger){
        return successorNode(new Node(attackStranger, Integer.MAX_VALUE)) != null;
    }
}
