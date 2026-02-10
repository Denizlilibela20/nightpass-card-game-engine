/**
 * Primary AVL tree index for the active deck.
 *
 * Cards are ordered by (current attack, current health, timestamp),
 * enabling efficient O(log n) selection for:
 *  - Battle priority rules
 *  - Steal card operations
 *
 * This structure is optimized for complex multicriteria queries
 * required during combat resolution.
 */


public class AVLCaseOne {
    //i used comparable interface for checking A H and Ts and overrided the method compareto
    private static final class Node implements Comparable<Node> {
        int attackCur;
        int healthCur;
        int timeStamp;
        Card card;
        Node left;
        Node right;
        int height = 1;
        int size = 1;

        //consturactor
        Node(Card card){
            this.attackCur = card.attackCurr;
            this.healthCur = card.healthCurr;
            this.timeStamp = card.timeStamp;
            this.card = card;
        }

        // consturactor used in searches
        Node(int attackCur, int healthCur, int ts){
            this.attackCur = attackCur;
            this.healthCur = healthCur;
            this.timeStamp = ts;
            this.card = null;
        }

        //comparable interface override
        public int compareTo(Node n) {
            if (attackCur != n.attackCur) return (attackCur < n.attackCur) ? -1 : 1;
            if (healthCur != n.healthCur) return (healthCur < n.healthCur) ? -1 : 1;
            if (timeStamp != n.timeStamp) return (timeStamp < n.timeStamp) ? -1 : 1;
            return 0;
        }
    }

    private Node root;

    public int getRootSize (){
        if(root == null) return 0;
        return root.size;
    }

    private static int height(Node n){
        if(n==null) return 0;
        else return n.height;
    }

    private static int size(Node n){
        if(n==null) return 0;
        else return n.size;}


    private static void pull(Node n){
        n.height = Math.max(height(n.left), height(n.right)) + 1;
        n.size = size(n.left) + size(n.right) + 1;
    }

    //if bf==2 means left if bf == -2 means right is unbalanced
    private static int balanceFactor(Node n){ return height(n.left) - height(n.right);}
    private static int compare(Node a, Node b){ return a.compareTo(b);}
    private static Node minNode(Node n){ while(n!=null && n.left !=null) n=n.left; return n;}
    private static Node maxNode(Node n){ while(n!=null && n.right !=null) n=n.right; return n;}



    //left heavy case for AVL tree
    private static Node rightRotation(Node n){
        Node l1 = n.left;
        Node r_l1 = l1.right;
        l1.right = n;
        n.left = r_l1;
        pull(n);
        pull(l1);
        return l1;
    }

    //right heavy case for AVL
    private static Node leftRotation(Node n){
        Node r1 = n.right;
        Node l_r1 = r1.left;
        r1.left = n;
        n.right = l_r1;
        pull(n);
        pull(r1);
        return r1;
    }

    //balancing the tree
    private Node balance(Node n){
        pull(n);
        int bf = balanceFactor(n);
        if (bf == 2){
            //check if its LR case
            if (balanceFactor(n.left) < 0)
                n.left = leftRotation(n.left); //LR case
            return rightRotation(n); //LL case
        }
        if (bf == -2){
            //check if its RL case
            if (balanceFactor(n.right) > 0)
                n.right = rightRotation(n.right); //RL case
            return leftRotation(n); //RR case
        }
        return n;
    }

    //inserting a new node to tree
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
        int a_attackCur = a.attackCur;
        int a_healthCur = a.healthCur;
        int a_timeStamp = a.timeStamp;
        Card a_card = a.card;

        a.attackCur = b.attackCur;
        a.healthCur = b.healthCur;
        a.timeStamp = b.timeStamp;
        a.card = b.card;

        b.attackCur = a_attackCur;
        b.healthCur = a_healthCur;
        b.timeStamp = a_timeStamp;
        b.card = a_card;

    }
    private Node deleteNode(Node n, Node x){
        if (n == null) return null;
        int c = compare(x, n);
        if (c < 0){
            n.left = deleteNode(n.left, x);
        } else if (c > 0){
            n.right = deleteNode(n.right, x);
        } else {
            //we found the node
            if (n.left ==null) return n.right;// 1 child on right
            if (n.right ==null) return n.left;//1 child on left

            Node s = minNode(n.right);
            swapKey(n, s);
            n.right = deleteNode(n.right, s);
        }
        return balance(n);
    }


    //lower node finder
    private Node lowerBoundNode(Node n){
        Node current = root;
        Node candidate = null;
        while (current != null){
            if (compare(current, n) >= 0){
                candidate = current;
                current = current.left;
            }
            else
                current = current.right;
        }
        return candidate;
    }

    private Node sucessorNode(Node n){
        Node current = root;
        Node candidate = null;
        while (current != null){
            if (compare(current, n) > 0){
                candidate = current;
                current = current.left;
            }
            else
                current = current.right;
        }
        return candidate;
    }


    //for accessing to insert and remove from other classes
    public void insert(Card c){ root = insertNode(root,new Node(c));}
    public void remove(Card c){ root = deleteNode(root,new Node(c));}


    //first node which is < than the key
    private Node predecessorNode(Node key){
        Node current = root;
        Node candidate = null;
        while (current != null){
            int c = compare(current, key);
            if (c < 0){
                candidate = current;
                current = current.right;
            }
            else {current = current.left;}
        }
        return candidate;
    }


    /**
     * Priority 1 selection:
     * Selects the weakest card that can both survive the attack and defeat the opponent.
     *  * Tiebreaking order:
     *  * - Minimum attack
     *  * - Minimum health
     *  * - Earliest deck entry timestamp
     */
    public Card pickCardP1(int attackStranger, int healthStanger){
        //left node of A >= hstr
        //a minimal if same H minimal if same timeStamp minimal
        Node n = lowerBoundNode(new Node(healthStanger, Integer.MIN_VALUE, Integer.MIN_VALUE));

        while (n != null){
            int a = n.attackCur;
            //while A constant first node which H> attackStranger
            //h minimal if same timeStamp minimal
            Node j = sucessorNode(new Node(a, attackStranger, Integer.MAX_VALUE));
            if (j != null && j.attackCur == a) {
                return j.card;
            }
            //doesnt exist so check next node
            n = sucessorNode(new Node(a, Integer.MAX_VALUE, Integer.MAX_VALUE));
        }
        return null;
    }

    /**
     * Priority 2 (Survive and Deal Maximum Damage).
     *
     * Selects a card that:
     * - Can survive the opponents attack (healthCurr > attackStranger)
     * - Cannot defeat the opponent (attackCurr < healthStranger)
     *
     * Tiebreaking order:
     * - Maximum attack
     * - Minimum health
     * - Earliest deck entry timestamp
     */
    public Card pickCardP2(int attackStranger, int healthStranger){
        //move to the first block which satisfies A < healthStranger
        Node n = predecessorNode(new Node(healthStranger, Integer.MIN_VALUE, Integer.MIN_VALUE));

        while (n != null){
            int a = n.attackCur;
            //go to the first node which h > attackStranger (h min if same timeStamp min)
            Node j = sucessorNode(new Node(a, attackStranger, Integer.MAX_VALUE));

            if (j != null && j.attackCur == a){
                //for tiebreak
                return j.card;
            }

            //node doesnt exist, go to the prev A node for since we go from high to low this time
            n = predecessorNode(new Node(a, Integer.MIN_VALUE, Integer.MIN_VALUE));
        }
        return null;
    }

    /**
     * Priority 3 (Kill but Do Not Survive).
     *
     * Selects the weakest card that:
     * - Can defeat the opponent (attackCurr >= healthStranger)
     * - Cannot survive the opponents attack (healthCurr <= attackStranger)
     *
     * Tiebreaking order:
     * - Minimum attack
     * - Minimum health
     * - Earliest deck entry timestamp
     */
    public Card pickCardP3(int attackStranger, int healthStranger){
        //starts from the leftmost node of the block where A >= healthStranger
        Node i = lowerBoundNode(new Node(healthStranger, Integer.MIN_VALUE, Integer.MIN_VALUE));

        while (i != null){
            int a = i.attackCur;

            //the first element of this a block
            //minimal H if not minimal timeStamp
            Node f = lowerBoundNode(new Node(a, Integer.MIN_VALUE, Integer.MIN_VALUE));

            if (f != null && f.attackCur == a){
                //since H is increasing if the first element has h<=attackStranger
                //its valid otherwise no card in this A block can be valid.
                if (f.healthCur <= attackStranger) return f.card;
            }

            //check next block
            i = sucessorNode(new Node(a, Integer.MAX_VALUE, Integer.MAX_VALUE));
        }
        return null;
    }

    /**
     * Priority 4 (Maximum Damage Fallback).
     *
     * Used when no card can survive the battle.
     *
     * Selects the card with:
     * - Maximum attack
     *
     * Tie-breaking order:
     * - Minimum health
     * - Earliest deck-entry timestamp
     */
    public Card pickCardP4(){
        Node max = maxNode(root);
        if (max == null) return null;

        int a = max.attackCur;
        //the node with minimal h in the same A block
        Node best = lowerBoundNode(new Node(a, Integer.MIN_VALUE, Integer.MIN_VALUE));
        if (best != null && best.attackCur == a) return best.card;
        return max.card;
    }


    /**
     * Selects a card for the steal operation.
     *
     * Chooses the weakest card such that:
     * - attackCurr > attackLimit
     * - healthCurr > healthLimit
     *
     * Tie-breaking order:
     * - Minimum attack
     * - Minimum health
     * - Earliest deck-entry timestamp
     */
    public Card pickSteal(int aLimit, int hLimit){
        //leftmost of A > aLimit
        Node n = sucessorNode(new Node(aLimit, Integer.MAX_VALUE, Integer.MAX_VALUE));

        while (n != null){
            int a = n.attackCur;

            //The first node in the same A block with H > hLimit
            Node j = lowerBoundNode(new Node(a, hLimit, Integer.MAX_VALUE));
            if (j != null && j.attackCur == a){
                return j.card; // A min (>aLimit) H min (>hLimit) timeStamp min
            }
            //next A block
            n = sucessorNode(new Node(a, Integer.MAX_VALUE, Integer.MAX_VALUE));
        }
        return null;
    }

    //deleting and adding again the card (updates the tree)
    public void reindex(Card c, int newAttack, int newHealth, int newTimeStamp){
        remove(c);
        c.attackCurr = newAttack;
        c.healthCurr = newHealth;
        c.timeStamp = newTimeStamp;
        insert(c);
    }
}
