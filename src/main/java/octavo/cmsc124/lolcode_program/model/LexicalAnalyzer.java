package octavo.cmsc124.lolcode_program.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LexicalAnalyzer {

    private List<Lexeme> lexemes;

    public LexicalAnalyzer() {
        this.lexemes = new ArrayList<>();
    }

    public List<Lexeme> analyzeCode(List<Map.Entry<Integer,String>> lines) {

        lines.forEach( (line) -> {
            List<List<String>> lexLine = analyzeLine(line.getValue());
            lexLine.forEach((listOfLexemes) -> {
                lexemes.add(new Lexeme(listOfLexemes.get(0), LexemeType.valueOf(listOfLexemes.get(1)), line.getKey()));
            });
        });

        return lexemes;
    }

    private List<List<String>> analyzeLine(String line) {

        List<List<String>> lineTokens = new ArrayList<>();

        // Define regex patterns for keywords, identifiers, numbers, and operators

        String codeDelimiter = "(HAI|KTHXBYE)";

        String variableDelimiter = "\\bWAZZUP|BUHBYE\\b";
        String variableDeclaration = "\\bI HAS A\\b";
        String variableAssignment = "ITZ";
        String variableIdentifier = "[a-z|A-Z][a-z|A-Z|0-9|_]*";

        String numbrLiteral = "(?:\\-)?\\d+";
        String numbarLiteral = "(?:\\-)?\\d+\\.\\d+";
        String yarnLiteral = "\\\"([^\"]*)\\\"";
        String troofLiteral = "\\bWIN|FAIL\\b";
        String literal = "(" + numbarLiteral + "|" + numbrLiteral + "|" + yarnLiteral + "|" + troofLiteral + ")";
        String outputKeyword = "VISIBLE";
        String dataTypeKeyword = "\\bNOOB|TROOF|NUMBAR|NUMBR|YARN|WIN|FAIL\\b";

        String functionDelimiter = "\\bIF U SAY SO\\b";
        String functionIdentifier = "HOW IZ I [a-z|A-Z][a-z|A-Z|0-9|_]*";
        String functionCallKeyword = "\\bI IZ [a-z|A-Z][a-z|A-Z|0-9|_]*\\b";
        String functionParameter = "YR [a-z|A-Z][a-z|A-Z|0-9|_]*|YR (?:\\-)?\\d+[^.]|YR (?:\\-)?\\d+\\.\\d+|YR \\\"(.*)\\\"|YR (WIN|FAIL)";

        String returnKeyword = "\\bFOUND YR\\b";

        String loopDelimiter = "\\bIM OUTTA YR [a-z|A-Z][a-z|A-Z|0-9|_]*\\b";
        String loopIdentifier = "\\bIM IN YR [a-z|A-Z][a-z|A-Z|0-9|_]*\\b";
        String loopOperation = "\\bUPPIN|NERFIN\\b";
        String loopCondition = "\\bTIL|WILE\\b";
        String loopKeyword = "\\bYR\\b";

        String arithmeticOperation = "\\bSUM OF|DIFF OF|PRODUKT OF|QUOSHUNT OF|MOD OF|BIGGR OF|SMALLR OF\\b";
        String booleanOperation = "\\bBOTH OF|EITHER OF|WON OF|NOT|ALL OF|ANY OF\\b";
        String comparisonOperation = "\\bBOTH SAEM|DIFFRINT\\b";

        String operandSeparator = "\\bAN\\b";
        String printingSeparator = "\\+";
        String conditionDelimiter = "\\bMKAY\\b";

        String inputKeyword = "\\bGIMMEH\\b";
        String concatenationKeyword = "\\bSMOOSH\\b";
        String typecastOperator = "\\bMAEK|IS NOW A|A\\b";
        String reassignmentKeyword = "\\bR\\b";

        String flowControlDelimiter = "\\bO RLY\\?|WTF\\?|OIC\\b";
        String flowControlKeyword = "\\bYA RLY|NO WAI|OMG|OMGWTF|MEBBE\\b";

        String breakOrExitOperator = "\\bGTFO\\b";

        String inlineComment = "\\bBTW(.*)\\b";
        String blockComment = "OBTW[\\s\\S]*?TLDR";

        // Combine patterns into a single pattern
        String combinedPattern = "(" + codeDelimiter + "|" + loopKeyword + "|" + outputKeyword + "|" +  literal
                + "|" + functionIdentifier + "|" + functionParameter + "|" + loopIdentifier + "|" + functionCallKeyword
                + "|" + functionDelimiter + "|" + variableAssignment + "|" + variableDelimiter + "|" + variableDeclaration
                + "|" + returnKeyword + "|" + loopCondition
                + "|" + arithmeticOperation + "|" + booleanOperation + "|" + printingSeparator + "|" + conditionDelimiter
                + "|" + inputKeyword + "|" + concatenationKeyword + "|" + typecastOperator + "|" + flowControlDelimiter
                + "|" + flowControlKeyword + "|" + breakOrExitOperator + "|" + dataTypeKeyword
                + "|" + loopDelimiter + "|"  + operandSeparator
                + "|" + comparisonOperation + "|"  + reassignmentKeyword
                + "|" + inlineComment + "|" + blockComment
                + "|" + variableIdentifier + ")";

        // Create a Pattern object
        Pattern pattern = Pattern.compile(combinedPattern);

        // Create a Matcher object
        Matcher matcher = pattern.matcher(line);

        // Process matches
        while (matcher.find()) {
            String token = matcher.group();

            if(token.matches(inlineComment)){
                lineTokens.add(new ArrayList<>(List.of("BTW", LexemeType.COMMENT_KEYWORD.toString())));
                if(token.strip().length() > 3){
                    lineTokens.add(new ArrayList<>(List.of(token.substring(4), LexemeType.INLINE_COMMENT.toString())));
                }
            }

            else if(token.matches(blockComment)){
                lineTokens.add(new ArrayList<>(List.of("OBTW", LexemeType.COMMENT_KEYWORD.toString())));
                if(token.strip().length() > 4){
                    lineTokens.add(new ArrayList<>(List.of(token.substring(5, token.length()-4), LexemeType.BLOCK_COMMENT.toString())));
                }
                lineTokens.add(new ArrayList<>(List.of("TLDR", LexemeType.COMMENT_KEYWORD.toString())));
            }

            else if (token.matches(codeDelimiter)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.CODE_DELIMITER.toString())));
            }

            else if (token.matches(variableDelimiter)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.VARIABLE_DELIMITER.toString())));
            }
            else if (token.matches(variableDeclaration)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.VARIABLE_DECLARATION.toString())));
            }
            else if (token.matches(variableAssignment)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.VARIABLE_ASSIGNMENT.toString())));
            }


            else if (token.matches(outputKeyword)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.OUTPUT_KEYWORD.toString())));
            }


            else if (token.matches(loopDelimiter)) {
                lineTokens.add(new ArrayList<>(List.of(token.substring(0,11), LexemeType.LOOP_DELIMITER.toString())));
                lineTokens.add(new ArrayList<>(List.of(token.substring(12), LexemeType.LOOP_IDENTIFIER.toString())));
            }
            else if (token.matches(loopIdentifier)) {
                lineTokens.add(new ArrayList<>(List.of(token.substring(0,8), LexemeType.LOOP_DELIMITER.toString())));
                lineTokens.add(new ArrayList<>(List.of(token.substring(9), LexemeType.LOOP_IDENTIFIER.toString())));
            }
            else if (token.matches(loopOperation)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.LOOP_OPERATION.toString())));
            }
            else if (token.matches(loopCondition)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.LOOP_CONDITION.toString())));
            }
            else if (token.matches(loopKeyword)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.LOOP_OR_FUNCTION_KEYWORD.toString())));
            }


            else if (token.matches(functionDelimiter)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.FUNCTION_DELIMITER.toString())));
            }
            else if (token.matches(functionIdentifier)) {
                lineTokens.add(new ArrayList<>(List.of(token.substring(0, 8), LexemeType.FUNCTION_DELIMITER.toString())));
                lineTokens.add(new ArrayList<>(List.of(token.substring(9), LexemeType.FUNCTION_IDENTIFIER.toString())));
            }
            else if (token.matches(functionCallKeyword)) {
                lineTokens.add(new ArrayList<>(List.of(token.substring(0, 4), LexemeType.FUNCTION_CALL_KEYWORD.toString())));
                lineTokens.add(new ArrayList<>(List.of(token.substring(5), LexemeType.FUNCTION_IDENTIFIER.toString())));
            }
            else if (token.matches(functionParameter)) {
                lineTokens.add(new ArrayList<>(List.of(token.substring(0, 2), LexemeType.LOOP_OR_FUNCTION_KEYWORD.toString())));
                lineTokens.add(new ArrayList<>(List.of(token.substring(3), LexemeType.VARIABLE_IDENTIFIER.toString())));
            }



            else if (token.matches(returnKeyword)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.RETURN_KEYWORD.toString())));
            }
            else if (token.matches(dataTypeKeyword)){
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.DATA_TYPE_KEYWORD.toString())));
            }



            else if (token.matches(arithmeticOperation)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.ARITHMETIC_OPERATION.toString())));
            }
            else if (token.matches(booleanOperation)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.BOOLEAN_OPERATION.toString())));
            }
            else if (token.matches(comparisonOperation)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.COMPARISON_OPERATION.toString())));
            }


            else if (token.matches(operandSeparator)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.OPERAND_SEPARATOR.toString())));
            }
            else if (token.matches(printingSeparator)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.PRINTING_SEPARATOR.toString())));
            }
            else if (token.matches(conditionDelimiter)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.CONDITION_DELIMITER.toString())));
            }


            else if (token.matches(inputKeyword)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.INPUT_KEYWORD.toString())));
            }
            else if (token.matches(concatenationKeyword)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.CONCATENATION_KEYWORD.toString())));
            }
            else if (token.matches(typecastOperator)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.TYPECASTING_OPERATOR.toString())));
            }
            else if (token.matches(reassignmentKeyword)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.REASSIGNMENT_OPERATOR.toString())));
            }


            else if (token.matches(flowControlDelimiter)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.FLOW_CONTROL_DELIMITER.toString())));
            }
            else if (token.matches(flowControlKeyword)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.FLOW_CONTROL_KEYWORD.toString())));
            }


            else if (token.matches(breakOrExitOperator)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.BREAK_OR_EXIT_OPERATOR.toString())));
            }


            else if (token.matches(variableIdentifier)) {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.VARIABLE_IDENTIFIER.toString())));
            }

            else if (token.matches(literal)) {

                if(token.matches(yarnLiteral)){
                    lineTokens.add(new ArrayList<>(List.of("\"", LexemeType.STRING_DELIMITER.toString())));
                    if(!token.substring(1, token.length() - 1).equals("")){
                        lineTokens.add(new ArrayList<>(List.of(token.substring(1, token.length() - 1), LexemeType.LITERAL.toString())));
                    }
                    lineTokens.add(new ArrayList<>(List.of("\"", LexemeType.STRING_DELIMITER.toString())));
                }else{
                    lineTokens.add(new ArrayList<>(List.of(token, LexemeType.LITERAL.toString())));
                }
            }

            else {
                lineTokens.add(new ArrayList<>(List.of(token, LexemeType.UNKNOWN.toString())));
            }
        }

        return lineTokens;
    }
}

