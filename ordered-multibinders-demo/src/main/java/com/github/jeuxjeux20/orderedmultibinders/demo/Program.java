package com.github.jeuxjeux20.orderedmultibinders.demo;

import com.github.jeuxjeux20.orderedmultibinders.Order;
import com.github.jeuxjeux20.orderedmultibinders.OrderedMultibinders;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

import java.util.EnumSet;

public class Program {
    public static void main(String[] args) {
        new BakeACakeDemo().run();
    }
}
