package com.example.liarsdice;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class Server {

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(8000);
            while (true) {

                Socket player1 = server.accept();
                System.out.println("Player 1 Connected");

                Socket player2 = server.accept();
                System.out.println("Player 2 Connected");

                new Thread(new RunGame(player1, player2)).start();
            }

        } catch (IOException e) {
            System.out.println("Server Failed");
        }
    }

}

class RunGame implements Runnable {
    private Socket p1;
    private Socket p2;
    private DataOutputStream p1Out;
    private DataInputStream p1In;

    private DataOutputStream p2Out;
    private DataInputStream p2In;

    //holds all hands of dice
    private ArrayList<ArrayList<Dice>> Hands = new ArrayList<>();

    //holds the number of dice in each players hand, 5 is defualt
    int player1HandSize = 5;
    int player2HandSize = 5;

    //-1 to accuse 0 to guess face 1 to guess num
    private int choice;

    //default face and num is 1
    private int currentFace = 1;
    private int currentNum = 1;

    private boolean hand = true;

    private int turn = 0;

    //creates a new game
    public RunGame(Socket p1, Socket p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    //starts a new thread
    @Override
    public void run() {
        try {
            //starts game once both players join
            System.out.println("Game Starting");
            p1Out = new DataOutputStream(p1.getOutputStream());
            p1In = new DataInputStream(p1.getInputStream());
            System.out.println("Created p1 in and out");
            p2Out = new DataOutputStream(p2.getOutputStream());
            p2In = new DataInputStream(p2.getInputStream());
            System.out.println("Created p2 in and out");

            //tells players which player they are
            p1Out.writeInt(1);
            p2Out.writeInt(2);

            //keeps game playing until one persons hand is 0
            while (player1HandSize > 0 || player2HandSize > 0) {
                //clears hands and rolls new dice
                hand = true;
                Hands.clear();
                rollDice();
                currentNum = 0;
                currentFace = 0;
                do {
                    //plays a turn
                    turn();
                    //checks if a new hand is needed
                    if (hand == false) {
                        //tells players current round is over
                        p1Out.writeBoolean(false);
                        p2Out.writeBoolean(false);
                    } else {
                        turn++;
                        //tells players current round is still going
                        p1Out.writeBoolean(true);
                        p2Out.writeBoolean(true);
                    }
                } while (hand);

            }
            if(player2HandSize == 0)
                System.out.println("Player 1 wins");
            else
                System.out.println("Player 2 wins");

        } catch (IOException ex) {

        }
    }

    private void turn() {
        try {
            //tells players current face and num
            p1Out.writeInt(currentFace);
            p1Out.writeInt(currentNum);

            p2Out.writeInt(currentFace);
            p2Out.writeInt(currentNum);

            System.out.println("Turn " + turn);

            //checks who turn it is
            if (turn % 2 == 0) {
                //tells p1 it's their turn and p2 it isn't
                p1Out.writeBoolean(true);
                p2Out.writeBoolean(false);

                //gets their choice of what to do
                choice = p1In.readInt();
                System.out.println("Player 1 choice is " + choice);
                if (choice == -1) {
                    accuse();
                    hand = false;
                } else {
                    System.out.println("Player going to guess num");
                    int num = p1In.readInt();
                    System.out.println("p1 guessed num is " + num);
                    if (choice == 0) {
                        currentFace = num;
                        System.out.println(currentFace);
                    } else {
                        currentNum = num;
                    }
                }
            } else {
                //tells p2 it's their turn and p1 it isn't
                p2Out.writeBoolean(true);
                p1Out.writeBoolean(false);

                //gets their choice of what to do
                choice = p2In.readInt();
                System.out.println("Player 2 choice is " + choice);
                if (choice == -1) {
                    accuse();
                    hand = false;
                } else {
                    int num = p2In.readInt();
                    if (choice == 0) {
                        currentFace = num;
                        System.out.println(currentFace);
                    } else {
                        currentNum = num;
                    }
                }
            }
            System.out.println("Current face: " + currentFace + " current num: " + currentNum);
        } catch (IOException e) {
            System.out.println("Error " + e);
        }
    }

    //checks accusation to see who wins the accusation
    private void accuse() {
        int count = 0;
        for (Dice dice : Hands.get(0)) {
            if (dice.getNumber() == currentFace) {
                count++;
            }
        }
        for (Dice dice : Hands.get(1)) {
            if (dice.getNumber() == currentFace) {
                count++;
            }
        }
        System.out.println("There is: " + count + " " + currentFace + " against " + currentNum);
        if (turn % 2 == 0) {
            if (count >= currentNum) {
                System.out.println("Player one lost, player two won");
                player1HandSize--;
            } else {
                System.out.println("Player two lost, player one won");
                player2HandSize--;
            }
        } else {
            if (count >= currentNum) {
                System.out.println("Player two lost, player one won");
                player2HandSize--;
            } else {
                System.out.println("Player one lost, player two won");
                player1HandSize--;
            }
        }
    }

    //sends the dice to the players
    private void sendDice() {
        try {
            p1Out.writeInt(Hands.get(0).size());
            for (Dice dice : Hands.get(0)) {
                try {
                    p1Out.writeInt(dice.getNumber());
                } catch (IOException e) {
                    System.out.println("Failed to send p1 dice");
                }
            }
            p2Out.writeInt(Hands.get(1).size());
            for (Dice dice : Hands.get(1)) {
                try {
                    p2Out.writeInt(dice.getNumber());
                } catch (IOException e) {
                    System.out.println("Failed to send p2 dice");
                }
            }
        } catch (IOException ex) {
            System.out.println("Failed to send dice");
        }
    }
    //rolls new dice for each hand
    private void rollDice() {
        Hands.add(new ArrayList<Dice>());
        for (int i = 0; i < player1HandSize; i++) {
            Hands.get(0).add(new Dice());
            System.out.println("Player 1: " + Hands.get(0).get(i));
        }
        Hands.add(new ArrayList<Dice>());
        for (int i = 0; i < player2HandSize; i++) {
            Hands.get(1).add(new Dice());
            System.out.println("Player 2: " + Hands.get(1).get(i));
        }
        sendDice();
    }

}

