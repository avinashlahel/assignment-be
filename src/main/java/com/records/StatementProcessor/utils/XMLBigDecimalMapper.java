package com.records.StatementProcessor.utils;

import com.records.StatementProcessor.exception.EmptyFieldException;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class XMLBigDecimalMapper extends XmlAdapter<String, BigDecimal> {

    @Override
    public BigDecimal unmarshal(String v) throws Exception {
        if(v==null || v.isEmpty())
            throw new EmptyFieldException("Empty field identified while parsing a node");

        return new BigDecimal(v).setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public String marshal(BigDecimal v) throws Exception {
        return v.toString();
    }
}
