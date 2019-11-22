package com.cursoudemy.tictactoe.model;

public class User {
    private String name;
    private int points;
    private int games;

    public User() {
    }

    public User(String name, int points, int games) {
        this.name = name;
        this.points = points;
        this.games = games;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getGames() {
        return games;
    }

    public void setGames(int games) {
        this.games = games;
    }
}
