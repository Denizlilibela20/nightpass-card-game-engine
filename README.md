Nightpass – Survival Card Game Engine (Java)

A command driven card battle engine implemented in Java, designed for large-scale inputs and strict time constraints.
The project focuses on efficient card selection, deterministic priority rules, and performance-aware data structures, using custom AVL tree indices.

This engine simulates the Nightpass survival card game, where cards are dynamically drawn, battled, discarded, revived, and stolen according to well-defined priority and tie-breaking rules.

Features:
1) Core Gameplay

- Draw cards with initial attack and health values

- Battle against an opponent using 4-tier priority-based selection

- Deterministic tie-breaking using deck-entry timestamps

- Steal cards based on constrained attack/health thresholds

- Track scores and determine the current winner

2) Advanced Mechanics (Type-2)

- Discard pile for defeated cards

- Healing phase with full and partial revivals

- Permanent attack penalties on revival

- Revival prioritization based on missing health


Architecture Overview:

The project is structured with clear separation of concerns:
1) Main.java
- Entry point of the application
- Handles file-based input/output
- Delegates all game logic to DeckOperations

2) DeckOperations.java
- Central game engine
- Processes all commands (draw_card, battle, steal_card, queries)
- Maintains scores, timestamps, and game flow
- Coordinates deck, discard pile, and healing logic

3) Card.java
- Represents a single card entity
Stores:
- Initial stats (Ainit, Hinit)
- Baseline stats (Abase, Hbase)
- Current stats (Acur, Hcur)
- Handles stat updates after battles and revivals

4) AVLCaseOne.java
- Primary deck index
- AVL tree ordered by (Acur, Hcur, timestamp)
- Used for all battle priority (P1–P4) and steal operations
- Enables logarithmic-time selection under large inputs

5) AVLCaseTwo.java
- Secondary AVL index ordered by current health
- Acts as a guard structure to check survivability
- Prevents unnecessary P1/P2 searches when no card can survive
- Improves performance for large-scale scenarios

5) DiscardPile.java
- Manages defeated cards (Hcur = 0)
- Implements healing phase priorities
Supports:
- Full revival
- Partial revival 
- Permanent attack penalties
- Ensures correct reindexing when cards return to the deck


Battle Selection Logic:
When battling the opponent, cards are selected using the following priorities:

1) Priority 1 – Survive and Kill
Hcur > opponentAttack and Acur ≥ opponentHealth
Choose the weakest such card (min attack → min health → earliest entry)

2) Priority 2 – Survive and Deal Maximum Damage
Hcur > opponentAttack and Acur < opponentHealth
Choose the card with maximum attack
Tie-break: minimum health → earliest entry

3) Priority 3 – Kill but Do Not Survive
Hcur ≤ opponentAttack and Acur ≥ opponentHealth
Choose the weakest killing card

4) Priority 4 – Maximum Damage Fallback
No survivable or killing card exists
Choose the card with maximum attack
Tie-break: minimum health → earliest entry

Tie-breaking rule:
Any stat change (battle damage or revival) makes the card count as newly entered.

Healing Phase (Type-2 Only):
After each battle, defeated cards may be revived using a healing pool:
Fully revive the card with the largest missing health that fits the pool
Repeat while possible
If no full revival fits, apply remaining points to the smallest missing health card (partial revive)

Revival Penalties:

Full revive → Abase *= 0.90
Partial revive → Abase *= 0.95
Penalties are permanent and applied multiplicatively



Performance Considerations:

Designed for ~550,000 commands and ~400,000 cards
All major operations run in O(log n) time
Uses custom AVL trees (no Java collections except ArrayList)
Avoids redundant searches via multi-index design


Testing:
All test cases are provided under:
testcase_inputs/

To run all tests and validate outputs:

python3 test_runner.py


Optional:

python3 test_runner.py --type type1
python3 test_runner.py --type type2

Notes:

Deterministic output guaranteed via timestamp based tiebreaking

Any card stat update triggers reindexing

Designed to pass strict time limits under large inputs

even in large case(550k line ofcommands) it runs under 5 seconds.

Disclaimer:

This project was implemented as part of a university-level systems programming assignment.
All code is original and written from scratch with a focus on algorithmic efficiency and clean architecture.
