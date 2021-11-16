package testcases;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.junit.*;

import java_cup.runtime.Symbol;
import java_cup.runtime.Scanner;

import frontend.FrontEnd;
import frontend.Parser;
import frontend.Typechecker;
import simulator.Simulator;

import ast.State;
import ast.Statechart;
import ast.Transition;
import ast.Environment;
import ast.Declaration;
import ast.Name;
import ast.Type;
import symbolic_execution.se_tree.*;
import symbolic_execution.SymbolicExecutionEngine;
import symbolic_execution.SymbolicExecutionResult;

public class SymTest{
  @Test
  public void symtest()
  {
    System.out.println("Symbolic Execution");
    Statechart statechart = null;
    //Typechecker typechecker;
    String input = "data/testcase2.stb";

    try {
      Parser parser = new FrontEnd(input).getParser();    
      Symbol result = parser.parse();
      statechart = (Statechart)result.value;
      //System.out.println("Printing parsed Statechart ...");
      //System.out.println(statechart.toString());
      //System.out.println("Printing parsed Statechart ... done!");
    }
    catch(FileNotFoundException e) {
      System.out.println("Couldn't open file '" + input + "'"); 
    }
    catch(Exception e) {
      System.out.println("Couldn't parse '" + input + "' : " + e.getMessage()); 
      e.printStackTrace();
      System.exit(1);
    }

    try {
      new Typechecker(statechart).typecheck();
      //System.out.println(input + ": Printing typechecked Statechart ...");
      //System.out.println(statechart.toString());
      //System.out.println(input + ": Printing typechecked Statechart ... done!");
    }
    catch(Exception e) {
      System.out.println("Couldn't typecheck '" + input + "' : " + e.getMessage()); 
      e.printStackTrace();
    }

    System.out.println("Statechart completed");

    /*//SETNode
    SETNode n1 = new SETNode(null);
    SETNode n2 = new SETNode(n1);
    SETNode n3 = new SETNode(n2);
    SETNode n4 = new SETNode(n3);
    System.out.println("Depth is");
    System.out.println(n4.depth);

    //SymbolicExecutionResult

    List<SETNode> live1 = new ArrayList<SETNode>();
    live1.add(n1);
    live1.add(n2);
    List<SETNode> done1 = new ArrayList<SETNode>();
    done1.add(n3);
    done1.add(n4);

    SymbolicExecutionResult res = new SymbolicExecutionResult();
    res.setLiveNodes(live1);
    res.setDoneNodes(done1);

    List<SETNode> live = new ArrayList<SETNode>();
    live.addAll(res.getLiveNodes());
    List<SETNode> done = new ArrayList<SETNode>();
    done.addAll(res.getDoneNodes());

    System.out.println(live.get(0).depth);
    System.out.println(live.get(1).depth);
    System.out.println(done.get(0).depth);
    System.out.println(done.get(1).depth);
    */
    SymbolicExecutionEngine symbolictest = new SymbolicExecutionEngine(statechart, 8);
    SymbolicExecutionResult res = new SymbolicExecutionResult();
    //symbolictest.execute();
    res.setDoneNodes(symbolictest.execute());

    System.out.println(res.getDoneNodes());

  }

}