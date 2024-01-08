package octavo.cmsc124.lolcode_program.model;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import octavo.cmsc124.lolcode_program.controller.GuiController;

import java.util.*;

public class SemanticAnalyzer extends Thread {

    private List<Lexeme> lexemes;
    private int currentLexemeIndex;
    private Map<String, Map<String, Object>> functionList = new HashMap<>();
    private int currentFunctionListIndex;
    private List<Integer> loopCondition;
    public ArrayList<Variable> variableList;
    private Map<String, Object> varTable;
    private String tempOutputString = "";
    private String tempConcatString = "";
    private ArrayList<java.lang.Boolean> troofList = new ArrayList<java.lang.Boolean>();
    private boolean hasInput = false;
    private boolean ifElseCondition = false;

    public SemanticAnalyzer(List<Lexeme> lexemes) {
        this.lexemes = lexemes;
        this.currentLexemeIndex = 0;
        this.currentFunctionListIndex = 0;
        this.varTable = new HashMap<>() {{
            put("IT", DataType.NOOB);
        }};
        this.variableList = new ArrayList<>();
    }

    public void run() {
        try {
            program();

            varTable.keySet().forEach((item) -> {
                Object varValue = varTable.get(item);
                DataType dataType = DataType.NOOB;

                if (varValue instanceof String) {
                    dataType = DataType.YARN;
                } else if (varValue instanceof Boolean) {
                    dataType = DataType.TROOF;
                } else if (varValue instanceof Float) {
                    dataType = DataType.NUMBAR;
                } else if (varValue instanceof Integer) {
                    dataType = DataType.NUMBR;
                } else if (varValue == null) {
                    varValue = DataType.NOOB;
                }

                System.out.println("varValue: " + varValue);
                System.out.println("dataType: " + dataType);

                variableList.add(new Variable(item, varValue, dataType));
            });

            GuiController.staticVariableObservableList.addAll(variableList);
        } catch (SemanticErrorException e) {
            // Handle the exception (terminate the thread, log the error, etc.)
            System.out.println("Thread terminated due to semantic error: " + e.getMessage());
        }


        System.out.println(variableList);
    }


    private void program() throws SemanticErrorException {
        // Implement the syntax rules for the entire
        GuiController.staticOutputPane.setStyle("");
        statementList();
    }

    public void statementList() throws SemanticErrorException {
        if (match(LexemeType.CODE_DELIMITER) && !(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "KTHXBYE"))) {

            consume(LexemeType.CODE_DELIMITER);

            while (currentLexemeIndex != lexemes.size() - 1 && !match(LexemeType.CODE_DELIMITER)) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                if (match(LexemeType.VARIABLE_DELIMITER)) { // HOW IZ I
                    variableDeclaration();
                } else if (match(LexemeType.OUTPUT_KEYWORD)) { // VISIBLE
                    tempOutputString = "";
                    outputStatement();
                    GuiController.staticOutputPane.appendText(tempOutputString);
                    GuiController.staticOutputPane.setEditable(false);
                    varTable.replace("IT", tempOutputString);
                } else if (match(LexemeType.ARITHMETIC_OPERATION)) {
                    arithmeticOperation();
                } else if (match(LexemeType.BOOLEAN_OPERATION)) {
                    booleanOperation();
                } else if (match(LexemeType.COMPARISON_OPERATION)) { // BOTH SAEM & DIFFRINT
                    comparisonOperation();
                } else if (match(LexemeType.CONCATENATION_KEYWORD)) { // SMOOSH
                    tempConcatString = "";
                    concatenationOperation();
                    varTable.replace("IT", tempConcatString);
                } else if (match(LexemeType.VARIABLE_IDENTIFIER)) { // REASSIGNMENT: use of R
                    variableAssignmentAndTypeCasting();
                } else if (match(LexemeType.TYPECASTING_OPERATOR)) { // REASSIGNMENT: use of R
                    typeCasting();
                } else if (match(LexemeType.INPUT_KEYWORD)) { // INPUT: GIMMEH x
                    inputOperation();
                }
                else if (match(LexemeType.FLOW_CONTROL_DELIMITER)) {
                    if (Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "O RLY?")) { // if-else case
                        ifElseControlFlow();
                    } else {                      // switch case
                        switchCaseControlFlow();
                    }
                } else if (match(LexemeType.LOOP_DELIMITER)) {
                    loop();
                } else if (match(LexemeType.FUNCTION_DELIMITER)) {
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

                        throw new SemanticErrorException(errorMessage);
                    }
                } else {
                    String errorMessage = "Syntax Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() + ": " +
                            "Unexpected " +
                            (currentLexemeIndex < lexemes.size() ? lexemes.get(currentLexemeIndex).getTypeStr() : "end of input");

                    GuiController.staticOutputPane.appendText(errorMessage + "\n");

                    throw new SemanticErrorException(errorMessage);
                }
                System.out.println("Lexeme: " + lexemes.get(currentLexemeIndex).getLexeme().toString());
            }
        } else {
            consume(LexemeType.CODE_DELIMITER);
        }
    }

    private void expression(LexemeType exclude) throws SemanticErrorException {

        if (match(exclude)) {                     // Exclusion
            System.out.println("Error");
            throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber()
            + ": " + exclude + " must not be invoked");
        } else if (match(LexemeType.ARITHMETIC_OPERATION)) {
            arithmeticOperation();
        } else if (match(LexemeType.BOOLEAN_OPERATION)) {
            booleanOperation();
        } else if (match(LexemeType.COMPARISON_OPERATION)) {
            comparisonOperation();
        } else if (match(LexemeType.CONCATENATION_KEYWORD)) { // Smoosh
            tempConcatString = "";
            concatenationOperation();
            varTable.replace("IT", tempConcatString);
        } else if (match(LexemeType.TYPECASTING_OPERATOR)) {
            typeCasting();
        } else if (match(LexemeType.OUTPUT_KEYWORD)) {
            tempOutputString = "";
            outputStatement();
            GuiController.staticOutputPane.appendText(tempOutputString);
            GuiController.staticOutputPane.setEditable(false);
            varTable.replace("IT", tempOutputString);
        }  else if (match(LexemeType.INPUT_KEYWORD)) { // INPUT: GIMMEH x
            inputOperation();
        } else if (match(LexemeType.LITERAL)) {
            int floatCheck = lexemes.get(currentLexemeIndex).getLexeme().indexOf(".");

            if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "WIN")){
                varTable.replace("IT", DataType.WIN);
            } else if (Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "FAIL")) {
                varTable.replace("IT", DataType.FAIL);
            }else if (floatCheck == -1) {
                varTable.replace("IT", Integer.parseInt(lexemes.get(currentLexemeIndex).getLexeme()));
            } else {
                varTable.replace("IT", Float.parseFloat(lexemes.get(currentLexemeIndex).getLexeme()));
            }

            consume(LexemeType.LITERAL);
        } else if (match(LexemeType.STRING_DELIMITER)) {
            // Checks if string is empty
            if (lexemes.get(currentLexemeIndex + 1).getType() == LexemeType.STRING_DELIMITER) {
                varTable.replace("IT", "");
            } else {
                varTable.replace("IT", lexemes.get(currentLexemeIndex + 1).getLexeme());
            }
            consumeString();
        } else if (match(LexemeType.VARIABLE_IDENTIFIER)) {
            if (varTable.containsKey(lexemes.get(currentLexemeIndex).getLexeme())) {
                varTable.replace("IT", varTable.get(lexemes.get(currentLexemeIndex).getLexeme()));
                variableAssignmentAndTypeCasting();
            } else {
                throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber()
                        + ": " + lexemes.get(currentLexemeIndex).getLexeme() + " is not defined.");
            }

        } else if (match(LexemeType.FLOW_CONTROL_DELIMITER)) {
            if (Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "O RLY?")) { // if-else case
                ifElseControlFlow();
            } else {                      // switch case
                switchCaseControlFlow();
            }
        } else if (match(LexemeType.LOOP_DELIMITER)) {
            loop();
        } else if (match(LexemeType.FUNCTION_DELIMITER)) {
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

                throw new SemanticErrorException(errorMessage);
            }
        }
    }

//    private void loopExpression() throws SemanticErrorException {
//
//        if (loopExpressionMatch(LexemeType.ARITHMETIC_OPERATION)) {
//            arithmeticOperation();
//        } else if (loopExpressionMatch(LexemeType.BOOLEAN_OPERATION)) {
//            booleanOperation();
//        } else if (loopExpressionMatch(LexemeType.COMPARISON_OPERATION)) {
//            comparisonOperation();
//        } else if (loopExpressionMatch(LexemeType.CONCATENATION_KEYWORD)) { // Smoosh
//            tempConcatString = "";
//            concatenationOperation();
//            varTable.replace("IT", tempConcatString);
//        } else if (loopExpressionMatch(LexemeType.TYPECASTING_OPERATOR)) {
//            typeCasting();
//        } else if (loopExpressionMatch(LexemeType.OUTPUT_KEYWORD)) {
//            tempOutputString = "";
//            outputStatement();
//            GuiController.staticOutputPane.appendText(tempOutputString);
//            GuiController.staticOutputPane.setEditable(false);
//            varTable.replace("IT", tempOutputString);
//        }  else if (loopExpressionMatch(LexemeType.INPUT_KEYWORD)) { // INPUT: GIMMEH x
//            inputOperation();
//        } else if (loopExpressionMatch(LexemeType.LITERAL)) {
//            int floatCheck = lexemes.get(currentLexemeIndex).getLexeme().indexOf(".");
//
//            if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "WIN")){
//                varTable.replace("IT", DataType.WIN);
//            } else if (Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "FAIL")) {
//                varTable.replace("IT", DataType.FAIL);
//            }else if (floatCheck == -1) {
//                varTable.replace("IT", Integer.parseInt(lexemes.get(currentLexemeIndex).getLexeme()));
//            } else {
//                varTable.replace("IT", Float.parseFloat(lexemes.get(currentLexemeIndex).getLexeme()));
//            }
//
//            consume(LexemeType.LITERAL);
//        } else if (loopExpressionMatch(LexemeType.STRING_DELIMITER)) {
//            // Checks if string is empty
//            if (lexemes.get(currentLexemeIndex + 1).getType() == LexemeType.STRING_DELIMITER) {
//                varTable.replace("IT", "");
//            } else {
//                varTable.replace("IT", lexemes.get(currentLexemeIndex + 1).getLexeme());
//            }
//            consumeString();
//        } else if (loopExpressionMatch(LexemeType.VARIABLE_IDENTIFIER)) {
//            if (varTable.containsKey(lexemes.get(currentLexemeIndex).getLexeme())) {
//                varTable.replace("IT", varTable.get(lexemes.get(currentLexemeIndex).getLexeme()));
//                variableAssignmentAndTypeCasting();
//            } else {
//                throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber()
//                        + ": " + lexemes.get(currentLexemeIndex).getLexeme() + " is not defined.");
//            }
//
//        } else if (loopExpressionMatch(LexemeType.FLOW_CONTROL_DELIMITER)) {
//            if (Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "O RLY?")) { // if-else case
//                ifElseControlFlow();
//            } else {                      // switch case
//                switchCaseControlFlow();
//            }
//        } else if (loopExpressionMatch(LexemeType.LOOP_DELIMITER)) {
//            loop();
//        } else if (loopExpressionMatch(LexemeType.FUNCTION_DELIMITER)) {
//            function();
//        } else if (loopExpressionMatch(LexemeType.FUNCTION_CALL_KEYWORD)) {
//            functionCall();
//        } else if (loopExpressionMatch(LexemeType.COMMENT_KEYWORD)) {
//            consume(LexemeType.COMMENT_KEYWORD);
//
//            if(match(LexemeType.INLINE_COMMENT)){
//                consume(LexemeType.INLINE_COMMENT);
//            } else if (loopExpressionMatch(LexemeType.BLOCK_COMMENT)) {
//                String errorMessage = "Syntax Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() + ": " +
//                        lexemes.get(currentLexemeIndex).getStringType() + " is not allowed as an inline comment";
//
//                throw new SemanticErrorException(errorMessage);
//            }
//        }
//    }


    private void ifElseControlFlow() throws SemanticErrorException {
        consume(LexemeType.FLOW_CONTROL_DELIMITER);

        System.out.println("1 ====> IT: " + varTable.get("IT"));
        System.out.println(Objects.equals(varTable.get("IT").toString(), "WIN"));
//
//        // Cast first the value of it to TROOF i.e. WIN or FAIL
//        cast("IT", "", DataType.TROOF.toString(), true);
//
//        System.out.println("2 ====> IT: " + varTable.get("IT"));

        ifElseCondition = Objects.equals(varTable.get("IT").toString(), "WIN");

        System.out.println("====> ifElseCondition: " + ifElseCondition);

        if(ifElseCondition){
            // YA RLY (required)
            consume(LexemeType.FLOW_CONTROL_KEYWORD);

            while (!match(LexemeType.FLOW_CONTROL_KEYWORD) || !match(LexemeType.FLOW_CONTROL_DELIMITER)){        // Continue until if-else keyword is encountered
                expression(LexemeType.NULL_VALUE);

                if(match(LexemeType.FLOW_CONTROL_KEYWORD) || match(LexemeType.FLOW_CONTROL_DELIMITER)){
                    break;
                }
            }

            // Consume remaining codes that is not reached by the if-else condition
            while (!match(LexemeType.FLOW_CONTROL_DELIMITER)){
                consume(lexemes.get(currentLexemeIndex).getType());
            }

        } else if (match(LexemeType.FLOW_CONTROL_KEYWORD)) {
            // Consume the YA RLY block
            consume(LexemeType.FLOW_CONTROL_KEYWORD);
            while(!match(LexemeType.FLOW_CONTROL_KEYWORD)){
                consume(lexemes.get(currentLexemeIndex).getType());
            }

            if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "MEBBE")){
                // MEBBE
                while(match(LexemeType.FLOW_CONTROL_KEYWORD) && Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "MEBBE")){
                    consume(LexemeType.FLOW_CONTROL_KEYWORD);
                    expression(LexemeType.NULL_VALUE);
                    cast(varTable.get("IT").toString(), "", DataType.TROOF.toString(), true);
                    if(Objects.equals(varTable.get("IT").toString(), "WIN")){
                        while(!match(LexemeType.FLOW_CONTROL_KEYWORD)){     // Continue until if-else keyword is encountered
                            expression(LexemeType.NULL_VALUE);
                        }
                        while (!match(LexemeType.FLOW_CONTROL_DELIMITER)){
                            consume(lexemes.get(currentLexemeIndex).getType());
                        }

                        break;
                    }else{
                        while(!match(LexemeType.FLOW_CONTROL_KEYWORD)){
                            consume(lexemes.get(currentLexemeIndex).getType());
                        }
                    }
                }

                if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "NO WAI")){
                    consume(LexemeType.FLOW_CONTROL_KEYWORD);
                    System.out.println("NOOOOOO WAIIIII");
                    while (!match(LexemeType.FLOW_CONTROL_DELIMITER)){  // Continue until if-else delimiter is encountered
                        expression(LexemeType.NULL_VALUE);
//                        System.out.print(".");
//                        System.out.print(lexemes.get(currentLexemeIndex).getLexeme());
//
//                        if(match(LexemeType.FLOW_CONTROL_DELIMITER)){
//                            break;
//                        }
                    }
                }

                // Consume remaining codes that is not reached by the if-else condition
                while (!match(LexemeType.FLOW_CONTROL_DELIMITER)){
                    consume(lexemes.get(currentLexemeIndex).getType());
                }
            }else if (match(LexemeType.FLOW_CONTROL_KEYWORD) && Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "NO WAI")){
                consume(LexemeType.FLOW_CONTROL_KEYWORD);

                if(!ifElseCondition){
                    while (!match(LexemeType.FLOW_CONTROL_DELIMITER)){  // Continue until if-else delimiter is encountered
                        expression(LexemeType.NULL_VALUE);
                    }
                }

                // Consume remaining codes that is not reached by the if-else condition
//                while (!match(LexemeType.FLOW_CONTROL_DELIMITER)){
//                    consume(lexemes.get(currentLexemeIndex).getType());
//                }
            }


        } else if (!ifElseCondition) {
            // If there is no NO WAI
            // Consume remaining codes that is not reached by the if-else condition
            while (!match(LexemeType.FLOW_CONTROL_DELIMITER)){
                expression(LexemeType.NULL_VALUE);
            }
        }

        consume(LexemeType.FLOW_CONTROL_DELIMITER);
    }

    private void switchCaseControlFlow() throws SemanticErrorException {
        consume(LexemeType.FLOW_CONTROL_DELIMITER);

        while(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "OMG")){

            consume(LexemeType.FLOW_CONTROL_KEYWORD);

            boolean isCaseTrue;

            // Case must be literal
            if(match(LexemeType.STRING_DELIMITER)){
                isCaseTrue = Objects.equals(varTable.get("IT").toString(), lexemes.get(currentLexemeIndex + 1).getLexeme());

                consumeString();
            }else{
                isCaseTrue = Objects.equals(varTable.get("IT").toString(), lexemes.get(currentLexemeIndex).getLexeme());

//                System.out.println("================= Lexeme : " + lexemes.get(currentLexemeIndex).getLexeme());
//                System.out.println(varTable.get("IT") == lexemes.get(currentLexemeIndex).getLexeme());

                consume(LexemeType.LITERAL);
            }

//            System.out.println("================= IT : " + varTable.get("IT"));
//            System.out.println("================= isCaseTrue : " + isCaseTrue);

            if (isCaseTrue){
                // Handles cases when expression is more than 1
                while(!match(LexemeType.FLOW_CONTROL_KEYWORD) && !match(LexemeType.FLOW_CONTROL_DELIMITER) &&
                        !match(LexemeType.BREAK_OR_EXIT_OPERATOR)){
                    expression(LexemeType.NULL_VALUE);
                }

                // If Break Keyword is Encountered
                if(match(LexemeType.BREAK_OR_EXIT_OPERATOR)){
                    consume(LexemeType.BREAK_OR_EXIT_OPERATOR);

                    // If case is true, and it has a break, consume all codes until the OIC is encountered
                    while(!match(LexemeType.FLOW_CONTROL_DELIMITER)){
                        consume(lexemes.get(currentLexemeIndex).getType());
                    }
                }
                // If there is no break keyword, all expression below the case are executed until GTFO or OMGWTF or OIC is encountered
                else if (Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "OMG") ||
                        Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "OMGWTF")) {
                    while (Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "OMG")){
                        consume(LexemeType.FLOW_CONTROL_KEYWORD);
                        consume(LexemeType.LITERAL);

                        while (!match(LexemeType.FLOW_CONTROL_KEYWORD) && !match(LexemeType.BREAK_OR_EXIT_OPERATOR)){
                            expression(LexemeType.NULL_VALUE);
                        }

                        if(match(LexemeType.BREAK_OR_EXIT_OPERATOR)){
                            System.out.println("=========> Case True: Next BREAK encountered");
                            consume(LexemeType.BREAK_OR_EXIT_OPERATOR);

                            break;
                        }
                    }

                    // If there are any residual cases, including the default, consume them all
                    while(!match(LexemeType.FLOW_CONTROL_DELIMITER)){
                        consume(lexemes.get(currentLexemeIndex).getType());
                    }
                }
//                else if (Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "OMGWTF")) {
//                    while(!match(LexemeType.FLOW_CONTROL_DELIMITER)){
//                        consume(lexemes.get(currentLexemeIndex).getType());
//                    }
//                }
            }else{ // If case is not true, consume the code block and move to next case
                while(!match(LexemeType.FLOW_CONTROL_KEYWORD)){
                    consume(lexemes.get(currentLexemeIndex).getType());
                }
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

    private void loop() throws SemanticErrorException {
        String loopIdentifier, tempVar, varValue;
        boolean isIncrement, isWile;

        consume(LexemeType.LOOP_DELIMITER);

        loopIdentifier = lexemes.get(currentLexemeIndex).getLexeme();
        consume(LexemeType.LOOP_IDENTIFIER);

        isIncrement = Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "UPPIN");
        consume(LexemeType.LOOP_OPERATION);
        consume(LexemeType.LOOP_OR_FUNCTION_KEYWORD);

        if(varTable.containsKey(lexemes.get(currentLexemeIndex).getLexeme())){
            tempVar = lexemes.get(currentLexemeIndex).getLexeme();
            consume(LexemeType.VARIABLE_IDENTIFIER);
        }else{
            throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber()
            + ": " + lexemes.get(currentLexemeIndex).getLexeme() + " is undefined");
        }

        isWile = Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "WILE");
        consume(LexemeType.LOOP_CONDITION);

        Map<String, Integer> loopConditionIndex = getLoopConditionIndex();

        varValue = varTable.get(tempVar).toString();

        if(isWile){
            wileLoop(isIncrement, tempVar, loopConditionIndex);
        }else{
            tilLoop(isIncrement, tempVar, loopConditionIndex);
        }

        varTable.replace(tempVar, varValue);

//        // Inside loop expressions
//        while (!match(LexemeType.LOOP_DELIMITER) && !match(LexemeType.BREAK_OR_EXIT_OPERATOR)) {
//            // Exclude loop condition - Loop of Loop is not allowed yet
//            expression(LexemeType.LOOP_DELIMITER);
//        }
//
//        if (match(LexemeType.BREAK_OR_EXIT_OPERATOR)) {
//            consume(LexemeType.BREAK_OR_EXIT_OPERATOR);
//        }

        System.out.println("Outside loop");

        consume(LexemeType.LOOP_DELIMITER);

        if(!Objects.equals(loopIdentifier, lexemes.get(currentLexemeIndex).getLexeme())){
            throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                    ": loop identifier (" + loopIdentifier + " and " + lexemes.get(currentLexemeIndex).getLexeme() +
                    ") not matched");
        }else{
            consume(LexemeType.LOOP_IDENTIFIER);
        }
    }

    private void wileLoop(boolean isIncrement, String tempVar, Map<String, Integer> loopConditionIndex) throws SemanticErrorException{

        boolean isLoopConditionTrue = Objects.equals(varTable.get("IT").toString(), "WIN");

        while (isLoopConditionTrue){
            expression(LexemeType.NULL_VALUE);

            if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "IM OUTTA YR") || match(LexemeType.BREAK_OR_EXIT_OPERATOR)){

                currentLexemeIndex = loopConditionIndex.get("Start");
                if(isIncrement){
                    if(varTable.get(tempVar).toString().contains(".")){
                        varTable.replace(tempVar, Float.parseFloat(varTable.get(tempVar).toString()) + 1);
                    }else{
                        varTable.replace(tempVar, (int) Float.parseFloat(varTable.get(tempVar).toString()) + 1);
                    }
                }else{
                    if(varTable.get(tempVar).toString().contains(".")){
                        varTable.replace(tempVar, Float.parseFloat(varTable.get(tempVar).toString()) - 1);
                    }else{
                        varTable.replace(tempVar, (int) Float.parseFloat(varTable.get(tempVar).toString()) - 1);
                    }
                }

                expression(LexemeType.NULL_VALUE);
                isLoopConditionTrue = Objects.equals(varTable.get("IT").toString(), "WIN");

                if(!isLoopConditionTrue){
                    while(!match(LexemeType.LOOP_DELIMITER)){
                        consume(lexemes.get(currentLexemeIndex).getType());
                    }

//                    // If there is a nested loop, consume the loop delimiter and identifier
//                    if(Objects.equals(lexemes.get(currentLexemeIndex + 3).getLexeme(), "NERFIN") ||
//                            Objects.equals(lexemes.get(currentLexemeIndex + 3).getLexeme(), "UPPIN")){
//                        consume(LexemeType.LOOP_DELIMITER);
//
//                        while(!match(LexemeType.LOOP_DELIMITER)){
//                            consume(lexemes.get(currentLexemeIndex).getType());
//                        }
//                    }

                    break;
                }

            }
        }

        if(!match(LexemeType.LOOP_DELIMITER)){
            while(!match(LexemeType.LOOP_DELIMITER)){
                consume(lexemes.get(currentLexemeIndex).getType());
            }
        }

//        // If there is a nested loop, consume the loop delimiter and identifier
//        if(Objects.equals(lexemes.get(currentLexemeIndex + 3).getLexeme(), "NERFIN") ||
//                Objects.equals(lexemes.get(currentLexemeIndex + 3).getLexeme(), "UPPIN")){
//            consume(LexemeType.LOOP_DELIMITER);
//
//            while(!match(LexemeType.LOOP_DELIMITER)){
//                consume(lexemes.get(currentLexemeIndex).getType());
//            }
//        }
    }

    private void tilLoop(boolean isIncrement, String tempVar, Map<String, Integer> loopConditionIndex) throws SemanticErrorException{
        boolean isLoopConditionTrue = Objects.equals(varTable.get("IT").toString(), "WIN");

        while (!isLoopConditionTrue){
            expression(LexemeType.NULL_VALUE);

            if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "IM OUTTA YR") || match(LexemeType.BREAK_OR_EXIT_OPERATOR)){

                currentLexemeIndex = loopConditionIndex.get("Start");
                if(isIncrement){
                    if(varTable.get(tempVar).toString().contains(".")){
                        varTable.replace(tempVar, Float.parseFloat(varTable.get(tempVar).toString()) + 1);
                    }else{
                        varTable.replace(tempVar, (int) Float.parseFloat(varTable.get(tempVar).toString()) + 1);
                    }
                }else{
                    if(varTable.get(tempVar).toString().contains(".")){
                        varTable.replace(tempVar, Float.parseFloat(varTable.get(tempVar).toString()) - 1);
                    }else{
                        varTable.replace(tempVar, (int) Float.parseFloat(varTable.get(tempVar).toString()) - 1);
                    }
                }

                expression(LexemeType.NULL_VALUE);
                isLoopConditionTrue = Objects.equals(varTable.get("IT").toString(), "WIN");

                if(isLoopConditionTrue){
                    while(!match(LexemeType.LOOP_DELIMITER)){
                        consume(lexemes.get(currentLexemeIndex).getType());
                    }

//                    // If there is a nested loop, consume the loop delimiter and identifier
//                    if(Objects.equals(lexemes.get(currentLexemeIndex + 3).getLexeme(), "NERFIN") ||
//                            Objects.equals(lexemes.get(currentLexemeIndex + 3).getLexeme(), "UPPIN")){
//                        consume(LexemeType.LOOP_DELIMITER);
//
//                        while(!match(LexemeType.LOOP_DELIMITER)){
//                            consume(lexemes.get(currentLexemeIndex).getType());
//                        }
//                    }

                    break;
                }

            }
        }

        if(!match(LexemeType.LOOP_DELIMITER)){
            while(!match(LexemeType.LOOP_DELIMITER)){
                consume(lexemes.get(currentLexemeIndex).getType());
            }
        }

//        // If there is a nested loop, consume the loop delimiter and identifier
//        if(Objects.equals(lexemes.get(currentLexemeIndex + 3).getLexeme(), "NERFIN") ||
//                Objects.equals(lexemes.get(currentLexemeIndex + 3).getLexeme(), "UPPIN")){
//            consume(LexemeType.LOOP_DELIMITER);
//
//            while(!match(LexemeType.LOOP_DELIMITER)){
//                consume(lexemes.get(currentLexemeIndex).getType());
//            }
//        }
    }
    private Map<String, Integer> getLoopConditionIndex() throws SemanticErrorException{
        System.out.println("===== This is inside getLoopConditionIndex");

        Map<String, Integer> loopConditionIndex = new HashMap<>();

        int currentLexemeIndex = this.currentLexemeIndex, lexemeIndexAfterExpression;

        expression(LexemeType.NULL_VALUE);

        lexemeIndexAfterExpression = this.currentLexemeIndex;

        loopConditionIndex.put("Start", currentLexemeIndex);
        loopConditionIndex.put("End", lexemeIndexAfterExpression);

        return loopConditionIndex;
    }

    private void function() throws SemanticErrorException {

        consume(LexemeType.FUNCTION_DELIMITER);

        String functionIdentifier = lexemes.get(currentLexemeIndex).getLexeme();

        Map<String, Object> tempFunctionDetails = new HashMap<>();

        ArrayList<String> tempVariables = new ArrayList<>();
        consume(LexemeType.FUNCTION_IDENTIFIER);

        while(match(LexemeType.LOOP_OR_FUNCTION_KEYWORD)){
            consume(LexemeType.LOOP_OR_FUNCTION_KEYWORD);

            tempVariables.add(lexemes.get(currentLexemeIndex).getLexeme());
            consume(LexemeType.VARIABLE_IDENTIFIER);

            if(match(LexemeType.OPERAND_SEPARATOR)){
                consume(LexemeType.OPERAND_SEPARATOR);
            }
        }

        tempFunctionDetails.put("startIndex", currentLexemeIndex);

        while(!match(LexemeType.FUNCTION_DELIMITER) && !match(LexemeType.RETURN_KEYWORD) && !match(LexemeType.BREAK_OR_EXIT_OPERATOR)){
            consume(lexemes.get(currentLexemeIndex).getType());
        }

        if(match(LexemeType.RETURN_KEYWORD)){
            consume(LexemeType.RETURN_KEYWORD);

            while(!match(LexemeType.FUNCTION_DELIMITER)){
                consume(lexemes.get(currentLexemeIndex).getType());
            }
        }

        if(match(LexemeType.BREAK_OR_EXIT_OPERATOR)){
            consume(LexemeType.BREAK_OR_EXIT_OPERATOR);
        }

        tempFunctionDetails.put("endIndex", currentLexemeIndex);
        tempFunctionDetails.put("variables", tempVariables);

        consume(LexemeType.FUNCTION_DELIMITER);

        functionList.put(functionIdentifier, tempFunctionDetails);
    }

    private void functionCall() throws SemanticErrorException {
        consume(LexemeType.FUNCTION_CALL_KEYWORD);

        String identifier = lexemes.get(currentLexemeIndex).getLexeme();
        ArrayList<String> parameters = new ArrayList<>();
        int functionCallIndex;
        Map<String, Object> varTableCopy = new HashMap<>();

        Map<String, Object> tempFunctionVarTable = new HashMap<>();

        // Check if identifier is valid
        if(!functionList.containsKey(identifier)){
            throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                    ": there is no function identifier named " + identifier);
        }


        consume(LexemeType.FUNCTION_IDENTIFIER);

        while(match(LexemeType.LOOP_OR_FUNCTION_KEYWORD)){
            consume(LexemeType.LOOP_OR_FUNCTION_KEYWORD);

            try{
                expression(LexemeType.NULL_VALUE);
            }catch (NullPointerException e){
                throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                        ": invalid expression");
            }
            parameters.add(varTable.get("IT").toString());

            if(match(LexemeType.OPERAND_SEPARATOR)){
                consume(LexemeType.OPERAND_SEPARATOR);
            }
        }

        consume(LexemeType.CONDITION_DELIMITER);

        // Function is start here


        // Copy the contents of varTable and clear it
        varTableCopy.putAll(varTable);
        varTable.clear();
        varTable.put("IT", DataType.NOOB);

//        System.out.println("+++====>>>> variables in funcList: " + functionList.get(identifier).get("variables").getClass());
//        functionList.get(identifier).get("variables");

        ArrayList<String> tempVariables = (ArrayList<String>) functionList.get(identifier).get("variables");

        for(int i = 0; i < tempVariables.size(); i++){
            varTable.put(tempVariables.get(i), parameters.get(i));
        }

        functionCallIndex = currentLexemeIndex;

        currentLexemeIndex = Integer.parseInt(functionList.get(identifier).get("startIndex").toString());

        System.out.println("Nowwwww Here: " + lexemes.get(currentLexemeIndex).getLexeme());

        while(!match(LexemeType.FUNCTION_DELIMITER) && !match(LexemeType.BREAK_OR_EXIT_OPERATOR) && !match(LexemeType.RETURN_KEYWORD)){
            System.out.println("TRYYYY");
            expression(LexemeType.FUNCTION_DELIMITER);
        }

        if(match(LexemeType.BREAK_OR_EXIT_OPERATOR)){
            varTable.replace("IT", DataType.NOOB);
            consume(LexemeType.BREAK_OR_EXIT_OPERATOR);
        }

        if(match(LexemeType.RETURN_KEYWORD)){
            consume(LexemeType.RETURN_KEYWORD);

            expression(LexemeType.FUNCTION_DELIMITER);
        }

        String itValue = varTable.get("IT").toString();
        varTable.clear();
        varTable.putAll(varTableCopy);
        varTable.replace("IT", itValue);

        currentLexemeIndex = functionCallIndex;
    }

    private void inputOperation() throws SemanticErrorException {
        hasInput = false;
        consume(LexemeType.INPUT_KEYWORD);

//        GuiController.staticOutputPane.requestFocus();

        Object[] tempParagraphList = GuiController.staticOutputPane.getParagraphs().toArray();
        int tempLength = tempParagraphList.length;

//        GuiController.staticOutputPane.setStyle("-fx-text-fill: green;");
        GuiController.staticOutputPane.appendText("> ");
        GuiController.staticOutputPane.setEditable(true);

        GuiController.staticOutputPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                hasInput = true; // Stop the waiting thread
                GuiController.staticOutputPane.setOnKeyPressed(null);
                event.consume();
            }
        });

        waitUserInput();

        GuiController.staticOutputPane.setEditable(false);
//        GuiController.staticOutputPane.setStyle("");

        String[] paragraphList = new String[tempParagraphList.length];

        for(int i = 0; i<tempParagraphList.length; i++){
            paragraphList[i] = String.valueOf(tempParagraphList[i]);
        }

        varTable.replace(lexemes.get(currentLexemeIndex).getLexeme(), paragraphList[tempLength-1].substring(2));

        consume(LexemeType.VARIABLE_IDENTIFIER);
    }

    private void waitUserInput() {
        while (!hasInput) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void variableDeclaration() throws SemanticErrorException {
        String varName = "";

        consume(LexemeType.VARIABLE_DELIMITER);

        while (!match(LexemeType.VARIABLE_DELIMITER)) {
            consume(LexemeType.VARIABLE_DECLARATION);

            if (match(LexemeType.VARIABLE_IDENTIFIER)) {
                varName = lexemes.get(currentLexemeIndex).getLexeme();
                varTable.put(varName, DataType.NOOB);

                consume(LexemeType.VARIABLE_IDENTIFIER);
            }

            if (match(LexemeType.VARIABLE_ASSIGNMENT)) {
                consume(LexemeType.VARIABLE_ASSIGNMENT);

                // If the initialized value is a string
                if (match(LexemeType.STRING_DELIMITER)) {
                    String temp = "";

                    // Check if the string is empty
                    if (lexemes.get(currentLexemeIndex + 1).getType() != LexemeType.STRING_DELIMITER) {
                        temp = lexemes.get(currentLexemeIndex + 1).getLexeme();
                    }

                    varTable.replace(varName, temp);

                    consumeString();
                } else {
                    // If the initialization is literal, cast it to its type before adding to variable table
                    if (match(LexemeType.LITERAL)) {
                        int floatCheck = lexemes.get(currentLexemeIndex).getLexeme().indexOf(".");

                        if (floatCheck == -1 && (Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "WIN") ||
                                Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "FAIL"))) {
                            varTable.replace(varName, lexemes.get(currentLexemeIndex).getLexeme());
                        } else if (floatCheck == -1) {
                            varTable.replace(varName, Integer.parseInt(lexemes.get(currentLexemeIndex).getLexeme()));
                        } else {
                            varTable.replace(varName, Float.parseFloat(lexemes.get(currentLexemeIndex).getLexeme()));
                        }

                        consume(LexemeType.LITERAL);

                    } else if (match(LexemeType.VARIABLE_IDENTIFIER)) {
                        String tempVar = lexemes.get(currentLexemeIndex).getLexeme();

                        if (varTable.get(varName) == null) {
                            throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber()
                                    + ": " + varName + " is not defined.");
                        } else {
                            varTable.replace(varName, varTable.get(tempVar));
                        }

                        consume(LexemeType.VARIABLE_IDENTIFIER);

                    } else {
                        // Expression
                        // Call the Expression
                        expression(LexemeType.OUTPUT_KEYWORD);

                        // Assume that the expression assign its output to variable IT
                        varTable.replace(varName, varTable.get("IT"));
                    }

                }
            }

            System.out.println(varTable);
        }

        consume(LexemeType.VARIABLE_DELIMITER);
    }

    private void variableAssignmentAndTypeCasting() throws SemanticErrorException {
        String tempVar = lexemes.get(currentLexemeIndex).getLexeme();
        consume(LexemeType.VARIABLE_IDENTIFIER);

        if (!varTable.containsKey(tempVar)) {
            throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                    " : " + tempVar + " is not defined");
        }else{
            varTable.replace("IT", varTable.get(tempVar));
        }

        if (match(LexemeType.REASSIGNMENT_OPERATOR)) {
            consume(LexemeType.REASSIGNMENT_OPERATOR);

            if (match(LexemeType.TYPECASTING_OPERATOR)) {
                consume(LexemeType.TYPECASTING_OPERATOR);

                String tempVar2 = lexemes.get(currentLexemeIndex).getLexeme();
                String tempDataType = lexemes.get(currentLexemeIndex + 1).getLexeme();

                if (!varTable.containsKey(tempVar2)) {
                    throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber()
                            + ": " + tempVar2 + " is not defined.");
                }

                cast(tempVar, tempVar2, tempDataType, false);

                consume(LexemeType.VARIABLE_IDENTIFIER);
                consume(LexemeType.DATA_TYPE_KEYWORD);
            } else {
                expression(LexemeType.NULL_VALUE);
                varTable.replace(tempVar, varTable.get("IT"));
            }
        } else if (match(LexemeType.TYPECASTING_OPERATOR)) {
            consume(LexemeType.TYPECASTING_OPERATOR);

            String tempDataType = lexemes.get(currentLexemeIndex).getLexeme();

            cast(tempVar, "", tempDataType, false);

            consume(LexemeType.DATA_TYPE_KEYWORD);
        }
    }

    private void cast(String tempVar, String tempVar2, String tempDataType, boolean isExplicit) throws SemanticErrorException {

        switch (tempDataType) {
            case "NOOB":
                try {
                    if (!isExplicit) {
                        varTable.replace(tempVar, DataType.NOOB);
                    } else {
                        varTable.replace("IT", DataType.NOOB);
                    }
                } catch (NumberFormatException e) {
                    throw new SemanticErrorException("Semantic error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                            ": " + tempVar2 + " value cannot be casted to type NOOB");
                }
                break;
            case "YARN":
                try {
                    if (Objects.equals(tempVar2, "") && !isExplicit) {
                        varTable.replace(tempVar, varTable.get(tempVar).toString());
                    } else if (!Objects.equals(tempVar2, "") && !isExplicit) {
                        varTable.replace(tempVar, varTable.get(tempVar2).toString());
                    } else if (isExplicit) {
                        if (varTable.containsKey(tempVar)) {
                            varTable.replace("IT", varTable.get(tempVar).toString());
                        } else {
                            varTable.replace("IT", tempVar);
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new SemanticErrorException("Semantic error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                            ": " + tempVar2 + " value cannot be casted to type YARN");
                }
                break;
            case "NUMBAR":
                try {
                    if (Objects.equals(tempVar2, "") && !isExplicit) {
                        varTable.replace(tempVar, Float.parseFloat(varTable.get(tempVar).toString()));
                    } else if (!Objects.equals(tempVar2, "") && !isExplicit) {
                        varTable.replace(tempVar, Float.parseFloat(varTable.get(tempVar2).toString()));
                    } else if (isExplicit) {
                        if (varTable.containsKey(tempVar)) {
                            varTable.replace("IT", Float.parseFloat(varTable.get(tempVar).toString()));
                        } else {
                            varTable.replace("IT", Float.parseFloat(tempVar));
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new SemanticErrorException("Semantic error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                            ": " + tempVar2 + " value cannot be casted to type NUMBAR");
                }
                break;
            case "NUMBR":
                try {
                    if (Objects.equals(tempVar2, "")) {
                        varTable.replace(tempVar, (int) Float.parseFloat(varTable.get(tempVar).toString()));
                    } else if (!Objects.equals(tempVar2, "") && !isExplicit) {
                        varTable.replace(tempVar, (int) Float.parseFloat(varTable.get(tempVar2).toString()));
                    } else if (isExplicit) {
                        if (varTable.containsKey(tempVar)) {
                            varTable.replace("IT", (int) Float.parseFloat(varTable.get(tempVar).toString()));
                        } else {
                            varTable.replace("IT", (int) Float.parseFloat(tempVar));
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new SemanticErrorException("Semantic error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                            ": " + tempVar2 + " value cannot be casted to type NUMBR");
                }

                break;
            case "TROOF":
                try {

                    if (varTable.containsKey(tempVar)) {
                        if (!isExplicit && (Objects.equals(varTable.get(tempVar).toString(), "") ||
                                Objects.equals(varTable.get(tempVar).toString(), "0") ||
                                Objects.equals(varTable.get(tempVar).toString(), "FAIL"))
                        ) {
                            varTable.replace(tempVar, DataType.FAIL);
                        } else if (!isExplicit) {
                            varTable.replace(tempVar, DataType.WIN);
                        } else if ((
                                // TODO: fix bug in varTable.get(tempVar) == "FAIL"
                                Objects.equals(varTable.get(tempVar).toString(), "") ||
                                        Objects.equals(varTable.get(tempVar).toString(), "0") ||
                                        Objects.equals(varTable.get(tempVar).toString(), "FAIL")
                        )) {
                            System.out.println("\n================ NICE TEST ==============");
                            varTable.replace("IT", DataType.FAIL);
                        } else {
                            System.out.println("\n================ TEST ==============");
                            System.out.println(varTable.get(tempVar));
                            varTable.replace("IT", DataType.WIN);
                        }
                    } else { // Ensure that error would not occur
                        if ((
                                Objects.equals(tempVar, "") || Objects.equals(tempVar, "0") ||
                                        Objects.equals(tempVar, "FAIL")
                        )) {
                            varTable.replace("IT", DataType.FAIL);
                        } else {
                            varTable.replace("IT", DataType.WIN);
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new SemanticErrorException("Semantic error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                            ": " + tempVar2 + " value cannot be casted to type TROOF");
                }
        }
    }

    private void typeCasting() throws SemanticErrorException {

        String literalOrVar = "";
        String tempDataType = "";

        consume(LexemeType.TYPECASTING_OPERATOR);

        literalOrVar = lexemes.get(currentLexemeIndex).getLexeme();

        consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);

        if (match(LexemeType.TYPECASTING_OPERATOR)) {   // MAEK x A YARN
            consume(LexemeType.TYPECASTING_OPERATOR);
        }

        tempDataType = lexemes.get(currentLexemeIndex).getLexeme();
        consume(LexemeType.DATA_TYPE_KEYWORD);

        cast(literalOrVar, "", tempDataType, true);
    }

    private void arithmeticOperation() throws SemanticErrorException {
        float a, b;

        String operation = lexemes.get(currentLexemeIndex).getLexeme();

        consume(LexemeType.ARITHMETIC_OPERATION);

        if (match(LexemeType.ARITHMETIC_OPERATION)) {
            arithmeticOperation();
            a = Float.parseFloat(varTable.get("IT").toString());

            consume(LexemeType.OPERAND_SEPARATOR);

            if (match(LexemeType.LITERAL)) {
                if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "WIN")){
                    b = 1;
                } else if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "FAIL")){
                    b = 0;
                } else if (!lexemes.get(currentLexemeIndex).getLexeme().contains(".")) {
                    b = Integer.parseInt(lexemes.get(currentLexemeIndex).getLexeme());
                } else {
                    b = Float.parseFloat(lexemes.get(currentLexemeIndex).getLexeme());
                }

                consume(LexemeType.LITERAL);
            } else if (match(LexemeType.ARITHMETIC_OPERATION)) {
                arithmeticOperation();
                b = Float.parseFloat(varTable.get("IT").toString());
            } else if (match(LexemeType.STRING_DELIMITER)) {
                try{
                    if (!lexemes.get(currentLexemeIndex + 1).getLexeme().contains(".")) {
                        b = Integer.parseInt(lexemes.get(currentLexemeIndex + 1).getLexeme());
                    } else {
                        b = Float.parseFloat(lexemes.get(currentLexemeIndex + 1).getLexeme());
                    }
                }catch (NumberFormatException e){
                    throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                            ": only NUMBR or NUMBAR data type is allowed");
                }

                consumeString();
            } else {
                try {
                    if (!varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString().contains(".")) {
                        b = Integer.parseInt(varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString());
                    } else {
                        b = Float.parseFloat(varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString());
                    }
                }catch (NumberFormatException e){
                    throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                            ": only NUMBR or NUMBAR data type is allowed");
                }

                consume(LexemeType.VARIABLE_IDENTIFIER);
            }
        } else {
            if (match(LexemeType.LITERAL)) {
                if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "WIN")){
                    a = 1;
                } else if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "FAIL")){
                    a = 0;
                } else if (!lexemes.get(currentLexemeIndex).getLexeme().contains(".")) {
                    a = Integer.parseInt(lexemes.get(currentLexemeIndex).getLexeme());
                } else {
                    a = Float.parseFloat(lexemes.get(currentLexemeIndex).getLexeme());
                }

                consume(LexemeType.LITERAL);
            } else if (match(LexemeType.ARITHMETIC_OPERATION)) {
                arithmeticOperation();
                a = Float.parseFloat(varTable.get("IT").toString());
            } else if (match(LexemeType.STRING_DELIMITER)) {
                try{
                    if (!lexemes.get(currentLexemeIndex + 1).getLexeme().contains(".")) {
                        a = Integer.parseInt(lexemes.get(currentLexemeIndex + 1).getLexeme());
                    } else {
                        a = Float.parseFloat(lexemes.get(currentLexemeIndex + 1).getLexeme());
                    }
                }catch (NumberFormatException e){
                    throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                            ": only NUMBR or NUMBAR data type is allowed");
                }
                consumeString();
            } else {

                try {
                    if (!varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString().contains(".")) {
                        a = Integer.parseInt(varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString());
                    } else {
                        a = Float.parseFloat(varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString());
                    }
                }catch (NumberFormatException e){
                    throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                            ": only NUMBR or NUMBAR data type is allowed");
                }

                consume(LexemeType.VARIABLE_IDENTIFIER);
            }

            consume(LexemeType.OPERAND_SEPARATOR);

            if (match(LexemeType.ARITHMETIC_OPERATION)) {
                arithmeticOperation();
                b = Float.parseFloat(varTable.get("IT").toString());
            } else {
                if (match(LexemeType.LITERAL)) {
                    if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "WIN")){
                        b = 1;
                    } else if(Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "FAIL")){
                        b = 0;
                    } else if (!lexemes.get(currentLexemeIndex).getLexeme().contains(".")) {
                        b = Integer.parseInt(lexemes.get(currentLexemeIndex).getLexeme());
                    } else {
                        b = Float.parseFloat(lexemes.get(currentLexemeIndex).getLexeme());
                    }

                    consume(LexemeType.LITERAL);
                } else if (match(LexemeType.STRING_DELIMITER)) {
                    try{
                        if (!lexemes.get(currentLexemeIndex + 1).getLexeme().contains(".")) {
                            b = Integer.parseInt(lexemes.get(currentLexemeIndex + 1).getLexeme());
                        } else {
                            b = Float.parseFloat(lexemes.get(currentLexemeIndex + 1).getLexeme());
                        }
                    }catch (NullPointerException e){
                        throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                                ": only NUMBR or NUMBAR data type is allowed");
                    }

                    consumeString();
                } else {
                    try{
                        if (!varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString().contains(".")) {
                            b = Integer.parseInt(varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString());
                        } else {
                            b = Float.parseFloat(varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString());
                        }
                    }catch (NumberFormatException e){
                        throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                                ": only NUMBR or NUMBAR data type is allowed");
                    }

                    consume(LexemeType.VARIABLE_IDENTIFIER);
                }
            }
        }

        float temp = 0;

        switch (operation) {
            case "SUM OF":
                temp = Arithmetic.sumOf(a, b);

                if(isFloat(temp)){
                    varTable.replace("IT", temp);
                }else{
                    varTable.replace("IT", (int) temp);
                }
                break;
            case "DIFF OF":
                temp = Arithmetic.diffOf(a, b);

                if(isFloat(temp)){
                    varTable.replace("IT", temp);
                }else{
                    varTable.replace("IT", (int) temp);
                }
                break;
            case "PRODUKT OF":
                temp = Arithmetic.produktOf(a, b);

                if(isFloat(temp)){
                    varTable.replace("IT", temp);
                }else{
                    varTable.replace("IT", (int) temp);
                }
                break;
            case "QUOSHUNT OF":
                temp = Arithmetic.quoshuntOf(a, b);

                if(isFloat(temp)){
                    varTable.replace("IT", temp);
                }else{
                    varTable.replace("IT", (int) temp);
                }
                break;
            case "MOD OF":
                temp = Arithmetic.modOf(a, b);

                if(isFloat(temp)){
                    varTable.replace("IT", temp);
                }else{
                    varTable.replace("IT", (int) temp);
                }
                break;
            case "BIGGR OF":
                temp = Arithmetic.biggrOf(a, b);

                if(isFloat(temp)){
                    varTable.replace("IT", temp);
                }else{
                    varTable.replace("IT", (int) temp);
                }
                break;
            case "SMALLR OF":
                temp = Arithmetic.smallrOf(a, b);

                if(isFloat(temp)){
                    varTable.replace("IT", temp);
                }else{
                    varTable.replace("IT", (int) temp);
                }
                break;
        }
    }

    private boolean isFloat(float temp){
        return temp % (int) temp != 0.0;
    }

    private void comparisonOperation() throws SemanticErrorException {
        String comparisonFunction = lexemes.get(currentLexemeIndex).getLexeme();
        float operand1 = 0, operand2 = 0;

        consume(LexemeType.COMPARISON_OPERATION);

        if (match(LexemeType.ARITHMETIC_OPERATION)) {
            arithmeticOperation();
            operand1 = Float.parseFloat(varTable.get("IT").toString());
        } else if (match(LexemeType.VARIABLE_IDENTIFIER) || match(LexemeType.LITERAL)) {
            expression(LexemeType.NULL_VALUE);
            operand1 = Float.parseFloat(varTable.get("IT").toString());
        }  else if (match(LexemeType.STRING_DELIMITER)) {
            throw new SemanticErrorException("Semantic Error at line" + lexemes.get(currentLexemeIndex).getLineNumber()
            + ": only NUMBR or NUMBAR data type is allowed");
        }

        consume(LexemeType.OPERAND_SEPARATOR);

        if (match(LexemeType.ARITHMETIC_OPERATION)) {
            arithmeticOperation();
            operand2 = Float.parseFloat(varTable.get("IT").toString());
        } else if (match(LexemeType.VARIABLE_IDENTIFIER) || match(LexemeType.LITERAL)) {
            expression(LexemeType.NULL_VALUE);
            operand2 = Float.parseFloat(varTable.get("IT").toString());
        } else if (match(LexemeType.STRING_DELIMITER)) {
            throw new SemanticErrorException("Semantic Error at line" + lexemes.get(currentLexemeIndex).getLineNumber()
                    + ": only NUMBR or NUMBAR data type is allowed");
        }

        switch (comparisonFunction){
            case "BOTH SAEM":
                if(operand1 == operand2){
                    varTable.replace("IT", DataType.WIN);
                }else{
                    varTable.replace("IT", DataType.FAIL);
                }
                break;
            case "DIFFRINT":
                if(operand1 != operand2){
                    varTable.replace("IT", DataType.WIN);
                }else{
                    varTable.replace("IT", DataType.FAIL);
                }
                break;
        }
    }

    private void booleanOperation() throws SemanticErrorException {
        // ALL OF and ANY OF
        if (Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ALL OF") ||
                Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ANY OF")) {

            String booleanFunction = lexemes.get(currentLexemeIndex).getLexeme();
            troofList.clear();

            consume(LexemeType.BOOLEAN_OPERATION);

            if (match(LexemeType.BOOLEAN_OPERATION) && !Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ALL OF")
                    && !Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ANY OF")) {
                booleanOperation();

                troofList.add(varTable.get("IT") == DataType.WIN);

                while (match(LexemeType.OPERAND_SEPARATOR)) {

                    consume(LexemeType.OPERAND_SEPARATOR);

                    if (match(LexemeType.BOOLEAN_OPERATION) && !Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ALL OF")
                            && !Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ANY OF")) {
                        booleanOperation();
                        troofList.add(varTable.get("IT") == DataType.WIN);
                    } else {
                        if (match(LexemeType.STRING_DELIMITER)) {
                            if (lexemes.get(currentLexemeIndex + 1).getType() != LexemeType.STRING_DELIMITER) {
                                try {
                                    cast(lexemes.get(currentLexemeIndex + 1).getLexeme(), "", DataType.TROOF.toString(), true);
                                } catch (NullPointerException e) {
                                    throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                                            ": " + lexemes.get(currentLexemeIndex).getLexeme() + " cannot be casted");
                                }
                                troofList.add(varTable.get("IT") == DataType.WIN);
                            }
                            consumeString();
                        } else {
                            try {
                                cast(lexemes.get(currentLexemeIndex).getLexeme(), "", DataType.TROOF.toString(), true);
                            } catch (NullPointerException e) {
                                throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                                        ": " + lexemes.get(currentLexemeIndex).getLexeme() + " cannot be casted");
                            }
                            troofList.add(varTable.get("IT") == DataType.WIN);
                            consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                        }
                    }
                }
            } else {
                if (match(LexemeType.STRING_DELIMITER)) {
                    if (lexemes.get(currentLexemeIndex + 1).getType() != LexemeType.STRING_DELIMITER) {
                        try {
                            cast(lexemes.get(currentLexemeIndex + 1).getLexeme(), "", DataType.TROOF.toString(), true);
                        } catch (NullPointerException e) {
                            throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                                    ": " + lexemes.get(currentLexemeIndex).getLexeme() + " cannot be casted");
                        }
                        troofList.add(varTable.get("IT") == DataType.WIN);
                    }
                    consumeString();
                } else {
                    try {
                        cast(lexemes.get(currentLexemeIndex).getLexeme(), "", DataType.TROOF.toString(), true);
                    } catch (NullPointerException e) {
                        throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                                ": " + lexemes.get(currentLexemeIndex).getLexeme() + " cannot be casted");
                    }
                    troofList.add(varTable.get("IT") == DataType.WIN);
                    consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                }
                while (match(LexemeType.OPERAND_SEPARATOR)) {
                    consume(LexemeType.OPERAND_SEPARATOR);
                    if (match(LexemeType.BOOLEAN_OPERATION) && !Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ALL OF")
                            && !Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "ANY OF")) {
                        booleanOperation();
                        troofList.add(varTable.get("IT") == DataType.WIN);
                    } else {
                        if (match(LexemeType.STRING_DELIMITER)) {
                            if (lexemes.get(currentLexemeIndex + 1).getType() != LexemeType.STRING_DELIMITER) {
                                try {
                                    cast(lexemes.get(currentLexemeIndex + 1).getLexeme(), "", DataType.TROOF.toString(), true);
                                } catch (NullPointerException e) {
                                    throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                                            ": " + lexemes.get(currentLexemeIndex).getLexeme() + " cannot be casted");
                                }
                                troofList.add(varTable.get("IT") == DataType.WIN);
                            }
                            consumeString();
                        } else {
                            try {
                                cast(lexemes.get(currentLexemeIndex).getLexeme(), "", DataType.TROOF.toString(), true);
                            } catch (NullPointerException e) {
                                throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                                        ": " + lexemes.get(currentLexemeIndex).getLexeme() + " cannot be casted");
                            }
                            troofList.add(varTable.get("IT") == DataType.WIN);
                            consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                        }
                    }
                }
            }
            consume(LexemeType.CONDITION_DELIMITER);

            switch (booleanFunction){
                case "ALL OF":
                    varTable.replace("IT", Boolean.allOf(troofList) ? DataType.WIN : DataType.FAIL);
                    break;
                case "ANY OF":
                    varTable.replace("IT", Boolean.anyOf(troofList) ? DataType.WIN : DataType.FAIL);
                    break;
            }
        }

        // NOT
        else if (Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "NOT")) {
            // cast(String tempVar, String tempVar2, String tempDataType, boolean isExplicit)
            consume(LexemeType.BOOLEAN_OPERATION);

            boolean notOperand = false;

            if (match(LexemeType.BOOLEAN_OPERATION)) {
                booleanOperation();
                notOperand = varTable.get("IT") == DataType.WIN;
            } else if (match(LexemeType.STRING_DELIMITER)) {
                if (lexemes.get(currentLexemeIndex + 1).getType() != LexemeType.STRING_DELIMITER) {
                    try {
                        cast(lexemes.get(currentLexemeIndex + 1).getLexeme(), "", DataType.TROOF.toString(), true);
                    } catch (NullPointerException e) {
                        throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                                ": " + lexemes.get(currentLexemeIndex).getLexeme() + " cannot be casted");
                    }
                    notOperand = varTable.get("IT") == DataType.WIN;
                }
                consumeString();
            } else {
//                    String tempVar = lexemes.get(currentLexemeIndex).getLexeme();
                try {
                    cast(lexemes.get(currentLexemeIndex).getLexeme(), "", DataType.TROOF.toString(), true);
                } catch (NullPointerException e) {
                    throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                            ": " + lexemes.get(currentLexemeIndex).getLexeme() + " cannot be casted");
                }
                notOperand = varTable.get("IT") == DataType.WIN;
//                System.out.println(varTable.get("IT"));
                consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
            }


            varTable.replace("IT", Boolean.not(notOperand) ? DataType.WIN : DataType.FAIL);
        }

        // BOTH OF | EITHER OF | WON OF
        else {
            String booleanFunction = lexemes.get(currentLexemeIndex).getLexeme();
            boolean operand1 = false;
            boolean operand2 = false;

            consume(LexemeType.BOOLEAN_OPERATION);

            if (match(LexemeType.BOOLEAN_OPERATION)) {
                booleanOperation();
                operand1 = varTable.get("IT") == DataType.WIN;

                consume(LexemeType.OPERAND_SEPARATOR);

                if (match(LexemeType.STRING_DELIMITER)) {
                    if (lexemes.get(currentLexemeIndex + 1).getType() != LexemeType.STRING_DELIMITER) {
                        try {
                            cast(lexemes.get(currentLexemeIndex + 1).getLexeme(), "", DataType.TROOF.toString(), true);
                        } catch (NullPointerException e) {
                            throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                                    ": " + lexemes.get(currentLexemeIndex).getLexeme() + " cannot be casted");
                        }
                        operand2 = varTable.get("IT") == DataType.WIN;
                    }

                    consumeString();
                } else if (match(LexemeType.BOOLEAN_OPERATION)) {
                    booleanOperation();
                    operand2 = varTable.get("IT") == DataType.WIN;
                } else {
                    try {
                        cast(lexemes.get(currentLexemeIndex).getLexeme(), "", DataType.TROOF.toString(), true);
                    } catch (NullPointerException e) {
                        throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                                ": " + lexemes.get(currentLexemeIndex).getLexeme() + " cannot be casted");
                    }

                    operand2 = varTable.get("IT") == DataType.WIN;
                    consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                }
            } else {
                if (match(LexemeType.STRING_DELIMITER)) {
                    if (lexemes.get(currentLexemeIndex + 1).getType() != LexemeType.STRING_DELIMITER) {
                        try {
                            cast(lexemes.get(currentLexemeIndex + 1).getLexeme(), "", DataType.TROOF.toString(), true);
                        } catch (NullPointerException e) {
                            throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                                    ": " + lexemes.get(currentLexemeIndex).getLexeme() + " cannot be casted");
                        }
                        operand1 = varTable.get("IT") == DataType.WIN;
                    }

                    consumeString();
                } else {
                    try {
                        cast(lexemes.get(currentLexemeIndex).getLexeme(), "", DataType.TROOF.toString(), true);
                    } catch (NullPointerException e) {
                        throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                                ": " + lexemes.get(currentLexemeIndex).getLexeme() + " cannot be casted");
                    }

                    operand1 = varTable.get("IT") == DataType.WIN;
                    consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                }
                consume(LexemeType.OPERAND_SEPARATOR);

                if (match(LexemeType.BOOLEAN_OPERATION)) {
                    booleanOperation();
                    operand2 = varTable.get("IT") == DataType.WIN;
                } else if (match(LexemeType.STRING_DELIMITER)) {
                    if (lexemes.get(currentLexemeIndex + 1).getType() != LexemeType.STRING_DELIMITER) {
                        try {
                            cast(lexemes.get(currentLexemeIndex + 1).getLexeme(), "", DataType.TROOF.toString(), true);
                        } catch (NullPointerException e) {
                            throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                                    ": " + lexemes.get(currentLexemeIndex).getLexeme() + " cannot be casted");
                        }
                        operand2 = varTable.get("IT") == DataType.WIN;
                    }
                    consumeString();
                } else {
                    try {
                        cast(lexemes.get(currentLexemeIndex).getLexeme(), "", DataType.TROOF.toString(), true);
                    } catch (NullPointerException e) {
                        throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber() +
                                ": " + lexemes.get(currentLexemeIndex).getLexeme() + " cannot be casted");
                    }

                    operand2 = varTable.get("IT") == DataType.WIN;
                    consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
                }

            }

            switch (booleanFunction){
                case "BOTH OF":
                    varTable.replace("IT", Boolean.bothOF(operand1, operand2) ? DataType.WIN : DataType.FAIL);
                    break;
                case "EITHER OF":
                    varTable.replace("IT", Boolean.eitherOF(operand1, operand2) ? DataType.WIN : DataType.FAIL);
                    break;
                case "WON OF":
                    varTable.replace("IT", Boolean.wonOF(operand1, operand2) ? DataType.WIN : DataType.FAIL);
                    break;
            }
        }
    }

    private void outputStatement() throws SemanticErrorException {
        consume(LexemeType.OUTPUT_KEYWORD);

        if (match(LexemeType.STRING_DELIMITER)) {
            if (lexemes.get(currentLexemeIndex + 1).getType() != LexemeType.STRING_DELIMITER) {
                tempOutputString += lexemes.get(currentLexemeIndex + 1).getLexeme();
            }

            consumeString();
        } else {
            expression(LexemeType.OUTPUT_KEYWORD);
            tempOutputString += varTable.get("IT").toString();
        }

        while (match(LexemeType.PRINTING_SEPARATOR)) {
            consume(LexemeType.PRINTING_SEPARATOR);

            if (match(LexemeType.STRING_DELIMITER)) {
                if (lexemes.get(currentLexemeIndex + 1).getType() != LexemeType.STRING_DELIMITER) {
                    tempOutputString += lexemes.get(currentLexemeIndex + 1).getLexeme();
                }

                consumeString();
            } else {
                expression(LexemeType.OUTPUT_KEYWORD);
                tempOutputString += varTable.get("IT").toString();
            }
        }

        tempOutputString += "\n";
    }

    private void consumeString() throws SemanticErrorException {
        consume(LexemeType.STRING_DELIMITER);

        // Checks if the string is empty
        if (match(LexemeType.LITERAL)) {
            consume(LexemeType.LITERAL);
        }

        consume(LexemeType.STRING_DELIMITER);
    }

    private void concatenationOperation() throws SemanticErrorException {
        consume(LexemeType.CONCATENATION_KEYWORD);

        if (match(LexemeType.STRING_DELIMITER)) {
            if (lexemes.get(currentLexemeIndex + 1).getType() != LexemeType.STRING_DELIMITER) {
                tempConcatString += lexemes.get(currentLexemeIndex + 1).getLexeme();
            }

            consumeString();
        } else {
                expression(LexemeType.CONCATENATION_KEYWORD);
                tempConcatString += varTable.get("IT").toString();
        }

        while (match(LexemeType.OPERAND_SEPARATOR)) {
            consume(LexemeType.OPERAND_SEPARATOR);

            if (match(LexemeType.STRING_DELIMITER)) {
                if (lexemes.get(currentLexemeIndex + 1).getType() != LexemeType.STRING_DELIMITER) {
                    tempConcatString += lexemes.get(currentLexemeIndex + 1).getLexeme();
                }

                consumeString();
            } else {
//                if(match(LexemeType.ARITHMETIC_OPERATION)){
//                    arithmeticOperation();
//                }else{
                    expression(LexemeType.CONCATENATION_KEYWORD);
                    tempConcatString += varTable.get("IT").toString();
//                }
            }
        }
    }

    private boolean match(LexemeType expectedType) {
        if (currentLexemeIndex < lexemes.size()) {
            Lexeme currentLexeme = lexemes.get(currentLexemeIndex);
            return currentLexeme.getType() == expectedType;
        }
        return false;
    }

//    private boolean loopExpressionMatch(LexemeType expectedType) {
//        if (currentLoopIndex < loopCondition.size()) {
//            Lexeme currentLexeme = loopCondition.get(currentLexemeIndex);
//            return currentLexeme.getType() == expectedType;
//        }
//        return false;
//    }

    private void consume(LexemeType expectedType) throws SemanticErrorException {
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
                    (currentLexemeIndex < lexemes.size() ? lexemes.get(currentLexemeIndex).getStringType() : "end of input") + ".";

//            GuiController.staticOutputPane.appendText(errorMessage + "\n");

            throw new SemanticErrorException(errorMessage);
        }
    }

    private void consume(LexemeType expectedType1, LexemeType expectedType2) throws SemanticErrorException {
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

            String errorMessage = "Syntax Error in Line " + lexemes.get(currentLexemeIndex).getLineNumber() + ": " +
                    "Expected " + expectedType1.toString().toLowerCase().replace("_", " ") +
                    " or " + expectedType2.toString().toLowerCase().replace("_", " ") +
                    ", but found " +
                    (currentLexemeIndex < lexemes.size() ? lexemes.get(currentLexemeIndex).getStringType() : "end of input") + ".";

            GuiController.staticOutputPane.appendText(errorMessage + "\n");

            throw new SemanticErrorException(errorMessage);
        }
    }

//    private void loopConsume(LexemeType expectedType) throws SemanticErrorException {
//        if (loopExpressionMatch(expectedType)) {
//            System.out.println("Match " + expectedType + " (expected) and " + loopCondition.get(currentLoopIndex).getType().toString()
//                    + " Lexeme: " + loopCondition.get(currentLoopIndex).getLexeme() + " line number: " + loopCondition.get(currentLoopIndex).getLineNumber());
//            currentLoopIndex++;
//        } else {
//            // Handle syntax error
//            System.out.println("Syntax error: Expected " + expectedType + ", but found " +
//                    (currentLoopIndex < loopCondition.size() ? loopCondition.get(currentLoopIndex).getType().toString()
//                            + " Lexeme test: " + loopCondition.get(currentLoopIndex).getLexeme()
//                            + " line number: " + loopCondition.get(currentLoopIndex).getLineNumber() : "end of input"));
//
//            String errorMessage = "Syntax Error in Line " + loopCondition.get(currentLoopIndex).getLineNumber() + ": " +
//                    "Expected " + expectedType.toString().toLowerCase().replace("_", " ") + ", but found " +
//                    (currentLoopIndex < loopCondition.size() ? loopCondition.get(currentLoopIndex).getStringType() : "end of input") + ".";
//
////            GuiController.staticOutputPane.appendText(errorMessage + "\n");
//
//            throw new SemanticErrorException(errorMessage);
//        }
//    }

    private static class SemanticErrorException extends Exception {
        public SemanticErrorException(String message) {
            super(message);
            GuiController.staticOutputPane.clear();
            GuiController.staticOutputPane.setStyle("-fx-text-fill: red;");
            GuiController.staticOutputPane.appendText(message + "\n");
            GuiController.staticOutputPane.setEditable(false);
        }
    }
}

