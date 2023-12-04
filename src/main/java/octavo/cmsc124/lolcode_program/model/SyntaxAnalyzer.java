package octavo.cmsc124.lolcode_program.model;

import java.util.List;
import java.util.Objects;

public class SyntaxAnalyzer {

    private final List<Lexeme> lexemes;
    private int currentLexemeIndex;

    public SyntaxAnalyzer(List<Lexeme> lexemes) {
        this.lexemes = lexemes;
        this.currentLexemeIndex = 0;
    }

    public void analyze() {
        program();
    }

    private void program() {
        // Implement the syntax rules for the entire program
        statementList();
    }

    private void statementList() {
        // Implement the syntax rules for a list of statements
        while (currentLexemeIndex < lexemes.size()) {
            statement();
        }
    }

    private void statement() {
        try{
            if(match(LexemeType.CODE_DELIMITER) && !(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "KTHXBYE"))){
                consume(LexemeType.CODE_DELIMITER);

                while(currentLexemeIndex != lexemes.size()-1){
                    if (match(LexemeType.VARIABLE_DELIMITER) && !(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "BUHBYE"))) {
                        consume(LexemeType.VARIABLE_DELIMITER);
                        variableDeclaration();
                        while(!match(LexemeType.VARIABLE_DELIMITER)){
                            variableDeclaration();
                        }
                    } else if ((Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "BUHBYE"))) {
                        consume(LexemeType.VARIABLE_DELIMITER);
                    } else if (match(LexemeType.OUTPUT_KEYWORD)) {
                        outputStatement();
                    } else if (match(LexemeType.ARITHMETIC_OPERATION)){
                        arithmeticOperation();
                    } else if(match(LexemeType.BOOLEAN_OPERATION)){
                        booleanOperation();
                    }
//                else {
//                    // Handle other statement types
//                    // For simplicity, assume other statements are variable assignments
//                    variableAssignment();
//                }
                    System.out.println("Lexeme: " + lexemes.get(currentLexemeIndex).getLexeme().toString());
                }
            }else{
                consume(LexemeType.CODE_DELIMITER);
            }
        }catch (Error e){
            System.out.println("Syntax Error: " + e);
            System.exit(1);
        }

    }

    private void variableDeclaration() {
        consume(LexemeType.VARIABLE_DECLARATION);
        consume(LexemeType.VARIABLE_IDENTIFIER);

        if(match(LexemeType.VARIABLE_ASSIGNMENT)){
            consume(LexemeType.VARIABLE_ASSIGNMENT);
            consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
        }
    }

    private void arithmeticOperation(){
        consume(LexemeType.ARITHMETIC_OPERATION);

        if(match(LexemeType.ARITHMETIC_OPERATION)){
            arithmeticOperation();
            consume(LexemeType.OPERAND_SEPARATOR);
            consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
        }else{
            consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
            consume(LexemeType.OPERAND_SEPARATOR);
            if(match(LexemeType.ARITHMETIC_OPERATION)){
                arithmeticOperation();
            }else{
                consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
            }
        }
    }

    private void booleanOperation(){
        if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ALL OF") ||
                Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ANY OF")){

            consume(LexemeType.BOOLEAN_OPERATION);

            if (match(LexemeType.BOOLEAN_OPERATION)) {
                booleanOperation();
                while(match(LexemeType.OPERAND_SEPARATOR)){
                    consume(LexemeType.OPERAND_SEPARATOR);
                    consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                }
            }else {
                consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                while(match(LexemeType.OPERAND_SEPARATOR)) {
                    consume(LexemeType.OPERAND_SEPARATOR);
                    if (match(LexemeType.BOOLEAN_OPERATION)) {
                        booleanOperation();
                    } else {
                        consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                    }
                }
                consume(LexemeType.CONDITION_DELIMITER);
            }

        }else if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "NOT")){
            consume(LexemeType.BOOLEAN_OPERATION);

            if (match(LexemeType.BOOLEAN_OPERATION)) {
                booleanOperation();
            } else {
                consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
            }
        }
        else {
            consume(LexemeType.BOOLEAN_OPERATION);

            if (match(LexemeType.BOOLEAN_OPERATION)) {
                booleanOperation();
                consume(LexemeType.OPERAND_SEPARATOR);
                consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
            } else {
                consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                consume(LexemeType.OPERAND_SEPARATOR);
                if (match(LexemeType.BOOLEAN_OPERATION)) {
                    booleanOperation();
                } else {
                    consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                }
            }
        }
    }

    private void outputStatement() {
        consume(LexemeType.OUTPUT_KEYWORD);
        expression();
    }

//    private void variableAssignment() {
//        // Implement the syntax rules for variable assignment
//        consume(LexemeType.VARIABLE_IDENTIFIER);
//        consume(LexemeType.VARIABLE_ASSIGNMENT);
//        expression();
//    }

    private void expression() {
        // Implement the syntax rules for expressions
        // For simplicity, assume a literal expression
        if(match(LexemeType.LITERAL)){
            consume(LexemeType.LITERAL);

            if(match(LexemeType.PRINTING_SEPARATOR)){
                expression();
            }
        } else if (match(LexemeType.STRING_DELIMITER)) {
            consume(LexemeType.STRING_DELIMITER);
            consume(LexemeType.LITERAL);
            consume(LexemeType.STRING_DELIMITER);

            if(match(LexemeType.PRINTING_SEPARATOR)){
                expression();
            }
        } else if (match(LexemeType.VARIABLE_IDENTIFIER)) {
            consume(LexemeType.VARIABLE_IDENTIFIER);
        }else if (match(LexemeType.ARITHMETIC_OPERATION)){
            arithmeticOperation();
        }else if(match(LexemeType.BOOLEAN_OPERATION)){
            booleanOperation();
        }
    }

    // Helper methods for easier traversal of lexemes
    private boolean match(LexemeType expectedType) {
        if (currentLexemeIndex < lexemes.size()) {
            Lexeme currentLexeme = lexemes.get(currentLexemeIndex);
            return currentLexeme.getType() == expectedType;
        }
        return false;
    }

    private void consume(LexemeType expectedType) {
        if (match(expectedType)) {
            System.out.println("Match " + expectedType + " (expected) and " + lexemes.get(currentLexemeIndex).getType().toString()
                    + " Lexeme: " + lexemes.get(currentLexemeIndex).getLexeme());
            currentLexemeIndex++;
        } else {
            // Handle syntax error
            System.out.println("Syntax error: Expected " + expectedType + ", but found " +
                    (currentLexemeIndex < lexemes.size() ? lexemes.get(currentLexemeIndex).getType().toString()
                            + " Lexeme: " + lexemes.get(currentLexemeIndex).getLexeme() : "end of input"));

            System.exit(1);
        }
    }

    private void consume(LexemeType expectedType1, LexemeType expectedType2) {
        if (match(expectedType1) || match(expectedType2)) {
            System.out.println("Match " + expectedType1 + " or " + expectedType2 + " (expected) and "
                    + lexemes.get(currentLexemeIndex).getType().toString()
                    + " Lexeme: " + lexemes.get(currentLexemeIndex).getLexeme());
            currentLexemeIndex++;
        } else {
            // Handle syntax error
            System.out.println("Syntax error: Expected " + expectedType1 + " or " + expectedType2 + ", but found " +
                    (currentLexemeIndex < lexemes.size() ? lexemes.get(currentLexemeIndex).getType().toString()
                            + " Lexeme: " + lexemes.get(currentLexemeIndex).getLexeme() : "end of input"));

            System.exit(1);
        }
    }
}
