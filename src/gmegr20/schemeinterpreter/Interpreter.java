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

            //Arithmetic operations
            case "+" -> {
                return procedureAdd(listTokenizer, currentNamespace);
            }
            case "-" -> {
                return procedureSubtract(listTokenizer, currentNamespace);
            }
            case "*" -> {
                return procedureMultiply(listTokenizer, currentNamespace);
            }
            case "/" -> {
                return procedureDivide(listTokenizer, currentNamespace);
            }

            //Comparing operations
            case "=" -> {
                return procedureEquals(listTokenizer, currentNamespace);
            }
            case ">" -> {
                return procedureMoreThan(listTokenizer, currentNamespace);
            }
            case "<" -> {
                return procedureLessThan(listTokenizer, currentNamespace);
            }

            //List operations
            case "car" -> {
                String currList = evaluate(listTokenizer.next(), currentNamespace);
                MyTokenizer tempTok = new MyTokenizer(currList.substring(2));
                return tempTok.next();
            }
            case "cdr" -> {
                String currList = evaluate(listTokenizer.next(), currentNamespace);
                MyTokenizer tempTok = new MyTokenizer(currList.substring(2));
                String firstElem = tempTok.next();
                return "'(" + currList.substring(currList.indexOf(firstElem) + firstElem.length()).trim();
            }
            case "cons" -> {
                String arg1 = evaluate(listTokenizer.next(), currentNamespace);
                String arg2 = evaluate(listTokenizer.next(), currentNamespace);
                if(!isList(arg2)) {
                    System.err.println("Argument 2 to \"cons\" is not a list.");
                    return "";
                }
                return "'(" + arg1 + " " + arg2.substring(2);
            }
            case "append" -> {
                String list1 = listTokenizer.next().trim();
                String list2 = listTokenizer.next().trim();
                return  list1.substring(0, list1.length() - 1).trim() + " " +
                        list2.substring(2).trim();
            }

            //Flow Control
            case "if" -> {
                String condition = evaluate(listTokenizer.next(), currentNamespace);
                if(condition.equals(TRUE_BOOLEAN)) {
                    return evaluate(listTokenizer.next());
                } else if(condition.equals(FALSE_BOOLEAN)) {
                    StringBuilder code = new StringBuilder();
                    listTokenizer.next();
                    String curr = listTokenizer.next();
                    while(curr != null) {
                        code.append(curr);
                        curr = listTokenizer.next();
                    }
                    return evaluate(code.toString(), currentNamespace);
                } else {
                    System.err.println("Wrong type of expression in 'if' condition.");
                }
            }

            //Other
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

    //Procedures
    private String procedureAdd(MyTokenizer currentList, Namespace currentNamespace) {
        int ans = 0;
        String next = currentList.next();
        while(next != null) {
            ans += Integer.parseInt(evaluate(next, currentNamespace));
            next = currentList.next();
        }
        return Integer.toString(ans);
    }
    private String procedureSubtract(MyTokenizer currentList, Namespace currentNamespace) {
        String next = currentList.next();
        int ans = Integer.parseInt(evaluate(next, currentNamespace));
        next = currentList.next();
        while(next != null) {
            ans -= Integer.parseInt(evaluate(next, currentNamespace));
            next = currentList.next();
        }
        return Integer.toString(ans);
    }
    private String procedureMultiply(MyTokenizer currentList, Namespace currentNamespace) {
        int ans = 1;
        String next = currentList.next();
        while(next != null) {
            ans *= Integer.parseInt(evaluate(next, currentNamespace));
            next = currentList.next();
        }
        return Integer.toString(ans);
    }
    private String procedureDivide(MyTokenizer currentList, Namespace currentNamespace) {
        String next = currentList.next();
        int ans = Integer.parseInt(evaluate(next, currentNamespace));
        next = currentList.next();
        while(next != null) {
            ans /= Integer.parseInt(evaluate(next, currentNamespace));
            next = currentList.next();
        }
        return Integer.toString(ans);
    }
    private String procedureEquals(MyTokenizer currentList, Namespace currentNamespace) {
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
    private String procedureMoreThan(MyTokenizer currentList, Namespace currentNamespace) {
        String value1 = evaluate(currentList.next(), currentNamespace);
        String value2 = evaluate(currentList.next(), currentNamespace);
        if(isNumber(value1) && isNumber(value2)) {
            return (Integer.parseInt(value1) > Integer.parseInt(value2)) ? TRUE_BOOLEAN : FALSE_BOOLEAN;
        } else {
            System.err.println("Invalid values for numeric comparison");
            return "";
        }
    }
    private String procedureLessThan(MyTokenizer currentList, Namespace currentNamespace) {
        String value1 = evaluate(currentList.next(), currentNamespace);
        String value2 = evaluate(currentList.next(), currentNamespace);
        if(isNumber(value1) && isNumber(value2)) {
            return (Integer.parseInt(value1) < Integer.parseInt(value2)) ? TRUE_BOOLEAN : FALSE_BOOLEAN;
        } else {
            System.err.println("Invalid values for numeric comparison");
            return "";
        }
    }

    //Predicates
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