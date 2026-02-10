/**
 * Represents a single card in the Nightpass game.
 *
 * A card maintains multiple layers of state:
 * - Initial stats (attackInitial, healthInitial)
 * - Baseline stats (attackBase, healthBase) affected by permanent effects
 * - Current stats (attackCurr, healthCurr) affected by battle damage
 *
 * The class also tracks deck entry and discard timestamps to support
 * deterministic tie breaking rules defined in the specification.
 */
public class Card {
    public final String name;
    public int timeStamp;
    public int discardTimestamp;

    public final int attackInitial;
    public final int healthInitial;

    public int attackBase;
    public int healthBase;

    public int attackCurr;
    public int healthCurr;

    public int revivalProgress;
    public int healthMissing;

    //constructor
    public Card(String name, int attackInitial, int healthInitialize, int timeStampInit){

        this.name = name;
        this.attackInitial = attackInitial;
        this.healthInitial = healthInitialize;
        this.timeStamp = timeStampInit;

        //base has to be initial at the starting
        this.attackBase = attackInitial;
        this.healthBase = healthInitialize;
        this.healthCurr = this.healthBase;
        this.attackCurr = this.attackBase;
    }
}
