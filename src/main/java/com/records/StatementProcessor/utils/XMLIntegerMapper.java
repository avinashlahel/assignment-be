package com.records.StatementProcessor.utils;

import com.records.StatementProcessor.exception.EmptyFieldException;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class XMLIntegerMapper extends XmlAdapter<String,Integer> {

    @Override
    public Integer unmarshal(String v) throws Exception {
        if(v==null || v.isEmpty())
            throw new EmptyFieldException("Empty field identified while parsing a node");

        return Integer.parseInt(v);
    }

    @Override
    public String marshal(Integer v) throws Exception {
        return v.toString();
    }
}
