package octavo.cmsc124.lolcode_program.model;

public enum LexemeType {
    CODE_DELIMITER,

    VARIABLE_DELIMITER,
    VARIABLE_DECLARATION,
    VARIABLE_ASSIGNMENT,
    VARIABLE_IDENTIFIER,

    LITERAL,
    STRING_DELIMITER,
    OUTPUT_KEYWORD,

    FUNCTION_DELIMITER,
    FUNCTION_IDENTIFIER,
    FUNCTION_PARAMETER_KEYWORD,
    FUNCTION_PARAMETER,
    FUNCTION_CALL_KEYWORD,

    RETURN_KEYWORD,

    LOOP_DELIMITER,
    LOOP_IDENTIFIER,
    LOOP_OPERATION,
    LOOP_CONDITION,

    ARITHMETIC_OPERATION,
    BOOLEAN_OPERATION,
    COMPARISON_OPERATION,

    OPERAND_SEPARATOR,
    PRINTING_SEPARATOR,
    CONDITION_DELIMITER,

    INPUT_KEYWORD,
    CONCATENATION_KEYWORD,
    TYPECASTING_OPERATOR,
    REASSIGNMENT_OPERATOR,

    FLOW_CONTROL_DELIMITER,
    FLOW_CONTROL_KEYWORD,

    BREAK_OR_EXIT_OPERATOR,

    UNKNOWN
}
