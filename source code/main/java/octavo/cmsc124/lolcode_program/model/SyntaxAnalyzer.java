package octavo.cmsc124.lolcode_program.model;
import octavo.cmsc124.lolcode_program.controller.GuiController;

import java.util.List;
import java.util.Objects;

public class SyntaxAnalyzer extends Thread{

    private final List<Lexeme> lexemes;
    private int currentLexemeIndex;

    public SyntaxAnalyzer(List<Lexeme> lexemes) {
        this.lexemes = lexemes;
        this.currentLexemeIndex = 0;
    }

    public int getCurrentLexemeIndex(){
        return this.currentLexemeIndex;
    }

    public List<Lexeme> getLexemes(){
        return this.lexemes;
    }

    public void run(){
        try {
            program();
            GuiController.hasSyntaxError = false;
        } catch (SyntaxErrorException e) {
            // Handle the exception (terminate the thread, log the error, etc.)
            System.out.println("Thread terminated due to syntax error: " + e.getMessage());
            GuiController.hasSyntaxError = true;
        }
    }

    private void program() throws SyntaxErrorException {
        // Implement the syntax rules for the entire program
        statementList();
    }

    private void statementList() throws SyntaxErrorException {
        // Implement the syntax rules for a list of statements
        while (currentLexemeIndex < lexemes.size()) {
            statement();
        }
    }

    private void statement() throws SyntaxErrorException {
            if(match(LexemeType.CODE_DELIMITER) && !(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "KTHXBYE"))){

                consume(LexemeType.CODE_DELIMITER);

                while(currentLexemeIndex != lexemes.size()-1 && !match(LexemeType.CODE_DELIMITER)){
                    if(Thread.currentThread().isInterrupted()){
                        return;
                    }

                    if (match(LexemeType.VARIABLE_DELIMITER)) { // HOW IZ I
                        variableDeclaration();
                    }else if (match(LexemeType.OUTPUT_KEYWORD)) { // VISIBLE
                        outputStatement();
                    } else if (match(LexemeType.ARITHMETIC_OPERATION)){
                        arithmeticOperation();
                    } else if(match(LexemeType.BOOLEAN_OPERATION)){
                        booleanOperation();
                    } else if(match(LexemeType.COMPARISON_OPERATION)){ // BOTH SAEM & DIFFRINT
                        comparisonOperation();
                    } else if(match(LexemeType.CONCATENATION_KEYWORD)){ // SMOOSH
                        concatenationOperation();
                    } else if(match(LexemeType.VARIABLE_IDENTIFIER)){ // REASSIGNMENT: use of R
                        variableAssignmentAndTypeCasting();
                    } else if(match(LexemeType.TYPECASTING_OPERATOR)){ // REASSIGNMENT: use of R
                        typeCasting();
                    } else if (match(LexemeType.INPUT_KEYWORD)) { // INPUT: GIMMEH x
                        consume(LexemeType.INPUT_KEYWORD);
                        consume(LexemeType.VARIABLE_IDENTIFIER);
                    } else if (match(LexemeType.FLOW_CONTROL_DELIMITER)) {
                        if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "O RLY?")){ // if-else case
                            ifElseControlFlow();
                        }else{                      // switch case
                            switchCaseControlFlow();
                        }
                    } else if (match(LexemeType.LOOP_DELIMITER)) {
                        loop();
                    } else if (match(LexemeType.FUNCTION_DELIMITER)){
                        function();
                    } else if (match(LexemeType.FUNCTION_CALL_KEYWORD)) {
                        functionCall();
                    } else if (match(LexemeType.COMMENT_KEYWORD)) {
                        consume(LexemeType.COMMENT_KEYWORD);

                        if(match(LexemeType.INLINE_COMMENT)){
                            consume(LexemeType.INLINE_COMMENT);
                        } else if (match(LexemeType.BLOCK_COMMENT)) {
                            String errorMessage = "Syntax Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() + ": " +
                                    lexemes.get(currentLexemeIndex).getStringType() + " is not allowed as an inline comment";

                            throw new SyntaxErrorException(errorMessage);
                        }
                    } else {
                        String errorMessage = "Syntax Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() + ": " +
                                "Unexpected " +
                                (currentLexemeIndex < lexemes.size() ? lexemes.get(currentLexemeIndex).getTypeStr() : "end of input");

//                        GuiController.staticOutputPane.appendText(errorMessage + "\n");

                        throw new SyntaxErrorException(errorMessage);
                    }
//                    System.out.println("Lexeme: " + lexemes.get(currentLexemeIndex).getLexeme().toString());
                }
            }else {
                consume(LexemeType.CODE_DELIMITER);
            }
    }

    private void expression(LexemeType exclude) throws SyntaxErrorException {

        if(match(exclude)){                     // Exclusion
            System.out.println("Error");
        }else if (match(LexemeType.ARITHMETIC_OPERATION)){
            arithmeticOperation();
        } else if(match(LexemeType.BOOLEAN_OPERATION)){
            booleanOperation();
        } else if(match(LexemeType.COMPARISON_OPERATION)){
            comparisonOperation();
        } else if(match(LexemeType.CONCATENATION_KEYWORD)){
            concatenationOperation();
        } else if(match(LexemeType.TYPECASTING_OPERATOR)){
            typeCasting();
        } else if(match(LexemeType.OUTPUT_KEYWORD)){
            outputStatement();
        } else if(match(LexemeType.LITERAL)){
            consume(LexemeType.LITERAL);
        } else if(match(LexemeType.STRING_DELIMITER)){
            consumeString();
        } else if(match(LexemeType.VARIABLE_IDENTIFIER)){
            variableAssignmentAndTypeCasting();
        } else if (match(LexemeType.INPUT_KEYWORD)) { // INPUT: GIMMEH x
            consume(LexemeType.INPUT_KEYWORD);
            consume(LexemeType.VARIABLE_IDENTIFIER);
        } else if(match(LexemeType.LOOP_DELIMITER)){
            loop();
        } else if(match(LexemeType.FUNCTION_DELIMITER)){
            function();
        } else if(match(LexemeType.FUNCTION_CALL_KEYWORD)){
            functionCall();
        } else if (match(LexemeType.COMMENT_KEYWORD)) {
            consume(LexemeType.COMMENT_KEYWORD);

            if(match(LexemeType.INLINE_COMMENT)){
                consume(LexemeType.INLINE_COMMENT);
            } else if (match(LexemeType.BLOCK_COMMENT)) {
                String errorMessage = "Syntax Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() + ": " +
                        lexemes.get(currentLexemeIndex).getStringType() + " is not allowed as an inline comment";

                throw new SyntaxErrorException(errorMessage);
            }
        }
    }

    private void ifElseControlFlow() throws SyntaxErrorException {
        consume(LexemeType.FLOW_CONTROL_DELIMITER);


        if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "YA RLY")){
            // YA RLY (required)
            consume(LexemeType.FLOW_CONTROL_KEYWORD);

            while (!match(LexemeType.FLOW_CONTROL_KEYWORD) || !match(LexemeType.FLOW_CONTROL_DELIMITER)){        // Continue until if-else keyword is encountered
                expression(LexemeType.NULL_VALUE);

                if(match(LexemeType.FLOW_CONTROL_KEYWORD) || match(LexemeType.FLOW_CONTROL_DELIMITER)){
                    break;
                }
            }
        }

        if (Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "MEBBE")) {
            // MEBBE
            while(match(LexemeType.FLOW_CONTROL_KEYWORD) && Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "MEBBE")){
                consume(LexemeType.FLOW_CONTROL_KEYWORD);
                expression(LexemeType.NULL_VALUE);


                while(!match(LexemeType.FLOW_CONTROL_KEYWORD)){     // Continue until if-else keyword is encountered
                    expression(LexemeType.NULL_VALUE);
                }

            }
        }

        if (match(LexemeType.FLOW_CONTROL_KEYWORD) && Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "NO WAI")){
            // NO WAI
            consume(LexemeType.FLOW_CONTROL_KEYWORD);

            while (!match(LexemeType.FLOW_CONTROL_DELIMITER)){  // Continue until if-else delimiter is encountered
                expression(LexemeType.NULL_VALUE);
            }
        }

        consume(LexemeType.FLOW_CONTROL_DELIMITER);
    }

    private void switchCaseControlFlow() throws SyntaxErrorException {
        consume(LexemeType.FLOW_CONTROL_DELIMITER);

        while(match(LexemeType.FLOW_CONTROL_KEYWORD) && !Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "OMGWTF")){
            consume(LexemeType.FLOW_CONTROL_KEYWORD);

            // Case must be literal
            if(match(LexemeType.STRING_DELIMITER)){
                consumeString();
            }else{
                consume(LexemeType.LITERAL);
            }

            // Handles cases when expression is more than 1
            while(!match(LexemeType.FLOW_CONTROL_KEYWORD) && !match(LexemeType.FLOW_CONTROL_DELIMITER) &&
                    !match(LexemeType.BREAK_OR_EXIT_OPERATOR)){
                expression(LexemeType.NULL_VALUE);
            }

            // If Break Keyword is Encountered
            if(match(LexemeType.BREAK_OR_EXIT_OPERATOR)){
                consume(LexemeType.BREAK_OR_EXIT_OPERATOR);
            }
        }

        // If a default condition is encountered
        if(match(LexemeType.FLOW_CONTROL_KEYWORD) && Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "OMGWTF")){
            consume(LexemeType.FLOW_CONTROL_KEYWORD);

            // Handles cases when expression is more than 1
            while(!match(LexemeType.FLOW_CONTROL_KEYWORD) && !match(LexemeType.FLOW_CONTROL_DELIMITER)){
                expression(LexemeType.NULL_VALUE);
            }
        }

        consume(LexemeType.FLOW_CONTROL_DELIMITER);
    }

    private void loop() throws SyntaxErrorException{
        consume(LexemeType.LOOP_DELIMITER);
        consume(LexemeType.LOOP_IDENTIFIER);
        consume(LexemeType.LOOP_OPERATION);
        consume(LexemeType.LOOP_OR_FUNCTION_KEYWORD);
        consume(LexemeType.VARIABLE_IDENTIFIER);
        consume(LexemeType.LOOP_CONDITION);

        while(!Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "IM OUTTA YR") && !match(LexemeType.BREAK_OR_EXIT_OPERATOR)){
            expression(LexemeType.NULL_VALUE);
        }

        if(match(LexemeType.BREAK_OR_EXIT_OPERATOR)){
            consume(LexemeType.BREAK_OR_EXIT_OPERATOR);
        }

        consume(LexemeType.LOOP_DELIMITER);
        consume(LexemeType.LOOP_IDENTIFIER);
    }

    private void function() throws SyntaxErrorException {
        consume(LexemeType.FUNCTION_DELIMITER);

        int referenceLineNumber = lexemes.get(currentLexemeIndex).getLineNumber();

        consume(LexemeType.FUNCTION_IDENTIFIER);

        if(match(LexemeType.VARIABLE_IDENTIFIER) && lexemes.get(currentLexemeIndex).getLineNumber() == referenceLineNumber){
            throw new SyntaxErrorException("Syntax error at line " + referenceLineNumber + ": " +
                    lexemes.get(currentLexemeIndex).getLexeme() + " is unexpected");
        }

        while(match(LexemeType.LOOP_OR_FUNCTION_KEYWORD)){
            consume(LexemeType.LOOP_OR_FUNCTION_KEYWORD);
            consume(LexemeType.VARIABLE_IDENTIFIER);

            if(match(LexemeType.OPERAND_SEPARATOR)){
                consume(LexemeType.OPERAND_SEPARATOR);
            }

            if(match(LexemeType.VARIABLE_IDENTIFIER) && lexemes.get(currentLexemeIndex).getLineNumber() == referenceLineNumber){
                throw new SyntaxErrorException("Syntax error at line " + referenceLineNumber + ": " +
                        lexemes.get(currentLexemeIndex).getLexeme() + " is unexpected");
            }
        }

        while(!match(LexemeType.FUNCTION_DELIMITER) && !match(LexemeType.RETURN_KEYWORD) && !match(LexemeType.BREAK_OR_EXIT_OPERATOR)){
            expression(LexemeType.FUNCTION_DELIMITER);
        }

        if(match(LexemeType.RETURN_KEYWORD)){
            consume(LexemeType.RETURN_KEYWORD);

            // Function inside a Function is not allowed
            expression(LexemeType.FUNCTION_DELIMITER);
        }

        if(match(LexemeType.BREAK_OR_EXIT_OPERATOR)){
            consume(LexemeType.BREAK_OR_EXIT_OPERATOR);
        }

        consume(LexemeType.FUNCTION_DELIMITER);
    }

    private void functionCall() throws SyntaxErrorException {
        consume(LexemeType.FUNCTION_CALL_KEYWORD);
        consume(LexemeType.FUNCTION_IDENTIFIER);

        while(match(LexemeType.LOOP_OR_FUNCTION_KEYWORD)){
            consume(LexemeType.LOOP_OR_FUNCTION_KEYWORD);

            expression(LexemeType.NULL_VALUE);


            if(match(LexemeType.OPERAND_SEPARATOR)){
                consume(LexemeType.OPERAND_SEPARATOR);
            }
        }

        consume(LexemeType.CONDITION_DELIMITER);
    }

    private void variableDeclaration() throws SyntaxErrorException {
        consume(LexemeType.VARIABLE_DELIMITER);

        while(!match(LexemeType.VARIABLE_DELIMITER)){
            consume(LexemeType.VARIABLE_DECLARATION);
            consume(LexemeType.VARIABLE_IDENTIFIER);

            if(match(LexemeType.VARIABLE_ASSIGNMENT)){
                consume(LexemeType.VARIABLE_ASSIGNMENT);

                if(match(LexemeType.STRING_DELIMITER)){
                    consumeString();
                }else if(match(LexemeType.LITERAL) || match(LexemeType.VARIABLE_IDENTIFIER)){
                    consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                }else{
                    expression(LexemeType.OUTPUT_KEYWORD);
                }
            }
        }

        consume(LexemeType.VARIABLE_DELIMITER);
    }

    private void variableAssignmentAndTypeCasting() throws SyntaxErrorException {
        consume(LexemeType.VARIABLE_IDENTIFIER);

        if(match(LexemeType.REASSIGNMENT_OPERATOR)){
            consume(LexemeType.REASSIGNMENT_OPERATOR);

            if(match(LexemeType.TYPECASTING_OPERATOR)){
                consume(LexemeType.TYPECASTING_OPERATOR);
                consume(LexemeType.VARIABLE_IDENTIFIER);
                consume(LexemeType.DATA_TYPE_KEYWORD);
            }else{
                expression(LexemeType.NULL_VALUE);
            }
        }else if(match(LexemeType.TYPECASTING_OPERATOR)){
            consume(LexemeType.TYPECASTING_OPERATOR);
            consume(LexemeType.DATA_TYPE_KEYWORD);
        }
    }

    private void arithmeticOperation() throws SyntaxErrorException {
        consume(LexemeType.ARITHMETIC_OPERATION);

        if(match(LexemeType.ARITHMETIC_OPERATION)){
            arithmeticOperation();
        } else if (match(LexemeType.LITERAL)) {
            consume(LexemeType.LITERAL);
        } else if(match(LexemeType.VARIABLE_IDENTIFIER)){
            consume(LexemeType.VARIABLE_IDENTIFIER);
        } else if (match(LexemeType.STRING_DELIMITER)){
            consumeString();
//            throw new SyntaxErrorException("Syntax Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
//                    ": only NUMBR or NUMBAR data type is allowed (" + lexemes.get(currentLexemeIndex).getLexeme() + ")" );
        }

        consume(LexemeType.OPERAND_SEPARATOR);

        if(match(LexemeType.ARITHMETIC_OPERATION)){
            arithmeticOperation();
        } else if (match(LexemeType.LITERAL)) {
            consume(LexemeType.LITERAL);
        } else if(match(LexemeType.VARIABLE_IDENTIFIER)){
            consume(LexemeType.VARIABLE_IDENTIFIER);
        } else if (match(LexemeType.STRING_DELIMITER)){
            consumeString();
//            throw new SyntaxErrorException("Syntax Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
//                    ": only NUMBR or NUMBAR data type is allowed (" + lexemes.get(currentLexemeIndex).getLexeme() + ")");
        }
    }

    private void comparisonOperation() throws SyntaxErrorException {
        consume(LexemeType.COMPARISON_OPERATION);

        if(match(LexemeType.ARITHMETIC_OPERATION)){
            arithmeticOperation();
        }else if(match(LexemeType.LITERAL)){
            if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "WIN") ||
                    Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "FAIL")){
                throw new SyntaxErrorException("Syntax Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                        ": only NUMBR or NUMBAR data type is allowed (" + lexemes.get(currentLexemeIndex).getLexeme() + ")");
            }
            consume(LexemeType.LITERAL);
        }else if(match(LexemeType.VARIABLE_IDENTIFIER)){
            consume(LexemeType.VARIABLE_IDENTIFIER);
        }else if (match(LexemeType.STRING_DELIMITER)){
            throw new SyntaxErrorException("Syntax Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                    ": only NUMBR or NUMBAR data type is allowed (" + lexemes.get(currentLexemeIndex).getLexeme() + ")");
        }

        consume(LexemeType.OPERAND_SEPARATOR);

        if(match(LexemeType.ARITHMETIC_OPERATION)){
            arithmeticOperation();
        }else if(match(LexemeType.LITERAL)){
            if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "WIN") ||
                    Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "FAIL")){
                throw new SyntaxErrorException("Syntax Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                        ": only NUMBR or NUMBAR data type is allowed (" + lexemes.get(currentLexemeIndex).getLexeme() + ")" );
            }
            consume(LexemeType.LITERAL);
        }else if(match(LexemeType.VARIABLE_IDENTIFIER)){
            consume(LexemeType.VARIABLE_IDENTIFIER);
        }else if (match(LexemeType.STRING_DELIMITER)){
            throw new SyntaxErrorException("Syntax Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                    ": only NUMBR or NUMBAR data type is allowed (" + lexemes.get(currentLexemeIndex).getLexeme() + ")" );
        }
    }

    private void booleanOperation() throws SyntaxErrorException {
        // ALL OF and ANY OF
        if (Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ALL OF") ||
                Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ANY OF")) {

            consume(LexemeType.BOOLEAN_OPERATION);

            if (match(LexemeType.BOOLEAN_OPERATION) && !Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ALL OF")
                    && !Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ANY OF")) {
                booleanOperation();
                while (match(LexemeType.OPERAND_SEPARATOR)) {
                    consume(LexemeType.OPERAND_SEPARATOR);

                    if (match(LexemeType.BOOLEAN_OPERATION) && !Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ALL OF")
                            && !Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ANY OF")) {
                        booleanOperation();
                    }else {
                        if (match(LexemeType.STRING_DELIMITER)) {
                            consumeString();
                        } else {
                            consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                        }
                    }
                }
            } else {
                if (match(LexemeType.STRING_DELIMITER)) {
                    consumeString();
                } else {
                    consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                }
                while (match(LexemeType.OPERAND_SEPARATOR)) {
                    consume(LexemeType.OPERAND_SEPARATOR);
                    if (match(LexemeType.BOOLEAN_OPERATION) && !Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ALL OF")
                            && !Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ANY OF")) {
                        booleanOperation();
                    } else {
                        if (match(LexemeType.STRING_DELIMITER)) {
                            consumeString();
                        } else {
                            consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                        }
                    }
                }
            }
            consume(LexemeType.CONDITION_DELIMITER);

        }

        // NOT
        else if (Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "NOT")) {

            consume(LexemeType.BOOLEAN_OPERATION);

            if (match(LexemeType.BOOLEAN_OPERATION)) {
                booleanOperation();
            } else {
                if (match(LexemeType.STRING_DELIMITER)) {
                    consumeString();
                } else {
                    consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                }
            }
        }

        // BOTH OF | EITHER OF | WON OF
        else {
            consume(LexemeType.BOOLEAN_OPERATION);

            if (match(LexemeType.BOOLEAN_OPERATION)) {
                booleanOperation();
                consume(LexemeType.OPERAND_SEPARATOR);
                if (match(LexemeType.STRING_DELIMITER)) {
                    consumeString();
                } else if (match(LexemeType.BOOLEAN_OPERATION)) {
                    booleanOperation();
                } else {
                    consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                }
            } else {
                if (match(LexemeType.STRING_DELIMITER)) {
                    consumeString();
                } else if (match(LexemeType.BOOLEAN_OPERATION)) {
                    booleanOperation();
                } else {
                    consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                }

                consume(LexemeType.OPERAND_SEPARATOR);

                if (match(LexemeType.BOOLEAN_OPERATION)) {
                    booleanOperation();
                } else {
                    if (match(LexemeType.STRING_DELIMITER)) {
                        consumeString();
                    } else if (match(LexemeType.BOOLEAN_OPERATION)) {
                        booleanOperation();
                    } else {
                        consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                    }
                }
            }
        }
    }


    private void outputStatement() throws SyntaxErrorException {
        consume(LexemeType.OUTPUT_KEYWORD);

        int referenceLineNumber = lexemes.get(currentLexemeIndex).getLineNumber();

        if(match(LexemeType.STRING_DELIMITER)){
            consumeString();
        }else{
            expression(LexemeType.OUTPUT_KEYWORD);
        }

        while(match(LexemeType.PRINTING_SEPARATOR)){
            consume(LexemeType.PRINTING_SEPARATOR);

            if(match(LexemeType.STRING_DELIMITER)){
                consumeString();
            }else{
                expression(LexemeType.OUTPUT_KEYWORD);
            }
        }

        if(lexemes.get(currentLexemeIndex).getLineNumber() == referenceLineNumber){
            throw new SyntaxErrorException("Syntax Error at line " + referenceLineNumber +
                ": invalid output statement. Check if the keywords and printing separator were properly used");
        }
    }

    private void consumeString() throws SyntaxErrorException {
        consume(LexemeType.STRING_DELIMITER);

        // Checks if the string is empty
        if(match(LexemeType.LITERAL)){
            consume(LexemeType.LITERAL);
        }
        consume(LexemeType.STRING_DELIMITER);
    }

    private void concatenationOperation() throws SyntaxErrorException {
        consume(LexemeType.CONCATENATION_KEYWORD);

        if(match(LexemeType.STRING_DELIMITER)){
            consumeString();
        }else{
            expression(LexemeType.NULL_VALUE);
        }

        while(match(LexemeType.OPERAND_SEPARATOR)){
            consume(LexemeType.OPERAND_SEPARATOR);
            if(match(LexemeType.STRING_DELIMITER)){
                consumeString();
            } else{
                expression(LexemeType.NULL_VALUE);
            }
        }
    }

    private void typeCasting() throws SyntaxErrorException {
        consume(LexemeType.TYPECASTING_OPERATOR);
        consume(LexemeType.VARIABLE_IDENTIFIER);

        if(match(LexemeType.TYPECASTING_OPERATOR)){
            consume(LexemeType.TYPECASTING_OPERATOR);
            consume(LexemeType.DATA_TYPE_KEYWORD);
        }else{
            consume(LexemeType.DATA_TYPE_KEYWORD);
        }
    }

    private boolean match(LexemeType expectedType) {
        if (currentLexemeIndex < lexemes.size()) {
            Lexeme currentLexeme = lexemes.get(currentLexemeIndex);
            return currentLexeme.getType() == expectedType;
        }
        return false;
    }

    private void consume(LexemeType expectedType) throws SyntaxErrorException {
        if (match(expectedType)) {
            System.out.println("Match " + expectedType + " (expected) and " + lexemes.get(currentLexemeIndex).getType().toString()
                    + " Lexeme: " + lexemes.get(currentLexemeIndex).getLexeme() + " line number: " + lexemes.get(currentLexemeIndex).getLineNumber());
            currentLexemeIndex++;
        } else {
            // Handle syntax error
            System.out.println("Syntax error: Expected " + expectedType + ", but found " +
                    (currentLexemeIndex < lexemes.size() ? lexemes.get(currentLexemeIndex).getType().toString()
                            + " Lexeme test: " + lexemes.get(currentLexemeIndex).getLexeme()
                            + " line number: " + lexemes.get(currentLexemeIndex).getLineNumber() : "end of input"));

            String errorMessage = "Syntax Error in Line " + lexemes.get(currentLexemeIndex).getLineNumber() + ": " +
                    "Expected " + expectedType.toString().toLowerCase().replace("_", " ") + ", but found " +
                    (currentLexemeIndex < lexemes.size() ? lexemes.get(currentLexemeIndex).getStringType(): "end of input") + "." ;

//            GuiController.staticOutputPane.appendText(errorMessage + "\n");

            throw new SyntaxErrorException(errorMessage);
        }
    }

    private void consume(LexemeType expectedType1, LexemeType expectedType2) throws SyntaxErrorException {
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

            String errorMessage = "Syntax Error at Line " + lexemes.get(currentLexemeIndex).getLineNumber() + ": " +
                    "Expected " + expectedType1.toString().toLowerCase().replace("_", " ") +
                    " or " + expectedType2.toString().toLowerCase().replace("_", " ") +
                    ", but found " +
                    (currentLexemeIndex < lexemes.size() ? lexemes.get(currentLexemeIndex).getStringType(): "end of input") + "." ;

//            GuiController.staticOutputPane.appendText(errorMessage + "\n");

            throw new SyntaxErrorException(errorMessage);
        }
    }


    // Custom exception class for syntax errors
    private static class SyntaxErrorException extends Exception {
        public SyntaxErrorException(String message) {
            super(message);
            GuiController.staticOutputPane.clear();
            GuiController.staticOutputPane.setStyle("-fx-text-fill: red;");
            GuiController.staticOutputPane.appendText(message + "\n");
            GuiController.staticOutputPane.setEditable(false);
        }
    }

}
