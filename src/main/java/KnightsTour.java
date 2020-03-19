import com.sun.tools.javac.util.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KnightsTour {

    public static void main(String[] args) {

        //Vytvorim si stack, krasa Javy a inych jazykov je ze vacsina veci je implemntovanych v zakladnom baliku
        Stack<Board> stackOfBoards = new Stack<>();

        //Vytvorim prvy board, a dam mu odkial startujem
        Board first = new Board(5, 5, 2, 2);

        //Pushnem do stacku
        stackOfBoards.push(first);

        //Spravim si list parov koordinatov, ked by ta zaujimalo, Pair je objekt, ktory v sebe vie udrzat dva dalsie objekty, ktore dokopy davaju nejaky zmysel, ako napriklad suradnice
        List<Pair<Integer, Integer>> coords = new ArrayList<>();


        //Hmm ideme dovtedy pokial sa nevyprazdni stack, to znamena predjem kazdy stav, mam tzv pazravy algoritmus, kedze chcem prejst vsatky (Nepripomina ti to popolvara? Hej pripomina, je to ten isty princip)
        while (!stackOfBoards.isEmpty()) {
            //Zoberiem z vrchu stacku
            Board actualBoard = stackOfBoards.pop();

            //Ak som skoncil, tak proste len zozbieram koordinaty mojej cesty na fancy vypis a breaknem loop.
            if (actualBoard.isFinished()) {
                System.out.println("Dokoncili sme to");

                Board toPrint = actualBoard;
                while (toPrint != null) {
                    coords.add(Pair.of(toPrint.actualX, toPrint.actualY));
                    toPrint = toPrint.previousBoard;
                }
                break;
            }

            //Vygenerujem si podla heuristiky dalsie tahy
            List<Board> nextPossibleBoards = actualBoard.generateBestMovesForHeuristics();

            //Pushnem ich do stacku
            for (Board nextPossibleBoard : nextPossibleBoards) {
                stackOfBoards.push(nextPossibleBoard);
            }
        }

        // otocim to lebo idem odzadu :P
        Collections.reverse(coords);

        //Vypisem koordinaty
        for (Pair<Integer, Integer> coord : coords) {
            System.out.println(coord.fst + " " + coord.snd);
        }

    }
}

class Board {
    private int[][] plocha;
    Board previousBoard;
    int actualX;
    int actualY;
    private int rows, cols;

    Board(int rows, int cols, int startX, int startY) {
        this.rows = rows;
        this.cols = cols;

        plocha = new int[rows][cols];
        previousBoard = null;

        actualX = startX;
        actualY = startY;

        markActualSpot();
    }

    private Board(Board original) {
        plocha = new int[original.rows][original.cols];
        rows = original.rows;
        cols = original.cols;
        //Copy the array
        for (int row = 0; row < plocha.length; row++) {
            System.arraycopy(original.plocha[row], 0, plocha[row], 0, plocha[row].length);
        }
    }

    void printBoard() {
        for (int row = 0; row < plocha.length; row++) {
            for (int col = 0; col < plocha[row].length; col++) {
                System.out.print(plocha[row][col] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private void markActualSpot() {
        plocha[actualY][actualX] = 1;
    }

    boolean isFinished() {
        //Som finished ak cela moja plocha obsahuje 1
        for (int row = 0; row < plocha.length; row++) {
            for (int col = 0; col < plocha[row].length; col++) {
                if (plocha[col][row] == 0) return false;
            }
        }
        return true;
    }

    List<Board> generateBestMovesForHeuristics() {
        //Vygenerujeme si Prvy level
        List<Board> firstLevelBoards = generateNextLevel();

        //Ak nemam ziadne moznosti tak vratim prazdny list
        if (firstLevelBoards.isEmpty()) return Collections.emptyList();

        //Najdem Boards pre ktore je najmensi pocet moznosti z nich ->      Z listu spravim stream (nieco ako list), na ten namapujem kolko moznosti je pre kazdy, zoradim to od najmensieho po najvacsie a vyberiem najmensie
        int lowestMoves = firstLevelBoards.stream().map(board -> Pair.of(board, board.generateNextLevel().size())).sorted(Comparator.comparing(o -> o.snd)).min(Comparator.comparing(o -> o.snd)).get().snd;

        //Tu spravim skoro to iste ako hore, az na to, ze si po namapovani len vyfiltrujem tie dosky, ktore maju rovnaky pocet moznosti ako najmensia (Toto je ta heuristika)
        //Ak to budes chciet zmenit na vsetky moznosti, tak daj prec ten filter
        List<Pair<Board, Integer>> pairs = firstLevelBoards.stream().map(board -> Pair.of(board, board.generateNextLevel().size())).filter(pair -> pair.snd == lowestMoves).collect(Collectors.toList());

        return pairs.stream().flatMap((Function<Pair<Board, Integer>, Stream<Board>>) boardIntegerPair -> Stream.of(boardIntegerPair.fst)).collect(Collectors.toList());
    }

    private List<Board> generateNextLevel() {
        List<Board> boards = new ArrayList<>();
        //Chod pre kazdy pohyb kona
        for (int i = 0; i < 8; i++) {
            int newX = actualX + Utils.moveX[i];
            int newY = actualY + Utils.moveY[i];

            if (canMoveToSpot(newX, newY)) {
                //Mozeme si vygenerovat dalsie stavy

                //Toto len nakopci to pole, kedze Java je jebnuta v tomto ako Ccko, tak ked by si priradila len referenciu, tak si menis hodnoty aj v original objekte co nechces. Nech ziju pointery -_-
                Board newBoard = new Board(this);

                //Nastavim aktualnu poziciu
                newBoard.actualX = newX;
                newBoard.actualY = newY;

                //Nastavim pointer na predchadzajuci kvoli backtrackingu
                newBoard.previousBoard = this;


                //Oznacim aktualne miesto ako prejdene
                newBoard.markActualSpot();


                //Pridam do listu
                boards.add(newBoard);
            }
        }
        return boards;
    }

    private boolean canMoveToSpot(int newPosX, int newPosY) {
        //Pozeram ci som bud mimo dosky alebo ci som na danom mieste uz bol aby som sa necyklil
        return !(newPosX < 0 || newPosX >= cols || newPosY < 0 || newPosY >= rows || plocha[newPosY][newPosX] == 1);
    }
}

class Utils {
    // Doprava a dole je kladne, dolava a hore je zaporne cislo
    static int[] moveX = {1, 1, 2, 2, -1, -1, -2, -2};
    static int[] moveY = {2, -2, 1, -1, 2, -2, 1, -1};
}
