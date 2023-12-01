package octavo.cmsc124.lolcode_program.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LexicalAnalyzer {

    private Map<String, Token> keywordsAndOperatorsMap;

    public LexicalAnalyzer() {
        this.keywordsAndOperatorsMap = new HashMap<>();

//        keywordsAndOperatorsMap.put("for", Token.KEYWORD);
//        keywordsAndOperatorsMap.put("while", Token.KEYWORD);
//        keywordsAndOperatorsMap.put("do", Token.KEYWORD);
//        keywordsAndOperatorsMap.put("if", Token.KEYWORD);
//        keywordsAndOperatorsMap.put("else", Token.KEYWORD);
//        keywordsAndOperatorsMap.put("print", Token.KEYWORD);
//        keywordsAndOperatorsMap.put("switch", Token.KEYWORD);
//        keywordsAndOperatorsMap.put("case", Token.KEYWORD);
//        keywordsAndOperatorsMap.put("default", Token.KEYWORD);
//        keywordsAndOperatorsMap.put("null", Token.KEYWORD);
//        keywordsAndOperatorsMap.put("+", Token.PLUS);
//        keywordsAndOperatorsMap.put("-", Token.MINUS);
//        keywordsAndOperatorsMap.put("*", Token.TIMES);
//        keywordsAndOperatorsMap.put("/", Token.DIVIDE);
//        keywordsAndOperatorsMap.put("..", Token.DOTDOT);
//        keywordsAndOperatorsMap.put(".", Token.DOT);
//        keywordsAndOperatorsMap.put(",", Token.COMMA);
//        keywordsAndOperatorsMap.put("=", Token.EQUAL);
//        keywordsAndOperatorsMap.put(":", Token.COLON);
//        keywordsAndOperatorsMap.put(";", Token.SEMICOLON);
//        keywordsAndOperatorsMap.put("(", Token.LEFT_PARENTHESIS);
//        keywordsAndOperatorsMap.put(")", Token.RIGHT_PARENTHESIS);
//        keywordsAndOperatorsMap.put(">=", Token.GREATER_OR_EQUALS);
//        keywordsAndOperatorsMap.put("<=", Token.LOWER_OR_EQUALS);
//        keywordsAndOperatorsMap.put(">", Token.GREATER_THAN);
//        keywordsAndOperatorsMap.put("<", Token.LOWER_THAN);
//        keywordsAndOperatorsMap.put("<>", Token.NOT_EQUALS);
//        keywordsAndOperatorsMap.put(":=", Token.ASSIGNMENT_OPERATOR);
//        keywordsAndOperatorsMap.put("@", Token.AT_SIGN);
//        keywordsAndOperatorsMap.put("{", Token.LEFT_BRACE);
//        keywordsAndOperatorsMap.put("}", Token.RIGHT_BRACE);
    }

    public List<Lexeme> analyzeCode(Map<Integer,String> lines) {

        List<Lexeme> lexemes = new ArrayList<>();

        lines.forEach( (numLine, line)  ->{
            Map<String, String> lexLine = analyzeLine(line.strip());
            lexLine.forEach((token, classification) -> lexemes.add(new Lexeme(token, classification)));
        });

        return lexemes;
    }

    private Map<String, String> analyzeLine(String line) {

        Map<String, String> lineTokens = new HashMap<>();
//        Automaton automaton = new Automaton();

//        for (String str : line.split(" ")) {
//            if (keywordsAndOperatorsMap.containsKey(str.toLowerCase()))
//                lineTokens.put(str, keywordsAndOperatorsMap.get(str.toLowerCase()));
//            else
//                lineTokens.put(str, automaton.evaluate(str));
//        }

//        return lineTokens;

        // Define regex patterns for keywords, identifiers, numbers, and operators
        String variableIdentifier = "[a-z|A-Z][a-z|A-Z|0-9|_]*";
        String variableDeclaration = "\\bI HAS A\\b";
        String functionIdentifier = "\\b(HOW IZ I)\\b | \\b(IF U SAY SO)\\b";
        String loopIdentifier = "\\b(IM IN YR)\\b | \\b(IM OUTTA YR)\\b";
        String numbrLiteral = "(?:-)?\\d+[^.]";
        String numbarLiteral = "(?:-)?\\d+\\.\\d+";
        String yarnLiteral = "\\\"(.*)\\\"";
        String stringDelimeter = "\"";
        String codeDelimeter = "(HAI|KTHXBYE)";
        String outputKeyword = "VISIBLE";

        // Combine patterns into a single pattern
        String combinedPattern = "(" + numbarLiteral + "|" + numbrLiteral + "|" + outputKeyword + "|" + variableDeclaration  + "|" + functionIdentifier + "|" + loopIdentifier + "|"
                  + yarnLiteral + "|" + stringDelimeter + "|" + codeDelimeter + "|" +  variableIdentifier + ")";

        // Create a Pattern object
        Pattern pattern = Pattern.compile(combinedPattern);

        // Create a Matcher object
        Matcher matcher = pattern.matcher(line);

        // Process matches
        while (matcher.find()) {
            String token = matcher.group();
            if (token.matches(codeDelimeter)) {
                System.out.println("Code Delimeter: " + token);
                lineTokens.put(token, "Code Delimeter");
            } else if (token.matches(variableDeclaration)) {
                System.out.println("Variable Declaration: " + token);
                lineTokens.put(token, "Variable Declaration");
            }else if (token.matches(outputKeyword)) {
                System.out.println("Output Keyword: " + token);
                lineTokens.put(token, "Output Keyword");
            } else if (token.matches(functionIdentifier)) {
                System.out.println("Function Identifier: " + token);
                lineTokens.put(token, "Function Identifier");
            } else if (token.matches(loopIdentifier)) {
                System.out.println("Loop Identifier: " + token);
                lineTokens.put(token, "Loop Identifier");
            } else if (token.matches(numbrLiteral)) {
                System.out.println("Numbr Literal: " + token);
                lineTokens.put(token, "Numbr Literal");
            } else if (token.matches(numbarLiteral)) {
                System.out.println("Numbar Literal: " + token);
                lineTokens.put(token, "Numbar Literal");
            } else if (token.matches(yarnLiteral)) {
                System.out.println("Yarn Literal: " + token);
                lineTokens.put("\"", "String Delimeter");
                lineTokens.put(token.substring(1, token.length() - 1), "Yarn Literal");
                lineTokens.put("\"", "String Delimeter");
            } else if (token.matches(stringDelimeter)) {
                System.out.println("String Delimeter: " + token);
                lineTokens.put(token, "String Delimeter");
            } else if (token.matches(variableIdentifier)) {
                System.out.println("Variable Identifier: " + token);
                lineTokens.put(token, "Variable Identifier");
            } else {
                System.out.println("Unknown: " + token);
            }
        }

        return lineTokens;
    }
}

