package com.records.StatementProcessor.utils;

import com.records.StatementProcessor.exception.EmptyFieldException;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class XMLStringMapper extends XmlAdapter<String, String> {

    @Override
    public String unmarshal(String v) throws Exception {
        if(v==null || v.isEmpty())
            throw new EmptyFieldException("Empty field identified while parsing a node");

        return v;
    }

    @Override
    public String marshal(String v) throws Exception {
        return v;
    }
}
