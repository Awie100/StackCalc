package me.awie1000.stackcalc;

import java.util.Collection;
import java.util.HashMap;

public class Macros {
    private HashMap<String, String[]> macros;

    public Macros() {
        macros = new HashMap<>();
    }

    public boolean has(String key) {
        return macros.containsKey(key);
    }

    public boolean register(String[] tokens) throws MacroError {
        if(tokens.length < 2) throw new MacroError("Cannot add macro: too short.");
        if(Calculator.isOperation(tokens[0])) throw new MacroError(String.format("Cannot add macro: '%s' is a protected word.", tokens[0]));
        if(Calculator.isNumber(tokens[0])) throw new MacroError(String.format("Cannot add macro: '%s' is a number.", tokens[0]));
        boolean overwrite = this.has(tokens[0]);
        macros.put(tokens[0], tokens);
        return overwrite;
    }

    public String[] get(String key) {
        return macros.get(key);
    }

    public boolean delete(String key) {
        return macros.remove(key) != null;
    }

    public Collection<String> keys() {return macros.keySet(); }

    public Collection<String[]> dump() {
        return macros.values();
    }
}
