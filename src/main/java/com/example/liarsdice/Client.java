package com.example.liarsdice;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {

    //creates a new game
    public static void main(String[] args) {
        playGame game = new playGame();
    }

}

class playGame {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    //holds the dice in hand
    private ArrayList<Dice> hand = new ArrayList<>();
    //hand size
    private int handSize;
    private int playerNum;
    private int currentFace;
    private int currentNum;
    private Scanner input;

    public playGame() {
        try {
            input = new Scanner(System.in);

            //connects client to server
            socket = new Socket("localhost", 8000);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            //gets players number
            playerNum = in.readInt();
            System.out.println("Welcome Player: " + playerNum);

            while (true) {
                //fills hand with dice from server
                fillHand();
                boolean newRound = true;

                do {
                    //reads in the current face and number
                    currentFace = in.readInt();
                    currentNum = in.readInt();

                    System.out.println("Current face: " + currentFace + " current num: " + currentNum);

                    //checks if it is your turn
                    boolean isTurn = in.readBoolean();
                    if (isTurn == true) {
                        //if turn play out turn
                        playTurn();
                    }
                    //checks to see if it is a new round or not
                    newRound = in.readBoolean();
                } while (newRound);

            }
        } catch (IOException e) {

        }
    }

    public void playTurn() {
        try {
            System.out.println("Please enter in -1 to accuse, 0 to guess face, 1 to guess num");
            //gets guess option
            int guess = input.nextInt();
            out.writeInt(guess);
            if (guess == 0) {
                //if 0 prompts for a valid face guess
                System.out.println("Please enter a face ");
                int faceGuess = input.nextInt();
                while (faceGuess <= currentFace) {
                    System.out.println("Please enter a face ");
                    faceGuess = input.nextInt();
                }
                out.writeInt(faceGuess);
                System.out.println("Guessed " + faceGuess);
            } else if (guess == 1) {
                //if 1 prompts for a valid number guess
                System.out.println("Please enter a number ");
                int numGuess = input.nextInt();
                while (numGuess <= currentNum) {
                    System.out.println("Please enter a number ");
                    numGuess = input.nextInt();
                }
                out.writeInt(numGuess);
            }

        } catch (IOException e) {

        }
    }

    public void fillHand() {
        //reads in hand from server
        try {
            handSize = in.readInt();
            for (int i = 0; i < handSize; i++) {
                hand.add(new Dice(in.readInt()));
                System.out.println(hand.get(i));
            }
        } catch (IOException e) {

        }

        System.out.println("Current hand size: " + handSize);
    }
}


