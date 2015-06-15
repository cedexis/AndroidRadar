package com.cedexis.simpleradardemo;

import android.util.Pair;

import java.util.List;

public class TupleSearcher<S,T> {

    Iterable<Pair<S, T>> _data;

    public TupleSearcher(Iterable<Pair<S, T>> _data) {
        this._data = _data;
    }

    public Pair<S, T> search(S firstValue) {
        for (Pair<S, T> pair : _data) {
            if (pair.first.equals(firstValue)) {
                return pair;
            }
        }
        return null;
    }
}
