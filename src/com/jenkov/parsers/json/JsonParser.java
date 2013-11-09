package com.jenkov.parsers.json;

import com.jenkov.parsers.core.DataCharBuffer;
import com.jenkov.parsers.core.IndexBuffer;
import com.jenkov.parsers.core.ParserException;

/**
 *
 */
public class JsonParser {

    private IndexBuffer   elementBuffer = null;
    private int           elementIndex  = 0;
    private JsonTokenizer jsonTokenizer = new JsonTokenizer();


    public void parse(DataCharBuffer dataBuffer, IndexBuffer indexBuffer) {
        this.elementBuffer = indexBuffer;
        this.elementIndex  = 0;
        this.jsonTokenizer.reinit(dataBuffer, new IndexBuffer(dataBuffer.data.length, true));

        parseObject(this.jsonTokenizer);
    }

    private void parseObject(JsonTokenizer tokenizer) {
        assertHasMoreTokens(tokenizer);
        tokenizer.parseToken();
        assertThisTokenType(tokenizer, TokenTypes.JSON_OBJECT_START);
        setElementData     (tokenizer, ElementTypes.JSON_OBJECT_START);

        tokenizer.nextToken();
        tokenizer.parseToken();

        while( tokenizer.tokenType() != TokenTypes.JSON_OBJECT_END) {
            assertThisTokenType(tokenizer, TokenTypes.JSON_STRING_TOKEN);
            setElementData(tokenizer, ElementTypes.JSON_PROPERTY_NAME);

            tokenizer.nextToken();
            tokenizer.parseToken();
            assertThisTokenType(tokenizer, TokenTypes.JSON_PROPERTY_NAME_VALUE_SEPARATOR);

            tokenizer.nextToken();
            tokenizer.parseToken();
            if(tokenizer.tokenType() == TokenTypes.JSON_STRING_TOKEN) {
                setElementData(tokenizer, ElementTypes.JSON_PROPERTY_VALUE);
            } else if(tokenizer.tokenType() == TokenTypes.JSON_ARRAY_START) {
                parseArray(tokenizer);
            }

            tokenizer.nextToken();
            tokenizer.parseToken();
            if(tokenizer.tokenType() == TokenTypes.JSON_PROPERTY_SEPARATOR) {
                tokenizer.nextToken();  //skip , tokens if found here.
                tokenizer.parseToken();
            }

        }
        setElementData(tokenizer, ElementTypes.JSON_OBJECT_END);



    }

    private void parseArray(JsonTokenizer tokenizer) {
        setElementData(tokenizer, ElementTypes.JSON_ARRAY_START);

        tokenizer.nextToken();
        tokenizer.parseToken();

        while(tokenizer.tokenType() != TokenTypes.JSON_ARRAY_END) {

            int tokenType = tokenizer.tokenType(); // extracted only for debug purposes.

            if(tokenizer.tokenType() == TokenTypes.JSON_STRING_TOKEN) {
                setElementData(tokenizer, ElementTypes.JSON_ARRAY_VALUE);
            } else if(tokenizer.tokenType() == TokenTypes.JSON_OBJECT_START) {
                parseObject(tokenizer);
            }

            tokenizer.nextToken();
            tokenizer.parseToken();
            tokenType = tokenizer.tokenType(); // extracted only for debug purposes.
            if(tokenizer.tokenType() == TokenTypes.JSON_PROPERTY_SEPARATOR) {
                tokenizer.nextToken();
                tokenizer.parseToken();
            }
        }

        setElementData(tokenizer, ElementTypes.JSON_ARRAY_END);
    }

    private void setElementData(JsonTokenizer tokenizer, byte elementType) {
        this.elementBuffer.position[this.elementIndex] = tokenizer.tokenPosition();
        this.elementBuffer.length  [this.elementIndex] = tokenizer.tokenLength();
        this.elementBuffer.type    [this.elementIndex] = elementType;
        this.elementIndex++;
    }

    private void assertThisTokenType(JsonTokenizer tokenizer, byte expectedTokenType) {
        if(tokenizer.tokenType() != expectedTokenType) {
            throw new ParserException("Token type mismatch: Expected " + expectedTokenType + " but found " + tokenizer.tokenType());
        }
    }


    private void assertHasMoreTokens(JsonTokenizer tokenizer) {
        if(! tokenizer.hasMoreTokens()) {
            throw new ParserException("Expected more tokens available in the tokenizer");
        }
    }


}
