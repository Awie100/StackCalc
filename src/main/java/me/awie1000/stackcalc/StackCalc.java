package me.awie1000.stackcalc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class StackCalc extends JavaPlugin {

    final String MACRO_FILE = "./plugins/calc.macros.txt";
    Macros macros;

    @Override
    public void onEnable() {
        // Plugin startup logic

        getLogger().info("[CALC] Loading Macros...");
        //load macros
        macros = new Macros();
        try {
            BufferedReader macroReader = new BufferedReader(new FileReader(MACRO_FILE));
            String line;
            while ((line = macroReader.readLine()) != null && !line.equals("")) {
                macros.register(line.split("\\s+"));
            }
            macroReader.close();
        } catch (IOException e) {
            getLogger().info("[CALC] No macro file. Starting without macros.");
        } catch (MacroError e) {
            getLogger().info(e.getMessage());
            macros = new Macros();
        }

        //command registration
        this.getCommand("calc").setExecutor(new CommandCalc(this));
        this.getCommand("macro").setExecutor(new CommandMacro(this));
        getLogger().info("[CALC] Calculator Set Up!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        getLogger().info("[CALC] Saving Macros...");
        //save macros
        if(macros == null) return;
        try {
            BufferedWriter macroWriter = new BufferedWriter(new FileWriter(MACRO_FILE));
            for(String[] macro : macros.dump()) {
                macroWriter.write(String.join(" ", macro));
                macroWriter.newLine();
            }
            macroWriter.close();
        } catch (IOException e) {
            getLogger().info("[CALC] Error: cannot find/create macro file.");
        }
    }
}

class PlayerPrinter {
    Player player;
    public PlayerPrinter(Player player) {
        this.player = player;
    }

    public void print(String msg) {
        Component text = Component.text().content("[Calc]: " + msg).color(NamedTextColor.GREEN).build();
        player.sendMessage(text);
    }

    public void error(String err) {
        Component text = Component.text().content("[CalcErr]: " + err).color(NamedTextColor.RED).build();
        player.sendMessage(text);
    }
}

class CommandCalc implements TabExecutor {
    private StackCalc plugin;

    public CommandCalc(StackCalc plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        PlayerPrinter printer = new PlayerPrinter((Player) commandSender);
        Calculator calc =  new Calculator(printer, plugin.macros);
        boolean success = calc.run(args);
        return success;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        ArrayList<String> autoCompletes = new ArrayList<>();

        if (args.length < 1) return autoCompletes;

        String lastArg = args[args.length - 1];
        autoCompletes.add(lastArg);
        autoCompletes.addAll(Arrays.stream(Calculator.operationList).filter(op -> op.startsWith(lastArg)).collect(Collectors.toList()));
        autoCompletes.addAll(plugin.macros.keys().stream().filter(macro -> macro.startsWith(lastArg)).collect(Collectors.toList()));
        return autoCompletes;
    }
}

class CommandMacro implements TabExecutor {
    private StackCalc plugin;

    public CommandMacro(StackCalc plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        PlayerPrinter printer = new PlayerPrinter((Player) commandSender);
        if(args.length < 1) return false;
        try{
            switch (args[0]) {
                case "list":
                    for (String[] macro : plugin.macros.dump()) {
                        printer.print(String.join(" ", macro));
                    }
                    break;
                case "add":
                    plugin.macros.register(Arrays.copyOfRange(args, 1, args.length));
                    printer.print(String.format("Macro '%s' registered.", args[1]));
                    break;
                case "delete":
                    if(args.length < 2) throw new MacroError("Delete needs the name of a macro to delete");
                    boolean deleted = plugin.macros.delete(args[1]);
                    if(!deleted) throw new MacroError(String.format("'%s' is not a valid macro", args[1]));
                    printer.print(String.format("Macro '%s' was deleted", args[1]));
                    break;
                default:
                    throw new MacroError(String.format("Unknown control sequence '%s'", args[0]));
            }
        }catch (MacroError e) {
            printer.error(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        ArrayList<String> autoCompletes = new ArrayList<>();

        if(args.length < 1) return autoCompletes;

        if(args.length == 1) {
            autoCompletes.add("add");
            autoCompletes.add("list");
            autoCompletes.add("delete");
            return autoCompletes;
        }

        String lastArg = args[args.length - 1];
        switch (args[0]) {
            case "list":
                return autoCompletes;
            case "add":
                if(args.length == 2) return autoCompletes;
                autoCompletes.addAll(Arrays.stream(Calculator.operationList).filter(op -> op.startsWith(lastArg)).collect(Collectors.toList()));
                autoCompletes.addAll(plugin.macros.keys().stream().filter(macro -> macro.startsWith(lastArg)).collect(Collectors.toList()));
                return autoCompletes;
            case "delete":
                if(args.length > 2) return autoCompletes;
                autoCompletes.addAll(Arrays.stream(Calculator.operationList).filter(op -> op.startsWith(lastArg)).collect(Collectors.toList()));
                autoCompletes.addAll(plugin.macros.keys().stream().filter(macro -> macro.startsWith(lastArg)).collect(Collectors.toList()));
                return autoCompletes;
        }
        return autoCompletes;
    }
}