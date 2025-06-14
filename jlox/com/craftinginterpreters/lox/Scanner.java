package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();

  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("and", TokenType.AND);
    keywords.put("class", TokenType.CLASS);
    keywords.put("else", TokenType.ELSE);
    keywords.put("false", TokenType.FALSE);
    keywords.put("for", TokenType.FOR);
    keywords.put("fun", TokenType.FUN);
    keywords.put("if", TokenType.IF);
    keywords.put("nil", TokenType.NIL);
    keywords.put("or", TokenType.OR);
    keywords.put("print", TokenType.PRINT);
    keywords.put("return", TokenType.RETURN);
    keywords.put("super", TokenType.SUPER);
    keywords.put("this", TokenType.THIS);
    keywords.put("true", TokenType.TRUE);
    keywords.put("var", TokenType.VAR);
    keywords.put("while", TokenType.WHILE);
  }

  private int start = 0;
  private int current = 0;
  private int line = 1;

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }

    tokens.add(new Token(TokenType.EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      // single character lexemes
      case '(' -> addToken(TokenType.LEFT_PAREN);
      case ')' -> addToken(TokenType.RIGHT_PAREN);
      case '{' -> addToken(TokenType.LEFT_BRACE);
      case '}' -> addToken(TokenType.RIGHT_BRACE);
      case ',' -> addToken(TokenType.COMMA);
      case '.' -> addToken(TokenType.DOT);
      case '-' -> addToken(TokenType.MINUS);
      case '+' -> addToken(TokenType.PLUS);
      case ';' -> addToken(TokenType.SEMICOLON);
      case '*' -> addToken(TokenType.STAR);

      // 1 or 2 character lexemes
      case '!' -> addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
      case '=' -> addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
      case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
      case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.EQUAL);
      case '/' -> {
        if (match('/')) {
          while (peek() != '\n' && !isAtEnd()) advance();
        } else {
          addToken(TokenType.SLASH);
        }
      }

      // whitespace
      case ' ', '\r', '\t' -> {}
      case '\n' -> line++;

      // strings
      case '"' -> string();

      default -> {
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Lox.error(line, "Unexpected character '" + c + "'");
        }
      }
    }
  }

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') {
        line++;
      }
      advance();

      if (isAtEnd()) {
        Lox.error(line, "Unterminated string.");
        return;
      }

      // only get here with the closing "
      advance();

      String value = source.substring(start + 1, current - 1);
      addToken(TokenType.STRING, value);
    }
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if (type == null) {
      type = TokenType.IDENTIFIER;
    }
    addToken(type);
  }

  private void number() {
    // advance as long as we keep seeing digits
    while (isDigit(peek())) advance();

    // look for an optional fractional part
    if (peek() == '.' && isDigit(peekNext())) {
      advance();
      while (isDigit(peek())) advance();
    }

    String value = source.substring(start, current);
    addToken(TokenType.NUMBER, value);
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

  // match is like a conditional advance, it only consume the current
  // character if it's the one we were looking for
  private boolean match(char expected) {
    if (isAtEnd()) {
      return false;
    }
    if (source.charAt(current) != expected) {
      return false;
    }
    current++;
    return true;
  }

  private char peek() {
    if (isAtEnd()) {
      return '\0';
    }
    return source.charAt(current);
  }

  private char peekNext() {
    int next = current + 1;
    if (next >= source.length()) {
      return '\0';
    }
    return source.charAt(next);
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }
}
