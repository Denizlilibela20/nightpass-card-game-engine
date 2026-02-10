/**
 * Core game engine responsible for applying all Nightpass rules.
 *
 * This class:
 * Manages battles, scoring, and state transitions
 * Coordinates multiple AVL-based indices for efficient card selection
 * Handles card deaths, reindexing, and revival mechanics
 *
 * It acts as the single authority for game logic, while data structures
 * such as AVL trees encapsulate performance critical operations.
 */


public class DeckOperations {
    private int timeStamp = 0;
    private int discardTimeStamp = 0;

    // AVLCaseOne is the primary AVL index for the deck, ordered by (attackCurr, healthCurr, timeStamp)
    // Used to efficiently select cards for battle priorities and steal operations.
    private final AVLCaseOne p1 = new AVLCaseOne();

    // Secondary AVL index(AVLCaseTwo) ordered by (healthCur, timeStamp)
    // Used only to quickly check whether any card can survive the opponents attack, for better efficiency.
    // before attempting survival based battle priorities.
    // Acts as a fast precheck to determine if survivalbased priorities are applicable.
    private final AVLCaseTwo healthChecker = new AVLCaseTwo();
    private final DiscardPile discardPile = new DiscardPile();
    public int survivorScore = 0;
    public int strangerScore = 0;


    //these method is used for healing phase calculation
    private static int reduceFull(int attackBase){
        return (int)(((long) attackBase * 90L) / 100L);
    }
    //these method is used for healing phase calculation

    private static int reducePartial(int attackBase){
        return (int)(((long) attackBase * 95L) / 100L);
    }
    //these method is used for healing phase calculation
    private static int recomputeAcur(int attackBase, int healthCur, int healthBase){
        long prod = (long) attackBase * (long) healthCur;
        int acur = (int)(prod / (long) healthBase);
        return Math.max(1, acur);
    }

    public void addCard(String name, int attack, int hp){
        Card card = new Card(name, attack,hp,++timeStamp);
        p1.insert(card);
        healthChecker.insert(card);
    }
    public int deckSize(){
        return p1.getRootSize();
    }

    public int discardPileCount(){
        return discardPile.size();
    }

    public String battle(int attackStranger, int healthStranger, int heal){
        Card c = null;
        int prioUsed = 0;
        int revivedCount = 0;
        //checking heal to prevent unnecesary search( for p3 and p4)
        if (healthChecker.existsHGreaterThan(attackStranger)) {
            if((c = p1.pickCardP1(attackStranger, healthStranger)) != null) { prioUsed = 1;}
            else if ((c = p1.pickCardP2(attackStranger, healthStranger)) != null){prioUsed = 2; }
        }
        if (c == null) {
            if ((c = p1.pickCardP3(attackStranger, healthStranger)) != null){ prioUsed = 3; }
            else if ((c = p1.pickCardP4()) != null) { prioUsed = 4; }
            else {
                return "No card to play "+revivedCount+ "cards revived";
            }
        }

        // war calculations
        int myHealthAfter =c.healthCurr - attackStranger;
        int StangerHealthAfter = healthStranger - c.attackCurr;

        boolean myDead=(myHealthAfter <= 0);
        boolean strangerDead=(StangerHealthAfter <= 0);

        //different score situations
        if (myDead) strangerScore += 2;
        if (strangerDead) survivorScore += 2;
        if (!myDead && myHealthAfter < c.healthCurr) strangerScore += 1;
        if (!strangerDead && StangerHealthAfter < healthStranger) survivorScore += 1;

        //update the deck
        if (myDead){
            p1.remove(c);
            healthChecker.remove(c);
            c.healthCurr = 0;
            c.healthMissing = c.healthBase;
            discardPile.insertDeathPile(c,++discardTimeStamp);
        }
        else {
            // we are so alive, letsgooo boisssss
            //however, we still have to do penalties
            int oldHealth = c.healthCurr;
            int oldTimeStamp = c.timeStamp;
            int oldAttack = c.attackCurr;

            // calculate heal
            int newHeal = Math.max(0, Math.min(myHealthAfter, c.healthBase));
            long temp = (long)c.attackBase * (long)newHeal;
            int newAttack = (int)Math.max(1L, temp / (long)c.healthBase);

            //do deleting and readding if these are changed
            if (newHeal != oldHealth || newAttack != oldAttack) {
                p1.reindex(c, newAttack, newHeal, ++timeStamp);
                healthChecker.reindex(c, oldHealth, oldTimeStamp);
            }
        }

        //healing phase
        if (heal > 0) {
            boolean loopCheck = false;
            Card revived = discardPile.findP1(heal);
            if (revived == null) loopCheck = true;
            //for p1 and p2
            while (!loopCheck) {
                int cost = revived.healthMissing;
                discardPile.remove(revived);

                revived.attackBase = reduceFull(revived.attackBase);
                revived.healthCurr = revived.healthBase;
                revived.healthMissing = 0;
                revived.attackCurr = recomputeAcur(revived.attackBase, revived.healthCurr, revived.healthBase);

                revived.timeStamp = ++timeStamp;
                p1.insert(revived);
                healthChecker.insert(revived);

                heal -= cost;
                revivedCount++;
                revived = (heal > 0) ? discardPile.findP1(heal) : null;
                if (revived == null) loopCheck = true;
            }

            // for p3
            if(heal>0) {
                Card target = discardPile.findP3(heal);
                if (target != null) {
                    discardPile.remove(target);
                    target.attackBase = reducePartial(target.attackBase);
                    target.healthMissing -= heal;
                    target.attackCurr = recomputeAcur(target.attackBase, target.healthCurr, target.healthBase);
                    target.discardTimestamp = ++discardTimeStamp;
                    discardPile.insertDeathPile(target, discardTimeStamp);
                    heal  = 0;
                }
            }
        }


        // output format
        if(!myDead) return "Found with priority " + prioUsed + ", Survivor plays " + c.name
                + ", the played card returned to deck, "+revivedCount+" cards revived";
        else return "Found with priority " + prioUsed + ", Survivor plays " + c.name
                + ", the played card is discarded, "+revivedCount+" cards revived";
    }


    // stealcard
    public Card stealCard(int attackLimit, int healthLimit){
        Card c = p1.pickSteal(attackLimit, healthLimit);
        if (c == null) return null;
        p1.remove(c);
        healthChecker.remove(c);
        return c;
    }


    // find winner
    public String findWinning(){
        if (survivorScore >= strangerScore) {
            return "The Survivor, Score: " + survivorScore;
        } else {
            return "The Stranger, Score: " + strangerScore;
        }
    }

}
