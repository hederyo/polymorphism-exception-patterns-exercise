package com.parcel;

import com.parcel.ui.ConsoleMenu;

import java.util.Scanner;

/**
 * Main application entry point for the Parcel Delivery System.
 */
public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleMenu menu = new ConsoleMenu(scanner);
            menu.start();
        }
    }
}
