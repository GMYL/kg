package hb.kg.common.util.json.parser.deserializer;

import java.lang.reflect.Type;

import hb.kg.common.exception.JSONException;
import hb.kg.common.util.json.JSONPObject;
import hb.kg.common.util.json.parser.DefaultJSONParser;
import hb.kg.common.util.json.parser.JSONLexerBase;
import hb.kg.common.util.json.parser.JSONToken;
import hb.kg.common.util.json.parser.SymbolTable;

public class JSONPDeserializer implements ObjectDeserializer {
    public static final JSONPDeserializer instance = new JSONPDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser,
                            Type type,
                            Object fieldName) {
        JSONLexerBase lexer = (JSONLexerBase) parser.getLexer();
        SymbolTable symbolTable = parser.getSymbolTable();
        String funcName = lexer.scanSymbolUnQuoted(symbolTable);
        lexer.nextToken();
        int tok = lexer.token();
        if (tok == JSONToken.DOT) {
            String name = lexer.scanSymbolUnQuoted(parser.getSymbolTable());
            funcName += ".";
            funcName += name;
            lexer.nextToken();
            tok = lexer.token();
        }
        JSONPObject jsonp = new JSONPObject(funcName);
        if (tok != JSONToken.LPAREN) {
            throw new JSONException("illegal jsonp : " + lexer.info());
        }
        lexer.nextToken();
        for (;;) {
            Object arg = parser.parse();
            jsonp.addParameter(arg);
            tok = lexer.token();
            if (tok == JSONToken.COMMA) {
                lexer.nextToken();
            } else if (tok == JSONToken.RPAREN) {
                lexer.nextToken();
                break;
            } else {
                throw new JSONException("illegal jsonp : " + lexer.info());
            }
        }
        tok = lexer.token();
        if (tok == JSONToken.SEMI) {
            lexer.nextToken();
        }
        return (T) jsonp;
    }

    public int getFastMatchToken() {
        return 0;
    }
}
