package com.example.trackexpence.utilities;

import com.example.trackexpence.models.expenses.MyExpenses;

import java.util.Comparator;

public class DataComparatorE implements Comparator<MyExpenses> {
    @Override
    public int compare(MyExpenses item1, MyExpenses item2) {
        // compare the dates of item1 and item2
        return item2.getDate().compareTo(item1.getDate());
    }
}
