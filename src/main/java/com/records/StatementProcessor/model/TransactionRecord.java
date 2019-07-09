package com.records.StatementProcessor.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.Objects;

@XmlRootElement(name = "record")
public class TransactionRecord {

    private int reference;
    private String accountNumber;
    private String description;
    private BigDecimal startBalance;
    private BigDecimal mutation;
    private BigDecimal endBalance;

    private String processingResult;

    public String getProcessingResult() {
        return processingResult;
    }

    public void setProcessingResult(String processingResult) {
        this.processingResult = processingResult;
    }

    @XmlAttribute(name = "reference")
    public int getReference() {
        return reference;
    }

    public void setReference(int reference) {
        this.reference = reference;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getStartBalance() {
        return startBalance;
    }

    public void setStartBalance(BigDecimal startBalance) {
        this.startBalance = startBalance;
    }

    public BigDecimal getMutation() {
        return mutation;
    }

    public void setMutation(BigDecimal mutation) {
        this.mutation = mutation;
    }

    public BigDecimal getEndBalance() {
        return endBalance;
    }

    public void setEndBalance(BigDecimal endBalance) {
        this.endBalance = endBalance;
    }

    @Override
    public String toString() {
        return "TransactionRecord{" +
                "reference=" + reference +
                ", accountNumber='" + accountNumber + '\'' +
                ", description='" + description + '\'' +
                ", processingResult='" + processingResult + '\'' +
                '}';
    }

    /*
        Two Records are considered equal if their Reference Number is same
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionRecord that = (TransactionRecord) o;
        return reference == that.reference;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference);
    }
}
