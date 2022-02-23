package gmegr20.schemeinterpreter;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.StringTokenizer;

import static gmegr20.schemeinterpreter.Constants.*;

public class Interpreter {
    private final Namespace globalNamespace;
    public Interpreter() {
        globalNamespace = new Namespace();
    }

    public void evaluateFile(File f) throws FileNotFoundException {
        MyTokenizer mt = new MyTokenizer(f);
        String curr = mt.next();
        while(curr != null) {
            String currValue = evaluate(curr);
            if(currValue.length() > 0) {
                if(currValue.charAt(0) == '\'') {
                    System.out.print(currValue.substring(1) + " ");
                } else {
                    System.out.print(currValue + " ");
                }
            }
            curr = mt.next();
        }
        System.out.print("\n");
    }

    private String evaluate(String s) {
        return evaluate(s, globalNamespace);
    }

    private String evaluate(String s, Namespace outerNamespace) {
        if(DEBUG_MODE) {System.out.println("Evaluating: " + s);}
        MyTokenizer mt = new MyTokenizer(s);
        String curr = mt.next();
        StringBuilder aggregate = new StringBuilder();
        while(curr != null) {
            curr = curr.trim();

            if (curr.length() != 0) {
                if (curr.charAt(0) == '(') {
                    String newValue = evaluateList(curr, outerNamespace);
                    if (newValue.length() != 0) {
                        aggregate.append(" ").append(newValue);
                    }
                } else if (isNumber(curr) || isBoolean(curr) || curr.charAt(0) == '\'') {
                    aggregate.append(" ").append(curr);
                } else {
                    NamespaceItem currentItem = outerNamespace.get(curr);
                    if(currentItem == null) {
                        System.err.println("Error while evaluating: " + curr + " could not be identified.");
                        return "";
                    }
                    aggregate.append(" ").append(currentItem.getValue());
                }
            }
            curr = mt.next();
        }

        return aggregate.toString().trim();
    }

    private String evaluateList(String list, Namespace outerNamespace) {
        Namespace currentNamespace = new Namespace(outerNamespace);
        list = list.trim();
        list = list.trim().substring(1, list.length() - 1);
        MyTokenizer listTokenizer = new MyTokenizer(list);
        String func = listTokenizer.next().trim();
        if(DEBUG_MODE) {System.out.println("Function: " + func + " Evaluating: " + list);}
        switch (func) {
            case "+" -> {
                return evaluateProcedureAdd(listTokenizer, currentNamespace);
            }
            case "-" -> {
                return evaluateProcedureSubtract(listTokenizer, currentNamespace);
            }
            case "*" -> {
                return evaluateProcedureMultiply(listTokenizer, currentNamespace);
            }
            case "/" -> {
                return evaluateProcedureDivide(listTokenizer, currentNamespace);
            }
            case "=" -> {
                return evaluateProcedureEquals(listTokenizer, currentNamespace);
            }
            case ">" -> {
                return evaluateProcedureMoreThan(listTokenizer, currentNamespace);
            }
            case "<" -> {
                return evaluateProcedureLessThan(listTokenizer, currentNamespace);
            }
            case "car" -> {
                return evaluateProcedure_car(listTokenizer, currentNamespace);
            }
            case "cdr" -> {
                return evaluateProcedure_cdr(listTokenizer, currentNamespace);
            }
            case "cons" -> {
                return evaluateProcedure_cons(listTokenizer, currentNamespace);
            }
            case "map" -> {
                return evaluateProcedureMap(listTokenizer, currentNamespace);
            }
            case "append" -> {
                return evaluateProcedure_append(listTokenizer, currentNamespace);
            }
            case "and" -> {
                return evaluateProcedureAnd(listTokenizer, currentNamespace);
            }
            case "or" -> {
                return evaluateProcedureOr(listTokenizer, currentNamespace);
            }
            case "if" -> {
                return evaluateMacroIf(listTokenizer, currentNamespace);
            }
            case "define" -> {
                String identifier = listTokenizer.next().trim();
                if(identifier.charAt(0) == '(') {
                    StringTokenizer parameterTokenizer = new StringTokenizer(identifier.substring(1, identifier.length() - 1));
                    String name = parameterTokenizer.nextToken();
                    ArrayList<String> parameters = new ArrayList<>();
                    while(parameterTokenizer.hasMoreTokens()) {
                        parameters.add(parameterTokenizer.nextToken());
                    }
                    StringBuilder code = new StringBuilder();
                    String curr = listTokenizer.next();
                    while(curr != null) {
                        code.append(curr);
                        curr = listTokenizer.next();
                    }
                    outerNamespace.addLambda(name, parameters, code.toString());
                    currentNamespace.addLambda(name, parameters, code.toString());
                } else {
                    String literalValue = evaluate(listTokenizer.next(), currentNamespace);
                    outerNamespace.addLiteral(identifier, literalValue);
                    currentNamespace.addLiteral(identifier, literalValue);
                }
            }
            default -> {
                if(!currentNamespace.contains(func) && func.charAt(0) != '(') {
                    System.err.println("Error while evaluating: " + func + " could not be identified.");
                    return "";
                } else {
                    return evaluateFunctionList(func, listTokenizer, currentNamespace);
                }
            }
        }
        return "";
    }

    // Procedures/Macros

    // Arithmetic operations
    private String evaluateProcedureAdd(MyTokenizer currentList, Namespace currentNamespace) {
        int ans = 0;
        String next = currentList.next();
        while(next != null) {
            ans += Integer.parseInt(evaluate(next, currentNamespace));
            next = currentList.next();
        }
        return Integer.toString(ans);
    }
    private String evaluateProcedureSubtract(MyTokenizer currentList, Namespace currentNamespace) {
        String next = currentList.next();
        int ans = Integer.parseInt(evaluate(next, currentNamespace));
        next = currentList.next();
        while(next != null) {
            ans -= Integer.parseInt(evaluate(next, currentNamespace));
            next = currentList.next();
        }
        return Integer.toString(ans);
    }
    private String evaluateProcedureMultiply(MyTokenizer currentList, Namespace currentNamespace) {
        int ans = 1;
        String next = currentList.next();
        while(next != null) {
            ans *= Integer.parseInt(evaluate(next, currentNamespace));
            next = currentList.next();
        }
        return Integer.toString(ans);
    }
    private String evaluateProcedureDivide(MyTokenizer currentList, Namespace currentNamespace) {
        String next = currentList.next();
        int ans = Integer.parseInt(evaluate(next, currentNamespace));
        next = currentList.next();
        while(next != null) {
            ans /= Integer.parseInt(evaluate(next, currentNamespace));
            next = currentList.next();
        }
        return Integer.toString(ans);
    }

    // Boolean operations
    private String evaluateProcedureAnd(MyTokenizer currentList, Namespace currentNamespace) {
        String curr = currentList.next();
        while(curr != null) {
            if(!evaluate(curr, currentNamespace).equals(TRUE_BOOLEAN)) {
                return FALSE_BOOLEAN;
            }
            curr = currentList.next();
        }
        return TRUE_BOOLEAN;
    }
    private String evaluateProcedureOr(MyTokenizer currentList, Namespace currentNamespace) {
        String curr = currentList.next();
        while (curr != null) {
            if(evaluate(curr, currentNamespace).equals(TRUE_BOOLEAN)) {
                return TRUE_BOOLEAN;
            }
            curr = currentList.next();
        }
        return FALSE_BOOLEAN;
    }


    // Compare operations
    private String evaluateProcedureEquals(MyTokenizer currentList, Namespace currentNamespace) {
        String toEqual = evaluate(currentList.next(), currentNamespace);
        String value2 = currentList.next();
        while(value2 != null) {
            if(!evaluate(value2, currentNamespace).equals(toEqual)) {
                return FALSE_BOOLEAN;
            }
            value2 = currentList.next();
        }
        return TRUE_BOOLEAN;
    }
    private String evaluateProcedureMoreThan(MyTokenizer currentList, Namespace currentNamespace) {
        String value1 = evaluate(currentList.next(), currentNamespace);
        String value2 = evaluate(currentList.next(), currentNamespace);
        if(isNumber(value1) && isNumber(value2)) {
            return (Integer.parseInt(value1) > Integer.parseInt(value2)) ? TRUE_BOOLEAN : FALSE_BOOLEAN;
        } else {
            System.err.println("Invalid values for numeric comparison");
            return "";
        }
    }
    private String evaluateProcedureLessThan(MyTokenizer currentList, Namespace currentNamespace) {
        String value1 = evaluate(currentList.next(), currentNamespace);
        String value2 = evaluate(currentList.next(), currentNamespace);
        if(isNumber(value1) && isNumber(value2)) {
            return (Integer.parseInt(value1) < Integer.parseInt(value2)) ? TRUE_BOOLEAN : FALSE_BOOLEAN;
        } else {
            System.err.println("Invalid values for numeric comparison");
            return "";
        }
    }

    // List operations
    private String evaluateProcedure_car(MyTokenizer currentList, Namespace currentNamespace) {
        String currList = evaluate(currentList.next(), currentNamespace);
        MyTokenizer tempTok = new MyTokenizer(currList.substring(2));
        return tempTok.next();
    }
    private String evaluateProcedure_cdr(MyTokenizer currentList, Namespace currentNamespace) {
        String currList = evaluate(currentList.next(), currentNamespace);
        MyTokenizer tempTok = new MyTokenizer(currList.substring(2));
        String firstElem = tempTok.next();
        return "'(" + currList.substring(currList.indexOf(firstElem) + firstElem.length()).trim();
    }
    private String evaluateProcedure_cons(MyTokenizer currentList, Namespace currentNamespace) {
        String arg1 = evaluate(currentList.next(), currentNamespace);
        String arg2 = evaluate(currentList.next(), currentNamespace);
        if(!isList(arg2)) {
            System.err.println("Argument 2 to \"cons\" is not a list.");
            return "";
        }
        return "'(" + arg1 + " " + arg2.substring(2);
    }
    private String evaluateProcedureMap(MyTokenizer currentList, Namespace currentNamespace) {
        StringBuilder result = new StringBuilder();
        String function = currentList.next();
        String arguments = currentList.next().trim();
        arguments = arguments.substring(2, arguments.length() - 1);
        MyTokenizer argumentTokenizer = new MyTokenizer(arguments);
        String curr = argumentTokenizer.next();
        while(curr != null) {
            ArrayList<String> argumentList = new ArrayList<>();
            argumentList.add(curr);
            result.append(evaluateFunctionCode(getLambda(function, currentNamespace), argumentList, currentNamespace)).append(' ');
            curr = argumentTokenizer.next();
        }
        return "'(" + result.toString().trim() + ')';
    }
    private String evaluateProcedure_append(MyTokenizer currentList, Namespace currentNamespace) {
        String list1 = currentList.next().trim();
        String list2 = currentList.next().trim();
        return  list1.substring(0, list1.length() - 1).trim() + " " +
                list2.substring(2).trim();
    }

    // Flow control
    private String evaluateMacroIf(MyTokenizer currentList, Namespace currentNamespace) {
        String condition = evaluate(currentList.next(), currentNamespace);
        if(condition.equals(TRUE_BOOLEAN)) {
            return evaluate(currentList.next());
        } else if(condition.equals(FALSE_BOOLEAN)) {
            StringBuilder code = new StringBuilder();
            currentList.next();
            String curr = currentList.next();
            while(curr != null) {
                code.append(curr);
                curr = currentList.next();
            }
            return evaluate(code.toString(), currentNamespace);
        } else {
            System.err.println("Wrong type of expression in 'if' condition.");
            return "";
        }
    }

    private String evaluateFunctionList(String function, MyTokenizer currentList, Namespace currentNamespace) {
        NamespaceItem currentLambda = getLambda(function, currentNamespace);
        ArrayList<String> argumentList = new ArrayList<>();
        for (int i = 0; i < currentLambda.getParameterCount(); i++) {
            argumentList.add(currentList.next());
        }
        return evaluateFunctionCode(currentLambda, argumentList, currentNamespace);
    }

    private String evaluateFunctionCode(NamespaceItem currentLambda, ArrayList<String> argumentList, Namespace outerNamespace) {
        Namespace newNamespace = new Namespace(outerNamespace);
        for (int i = 0; i < currentLambda.getParameterCount(); i++) {
            newNamespace.addLiteral(currentLambda.getParameter(i), evaluate(argumentList.get(i), outerNamespace));
        }
        return evaluate(currentLambda.getValue(), newNamespace);
    }

    // Predicates
    private boolean isList(String s) {
        s = s.trim();
        return s.charAt(0) == '\'' && s.charAt(1) == '(' && s.charAt(s.length() - 1) == ')';
    }
    private boolean isBoolean(String s) {
        return s.trim().equals(TRUE_BOOLEAN) || s.trim().equals(FALSE_BOOLEAN);
    }
    private boolean isNumber(String s) {
        int startIndex = 0;
        if(s.charAt(0) == '-') {
            if(s.length() == 1)
                return false;
            startIndex++;
        }

        for(int i = startIndex; i < s.length(); ++i) {
            if(s.charAt(i) < '0' || s.charAt(i) > '9')
                return false;
        }
        return true;
    }

    private NamespaceItem getLambda(String function, Namespace currentNamespace) {
        if(currentNamespace.contains(function)) {
            return currentNamespace.get(function);
        } else {
            StringBuilder code;
            ArrayList<String> parameterList = new ArrayList<>();
            MyTokenizer lambdaTokenizer = new MyTokenizer(function.substring(1, function.length() - 1));
            if(!lambdaTokenizer.next().equals("lambda")) {
                System.err.println("Wrong command to evaluate.");
                return null;
            }
            String parameters = lambdaTokenizer.next();
            StringTokenizer parameterTokenizer = new StringTokenizer(parameters.trim().substring(1, parameters.length() - 1));
            while(parameterTokenizer.hasMoreTokens()) {
                parameterList.add(parameterTokenizer.nextToken());
            }
            String curr = lambdaTokenizer.next();
            code = new StringBuilder();
            while(curr != null) {
                code.append(curr);
                curr = lambdaTokenizer.next();
            }
            return new NamespaceItem(parameterList, code.toString());
        }
    }
}