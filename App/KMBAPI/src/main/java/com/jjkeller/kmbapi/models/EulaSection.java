package com.jjkeller.kmbapi.models;

import java.io.Serializable;

/**
 * Created by RDelgado on 5/19/2017.
 * Serializable object to handle the sections on the end-user license agreement
 */

public class EulaSection implements Serializable {

    private int number;
    private int startPosition;
    private String name;

    public EulaSection(int number, int startPosition, String name) {
        this.number = number;
        this.startPosition = startPosition;
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
