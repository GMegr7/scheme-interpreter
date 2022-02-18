package gmegr20.schemeinterpreter;

import java.io.File;
import java.io.FileNotFoundException;
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
                System.out.print(currValue + " ");
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
        list = list.substring(1, list.length() - 1);
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
            case "append" -> {
                return evaluateProcedure_append(listTokenizer, currentNamespace);
            }
            case "if" -> {
                return evaluateMacro_if(listTokenizer, currentNamespace);
            }
            case "define" -> {
                String identifier = listTokenizer.next().trim();
                if(identifier.charAt(0) == '(') {
                    StringTokenizer st = new StringTokenizer(identifier.substring(1, identifier.length() - 1));
                    String name = st.nextToken();
                    ArrayList<String> parameters = new ArrayList<>();
                    while(st.hasMoreTokens()) {
                        parameters.add(st.nextToken());
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
                NamespaceItem funcItem = currentNamespace.get(func);
                if(funcItem == null) {
                    System.err.println("Error while evaluating: " + func + " could not be identified.");
                    return "";
                }
                if(funcItem.isFunction()) {
                    Namespace newNamespace = new Namespace(currentNamespace);
                    for(int i = 0; i < funcItem.getParameterCount(); ++i) {
                        newNamespace.addLiteral(funcItem.getParameter(i), evaluate(listTokenizer.next(), currentNamespace));
                    }
                    return evaluate(funcItem.getValue(), newNamespace);
                } else {
                    return funcItem.getValue();
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
    private String evaluateProcedure_append(MyTokenizer currentList, Namespace currentNamespace) {
        String list1 = currentList.next().trim();
        String list2 = currentList.next().trim();
        return  list1.substring(0, list1.length() - 1).trim() + " " +
                list2.substring(2).trim();
    }

    // Flow control
    private String evaluateMacro_if(MyTokenizer currentList, Namespace currentNamespace) {
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

//    public void evaluateFile(File f) throws FileNotFoundException {
//        Scanner fileScanner = new Scanner(f);
//        System.out.println("File read successfully!\n");
//        StringBuilder soFar = new StringBuilder();
//        int balance = 0;
//        while(fileScanner.hasNext()) {
//            String token = fileScanner.next();
//            for (int i = 0; i < token.length(); i++) {
//                char curr = token.charAt(i);
//                switch (curr) {
//                    case '(' -> {
//                        if (balance == 0 && !soFar.toString().isBlank()) {
//                            evaluate(soFar.toString());
//                            soFar = new StringBuilder();
//                        }
//                        soFar.append(" ( ");
//                        balance++;
//                    }
//                    case ')' -> {
//                        soFar.append(" ) ");
//                        balance--;
//                        if (balance == 0 && !soFar.toString().isBlank()) {
//                            evaluate(soFar.toString());
//                            soFar = new StringBuilder();
//                        }
//                    }
//                    default -> {
//                        soFar.append(curr);
//                    }
//                }
//            }
//            if(balance == 0 && !soFar.toString().isBlank()) {
//                evaluate(soFar.toString());
//                soFar = new StringBuilder();
//            } else {
//                soFar.append(' ');
//            }
//        }
//    }
}