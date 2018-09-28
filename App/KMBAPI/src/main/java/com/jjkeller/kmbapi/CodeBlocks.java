package com.jjkeller.kmbapi;

import java.io.IOException;

/**
 * Created by jld5296 on 9/19/16.
 */

public final class CodeBlocks {
    public interface Action {
        void execute();

    }
    public interface Action1<T1> {
        void execute(T1 input1);

    }
    public interface Action2<T1, T2> {
        void execute(T1 input1, T2 input2);

    }
    public interface Action3<T1, T2, T3> {
        void execute(T1 input1, T2 input2, T3 input3);

    }
    public interface Func0<TResult> {
        TResult execute();
    }
    public interface Func1<T1, TResult>  {
        TResult execute(T1 input1);
    }
    public interface Func1IOException<T1, TResult> {
        TResult execute(T1 input1) throws IOException;
    }
    public interface Func2 <T1, T2, TResult> {
         TResult execute(T1 input1, T2 input2);
    }
    public interface Func3 <T1, T2, T3, TResult> {
         TResult execute(T1 input1, T2 input2, T3 input3);
    }
}
