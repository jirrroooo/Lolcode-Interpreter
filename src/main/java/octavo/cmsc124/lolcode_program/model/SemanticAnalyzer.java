package octavo.cmsc124.lolcode_program.model;

import octavo.cmsc124.lolcode_program.controller.GuiController;

import java.util.*;

public class SemanticAnalyzer extends Thread {

    private List<Lexeme> lexemes;
    private int currentLexemeIndex;
    public ArrayList<Variable> variableList;
    private Map<String, Object> varTable;
    private String tempOutputString = "";
    private ArrayList<java.lang.Boolean> troofList = new ArrayList<java.lang.Boolean>();

    public SemanticAnalyzer(List<Lexeme> lexemes) {
        this.lexemes = lexemes;
        this.currentLexemeIndex = 0;
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
                    varTable.replace("IT", tempOutputString);
                } else if (match(LexemeType.ARITHMETIC_OPERATION)) {
                    varTable.replace("IT", arithmeticOperation());
                } else if (match(LexemeType.BOOLEAN_OPERATION)) {
                    booleanOperation();
                } else if (match(LexemeType.COMPARISON_OPERATION)) { // BOTH SAEM & DIFFRINT
                    comparisonOperation();
                } else if (match(LexemeType.CONCATENATION_KEYWORD)) { // SMOOSH
                    tempOutputString = "";
                    concatenationOperation();
                    varTable.replace("IT", tempOutputString);
                } else if (match(LexemeType.VARIABLE_IDENTIFIER)) { // REASSIGNMENT: use of R
                    variableAssignmentAndTypeCasting();
                } else if (match(LexemeType.TYPECASTING_OPERATOR)) { // REASSIGNMENT: use of R
                    typeCasting();
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
        } else if (match(LexemeType.ARITHMETIC_OPERATION)) {
            varTable.replace("IT", arithmeticOperation());
        } else if (match(LexemeType.BOOLEAN_OPERATION)) {
            booleanOperation();
        } else if (match(LexemeType.COMPARISON_OPERATION)) {
            comparisonOperation();
        } else if (match(LexemeType.CONCATENATION_KEYWORD)) {
            concatenationOperation();
        } else if (match(LexemeType.TYPECASTING_OPERATOR)) {
            typeCasting();
        } else if (match(LexemeType.OUTPUT_KEYWORD)) {
            outputStatement();
        } else if (match(LexemeType.LITERAL)) {
            int floatCheck = lexemes.get(currentLexemeIndex).getLexeme().indexOf(".");

            if (floatCheck == -1) {
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
            }
        }
    }


    private void ifElseControlFlow() throws SemanticErrorException {
        consume(LexemeType.FLOW_CONTROL_DELIMITER);

        if (match(LexemeType.FLOW_CONTROL_KEYWORD)) {
            while (match(LexemeType.FLOW_CONTROL_KEYWORD)) {
                consume(LexemeType.FLOW_CONTROL_KEYWORD);

                // Handles cases when expression is more than 1
                while (!match(LexemeType.FLOW_CONTROL_KEYWORD) && !match(LexemeType.FLOW_CONTROL_DELIMITER)) {
                    expression(LexemeType.NULL_VALUE);
                }
            }
        }

        consume(LexemeType.FLOW_CONTROL_DELIMITER);
    }

    private void switchCaseControlFlow() throws SemanticErrorException {
        consume(LexemeType.FLOW_CONTROL_DELIMITER);

        while (match(LexemeType.FLOW_CONTROL_KEYWORD) && !Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "OMGWTF")) {
            consume(LexemeType.FLOW_CONTROL_KEYWORD);

            if (match(LexemeType.STRING_DELIMITER)) {
                consumeString();
            } else {
                consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
            }

            // Handles cases when expression is more than 1
            while (!match(LexemeType.FLOW_CONTROL_KEYWORD) && !match(LexemeType.FLOW_CONTROL_DELIMITER) &&
                    !match(LexemeType.BREAK_OR_EXIT_OPERATOR)) {
                expression(LexemeType.NULL_VALUE);
            }

            // If Break Keyword is Encountered
            if (match(LexemeType.BREAK_OR_EXIT_OPERATOR)) {
                consume(LexemeType.BREAK_OR_EXIT_OPERATOR);
            }
        }

        // If a default condition is encountered
        if (match(LexemeType.FLOW_CONTROL_KEYWORD) && Objects.equals(lexemes.get(currentLexemeIndex).getLexeme(), "OMGWTF")) {
            consume(LexemeType.FLOW_CONTROL_KEYWORD);

            // Handles cases when expression is more than 1
            while (!match(LexemeType.FLOW_CONTROL_KEYWORD) && !match(LexemeType.FLOW_CONTROL_DELIMITER)) {
                expression(LexemeType.NULL_VALUE);
            }
        }

        consume(LexemeType.FLOW_CONTROL_DELIMITER);
    }

    private void loop() throws SemanticErrorException {
        consume(LexemeType.LOOP_DELIMITER);
        consume(LexemeType.LOOP_IDENTIFIER);
        consume(LexemeType.LOOP_OPERATION);
        consume(LexemeType.LOOP_OR_FUNCTION_KEYWORD);
        consume(LexemeType.VARIABLE_IDENTIFIER);
        consume(LexemeType.LOOP_CONDITION);

        while (!match(LexemeType.LOOP_DELIMITER) && !match(LexemeType.BREAK_OR_EXIT_OPERATOR)) {
            // Exclude loop condition - Loop of Loop is not allowed yet
            expression(LexemeType.LOOP_DELIMITER);
        }

        if (match(LexemeType.BREAK_OR_EXIT_OPERATOR)) {
            consume(LexemeType.BREAK_OR_EXIT_OPERATOR);
        }

        consume(LexemeType.LOOP_DELIMITER);
        consume(LexemeType.LOOP_IDENTIFIER);
    }

    private void function() throws SemanticErrorException {
        consume(LexemeType.FUNCTION_DELIMITER);
        consume(LexemeType.FUNCTION_IDENTIFIER);

        while (match(LexemeType.LOOP_OR_FUNCTION_KEYWORD)) {
            consume(LexemeType.LOOP_OR_FUNCTION_KEYWORD);
            consume(LexemeType.VARIABLE_IDENTIFIER);

            if (match(LexemeType.OPERAND_SEPARATOR)) {
                consume(LexemeType.OPERAND_SEPARATOR);
            }
        }

        while (!match(LexemeType.FUNCTION_DELIMITER) && !match(LexemeType.RETURN_KEYWORD)) {
            expression(LexemeType.FUNCTION_DELIMITER);
        }

        if (match(LexemeType.RETURN_KEYWORD)) {
            consume(LexemeType.RETURN_KEYWORD);

            // Function inside a Function is not allowed
            expression(LexemeType.FUNCTION_DELIMITER);
        }

        consume(LexemeType.FUNCTION_DELIMITER);
    }

    private void functionCall() throws SemanticErrorException {
        consume(LexemeType.FUNCTION_CALL_KEYWORD);
        consume(LexemeType.FUNCTION_IDENTIFIER);

        while (match(LexemeType.LOOP_OR_FUNCTION_KEYWORD)) {
            consume(LexemeType.LOOP_OR_FUNCTION_KEYWORD);

            if (match(LexemeType.STRING_DELIMITER)) {
                consumeString();
            } else {
                consume(LexemeType.VARIABLE_IDENTIFIER, LexemeType.LITERAL);
            }


            if (match(LexemeType.OPERAND_SEPARATOR)) {
                consume(LexemeType.OPERAND_SEPARATOR);
            }
        }

        consume(LexemeType.CONDITION_DELIMITER);
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

                        if (floatCheck == -1) {
                            varTable.replace(varName, Integer.parseInt(lexemes.get(currentLexemeIndex).getLexeme()));
                        } else {
                            varTable.replace(varName, Float.parseFloat(lexemes.get(currentLexemeIndex).getLexeme()));
                        }
                    } else if (match(LexemeType.VARIABLE_IDENTIFIER)) {
                        String tempVar = lexemes.get(currentLexemeIndex).getLexeme();

                        if (varTable.get(varName) == null) {
                            throw new SemanticErrorException("Semantic Error at line " + lexemes.get(currentLexemeIndex).getLineNumber()
                                    + ": " + varName + " is not defined.");
                        } else {
                            varTable.replace(varName, varTable.get(tempVar));
                        }
                    } else {
                        // Expression
                        // Call the Expression
                        expression(LexemeType.OUTPUT_KEYWORD);

                        // Assume that the expression assign its output to variable IT
                        varTable.replace(varName, varTable.get("IT"));
                    }

                    consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
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
                                Objects.equals(varTable.get(tempVar).toString(), "") ||
                                        Objects.equals(varTable.get(tempVar).toString(), "0")
                        )) {
                            varTable.replace("IT", DataType.FAIL);
                        } else {
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

    private float arithmeticOperation() throws SemanticErrorException {
        float a, b;

        String operation = lexemes.get(currentLexemeIndex).getLexeme();

        consume(LexemeType.ARITHMETIC_OPERATION);

        if (match(LexemeType.ARITHMETIC_OPERATION)) {
            a = arithmeticOperation();

            consume(LexemeType.OPERAND_SEPARATOR);

            if (match(LexemeType.LITERAL)) {
                if (!lexemes.get(currentLexemeIndex).getLexeme().contains(".")) {
                    b = Integer.parseInt(lexemes.get(currentLexemeIndex).getLexeme());
                } else {
                    b = Float.parseFloat(lexemes.get(currentLexemeIndex).getLexeme());
                }

                consume(LexemeType.LITERAL);
            } else if (match(LexemeType.ARITHMETIC_OPERATION)) {
                b = arithmeticOperation();
            } else {
                if (!varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString().contains(".")) {
                    b = Integer.parseInt(varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString());
                } else {
                    b = Float.parseFloat(varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString());
                }

                consume(LexemeType.VARIABLE_IDENTIFIER);
            }
        } else {
            if (match(LexemeType.LITERAL)) {
                if (!lexemes.get(currentLexemeIndex).getLexeme().contains(".")) {
                    a = Integer.parseInt(lexemes.get(currentLexemeIndex).getLexeme());
                } else {
                    a = Float.parseFloat(lexemes.get(currentLexemeIndex).getLexeme());
                }

                consume(LexemeType.LITERAL);
            } else if (match(LexemeType.ARITHMETIC_OPERATION)) {
                a = arithmeticOperation();
            } else {
                if (!varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString().contains(".")) {
                    a = Integer.parseInt(varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString());
                } else {
                    a = Float.parseFloat(varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString());
                }

                consume(LexemeType.VARIABLE_IDENTIFIER);
            }

            consume(LexemeType.OPERAND_SEPARATOR);

            if (match(LexemeType.ARITHMETIC_OPERATION)) {
                b = arithmeticOperation();
            } else {
                if (match(LexemeType.LITERAL)) {
                    if (!lexemes.get(currentLexemeIndex).getLexeme().contains(".")) {
                        b = Integer.parseInt(lexemes.get(currentLexemeIndex).getLexeme());
                    } else {
                        b = Float.parseFloat(lexemes.get(currentLexemeIndex).getLexeme());
                    }

                    consume(LexemeType.LITERAL);
                } else {
                    if (!varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString().contains(".")) {
                        b = Integer.parseInt(varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString());
                    } else {
                        b = Float.parseFloat(varTable.get(lexemes.get(currentLexemeIndex).getLexeme()).toString());
                    }

                    consume(LexemeType.VARIABLE_IDENTIFIER);
                }
            }
        }

        switch (operation) {
            case "SUM OF":
                return Arithmetic.sumOf(a, b);
            case "DIFF OF":
                return Arithmetic.diffOf(a, b);

            case "PRODUKT OF":
                return Arithmetic.produktOf(a, b);
            case "QUOSHUNT OF":
                return Arithmetic.quoshuntOf(a, b);
            case "MOD OF":
                return Arithmetic.modOf(a, b);
            case "BIGGR OF":
                return Arithmetic.biggrOf(a, b);
            case "SMALLR OF":
                return Arithmetic.smallrOf(a, b);
        }


//        if (match(LexemeType.ARITHMETIC_OPERATION)) {
//            arithmeticOperation();
//            consume(LexemeType.OPERAND_SEPARATOR);
//            consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
//        } else {
//            consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
//            consume(LexemeType.OPERAND_SEPARATOR);
//            if (match(LexemeType.ARITHMETIC_OPERATION)) {
//                arithmeticOperation();
//            } else {
//                consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
//            }
//        }

        return 0;
    }

    private void comparisonOperation() throws SemanticErrorException {
        consume(LexemeType.COMPARISON_OPERATION);

        consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
        consume(LexemeType.OPERAND_SEPARATOR);

        if (match(LexemeType.ARITHMETIC_OPERATION)) {
            consume(LexemeType.ARITHMETIC_OPERATION);
            consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
            consume(LexemeType.OPERAND_SEPARATOR);
            consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
        } else if (!match(LexemeType.ARITHMETIC_OPERATION)) {
            consume(LexemeType.LITERAL, LexemeType.VARIABLE_IDENTIFIER);
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
                tempOutputString += lexemes.get(currentLexemeIndex + 1).getLexeme();
            }

            consumeString();
        } else {
            expression(LexemeType.CONCATENATION_KEYWORD);
            tempOutputString += varTable.get("IT").toString();
        }

        while (match(LexemeType.OPERAND_SEPARATOR)) {
            consume(LexemeType.OPERAND_SEPARATOR);

            if (match(LexemeType.STRING_DELIMITER)) {
                if (lexemes.get(currentLexemeIndex + 1).getType() != LexemeType.STRING_DELIMITER) {
                    tempOutputString += lexemes.get(currentLexemeIndex + 1).getLexeme();
                }

                consumeString();
            } else {
                expression(LexemeType.CONCATENATION_KEYWORD);
                tempOutputString += varTable.get("IT").toString();
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

//    private void handleUnexpectedLexeme() throws SemanticErrorException {
//        Lexeme currentLexeme = lexemes.get(currentLexemeIndex);
//        String errorMessage = "Syntax Error at line " + currentLexeme.getLineNumber() + ": " +
//                "Unexpected " + currentLexeme.getTypeStr();
//
//        GuiController.staticOutputPane.appendText(errorMessage + "\n");
//
//        throw new SemanticErrorException(errorMessage);
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

    private static class SemanticErrorException extends Exception {
        public SemanticErrorException(String message) {
            super(message);
            GuiController.staticOutputPane.clear();
            GuiController.staticOutputPane.appendText(message + "\n");
        }
    }
}

