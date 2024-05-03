package me.awie1000.stackcalc;

import org.checkerframework.checker.units.qual.N;

import java.util.*;

public class Calculator {

    final int DEPTH_MAX = 10;
    public final static String[] operationList = {".", "rev", "dup", "+", "-", "*", "/", "//", "%"};
    Stack<Number> stack;
    Macros macros;
    PlayerPrinter out;

    public Calculator(PlayerPrinter out, Macros macros) {
        this.stack = new Stack<>();
        this.macros = macros;
        this.out = out;
    }

    static boolean isOperation(String token) {
        return token.matches("^([+]|-|[*]|[.]|/|%|//|rev|dup)$");
    }

    static boolean isNumber(String token) {
        return token.matches("^[+-]?([0-9]*[.])?[0-9]+$");
    }

    private void doOperation(String token) throws CalcError {
        try {
            //misc operators
            switch (token) {
                case ".": // 1 arg
                    Number num = stack.pop();
                    out.print(num.toString());
                    return;
                case "rev": // 1 + n args
                    int count = stack.pop().toInt();
                    Number[] numList = new Number[count];
                    for(int i = 0; i < count; i++) {
                        numList[i] = stack.pop();
                    }
                    for(int i = 0; i < count; i++) {
                        stack.push(numList[i]);
                    }
                    return;
                case "dup": // 1 arg
                    stack.push(Number.copy(stack.peek()));
                    return;
            }

            //binary operators
            Number num2 = stack.pop();
            Number num1 = stack.pop();

            switch (token) {
                case "+":
                    stack.push(num1.add(num2));
                    return;
                case "-":
                    stack.push(num1.sub(num2));
                    return;
                case "*":
                    stack.push(num1.mul(num2));
                    return;
                case "/":
                    stack.push(num1.div(num2));
                    return;
                case "//":
                    stack.push(num1.intDiv(num2));
                    return;
                case "%":
                    stack.push(num1.mod(num2));
                    return;
                default:
                    //SHOULD NEVER BE THROWN
                    throw new CalcError(String.format("Unknown Operation '%s'", token));
            }
        } catch (EmptyStackException e) {

            throw new CalcError("Not enough numbers for operation!");
        }
    }

    private Number tryParseNumber(String token) {
        if(token == null) return null;

        //int
        try {
            int value = Integer.parseInt(token);
            return Number.fromInt(value);
        } catch (NumberFormatException e) {
            //Do nothing
        }

        //float
        try {
            float value = Float.parseFloat(token);
            return Number.fromFloat(value);
        } catch (NumberFormatException e) {
            //Do nothing
        }

        return null;
    }

    private void subr(String [] args, int idx, int depth) throws CalcError {
        try {
            while (idx < args.length) {
                //out.print(stack.toString());
                String token = args[idx];
                if (isOperation(token)) {
                    doOperation(token);
                } else if (isNumber(token)){
                    Number num = tryParseNumber(token);
                    if(num == null) throw new CalcError(String.format("Unrecognized number '%s'", token));
                    stack.push(num);
                } else if (macros.has(token)) {
                    if(depth == DEPTH_MAX) throw new CalcError("Too much recursion!");
                    this.subr(macros.get(token), 1, depth+1);
                } else {
                    throw new CalcError(String.format("Unrecognized token '%s'", token));
                }
                idx++;
            }
        } catch (CalcError e) {
            throw new CalcError(String.format("%s (at pos %d)", e.getMessage(), idx));
        }
    }

    public boolean run(String[] args) {
        //out.print(String.valueOf(args.length));
        try {
            this.subr(args, 0, 0);
        } catch (CalcError e) {
            out.error(e.getMessage());
            return false;
        }
        return true;
    }
}
