package gmegr20.schemeinterpreter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class MyTokenizer {
    private enum Mode{STR, FILE}

    private final Mode currentMode;

    private int balance = 0;
    private final StringBuilder soFar = new StringBuilder();
    private final StringTokenizer stringScanner;
    private final Scanner fileScanner;

    public MyTokenizer(String in) {
        currentMode = Mode.STR;
        stringScanner = new StringTokenizer(in);
        balance = 0;
        fileScanner = null;
    }

    public MyTokenizer(File f) throws FileNotFoundException {
        currentMode = Mode.FILE;
        fileScanner = new Scanner(f);
        stringScanner = null;
    }

    public String next() {
        return switch (currentMode) {
            case STR -> nextTokenFromString();
            case FILE -> nextTokenFromFile();
        };
    }

    private String nextTokenFromString() {
        int i = 0;
        while(true) {
            while (i < soFar.length()) {
                switch (soFar.charAt(i)) {
                    case '\'' -> {
                        if(balance == 0) {
                            soFar.delete(0, i + 1);
                            return "'" + nextTokenFromString();
                        }
                        i++;
                    }
                    case ' ' -> {
                        if (balance == 0 && !soFar.substring(0, i).isBlank()) {
                            String ret = soFar.substring(0, i);
                            soFar.delete(0, i + 1);
                            i = 0;
                            if(!ret.isBlank()) {
                                return ret.trim();
                            }
                        } else {
                            i++;
                        }
                    }
                    case '(' -> {
                        if(balance == 0 && !soFar.substring(0, i).isBlank()) {
                            String ret = soFar.substring(0, i);
                            soFar.delete(0,i);
                            i = 0;
                            if(!ret.isBlank()) {
                                return ret.trim();
                            }
                        } else {
                            balance++;
                            i++;
                        }
                    }
                    case ')' -> {
                        balance--;
                        if(balance == 0) {
                            String ret = soFar.substring(0, i + 1);
                            soFar.delete(0, i + 1);
                            i = 0;
                            return ret.trim();
                        } else {
                            i++;
                        }
                    }
                    default -> {
                        i++;
                    }
                }
            }
            if(stringScanner.hasMoreTokens()) {
//                soFar.append(' ');
                soFar.append(stringScanner.nextToken());
                soFar.append(' ');
            } else {
                return null;
            }
        }
    }

    private String nextTokenFromFile() {
        int i = 0;
        while(true) {
            while (i < soFar.length()) {
                switch (soFar.charAt(i)) {
                    case '\'' -> {
                        if(balance == 0) {
                            soFar.delete(0, i + 1);
                            return "'" + nextTokenFromFile();
                        }
                        i++;
                    }
                    case ' ' -> {
                        if (balance == 0 && !soFar.substring(0, i).isBlank()) {
                            String ret = soFar.substring(0, i);
                            soFar.delete(0, i + 1);
                            i = 0;
                            if(!ret.isBlank()) {
                                return ret.trim();
                            }
                        } else {
                            i++;
                        }
                    }
                    case '(' -> {
                        if(balance == 0 && !soFar.substring(0, i).isBlank()) {
                            String ret = soFar.substring(0, i);
                            soFar.delete(0,i);
                            i = 0;
                            if(!ret.isBlank()) {
                                return ret.trim();
                            }
                        } else {
                            balance++;
                            i++;
                        }
                    }
                    case ')' -> {
                        balance--;
                        if(balance == 0) {
                            String ret = soFar.substring(0, i + 1);
                            soFar.delete(0, i + 1);
                            i = 0;
                            return ret.trim();
                        } else {
                            i++;
                        }
                    }
                    default -> {
                        i++;
                    }
                }
            }
            if(fileScanner.hasNext()) {
//                soFar.append(' ');
                soFar.append(fileScanner.next());
                soFar.append(' ');
            } else {
                return null;
            }
        }
    }
}
