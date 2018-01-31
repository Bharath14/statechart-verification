import java.util.List;
import java.util.ArrayList;

import ast.Statechart;
import ast.State;
import ast.Transition;
import ast.Declaration;
import ast.Expression;
import ast.Type;
import ast.Environment;
import ast.Name;
import ast.Statement;
import ast.IfStatement;
import ast.WhileStatement;
import ast.AssignmentStatement;
import ast.StatementList;
import ast.BooleanConstant;
import ast.IntegerConstant;
import ast.BinaryExpression;
import ast.BasicType;
import ast.Struct;

public class Typechecker {

  private final Statechart statechart;

  public Typechecker(Statechart statechart) {
    this.statechart = statechart;
  }

  public void typecheckDeclarations(State state) throws Exception {

    for(Declaration dec : state.declarations) {
      Type type = this.statechart.lookupType(dec.typeName);
      if(type == null) {
        throw new Exception(
          "Declaration '" + dec.toString() + "' in state '" + state.name +
          "' didn't typecheck : declaration type '" +
          dec.typeName + "' doesn't exist.");
      }
      else {
        dec.setType(type);
      }
    }
    for(State st : state.states) {
      typecheckDeclarations(st);
    }
  }

  public void typecheck() throws Exception {

    this.typecheckTypeDeclarations();
    this.typecheckDeclarations();
    this.typecheckCode();
  }

  private void typecheckTypeDeclarations() throws Exception {
    for(int i = 0; i < this.statechart.types.size(); i++) {
      Type td = this.statechart.types.get(i);
      if(td instanceof Struct) {
        this.typecheckStruct((Struct)td, i);
      }
    }
  }

  private Type getKnownType(String tname, int i) {
    for(int j = 0; j < i; j++) {
      Type type = this.statechart.types.get(j);
      if(type.name.equals(tname)) {
        return type;
      }
    }
    return null;
  }

  private void typecheckStruct(Struct struct, int i) throws Exception {
    for(Declaration d : struct.declarations) {
      Type type = this.getKnownType(d.typeName, i);
      if(type == null) {
        throw new Exception("Struct typecheck error : type '" + d.typeName + "' unknown in " + struct.name);
      }
      else {
        d.setType(type);
      }
    }
  }
 
  private void typecheckDeclarations() throws Exception {
    this.typecheckDeclarations(this.statechart);
  }

  /*
      Sets the type of the name object to appropriate type if the following work out:
       - If name is a identifier, it will be looked up in the local environment env. If
         a declaration is found, the same will be used to define the type of name.
       - If name is a fully qualified name (e.g. a.b.c), its state is found (by name a.b),
         If this state state is an ancestor of the state st of env, then we look up for name
         in the declaration list of state. If found, the same will be used to define the type
         of name.
  */
  private void typecheckName(Name name, Environment env) throws Exception {
    Declaration dec = null;
    if(name.name.size() > 1) {
      String lastName = name.name.get(name.name.size() - 1);
      List<String> stateName = new ArrayList<String>(name.name.subList(0, name.name.size() - 1));
      State state = this.statechart.nameToState(new Name(stateName));
      if(state != null) {
        State st = env.getDeclarations().getState();
        if(st.equals(state) || state.isAncestor(st)) {
          dec = state.declarations.lookup(lastName);
        }
        else {
          throw new Exception("Undefined variable " + name.toString() + "; contents of state " + stateName + " are not visible in this context.");
        }
      }
      else {
        throw new Exception("Undefined variable " + name.toString() + "; no state by name " + stateName + " found.");
      }
    }
    else if(name.name.size() == 1) {
      dec = env.lookup(name.name.get(0));
    }
    else {
      throw new Exception("Empty variable name.");
    }

    if(dec == null) {
      throw new Exception("Undefined variable " + name.toString() + " looked in : " + env);
    }
    else {
      name.setDeclaration(dec);
      name.setType(dec.getType());
    }
  }

  private void typecheckBooleanConstant(BooleanConstant b) {
    b.setType(this.statechart.lookupType("boolean"));
  }

  private void typecheckIntegerConstant(IntegerConstant b) {
    b.setType(this.statechart.lookupType("int"));
  }

  private void typecheckBinaryExpression(BinaryExpression b, Environment env) throws Exception {
    typecheckExpression(b.left, env);
    typecheckExpression(b.right, env);
    if(
        b.operator.equals("+") ||
        b.operator.equals("*") ||
        b.operator.equals("-") ||
        b.operator.equals("/") ) {

      Type type = this.statechart.lookupType("int");
      if(b.left.getType().equals(type) &&
        b.right.getType().equals(type)) {
        b.setType(type);
      }
      else {
        throw new Exception("typecheckBinaryExpression failed: operant type mismatch between " + b.left + " and " + b.right);
      }
   }
    else if(
        b.operator.equals(">=") ||
        b.operator.equals(">") ||
        b.operator.equals("<=") ||
        b.operator.equals("<") ||
        b.operator.equals("!=") ||
        b.operator.equals("=") ) {

      Type type = this.statechart.lookupType("int");
      if(b.left.getType().equals(type) &&
        b.right.getType().equals(type)) {
        b.setType(this.statechart.lookupType("boolean"));
      }
      else {
        throw new Exception("typecheckBinaryExpression failed: operant type mismatch between " + b.left + " and " + b.right);
      }
    }
    else if(
        b.operator.equals("&&") ||
        b.operator.equals("||") ) {

      Type type = this.statechart.lookupType("boolean");
      if(b.left.getType().equals(type) &&
        b.right.getType().equals(type)) {
        b.setType(type);
      }
      else {
        throw new Exception("typecheckBinaryExpression failed: operant type mismatch between " + b.left + " and " + b.right);
      }
    }
  }

  private void typecheckExpression(Expression exp, Environment env) throws Exception {
    if(exp instanceof Name) {
      typecheckName((Name)exp, env); 
    }
    else if(exp instanceof BooleanConstant) {
      typecheckBooleanConstant((BooleanConstant)exp); 
    }
    else if(exp instanceof IntegerConstant) {
      typecheckIntegerConstant((IntegerConstant)exp); 
    }
    else if(exp instanceof BinaryExpression) {
      typecheckBinaryExpression((BinaryExpression)exp, env); 
    }
    else {
      throw new Exception("Typechecking failed for expression: " + exp.toString() +
        " of type " + exp.getClass().getName() + ".");
    }
  }

  private void typecheckGuard(Expression guard, Environment env) throws Exception {
    this.typecheckExpression(guard, env);
    if(guard.getType().name != "boolean") {
      throw new Exception("Typechecking failed for guard : " + guard.toString() +
        " of type " + guard.getType().name + ". Should be boolean.");
    }
  }

  private void typecheckStatementList(
    StatementList sl,
    Environment renv,
    Environment wenv,
    Environment rwenv,
    Environment roenv,
    Environment woenv) throws Exception {
      List<Statement> statements = sl.getStatements();
      for(Statement st : statements) {
        this.typecheckStatement(st, renv, wenv, rwenv, roenv, woenv);
      }
  }

  private void typecheckAssignmentStatement (
      Statement s,
      Environment renv,
      Environment wenv,
      Environment rwenv,
      Environment roenv,
      Environment woenv) throws Exception {
      AssignmentStatement as = (AssignmentStatement)s;

    this.typecheckName(as.lhs, wenv);
    this.typecheckExpression(as.rhs, rwenv);

    if(!as.lhs.getType().equals(as.rhs.getType())) {
      throw new Exception("assignment lhs and rhs types don't match.");
    }
  }

  private void typecheckIfStatement (
      IfStatement is,
      Environment renv,
      Environment wenv,
      Environment rwenv,
      Environment roenv,
      Environment woenv) throws Exception {

    this.typecheckExpression(is.condition, renv);
    this.typecheckStatement(is.then_body, renv, wenv, rwenv, roenv, woenv);
    this.typecheckStatement(is.else_body, renv, wenv, rwenv, roenv, woenv);

    if(!is.condition.getType().name.equals("boolean")) {
      throw new Exception("Type checking failed in the if : condition '" + is.condition + "' didn't type check to a boolean.");
    }
  }

  private void typecheckWhileStatement (
      WhileStatement ws,
      Environment renv,
      Environment wenv,
      Environment rwenv,
      Environment roenv,
      Environment woenv) throws Exception {

    this.typecheckExpression(ws.condition, renv);
    this.typecheckStatement(ws.body, renv, wenv, rwenv, roenv, woenv);

    if(!ws.condition.getType().name.equals("boolean")) {
      throw new Exception("Type checking failed in the while loop : condition '" + ws.condition + "' didn't type check to a boolean.");
    }
  }

  private void typecheckStatement(
    Statement s,
    Environment renv,
    Environment wenv,
    Environment rwenv,
    Environment roenv,
    Environment woenv) throws Exception {
    if(s instanceof AssignmentStatement) {
      this.typecheckAssignmentStatement(s, renv, wenv, rwenv, roenv, woenv);
    }
    else if(s instanceof StatementList) {
      this.typecheckStatementList((StatementList)s, renv, wenv, rwenv, roenv, woenv);
    }
    else if(s instanceof IfStatement) {
      this.typecheckIfStatement((IfStatement)s, renv, wenv, rwenv, roenv, woenv);
    }
    else if(s instanceof WhileStatement) {
      this.typecheckWhileStatement((WhileStatement)s, renv, wenv, rwenv, roenv, woenv);
    }
  }

  private void typecheckTransition(Transition transition) throws Exception {
    State lub = this.statechart.lub(transition.getSource(), transition.getDestination());
    if(lub == null) {
      throw new Exception("Typechecking failed for transition : " + transition.name +
        ". Null LUB ");
    }
    if(transition.getState() != lub) {
      throw new Exception("Typechecking failed for transition : " + transition.name +
        ". Should be placed in state " + lub.name + " but placed in state " +
        transition.getState().name + ".");
    }
    this.typecheckGuard(transition.guard, transition.getRWEnvironment());
    this.typecheckStatement(transition.action,
      transition.getReadEnvironment(),
      transition.getWriteEnvironment(),
      transition.getRWEnvironment(),
      transition.getReadOnlyEnvironment(),
      transition.getWriteOnlyEnvironment());
  }

  private void typecheckCode(State state) throws Exception {
    for(Transition t : state.transitions) {
      this.typecheckTransition(t);
    }

    for(State s : state.states) {
      this.typecheckCode(s);
    }
  }

  private void typecheckCode() throws Exception {
    this.typecheckCode(this.statechart);
  }
}
