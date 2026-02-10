
/**
 * CMPE 250 Project 1 - Nightpass Survivor Card Game
 * Entry point of the Nightpass Survivor Card Game.
 *
 * Responsible only for:
 * - Reading commands from the input file
 * - Parsing and routing commands to the game engine
 * - Writing formatted outputs to the output file
 *
 * All game rules, data structures, and state management are delegated
 * to DeckOperations to keep I/O and game logic clearly separated.
 *
 * @author Yiğit Özel
 */

import java.io.*;
import java.util.Scanner;

public class Main {
    private static DeckOperations operator = new DeckOperations();

    private static String draw_card(String name, int att, int hp) {
        operator.addCard(name, att, hp);
        return "Added " + name + " to the deck";
    }

    private static String deckCount() {
        return "Number of cards in the deck: " + String.valueOf(operator.deckSize());
    }

    private static String steal_card(int aLim, int hLim) {
        Card stolen = operator.stealCard(aLim, hLim);
        if (stolen == null) return "No card to steal";
        return "The Stranger stole the card: " + stolen.name;
    }

    private static String battle(int att, int hp, int heal) {
        return operator.battle(att, hp, heal);
    }

    private static String findWinning() {
        return operator.findWinning();
    }
    private static String discardPileCount(){
        return "Number of cards in the discard pile: " + String.valueOf(operator.discardPileCount());
    }

    public static void main(String[] args) {
        // Check command line arguments
        if (args.length != 2) {
            System.out.println("Usage: java Main <input_file> <output_file>");
            System.out.println("Example: java Main ../testcase_inputs/test.txt ../output/test.txt");
            return;
        }

        String inFile = args[0];
        String outFile = args[1];

        // Initialize file reader
        Scanner reader = null;
        try {
            reader = new Scanner(new File(inFile));
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found: " + inFile);
            e.printStackTrace();
            return;
        }

        // Initialize file writer
        FileWriter writer = null;
        try {
            writer = new FileWriter(outFile);
        } catch (IOException e) {
            System.out.println("Writing error: " + outFile);
            e.printStackTrace();
            if (reader != null)
                reader.close();
            return;
        }

        // Process commands line by line
        try {
            while (reader.hasNext()) {
                String line = reader.nextLine();
                Scanner scanner = new Scanner(line);
                String command = scanner.next();
                String out = "";

                switch (command) {
                    case "draw_card": {
                        String name = "";
                        int att = 0;
                        int hp = 0;
                        if (scanner.hasNext())
                            name = scanner.next();
                        if (scanner.hasNext())
                            att = scanner.nextInt();
                        if (scanner.hasNext())
                            hp = scanner.nextInt();
                        out = draw_card(name, att, hp); // suggested method for draw_card command
                        break;
                    }
                    case "battle": {
                        int att = 0;
                        int hp = 0;
                        int heal = 0;
                        if (scanner.hasNext())
                            att = scanner.nextInt();
                        if (scanner.hasNext())
                            hp = scanner.nextInt();
                        if (scanner.hasNext())
                            heal = scanner.nextInt();
                        out = battle(att, hp, heal); // suggested method for battle command
                        break;
                    }
                    case "find_winning": {
                        out = findWinning(); // suggested method for find_winning command
                        break;
                    }
                    case "deck_count": {
                        out = deckCount(); // suggested method for deck_count command
                        break;
                    }


                     case "discard_pile_count": {
                        out = discardPileCount(); // suggested method for discard_pile_count command
                        break;
                     }
                    case "steal_card": {
                        int att = 0;
                        int hp = 0;
                        if (scanner.hasNext())
                            att = scanner.nextInt();
                        if (scanner.hasNext())
                            hp = scanner.nextInt();
                        out = steal_card(att, hp); // suggested method for steal_card command
                        break;
                    }
                    default: {
                        System.out.println("Invalid command: " + command);
                        scanner.close();
                        writer.close();
                        reader.close();
                        return;
                    }
                }

                scanner.close();

                try {
                    writer.write(out);
                    writer.write("\n");
                } catch (IOException e2) {
                    System.out.println("Writing error");
                    e2.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.out.println("Error processing commands: " + e.getMessage());
            e.printStackTrace();
        }

        // Clean up resources
        try {
            writer.close();
        } catch (IOException e2) {
            System.out.println("Writing error");
            e2.printStackTrace();
        }

        if (reader != null) {
            reader.close();
        }

        System.out.println("end");
        return;
    }
}
