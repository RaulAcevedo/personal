package com.jjkeller.kmbapi.controller.dataaccess;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class AbstractDBAdapterTest {

    @Test
    public void removeColumnFromCreateStatement_should_remove_first_column() throws Exception {
        String createStatement = "create table test (Key integer primary key autoincrement, ColumnA string, ColumnB integer not null, ColumnC boolean not null default false, ColumnD boolean);";

        String result = AbstractDBAdapter.removeColumnFromCreateStatement(createStatement, "Key");

        String expectedStatement = "create table test ( ColumnA string, ColumnB integer not null, ColumnC boolean not null default false, ColumnD boolean);";
        assertEquals(expectedStatement, result);
    }

    @Test
    public void removeColumnFromCreateStatement_should_remove_middle_column() throws Exception {
        String createStatement = "create table test (Key integer primary key autoincrement, ColumnA string, ColumnB integer not null, ColumnC boolean not null default false, ColumnD boolean);";

        String result = AbstractDBAdapter.removeColumnFromCreateStatement(createStatement, "ColumnA");
        String expectedStatement = "create table test (Key integer primary key autoincrement, ColumnB integer not null, ColumnC boolean not null default false, ColumnD boolean);";
        assertEquals(expectedStatement, result);

        result = AbstractDBAdapter.removeColumnFromCreateStatement(createStatement, "ColumnB");
        expectedStatement = "create table test (Key integer primary key autoincrement, ColumnA string, ColumnC boolean not null default false, ColumnD boolean);";
        assertEquals(expectedStatement, result);

        result = AbstractDBAdapter.removeColumnFromCreateStatement(createStatement, "ColumnC");
        expectedStatement = "create table test (Key integer primary key autoincrement, ColumnA string, ColumnB integer not null, ColumnD boolean);";
        assertEquals(expectedStatement, result);
    }

    @Test
    public void removeColumnFromCreateStatement_should_remove_last_column() throws Exception {
        String createStatement = "create table test (Key integer primary key autoincrement, ColumnA string, ColumnB integer not null, ColumnC boolean not null default false, ColumnD boolean);";

        String result = AbstractDBAdapter.removeColumnFromCreateStatement(createStatement, "ColumnD");

        String expectedStatement = "create table test (Key integer primary key autoincrement, ColumnA string, ColumnB integer not null, ColumnC boolean not null default false);";
        assertEquals(expectedStatement, result);
    }

    @Test
    public void removeColumnFromCreateStatement_should_handle_new_lines() throws Exception {
        String createStatement = "create table test (Key integer primary key autoincrement,\nColumnA string,\nColumnB integer not null,\nColumnC boolean not null default false,\nColumnD boolean);";

        String result = AbstractDBAdapter.removeColumnFromCreateStatement(createStatement, "ColumnB");

        String expectedStatement = "create table test (Key integer primary key autoincrement,\nColumnA string,\nColumnC boolean not null default false,\nColumnD boolean);";
        assertEquals(expectedStatement, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeColumnFromCreateStatement_should_throw_exception_if_column_does_not_exist() throws Exception {
        String createStatement = "create table test (Key integer primary key autoincrement, ColumnA string);";

        AbstractDBAdapter.removeColumnFromCreateStatement(createStatement, "DoesNotExist");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeColumnFromCreateStatement_should_throw_exception_when_trying_to_drop_the_only_column_in_a_table() throws Exception {
        String createStatement = "create table test (LastColumn integer);";

        AbstractDBAdapter.removeColumnFromCreateStatement(createStatement, "LastColumn"); // should throw
    }
}
